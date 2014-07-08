package de.fu_berlin.imp.apiua.core.services;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService.StyledLabelProvider;

import org.eclipse.swt.widgets.Control;

import com.bkahlert.nebula.information.ISubjectInformationProvider;
import com.bkahlert.nebula.information.InformationControlManager;

/**
 * {@link IInformationPresenterService} that displays information based on the
 * given {@link URI}.
 * 
 * @author bkahlert
 * 
 */
public interface IUriPresenterService extends IInformationPresenterService<URI> {

	public static interface IUriLabelProvider extends
			IInformationLabelProvider<URI> {
	}

	public static abstract class StyledUriInformationLabelProvider extends
			StyledLabelProvider implements IUriLabelProvider {
	}

	public <CONTROL extends Control> InformationControlManager<CONTROL, URI> enable(
			CONTROL control,
			ISubjectInformationProvider<CONTROL, URI> subjectInformationProvider);
}
