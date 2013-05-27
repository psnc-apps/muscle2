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
=end
# configure cxa properties
cxa = Cxa.LAST

cxa.env["max_timesteps"] = 4
cxa.env["default_dt"] = 1
cxa.env["cxa_path"] = File.dirname(__FILE__)
cxa.env["src:file"] = File.dirname(__FILE__) + "/../resources/terminal_in.dat"
cxa.env["sink:file"] = 'boolMask'
cxa.env["sink:relative"] = true
cxa.env["sink:suffix"] = "dat"

# declare kernels
cxa.add_kernel('proc', 'examples.terminals.Processor')
cxa.add_terminal('src', 'muscle.core.conduit.terminal.DoubleFileSource')
cxa.add_terminal('sink', 'examples.terminals.BooleanFileSink')

# configure connection scheme
cs = cxa.cs

cs.attach('src' => 'proc') {
	tie('initialData')
}
cs.attach('proc' => 'sink') {
	tie('largeMask')
}
