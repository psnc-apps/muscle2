/*
Copyright 2008,2009 Complex Automata Simulation Technique (COAST) consortium

GNU Lesser General Public License

This file is part of MUSCLE (Multiscale Coupling Library and Environment).

    MUSCLE is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MUSCLE is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/

package utilities;

import java.util.AbstractList;


/**
read only array list backed by a C-style array of references<br>
a list view of an array can also be obtained via<br>
public static <T> List<T> java.util.Arrays.asList(T... a)<br>
e.g. List<String> moo = Arrays.asList("Foo", "Bar", "Baz");
@author Jan Hegewald
*/
public class CArrayList<E> extends AbstractList<E> {

	private E[] data;

	
	//
	public CArrayList(E[] newData) {

		data = newData;
		assert data != null;
	}


	//
	public E get(int index) {

		assert data != null;
		return data[index];
	}

	
	//
	public int size() {

		assert data != null;
		return data.length;
	}

}
