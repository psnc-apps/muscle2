abort "Run 'source [MUSCLE_HOME]/etc/muscle.profile' before this script" if not ENV.has_key?('MUSCLE_HOME')
dir = ENV['MUSCLE_HOME'] + '/share/muscle/examples/mpiring'

# declare kernels
lhc = MPIInstance.new('LHC', "#{dir}/LHC", {:mpiexec_args => '-np 2', :java_class => 'examples.mpiring.LHC2'})
psb = MPIInstance.new('PSB', "#{dir}/PSB", {:mpiexec_args => '-np 2', :java_class => 'examples.mpiring.PSB2'})

print lhc

# configure connection scheme
psb.couple(lhc, 'pipe-out' => 'pipe-in')

# configure cxa properties
$env['max_timesteps'] = 1
$env['default_dt'] = 1

psb['InitialEnergy'] = 1.2
psb['DeltaEnergy'] = 0.1
psb['MaxEnergy'] = 4.0

lhc['DeltaEnergy'] = 0.2
lhc['MaxEnergy'] = 12.0
