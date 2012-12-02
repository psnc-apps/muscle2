/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.util.serialization;

import java.io.Serializable;

/**
 *
 * @author Joris Borgdorff
 */
public class JavaObjectMessageConverter<T extends Serializable> extends BasicMessageConverter<T,byte[]> {
	public JavaObjectMessageConverter() {
		super(new ByteJavaObjectConverter<T>());
	}
}
