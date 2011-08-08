#! /usr/bin/env ruby

# == Copyright and License
# Copyright 2008,2009 Complex Automata Simulation Technique (COAST) consortium
# 
# GNU Lesser General Public License
# 
# This file is part of MUSCLE (Multiscale Coupling Library and Environment).
# 
#     MUSCLE is free software: you can redistribute it and/or modify
#     it under the terms of the GNU Lesser General Public License as published by
#     the Free Software Foundation, either version 3 of the License, or
#     (at your option) any later version.
# 
#     MUSCLE is distributed in the hope that it will be useful,
#     but WITHOUT ANY WARRANTY; without even the implied warranty of
#     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#     GNU Lesser General Public License for more details.
# 
#     You should have received a copy of the GNU Lesser General Public License
#     along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
#
#
# == Synopsis
# This is the main build script for MUSCLE (Multiscale Coupling Library and Environment).
#
# == Author
# Jan Hegewald
#

# version warning
puts "warning: this script has not been tested with your version of ruby (#{RUBY_VERSION})" unless RUBY_VERSION < "1.9"

require 'fileutils'
include FileUtils::Verbose

# some utility methods
module Misc
	#
	def Misc.run(cmd)
		if $env[:verbose]
			puts "executing command:" if $env[:execute]
			puts cmd
			STDOUT.flush
		end

		if $env[:execute] # commands output will go where the output this ruby script goes
			unless system(cmd)
				puts "error: last command failed with error code #{$?.exitstatus}"
				exit $?.exitstatus
			end
		end
	end


	#
	def Misc.find_by_suffix(paths, suffix, skipdirs=[])
		require 'find'
			
		java_files = []
		skipdirs += %w(.svn .hg)
		
		paths.each do |path|
			Find.find(path) do |item|
				Find.prune if skipdirs.include?(File.basename(item))
				if File.extname(item).downcase == suffix
					java_files << item
				end
			end
		end

		java_files
	end


	# javac
	def Misc.javac(build_dir, sources, classpaths)
		#flags = %w(-nowarn -Xlint:none)
		flags = %w(-encoding ISO8859-1 -Xlint:deprecated)
		dst_dir = "#{$env[:muscle_dir]}/#{build_dir}/intermediate/classes"
		# rm files from previous build
		rm_rf dst_dir if File.directory?(dst_dir)
		mkdir_p dst_dir # make sure dir exists
		# javac
		Misc.run "javac#{" "+flags.join(' ') unless flags.empty?}#{" -classpath "+classpaths.join(File::PATH_SEPARATOR) unless classpaths.empty?} -d #{dst_dir} #{sources.join(' ')}"
	end

	# jar
	def Misc.jar(build_dir, output_name)

		classes_dir = "#{$env[:muscle_dir]}/#{build_dir}/intermediate/classes"
		classes = Misc.find_by_suffix([classes_dir], ".class")
		classes.map! {|item| item[classes_dir.length+1..-1]} # paths must be relative to classes dir
		classes.map! {|item| item.gsub(/\$/, '\$')} # escape the $ in nested class names to be able to pass them to the jar utility
		classes.map! {|item| "-C #{$env[:muscle_dir]}/#{build_dir}/intermediate/classes #{item}"} # let jar cd to our classes dir to be able to find the files
		Misc.run "jar -cf #{$env[:muscle_dir]}/#{build_dir}/#{output_name}.jar #{classes.join(' ')}"
	end

end

