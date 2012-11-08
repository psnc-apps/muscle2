require 'jvm'
require 'utilities'
include MuscleUtils

PROP_MAIN_PORT = "pl.psnc.mapper.muscle.mainport"
PROP_DEBUG = "pl.psnc.mapper.muscle.debug"
PROP_TRACE = "pl.psnc.mapper.muscle.trace"
PROP_MTO_ADDRESS = "pl.psnc.mapper.muscle.mto.address"
PROP_MTO_PORT = "pl.psnc.mapper.muscle.mto.port"

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
	
	def add_env(e)
		@env.merge!(e) do |key, oldval, newval| 
			if(key == "CLASSPATH" && oldval != nil)
				oldval=newval+File::PATH_SEPARATOR+oldval
			else
				oldval=newval
			end
		end
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
		@env.merge!(hsh)
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
			@env.keys.sort.each {|k| puts "#{k.inspect}=>#{env[k].inspect}"}
		else
			# print value for the specified key(s)
			if(keys.size == 1)
				# print raw value if output is for a single key (useful if you want to further process the output, e.g. CLASSPATH)
				puts @env[keys.first] if @env.has_key? keys.first
			else
				keys.each {|k| puts "#{k.inspect}=>#{env[k].inspect}" if @env.has_key? k}
			end
		end
	end
	
	def stage_files
		for file in @env['stage_files'] do
			dir = Dir.glob(file)
			if dir.empty?
				puts "\tWarning: filename #{file} does not result in any files."
			end
			FileUtils::cp_r(dir, @env['tmp_path'])
		end
	end

	def gzip_stage_files
		for file in @env['gzip_stage_files'] do
			if not system("tar --exclude=.git --exclude=.svn -czf #{@env['tmp_path']}/#{File.basename(file)}.tgz #{file}")
				puts "\tWarning: filename #{file} could not be compressed or staged."
			end
		end
	end
	
	def run_manager(args)
		tmpXmx = @env['Xmx']
		tmpXms = @env['Xms']
		@env['Xms'] = '20m'
		@env['Xmx'] = '100m'
		@env[:as_manager] = true
		command = JVM.build_command(args, @env)
		@env.delete(:as_manager)
		
		@env['Xms'] = tmpXms
		@env['Xmx'] = tmpXmx
		puts "=== Running MUSCLE2 Simulation Manager ==="
		if @env['verbose']
			puts "Executing: #{command}"
		end
		Process.fork {exec(command)}
	end
	
	def poll_manager(manager_pid)
		if Process::waitpid(manager_pid, Process::WNOHANG)
			if not $?
				puts "Simulation Manager exited with an error."
				exit 1
			elsif $?.exitstatus != 0
				puts "Simulation Manager exited with an error."
				exit $?.exitstatus
			else
				puts "Simulation Manager exited before setting up connection."
				exit 0
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

		while File.exists?(contact_file_name + ".lock") #waiting for lock file to disappear 
			poll_manager(manager_pid)
			sleep 0.2
		end

		File.open(contact_file_name, "rb").read
	end
	
	def run_client(args, contact_addr)
		args << "-m"
		if @env['manager']
			args << @env['manager']
		else
			# main
			args << contact_addr
		end

		puts "=== Running MUSCLE2 Simulation ==="
		command = JVM.build_command(args, @env)
		if @env['verbose']
			puts command
		end
		Process.fork {exec(command)}
	end
	
	def add_to_command(command, kernel_name, prop)
		cenv = Cxa.LAST.env
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
		add_to_command(native_command, kernel_name, "mpiexec_command")
		add_to_command(native_command, kernel_name, "mpiexec_args")
		add_to_command(native_command, kernel_name, "debugger")
		
		if not add_to_command(native_command, kernel_name, "command")
			puts "Missing #{kernel_name}:command property"
			exit 1
		end

		args = Cxa.LAST.env["#{kernel_name}:args"]
		if args
			puts "Args: #{args}"
			native_command << args.split(" ")
		end

		native_command << "--"

		# Remove --native from subcommand
		extra_args.reject! {|x| x == "--native" || x == "-n"}
		
		# If a shorthand notation contained -n, remove it
		# but, first remove jvmflags, as this also will start with a single '-'
		i = extra_args.index "--jvmflags"
		if i
			extra_args.delete_at(i)
			native_command << "--jvmflags" << extra_args.delete_at(i)
		end
			
		# remove -anm or -nm or comparable
		extra_args.collect! { |x| if x =~ /^-(n|\w*n)/ then x.delete "n" else x end }

		native_command << extra_args
		
		command = native_command.join(" ")
		
		puts "Executing: #{command}"
		exec(command)
	end
	
	def exec_mpi(args)
		command = JVM.build_command(args, @env)
		puts "Executing: #{command}"
		exec(command)
	end
	
	def apply_intercluster
		if(env['port_min'].nil? or @env['port_max'].nil?)
			puts "Warning: intercluster specified, but no local port range given."
			puts "Maybe $MUSCLE_HOME/etc/muscle.profile was not sourced and $MUSCLE_PORT_MIN or $MUSCLE_PORT_MAX were not set?"
			puts "To specify them manually, use the flags --port-min and --port-max."
			return false
		else
			mto = @env['mto'] || ENV['MUSCLE_MTO']
			if (! mto.nil?)
				mtoHost = mto.split(':')[0]
				mtoPort = mto.split(':')[1]
			end

			if(mtoPort.nil? or mtoHost.nil?)
				puts "Warning: intercluster specified, but no MTO address/port given."
				puts "Maybe $MUSCLE_HOME/etc/muscle.profile was not sourced and $MUSCLE_MTO was not set?"
				puts "To specify the MTO address manually, use the flag --mto."
				return false
			else
				if @env.has_key?('qcg')
					if @env.has_key?('main')
						@env['bindport'] = 22 #master
					else
						@env['manager'] = "localhost:22" #slave
					end
				else
					@env['localport'] = 0 
				end

				if not @env.has_key?("jvmflags")
					@env["jvmflags"] = []
				end

				@env["jvmflags"] << "-Dpl.psnc.muscle.socket.factory=muscle.net.CrossSocketFactory"
				@env["jvmflags"] << "-D" + PROP_MTO_ADDRESS		+ "=" + mtoHost
				@env["jvmflags"] << "-D" + PROP_MTO_PORT			 + "=" + mtoPort.to_s
			end
		end
		return true
	end
	
	def muscle_tmp_path
		"#{env['tmp_path']}/.muscle"
	end
	
	# visibility
	attr_reader :env, :env_basename
end
