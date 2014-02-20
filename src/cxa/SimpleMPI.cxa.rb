abort "Run 'source [MUSCLE_HOME]/etc/muscle.profile' before this script" if not ENV.has_key?('MUSCLE_HOME')
dir = ENV['MUSCLE_HOME'] + '/share/muscle/examples/simplempi'

# Declare kernels
w = MPIInstance.new('w', "#{dir}/mpisender", mpi_args: '-np 4')
r = Instance.new('r', 'examples.simplejava.ConsoleWriter')

# Couple w to r
w.couple(r, 'data', ['multiply_0.5'])

# Set variables
$env['max_timesteps'] = 4
r['dt'] = 1;
w['dt'] = 1;
