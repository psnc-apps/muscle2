require 'utilities'
include MuscleUtils

PROP_PORT_RANGE_MIN = "pl.psnc.mapper.muscle.portrange.min"
PROP_PORT_RANGE_MAX = "pl.psnc.mapper.muscle.portrange.max"
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
	
	def print_env(keys)
	  if keys == true
  		# print the complete env (sorted)
  		env.keys.sort.each {|k| puts "#{k.inspect}=>#{env[k].inspect}"}
  	else
  		# print value for the specified key(s)
  		if(keys.size == 1)
  			# print raw value if output is for a single key (useful if you want to further process the output, e.g. CLASSPATH)
  			puts env[keys.first] if env.has_key? keys.first
  		else
  			keys.each {|k| puts "#{k.inspect}=>#{env[k].inspect}" if env.has_key? k}
  		end
  	end
	end
	
	def run_manager(args)
	  tmpXmx = env['Xmx']
  	tmpXms = env['Xms']
  	env['Xms'] = '20m'
  	env['Xmx'] = '100m'
  	command = JVM.build_command(args, env).first
  	env['Xms'] = tmpXms
  	env['Xmx'] = tmpXmx
  	puts "=== Running MUSCLE2 Simulation Manager ==="
  	if env['verbose']
  		puts "Executing: " + command
  	end
  	return Process.fork {exec(command)}
  end
	
	def run_client(args, contact_file_name = nil)
	  args << "-m"
	  if env['manager']
      	args << env['manager']
    else
      # main
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
  		args << contact_addr
  	end

  	puts "=== Running MUSCLE2 Simulation ==="
    command = JVM.build_command(args, env).first
  	if env['verbose']
  		puts command
  	end
  	return Process.fork {exec(command)}
  end
	
	def exec_native(kernel_name, extra_args)
  	
    native_command = []
    cxa = Cxa.LAST
  	if cxa.env[ kernel_name + ":mpiexec_command"] 
  		native_command << cxa.env[ kernel_name + ":mpiexec_command"]
  	end

  	if cxa.env[ kernel_name + ":mpiexec_args"] 
  		native_command << cxa.env[ kernel_name + ":mpiexec_args"].split(" ")
  	end

  	if cxa.env[kernel_name + ":debugger"] 
  		native_command << cxa.env[ kernel_name + ":debugger"]
  	end

  	if cxa.env[ kernel_name + ":command"] 
  		native_command << cxa.env[ kernel_name + ":command"]
  	else
  		puts "Missing " + kernel_name + ":command property"
  		exit 1
  	end

  	if cxa.env[ kernel_name + ":args"] 
  		puts "Args: " + cxa.env[ kernel_name + ":args"] 
  		native_command << cxa.env[ kernel_name + ":args"].split(" ")
  	end

  	extra_args.delete("--native");

  	native_command << "--"
  	native_command << extra_args
  	
  	command = native_command.join(" ")
  	
  	puts "Executing: " + command
  	Process.exec(command)
	end
	
	def exec_mpi(args)
	  command = JVM.build_command(args, env).first
	  puts "Executing: " + command
	  Process.exec(command)
	end
	
	def apply_intercluster
    port_min = env['port_min'] || ENV['MUSCLE_PORT_MIN']
    port_max = env['port_max'] || ENV['MUSCLE_PORT_MAX']
    if(port_min.nil? or port_max.nil?)
  	  puts "Warning: intercluster specified, but no local port range given! Intercluster ignored."
    else
    	mto =  env['mto'] || ENV['MUSCLE_MTO']
    	if (! mto.nil?)
    		mtoHost = mto.split(':')[0]
    		mtoPort = mto.split(':')[1]
    	end

    	if(mtoPort.nil? or mtoHost.nil?)
    	  puts "Warning: intercluster specified, but no MTO address/port given! Intercluster ignored."
    	else
    	  if(env.has_key?('qcg'))
    	  	if (env['main'])
    			  env['bindport'] = 22 #master
    		  else
    			  env['manager'] = "localhost:22" #slave
    		  end
    	  else
    		  env['localport'] = 0 
    	  end

    	  if(env["jvmflags"].nil?)
    		  env["jvmflags"] = Array.new
    	  end

    	  env["jvmflags"] << "-Dpl.psnc.muscle.socket.factory=muscle.net.CrossSocketFactory"
    	  env["jvmflags"] << "-D" + PROP_PORT_RANGE_MIN + "=" + port_min
    	  env["jvmflags"] << "-D" + PROP_PORT_RANGE_MAX + "=" + port_max
    	  env["jvmflags"] << "-D" + PROP_MTO_ADDRESS    + "=" + mtoHost
    	  env["jvmflags"] << "-D" + PROP_MTO_PORT       + "=" + mtoPort.to_s

    	end
    end
	end
	
	# visibility
	attr_reader :env, :env_basename
end