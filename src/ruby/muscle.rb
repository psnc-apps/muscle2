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
# This is the bootstrap utility for MUSCLE (Multiscale Coupling Library and Environment).
# 	[1] http://muscle.berlios.de
#	[2] http://www.complex-automata.org
#
# == Author
# Jan Hegewald
#

# version warning
#puts "warning: this script has not been tested with your version of ruby (#{RUBY_VERSION})" unless RUBY_VERSION < "1.9"

#module Muscle
if File.symlink? __FILE__
	PARENT_DIR = File.dirname(File.expand_path(File.readlink(__FILE__)))
else
	PARENT_DIR = File.dirname(File.expand_path(__FILE__))
end unless defined? PARENT_DIR
$LOAD_PATH << PARENT_DIR

require 'utilities'
require 'cli'
require 'cxa'
include MuscleUtils
require 'uri'

# see if we are on a windows box
if host_os.include?('windows')
	$mswin = true
else
	$mswin = false
end

# modify Hash so it will return evaluated proc objects if value is a proc
class Hash
	alias get [] # copy original [] method
	def [](k)
		v = get(k)
		return v.call if v.kind_of?(Proc) && v.arity == -1 # proc with no args
		v
	end

	def evaluate(key=nil)
		# return a copy of self with proc objects replaced with their call results
		if key==nil
			# loop each value and replace with evaluated results if it is a proc
			h={}
			each_key do |k|
				h[k] = evaluate(k)
			end
			return h			
		# return a the value for the provided key with proc objects replaced with their call results
		elsif key != nil
			# loop each value and replace with evaluated results if it is a proc
			val = self[key]
			if val.kind_of?(Proc) && (RUBY_VERSION < "1.9" ? (val.arity == -1) : (val.arity == 0)) # proc with no args
				return val.call
			elsif val.kind_of?(Hash)
				# recurse for nested hashes
				return val.evaluate
			end				
			return val			
		end
	end
end

#
class Muscle
	def initialize
		@@LAST = self
		@env = {}

		# set value for LIBPATHENV or abort
		assert_LIBPATHENV @env

		@env_basename = "muscle.env.rb"
		# load (machine specific) default env
		load_env(File.expand_path("#{PARENT_DIR}/#{@env_basename}"), true)
	end
	
	#
	def add_env(e)
		@env.merge!(e){|key, oldval, newval| 
				if(key == "CLASSPATH" && oldval != nil)
					oldval=newval+File::PATH_SEPARATOR+oldval
				else
					oldval=newval
				end}
	end

	# helper method to add path variables
	def add_path(hsh)
		hsh.each do |path_key,path|
			@env[path_key] = "" if @env[path_key] == nil
			if(path.class == Array)
				@env[path_key] = (@env[path_key].split(File::PATH_SEPARATOR) + path).join(File::PATH_SEPARATOR)
			else
				@env[path_key] = (@env[path_key].split(File::PATH_SEPARATOR) + path.split(File::PATH_SEPARATOR)).join(File::PATH_SEPARATOR)		
			end
			# delete any empty items
			@env[path_key] = ((@env[path_key].split(File::PATH_SEPARATOR)).delete_if {|x| x==''}).join(File::PATH_SEPARATOR)	
		end
	end


	def add_classpath(p)
		add_path("CLASSPATH"=>p)
	end


	def add_libpath(p)
		add_path("libpath"=>p)
		ENV[@env["LIBPATHENV"]] = @env["libpath"]
	end

	# overwrite env setting
	def set(hsh)
		hsh.each do |k,v|
			@env[k] = v
		end
	end

	def Muscle.jclass
		'muscle.Env'
	end
	
	def Muscle.LAST
		@@LAST
	end
			
	# visibility
	attr_reader :env, :env_basename
end


require 'pp'
# !!!: begin

cli = MuscleCli.new

# see if we should do anything at all
if ARGV.size == 0
	RDoc::usage_no_exit('Synopsis')
	puts cli.help
	exit
end

m = Muscle.new

args, cli_env = cli.parse ARGV

# load cxa specific muscle env, if exists
#if cli_env.has_key?('cxa_file')
#	load_env(File.expand_path("#{File.dirname(cli_env['cxa_file'])}/#{m.env_basename}"))
#end

# add cli muscle env
m.add_env cli_env

# load (machine specific) user specific muscle env (~ BASENAME MACHINE?)
begin
	load_env(File.expand_path("~/#{m.env_basename}"))
rescue ArgumentError # File.expand_path("~") usually leads to an error on Windows machines if the HOME environment is not set
	puts "#{__FILE__}:#{__LINE__} warning: can not load <\"~/#{m.env_basename}\">"
end

# if we manually assign a port to a container, the container gets this port as its name
unless m.env['container_name'].nil?
	if m.env['main']
		m.env['container_name'] = m.env['main_container_name']#"Main-Container" # default JADE name for Main-Container, beware if you change this: some JADE/thirdparty code relies on it
	else
		m.env['container_name'] = m.env['localport'].to_s if m.env['localport'] != nil
	end
end

# !!!: load cxa configuration
if m.env.has_key?('cxa_file')
	cxa = Cxa.new(m.env['cxa_file'], m.env)
end

