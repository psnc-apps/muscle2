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

if File.symlink? __FILE__
	PARENT_DIR = File.dirname(File.expand_path(File.readlink(__FILE__)))
else
	PARENT_DIR = File.dirname(File.expand_path(__FILE__))
end unless defined? PARENT_DIR
$LOAD_PATH << PARENT_DIR


#
class ConnectionScheme

	def initialize
		super()
		@@LAST=self
		@pipelines = []
	end
	
	def ConnectionScheme.LAST
		@@LAST
	end
			
	#
	def attach(hsh)
		raise("can only accept hash with one key/val") if hsh.length != 1
		if block_given?
			# store values we need for the #tie, which should be called in the block
			@current_src_kernel = hsh.keys.first
			@current_tgt_kernel = hsh[@current_src_kernel]
			raise("can not be nil: #{@current_src_kernel.inspect} #{@current_tgt_kernel.inspect}") if @current_src_kernel.nil? || @current_tgt_kernel.nil?

			yield
			@current_src_kernel = nil
			@current_tgt_kernel = nil
		else
			raise("empty attach for #{hsh.inspect}")
		end
	end
	
	#
	def tie(entrance_name, exit_name=entrance_name, conduit=Conduit.new(Cxa.LAST.env[Cxa.LAST.env['CONNECTION_SCHEME_CLASS']]['default_conduit']))
		abort("first arg to tie can not be a #{entrance_name.class}") if(entrance_name.class==Hash) # sanity check
		tie_single_args(entrance_name, exit_name, conduit)
	end


	# call with (entrance, exit, [conduit])
	def tie_single_args(entrance_name, exit_name, conduit)

		entrance = ConduitEntrance.new(@current_src_kernel, entrance_name)
		exit = ConduitExit.new(@current_tgt_kernel, exit_name)

		@pipelines << Pipeline.new(@current_src_kernel, entrance, conduit, exit, @current_tgt_kernel)
	end

	# alternative version, can be called with a hash do define the entrance=>exit
	# call with (entrance => exit)
	# or ({entrance => exit}, conduit)
	def tie_hash_arg(hsh, conduit)
		raise("can only accept hash with one key/val") if hsh.length != 1
		tie_single_args(hsh.keys.first, hsh.values.first, conduit)
	end
	
	# produces cs in old style format
	# {exit name} {{full class name of conduit}[#ID][(carg0,cargn)]} {entrance name}
	# e.g.:
	# data@r muscle.core.conduit.AutomaticConduit#A(multiply_2) data@w
	def to_s
		lines = []
		@pipelines.each do |p|
			lines << "#{p.exit} #{p.conduit} #{p.entrance}"
		end
		lines.join("\n")
	end
		
	def ConnectionScheme.jclass
		'muscle.core.ConnectionScheme'
	end
	
	# visibility
	private :tie_hash_arg, :tie_single_args
end
# helper method for setup files, redirect tie method to ConnectionScheme
unless respond_to?(:tie, true)
	def tie(*args)
		ConnectionScheme.LAST.tie(*args)
	end
else
	raise("method #{self}#tie already defined")
end

#
class Pipeline
	def initialize(src_kernel, entrance, conduit, exit, tgt_kernel)
		@src_kernel = src_kernel
		@entrance = entrance
		@conduit = conduit
		@exit = exit
		@tgt_kernel = tgt_kernel
	end
	attr_reader :src_kernel, :entrance, :conduit, :exit, :tgt_kernel
end

#
class Conduit
	def initialize(cls, args=[])
		@cls = cls
		@args = args
		@@counter||=-1
		@@counter+= 1
		@name = @@counter.to_s
	end
	
	def dup
		@@counter+= 1
		super
	end
	
	def to_s
		"#{@cls}##{@name}#{"(#{@args.join(',')})" unless @args.empty?}"
	end	
end


class Portal
	def initialize(kernel, name)
		@kernel = kernel
		@name = name
	end
	
	def to_s
		"#{@name}@#{@kernel}"
	end
end
class ConduitEntrance < Portal
end
class ConduitExit < Portal
end


## test
if $0 == __FILE__
	cs = ConnectionScheme.new

	cs.attach("k0"=>"k1") {
		tie("entrance0" => "exit0")
		tie("entrance1" => "exit1")
	}
#	cs.attach("k1"=>"k0") {
#		tie("entrance0" => "exit0")
#	}
	cs.attach("k2"=>"k3") {
		tie("entrance0" => "exit0")
		tie("entrance1" => "exit1")
	}

#	puts "writing dot file ..."
#	require 'csgraph'
#	system("open -a Preview #{cs.write_to_graphic_file}")
end
