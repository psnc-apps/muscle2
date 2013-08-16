
# declare kernels
w = Instance.new('w', 'examples.simplejava.Sender')
r = Instance.new('r', 'examples.simplejava.ConsoleWriter')

# configure cxa properties
$env['max_timesteps'] = 4
r['dt'] = 1;
w['dt'] = 1;

# configure connection scheme
w.couple(r, 'data', ['multiply_0.5'])
