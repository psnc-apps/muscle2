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

PARENT_DIR = File.dirname(File.expand_path(__FILE__)) unless defined? PARENT_DIR
$LOAD_PATH << PARENT_DIR

require 'timeout'

module MuscleUtils
	# run plain java with muscle classpath applied
	def java(args)
		cp = default_classpaths.join(File::PATH_SEPARATOR)
		cmd = "java -cp #{cp} #{args}"
		`#{cmd}`.chomp
	end

	def mkTmpPath(arg)
		require 'fileutils'
		host = `hostname -f`.chomp
		time = Time.now.strftime('%Y-%m-%d_%H-%M-%S')
		tmpdir_path = File.expand_path(arg) + "/#{host}_#{time}_#{Process.pid}"
		FileUtils::mkdir_p(tmpdir_path + '/.muscle');
		return tmpdir_path
	end

	# returns an array with cpasslaths
	def default_classpaths
		base_dir = ENV['MUSCLE_HOME']

		# configure java CLASSPATH
		# classpath must include jade and muscle classes
		# remember: path separator is ';' on windows systems, else ':' better just use File::PATH_SEPARATOR
		# be careful: ENV['CLASSPATH'] might be nil or an empty string
		cp = []
		cp += ENV['CLASSPATH'].split(File::PATH_SEPARATOR) unless ENV['CLASSPATH'].nil?
    if ENV['MUSCLE_CORE_CLASSPATH'].nil?
      cp += Dir.glob("#{base_dir}/share/muscle/java/*.jar")
      cp += Dir.glob("#{base_dir}/share/muscle/java/thirdparty/*.jar")
    else
      cp += ENV['MUSCLE_CORE_CLASSPATH'].split(File::PATH_SEPARATOR)
    end
	end

	# load env files
	def load_env(path)
		base_load_path = File.expand_path(path)
		machine = `hostname -s`.chomp
		machine_load_path = File.join(File.dirname(base_load_path), "#{File.basename(base_load_path, File.extname(base_load_path))}.#{machine}#{File.extname(base_load_path)}")

		load_path = base_load_path
		load_path = machine_load_path if File.readable?(machine_load_path)

		load(load_path)
	end	

	# determine current host os
	def host_os
		require 'rbconfig'

		case RbConfig::CONFIG['host_os']
		when /mswin|windows/i
			'windows'
		when /linux/i
			'linux'
		when /darwin/i
			'darwin'
		when /sunos|solaris/i
			'solaris'
		else
			'unknown'
		end
	end

	# set value for LIBPATHENV or abort
	def assert_LIBPATHENV env
		if(env['LIBPATHENV'] == nil)
			# try fo figure out the lib path environment key automatically
			require 'rbconfig'
			env['LIBPATHENV'] = 
			if(ENV['LIBPATHENV'] != nil)
				ENV['LIBPATHENV']
			elsif( RbConfig::CONFIG['LIBPATHENV'] != nil ) # is nil on mswin or with jruby
				RbConfig::CONFIG['LIBPATHENV']
			else
				case host_os
				when 'windows'
					'PATH'
				when 'linux'
					'LD_LIBRARY_PATH'
				when 'darwin'
					'DYLD_LIBRARY_PATH'
				else
					nil
				end
			end

			# print an error if we can not determine the LIBPATHENV
			if(env['LIBPATHENV'] == nil)
				abort("Error: can not determine value of environment key for LIBPATHENV, set an environment variable 'LIBPATHENV' to your preferred value (the value is usually something like 'LD_LIBRARY_PATH', 'DYLD_LIBRARY_PATH' or 'PATH', an environment path variable from where to load dynamic native libraries)")
			end			
		end
		env
	end

	# Detect the MPI rank from common environment variables that are set when MPI is active.
	def detect_mpi_rank
		possible_mpi_rank_vars=['OMPI_MCA_orte_ess_vpid',
			'OMPI_MCA_ns_nds_vpid',
			'PMI_RANK',
			'MP_CHILD',
			'SLURM_PROCID',
			'X10_PLACE',
			'MP_CHILD']

		rank = nil
		possible_mpi_rank_vars.each {|var| rank = ENV[var] if ENV.has_key? var }
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

	# Set a static number of times that the program was interrupted by the
	# user or a signal.
	$interruptions = 0

	def kill_processes(procs, exit_val)
		$interruptions = $interruptions + 1

		# Return if the processes are already killed
		if procs.nil?
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

  def which(cmd)
    exts = ENV.has_key?('PATHEXT') ? ENV['PATHEXT'].split(';') : ['']
    ENV['PATH'].split(File::PATH_SEPARATOR).each do |path|
      exts.each { |ext|
        exe = File.join(path, "#{cmd}#{ext}")
        return exe if File.executable? exe
      }
    end
    return nil
  end
end # module MuscleUtils
