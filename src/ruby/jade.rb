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

class Agent
	def initialize(name, cls, args=[])
		@name = name
		@cls = cls
		@args = args
		@env = {}
	end
	
	def ==(other)
		@name == other.name
	end

	def eql?(other)
		@name == other.name && @cls == other.cls && @args == other.args
	end
	
	def to_s
		if @args.empty?
			"#{@name}:#{@cls}"
		else
			"#{@name}:#{@cls}(#{@args.join(',')})"
		end
	end

	# visibility
	attr_reader :name, :cls, :args, :env
end

class TerminalAgent < Agent
end

class InstanceAgent < Agent
end

# test
if $0 == __FILE__
puts KernelAgent.new("a", "foo.bar.cls", %w(a b c)).inspect
end

