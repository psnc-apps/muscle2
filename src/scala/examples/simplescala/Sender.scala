package examples.simplescala

import muscle.core.kernel.Submodel
import muscle.core.ConduitEntrance

class Sender extends Submodel {
	var data: Array[Array[Double]] = Array.ofDim[Double](2,2)
	var entrance: ConduitEntrance[Array[Array[Double]]] = _

	override def addPortals() {
		entrance = addEntrance("data", classOf[Array[Array[Double]]])	
	}

	override def intermediateObservation() {
		entrance.send(data)
	}
}

