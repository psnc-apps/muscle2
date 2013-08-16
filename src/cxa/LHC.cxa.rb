abort "Run 'source [MUSCLE_HOME]/etc/muscle.profile' before this script" if not ENV.has_key?('MUSCLE_HOME')
java_libdir = ENV['MUSCLE_HOME'] + '/share/muscle/examples/lib'

add_classpath "#{java_libdir}/jna.jar"
add_classpath "#{java_libdir}/platform.jar"

# declare kernels
lhc = Instance.new('LHC', 'examples.mpiring.LHC')
psb = Instance.new('PSB', 'examples.mpiring.PSB')

# configure connection scheme
psb.couple(lhc, 'pipe')

# Variables
$env['max_timesteps'] = 5
$env['default_dt'] = 1

psb['InitialEnergy'] = 1.2
psb['DeltaEnergy'] = 0.1
psb['MaxEnergy'] = 4.0

lhc['DeltaEnergy'] = 0.2
lhc['MaxEnergy'] = 12.0
