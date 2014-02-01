package de.fu_berlin.imp.seqan.usability_analyzer.core.services;

import java.net.URI;

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

	public static class UriLabelProvider extends InformationLabelProvider<URI> {

	}

	public <CONTROL extends Control> InformationControlManager<CONTROL, URI> enable(
			CONTROL control,
			ISubjectInformationProvider<CONTROL, URI> subjectInformationProvider);
}
