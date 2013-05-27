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

# configuration file for a MUSCLE CxA
abort "this is a configuration file for to be used with the MUSCLE bootstrap utility" if __FILE__ == $0

# configure cxa properties
cxa = Cxa.LAST

cxa.env["max_timesteps"] = 1;
cxa.env["default_dt"] = 1

cxa.env["Check:num_seeds"] = 1

cxa.env["Bounce:command"] = ENV['MUSCLE_HOME'] + "/share/muscle/examples/dataintegrity/bounce"
cxa.env["Check:command"] = ENV['MUSCLE_HOME'] + "/share/muscle/examples/dataintegrity/check"

# declare kernels
cxa.add_kernel('Bounce', 'muscle.core.standalone.NativeKernel')
cxa.add_kernel('Check', 'examples.dataintegrity.Check')

# configure connection scheme
cs = cxa.cs

cs.attach('Check' => 'Bounce') {
	tie('datatype', 'datatype')
	# 4.71s user 0.66s system 81% cpu 6.566 total
	#tie('out', 'in',["serialize","chunk_32","thread","compress"],["decompress", "dechunk_32", "deserialize"])
	# 4.71s user 0.63s system 80% cpu 6.602 total 
	#tie('out', 'in',["serialize","chunk_32","compress"],["decompress", "dechunk_32", "deserialize"])
	# 4.00s user 0.55s system 79% cpu 5.708 total
	#tie('out', 'in',["serialize","chunk_4","thread","compress"],["decompress", "dechunk_4", "deserialize"])
	# 3.98s user 0.55s system 80% cpu 5.641 total
	#tie('out', 'in',["serialize","compress"],["decompress", "deserialize"])
	# 3.93s user 0.55s system 79% cpu 5.617 total
	tie('out', 'in',["serialize","chunk_4","compress","thread"],["decompress", "dechunk_4", "deserialize"])
}

cs.attach('Bounce' => 'Check') {
	# 4.71s user 0.66s system 81% cpu 6.566 total
	#tie('out', 'in',["serialize", "chunk_32", "thread","compress"],["decompress", "dechunk_32", "deserialize"])
	# 4.71s user 0.63s system 80% cpu 6.602 total
	#tie('out', 'in',["serialize", "chunk_32", "compress"],["decompress", "dechunk_32", "deserialize"])
	# 4.00s user 0.55s system 79% cpu 5.708 total
	#tie('out', 'in',["serialize","chunk_4","thread","compress"],["decompress", "dechunk_4", "deserialize"])
	# 3.98s user 0.55s system 80% cpu 5.641 total
	#tie('out', 'in',["serialize", "compress"],["decompress", "deserialize"])
	# 3.93s user 0.55s system 79% cpu 5.617 total
	tie('out', 'in',["serialize","chunk_4","compress","thread"],["decompress", "dechunk_4", "deserialize"])
}
