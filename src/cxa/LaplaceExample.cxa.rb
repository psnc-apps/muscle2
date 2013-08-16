# Set to true to get periodic boundary conditions
wrapAround = false

# declare kernels
east = Instance.new('east', 'examples.laplace.KernelEast')
west = Instance.new('west', 'examples.laplace.KernelWest')

# configure coupling
east.couple(west, 'westBoundary' => 'remoteEast')
west.couple(east, 'eastBoundary' => 'remoteWest')

if wrapAround
  east.couple(west, 'eastBoundary' => 'remoteWest')
  west.couple(east, 'westBoundary' => 'remoteEast')
end

# set other variables
$env['max_timesteps'] = 10000
$env['default_dt'] = 1
$env['nx'] = 100
$env['ny'] = 50
$env['dx'] = 4
$env['wrapAround'] = wrapAround