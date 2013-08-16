# declare kernels
bounce = Instance.new('Bounce', 'muscle.core.standalone.NativeKernel')
check = Instance.new('Check', 'examples.dataintegrity.Check')

# configure connection scheme
check.couple(bounce, {'datatype' => 'datatype', 'out' => 'in'})
bounce.couple(check, 'out' => 'in')

# configure cxa properties
$env['max_timesteps'] = 1
$env['default_dt'] = 1

abort "Run 'source [MUSCLE_HOME]/etc/muscle.profile' before this script" if not ENV.has_key?('MUSCLE_HOME')
dir = ENV['MUSCLE_HOME'] + '/share/muscle/examples/dataintegrity'

check['num_seeds'] = 5
check['command'] = dir + '/check'
bounce['command'] = dir + '/bounce'

