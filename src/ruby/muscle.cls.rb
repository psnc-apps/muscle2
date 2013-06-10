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
=end
require 'jvm'
require 'utilities'
include MuscleUtils

PROP_MAIN_PORT = 'pl.psnc.mapper.muscle.mainport'
PROP_DEBUG = 'pl.psnc.mapper.muscle.debug'
PROP_TRACE = 'pl.psnc.mapper.muscle.trace'
PROP_MTO_ADDRESS = 'pl.psnc.mapper.muscle.mto.address'
PROP_MTO_PORT = 'pl.psnc.mapper.muscle.mto.port'

class Muscle
	def initialize
		@@LAST = self
		@env = {}
		@cxa = nil
		
		# set value for LIBPATHENV or abort
		assert_LIBPATHENV self.env

		# load (machine specific) default env
		load_env(File.expand_path("#{PARENT_DIR}/muscle.env.rb"), true)
	end
	
	def add_env(e)
		self.env.merge!(e) do |key, oldval, newval| 
			if(key == 'CLASSPATH' && oldval != nil)
				oldval= newval << File::PATH_SEPARATOR << oldval
			else
				oldval= newval
			end
		end
	end

	# helper method to add path variables
	def add_path(hsh)
		hsh.each do |path_key,path|
			self.env[path_key] = '' if self.env[path_key] == nil
			if(path.class == Array)
				self.env[path_key] = (self.env[path_key].split(File::PATH_SEPARATOR) + path).join(File::PATH_SEPARATOR)
			else
				self.env[path_key] = (self.env[path_key].split(File::PATH_SEPARATOR) + path.split(File::PATH_SEPARATOR)).join(File::PATH_SEPARATOR)		
			end
			# delete any empty items
			self.env[path_key] = ((self.env[path_key].split(File::PATH_SEPARATOR)).delete_if {|x| x==''}).join(File::PATH_SEPARATOR)	
		end
	end

	def add_classpath(p)
		add_path('CLASSPATH'=>p)
	end

	def add_libpath(p)
		add_path('libpath'=>p)
		ENV[self.env['LIBPATHENV']] = self.env['libpath']
	end

	def load_cxa
		@cxa = Cxa.new(self.env['cxa_file'], self.env)
	end

	# overwrite env setting
	def set(hsh)
		self.env.merge!(hsh)
	end

	def Muscle.jclass
		'muscle.Env'
	end
	
	def Muscle.LAST
		@@LAST
	end
	
	def print_env(keys)
		if keys == true
			# print the complete env (sorted)
			self.env.keys.sort.each {|k| puts "#{k.inspect}=>#{self.env[k].inspect}"}
		else
			# print value for the specified key(s)
			if(keys.size == 1)
				# print raw value if output is for a single key (useful if you want to further process the output, e.g. CLASSPATH)
				puts self.env[keys.first] if self.env.has_key? keys.first
			else
				keys.each {|k| puts "#{k.inspect}=>#{self.env[k].inspect}" if self.env.has_key? k}
			end
		end
	end
	
	def stage_files
		files = self.env['stage_files']
		
		if files.size == 1
			puts "Staging file #{files.first.inspect}"
		elsif files.size > 1
			puts "Staging files #{files.inspect}"
		end
		files.push(self.env['cxa_file'])

		for file in files do
			dir = Dir.glob(file)
			if dir.empty?
				puts "\tWarning: filename #{file} does not result in any files."
			end
			FileUtils::cp_r(dir, self.env['tmp_path'])
		end
	end

	def gzip_stage_files
		files = self.env['gzip_stage_files']
		
		if files.size == 1
			puts "Zipping and staging file #{files.first.inspect}"
		elsif files.size > 1
			puts "Zipping and staging files #{files.inspect}"
		else
			return
		end
		
		for file in files do
			if not system("tar --exclude=.git --exclude=.svn --exclude=.hg -czf #{self.env['tmp_path']}/#{File.basename(file)}.tgz #{file}")
				puts "\tWarning: filename #{file} could not be compressed or staged."
			end
		end
	end
	
	def run_manager(clazz, args)
		tmpXmx = self.env['Xmx']
		tmpXms = self.env['Xms']
		self.env['Xms'] = '20m'
		self.env['Xmx'] = '100m'
		self.env[:as_manager] = true

		puts '=== Running MUSCLE2 Simulation Manager ==='
		pid = run_command(clazz, args)

		self.env.delete(:as_manager)		
		self.env['Xms'] = tmpXms
		self.env['Xmx'] = tmpXmx
		
		return pid
	end
	
	def poll_manager(manager_pid)
		if Process::waitpid(manager_pid, Process::WNOHANG)
			if not $?
				puts 'Simulation Manager exited with an error.'
				exit 1
			else
				stat = $?.exitstatus
				if stat
					puts 'Simulation Manager exited with an error.'
					exit stat
				else
					puts 'Simulation Manager exited before setting up connection.'
					exit 1
				end
			end
		end
	end
	
	def find_manager_contact(manager_pid, contact_file_name = nil)
		if not contact_file_name
			contact_file_name = "#{muscle_tmp_path}/simulationmanager.address"
		end
		tries_count = 0
		while !File.exists?(contact_file_name)
			poll_manager(manager_pid)
			sleep 0.2
			tries_count += 1
			if tries_count % 25 == 0
				puts "Waiting for simulation manager to start listening, notified in file: #{contact_file_name}"
			end
		end

		while File.exists?(contact_file_name + '.lock') #waiting for lock file to disappear 
			poll_manager(manager_pid)
			sleep 0.2
		end

		File.open(contact_file_name, 'rb').read
	end
	
	def run_client(clazz, args, contact_addr)
		args << '-m'
		if self.env['manager']
			args << self.env['manager']
		else
			# main
			args << contact_addr
		end
		
		if self.env['instancethreads']
			args << '-t' << self.env['instancethreads']
		end
		
		puts '=== Running MUSCLE2 Simulation ==='
		run_command(clazz, args)
	end
	
	def add_to_command(command, kernel_name, prop)
		cenv = self.cxa.env
		key = "#{kernel_name}:#{prop}"
		if cenv.has_key?(key)
			command << cenv[key]
			return true
		else
			return false
		end
	end
	
	def exec_native(kernel_name, extra_args)
		native_command = []
		add_to_command(native_command, kernel_name, 'mpiexec_command')
		add_to_command(native_command, kernel_name, 'mpiexec_args')
		add_to_command(native_command, kernel_name, 'debugger')
		
		if not add_to_command(native_command, kernel_name, 'command')
			puts "Missing #{kernel_name}:command property"
			exit 1
		end

		args = self.cxa.env["#{kernel_name}:args"]
		if args
			puts "Args: #{args}"
			native_command << args.split(' ')
		end

		native_command << '--'

		# Remove --native from subcommand
		extra_args.reject! {|x| x == '--native' || x == '-n'}
		
		# If a shorthand notation contained -n, remove it
		# but, first remove jvmflags, as this also will start with a single '-'
		i = extra_args.index '--jvmflags'
		if i
			extra_args.delete_at(i)
			native_command << '--jvmflags' << extra_args.delete_at(i)
		end
			
		# remove -anm or -nm or comparable
		extra_args.collect! { |x| if x =~ /^-(n|\w*n)/ then x.delete 'n' else x end }

		native_command << extra_args
		
		command = native_command.join(' ')
		
		exec_command(command)
	end
	
	def exec_mpi(clazz, args)
		command = JVM.build_command(clazz, args, self.env)
		exec_command(command)
	end
	
	def exec_command(command)
		puts "Executing #{command}"
		
		if self.env.has_key?('execute') and not self.env['execute']
			exit 0
		else
			exec(command)
		end
	end

	def run_command(clazz, args)
		command = JVM.build_command(clazz, args, self.env)
		if self.env['verbose']
			puts command
		end
		
		if self.env.has_key?('execute') and not self.env['execute']
			return -1
		else
			return Process.fork {exec(command)}
		end
	end
	
	def apply_intercluster
		if(self.env['port_min'].nil? or self.env['port_max'].nil?)
			puts 'Warning: intercluster specified, but no local port range given.'
			puts 'Maybe $MUSCLE_HOME/etc/muscle.profile was not sourced and $MUSCLE_PORT_MIN or $MUSCLE_PORT_MAX were not set?'
			puts 'To specify them manually, use the flags --port-min and --port-max.'
			return false
		else
			mto = self.env['mto'] || ENV['MUSCLE_MTO']
			if not mto.nil?
				mtoHost = mto.split(':')[0]
				mtoPort = mto.split(':')[1]
			end

			if mtoPort.nil? or mtoHost.nil?
				puts 'Warning: intercluster specified, but no MTO address/port given.'
				puts 'Maybe $MUSCLE_HOME/etc/muscle.profile was not sourced and $MUSCLE_MTO was not set?'
				puts 'To specify the MTO address manually, use the flag --mto.'
				return false
			else
				if self.env.has_key?('qcg')
					if self.env.has_key?('main')
						self.env['bindport'] = 22 #master
					else
						self.env['manager'] = 'localhost:22' #slave
					end
				else
					self.env['localport'] = 0 
				end

				if not self.env.has_key?('jvmflags')
					self.env['jvmflags'] = []
				end

				self.env['jvmflags'] << '-Dpl.psnc.muscle.socket.factory=muscle.net.CrossSocketFactory'
				self.env['jvmflags'] << '-D' + PROP_MTO_ADDRESS + '=' + mtoHost
				self.env['jvmflags'] << '-D' + PROP_MTO_PORT    + '=' + mtoPort.to_s
			end
		end
		return true
	end
	
	def muscle_tmp_path
		"#{self.env['tmp_path']}/.muscle"
	end
	
	# visibility
	attr_reader :env, :cxa
end

def add_classpath(p)
	Muscle.LAST.add_classpath(p)
end

def add_libpath(p)
	Muscle.LAST.add_libpath(p)
end
