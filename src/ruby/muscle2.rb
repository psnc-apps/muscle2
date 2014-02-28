#! /usr/bin/env ruby

=begin
== Copyright and License
   Copyright 2008,2009 Complex Automata Simulation Technique (COAST) consortium
   Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project

   GNU Lesser General Public License

   This file is part of MUSCLE (Multiscale Coupling Library and Environment).

   MUSCLE is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   MUSCLE is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.

== Author
   Jan Hegewald
=end

# File will usually be symlinked: reference everything from the original file.
require 'timeout'
require 'utilities'
require 'cli2'
require 'cxa'
include MuscleUtils
require 'muscle.cls'

cli = MuscleCli.new

# see if we should do anything at all
if ARGV.size == 0
	puts cli.help
	exit
end

ARGV_COPY = ARGV.dup #this line is needed by --native mode
args, cli_env = cli.parse ARGV

m = Muscle.new

# add cli muscle env
m.add_env cli_env

# Add environment classpath and libpath to MUSCLE
if ENV['MUSCLE_CLASSPATH']
	m.add_classpath ENV['MUSCLE_CLASSPATH']
end

if ENV['MUSCLE_LIBPATH']
	m.add_libpath ENV['MUSCLE_LIBPATH']
end

if m.env['print_env'] != false
	# Only print the environment variables and quit
	m.print_env(m.env['print_env'])
	exit
end

if m.env.has_key?('cxa_file')
	begin
		# load CxA configuration
		cxa = m.load_cxa
	rescue LoadError
		puts "CxA file <#{m.env['cxa_file']}> not found. Modify the value of --cxa."
		exit 1
	end
else
	# No more useful actions without a CxA file
	puts 'No CxA file given; use --cxa. Aborting.'
	exit 1
end

m.stage_files
m.gzip_stage_files

# Generate the connection scheme file
cxa.generate_cs_file

instances = cxa.instances

if instances.empty?
	puts 'Specify at least one instance in the configuration file.'
	exit 1
end

def show_info(msg, instances, exit_value=1)
	puts "#{msg}Possible instance names:\n--------\n", instances.keys
	puts "--------\nTo run all instances use --allkernels."
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

if m.env.has_key?('qcg')
  if not m.env.has_key?('intercluster')
    puts "Warning: --qcg is specified but --intercluster is not. Ignoring --qcg.\n\n"
    m.env.delete('qcg')
  elsif not ENV.has_key?('QCG_COORDINATOR_URL')
    puts "QCG_COORDINATOR_URL environment variable must be set to use --qcg.\nAsk administrator for an appropriate value."
    exit 1
  elsif not ENV.has_key?('SESSION_ID')
    puts "SESSION_ID environment variable must be set to use --qcg.\nSet the same arbitrary but globally unique string for all parts of a single simulation."
    exit 1
  end
end

if active_instances.empty? and !m.env['main'] || m.env['use_mpi'] || m.env['native']
	# Unless we're only running main, we need to give an active instance
	show_info('No instance names given. ', instances)
elsif !active_instances.empty? and !m.env['main'] && !m.env['manager'] && !m.env['qcg']
	# if there are active instances, we need a way of contacting the manager.
	puts 'Either specify --main or give --manager contact information'
	exit 1
elsif active_instances.size > 1 and m.env['use_mpi']
	puts 'Multiple instances provided for MPI code. Aborting.'
	exit 1  
elsif active_instances.size > 1 and m.env['native']
	puts 'Warning: multiple instances provided for native code. Ignoring --native.'
	m.env.delete('native')
end

# Apply MTO arguments, if any, and if possible
if m.env.has_key?('intercluster') and not m.apply_intercluster
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
		puts 'Called with MPI argument, but the use of MPI is not detected. Aborting.'
		exit 1
	end

	puts "MPI RANK = #{rank}"
	# non-root rank
	if rank and rank.to_i > 0		
		runner = 'muscle.util.MpiSlaveKernelExecutor'
		m.exec_mpi(runner, [active_instances.first.cls])
	end
end

manager_pid = 0
$running_procs = {}

signals = ['HUP', 'INT', 'QUIT', 'ABRT', 'SYS',
	'PIPE', 'ALRM', 'TERM', 'TTIN', 'TTOU', 'XCPU', 'XFSZ',
	'PROF', 'USR1', 'USR2']
signals.each {|sig|
	Signal.trap(sig) do
		kill_processes($running_procs, 1)
		$running_procs = nil
	end
}

contact_addr = nil

if m.env['main']
	if active_instances.empty?
		puts 'Only starting MUSCLE2 Simulation Manager; not running the Simulation.'
	else
		puts 'Running both MUSCLE2 Simulation Manager and the Simulation'
	end

	muscle_main_class = 'muscle.manager.SimulationManager'
	if instances.size > 30
		instances_path = "#{m.muscle_tmp_path}/instances.param"
		File.open(instances_path, 'w') do |f|
			f.puts instances.keys.join("\n")
		end
		muscle_main_args = ['-f', instances_path]
	else
		muscle_main_args = instances.keys
	end
	
	if $running_procs != nil
		manager_pid = m.run_manager(muscle_main_class, muscle_main_args)
		if manager_pid != -1
			$running_procs[manager_pid] = 'Simulation Manager'
		end
	end
	
	if manager_pid == -1
		contact_addr = '[MANAGER_ADDRESS]'
	else
		if ENV.has_key?('MUSCLE_MANAGER_TIMEOUT')
			manager_timeout = ENV['MUSCLE_MANAGER_TIMEOUT'].to_i
		else
			manager_timeout = 30
		end
		
		begin
			Timeout::timeout(manager_timeout) do
        contact_addr = m.find_manager_contact(manager_pid)
      end
		rescue Timeout::Error
			puts "Simulation Manager did not run correctly after #{manager_timeout} seconds. Aborting."
			kill_processes($running_procs, 6)
			$running_procs = nil
		end
	end
end

if not active_instances.empty?
	muscle_local_class = 'muscle.client.LocalManager'
	if active_instances.size > 30
		instance_class_path = "#{m.muscle_tmp_path}/instance_classes.param"
		File.open(instance_class_path, 'w') do |f|
			f.puts active_instances.join("\n")
		end
		muscle_local_args = ['-f', instance_class_path]
	else
		muscle_local_args = active_instances
	end

	if $running_procs != nil
		pid = m.run_client(muscle_local_class, muscle_local_args, contact_addr)
		if pid != -1
			$running_procs[pid] = 'Simulation'
		end
	end
end

await_processes($running_procs)

exit 0
