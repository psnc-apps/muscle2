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
		@parser.separator "MUSCLE flags:"
		@parser.on("-c","--cxa FILE", "MUSCLE configuration file to run with") {|arg| @env['cxa_file'] = File.expand_path(arg) }
		@parser.on("-p","--tmp-path ARG", "path where MUSCLE and kernel output will go") {|arg| @env['tmp_path'] = mkTmpPath(arg) }
		@parser.on("-a","--allkernels", "launch all kernels") { @env["allkernels"] = true }
		@parser.on("-n","--native", "start kernel binary first (for standalone native kernels)") { @env['native'] = true }
		@parser.on("-N","--native-tmp-file ARG", "file to write host and port to when communicating with native code") { |arg| @env['native_tmp_file'] = arg }
		@parser.on("--mpi", "runs MUSCLE on MPI rank 0, and calls the Java 'execute()' method on other ranks") { @env['use_mpi'] = true }

		# control chief lead head main central
		@parser.separator "Simulation Manager flags:"
		@parser.on("-m","--main", "run Simulation Manager here") { @env['main'] = true }
		@parser.on("--bindport PORT", "port where this manager will be listening") {|arg| @env['bindport'] = arg.to_i; }
		@parser.on("--bindaddr IPADDR", "bind address of the manager and/or simulation") {|arg| @env['bindaddr'] = arg; }
		@parser.on("--bindinf  interface", "bind interface of the manager and/or simulation (e.g., eth0)") {|arg| @env['bindinf'] = arg; }
		
		@parser.separator "Local Manager flags:"
		@parser.on("-M","--manager HOST:PORT", "IP or hostname:port of the Simulation Manager") {|arg| @env['manager'] = arg; }
		@parser.on("-H","--heap RANGE", MemoryRange, "set range for Java Virtual Machine heap size (e.g., 42m..2g; default: 256m..2048m)") do |mem_range|
			@env['Xms'] = mem_range.from_mem
			@env['Xmx'] = mem_range.to_mem
		end
		
		@parser.separator "Muscle Transport Overlay (MTO) flags:"
		@parser.on("-i","--intercluster", "use MTO") { @env['intercluster'] = true }
		@parser.on("--port-min ARG", "define lower bound of the port range used, inclusive (default: $MUSCLE_PORT_MIN)") { |arg| @env['port_min'] = arg }
		@parser.on("--port-max ARG", "define higher bound of the port range used, inclusive (default: $MUSCLE_PORT_MAX)") { |arg| @env['port_max'] = arg }
		@parser.on("--mto HOST:PORT", "IP/hostname and port where MTO can be contacted (default: $MUSCLE_MTO)") {|arg| @env['mto'] = arg; }
		@parser.on("--qcg", "enable cooperation with QosCosGrid services") { @env['qcg'] = true }
		
		# jvm flags
		@parser.separator "Java Virtual Machine flags:"
		@parser.on("-C","--classpath ARG", "set the Java classpath") {|arg| @env["CLASSPATH"] = arg }
		@parser.on("-L","--logging-config FILE", "set custom logging configuration") {|arg| @env['logging_config_path'] = arg }
		@parser.on("-D","--jvmflags ARR", "additional flags to be passed to the JVM (e.g., --jvmflags -da,-help,-Duser.language=en)", Array) {|arr| @env["jvmflags"] = arr; }

		# misc flags
		@parser.separator "misc flags:"
		@parser.on("-h", "--help") { puts @parser.help; exit }
		@parser.on("-V","--version", "MUSCLE version") do
			puts java("muscle.Version")
			exit true
		end
		@parser.on("--print-env=[KEY0,KEY1,...]", Array, "prints the internal preferences (e.g., --print_env=CLASSPATH)") {|val| if val.nil? then @env['print_env'] = true;else @env['print_env'] = val;end }
		@parser.on("-v", "--verbose", "verbose logging") { @env['verbose'] = true }
		@parser.on("-d", "--debug", "verbose logging and does not purge MUSCLE temporary directory") { @env['debug'] = true; @env['verbose'] = true }
		
		@parser.on("-q","--quiet", "do not show output from MUSCLE") { @env['quiet'] = true }
		@parser.on("--print", "print command to stdout but do not execute it") { @env['execute'] = false; @env['verbose'] = true }
	end


	# returns remaining args and the cli env
	def parse(args)

		# parse CLI args
	
		@parser.parse!(args) rescue(puts $!;puts @parser.help; exit)
		
		return args, @env
	end

	#
	def help
	  puts "This is the bootstrap utility for MUSCLE  2.0 (Multiscale Coupling Library and Environment)."
    puts "[1] http://apps.man.poznan.pl/trac/muscle"
    puts "[2] http://www.mapper-project.eu/"
		@parser.help
	end
	
	#
	attr_reader :parser
end
