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

== Author
   Jan Hegewald
=end

class ConnectionScheme
	def initialize(comps)
		super()
		@components = comps
		@pipelines = []
	end

	def attach(hsh)
		raise "empty attach for #{hsh.inspect}" unless block_given?
		
		# store values we need for the #tie, which should be called in the block
		@current_entrance = self.components.get(hsh.keys.first)
		@current_exit = self.components.get(hsh.values.first)

		raise "Instances must be defined before coupling: #{hsh.inspect}" if self.current_entrance.nil? or self.current_exit.nil?

		yield
		@current_entrance = nil
		@current_exit = nil
	end

	def tie(entrance_name, exit_name=entrance_name, filters=[], exit_filters=nil)
		self.current_entrance.couple(self.current_exit, {entrance_name => exit_name}, filters, exit_filters)
	end

	def ConnectionScheme.jclass
		'muscle.core.ConnectionScheme'
	end
	
	attr_reader :current_entrance, :current_exit, :components
end

# helper method for setup files, redirect tie method to ConnectionScheme
def tie(*args)
	$muscle_connection_scheme.tie(*args)
end
