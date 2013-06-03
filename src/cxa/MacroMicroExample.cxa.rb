# declare kernels
macro = Instance.new('macro', 'examples.macromicrosubmodel.Macro')
micro = Instance.new('micro', 'examples.macromicrosubmodel.Micro')

# configure connection scheme
macro.couple(micro, 'macroObs')
micro.couple(macro, 'microObs')

# configure cxa properties
$env["max_timesteps"] = 4

macro["dt"] = 1

micro["dt"] = "1 ms"
micro["T"] = "1 ms"

