
# configuration file for a MUSCLE CxA
abort "this is a configuration file for to be used with the MUSCLE bootstrap utility" if __FILE__ == $0

# configure cxa properties
cxa = Cxa.LAST

# #safe
#cxa.env["max_timesteps"] = 16*10 

#unsafe
cxa.env["same_size_runs"] = 30;

cxa.env["cxa_path"] = File.dirname(__FILE__)

cxa.env["steps"] = 10

cxa.env["tests_count"] = 15
cxa.env["preparation_steps"]=cxa.env["steps"]*cxa.env["same_size_runs"]
cxa.env["max_timesteps"] = cxa.env["tests_count"] * cxa.env["steps"] * cxa.env["same_size_runs"] + cxa.env["preparation_steps"];
cxa.env["start_kiB_per_message"] = 0;

cxa.env["default_dt"] = 1
cxa.env["Pong1:T"] = 1
cxa.env["Pong2:T"] = 1

# declare kernels
cxa.add_kernel('Pong1', 'examples.pingpong.Pong')
cxa.add_kernel('Pong2', 'examples.pingpongsubmodel.Pong')
cxa.add_kernel('dup', 'muscle.core.kernel.DuplicationMapper')
cxa.add_kernel('combine', 'examples.pingpongsubmodel.PongCombiner')
cxa.add_kernel('Ping', 'examples.pingpong.Ping')

# configure connection scheme
cs = cxa.cs

cs.attach('Ping' => 'dup') {
	tie('out', 'in')
}
cs.attach('dup' => 'Pong1') {
	tie('out1', 'in')
}
cs.attach('dup' => 'Pong2') {
	tie('out2', 'in')
}
cs.attach('Pong1' => 'combine') {
	tie('out', 'in1')
}
cs.attach('Pong2' => 'combine') {
	tie('out', 'in2')
}
cs.attach('combine' => 'Ping') {
	tie('out', 'in')
}


