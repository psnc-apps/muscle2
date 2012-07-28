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
		@parser.on("--cxa FILE", "file from which to load the cxa") {|arg| @env['cxa_file'] = File.expand_path(arg) }
		@parser.on("--tmp-path ARG", "set root of the tmp path where kernel output will go in dedicated subdirectories") {|arg| @env['tmp_path'] = File.expand_path(arg) }
		@parser.on("--allkernels", "automatically launches all kernels") { @env["allkernels"] = true }
		@parser.on("--mpi", "checks the MPI rank, and runs MUSCLE on rank 0, and calls the kernel 'execute()' on others") { @env['use_mpi'] = true }
		@parser.on("--version", "shows info about this MUSCLE version") do
		

			puts java("muscle.Version")
			exit true
		end

		# control chief lead head main central
		@parser.separator "Simulation Manager flags:"
		@parser.on("--main", "make this instance also a MUSCLE global Simulation Manager") { @env['main'] = true }
		@parser.on("--bindport PORT", "port where this manager would be listening") {|arg| @env['bindport'] = arg.to_i; }
		@parser.on("--bindaddr IPADDR", "bind address of the manager - TBD") {|arg| @env['bindaddr'] = arg; }
		@parser.on("--bindinf  interface", "bind interface of the manager (e.g. eth0) - TBD") {|arg| @env['bindinf'] = arg; }
		
		@parser.separator "Local Manager flags:"		
		@parser.on("--manager HOST:PORT", "IP or hostname:port where the MUSCLE Simulation Manager can be contacted") {|arg| @env['manager'] = arg; }
		@parser.on("--heap RANGE", MemoryRange, "set range for JVM heap size (e.g. 42m..2g)") do |mem_range|
			@env['Xms'] = mem_range.from_mem
			@env['Xmx'] = mem_range.to_mem
		end
		
		@parser.separator "MTO flags:"
		@parser.on("--intercluster", "use Muscle Transport Overlay") { @env['intercluster'] = true }
		@parser.on("--port-min ARG", "define lower bound of the port range used (inclusive)") { |arg| @env['port_min'] = arg }
		@parser.on("--port-max ARG", "define higher bound of the port range used (inclusive)") { |arg| @env['port_max'] = arg }
		@parser.on("--qcg", "enable cooperation with QosCosGrid services") { @env['qcg'] = true }
		@parser.on("--mto HOST:PORT", "IP/hostname and port where MTO can be contacted") {|arg| @env['mto'] = arg; }
		
		# jvm flags
		@parser.separator "JVM flags:"
		@parser.on("--classpath ARG", "set classpath for the JVM") {|arg| @env["CLASSPATH"] = arg }
		@parser.on("--logging-config FILE", "set custom logging configuration") {|arg| @env['logging_config_path'] = arg }
		@parser.on("--jvmflags ARR", "additional flags to be passed to the jvm (e.g. --jvmflags -da,-help,-Duser.language=en)", Array) {|arr| @env["jvmflags"] = arr; }

		# misc flags
		@parser.separator "misc flags:"
		@parser.on("-h", "--help") { puts @parser.help; exit }
		@parser.on("--print-env=[KEY0,KEY1,...]", Array, "prints the internal preferences, e.g. --print_env=CLASSPATH") {|val| if val.nil? then @env['print_env'] = true;else @env['print_env'] = val;end }
		@parser.on("-v", "--verbose") { @env['verbose'] = true }
		@parser.on("-d", "--debug", "produces more verbose logs and do not purges MUSCLE temporary directory") { @env['debug'] = true; @env['verbose'] = true }
		
		@parser.on("--quiet") { @env['quiet'] = true }
		@parser.on("-p", "--print", "print command to stdout but do not execute it") { @env['execute'] = false; @env['verbose'] = true }
		@parser.on("--native-tmp-file ARG", "temporary file to write host and port to when calling from native code") { |arg| @env['native_tmp_file'] = arg }

	end


	# returns remaining args and the cli env
	def parse(args)

		# parse CLI args
	
		@parser.parse!(args) rescue(puts $!;puts @parser.help; exit)
		
		return args, @env
	end
	
	
	#
	def help
		@parser.help
	end
	
	#
	attr_reader :parser
end# class Cli
