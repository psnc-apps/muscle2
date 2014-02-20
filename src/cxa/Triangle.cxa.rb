# declare kernels
one = Instance.new('one', 'examples.triangle.One')
two = Instance.new('two', 'examples.triangle.Two')
three = Instance.new('three', 'examples.triangle.Three')

# configure connection scheme
one.couple(two, 'data')
two.couple(three, 'data')
three.couple(one, 'data')

# configure cxa properties
$env["max_timesteps"] = 2
$env["default_dt"] = 1
