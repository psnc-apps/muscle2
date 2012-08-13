
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

cxa.env["tests_count"] = 16
cxa.env["preparation_steps"]=cxa.env["steps"]*cxa.env["same_size_runs"]
cxa.env["max_timesteps"] = cxa.env["tests_count"] * cxa.env["steps"] * cxa.env["same_size_runs"] + cxa.env["preparation_steps"];
cxa.env["default_dt"] = 1

cxa.env["start_kiB_per_message"] = 0;

#cxa.env["Pong:command"] = ENV['MUSCLE_HOME'] + "/share/muscle/examples/pingpongcpp/pong"
cxa.env["Ping:command"] = ENV['MUSCLE_HOME'] + "/share/muscle/examples/pingpongcpp/ping"

# declare kernels
cxa.add_kernel('Pong', 'examples.pingpong.Pong')
cxa.add_kernel('Ping', 'examples.pingpongcpp.Ping')

# configure connection scheme
cs = cxa.cs

cs.attach('Ping' => 'Pong') {
	tie('out', 'in')
}

cs.attach('Pong' => 'Ping') {
	tie('out', 'in')
}
