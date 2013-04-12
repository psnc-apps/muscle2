package examples.simplescala

import muscle.core.kernel.Submodel
import muscle.core.ConduitEntrance

class Sender extends Submodel {
	private[this] var data: Array[Array[Double]] = Array.ofDim[Double](2,2)
	private[this] lazy val entrance: ConduitEntrance[Array[Array[Double]]] = { out("data") }

	override def intermediateObservation() {
		entrance.send(data)
	}

    override def solvingStep() {
        data(0)(1) += 1
    }
}

