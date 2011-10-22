/*
 * 
 */
package muscle.core.ident;

<<<<<<< HEAD
import java.io.Serializable;

/**
 * @author jborgdo1
 */
public interface Identifier extends Comparable<Identifier>, Serializable {
	public String getName();
	public IDType getType();
	public boolean isResolved();
	public void unResolve();
	public Location getLocation();
	public boolean identifies(Identifiable ident);
=======
/**
 * @author jborgdo1
 */
public interface Identifier extends Comparable<Identifier> {
	public String getName();
	public IDType getType();
	public Location getLocation();
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
}
