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


#module Muscle
if File.symlink? __FILE__
	PARENT_DIR = File.dirname(File.expand_path(File.readlink(__FILE__)))
else
	PARENT_DIR = File.dirname(File.expand_path(__FILE__))
end unless defined? PARENT_DIR
$LOAD_PATH << PARENT_DIR

require 'utilities'
require 'cli2'
require 'cxa'
include MuscleUtils
require 'uri'

#
class Muscle
	def initialize
		@@LAST = self
		@env = {}

		# set value for LIBPATHENV or abort
		assert_LIBPATHENV @env

		@env_basename = "muscle.env.rb"
		# load (machine specific) default env
		load_env(File.expand_path("#{PARENT_DIR}/#{@env_basename}"), true)
	end
	
	#
	def add_env(e)
		@env.merge!(e){|key, oldval, newval| 
				if(key == "CLASSPATH" && oldval != nil)
					oldval=newval+File::PATH_SEPARATOR+oldval
				else
					oldval=newval
				end}
	end

	# helper method to add path variables
	def add_path(hsh)
		hsh.each do |path_key,path|
			@env[path_key] = "" if @env[path_key] == nil
			if(path.class == Array)
				@env[path_key] = (@env[path_key].split(File::PATH_SEPARATOR) + path).join(File::PATH_SEPARATOR)
			else
				@env[path_key] = (@env[path_key].split(File::PATH_SEPARATOR) + path.split(File::PATH_SEPARATOR)).join(File::PATH_SEPARATOR)		
			end
			# delete any empty items
			@env[path_key] = ((@env[path_key].split(File::PATH_SEPARATOR)).delete_if {|x| x==''}).join(File::PATH_SEPARATOR)	
		end
	end


	def add_classpath(p)
		add_path("CLASSPATH"=>p)
	end


	def add_libpath(p)
		add_path("libpath"=>p)
		ENV[@env["LIBPATHENV"]] = @env["libpath"]
	end

	# overwrite env setting
	def set(hsh)
		hsh.each do |k,v|
			@env[k] = v
		end
	end

	def Muscle.jclass
		'muscle.Env'
	end
	
	def Muscle.LAST
		@@LAST
	end
			
	# visibility
	attr_reader :env, :env_basename
end


require 'pp'
# !!!: begin

cli = MuscleCli.new

# see if we should do anything at all
if ARGV.size == 0
#	RDoc::usage_no_exit('Synopsis')
	puts "This is the bootstrap utility for MUSCLE  2.0 (Multiscale Coupling Library and Environment)."
    puts "[1] http://apps.man.poznan.pl/trac/muscle"
    puts "[2] http://mapper-project.eu"
	puts cli.help
	exit
end

m = Muscle.new

args, cli_env = cli.parse ARGV


# add cli muscle env
m.add_env cli_env

# !!!: load cxa configuration
if m.env.has_key?('cxa_file')
	cxa = Cxa.new(m.env['cxa_file'], m.env)
end

#
if m.env['print_env'] != false
	if m.env['print_env'] == true
		# print the complete env (sorted)
		m.env.keys.sort.each {|k| puts "#{k.inspect}=>#{m.env[k].inspect}"}
	else
		# print value for the specified key(s)
		if(m.env['print_env'].size == 1)
			# print raw value if output is for a single key (useful if you want to further process the output, e.g. CLASSPATH)
			puts m.env[m.env['print_env'].first] if m.env.has_key? m.env['print_env'].first
		else
			m.env['print_env'].each {|k| puts "#{k.inspect}=>#{m.env[k].inspect}" if m.env.has_key? k}
		end
	end
	exit
end

unless cxa.nil?
  # dump cs file in legacy format
  File.open(cxa.env['muscle.core.ConnectionScheme legacy_cs_file_uri'].path, "w") do |f|
    f.puts "# DO NOT EDIT! This is file is generated automatically by <#{__FILE__}> at #{Time.now}"
    f.puts cxa.cs.to_s
  end
end

if(m.env.has_key?('intercluster'))
  PROP_PORT_RANGE_MIN = "pl.psnc.mapper.muscle.portrange.min"
  PROP_PORT_RANGE_MAX = "pl.psnc.mapper.muscle.portrange.max"
  PROP_MAIN_PORT = "pl.psnc.mapper.muscle.mainport"
  PROP_DEBUG = "pl.psnc.mapper.muscle.debug"
  PROP_TRACE = "pl.psnc.mapper.muscle.trace"
  PROP_MTO_ADDRESS = "pl.psnc.mapper.muscle.mto.address"
  PROP_MTO_PORT = "pl.psnc.mapper.muscle.mto.port"
  
  port_min = m.env['port_min'] || ENV['MUSCLE_PORT_MIN']
  port_max = m.env['port_max'] || ENV['MUSCLE_PORT_MAX']
  if(port_min.nil? or port_max.nil?)
	puts "Warning: intercluster specified, but no local port range given! Intercluster ignored."
  else
  
	mto =  m.env['mto'] || ENV['MUSCLE_MTO']
	if (! mto.nil?)
		mtoHost = mto.split(':')[0]
		mtoPort = mto.split(':')[1]
	end
	
	if(mtoPort.nil? or mtoHost.nil?)
	  puts "Warning: intercluster specified, but no MTO address/port given! Intercluster ignored."
	else
	  if(m.env.has_key?('qcg'))
	  	if (m.env['main'])
			m.env['localport'] = 22 #master
		else
			m.env['mainport'] = 22 #slave
		end
	  else
		m.env['localport'] = 0 
	  end
	  
	  if(m.env["jvmflags"].nil?)
		m.env["jvmflags"] = Array.new
	  end
	  
	  m.env["jvmflags"] << "-Dpl.psnc.muscle.socket.factory=muscle.net.CrossSocketFactory"
	  m.env["jvmflags"] << "-D" + PROP_PORT_RANGE_MIN + "=" + port_min
	  m.env["jvmflags"] << "-D" + PROP_PORT_RANGE_MAX + "=" + port_max
	  m.env["jvmflags"] << "-D" + PROP_MTO_ADDRESS    + "=" + mtoHost
	  m.env["jvmflags"] << "-D" + PROP_MTO_PORT       + "=" + mtoPort.to_s
	  
	end
  end
