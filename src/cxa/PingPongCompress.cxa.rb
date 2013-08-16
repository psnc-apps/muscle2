$env['same_size_runs'] = 30;
$env['tests_count'] = 5
$env['start_kiB_per_message'] = 0;
$env['steps'] = 10

# declare kernels
pong = Instance.new('Pong', 'examples.pingpong.Pong')
ping = Instance.new('Ping', 'examples.pingpong.Ping')

ping.couple(pong, {'out' => 'in'}, ['compress'], ['decompress'])
pong.couple(ping, {'out' => 'in'}, ['compress'], ['decompress'])

# configure cxa properties
$env['preparation_steps'] = $env['steps'] * $env['same_size_runs']
$env['max_timesteps'] = $env['tests_count'] * $env['steps'] * $env['same_size_runs'] + $env['preparation_steps'];
$env['default_dt'] = 1
