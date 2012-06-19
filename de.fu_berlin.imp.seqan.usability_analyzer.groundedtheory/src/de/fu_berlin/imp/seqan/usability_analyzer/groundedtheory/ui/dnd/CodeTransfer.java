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

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;

public class CodeTransfer extends ByteArrayTransfer {

	private static final Logger LOGGER = Logger.getLogger(CodeTransfer.class);

	private static final String CODE_TYPE = "gtm/code";
	private static final int CODE_ID = registerType(CODE_TYPE);
	private static CodeTransfer _instance = new CodeTransfer();

	private CodeTransfer() {
	}

	public static CodeTransfer getInstance() {
		return _instance;
	}

	public void javaToNative(Object object, TransferData transferData) {
		if (object == null || !(object instanceof List))
			return;

		if (isSupportedType(transferData)) {
			List<ICode> codes = ArrayUtils.getAdaptableObjects(
					((List<?>) object).toArray(), ICode.class);
			List<ICodeInstance> instances = ArrayUtils.getAdaptableObjects(
					((List<?>) object).toArray(), ICodeInstance.class);
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ObjectOutputStream writeOut = new ObjectOutputStream(out);
				for (ICode code : codes)
					writeOut.writeObject(new CodeIdentifier(code));
				for (ICodeInstance instance : instances)
					writeOut.writeObject(new CodeInstanceIdentifier(instance));
				byte[] buffer = out.toByteArray();
				writeOut.close();

				super.javaToNative(buffer, transferData);
			} catch (IOException e) {
				LOGGER.error("Error converting " + ICode.class.getSimpleName()
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
					if (object instanceof CodeIdentifier) {
						long id = ((CodeIdentifier) object).getId();
						ICode code = codeService.getCode(id);
						if (code != null) {
							objects.add(code);
						} else {
							LOGGER.error("Error finding deserialized "
									+ ICode.class.getSimpleName());
							continue;
						}
					} else if (object instanceof CodeInstanceIdentifier) {
						long codeId = ((CodeInstanceIdentifier) object)
								.getCodeId();
						ICode code = codeService.getCode(codeId);
						if (code == null) {
							LOGGER.error("Error finding deserialized "
									+ ICode.class.getSimpleName());
							continue;
						}

						URI codeableId = ((CodeInstanceIdentifier) object)
								.getCodeableId();
						ICodeInstance instance = null;
						for (ICodeInstance codeInstance : codeService
								.getInstances(code)) {
							if (codeInstance.getId().equals(codeableId)) {
								instance = codeInstance;
								break;
							}
						}
						if (instance != null) {
							objects.add(instance);
						} else {
							LOGGER.error("Error finding deserialized "
									+ ICodeInstance.class.getSimpleName());
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
		return new String[] { CODE_TYPE };
	}

	protected int[] getTypeIds() {
		return new int[] { CODE_ID };
	}
}