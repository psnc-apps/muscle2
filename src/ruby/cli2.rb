=begin
Copyright and License
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

Author
Jan Hegewald
Mariusz Mamonski
=end

require 'utilities'
include MuscleUtils

PARENT_DIR = File.dirname(File.expand_path(__FILE__)) unless defined? PARENT_DIR
$LOAD_PATH << PARENT_DIR

class MuscleCli
	#
	def initialize
		require 'optparse'
		require 'memoryrange'

		# build our cli-args parser
		@env = {}
		@parser = OptionParser.new
	
		# add Java heap ranges as an option type
		@parser.accept(MemoryRange, /(\d+)([kKmMgGtT])\.\.(\d+)([kKmMgGtT])/) do |mem_range, from, from_quantity, to, to_quantity|
			MemoryRange.new(from, from_quantity, to, to_quantity)
		end

		@parser.banner += "\nExample: muscle2 --cxa /opt/muscle/share/muscle/examples/cxa/SimpleExample.cxa.rb --main r w"

		# MUSCLE flags
		@parser.separator "\nMain"
		@parser.on("-c","--cxa FILE", "MUSCLE configuration file to execute") {|arg| @env['cxa_file'] = File.expand_path(arg) }
		@parser.on("-m","--main", "run Simulation Manager here") { @env['main'] = true }
		@parser.on("-M","--manager HOST:PORT", "IP address/hostname and port of the Simulation Manager") {|arg| @env['manager'] = arg; }
		@parser.on("-a","--allkernels", "launch all kernels") { @env["allkernels"] = true }
		@parser.on("-p","--tmp-path ARG", "path where MUSCLE and model output will go") {|arg| @env['tmp_path'] = mkTmpPath(arg) }
		@parser.on("-s","--stage PATHS", "stage in files or directories to tmp path before running, separated by colons ':'") { |arg| @env['stage_files'] = arg.split(":") }
		@parser.on("-z","--gzip-stage PATHS", "stage in zipped files or directories to tmp path before running, separated by colons ':'") { |arg| @env['gzip_stage_files'] = arg.split(":") }

		@parser.separator "\nAlternative execution"
		@parser.on("--mpi", "runs MUSCLE on MPI rank 0, and calls the Java 'execute()' method on other ranks") { @env['use_mpi'] = true }
		@parser.on("-n","--native", "start submodel binary first (for standalone native binary)") { @env['native'] = true }
		@parser.on("-N","--native-tmp-file ARG", "file to write host and port to when communicating with native code") { |arg| @env['native_tmp_file'] = arg }

		# Networking flags
		@parser.separator "\nNetworking"
		@parser.on("--port-min ARG", "lower bound of the port range used to listen for connections, inclusive (default: $MUSCLE_PORT_MIN or 9000)") { |arg| @env['port_min'] = arg }
		@parser.on("--port-max ARG", "higher bound of the port range used to listen for connections, inclusive (default: $MUSCLE_PORT_MAX or 9500)") { |arg| @env['port_max'] = arg }
		@parser.on("--bindport PORT", "listening port of the simulation manager (default: --port-min)") {|arg| @env['bindport'] = arg.to_i; }
		@parser.on("--bindaddr IPADDR", "address for listening to connections (default: current host name)") {|arg| @env['bindaddr'] = arg; }
		@parser.on("--bindinf  INTERFACE", "interface for listening to connections (e.g., eth0)") {|arg| @env['bindinf'] = arg; }

		@parser.separator "\nMuscle Transport Overlay (MTO)"
		@parser.on("-i","--intercluster", "use MTO") { @env['intercluster'] = true }
		@parser.on("--mto HOST:PORT", "IP address/hostname and port of the MTO (default: $MUSCLE_MTO)") {|arg| @env['mto'] = arg; }
		@parser.on("--qcg", "enable cooperation with QosCosGrid services") { @env['qcg'] = true }
		
		# jvm flags
		@parser.separator "\nJava Virtual Machine"
		@parser.on("-C","--classpath ARG", "set the Java classpath") {|arg| @env["CLASSPATH"] = arg }
		@parser.on("-L","--logging-config FILE", "use given Java logging configuration") {|arg| @env['logging_config_path'] = arg }
		@parser.on("-H","--heap RANGE", MemoryRange, "range for Java Virtual Machine heap size of the simulation (e.g., 42m..2g; default: 256m..2048m)") do |mem_range|
			@env['Xms'] = mem_range.from_mem
			@env['Xmx'] = mem_range.to_mem
		end
		@parser.on("-D","--jvmflags ARR", "additional flags to be passed to the JVM (e.g., --jvmflags -da,-help,-Duser.language=en)", Array) {|arr| @env["jvmflags"] = arr; }

		# misc flags
		@parser.separator "\nOther"
		@parser.on("-v", "--verbose", "verbose logging") { @env['verbose'] = true }
		@parser.on("-q","--quiet", "do not show output from MUSCLE") { @env['quiet'] = true }
		@parser.on("--print", "print command to stdout but do not execute it") { @env['execute'] = false; @env['verbose'] = true }
		@parser.on("--print-env=[KEY0,KEY1,...]", Array, "prints the internal preferences (e.g., --print_env=CLASSPATH)") {|val| if val.nil? then @env['print_env'] = true;else @env['print_env'] = val;end }
		@parser.on("-V","--version", "MUSCLE version") do
			info
			puts java("muscle.Version")
			exit 0
		end
		@parser.on("-h", "--help", "print help message") { help; exit }
	end


	# returns remaining args and the cli env
	def parse(args)
		# parse CLI args
		@parser.parse!(args) rescue(puts $!; help; exit)
		
		return args, @env
	end

	def info
		puts "This is the bootstrap utility for MUSCLE 2.0 (Multiscale Coupling Library and Environment)."
		puts "[1] http://apps.man.poznan.pl/trac/muscle"
		puts "[2] http://www.mapper-project.eu/\n\n"
	end

	def help
		info
		puts @parser.help
	end
	
	attr_reader :parser
end
