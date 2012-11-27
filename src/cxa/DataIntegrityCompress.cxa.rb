
# configuration file for a MUSCLE CxA
abort "this is a configuration file for to be used with the MUSCLE bootstrap utility" if __FILE__ == $0

# configure cxa properties
cxa = Cxa.LAST

cxa.env["max_timesteps"] = 1;
cxa.env["default_dt"] = 1

cxa.env["Check:num_seeds"] = 1

cxa.env["Bounce:command"] = ENV['MUSCLE_HOME'] + "/share/muscle/examples/dataintegrity/bounce"
cxa.env["Check:command"] = ENV['MUSCLE_HOME'] + "/share/muscle/examples/dataintegrity/check"

# declare kernels
cxa.add_kernel('Bounce', 'muscle.core.standalone.NativeKernel')
cxa.add_kernel('Check', 'examples.dataintegrity.Check')

# configure connection scheme
cs = cxa.cs

cs.attach('Check' => 'Bounce') {
	tie('datatype', 'datatype')
	tie('out', 'in',["muscle.core.conduit.filter.SerializeFilter","muscle.core.conduit.filter.CompressFilter"],["muscle.core.conduit.filter.DecompressFilter","muscle.core.conduit.filter.DeserializeFilter"])
}

cs.attach('Bounce' => 'Check') {
	tie('out', 'in', ["muscle.core.conduit.filter.SerializeFilter","muscle.core.conduit.filter.CompressFilter"],["muscle.core.conduit.filter.DecompressFilter","muscle.core.conduit.filter.DeserializeFilter"])
}
