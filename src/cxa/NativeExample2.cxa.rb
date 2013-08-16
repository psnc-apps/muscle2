# declare kernels
w = Instance.new('w', 'muscle.core.standalone.NativeKernel')
r = Instance.new('r', 'examples.simplejava.ConsoleWriter')

# configure connection scheme
w.couple(r, 'data')

# configure cxa properties
$env['max_timesteps'] = 2
$env['default_dt'] = 1

abort "Run 'source [MUSCLE_HOME]/etc/muscle.profile' before this script" if not ENV.has_key?('MUSCLE_HOME')
dir = ENV['MUSCLE_HOME'] + '/share/muscle/examples/simplecpp2'

w['command'] = "#{dir}/simplecpp2"

