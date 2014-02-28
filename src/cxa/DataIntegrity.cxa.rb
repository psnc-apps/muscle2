abort "Run 'source [MUSCLE_HOME]/etc/muscle.profile' before this script" unless ENV.has_key?('MUSCLE_HOME')
dir = ENV['MUSCLE_HOME'] + '/share/muscle/examples/dataintegrity'

# declare kernels
bounce = NativeInstance.new('Bounce', "#{dir}/bounce")
check = NativeInstance.new('Check', "#{dir}/check", {:java_class => 'examples.dataintegrity.Check'})

# configure connection scheme
check.couple(bounce, {'datatype' => 'datatype', 'out' => 'in'})
bounce.couple(check, 'out' => 'in')

# configure cxa properties
$env['max_timesteps'] = 1
$env['default_dt'] = 1

check['num_seeds'] = 5
