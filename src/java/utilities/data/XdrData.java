/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrDecodingStream;
import org.acplt.oncrpc.XdrEncodingStream;

/**
 *
 * @author Joris Borgdorff
 */
public class XdrData {
	private final XdrDatatype type;
	private final Object value;
	private final static XdrDatatype[] datatypes = XdrDatatype.values();
	
	public XdrData(Object value, XdrDatatype type) {
		this.type = type;
		this.value = value;
	}
	
	public Object getValue() {
		return value;
	}

	public XdrDatatype getType() {
		return type;
	}
	
	public static XdrData parseXdrData(XdrDecodingStream xdrIn) throws OncRpcException, IOException {
		int typeNum = xdrIn.xdrDecodeInt();
		
		if (typeNum < 0 || typeNum >= datatypes.length) {
			throw new OncRpcException("Datatype with number " + typeNum + " not recognized");
		}
		
		XdrDatatype type = datatypes[typeNum];
		Object value;
		int size;
		
		switch (type) {
			case STRING_MAP:
				size = xdrIn.xdrDecodeInt();
				Map<String,XdrData> xdrMap = new HashMap<String,XdrData>(size*3/2);
				
				for (int i = 0; i < size; i++) {
					xdrMap.put(xdrIn.xdrDecodeString(), XdrData.parseXdrData(xdrIn));
				}
				value = xdrMap;
				break;
			case LIST:
				size = xdrIn.xdrDecodeInt();
				List<XdrData> xdrList = new ArrayList<XdrData>(size);
				
				for (int i = 0; i < size; i++) {
					xdrList.add(XdrData.parseXdrData(xdrIn));
				}
				value = xdrList;
				break;
			case STRING:
				value = xdrIn.xdrDecodeString();
				break;
			case STRING_ARR:
				value = xdrIn.xdrDecodeStringVector();
				break;
			case BOOLEAN:
				value = xdrIn.xdrDecodeBoolean();
				break;
			case BOOLEAN_ARR:
				value = xdrIn.xdrDecodeBooleanVector();
				break;
			case BYTE:
				value = xdrIn.xdrDecodeByte();
				break;
			case BYTE_ARR:
				value = xdrIn.xdrDecodeByteVector();
				break;
			case SHORT:
				value = xdrIn.xdrDecodeShort();
				break;
			case SHORT_ARR:
				value = xdrIn.xdrDecodeShortVector();
				break;
			case INT:
				value = xdrIn.xdrDecodeInt();
				break;
			case INT_ARR:
				value = xdrIn.xdrDecodeIntVector();
				break;
			case LONG:
				value = xdrIn.xdrDecodeLong();
				break;
			case LONG_ARR:
				value = xdrIn.xdrDecodeLongVector();
				break;
			case FLOAT:
				value = xdrIn.xdrDecodeFloat();
				break;
			case FLOAT_ARR:
				value = xdrIn.xdrDecodeFloatVector();
				break;
			case DOUBLE:
				value = xdrIn.xdrDecodeDouble();
				break;
			case DOUBLE_ARR:
				value = xdrIn.xdrDecodeDoubleVector();
				break;
			default:
				throw new OncRpcException("Datatype " + type + " not recognized");
		}
		
		return new XdrData(value, type);
	}
	
	public void encodeXdrData(XdrEncodingStream xdrOut) throws OncRpcException, IOException {
		xdrOut.xdrEncodeInt(type.ordinal());
		
		switch (type) {
			case STRING_MAP:				
				Map<String,XdrData> xdrMap = (Map<String,XdrData>)value;
				xdrOut.xdrEncodeInt(xdrMap.size());
				
				for (Map.Entry<String,XdrData> entry : xdrMap.entrySet()) {
					xdrOut.xdrEncodeString(entry.getKey());
					entry.getValue().encodeXdrData(xdrOut);
				}
				break;
			case LIST:
				List<XdrData> xdrList = (List<XdrData>)value;
				xdrOut.xdrEncodeInt(xdrList.size());
				
				for (XdrData data : xdrList) {
					data.encodeXdrData(xdrOut);
				}
				break;
			case STRING:
				xdrOut.xdrEncodeString((String)value);
				break;
			case STRING_ARR:
				xdrOut.xdrEncodeStringVector((String[])value);
				break;
			case BOOLEAN:
				xdrOut.xdrEncodeBoolean((Boolean)value);
				break;
			case BOOLEAN_ARR:
				xdrOut.xdrEncodeBooleanVector((boolean[])value);
				break;
			case BYTE:
				xdrOut.xdrEncodeByte((Byte)value);
				break;
			case BYTE_ARR:
				xdrOut.xdrEncodeByteVector((byte[])value);
				break;
			case SHORT:
				xdrOut.xdrEncodeShort((Short)value);
				break;
			case SHORT_ARR:
				xdrOut.xdrEncodeShortVector((short[])value);
				break;
			case INT:
				xdrOut.xdrEncodeInt((Integer)value);
				break;
			case INT_ARR:
				xdrOut.xdrEncodeIntVector((int[])value);
				break;
			case LONG:
				xdrOut.xdrEncodeLong((Long)value);
				break;
			case LONG_ARR:
				xdrOut.xdrEncodeLongVector((long[])value);
				break;
			case FLOAT:
				xdrOut.xdrEncodeFloat((Float)value);
				break;
			case FLOAT_ARR:
				xdrOut.xdrEncodeFloatVector((float[])value);
				break;
			case DOUBLE:
				xdrOut.xdrEncodeDouble((Double)value);
				break;
			case DOUBLE_ARR:
				xdrOut.xdrEncodeDoubleVector((double[])value);
				break;
			default:
				throw new OncRpcException("Datatype " + type + " not recognized");
		}
	}
}
