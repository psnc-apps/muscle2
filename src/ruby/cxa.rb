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

require 'connectionscheme'
require 'components'
require 'instance'

if File.symlink? __FILE__
	PARENT_DIR = File.dirname(File.expand_path(File.readlink(__FILE__)))
else
	PARENT_DIR = File.dirname(File.expand_path(__FILE__))
end unless defined? PARENT_DIR

class Cxa
	@@LAST = nil
	
	def initialize(cxa_file = nil, rootenv=Muscle.LAST.env)
		@@LAST = self
		@components = Components.instance
		@cs = ConnectionScheme.new(self.components)
		
		# create an env entry in root env, and make it global
		rootenv[self.class.jclass] = {}
		@env = rootenv[self.class.jclass]		
		
		# load (machine specific) default env
		load_file "#{PARENT_DIR}/cxa.env.rb"
		# load user cxa
		unless cxa_file.nil?
			load_file cxa_file
			self.env["cxa_path"] = File.dirname(cxa_file)
		end
		
		self.env.merge!(self.components.env)
	end
	
	def load_file(cxa_file)
		# Set globals for use in the CxA file
		$env = self.env
		$muscle_connection_scheme = self.cs

    path = File.expand_path(cxa_file)
    begin
      load_env(path)
    rescue LoadError => e
      puts "Failed to load configuration file <#{path}>:"
      puts e.message
      exit(1)
    rescue ScriptError => e
      puts "Configuration file <#{path}> is not a valid Ruby file:"
      puts e.message
      stack_no_muscle = e.backtrace.reject { |x| x =~ /\/share\/muscle\/ruby\// }
      if not stack_no_muscle.empty?
        puts "====== Stack trace ======"
        puts stack_no_muscle
        puts "========================="
      end
      exit(1)
    end
    
		# After loading, remove the global $env variable again
		$env = nil
		$muscle_connection_scheme
	end
	
	def Cxa.jclass
		'muscle.core.CxADescription'
	end

	def Cxa.LAST
		@@LAST
	end
	
	def add_instance(*args)
		Instance.new(*args)
	end

	def add_kernel(*args)
		Instance.new(*args)
	end
	
	def add_terminal(*args)
		Terminal.new(*args)
	end
	
	def get(name)
		self.components.get(name)
 	end
	
	def instances
		self.components.instances
	end
	
	def generate_cs_file(file=env['muscle.core.ConnectionScheme legacy_cs_file_uri'].path)
	  File.open(file, 'w') do |f|
      f.puts "# DO NOT EDIT! This is file is generated automatically by <#{__FILE__}> at #{Time.now}"
      f.puts self.components.coupling_s
    end
  end
  
  	# visibility
	attr_reader :env, :cs, :components
end
