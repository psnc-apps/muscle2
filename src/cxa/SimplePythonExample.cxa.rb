cxa = Cxa.LAST
cxa.add_instance('py_kern', 'muscle.core.standalone.NativeKernel')

cxa.env['py_kern:command'] = '/usr/bin/python'
cxa.env['py_kern:args'] = ENV['MUSCLE_HOME'] + "/share/muscle/examples/simplepython/simplepython.py"

cxa.cs.attach('py_kern' => 'py_kern') {
	tie('out', 'in')
}
