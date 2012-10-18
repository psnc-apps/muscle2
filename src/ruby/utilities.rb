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

module MuscleUtils
	# run plain java with muscle classpath applied
	def java(args)
		cp = default_classpaths.join(File::PATH_SEPARATOR)
		cmd = "java -cp #{cp} #{args}"
		`#{cmd}`.chomp
	end
	
	
	# returns an array with cpasslaths
	def default_classpaths

		base_dir = ENV["MUSCLE_HOME"]

		# configure java CLASSPATH
		# classpath must include jade and muscle classes
		# remember: path separator is ';' on windows systems, else ':' better just use File::PATH_SEPARATOR
		# be careful: ENV["CLASSPATH"] might be nil or an empty string
		cp = []
		cp += ENV["CLASSPATH"].split(File::PATH_SEPARATOR) if ENV["CLASSPATH"] != nil
		cp += Dir.glob("#{base_dir}/share/muscle/java/*.jar")
		cp += Dir.glob("#{base_dir}/share/muscle/java/thirdparty/*.jar")
		cp
	end

	# test range of port number
	def assert_port(p, name="port")
		port_range = 0...2**16
		abort "#{name} <#{p}> out of range [#{port_range.min}--#{port_range.max}]" unless port_range === p.to_i
	end
		
	# load env files
	def load_env(path,mandatory=false)
		base_load_path = File.expand_path(path)
		machine = `hostname -s`.chomp
		machine_load_path = File.join(File.dirname(base_load_path), "#{File.basename(base_load_path, File.extname(base_load_path))}.#{machine}#{File.extname(base_load_path)}")

		load_path = base_load_path
		load_path = machine_load_path if File.readable?(machine_load_path)
		
		begin
			#eval( File.read(load_path), binding, load_path )
			load(load_path)
		rescue LoadError
			if mandatory
				abort($!)
			end
		end
	end	
	
	# determine current host os
	def host_os
		require 'rbconfig'

		case Config::CONFIG['host_os']
			when /mswin|windows/i
				"windows"
			when /linux/i
				"linux"
			when /darwin/i
				"darwin"
			when /sunos|solaris/i
				"solaris"
			else
				"unknown"
		end
	end

	# set value for LIBPATHENV or abort
	def assert_LIBPATHENV env
		if(env["LIBPATHENV"] == nil)
			# try fo figure out the lib path environment key automatically
			require 'rbconfig'
			env["LIBPATHENV"] = 
				if(ENV['LIBPATHENV'] != nil)
					ENV['LIBPATHENV']
				elsif( Config::CONFIG['LIBPATHENV'] != nil ) # is nil on mswin or with jruby
					Config::CONFIG['LIBPATHENV']
				else
					case host_os
						when "windows"
							"PATH"
						when "linux"
							"LD_LIBRARY_PATH"
						when "darwin"
							"DYLD_LIBRARY_PATH"
						else
							nil
					end
				end
		
			# print an error if we can not determine the LIBPATHENV
			if(env["LIBPATHENV"] == nil)
				abort("Error: can not determine value of environment key for LIBPATHENV, set an environment variable 'LIBPATHENV' to your preferred value (the value is usually something like 'LD_LIBRARY_PATH', 'DYLD_LIBRARY_PATH' or 'PATH', an environment path variable from where to load dynamic native libraries)")
			end			
		end
		env
	end

#
class TmpDir
	def TmpDir.create(name, rm=true)
		require 'tmpdir'
		require 'fileutils'
		path = File.join(Dir.tmpdir, "#{name}#{Time.now.to_f}")
		FileUtils::mkdir(path)
		at_exit {FileUtils::rm_rf(path) if File.directory?(path)} if rm
		path # return
	end
end


# build a tmp path which is unique per user and per jvm
# e.g. /tmp/UID_TIME_PID/
def mkJVMTmpPath
	require 'tmpdir'
	require 'fileutils'
	subdir_name = Time.now.strftime("#{Etc.getlogin}_%Y%m%d%H%M%S_#{Process.pid}")
	tmpdir_path = File.join(Dir.tmpdir, subdir_name)
	FileUtils::mkdir_p(tmpdir_path); # mkdir_p will not tell us if the dir already exists
	puts "tmp dir not empty: <#{tmpdir_path}>" if (Dir.entries(tmpdir_path)-[".", ".."]).size > 0
	tmpdir_path
