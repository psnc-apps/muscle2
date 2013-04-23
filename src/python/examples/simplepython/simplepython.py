import muscle, sys

muscle.init(sys.argv)

a = "the original data" 
muscle.send(a, "out", muscle.string)

muscle.log("received: " + muscle.receive("in", muscle.string));

muscle.finalize()

