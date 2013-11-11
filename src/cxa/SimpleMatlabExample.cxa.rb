abort "Run 'source [MUSCLE_HOME]/etc/muscle.profile' before this script" if not ENV.has_key?('MUSCLE_HOME')
dir = ENV['MUSCLE_HOME'] + '/share/muscle/examples/simplematlab'

# declare kernels
w = Instance.new('w', 'muscle.core.standalone.MatlabKernel')
r = Instance.new('r', 'examples.simplejava.ConsoleWriter')

# configure connection scheme
w.couple(r, 'data', ['multiply_0.5'])

# configure cxa properties
$env['max_timesteps'] = 4
$env['default_dt'] = 1;

w['script'] = "#{dir}/sender.m"