end

#
def writeInitialInfo(path, cmd, env)
	$STARTTIME = Time.now
	File.open(path, "w") do |f|
		f.puts "this is file <#{path}> crreated by <#{__FILE__}>"
		f.puts "start date: #{Time.now}"
		f.puts "cwd: #{Dir.pwd}"
		f.puts "command: #{$0} #{ARGV.join(' ')}"
		f.puts "config file: #{env['cxa_file']}"
		f.puts "sub command: #{cmd}"
		f.puts
		f.puts "executing ..."
	end
end


#
def writeClosingInfo(path)
	File.open(path, "a") do |f|
		f.puts
		f.puts "... terminating"
		f.puts
		f.puts "uptime: #{Time.now-$STARTTIME} s"
		f.puts "end date: #{Time.now}"
	end
end


##
#class Agent
#	def initialize(name, className, args)
#		@name = name
#		@className = className
#		@args	= args
#	end
#	
#	def to_s
#		if @args.size > 0
#			joinText = " "
#			joinText = "," if $prefs['leap']
#			@name+":"+@className+"("+@args.join(joinText)+")"
#		else
#			@name+":"+@className
#		end
#	end
#	
#end


	# the very root of all the sources
	def find_src_root
		path = nil
		if ENV['MUSCLE_SRC_ROOT']
			path = ENV['MUSCLE_SRC_ROOT']
		elsif File.symlink?(__FILE__) # try from dir where this skript is
			path = File.dirname(File.readlink(__FILE__))
			path = File.join(path, '..')
		else
			path = File.dirname(__FILE__)
			path = File.join(path, '..')
		end

		File.expand_path(path)
	end

