package de.fu_berlin.imp.seqan.usability_analyzer.core.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.IParameterValues;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;

/**
 * The {@link IImportanceService} can be used to find out the {@link Importance}
 * of a specific piece of information located by an {@link URI}.
 * <ul>
 * <li>
 * Registered {@link IImportanceService} are notified if the {@link Importance}
 * of an information changes.</li>
 * <li>Registered {@link IImportanceInterceptor}s can manipulate the calculated
 * {@link Importance}. This is especially useful to implement inheriting
 * {@link Importance}s.</li>
 * </ul>
 * 
 * @author bkahlert
 * 
 */
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

	/**
	 * Instances of this class can be used to manipulate the results returns by
	 * {@link IImportanceService#getImportance(Collection)} and
	 * {@link IImportanceService#getImportance(URI)}.
	 * <p>
	 * All requests and are passed to {@link #gettingImportance(Map)} which may
	 * overwrite the results. There is no deterministic order of calls if more
	 * than one {@link IImportanceInterceptor} is registered.
	 * 
	 * @author bkahlert
	 * 
	 */
	public static interface IImportanceInterceptor {

		/**
		 * This method is called whenever the importance of data is requested.
		 * 
		 * @param uris
		 *            contains the determined {@link Importance}s. May be
		 *            altered if wanted.
		 */
		public void gettingImportance(Map<URI, Importance> uris);

	}

	public void setImportance(Collection<URI> uris, Importance importance);

	public void setImportance(URI uri, Importance importance);

	public Map<URI, Importance> getImportance(Collection<URI> uris);

	public Importance getImportance(URI uri);

	/**
	 * Registers a {@link IImportanceServiceListener}
	 * 
	 * @param importanceServiceListener
	 */
	public void addImportanceServiceListener(
			IImportanceServiceListener importanceServiceListener);

	/**
	 * Unregisters a {@link IImportanceServiceListener}
	 * 
	 * @param importanceServiceListener
	 */
	public void removeImportanceServiceListener(
			IImportanceServiceListener importanceServiceListener);

	public void addImportanceInterceptor(
			IImportanceInterceptor importanceInterceptor);

	public void removeImportanceInterceptor(
			IImportanceInterceptor importanceInterceptor);

}
