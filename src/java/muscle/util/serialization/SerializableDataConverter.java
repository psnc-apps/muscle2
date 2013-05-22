/*
* Copyright 2008, 2009 Complex Automata Simulation Technique (COAST) consortium
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 * 
 */

package muscle.util.serialization;

import java.io.Serializable;
import muscle.util.data.SerializableData;
import muscle.util.data.SerializableDatatype;

/**
 *
 * @author Joris Borgdorff
 */
public class SerializableDataConverter<T extends Serializable> implements DataConverter<T,SerializableData> {
	private SerializableDatatype type = SerializableDatatype.NULL;
	
	@Override
	public SerializableData serialize(T data) {
		if (data == null) {
			type = SerializableDatatype.NULL;
		} else if (type.getDataClass() == null || !type.getDataClass().isInstance(data)) {
			type = SerializableData.inferDatatype(data);
		}
		return SerializableData.valueOf(data, type);
	}

	@Override
	@SuppressWarnings("unchecked")
	public T deserialize(SerializableData data) {
		return (T)data.getValue();
	}

	@Override
	public T copy(T data) {
		if (data == null) {
			type = SerializableDatatype.NULL;
		} else if (type.getDataClass() == null || !type.getDataClass().isInstance(data)) {
			type = SerializableData.inferDatatype(data);
		}
		return SerializableData.createIndependent(data, type);
	}
}
