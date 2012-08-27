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

require 'jade'
require 'connectionscheme'

if File.symlink? __FILE__
	PARENT_DIR = File.dirname(File.expand_path(File.readlink(__FILE__)))
else
	PARENT_DIR = File.dirname(File.expand_path(__FILE__))
end unless defined? PARENT_DIR

class Cxa
	def initialize(cxa_file = nil, rootenv=Muscle.LAST.env)
		@@LAST = self
		@cs = ConnectionScheme.new
		
		# create an env entry in root env
		rootenv[self.class.jclass] = {}
		@env = rootenv[self.class.jclass]
		
		@known_agents = []
		def @known_agents.<<(val)
			if(i=index(val))
				puts "overwriting agent <#{self[i]}> with new configuration <#{val}>" unless self[i].eql?(val)
			end
			super
		end

		@env_basename = "cxa.env.rb"
		
		# load (machine specific) default env
		load_env(File.expand_path("#{PARENT_DIR}/#{@env_basename}"), true)
		# load cxa specific env
		load_env(File.expand_path(cxa_file), true) unless cxa_file.nil?	
	end
	
	def Cxa.jclass
		'muscle.core.CxADescription'
	end

	def Cxa.LAST
		@@LAST
	end
	
	# convenience method for config file
	def add_kernel(*args)
		add KernelAgent.new(*args)
	end
	
	# convenience method for config file
	def add_terminal(*args)
		add TerminalAgent.new(*args)
	end

	def add(x)
		if x.kind_of?(JadeAgent)
			known_agents << x
		else
			puts "#{__FILE__}:#{__LINE__} error: do not know how to add #{x}"
		end
	end
	
	def generate_cs_file
	  File.open(env['muscle.core.ConnectionScheme legacy_cs_file_uri'].path, "w") do |f|
      f.puts "# DO NOT EDIT! This is file is generated automatically by <#{__FILE__}> at #{Time.now}"
      f.puts cs.to_s
    end
  end
  
	# visibility
	attr_reader :env, :cs, :known_agents
end


## test
if $0 == __FILE__
	cxa = Cxa.new("foo/bar")

	cxa.env.merge!({123=>456})
	cxa.env["kin_viscosity[m2/s]"] = "4E-6"
	puts cxa.inspect
end
