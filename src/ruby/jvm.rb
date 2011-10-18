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

begin; require 'rubygems'; rescue LoadError; end
require 'json'

class JVM
	def JVM.build_command(jargs, env)
		# build command to launch a java programme
		
		command = []
		
		# java VM args
		command << env['java']
		command << "-Duser.language=en"
		command << "-server"
		if env['quiet']
			# do not use the specified java.util.logging.config.file,
			# but create a temporary one which will omit any logging
			tmpLoggingConfig = Tempfile.new('muscle_logging_configuration')
			tmpLoggingConfig.puts "handlers = java.util.logging.ConsoleHandler"
			tmpLoggingConfig.puts ".level = OFF"
			tmpLoggingConfig.close
			command << "-Djava.util.logging.config.file="+tmpLoggingConfig.path
		else
			command << "-Djava.util.logging.config.file="+env['logging_config_path'] if env.has_key?('logging_config_path')
		end
		command << "-Djava.util.logging.config.class=muscle.logging.LoggingConfiguration"
#		command << "-ea"
		command << "-Xms#{env['Xms']}" if env.has_key?('Xms') # note: no space between key and value
		command << "-Xmx#{env['Xmx']}" if env.has_key?('Xmx') # note: no space between key and value
		command << "-Xss#{env['Xss']}" if env.has_key?('Xss') # note: no space between key and value
		# the java platform should load from file, so we dump a file with the muscle env
		
		# resolve all proc objects
		env = env.evaluate if env.respond_to?(:evaluate)

		# dump env
		File.open(env['muscle.Env dump uri'].path, "w") do |f|
			f.puts JSON.dump(env)
		end
		command << "-D#{Muscle.jclass}=#{env['muscle.Env dump uri']}"
		
#		$mandatoryJVMKeys.each {|key| command << "-#{key} "+env[key].to_s}
		command << "-classpath #{env['CLASSPATH']}" unless env['CLASSPATH'].nil?
		
		# additional jvm flags
		if env.has_key?("jvmflags")
			env["jvmflags"].each {|x| command << x}
		end
		
		# we also add the MUSCLE env flags to the JVM this way we can access the MUSCLE-env from any java class, not just agents
		# syntax for a custom JVM property: -D<name>=<value>
	#	$mandatoryMUSCLEKeys.each {|key| command << "-D#{key}="+env[key].to_s}

		command << "-Djava.io.tmpdir="+env['tmp_path'].to_s if env.has_key?('tmp_path') # used by standard java stuff
		
		command << jargs unless jargs.nil? # the java class to launch
		
		return command.join(" "), command
	end	
end
