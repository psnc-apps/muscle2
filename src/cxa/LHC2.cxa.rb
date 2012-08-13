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
Mariusz Mamonski
=end

# configuration file for a MUSCLE CxA
abort "this is a configuration file  to be used with the MUSCLE bootstrap utility" if __FILE__ == $0

# configure cxa properties
cxa = Cxa.LAST

cxa.env["max_timesteps"] = 1
cxa.env["default_dt"] = 1
cxa.env["cxa_path"] = File.dirname(__FILE__)

cxa.env["PSB:InitialEnergy"] = 1.2
cxa.env["PSB:DeltaEnergy"] = 0.1
cxa.env["PSB:MaxEnergy"] = 4.0
cxa.env["PSB:command"] = ENV['MUSCLE_HOME'] + "/share/muscle/examples/mpiring/PSB"
cxa.env["PSB:mpiexec_args"] = "-np 2"

cxa.env["LHC:DeltaEnergy"] = 0.2
cxa.env["LHC:MaxEnergy"] = 12.0
cxa.env["LHC:command"] = ENV['MUSCLE_HOME'] + "/share/muscle/examples/mpiring/LHC"
cxa.env["LHC:mpiexec_args"] = "-np 2"

# declare kernels
cxa.add_kernel('LHC', 'examples.mpiring.LHC2')
cxa.add_kernel('PSB', 'examples.mpiring.PSB2')

# configure connection scheme
cs = cxa.cs

cs.attach('PSB' => 'LHC') {
	tie('pipe-out', 'pipe-in')
}

