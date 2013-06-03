# declare kernels
lhc = Instance.new('LHC', 'examples.mpiring.LHC2')
psb = Instance.new('PSB', 'examples.mpiring.PSB2')

# configure connection scheme
psb.couple(lhc, 'pipe-out' => 'pipe-in')

# configure cxa properties
$env["max_timesteps"] = 1
$env["default_dt"] = 1

psb["InitialEnergy"] = 1.2
psb["DeltaEnergy"] = 0.1
psb["MaxEnergy"] = 4.0
psb["command"] = ENV['MUSCLE_HOME'] + "/share/muscle/examples/mpiring/PSB"
psb["mpiexec_args"] = "-np 2"

lhc["DeltaEnergy"] = 0.2
lhc["MaxEnergy"] = 12.0
lhc["command"] = ENV['MUSCLE_HOME'] + "/share/muscle/examples/mpiring/LHC"
lhc["mpiexec_args"] = "-np 2"

