package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.rcp.selectionUtils.ArrayUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

public class CodeableTransfer extends ByteArrayTransfer {

	private static final Logger LOGGER = Logger
			.getLogger(CodeableTransfer.class);

	private static final String CODEABLE_TYPE = "gtm/codeable";
	private static final int CODEABLE_ID = registerType(CODEABLE_TYPE);
	private static CodeableTransfer _instance = new CodeableTransfer();

	private CodeableTransfer() {
	}

	public static CodeableTransfer getInstance() {
		return _instance;
	}

	public void javaToNative(Object object, TransferData transferData) {
		if (object == null || !(object instanceof List))
			return;

		if (isSupportedType(transferData)) {
			List<ICodeable> codeables = ArrayUtils.getAdaptableObjects(
					((List<?>) object).toArray(), ICodeable.class);
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ObjectOutputStream writeOut = new ObjectOutputStream(out);
				for (ICodeable codeable : codeables)
					writeOut.writeObject(new CodeableIdentifier(codeable));
				byte[] buffer = out.toByteArray();
				writeOut.close();

				super.javaToNative(buffer, transferData);
			} catch (IOException e) {
				LOGGER.error(
						"Error converting " + ICodeable.class.getSimpleName()
								+ "s to native", e);
			}
		}
	}

	public Object nativeToJava(TransferData transferData) {
		if (isSupportedType(transferData)) {
			ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
					.getService(ICodeService.class);
			if (codeService == null) {
				LOGGER.error("No " + ICodeService.class.getSimpleName()
						+ " available");
				return null;
			}

			byte[] buffer = (byte[]) super.nativeToJava(transferData);
			if (buffer == null)
				return null;

			List<Object> objects = new LinkedList<Object>();
			ByteArrayInputStream in = null;
			ObjectInputStream readIn = null;
			try {
				in = new ByteArrayInputStream(buffer);
				readIn = new ObjectInputStream(in);
				Object object = null;
				while ((object = readIn.readObject()) != null) {
					if (object instanceof CodeableIdentifier) {
						URI id = ((CodeableIdentifier) object).getId();
						ICodeable codeable = codeService.getCodedObject(id);
						if (codeable != null) {
							objects.add(codeable);
						} else {
							LOGGER.error("Error finding deserialized "
									+ ICodeable.class.getSimpleName());
							continue;
						}
					}
				}
			} catch (EOFException e) {

			} catch (IOException e) {
				LOGGER.error("Error converting native back", e);
			} catch (ClassNotFoundException e) {
				LOGGER.error("Error converting native back", e);
			} finally {
				if (readIn != null)
					try {
						readIn.close();
					} catch (IOException e) {
					}
			}
			return objects;
		}

		return null;
	}

	protected String[] getTypeNames() {
		return new String[] { CODEABLE_TYPE };
	}

	protected int[] getTypeIds() {
		return new int[] { CODEABLE_ID };
	}
}