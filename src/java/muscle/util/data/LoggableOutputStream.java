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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.util.data;

import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joris
 */
public class LoggableOutputStream extends FilterOutputStream {
    protected String id;
    protected long pos = 0;
    protected FileOutputStream traceFile;
    @SuppressWarnings("NonConstantLogger")
    private final Logger logger;

    public LoggableOutputStream(Logger logger, String id, OutputStream out) throws IOException {
        super(out);
        this.logger = logger;
        logger.log(Level.FINEST, "id = {0}", id);
        this.id = id;
        this.traceFile = new FileOutputStream(System.getProperty("java.io.tmpdir") + "/" + id);
    }

    @Override
    public void write(int b) throws IOException {
        logger.log(Level.FINEST, "id = {0}, b = {1}, pos = {2}", new Object[]{id, b, pos});
        out.write(b);
        pos++;
        traceFile.write(b);
    }

    @Override
    public void close() throws IOException {
        logger.log(Level.FINEST, "id = {0}", id);
        out.close();
        traceFile.close();
    }

    @Override
    public void flush() throws IOException {
        logger.log(Level.FINEST, "id = {0}", id);
        out.flush();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        logger.log(Level.FINEST, "id = {0}, off = {1}, len = {2}, b = {3}, pos = {4}", new Object[]{id, off, len, SerializableData.bytesToHex(b, off, len), pos});
        out.write(b, off, len);
        pos += len;
        traceFile.write(b, off, len);
    }
    
}
