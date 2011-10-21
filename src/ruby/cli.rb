=begin
== Copyright and License
Copyright 2008,2009 Complex Automata Simulation Technique (COAST) consortium

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

PARENT_DIR = File.dirname(File.expand_path(__FILE__)) unless defined? PARENT_DIR
$LOAD_PATH << PARENT_DIR


class MuscleCli
	#
	def initialize
		require 'optparse'
		#RUBY_VERSION < "1.9" ? (require 'rdoc/usage') : (require 'rdoc')
		require 'memoryrange'

		# build our cli-args parser
		@env = {}
		@parser = OptionParser.new
		
		# add Java heap ranges as an option type
		@parser.accept(MemoryRange, /(\d+)([kKmMgGtT])\.\.(\d+)([kKmMgGtT])/) do |mem_range, from, from_quantity, to, to_quantity|
			MemoryRange.new(from, from_quantity, to, to_quantity)
		end
		
		#@parser.banner += "\nExample: ruby #{File.basename($0)} --main plumber --cxa_file path/to/cxa.rb"
		@parser.banner += "\nExample: muscle --main plumber --cxa_file path/to/cxa.rb"

		# MUSCLE flags
		@parser.separator "MUSCLE flags:"
		@parser.on("--cxa_file FILE", "file from which to load the cxa") {|arg| @env['cxa_file'] = File.expand_path(arg) }
		#@parser.on("--auto", "run full CxA from this command") { @env['auto'] = true }
		#@parser.on("--gui", "enable MUSCLE GUI -- all agents will try to send visualization data to a dedicated GUI agent") { @env['gui'] = true }
		@parser.on("--sandbox ARG", "run a single given kernel in a sanbox environment") {|arg| @env['sandbox'] = arg }
		#@parser.on("--kernelinfo", "print info about predefined kernels for a cxa") {|arg| @env["kernelinfo"] = true }
		@parser.on("--tmp_path ARG", "set root of the tmp path where kernel output will go in dedicated subdirectories") {|arg| @env['tmp_path'] = File.expand_path(arg) }
		@parser.on("--allkernels", "automatically launches all kernels") { @env["allkernels"] = true }
		@parser.on("--autoquit", "automatically quits platform using the muscle.utilities.agent.QuitMonitor agent") { @env["autoquit"] = true }
		@parser.on("--mpi", "checks the MPI rank, and runs MUSCLE on rank 0, and calls the kernel 'execute()' on others") { @env['use_mpi'] = true }
		@parser.on("--version", "shows info about this MUSCLE version") do

			puts java("muscle.Version")
			exit true
		end

#		@parser.on_tail("--config_file_path ARG", "overwrite environment variable for CONFIG_FILE_PATH to load a custom configuration file") {|arg| @env['CONFIG_FILE_PATH'] = arg}

		# JADE flags
		# control chief lead head main central
		@parser.separator "JADE flags:"
		@parser.on("--mainhost HOST", "IP or hostname where the main container lives") {|arg| @env['host'] = arg; }
		@parser.on("--mainport PORT", "port where the main container of this cxa platform should be contacted (required for all participating containers)") {|arg| @env['mainport'] = arg.to_i; }
		@parser.on("--localhost HOST", "IP or hostname where this container lives") {|arg| @env['localhost'] = arg; }
		@parser.on("--localport PORT", "port where this container should be contacted") {|arg| @env['localport'] = arg.to_i; }
		@parser.on("--main", "make this container the JADE main container and boot the AMS (white pages) and DF (yellow pages) agents") { @env['main'] = true }
		@parser.on("--container NAME", "name of container to start, ignored if --main option is present") {|arg| @env['container_name'] = arg; }
		@parser.on("--nojade", "launch a plain Java class without starting jade") { @env['jade'] = false }
		@parser.on("--jadegui", "launch the Jade-RMA GUI agent") { @env['rmagui'] = true }
		#@parser.on("--leap", "use JADE-LEAP") { @env['leap'] = true }
		
		@parser.separator "MTO flags:"
		@parser.on("--intercluster", "uses Muscle Transport Overlay") { @env['intercluster'] = true }
		@parser.on("--port_min ARG", "defines lower bound of the port range used (inclusive)") { |arg| @env['port_min'] = arg }
		@parser.on("--port_max ARG", "defines higher bound of the port range used (inclusive)") { |arg| @env['port_max'] = arg }
		@parser.on("--qcg", "enables cooperation with QosCosGrid services (forces local port)") { @env['qcg'] = true }
		@parser.on("--mtohost HOST", "IP or hostname where MTO lives") {|arg| @env['mtohost'] = arg; }
		@parser.on("--mtoport PORT", "port where MTO should be contacted") {|arg| @env['mtoport'] = arg.to_i; }
		
		# jvm flags
		@parser.separator "JVM flags:"
		@parser.on("--classpath ARG", "set classpath for the JVM") {|arg| @env["CLASSPATH"] = arg }
		@parser.on("--heap RANGE", MemoryRange, "set range for JVM heap size (e.g. 42m..2g)") do |mem_range|
			@env['Xms'] = mem_range.from_mem
			@env['Xmx'] = mem_range.to_mem
		end
		@parser.on("--logging_config_path FILE", "set logging configuration") {|arg| @env['logging_config_path'] = arg }
		@parser.on("--jvmflags ARR", "additional flags to be passed to the jvm (e.g. --jvmflags -da,-help,-Duser.language=en)", Array) {|arr| @env["jvmflags"] = arr; }

		# misc flags
		@parser.separator "misc flags:"
		@parser.on("-h", "--help") { RDoc::usage_no_exit('Synopsis');puts @parser.help; exit }
		@parser.on("--print_env=[KEY0,KEY1,...]", Array, "prints the internal preferences, e.g. --print_env=CLASSPATH") {|val| if val.nil? then @env['print_env'] = true;else @env['print_env'] = val;end }
		@parser.on("-v", "--verbose") { @env['verbose'] = true }
		@parser.on("--quiet") { @env['quiet'] = true }
		@parser.on("-p", "--print", "print command to stdout but do not execute it") { @env['execute'] = false; @env['verbose'] = true }
		@parser.on("--test [ARG]", Array, "run a(ll) test(s), implies --main") do |arg|
			@env['test'] = true
			@env['testinputs'] = arg
			puts arg
		end
	end


	# returns remaining args and the cli env
	def parse(args)

		# parse CLI args
		# in case we just want to launch a plain java class, pass any remaining args to that programme
		additional_args = []
		if( args.include?("--nojade") )
			i = args.index("--nojade")
			additional_args = args[i+1..-1] # everything after the --nojade
			args = args[0..i]
		end
		@parser.parse!(args) rescue(puts $!;puts @parser.help; exit)
		args = args + additional_args
		
		return args, @env
	end
	
	
	#
	def help
		@parser.help
	end
	
	
	#
	attr_reader :parser
end# class Cli
