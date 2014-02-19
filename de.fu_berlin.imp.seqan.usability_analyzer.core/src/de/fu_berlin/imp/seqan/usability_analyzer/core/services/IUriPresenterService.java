package de.fu_berlin.imp.seqan.usability_analyzer.core.services;

import java.net.URI;

import org.eclipse.swt.widgets.Control;

import com.bkahlert.nebula.information.ISubjectInformationProvider;
import com.bkahlert.nebula.information.InformationControlManager;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService.StyledLabelProvider;

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
