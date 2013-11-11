
muscleInit()

dataA = [ 0.0, 1.0, 2.0, 3.0, 4.0 ]

muscleWillStop()

while not( muscleWillStop() )
	muscleSend('data', dataA)
end

muscleFinalize()
