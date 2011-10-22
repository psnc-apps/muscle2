/*
 * 
 */
package muscle.core.ident;

<<<<<<< HEAD
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.util.Iterator;

/**
 * @author Joris Borgdorff
 */
public class JadeLocation implements jade.core.Location, Location, Iterable<Property> {
	private final String id, name, protocol, address;
	private final static String
			ID = "LOC_ID",
			NAME = "LOC_name",
			PROTOCOL = "LOC_protocol",
			ADDRESS = "LOC_address";
	
	public JadeLocation(jade.core.Location loc) {
		this.id = loc.getID();
		this.name = loc.getName();
		this.protocol = loc.getProtocol();
		this.address = loc.getAddress();
	}
	
	public JadeLocation(ServiceDescription sd) {
		Iterator i = sd.getAllProperties();
		String tmpId = null, tmpNm = null, tmpPr = null, tmpAddr = null;
		while (i.hasNext()) {
			Property p = (Property)i.next();
			if (p.getName().equals(ID)) tmpId = (String)p.getValue();
			if (p.getName().equals(NAME)) tmpNm = (String)p.getValue();
			if (p.getName().equals(PROTOCOL)) tmpPr = (String)p.getValue();
			if (p.getName().equals(ADDRESS)) tmpAddr = (String)p.getValue();
		}
		this.id = tmpId;
		this.name = tmpNm;
		this.protocol = tmpPr;
		this.address = tmpAddr;
	}
	
	public String getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getAddress() {
		return address;
	}

	public Iterator<Property> iterator() {
		return new PropertyIterator();
	}
	
	public boolean equals(Object o) {
		if (o == null || !o.getClass().equals(this.getClass())) return false;
		return getID().equals(((JadeLocation)o).getID());
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 47 * hash + this.getID().hashCode();
		return hash;
	}

	private class PropertyIterator implements Iterator<Property> {
		private int i;

		public PropertyIterator() {
			this.i = 0;
		}
		
		public boolean hasNext() {
			return i < 3;
		}

		public Property next() {
			Property p = null;
			switch (i) {
				case 0: p = new Property(ID, id); break;
				case 1: p = new Property(NAME, id); break;
				case 2: p = new Property(PROTOCOL, id); break;
				case 3: p = new Property(ADDRESS, id); break;
			}
			i++;
			return p;
		}

		public void remove() {
			throw new UnsupportedOperationException("Not supported yet.");
		}
=======
/**
 *
 * @author Joris Borgdorff
 */
public class JadeLocation implements jade.core.Location, Location {
	private final jade.core.Location jadeLocation;

	public JadeLocation(jade.core.Location loc) {
		this.jadeLocation = loc;
	}
	
	public String getID() {
		return jadeLocation.getID();
	}

	public String getName() {
		return jadeLocation.getName();
	}

	public String getProtocol() {
		return jadeLocation.getProtocol();
	}

	public String getAddress() {
		return jadeLocation.getAddress();
>>>>>>> a8c652eb292cdbcf135af6155a23c69c08aef9c0
	}
}
