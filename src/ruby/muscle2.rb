#! /usr/bin/env ruby

# == Copyright and License
# Copyright 2012 MAPPER Project
# 
# GNU Lesser General Public License
# 
# This file is part of MUSCLE (Multiscale Coupling Library and Environment).
# 
#     MUSCLE is free software: you can redistribute it and/or modify
#     it under the terms of the GNU Lesser General Public License as published by
#     the Free Software Foundation, either version 3 of the License, or
#     (at your option) any later version.
# 
#     MUSCLE is distributed in the hope that it will be useful,
#     but WITHOUT ANY WARRANTY; without even the implied warranty of
#     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#     GNU Lesser General Public License for more details.
# 
#     You should have received a copy of the GNU Lesser General Public License
#     along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
# 
#
#
# == Author
# Jan Hegewald
#

# File will usually be symlinked: reference everything from the original file.
if File.symlink? __FILE__
	PARENT_DIR = File.dirname(File.expand_path(File.readlink(__FILE__)))
else
	PARENT_DIR = File.dirname(File.expand_path(__FILE__))
end unless defined? PARENT_DIR
$LOAD_PATH << PARENT_DIR

require 'timeout'
require 'utilities'
require 'cli2'
require 'cxa'
include MuscleUtils
require 'uri'
require 'muscle.cls'

cli = MuscleCli.new

# see if we should do anything at all
if ARGV.size == 0
	puts cli.help
	exit
end

m = Muscle.new

ARGV_COPY = ARGV.dup #this line is needed by --native mode
args, cli_env = cli.parse ARGV

# add cli muscle env
m.add_env cli_env

# Add environment classpath and libpath to MUSCLE
if ENV["MUSCLE_CLASSPATH"]
	m.add_classpath ENV["MUSCLE_CLASSPATH"]
end

if ENV["MUSCLE_LIBPATH"]
	m.add_libpath ENV["MUSCLE_LIBPATH"]
end

if m.env['print_env'] != false
	# Only print the environment variables and quit
	m.print_env(m.env['print_env'])
	exit
end

if m.env.has_key?('cxa_file')
	# load CxA configuration
	cxa = Cxa.new(m.env['cxa_file'], m.env)
else
	# No more useful actions without a CxA file
	puts "--cxa option missing. Aborting"
	exit 1
end

if m.env['stage_files'].size == 1
	puts "Staging file #{m.env['stage_files'].first.inspect}"
elsif m.env['stage_files'].size > 1
	puts "Staging files #{m.env['stage_files'].inspect}"
end
m.env['stage_files'].push(m.env['cxa_file'])
m.stage_files

# Generate the connection scheme file
cxa.generate_cs_file

instances = cxa.instances

if instances.empty?
	puts "Specify at least one instance in the configuration file."
	exit 1
end

def show_info(msg, instances, exit_value=1)
	puts "#{msg}Possible instance names:\n--------\n", instances.keys
	puts "--------\nTo run all instances use the --allkernels flag."
	exit exit_value
end

# Instances specified on the command-line
active_instances = []
if m.env['allkernels']
	active_instances = instances.values
elsif args.size > 0
	unused = args - instances.keys
	if not unused.empty?
		show_info("Arguments #{unused} are not valid instance names. ", instances)
	end
	
	args.each {|arg| active_instances << instances[arg]}
end

if active_instances.empty? and !m.env['main'] || m.env['use_mpi'] || m.env['native']
	# Unless we're only running main, we need to give an active instance
	show_info("No instance names given. ", instances)
elsif !active_instances.empty? and !m.env['main'] && !m.env['manager'] && !m.env['qcg']
	# if there are active instances, we need a way of contacting the manager.
	puts "Either specify --main or give --manager contact information"
	exit 1
elsif active_instances.size > 1 and m.env['use_mpi']
	puts "Multiple instances provided for MPI code. Aborting."
	exit 1  
elsif active_instances.size > 1 and m.env['native']
	puts "Warning: multiple instances provided for native code. Ignoring the --native flag."
	m.env['native'] = false
end

# Apply MTO arguments, if any
if m.env.has_key?('intercluster') and not m.apply_intercluster
	puts "Intercluster was not properly specified."
	exit 1
end

at_exit {puts "\n\tExecuted in <#{Muscle.LAST.env['tmp_path']}>"}

if m.env['native']
	puts "Starting instance #{active_instances.first.name} in native mode (pid=#{Process.pid})"
	m.exec_native(active_instances.first.name, ARGV_COPY)
	# --native and --mpi are mutually exclusive, since --mpi runs through Java.
elsif m.env['use_mpi']
	# if using MPI, check rank and execute different command on all 
	rank = detect_mpi_rank

	if rank == nil
		puts "Called with MPI argument, but the use of MPI is not detected. Aborting."
		exit 1
	end

	puts "MPI RANK = #{rank}"
	# non-root rank
	if rank and rank.to_i > 0		
		runner = "muscle.util.MpiSlaveKernelExecutor"
		m.exec_mpi([runner, active_instances.first.cls])
	end
end

manager_pid = 0
$running_procs = {}

kill_running = lambda do
	kill_processes($running_procs, 1)
	$running_procs = nil
end

signals = ["HUP", "INT", "QUIT", "ILL", "ABRT", "FPE", "BUS", "SEGV", "SYS",
	"PIPE", "ALRM", "TERM", "TTIN", "TTOU", "XCPU", "XFSZ",
	"PROF", "USR1", "USR2"]
signals.each { |sig| Signal.trap(sig, kill_running) }

contact_addr = nil

if m.env['main']
	if active_instances.empty?
		puts "Only starting MUSCLE2 Simulation Manager; not running the Simulation."
	else
		puts "Running both MUSCLE2 Simulation Manager and the Simulation"
	end

	muscle_main_args = []
	muscle_main_args << "muscle.manager.SimulationManager"
	muscle_main_args << instances.keys
	if $running_procs != nil
		manager_pid = m.run_manager(muscle_main_args)
		$running_procs[manager_pid] = 'Simulation Manager'
	end

	begin
		Timeout::timeout(30) { contact_addr = m.find_manager_contact(manager_pid) }
	rescue Timeout::Error
		puts "Simulation Manager did not run correctly. Aborting."
		kill_processes($running_procs, 6)
		$running_procs = nil
	end
end

if not active_instances.empty?
	muscle_local_args = ["muscle.client.LocalManager"] + active_instances

	if $running_procs != nil
		pid = m.run_client(muscle_local_args, contact_addr)
		$running_procs[pid] = 'Simulation'
	end
end

await_processes($running_procs)

exit 0


