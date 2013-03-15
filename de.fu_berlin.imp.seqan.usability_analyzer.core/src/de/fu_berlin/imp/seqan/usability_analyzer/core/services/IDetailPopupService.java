package de.fu_berlin.imp.seqan.usability_analyzer.core.services;

import org.eclipse.jface.dialogs.PopupDialog;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;

/**
 * This service can display {@link PopupDialog}s that show detailed information
 * about the requested object.
 * 
 * @author bkahlert
 */
public interface IDetailPopupService {

	/**
	 * Shows a {@link PopupDialog} with detailed information about the given
	 * {@link ILocatable}.
	 * 
	 * @param locatable
	 * @return true if {@link PopupDialog} could be opened; false otherwise. The
	 *         reason in this case is that no detailed information could be
	 *         retrieved.
	 * 
	 * @ArbitraryThread
	 */
	public boolean showDetailPopup(ILocatable locatable);

	/**
	 * Closes an eventually opened {@link PopupDialog}.
	 * <p>
	 * This method may be called multiple times and also if no
	 * {@link PopupDialog} is open.
	 * 
	 * @ArbitraryThread
	 */
	public void hideDetailPopup();

}
