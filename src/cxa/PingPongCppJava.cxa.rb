$env['same_size_runs'] = 30;
$env['tests_count'] = 5
$env['start_kiB_per_message'] = 0;
$env['steps'] = 10

abort "Run 'source [MUSCLE_HOME]/etc/muscle.profile' before this script" if not ENV.has_key?('MUSCLE_HOME')
dir = ENV['MUSCLE_HOME'] + '/share/muscle/examples/pingpongcpp'

# declare kernels
ping = NativeInstance.new('Ping', "#{dir}/ping", {:java_class => 'examples.pingpongcpp.Ping'})
pong = Instance.new('Pong', 'examples.pingpong.Pong')

# configure connection scheme
ping.couple(pong, 'out' => 'in')
pong.couple(ping, 'out' => 'in')

# configure cxa properties
$env['preparation_steps'] = $env['steps'] * $env['same_size_runs']
$env['max_timesteps'] = $env['tests_count'] * $env['steps'] * $env['same_size_runs'] + $env['preparation_steps'];
$env['default_dt'] = 1
