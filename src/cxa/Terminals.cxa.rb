# configure cxa properties
cxa = Cxa.LAST

cxa.env["max_timesteps"] = 4
cxa.env["default_dt"] = 1
cxa.env["cxa_path"] = File.dirname(__FILE__)
cxa.env["src:file"] = File.dirname(__FILE__) + "/../resources/terminal_in.dat"
cxa.env["sink:file"] = 'boolMask'
cxa.env["sink:relative"] = true
cxa.env["sink:suffix"] = "dat"

# declare kernels
cxa.add_kernel('proc', 'examples.terminals.Processor')
cxa.add_terminal('src', 'muscle.core.conduit.terminal.DoubleFileSource')
cxa.add_terminal('sink', 'examples.terminals.BooleanFileSink')

# configure connection scheme
cs = cxa.cs

cs.attach('src' => 'proc') {
	tie('initialData')
}
cs.attach('proc' => 'sink') {
	tie('largeMask')
}
