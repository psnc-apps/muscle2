
muscleInit()

fprintf('Kernel Name = %s\n', muscleKernelName())

fprintf('has property "script"? = %d \n', muscleHasProperty('script'))
fprintf('Property[script] = %s \n', muscleGetProperty('script'))
fprintf('has property "not_existing"? = %d \n', muscleHasProperty('not_existing'))

dataA = [ 0.0, 1.0, 2.0, 3.0, 4.0 ]

muscleWillStop()

while not( muscleWillStop() )
	muscleSend('data', dataA)
end

muscleFinalize()
