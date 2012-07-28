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
#needed ???
#begin; require 'rubygems'; rescue LoadError; end
require 'json'

class JVM
	def JVM.build_command(jargs, env)
		# build command to launch a java application
		command = []
		
		# java VM args
		command << env['java']
		command << "-server"
		if env['quiet'] and env.has_key?('logging_quiet_config_path')
			command << "-Djava.util.logging.config.file="+env['logging_quiet_config_path']
		elsif env['verbose'] and env.has_key?('logging_verbose_config_path')
			command << "-Djava.util.logging.config.file="+env['logging_verbose_config_path']
		else
			command << "-Djava.util.logging.config.file="+env['logging_config_path'] if env.has_key?('logging_config_path')
		end
		command << "-Xms#{env['Xms']}" if env.has_key?('Xms') # note: no space between key and value
		command << "-Xmx#{env['Xmx']}" if env.has_key?('Xmx') # note: no space between key and value
		command << "-Xss#{env['Xss']}" if env.has_key?('Xss') # note: no space between key and value
		
		# resolve all proc objects
		# needed ???
		#env = env.evaluate if env.respond_to?(:evaluate)

		# the java platform should load CXA from JSON file, so we dump a file with the muscle env
		# dump env
		File.open(env['muscle.Env dump uri'].path, "w") do |f|
			f.puts JSON.dump(env)
		end
		command << "-D#{Muscle.jclass}=#{env['muscle.Env dump uri']}"
		
		command << "-classpath #{env['CLASSPATH']}" unless env['CLASSPATH'].nil?
		
		# additional jvm flags
		if env.has_key?("jvmflags")
			env["jvmflags"].each {|x| command << x}
		end

		command << "-Djava.io.tmpdir="+env['tmp_path'].to_s if env.has_key?('tmp_path') # used by standard java stuff
		command << "-Dmuscle.native.tmpfile="+env['native_tmp_file'].to_s if env.has_key?('native_tmp_file') # used to write the host and port of the file to
		
		command << jargs unless jargs.nil? # the java class to launch
		
		return command.join(" "), command
	end	
end
