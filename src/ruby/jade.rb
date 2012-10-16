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

	def initialize(*args)
		super
		@env = {}
	end
	
	# visibility
	attr :env
end

#
class TerminalAgent < JadeAgent

	def initialize(*args)
		super
		@env = {}
	end
	
	# visibility
	attr :env
end

# test
if $0 == __FILE__
puts KernelAgent.new("a", "foo.bar.cls", %w(a b c)).inspect
end