end


# if using MPI, check rank
if m.env['use_mpi']
	possible_mpi_rank_vars=["OMPI_MCA_orte_ess_vpid",
	                        "OMPI_MCA_ns_nds_vpid",
	                        "PMI_RANK",
	                        "MP_CHILD",
	                        "SLURM_PROCID",
	                        "X10_PLACE",
							"MP_CHILD"]
	rank = nil
	
	possible_mpi_rank_vars.each do |var|
		value = ENV[var]
		if value
			rank = value
		end
	end
 	puts "MPI RANK  = " + rank	
 	# non-root rank
	if rank and rank.to_i > 0
		
		unless cxa
			puts "No --cxa Aborting."
			exit 1
		end
		
		if args.size > 1
			puts "Multiple agents provided for MPI. Aborting."
			exit 1
		end
		
		runner = "utilities.MpiSlaveKernelExecutor"
		kernel = args[0]
		className = nil
		
		cxa.known_agents.each do |agent|
			if agent.name == kernel
				className = agent.cls
			end
		end
		
		command = JVM.build_command([runner, className], m.env).first

		puts command
		
		exit_value = run_command(command, m.env)
		
		exit exit_value if exit_value != nil
		exit
	end
end

if cxa == nil
	puts "--cxa option missing"
	exit 1
end

kernels = cxa.known_agents.find_all {|a| a.kind_of?(KernelAgent)}
kernels_names = kernels.collect {|k| k.name}

muscle_main_args = []
muscle_local_args = []

if m.env['main']
	muscle_main_args << "muscle.manager.SimulationManager"
	muscle_main_args << kernels_names
end

if m.env['allkernels'] || args.size > 0
	muscle_local_args << "muscle.core.LocalManager"	
	if m.env['manager']
		muscle_local_args << "-m"
		muscle_local_args << m.env['manager']
	elsif m.env['main']
		puts "Running both manager and kernels"
	else
		puts "no --manager contact information given"
		exit 1
	end
	if m.env['allkernels']
		muscle_local_args << kernels
	elsif args.size > 0
		args.each { |arg| kernels.each { |kernel| muscle_local_args << kernel if kernel.name == arg } }
	end
end

if muscle_main_args.size == 0 && muscle_local_args.size == 0
	puts "No kernel(s) name given"
	exit 1
end


at_exit {puts "\n\ttmp dir was: <#{Muscle.LAST.env['tmp_path']}>"}

manager_pid = 0
local_pid = 0

if muscle_main_args.size != 0
	command = JVM.build_command(muscle_main_args, m.env).first
	puts "Running MUSCLE2 Simulation Manager: " + command
	manager_pid = Process.fork {exec(command)}
end


if muscle_local_args.size != 0
	if manager_pid != 0
		contact_file_name = m.env['tmp_path'] + "/simulationmanager.#{manager_pid}.address"
		
		tries_count = 0
		
		while !File.exists?(contact_file_name)
			sleep 2
			if ++tries_count % 10 == 0
				puts "Waiting for manager contact file: #{contact_file_name}"
			end
		end
		
		while File.exists?(contact_file_name + ".lock") #waiting for lock file to disappear 
			sleep 2
		end
		
		contact_addr = File.open(contact_file_name, "rb").read
		puts "Using manager address: #{contact_addr}"
		muscle_local_args << "-m"
		muscle_local_args << contact_addr
	end
	command = JVM.build_command(muscle_local_args, m.env).first
	puts "Running MUSCLE2 Local Manager: " + command
	local_pid = Process.fork {exec(command)}
end

begin
	statuses = Process.waitall
	statuses.each { |status| exit status[1].exit if status[1].exitstatus !=0 } #exit with no zero value if only one process had non-zero exit value
rescue Interrupt
	puts "Interrupt received..."
	exit_val = 1	
	if manager_pid
    	puts "sending SIGINT signal to Simulation Manager (pid=#{manager_pid})"
        Process.kill("SIGINT", manager_pid)
    end
    
    if local_pid
    	puts "sending SIGINT signal to Local Manager (pid=#{local_pid})"
        Process.kill("SIGINT", local_pid)
    end
                
    begin
        Process.waitall
    rescue Interrupt
		if manager_pid
	    	puts "sending SIGKILL signal to Simulation Manager (pid=#{manager_pid})"
	        Process.kill("SIGKILL", manager_pid)
	    end
	    
	    if local_pid
	    	puts "sending SIGKILL signal to Local Manager (pid=#{local_pid})"
	        Process.kill("SIGKILL", local_pid)
	    end
    end
    
    Process.waitall
    exit 1
end

exit 0


