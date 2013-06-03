# declare kernels
w = Instance.new('w', 'examples.simplesubmodel.Sender')
r = Instance.new('r', 'examples.simplesubmodel.ConsoleWriter')

# configure connection scheme
w.couple(r, 'data')

# configure cxa properties
$env["max_timesteps"] = 4
$env["default_dt"] = 1