#
module Targets
	# default action
	def Targets.default
		cpp # the java stuff usually does not need to be compiled
	end

	# build all
	def Targets.all
		java
		cpp
		otf
	end

	# javac
	def Targets.java
		classpaths = Dir["#{$env[:muscle_dir]}/thirdparty/*.jar"]
		src_dir = "#{$env[:muscle_dir]}/src/java"
		sources = Misc.find_by_suffix([src_dir, "#{$env[:muscle_dir]}/src/cpp"], ".java", %w(development))

		Misc.javac "build", sources, classpaths
		
		# jar
		Misc.jar "build", "muscle"
	end

	# cmake, make
	def Targets.cpp
		java
		flags = %w()
		
		# configure for 64bit build on a mac
		require 'rbconfig'
		if (Config::CONFIG['host_os'] =~ /darwin/) != nil
			flags << "-DCMAKE_OSX_ARCHITECTURES:STRING=x86_64"
		end
		
		cmake_dst = "#{$env[:muscle_dir]}/build/intermediate/cmake"	
		mkdir_p cmake_dst # make sure dir exists
		# cmake
		# always create new cache file, so the user will not be bothered if e.g. CMakeCache.txt expects a different path
		rm "#{cmake_dst}/CMakeCache.txt" if File.exists?("#{cmake_dst}/CMakeCache.txt")
		Misc.run "#{$env[:muscle_dir]}/src/ruby/cmake.rb #{cmake_dst} #{flags.join(' ')} -DLIB_NAME=muscle #{$env[:muscle_dir]}"

		# make (this will probably fail on windows)
		Misc.run "make -w -j4 --directory=#{cmake_dst}"
		Misc.run "make -w --directory=#{cmake_dst} install"
	end
	
	# compile otf part
        def Targets.otf
		cpp
		Misc.run "which java"
		ENV['OTF_HOME'] = $env[:options][:otf]
		ENV['MUSCLE_DIR'] = "#{$env[:muscle_dir]}"

                make_dst = "#{$env[:muscle_dir]}/build/intermediate/otf"
		mkdir_p make_dst # make sure dir exists

                cp "#{$env[:muscle_dir]}/src/java/muscle/utilities/Makefile", "#{make_dst}/"
		Misc.run "make -w --directory=#{make_dst} install"
        end

	def Targets.install 
		prefix = $env[:options][:prefix]
		puts "Installing MUSCLE to: #{prefix}"

		Misc.run "sed 's|_PREFIX_|#{prefix}|' #{$env[:muscle_dir]}/scripts/muscle.in > #{$env[:muscle_dir]}/build/muscle"

		mkdir_p "#{prefix}/bin" # make sure dir exists
		mkdir_p "#{prefix}/lib" # make sure dir exists
		mkdir_p "#{prefix}/share/muscle/java/thirdparty" # make sure dir exists

		FileUtils.install "#{$env[:muscle_dir]}/build/muscle", "#{prefix}/bin", :mode => 0755, :verbose => true

		cp_r Dir.glob("#{$env[:muscle_dir]}/build/*.so"), "#{prefix}/lib"
		cp_r Dir.glob("#{$env[:muscle_dir]}/build/*.jar"), "#{prefix}/share/muscle/java"
		cp_r Dir.glob("#{$env[:muscle_dir]}/thirdparty/*.jar"), "#{prefix}/share/muscle/java/thirdparty"
		Misc.run "cd #{$env[:muscle_dir]}/src/cpp/muscle ; find . \\( -wholename '*.h' -o -wholename '*FindJNI*cmake' \\) -print0 | cpio -0pdmu #{prefix}/include"
		Misc.run "cd #{$env[:muscle_dir]}/scripts/OTF ; find . ! -wholename '*.svn*' -print0 | cpio -0pdmu #{prefix}/share/muscle/OTF"
		Misc.run "cd #{$env[:muscle_dir]}/doc ; find . ! -wholename '*.svn*' -print0 | cpio -0pdmu #{prefix}/share/muscle/doc"
		Misc.run "cd #{$env[:muscle_dir]}/src/ruby ; find . ! -wholename '*.svn*' -print0 | cpio -0pdmu #{prefix}/share/muscle/ruby"
		Misc.run "cd #{$env[:muscle_dir]}/src/resources ; find . ! -wholename '*.svn*' -print0 | cpio -0pdmu #{prefix}/share/muscle/resources"
	end

	def Targets.clean
		rm_rf Dir.glob("#{$env[:muscle_dir]}/build/intermediate") if File.directory?("#{$env[:muscle_dir]}/build/intermediate")
		rm_rf Dir.glob("#{$env[:muscle_dir]}/build/*.so")
	end


	# remove everything in the build directory
	def Targets.clobber
		rm_rf Dir.glob("#{$env[:muscle_dir]}/build/*")
	end

end

def get_opts
	require 'optparse'

	# Configure an OptionParser.
	parser = OptionParser.new

	parser.on('-h', '--help', 'displays usage information') do
  		puts parser
  		exit
	end

	parser.on('-t=TARGET', '--target=TARGET', %w(default all java cpp otf install clean clobber), 'name of the target to run: default, all, java, cpp, otf, install, clean, clobber') do |v|
  		$env[:options][:target] = v
	end

	parser.on('-p=PREFIX', '--prefix=PREFIX', 'installation prefix, default is /opt/muscle') do |v|
  		$env[:options][:prefix] = v
	end

	parser.on('-o=OTF', '--otf=OTF', 'OTF library location') do |v|
  		$env[:options][:otf] = v
	end

	parser.on('-j=JAVA', '--java=JAVA', 'java location, default is $JAVA_HOME') do |v|
  		$env[:options][:java] = v
	end

	# Parse command-line options.
	begin
  		parser.parse($*)
		rescue OptionParser::ParseError
  		puts $!
  		exit
	end
end
#
def main
	$env = {}
	$env[:execute] = true
	$env[:verbose] = true
	$env[:muscle_dir] = File.dirname(File.expand_path(__FILE__))
	
	# assert used dir is a muscle dir
	if( (%w(doc src thirdparty)-Dir.entries($env[:muscle_dir])).size != 0 )
		abort "can not run: used directory <#{$env[:muscle_dir]}> does not seem to be a muscle directory"
	else
		puts "executing in muscle directory <#{$env[:muscle_dir]}>"
	end
	
	$env[:options] = {:target=>'default', :prefix=>'/opt/muscle/', :otf=>'', :java=>ENV['JAVA_HOME'], :final=>false}
	get_opts 
	target = $env[:options][:target]

	ENV['JAVA_HOME'] = $env[:options][:java]
	puts "Using Java from: #{ENV['JAVA_HOME']}"
	ENV['PATH'] = "#{$env[:options][:java]}/bin:#{ENV['PATH']}"
	Dir.mkdir("build") if !File.directory?("build")

	require 'benchmark'
	benchmark = Benchmark.realtime do
		if (Targets.methods-Targets.class.superclass.methods).include? target
			Targets.method(target).call
		else
			abort "unknown target: #{target}}"
		end
	end
	
	puts "building [#{target}] took "+sprintf("%.0f", benchmark)+" second(s)."
end

# 
# !!!: begin
#
main if __FILE__ == $0

