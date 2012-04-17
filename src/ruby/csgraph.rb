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

# extend ConnectionScheme to be able to output a dot graph
# OBSOLETE
if File.symlink? __FILE__
	PARENT_DIR = File.dirname(File.expand_path(File.readlink(__FILE__)))
else
	PARENT_DIR = File.dirname(File.expand_path(__FILE__))
end unless defined? PARENT_DIR
$LOAD_PATH << PARENT_DIR

begin; require 'rubygems'; rescue LoadError; end
require 'rgl/dot'

# extend the functionality of rgl graph
class RGL::DOT::Digraph 
  attr_reader :elements
  
  
  def write_to_graphic_file(fmt='png', dotfile='graph') 
    src = dotfile + '.dot' 
    dot = dotfile + '.' + fmt 

    File.open(src, 'w') {|f| f << self.to_s << "\n"} 

    system( "dot -T#{fmt} #{src} -o #{dot}" ) 
    dot 
  end
  
	def add_vertex(vertex, params = {'fontsize'=>10, 'shape'=>"oval"})		
		params['name'] ||= vertex.to_s
		params['label'] ||= params['name']
		
		params = vertex.to_dot_params if vertex.respond_to?(:to_dot_params)
		v = RGL::DOT::Node.new(params)
		i = index(v)
		return elements[i] unless i.nil?

		self << v
		v
	end

	def add_edge(from, to, params = {})
		params['name'] ||= "#{from}--#{to}"
		params['from'] = from.to_s
		params['to'] = to.to_s
		params['from'] = from.to_dot_params['name'] if from.respond_to?(:to_dot_params)
		params['to'] = to.to_dot_params['name'] if to.respond_to?(:to_dot_params)
		add_vertex(params['from'])
		add_vertex(params['to'])
		e = RGL::DOT::DirectedEdge.new(params)
		self << e
		e
	end
	
	# returns element index based on its name
	def index(item)
		index = 0
		each_element do |e|
			return index if e.name==item.name
			index += 1
		end
		nil
	end
	
	def include_vertex?(vertex)
		each_vertex {|v| return true if v==vertex}
		false
	end

	def include_edge?(edge)
		each_edge {|e| return true if e==edge}
		false
	end
	
	def each_vertex
		each_element do |element|
			yield(element) if element.kind_of?(RGL::DOT::Node)
		end
	end

	def each_edge
		each_element do |element|
			yield(element) if element.kind_of?(RGL::DOT::Edge)
		end
	end
end 


# extend the functionality of ConnectionScheme to use dot
class ConnectionScheme
	
	def to_dot_graph
		begin; require 'rubygems'; rescue LoadError; end
		require 'rgl/dot'
		dot = RGL::DOT::Digraph.new
		@pipelines.each do |p|
			dot.add_edge(p.src_kernel, p.entrance)
			dot.add_edge(p.entrance, p.conduit)
			dot.add_edge(p.conduit, p.exit)
			dot.add_edge(p.exit, p.tgt_kernel)
		end
		dot
	end
	
	def write_to_graphic_file
		to_dot_graph.write_to_graphic_file
	end

	
end


# extend the functionality of Conduit to use dot
class Conduit
	
	def to_dot_params
		{'fontsize'=>12, 'shape'=>"point",
		'name'=>to_s,
		'label'=>to_s,
		}
	end
end
