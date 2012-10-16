/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.client.communication;

/**
 *
 * @author bobby
 */
public enum TcpDataProtocol {
	OBSERVATION(0), SIGNAL(1), KEEPALIVE(2), FINISHED(3), ERROR(-2), CLOSE(-1), MAGIC_NUMBER(134405);

	private final int num;
	private final static TcpDataProtocol[] values = TcpDataProtocol.values();
	private final static int[] nums = new int[values.length];
	static {
		for (int i = 0; i < nums.length; i++) {
			nums[i] = values[i].num;
		}
	}
	
	TcpDataProtocol(int n) {
		num = n;
	}
	
	public static TcpDataProtocol valueOf(int n) {
		for (int i = 0; i < nums.length; i++) {
			if (nums[i] == n) {
				return values[i];
			}
		}
		return ERROR;
	}
	
	public int intValue() {
		return num;
	}
}
