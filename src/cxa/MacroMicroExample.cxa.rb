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

=end

# configuration file for a MUSCLE CxA
abort "this is a configuration file for to be used with the MUSCLE bootstrap utility" if __FILE__ == $0

# configure cxa properties
cxa = Cxa.LAST

cxa.env["max_timesteps"] = 4
cxa.env["macro:dt"] = 1
cxa.env["micro:dt"] = "1 ms"
cxa.env["micro:T"] = "1 ms"
cxa.env["cxa_path"] = File.dirname(__FILE__)

# declare kernels
cxa.add_kernel('macro', 'examples.macromicrosubmodel.Macro')
cxa.add_kernel('micro', 'examples.macromicrosubmodel.Micro')

# configure connection scheme
cs = cxa.cs

cs.attach('macro' => 'micro') {
	tie('macroObs', 'macroObs')
}

cs.attach('micro' => 'macro') {
	tie('microObs', 'microObs')
}
