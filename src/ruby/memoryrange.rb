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

# represent memory range
class MemoryRange < Range
	attr_reader :from_mem, :to_mem
	def initialize(from, from_quantity, to, to_quantity)
		from = from.to_i
		to = to.to_i
		@from_mem = "#{from}#{from_quantity}"
		@to_mem = "#{to}#{to_quantity}"
		super(from, to)
	end
	def inspect
		super.inspect.sub(/(\d+)(\.{2,3}\d+)/, "\\1#{@from_quantity}\\2#{@to_quantity}")
	end
end
