#! /usr/bin/env ruby

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

== Synopsis
helper for cmake to be able to specify a output dir for cmake (specify global paths for cmake, because we will cd to another directoy)

== Usage
cmake.rb <build_dir> <args_for_cmake (e.g. cmakelists_dir)>
=end

abort("usage: cmake.rb <build_dir> <args_for_cmake (e.g. cmakelists_dir)>") if ARGV.size < 2

Dir.chdir(ARGV.shift)
exit($?.exitstatus) unless system("cmake #{ARGV.map {|x| "\"#{x}\""}.join(' ')}")
