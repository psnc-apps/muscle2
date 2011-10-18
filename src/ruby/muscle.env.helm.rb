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

abort "this is a configuration file for to be used with the MUSCLE bootstrap utility" if __FILE__ == $0

m = Muscle.LAST

# load default config
#eval( File.read("#{File.dirname(__FILE__)}/muscle.env.rb"), binding, "#{File.dirname(__FILE__)}/muscle.env.rb")
load("#{File.dirname(__FILE__)}/muscle.env.rb")


# configure java CLASSPATH
# classpath must include jade and muscle classes
# remember: path separator is ';' on windows systems, else ':' better just use File::PATH_SEPARATOR
# be careful: ENV["CLASSPATH"] might be nil or an empty string

#m.set "CLASSPATH"=>nil # clear classpath
#m.add_classpath ENV["CLASSPATH"].split(File::PATH_SEPARATOR) if ENV["CLASSPATH"] != nil
#m.add_classpath File.expand_path("~/COAST/Framework/bin/java")
#m.add_classpath File.expand_path("~/COAST/Framework/bin/cpp")
#m.add_classpath Dir.glob("/Users/hg/COAST/Framework/thirdparty/jars/{*.jar,xstream/*.jar,jade3.6/{addons/*.jar}}")
#m.add_classpath Dir.glob("/Users/hg/COAST/Framework/thirdparty/jars/jung/*.jar")
#m.add_classpath Dir.glob("/Users/hg/COAST/JADE/JADE-add-ons/add-ons/testSuite/lib/*.jar")
#if !m.env['leap']
#	m.add_classpath Dir.glob("/Users/hg/COAST/Framework/thirdparty/jars/jade3.6/plain/*.jar")		
#else # leap
#	m.add_classpath Dir.glob("/Users/hg/COAST/Framework/thirdparty/jars/jade3.6/leap/*.jar")
#end

m.add_classpath Dir.glob(File.expand_path("~/COAST/Framework/build_development")+"/*.jar")
m.add_classpath "/Users/hg/NetBeansProjects/Supervisor/build/classes"

#m.add_classpath Dir.glob("/Users/hg/COAST/Framework/thirdparty/jung2-2_0/*.jar")

#add_classpath "/Users/hg/coast.irmb.bau.tu-bs.de/home/shared/coast"		
#add_classpath "/Users/hg/VirtualFluids/Xcode/java"		
#add_classpath "/Users/hg/VirtualFluids/Xcode_virtualfluids4pe/java"		
#add_classpath "/Users/hg/VirtualFluids/Xcode_rigidbodyPE/java"		
#add_classpath "/Users/hg/COAST/Framework/src/native/cpp/exampleCppAgent"		
#add_classpath "/Users/hg/COAST/Framework/src/native/fortran/exampleFortranAgent"		
#add_classpath "/Users/hg/VirtualFluids_MUSCLE/Xcode_virtualfluids_muscle/java"		

m.env['muscle_src_root'] = File.expand_path("~/COAST/Framework/src")
m.env['logging_config_path'] = File.join(m.env['muscle_src_root'], "resources/development/logging/logging.debug.properties")

m.add_libpath "/Users/hg/VirtualFluids_MUSCLE/Xcode_virtualfluids_muscle/Release"
m.add_classpath "/Users/hg/VirtualFluids_MUSCLE/Xcode_virtualfluids_muscle/java"

m.add_classpath "/Users/hg/COAST/Framework/src/ruby/development/examples/simplesinkagent/ruby.jar"
m.add_classpath "/Users/hg/bin/jruby/lib/jruby.jar"
m.add_classpath Dir.glob "/Users/hg/lib/*.jar"

