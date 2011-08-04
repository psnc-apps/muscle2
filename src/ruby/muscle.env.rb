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

# default configuration file for the MUSCLE bootstrap utility
abort "this is a configuration file for to be used with the MUSCLE bootstrap utility" if __FILE__ == $0


m = Muscle.LAST


# some local variables to keep things simple
base_dir = ENV["MUSCLE_HOME"]
#
## configure java CLASSPATH
## classpath must include jade and muscle classes
## remember: path separator is ';' on windows systems, else ':' better just use File::PATH_SEPARATOR
## be careful: ENV["CLASSPATH"] might be nil or an empty string
#m.add_classpath ENV["CLASSPATH"].split(File::PATH_SEPARATOR) if ENV["CLASSPATH"] != nil
#m.add_classpath File.expand_path(File.join(base_dir, "build", "muscle.jar"))
#m.add_classpath Dir.glob("#{base_dir}/thirdparty/*.jar")

m.add_classpath default_classpaths

# configure native library path (e.g. LD_LIBRARY_PATH, DYLD_LIBRYRA_PATH or PATH)
# some of the settings below are mandatory
# add our standard path for native libraries
require 'utilities'
assert_LIBPATHENV(ENV)
m.add_libpath "#{ENV[ENV['LIBPATHENV']]}"
m.add_libpath "#{base_dir}/lib"

# use 50% of available physical ram for jvm maximum
max_ram_size_m = (ram_size*0.50).to_i/1024/1024 # if we specify a ram amount for java, it must be a multiple of 1024 bytes

e = {
"bootclass" => "muscle.core.Boot",
'execute' => true,
'verbose' => false,
'quiet' => false,
'java' => 'java', # java command
'Xms' => '256m', # default JVM heap size minimum
'Xmx' => "#{max_ram_size_m}m", # default JVM heap size maximum
'Xss' => '16m', # default stack size for the JVM thread
'rmagui' => false,
'jade' => true,
'test' => false,
#'jade_wd' => nil, # value for the JADE output dir (-file-dir flag)
# use a private port as default http://de.wikipedia.org/wiki/Port_%28Protokoll%29
# we create a different port for each user so multiple independent environments can be executed at the same time
'mainport' => (50301+Process.uid),
'changeportifbusy' => false, # only available with leap
'leap' => true,
'localport' => nil,
"allkernels" => false,
"autoquit" => false,
#:kernelinfo => false,
#'platformname' => "",
'main_container_name' => "Main-Container", # default JADE name for Main-Container, beware if you change this: some JADE/thirdparty code relies on it
'print_env' => false,
# configure java logging
'logging_config_path' => File.join(base_dir, "resources/logging/logging.properties"),
'tmp_path' => mkJVMTmpPath,
'muscle_src_root' => find_src_root,
}
#e['muscle.Env dump uri'] = URI.parse "file:#{File.join(e['tmp_path'], Muscle.jclass)}"
e['muscle.Env dump uri'] = Proc.new {URI.parse "file:#{File.join(Muscle.LAST.env['tmp_path'], Muscle.jclass)}"}

e['muscle.behaviour.KillPlatformBehaviour'] = {"logging_after_teardown_properties_path" => File.join(base_dir, "resources/logging/logging.after_teardown.properties")}


m.add_env(e)




