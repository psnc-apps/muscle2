package muscle.core.messaging;

import java.io.Serializable;

/**
 *
 * @author Joris Borgdorff
 */
public abstract class AbstractTime implements Comparable<AbstractTime>, Serializable {
	protected final double t;

	public AbstractTime(double t) {
		this.t = t;
	}
	
	public int compareTo(AbstractTime ts) {
		return Double.compare(t, ts.t);
	}
	
	public boolean equals(Object o) {
		if (o == null || !o.getClass().equals(this.getClass())) return false;
		return ((AbstractTime)o).t == this.t;
	}

	public int hashCode() {
		int hash = 5;
		hash = 67 * hash + (int) (Double.doubleToLongBits(this.t) ^ (Double.doubleToLongBits(this.t) >>> 32));
		return hash;
	}
}