def run_command(cmd, env = {})

	if env['verbose']
		puts "executing command:" if env['execute']
		puts cmd
	end
	
	exitval = nil
	if env['execute']
		if env.has_key?('tmp_path')
			infoPath = "#{env['tmp_path']}/info.MUSCLE_bootstrap.txt"
			writeInitialInfo(infoPath, cmd, env)
		end
	
		# workaround to clean up after jade ams has finished
		if((cmd =~ / -container/) == nil)
			if((cmd =~ /#{env["bootclass"]}/) != nil)
				# we assume this is a command to launch a JADE main container
				# in this case the default ams agent will be created by JADE
				# this agent will create a file APDescription.txt in -file-dir or pwd
				# (note there is currently a bug in JADE which will not add a directory separator in case it is missing from the -file-dir property
				# see ams.java and my email to the JADE mailing list from April 8th 2008)
				jadeFileDir = env['tmp_path'] if env.has_key?('tmp_path')
				cleanupfiles = %w(APDescription.txt)
				# set alsolute paths
				cleanupfiles.collect! {|n| File.join(jadeFileDir,n)}
				# do not try to delete a file which actually exists before we run our command
				cleanupfiles.collect! {|p| File.exist?(p) ? nil:p}
				cleanupfiles.compact!
				at_exit {cleanupfiles.each {|n| File.delete(n) if File.exist?(n)}}
			end
		end
		
#		begin
#			pid = Process.pid
#			puts "[#{Process.pid}] executing pid: #{pid}"
#			system(cmd) # commands output will go where the output this ruby script goes
#			exitval = $?.exitstatus if($? != nil)
#		rescue Interrupt # not working
#			puts "interrupting #{Process.pid}, #{pid}"
#			exitval = 1
#		end

		# Process.fork does not work on windows, the win32-process gem does not help with that (from the readme: IMPORTANT! Note that because fork is calling CreateProcess($PROGRAM_NAME), it will start again from the top of the script instead of from the point of the call. We will try to address this in a future release, if possible.)
		if not host_os.include?('windows')
			begin
				# note: fork may not work with windows!
				pid = Process.fork {exec(cmd)} # commands output will go where the output this ruby script goes
				puts "[#{Process.pid}] executing pid: #{pid}"
				Process.wait
				exitval = $?.exitstatus if($? != nil)
			rescue Interrupt
				puts "sending INT signal to #{Process.pid}, #{pid}"
				exitval = 1
#				Process.kill("SIGKILL", pid) # sigkill does not run java shutdown hooks anymore
				Process.kill("SIGINT", pid)
				begin
					Process.wait
				rescue Interrupt
					puts "sending QUIT signal to #{Process.pid}, #{pid}"
					Process.kill("SIGQUIT", pid)
					Process.wait
				end
			end
		else
			begin
				pid = Process.pid
				puts "[#{Process.pid}] executing pid: #{pid}"
				system(cmd) # commands output will go where the output this ruby script goes
				exitval = $?.exitstatus if($? != nil)
			rescue Interrupt # not working
				puts "interrupting #{Process.pid}, #{pid}"
				exitval = 1
			end		
		end

		if env.has_key?('tmp_path')
			writeClosingInfo(infoPath)
		end
	end

	return exitval
end


#
def assert_path(path, line)
	if not File.readable?(path)
		abort("Error: <#{path}> is not readable (#{line})")
	end
	true
end

# as assert_path, but must be writable
def assert_path_w(path, line)
	if not File.writable?(path)
		abort("Error: <#{path}> is not writable (#{line})")
	end
	assert_path(path, line)
end


#
def assert_file(path, line)
	if !File.readable?(path) || !File.file?(path)
		abort("Error: <#{path}> is not readable or not a file (#{line})")
	end
	true
end


#
def assert_dir(path, line)
	if !File.readable?(path) || !File.directory?(path)
		abort("Error: <#{path}> is not readable or not a directory (#{line})")
	end
	true
end


#
def escape(text)
	if not host_os.include?('windows')
		# regexp troubled me here, I could not get '\\\&' or '\\\1' to work as substitution, therefore the block
		text.gsub(/[ ()&@<>?!'|;\{}$`"]/) { |match| "\\"+match }#')# workaround Xcodes syntax highlighning bug
	else
		text.gsub(/[<>]/) { |match| "\""+match+"\"" }
	end
end


def detect_mpi_rank
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
	return rank
end

# Kill a single process with a given signal.
def kill_process(pid, signal, name)
	if pid
		begin
			if signal == 'SIGKILL'
				puts "Forcing #{name} to stop (pid=#{pid})"
			else
				puts "Stopping #{name} (pid=#{pid})"
			end
			Process.kill(signal, pid)
		rescue Errno::ESRCH
			puts "#{name} already finished."
		end
	end
end

$interruptions = 0

def kill_processes(procs, exit_val)
  $interruptions = $interruptions + 1
	
	# Return if the processes are already killed
	if not procs
		return
	end
	
	# First time, SIGINT, second time, SIGKILL. This happens if a user
	# presses Ctrl-C twice within 30 seconds and MUSCLE is still running.
	if $interruptions == 1
		procs.each { |pid, name| kill_process(pid, 'SIGINT', name) }
	elsif $interruptions == 2
		procs.each { |pid, name| kill_process(pid, 'SIGKILL', name) }
	else
		exit exit_val
	end

	# After 30 seconds, do a full kill.
	begin
		Timeout::timeout(30) { Process.waitall }
	rescue Timeout::Error
    kill_processes(procs, exit_val)
    Process.waitall
	end
  
  exit exit_val
end

# Wait until all processes are finished, and kill other processes if
# a process exited with an error.
def await_processes(procs)
	if not procs.empty?
 		pid = Process.wait
		del = procs.delete pid

		while not procs.empty?
			if $?.exitstatus != 0
				puts "#{del} (pid=#{pid}) exited with status #{$?.exitstatus}; stopping other processes"
				kill_processes(procs, $?.exitstatus)
			end
		
			pid = Process.wait
			del = procs.delete pid
			#exit with no zero value if only one process had non-zero exit value
		end
		if $?.exitstatus != 0
			exit $?.exitstatus
		end
	end
	# we didn't exit, so our status is good.
	exit 0
end

end # module MuscleUtils

