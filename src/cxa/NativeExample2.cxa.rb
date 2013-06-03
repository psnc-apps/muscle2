# declare kernels
w = Instance.new('w', 'muscle.core.standalone.NativeKernel')
r = Instance.new('r', 'examples.simplejava.ConsoleWriter')

# configure connection scheme
w.couple(r, 'data')

# configure cxa properties
$env["max_timesteps"] = 2
$env["default_dt"] = 1

w["command"] = ENV['MUSCLE_HOME'] + "/share/muscle/examples/simplecpp2/simplecpp2"

