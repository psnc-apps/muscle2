$env['same_size_runs'] = 30;
$env['steps'] = 10
$env['tests_count'] = 15
$env['start_kiB_per_message'] = 0;

# declare kernels
pong1 = Instance.new('Pong1', 'examples.pingpong.Pong')
pong2 = Instance.new('Pong2', 'examples.pingpongsubmodel.Pong')
dup   = Instance.new('dup', 'muscle.core.kernel.DuplicationMapper')
combine = Instance.new('combine', 'examples.pingpongsubmodel.PongCombiner')
ping = Instance.new('Ping', 'examples.pingpong.Ping')

# set variables
$env['preparation_steps'] = $env['steps'] * $env['same_size_runs']
$env['max_timesteps'] = cxa.env['tests_count'] * cxa.env['steps'] * cxa.env['same_size_runs'] + cxa.env['preparation_steps'];
$env['default_dt'] = 1

pong1['T'] = 1
pong2['T'] = 1

# configure couplings
ping.couple(dup, 'out' => 'in')
dup.couple(pong1, 'out1' => 'in')
dup.couple(pong2, 'out2' => 'in')
ping1.couple(combine, 'out' => 'in1')
ping2.couple(combine, 'out' => 'in2')
combine.couple(ping, 'out' => 'in')

