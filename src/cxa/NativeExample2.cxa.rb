abort "Run 'source [MUSCLE_HOME]/etc/muscle.profile' before this script" if not ENV.has_key?('MUSCLE_HOME')
dir = ENV['MUSCLE_HOME'] + '/share/muscle/examples/simplecpp2'

# declare kernels
w = NativeInstance.new('w', "#{dir}/simplecpp2")
r = NativeInstance.new('r', "#{dir}/simplecpp2r")

# configure connection scheme
w.couple(r, 'data')

# configure cxa properties
$env['max_timesteps'] = 2
$env['default_dt'] = 1

