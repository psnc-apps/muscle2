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
package muscle.util.data;

import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joris
 */
public class LoggableInputStream extends FilterInputStream {
    protected String id;
    protected long pos = 0;
    protected FileOutputStream traceFile;
    @SuppressWarnings("NonConstantLogger")
    private final Logger logger;

    public LoggableInputStream(Logger logger, String id, InputStream in) throws IOException {
        super(in);
        this.logger = logger;
        logger.log(Level.FINEST, "id = {0}", id);
        this.id = id;
        this.traceFile = new FileOutputStream(System.getProperty("java.io.tmpdir") + "/" + id);
    }

    @Override
    public int read() throws IOException {
        int b = in.read();
        logger.log(Level.FINEST, "id = {0}, b = {1}, pos = {2}", new Object[]{id, b, pos});
        if (b != -1) {
            pos++;
        }
        traceFile.write(b);
        return b;
    }

    @Override
    public void close() throws IOException {
        logger.log(Level.FINEST, "id = {0}", id);
        in.close();
        traceFile.close();
    }

    @Override
    public int available() throws IOException {
        int av = in.available();
        logger.log(Level.FINEST, "id = {0}, available bytes = {1}", new Object[]{id, av});
        return av;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        logger.log(Level.FINEST, "trying to read: id = {0}, off = {1}, len = {2} ", new Object[]{id, off, len});
        try {
            int bread = in.read(b, off, len);
            if (bread >= 0) {
                logger.log(Level.FINEST, "id = {0},  bread = {1}, off = {2}, b = {3}, pos = {4}", new Object[]{id, bread, off, SerializableData.bytesToHex(b, off, bread), pos});
                traceFile.write(b, off, bread);
                pos += bread;
            } else {
                logger.log(Level.FINEST, "id = {0},  bread = {1}, pos = {2}", new Object[]{id, bread, pos});
            }
            return bread;
        } catch (IOException ex) {
            logger.log(Level.WARNING, "read failed", ex);
            throw ex;
        }
        /* not reached */
    }
    
}
