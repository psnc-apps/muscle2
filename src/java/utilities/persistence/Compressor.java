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

package utilities.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;


/**
compress/decompress objects
<br>
compression level (-1--9):<br>
java.util.zip.Deflater.DEFAULT_COMPRESSION (-1)
java.util.zip.Deflater.NO_COMPRESSION (0)
java.util.zip.Deflater.BEST_SPEED (1)
java.util.zip.Deflater.BEST_COMPRESSION (9)
<br>
compression strategy:<br>
java.util.zip.Deflater.DEFAULT_STRATEGY (0)
java.util.zip.Deflater.FILTERED (1)
java.util.zip.Deflater.HUFFMAN_ONLY (2)
<br>note: java.util.zip.Deflater.DEFLATED (8) is not a strategy
@author Jan Hegewald
*/
public class Compressor<T extends java.io.Serializable> implements Serializer<T, byte[]> {


   private Deflater deflater;
   private Inflater inflater;

   //
   public Compressor() {

      deflater = new Deflater();
      inflater = new Inflater();
   }
   

   //
   public Compressor(int level, int strategy, boolean gzipCompatible) {

      deflater = new Deflater(level, gzipCompatible);
      deflater.setStrategy(strategy);
      
      inflater = new Inflater(gzipCompatible);
   }


   //
   @Override
	public byte[] dump(T object) {

		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			//GZIPOutputStream gzOut = new GZIPOutputStream(bOut); // this uses a DeflaterOutputStream with new Deflater(Deflater.DEFAULT_COMPRESSION, true)
			DeflaterOutputStream deflaterStream = new DeflaterOutputStream(byteStream, deflater);
			ObjectOutputStream objectOut = new ObjectOutputStream(deflaterStream);
			objectOut.writeObject(object);
			deflaterStream.finish();
			byteStream.flush();
			byteStream.close();

			return byteStream.toByteArray();
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
		}
	}


   //
   @Override
	public T load(byte[] bytes) {
		try {
			ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
			InflaterInputStream inflaterStream = new InflaterInputStream(byteStream, inflater);
			ObjectInputStream oIn = new ObjectInputStream(inflaterStream);
			return (T)oIn.readObject();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}