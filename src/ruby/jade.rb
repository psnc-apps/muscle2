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

require 'jvm'
require 'utilities'
include MuscleUtils

#OBSOLETE
class Jade
	def Jade.build_command(agents, env)
		# build command to launch cxa kernels
	
		command = JVM.build_command(nil, env).last
		
		
		# java VM args
		command << env["bootclass"].to_s

		# jade args
		services = %w(jade.core.mobility.AgentMobilityService
						  jade.core.event.NotificationService
						  jade.core.messaging.TopicManagementService)
		
		command << "-detect-main false"
		command << "-services #{services.join('\;')}" unless services.empty?

#command << "-local-port 1099"
#command << "-nodeport 1088"
#command << "-smport 1077"
	
		platformname_items = []
		platformname_items << "#{File.basename(env['cxa_file'])}" if env.has_key?('cxa_file')
#		platformname_items << "#{Process.pid}"
		platformname_items << "#{Etc.getlogin}" # name of current user
#		platformname_items << env['mainport'].to_s if(env.has_key?('mainport') && !env['changeportifbusy']) # if we use -jade_imtp_leap_LEAPIMTPManager_changeportifbusy true the real port might be different
		
		platformname = platformname_items.join('_')		
		# jade args
		if env['main']
			command << "-name #{platformname}"
			command << "-container-name #{env['container_name']}" if env.has_key?('container_name')
			command << "-nomtp -jade_domain_df_autocleanup true"
			command << "-jade_imtp_leap_LEAPIMTPManager_changeportifbusy true" if env['changeportifbusy']
		elsif not env['main']
			command << "-name #{platformname}" # only needed here if we want to use the detect-main option
			command << "-container"
			command << "-container-name #{env['container_name']}" if env.has_key?('container_name')
		end
		command << "-file-dir "+File.join(env['tmp_path'],"") if env.has_key?('tmp_path') # always pass a trailing slash here (bug in JADE)
		command << "-gui" if env['rmagui']
		command << "-jade_core_AgentContainerImpl_enablemonitor false" # this is some additional monitoring utility from the JADE Misc add-on
		command << "-host "+env['host'] if env.has_key?('host')
		command << "-local-host "+env['localhost'] if env.has_key?('localhost')
# we only need to pin down the container port, if we e.g. use leap and want to use a ssh tunnel
# a free port is selected without a warning, should the specified port not be free
		command << "-local-port "+env['localport'].to_s if env['localport'] != nil
		# use same port as local-port to contact remote containers, for more complex setups (e.g. leap split mode) this has to be different from the local-port
		command << "-port "+env['mainport'].to_s if env['mainport'] != nil

		# MUSCLE args
#		$mandatoryMUSCLEKeys.each {|key| command << "-#{key} "+env[key].to_s}
		
		if agents.size > 0
		
			if env["autoquit"]
				# collect names of all kernel agents in this cxa to pass as args to the QuitMonitor
				kernel_agents = Cxa.LAST.known_agents.find_all {|a| a.kind_of?(KernelAgent)}
				names = kernel_agents.collect {|k| k.name}
				quit_monitor = JadeAgent.new("muscle.utilities.agent.QuitMonitor", "muscle.utilities.agent.QuitMonitor", names)
				agents << quit_monitor
			end

			# agents
			if env['leap']
				command << "-agents"
				leap_strings = []
				agents.each {|a| leap_strings << escape(a.to_s)}

				# with leap, multiple agent commands must be separated with a ';' instead of a space character
				# escape the ';' on linux et al.
				command << leap_strings.join("#{escape(';')}")
			else	
				agents.each {|a| command << escape(a.to_s)}
			end
		end	

		return command.join(" "), command
	end	
end


#
class JadeAgent

	def initialize(name, cls, args=[])
		@name = name
		@cls = cls
		@args = args
	end
	
	def ==(other)
		@name == other.name
	end

	def eql?(other)
		@name == other.name && @cls == other.cls && @args == other.args
	end
	
	def to_jade_plain
		to_s(false)
	end

	def to_jade_leap
		to_s(true)
	end

	def to_s(leap_mode = true)
		if @args.size > 0
			joinText = " "
			joinText = "," if leap_mode
			@name+":"+@cls+"("+@args.join(joinText)+")"
		else
			@name+":"+@cls
		end
	end
	
	#
	def JadeAgent.agent_from_string(agent_string, agent_infos)

		name = nil
		cls = nil
		args = []
		
		# strip any args
		argBegin = agent_string.index('(')
		argEnd = agent_string.rindex(')')
		if argBegin && argEnd
			argBegin += 1
			argEnd -= 1
			if argBegin < argEnd && argBegin >= 0 && argEnd >= 0
				argString = agent_string.slice(argBegin..argEnd)
				if argString
					agent_string = agent_string.chomp("("+argString+")")
					args = argString.split(",")
				end
			end
		end

		# get name and class
		name, cls = agent_string.split(':')
		if not cls
			ainfo = nil
			agent_infos.each {|ai| (ainfo=ai;break) if ai.name==name}
			unless ainfo.nil?
				cls = ainfo.cls
			else
				abort("Error: can not parse #{agent_string}")
			end
		end
		return JadeAgent.new(name, cls, args)
	end

	# visibility
	attr_reader :name, :cls, :args
	
end


#
class KernelAgent < JadeAgent

	#@@m = $m

	def initialize(*args)
		super
		@env = {}
		#@env = {:properties_loader_class=>@@m.env[:muscle_kernelpropertiesloader_class]}
	end
	
	# visibility
	attr :env
end



# test
if $0 == __FILE__
puts KernelAgent.new("a", "foo.bar.cls", %w(a b c)).inspect
end

