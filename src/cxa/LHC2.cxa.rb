# declare kernels
lhc = Instance.new('LHC', 'examples.mpiring.LHC2')
psb = Instance.new('PSB', 'examples.mpiring.PSB2')

# configure connection scheme
psb.couple(lhc, 'pipe-out' => 'pipe-in')

# configure cxa properties
abort "Run 'source [MUSCLE_HOME]/etc/muscle.profile' before this script" if not ENV.has_key?('MUSCLE_HOME')
dir = ENV['MUSCLE_HOME'] + '/share/muscle/examples/mpiring'

$env['max_timesteps'] = 1
$env['default_dt'] = 1

psb['InitialEnergy'] = 1.2
psb['DeltaEnergy'] = 0.1
psb['MaxEnergy'] = 4.0
psb['command'] = "#{dir}/PSB"
psb['mpiexec_args'] = '-np 2'

lhc['DeltaEnergy'] = 0.2
lhc['MaxEnergy'] = 12.0
lhc['command'] = "#{dir}/LHC"
lhc['mpiexec_args'] = '-np 2'

