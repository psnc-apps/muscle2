abort "Run 'source [MUSCLE_HOME]/etc/muscle.profile' before this script" if not ENV.has_key?('MUSCLE_HOME')
dir = ENV['MUSCLE_HOME'] + '/share/muscle/examples/transmutable2'

# declare kernels
cpp = NativeInstance.new('cpp', "#{dir}/transmutable2")

# configure connection scheme
cpp.couple(cpp, 'writer' => 'reader')

# configure cxa properties
$env['max_timesteps'] = 3
cpp['dt'] = 1