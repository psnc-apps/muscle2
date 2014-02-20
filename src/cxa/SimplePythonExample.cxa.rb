abort "Run 'source [MUSCLE_HOME]/etc/muscle.profile' before this script" if not ENV.has_key?('MUSCLE_HOME')
dir = ENV['MUSCLE_HOME'] + '/share/muscle/examples/simplepython'

# Use the native kernel from MUSCLE
py_kern = PythonInstance.new('py_kern', "#{dir}/simplepython.py")

# define the coupling
py_kern.couple(py_kern, 'out' => 'in')

# Set the properties
$env['max_timesteps'] = 5
