package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

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

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IInformationPresenterService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl.LocatableInformationControl.IPostProcessor;

public class InformationPresenterService implements
		IInformationPresenterService {

	private List<IInformationBackgroundProvider> informationBackgroundProviders = new ArrayList<IInformationBackgroundProvider>();

	private Map<Control, InformationControlManager<?, ?>> informationControlManagers = new HashMap<Control, InformationControlManager<?, ?>>();

	private InformationControlCreator<ILocatable> creator = new InformationControlCreator<ILocatable>() {
		@Override
		protected InformationControl<ILocatable> doCreateInformationControl(
				Shell parent) {
			final LocatableInformationControl<ILocatable> control = new LocatableInformationControl<ILocatable>(
					ILocatable.class, parent);

			control.setPostProcessor(new IPostProcessor<ILocatable>() {
				@Override
				public void postProcess(ILocatable element, Composite root) {
					Color backgroundColor = null;
					for (IInformationBackgroundProvider informationBackgroundProvider : InformationPresenterService.this.informationBackgroundProviders) {
						backgroundColor = informationBackgroundProvider
								.getBackground(element);
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

	@Override
	public void addInformationBackgroundProvider(
			IInformationBackgroundProvider informationBackgroundProvider) {
		this.informationBackgroundProviders.add(informationBackgroundProvider);
	};

	@Override
	public void removeInformationBackgroundProvider(
			IInformationBackgroundProvider informationBackgroundProvider) {
		this.informationBackgroundProviders
				.remove(informationBackgroundProvider);
	};

	@Override
	public <CONTROL extends Control> InformationControlManager<CONTROL, ILocatable> enable(
			CONTROL control,
			ISubjectInformationProvider<CONTROL, ILocatable> subjectInformationProvider) {
		InformationControlManager<CONTROL, ILocatable> informationControlManager = new InformationControlManager<CONTROL, ILocatable>(
				ILocatable.class, this.creator, subjectInformationProvider);
		informationControlManager.install(control);
		this.informationControlManagers.put(control, informationControlManager);
		return informationControlManager;
	}

	@Override
	public <CONTROL extends Control> void disable(CONTROL control) {
		if (this.informationControlManagers.containsKey(control)) {
			this.informationControlManagers.get(control).dispose();
			this.informationControlManagers.remove(control);
		}
	};

}
