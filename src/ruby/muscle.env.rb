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

require 'tmpdir'
require 'utilities'

## configure java CLASSPATH
## classpath must include muscle classes
m = Muscle.LAST
m.add_classpath default_classpaths

# configure native library path (e.g. LD_LIBRARY_PATH, DYLD_LIBRARY_PATH or PATH)
# some of the settings below are mandatory
# add our standard path for native libraries
assert_LIBPATHENV(ENV)
m.add_libpath ENV[ENV['LIBPATHENV']].to_s
# Library path should be set in terminal, we will not append to it here.

logging_path = "#{ENV['MUSCLE_HOME']}/share/muscle/resources/logging"
tmp_path = mkTmpPath(Dir.tmpdir)

m.add_env({
'execute' => true,
'verbose' => false,
'quiet' => false,
'java' => 'java', # java command
'Xms' => '256m', # default JVM heap size minimum
'Xmx' => '2048m', # default JVM heap size maximum
'allkernels' => false,
'print_env' => false,
# configure java logging
'logging_config_path' => "#{logging_path}/logging.properties",
'logging_quiet_config_path' => "#{logging_path}/logging.quiet.properties",
'logging_verbose_config_path' => "#{logging_path}/logging.verbose.properties",
'logging_manager_config_path' => "#{logging_path}/logging.manager.properties",
'logging_quiet_manager_config_path' => "#{logging_path}/logging.quiet.manager.properties",
'logging_verbose_manager_config_path' => "#{logging_path}/logging.verbose.manager.properties",
'tmp_path' => tmp_path,
'port_min' => ENV['MUSCLE_PORT_MIN'],
'port_max' => ENV['MUSCLE_PORT_MAX'],
'stage_files' => [],
'gzip_stage_files' => [],
'sleep_when_idle' => '0',
Muscle.jclass => "#{tmp_path}/.muscle/#{Muscle.jclass}"
})
