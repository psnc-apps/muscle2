# declare kernels
proc = Instance.new('proc', 'examples.terminals.Processor')
src = Terminal.new('src', 'muscle.core.conduit.terminal.DoubleFileSource')
sink = Terminal.new('sink', 'examples.terminals.BooleanFileSink')

# configure connection scheme
src.couple(proc, 'initialData')
proc.couple(sink, 'largeMask')

# Set variables
$env["max_timesteps"] = 4
$env["default_dt"] = 1

src['file'] = File.dirname(__FILE__) + "/../resources/terminal_in.dat"

sink['file'] = 'boolMask'
sink['relative'] = true
sink['suffix'] = 'dat'
