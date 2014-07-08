package edu.uiuc.ncsa.cyberintegrator.ui.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

/**
 * @author 
 *         https://opensource.ncsa.illinois.edu/svn/cyberintegrator/trunk/edu.uiuc
 *         .ncsa.cyberintegrator.ui/src/edu/uiuc/ncsa/cyberintegrator/ui/dnd/
 *         UriTransfer.java *
 */
public class UriTransfer extends ByteArrayTransfer {

	private final Logger log = Logger.getLogger(UriTransfer.class);
	private static final String TYPE_NAME = "uri-transfer-format";
	public static final int TYPE_ID = registerType(TYPE_NAME);
	protected static UriTransfer instance;

	private UriTransfer() {

	}

	public static UriTransfer getInstance() {
		if (instance == null) {
			instance = new UriTransfer();
		}

		return instance;
	}

	protected String[] fromByteArray(byte[] bytes) {
		DataInputStream in = new DataInputStream(
				new ByteArrayInputStream(bytes));

		try {
			/* read number of gadgets */
			int n = in.readInt();

			/* read gadgets */
			String[] properties = new String[n];
			for (int i = 0; i < n; i++) {
				String r = readModel(in);
				if (r == null) {
					return null;
				}
				properties[i] = r;
			}
			return properties;
		} catch (Throwable e) {
			// XXX: This should NOT be shown to the user.
			log.error("Failed in fromByteArray", e);
			return null;
		}
	}

	@Override
	protected String[] getTypeNames() {
		return new String[] { TYPE_NAME };
	}

	@Override
	protected int[] getTypeIds() {
		return new int[] { TYPE_ID };
	}

	protected void javaToNative(Object object, TransferData transferData) {
		byte[] bytes = null;
		if (object instanceof String[]) {
			bytes = toByteArray((String[]) object);
		}
		if (object instanceof String) {
			bytes = toByteArray(new String[] { (String) object });
		}
		if (object instanceof URI[]) {
			LinkedList<String> strings = new LinkedList<String>();
			for (URI uri : (URI[]) object)
				strings.add(uri.toString());
			bytes = toByteArray(strings.toArray(new String[strings.size()]));
		}
		if (object instanceof URI) {
			bytes = toByteArray(new String[] { ((URI) object).toString() });
		}
		if (bytes != null) {
			super.javaToNative(bytes, transferData);
		}
	}

	protected Object nativeToJava(TransferData transferData) {
		byte[] bytes = (byte[]) super.nativeToJava(transferData);
		String[] fromByteArray = fromByteArray(bytes);

		return fromByteArray;
	}

	public byte[] toByteArray(String[] properties) {
		/**
		 * Transfer data is an array of URIs. Serialized version is: (int)
		 * number of URIs (URI) uri 1 (URI) uri 2 ... repeat for each subsequent
		 * URI see writeUri for the (URI) format.
		 */
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(byteOut);

		byte[] bytes = null;

		try {
			/* write number of markers */
			out.writeInt(properties.length);

			/* write markers */
			for (int i = 0; i < properties.length; i++) {
				writeModel(properties[i], out);
			}
			out.close();
			bytes = byteOut.toByteArray();
		} catch (IOException e) {
			// when in doubt send nothing
		}
		return bytes;
	}

	private String readModel(DataInputStream dataIn) throws Throwable {
		String subjectString = dataIn.readUTF();
		return subjectString;
	}

	private void writeModel(String m, DataOutputStream dataOut)
			throws IOException {
		dataOut.writeUTF(m);
	}

}