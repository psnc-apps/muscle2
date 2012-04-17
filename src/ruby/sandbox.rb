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

#OBSOLATE - TO BE DELETED

def do_sandbox(targetKernel, env=Muscle.LAST.env)
	puts "running sandbox for kernel <#{targetKernel}>"
	
	# create moc cxa resources
	tmp_dir = TmpDir.create("MOC_CxA",false)
tmp_dir="/Users/hg/tmp/cxa/sb"	
	# locate cxa template
	cxaTemplatePath = File.join(env['muscle_src_root'], "resources", "sandbox_template")
	assert_dir(cxaTemplatePath, __LINE__)
	require 'fileutils'
	# copy template
	FileUtils::cp_r(cxaTemplatePath, tmp_dir)
	cxa_file = File.join(tmp_dir, File.basename(cxaTemplatePath), "sandbox.cxa.rb")
	
	# load custom cxa
#	if env.has_key?('cxa_file')
#
#	end
	
	# determine a name for our targetKernel
	name = targetKernel.split('.').last

	# launch sandbox environment
	command = "ruby #{$0} --main --tmp_path #{tmp_dir} --cxa_file #{cxa_file} muscle.core.Sandbox:muscle.core.Sandbox"
	command += " #{name}:#{targetKernel}"
	command += " "+escape("QuitMonitor:muscle.utilities.agent.QuitMonitor(#{name})")
	command += " --verbose" if env['verbose']
	command += " --jadegui" if env['rmagui']
	command += " --heap #{env['Xmx']}" if env.has_key?('Xmx')
	command += " --classpath #{env["CLASSPATH"]}" if env.has_key?("CLASSPATH")
puts "command:\n#{command}"
	run_command command, env
end
