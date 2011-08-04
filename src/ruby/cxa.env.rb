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

# default configuration file for a MUSCLE CxA
# some of the settings below are mandatory
abort "this is a configuration file for to be used with the MUSCLE bootstrap utility" if __FILE__ == $0

cxa = Cxa.LAST

cxa.env['tmp_path'] = Muscle.LAST.env['tmp_path']

cxa.env['plumber_class'] = "muscle.core.Plumber"
cxa.env[cxa.env['plumber_class']] = {}

# configure muscle.core.ConnectionScheme class with path to legacy CS
cxa.env['CONNECTION_SCHEME_CLASS'] = "muscle.core.ConnectionScheme"
cxa.env['muscle.core.ConnectionScheme legacy_cs_file_uri'] = URI.parse "file:#{File.join(cxa.env['tmp_path'], 'connection_scheme')}"
cxa.env['muscle.core.ConnectionScheme'] = {"default_conduit"=>"muscle.core.conduit.VoidConduit", "cs_file_uri"=>"#{cxa.env['muscle.core.ConnectionScheme legacy_cs_file_uri'].to_s}"}

cxa.known_agents << JadeAgent.new('plumber', 'muscle.core.Plumber')
#cxa.known_agents << JadeAgent.new('gui' => 'muscle.gui.remote.RemoteComponentTailAgent')



