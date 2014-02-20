package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import com.bkahlert.nebula.information.ISubjectInformationProvider;
import com.bkahlert.nebula.information.InformationControl;
import com.bkahlert.nebula.information.InformationControlCreator;
import com.bkahlert.nebula.information.InformationControlManager;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IUriPresenterService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl.UriInformationControl.IPostProcessor;

public class UriPresenterService implements IUriPresenterService {

	private final List<IInformationBackgroundProvider<URI>> informationBackgroundProviders = new ArrayList<IInformationBackgroundProvider<URI>>();

	private final Map<Control, InformationControlManager<?, ?>> informationControlManagers = new HashMap<Control, InformationControlManager<?, ?>>();

	@Override
	public void addInformationBackgroundProvider(
			IInformationBackgroundProvider<URI> informationBackgroundProvider) {
		this.informationBackgroundProviders.add(informationBackgroundProvider);
	};

	@Override
	public void removeInformationBackgroundProvider(
			IInformationBackgroundProvider<URI> informationBackgroundProvider) {
		this.informationBackgroundProviders
				.remove(informationBackgroundProvider);
	};

	@Override
	public <CONTROL extends Control> void disable(CONTROL control) {
		if (this.informationControlManagers.containsKey(control)) {
			this.informationControlManagers.get(control).dispose();
			this.informationControlManagers.remove(control);
		}
	}

	@Override
	public <CONTROL extends Control> InformationControlManager<CONTROL, URI> enable(
			CONTROL control,
			ISubjectInformationProvider<CONTROL, URI> subjectInformationProvider) {
		return this.enable(control, URI.class, subjectInformationProvider);
	}

	@Override
	public <CONTROL extends Control> InformationControlManager<CONTROL, URI> enable(
			CONTROL control, Class<URI> informationClass,
			ISubjectInformationProvider<CONTROL, URI> subjectInformationProvider) {
		InformationControlCreator<URI> creator = new InformationControlCreator<URI>() {
			@Override
			protected InformationControl<URI> doCreateInformationControl(
					Shell parent) {
				final UriInformationControl control = new UriInformationControl(
						parent);

				control.setPostProcessor(new IPostProcessor() {
					@Override
					public void postProcess(URI uri, Composite root) {
						Color backgroundColor = null;
						for (IInformationBackgroundProvider<URI> informationBackgroundProvider : UriPresenterService.this.informationBackgroundProviders) {
							backgroundColor = informationBackgroundProvider
									.getBackground(uri);
							if (backgroundColor != null) {
								break;
							}
						}
						if (backgroundColor == null) {
							backgroundColor = SWTResourceManager
									.getColor(SWT.COLOR_INFO_BACKGROUND);
						}
						root.setBackground(backgroundColor);
					}
				});

				return control;
			}
		};
		InformationControlManager<CONTROL, URI> informationControlManager = new InformationControlManager<CONTROL, URI>(
				informationClass, creator, subjectInformationProvider);
		informationControlManager.install(control);
		this.informationControlManagers.put(control, informationControlManager);
		return informationControlManager;
	}

}
