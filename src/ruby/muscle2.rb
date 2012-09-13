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
require 'pp'
# !!!: begin

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

# Generate the connection scheme file
cxa.generate_cs_file

kernels = cxa.get_kernels
kernel_names = kernels.collect {|k| k.name}

if kernels.empty?
	puts "Specify at least one kernel in the configuration file."
	exit 1
end

# Kernels specified on the command-line
active_kernels = []
if m.env['allkernels']
	active_kernels = kernels
elsif args.size > 0
	args.each { |arg| kernels.each { |kernel| active_kernels << kernel if kernel.name == arg } }
end

if active_kernels.empty? and !m.env['main'] || m.env['use_mpi'] || m.env['native']
  # Unless we're only running main, we need to give an active kernel
	puts "No kernel names given. Possible kernel names:\n--------\n", kernel_names
	puts "--------\nTo run all kernels use the --allkernels flag."
	exit 1
elsif !active_kernels.empty? and !m.env['main'] && !m.env['manager'] && !m.env['qcg']
  # if there are active kernels, we need a way of contacting the manager.
  puts "Either specify --main or give --manager contact information"
	exit 1
elsif active_kernels.size > 1 and m.env['use_mpi'] || m.env['native']
	puts "Multiple kernels provided for native code. Aborting."
	exit 1  
end

# Apply MTO arguments, if any
if m.env.has_key?('intercluster')
  if not m.apply_intercluster
		puts "Intercluster was not properly specified. Aborting."
		exit 1
	end
end

at_exit {puts "\n\tExecuted in <#{Muscle.LAST.env['tmp_path']}>"}

if m.env['native']
  puts "starting kernel " + active_kernels.first.name + " in native mode"
	m.exec_native(active_kernels.first.name, ARGV_COPY)
# --native and --mpi are mutually exclusive, since --mpi runs through Java.
elsif m.env['use_mpi']
  # if using MPI, check rank and execute different command on all 
  rank = detect_mpi_rank
	
	if rank == nil
		puts "Called with MPI argument, but the use of MPI is not detected. Aborting."
		exit 1
	end

 	puts "MPI RANK  = " + rank	
 	# non-root rank
	if rank and rank.to_i > 0		
		runner = "muscle.util.MpiSlaveKernelExecutor"
		m.exec_mpi([runner, active_kernels.first.cls])
	end
end

manager_pid = 0
$running_procs = []

kill_running = lambda do
  kill_processes($running_procs)
  $running_procs = nil
end
signals = ["SEGV", "INT", "TERM", "QUIT", "ABRT"]
signals.each { |sig| Signal.trap(sig, kill_running) }

if m.env['main']
	if active_kernels.empty?
		puts "Only starting MUSCLE2 Simulation Manager; not running the Simulation."
	else
	  puts "Running both MUSCLE2 Simulation Manager and the Simulation"
	end

  muscle_main_args = []
	muscle_main_args << "muscle.manager.SimulationManager"
	muscle_main_args << kernel_names
  if $running_procs != nil
	  manager_pid = m.run_manager(muscle_main_args)
	  $running_procs << {:pid => manager_pid, :name => 'Simulation Manager'}
  end
end

if !active_kernels.empty?	
	
	muscle_local_args = []
  muscle_local_args << "muscle.client.LocalManager"
	muscle_local_args = muscle_local_args + active_kernels

  contact_file_name = m.env['tmp_path'] + "/simulationmanager.#{manager_pid}.address"
  if $running_procs != nil
	  pid = m.run_client(muscle_local_args, contact_file_name)
  	$running_procs << {:pid => pid, :name => 'Simulation'}
  end
end

kill_processes($running_procs)

exit 0


