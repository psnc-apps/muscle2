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

if File.symlink? __FILE__
	PARENT_DIR = File.dirname(File.expand_path(File.readlink(__FILE__)))
else
	PARENT_DIR = File.dirname(File.expand_path(__FILE__))
end unless defined? PARENT_DIR
$LOAD_PATH << PARENT_DIR

class ConnectionScheme

	def initialize(cxa)
		super()
		@cxa = cxa
		@pipelines = []
	end

	def attach(hsh)
		if block_given?
			# store values we need for the #tie, which should be called in the block
			@current_entrance = '@' << @cxa.get(hsh.keys.first).portal_name
			@current_exit = '@' << @cxa.get(hsh.values.first).portal_name

			raise("Instances must be defined before coupling: #{hsh.inspect}") if @current_entrance.nil? or @current_exit.nil?

			yield
			@current_entrance = nil
			@current_exit = nil
		else
			raise("empty attach for #{hsh.inspect}")
		end
	end

	def tie(entrance_name, exit_name=entrance_name, filters=[], exit_filters=nil)
		# Combine exit and entrance filters, separated by ''
		filters.push('', *exit_filters) unless exit_filters.nil?

		if filters.empty?
			@pipelines << (exit_name + @current_exit + ' -> ' + entrance_name + @current_entrance)
		else
			@pipelines << (exit_name + @current_exit + ' ->(' + filters.join(',') + ') ' + entrance_name + @current_entrance)
		end
	end

	# produces cs in old style format
	# {exit name} {{full class name of conduit}[#ID][(carg0,cargn)]} {entrance name}
	# e.g.:
	# data@r muscle.core.conduit.AutomaticConduit#A(multiply_2) data@w
	def to_s
		@pipelines.join("\n")
	end

	def ConnectionScheme.jclass
		'muscle.core.ConnectionScheme'
	end
end

def muscle_set_connection_scheme(cs)
	$muscle_connection_scheme = cs
end

# helper method for setup files, redirect tie method to ConnectionScheme
def tie(*args)
	$muscle_connection_scheme.tie(*args)
end
