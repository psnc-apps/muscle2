abort "Run 'source [MUSCLE_HOME]/etc/muscle.profile' before this script" if not ENV.has_key?('MUSCLE_HOME')
dir = ENV['MUSCLE_HOME'] + '/share/muscle/examples/simplematlab'

# declare kernels
w = MatlabInstance.new('w', "#{dir}/sender.m")
r = Instance.new('r', 'examples.simplejava.ConsoleWriter')
#r = MatlabInstance.new('r', "#{dir}/receiver.m")

# configure connection scheme
w.couple(r, 'data', ['multiply_0.5'])

# configure cxa properties
$env['max_timesteps'] = 4
$env['default_dt'] = 1;
