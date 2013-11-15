
muscleInit()

fprintf('Kernel Name = %s\n', muscleKernelName())

fprintf('has property "script"? = %d \n', muscleHasProperty('script'))
fprintf('Property[script] = %s \n', muscleGetProperty('script'))

dataA = [ 0.0, 1.0, 2.0, 3.0, 4.0 ]

while not( muscleWillStop() )
	muscleSend('data', dataA)
end

muscleFinalize()
