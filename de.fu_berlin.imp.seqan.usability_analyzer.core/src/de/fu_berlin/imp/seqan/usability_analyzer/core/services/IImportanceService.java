package de.fu_berlin.imp.seqan.usability_analyzer.core.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.IParameterValues;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;

public interface IImportanceService {

	public static enum Importance implements IParameterValues {
		LOW, DEFAULT, HIGH;

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Map getParameterValues() {
			Map params = new HashMap();
			for (Importance importance : values()) {
				params.put(importance.toString(), importance);
			}
			return params;
		}
	}

	public void setImportance(Collection<URI> uris, Importance importance);

	public void setImportance(URI uri, Importance importance);

	public Importance getImportance(URI uri);

	/**
	 * Registers a {@link IUriServiceListener}
	 * 
	 * @param importanceServiceListener
	 */
	public void addImportanceServiceListener(
			IImportanceServiceListener importanceServiceListener);

	/**
	 * Unregisters a {@link IUriServiceListener}
	 * 
	 * @param importanceServiceListener
	 */
	public void removeImportanceServiceListener(
			IImportanceServiceListener importanceServiceListener);

}
