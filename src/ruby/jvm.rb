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

require 'json'

PROP_PORT_RANGE_MIN = "pl.psnc.mapper.muscle.portrange.min"
PROP_PORT_RANGE_MAX = "pl.psnc.mapper.muscle.portrange.max"

class JVM
	def JVM.add_prop(env, command, key, value)
		JVM.add_pref(env, command, "-D#{key}=", value)
	end
	def JVM.add_pref(env, command, prefix, value)
		command << "#{prefix}#{env[value]}" if env[value]
	end
	def JVM.build_command(jargs, env)
		# build command to launch a java application
		command = []
		
		# java VM args
		command << env['java']
		command << "-server"

		# the java platform should load CXA from JSON file, so we dump a file with the muscle env
		# dump env
		File.open(env['muscle.Env dump uri'].path, "w") do |f|
			f.puts JSON.dump(env)
		end

		JVM.add_pref(env, command, '-Xms', 'Xms')
		JVM.add_pref(env, command, '-Xmx', 'Xmx')
		JVM.add_pref(env, command, '-Xss', 'Xss')
				                             # intentional space after classpath
		JVM.add_pref(env, command, '-classpath ', 'CLASSPATH')
		
		# additional jvm flags
		if env.has_key?("jvmflags")
			env["jvmflags"].each {|x| command << x}
		end

		log_infix = env[:as_manager] ? 'manager_' : ''
		
		# Properties to use within program
		if env['quiet'] and env.has_key?("logging_quiet_#{log_infix}config_path")
			JVM.add_prop(env, command, "java.util.logging.config.file", "logging_quiet_#{log_infix}config_path")
		elsif env['verbose'] and env.has_key?("logging_verbose_#{log_infix}config_path")
			JVM.add_prop(env, command, "java.util.logging.config.file", "logging_verbose_#{log_infix}config_path")
		else
			JVM.add_prop(env, command, "java.util.logging.config.file", "logging_#{log_infix}config_path")
		end

		JVM.add_prop(env, command, Muscle.jclass,             'muscle.Env dump uri')
		JVM.add_prop(env, command, 'java.io.tmpdir', 	        'tmp_path')
		JVM.add_prop(env, command, 'muscle.native.tmpfile',   'native_tmp_file')
		JVM.add_prop(env, command, 'muscle.manager.bindport', 'bindport')
		JVM.add_prop(env, command, 'muscle.net.bindaddr',     'bindaddr')
		JVM.add_prop(env, command, 'muscle.net.bindinf',      'bindinf')
		JVM.add_prop(env, command, PROP_PORT_RANGE_MIN,       'port_min')
		JVM.add_prop(env, command, PROP_PORT_RANGE_MAX,       'port_max')
		
		command << jargs unless jargs.nil? # the java class to launch
		
		return command.join(" ")
	end
end
