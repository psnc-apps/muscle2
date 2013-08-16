abort "Run 'source [MUSCLE_HOME]/etc/muscle.profile' before this script" if not ENV.has_key?('MUSCLE_HOME')
dir = ENV['MUSCLE_HOME'] + '/share/muscle/examples/simplepython'

# Use the native kernel from MUSCLE
py_kern = Instance.new('py_kern', 'muscle.core.standalone.NativeKernel')

# define the coupling
py_kern.couple(py_kern, 'out' => 'in')

# Set the properties
$env['max_timesteps'] = 5

# Use your favorite python interpreter
py_kern['command'] = '/usr/bin/python'
# With your script as the argument
py_kern['args'] = "#{dir}/simplepython.py"

