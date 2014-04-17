#!/usr/bin/env ruby

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
   Joris Borgdorff
=end

require 'cxa'
require 'muscle.cls'

abort "Usage: #{$0} CXA_FILE" unless ARGV.size == 1

m = Muscle.new
m.env['cxa_file'] = ARGV[0]

begin
	# load CxA configuration
	cxa = m.load_cxa
rescue LoadError
	puts "CxA file <#{m.env['cxa_file']}> not found."
	exit 1
end

# Generate the connection scheme file
cxa.generate_cs_file

gui_class = 'muscle.monitor.ActivityController'
m.run_command('Monitor', gui_class, [])
m.await_processes

exit(0)