#
if m.env['print_env'] != false
	if m.env['print_env'] == true
		# print the complete env (sorted)
		m.env.keys.sort.each {|k| puts "#{k.inspect}=>#{m.env[k].inspect}"}
	else
		# print value for the specified key(s)
		if(m.env['print_env'].size == 1)
			# print raw value if output is for a single key (useful if you want to further process the output, e.g. CLASSPATH)
			puts m.env[m.env['print_env'].first] if m.env.has_key? m.env['print_env'].first
		else
			m.env['print_env'].each {|k| puts "#{k.inspect}=>#{m.env[k].inspect}" if m.env.has_key? k}
		end
	end
	exit
end


# our env must contain some mandatory flags
# without these in our env we can not continue
$mandatoryJVMKeys = %w()#%w(CLASSPATH)
mandatoryKeys = %w()
mandatoryKeys += $mandatoryJVMKeys

# regardles how we got our args (via cli, config file or both)
# we have to know some mandatory values, else we abort here
mandatoryKeys.each do |key|
	if not m.env.has_key?(key)
		abort("Error: missing flag <"+key+"> -- provide it via config file or cli flag\nsee #{File.basename(__FILE__)} --help")
	end
end

# test paths
if m.env['test_paths'] != false
	m.env['test_paths'].each do |key|
		m.env[key].split(File::PATH_SEPARATOR).each do |path|
			assert_path(path, __LINE__)
		end unless m.env[key].nil?	# only test if key exists
	end unless m.env['test_paths'].nil? 
	
	
	#m.env["CLASSPATH"].split(File::PATH_SEPARATOR).each do |path|
	#	assert_path(path, __LINE__)
	#end
	#assert_file(m.env['logging_config_path'], __LINE__)
	## test path to cxa directory
	#assert_file(m.env['cxa_file'], __LINE__) if (m.env.has_key?('cxa_file') && !m.env.has_key?('sandbox'))
	##assert_path_w(m.env['jade_wd'], __LINE__)
	#
	## test path to cxa tmp directory
	#assert_path_w(m.env['tmp_path'], __LINE__) if m.env.has_key?('tmp_path')


	# test jade classpath
	if m.env['leap']
		if not m.env["CLASSPATH"].downcase.include?("jadeleap")
			abort("Error: we are in JADE-leap mode but the classpath does not contain JadeLeap.jar")
		end
	else
		if not m.env["CLASSPATH"].downcase.include?("jade")
			abort("Error: we are in JADE-basic mode but the classpath does not contain jade.jar")
		end
	end
end

# test port ranges
['mainport', 'localport'].each do |p|
	assert_port(m.env[p], p.to_s) if m.env[p] != nil
end

# print tmp_path before and after execution
puts "\ttmp dir is: <#{Muscle.LAST.env['tmp_path']}>\n\n"
at_exit {puts "\n\ttmp dir was: <#{Muscle.LAST.env['tmp_path']}>"}

# switch to sandbox mode
if m.env.has_key?('sandbox')
	require 'sandbox'
	do_sandbox(m.env['sandbox'])
	exit
end

# switch to test mode
if m.env['test']
	doTests(m.env['testinputs'])
	exit
end

# !!!: switch to jade mode
if m.env['jade']
	# we assume the remaining args are agent classes to launch

	# merge cxa kernels with global agent shortcuts
	boot_agents = [] # agent to boot
	args.each do |a|
		unless cxa.nil?
			cxa.known_agents << JadeAgent.agent_from_string(a, cxa.known_agents)
			boot_agents << cxa.known_agents.last
		else
			abort("unknown agent <#{a}>, maybe there is no cxa configured?")
		end
	end
	if m.env["allkernels"] && !cxa.nil?
		# automatically boot all predefined kernel agents
		(cxa.known_agents.find_all {|a| a.kind_of?(KernelAgent)}).each do |k|
			boot_agents << k		
		end
	end
	
	# switch to cxa-info mode
	if(boot_agents.size == 0 && m.env.has_key?('cxa_file') && !m.env['main'])
		ks = cxa.known_agents.find_all {|a| a.kind_of?(KernelAgent)}
		s = "#{ks.length} known kernels in CxA:\n"
		ks.each {|k| s += "\t#{k.name}: #{k.cls}\n"}
		as = cxa.known_agents.find_all {|a| a.kind_of?(JadeAgent)&&!a.kind_of?(KernelAgent)}
		s += "#{as.length} known administration agents in CxA:\n"
		as.each {|a| s += "\t#{a.name}: #{a.cls}\n"}
		puts s
		exit
	else
		command = Jade.build_command(boot_agents, m.env).first
	end
else
	command = JVM.build_command(args.first, m.env).first
	if args.size > 1
		# pass remaining args to the java class to launch
		command += " " + args[1..-1].join(' ')
	end
end

unless cxa.nil?
	# dump cs file in legacy format
	File.open(cxa.env['muscle.core.ConnectionScheme legacy_cs_file_uri'].path, "w") do |f|
		f.puts "# DO NOT EDIT! This is file is generated automatically by <#{__FILE__}> at #{Time.now}"
		f.puts cxa.cs.to_s
	end
end


# write dot file of CS
#require 'csgraph'
#system("open -a Preview #{cxa.cs.write_to_graphic_file}")


# !!!: run command
exit_value = run_command(command, m.env)
#puts "command was #{$0} #{ARGV.join(' ')}"
exit exit_value if exit_value != nil


#end# module Muscle
