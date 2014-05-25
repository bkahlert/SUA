package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdapterFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;

public class StringAdapterFactory implements IAdapterFactory {

	private static final Logger LOGGER = Logger
			.getLogger(StringAdapterFactory.class);

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { ICode.class };
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof String) {
			if (adapterType == URI.class) {
				String[] uris = StringUtils
						.split((String) adaptableObject, "|");
				if (uris.length == 0) {
					return null;
				}
				try {
					URI uri = new URI(uris[0]);
					return uri;
				} catch (Exception e) {
					LOGGER.error(e);
				}
			}
			if (adapterType == ICode.class) {
				String[] uris = StringUtils
						.split((String) adaptableObject, "|");
				if (uris.length == 0) {
					return null;
				}
				try {
					URI uri = new URI(uris[0]);
					ICode code = LocatorService.INSTANCE.resolve(uri,
							ICode.class, null).get();
					return code;
				} catch (Exception e) {
					LOGGER.error(e);
				}
			}
		}

		return null;
	}

}
