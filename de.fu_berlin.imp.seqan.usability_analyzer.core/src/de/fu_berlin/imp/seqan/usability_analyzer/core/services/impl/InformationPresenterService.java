package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import com.bkahlert.devel.nebula.utils.information.ISubjectInformationProvider;
import com.bkahlert.devel.nebula.utils.information.InformationControl;
import com.bkahlert.devel.nebula.utils.information.InformationControlCreator;
import com.bkahlert.devel.nebula.utils.information.InformationControlManager;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IInformationPresenterService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl.LocatableInformationControl.IPostProcessor;

public class InformationPresenterService implements
		IInformationPresenterService {

	private List<IInformationBackgroundProvider> informationBackgroundProviders = new ArrayList<IInformationBackgroundProvider>();
	private List<IInformationToolBarContributionsProvider> informationToolBarContributionsProviders = new ArrayList<IInformationToolBarContributionsProvider>();

	private Map<Control, InformationControlManager<?, ?>> informationControlManagers = new HashMap<Control, InformationControlManager<?, ?>>();

	private class OwnerInformationControlCreator extends
			InformationControlCreator<ILocatable> {
		private InformationControlManager<?, ILocatable> owner = null;

		public void setOwner(
				InformationControlManager<?, ILocatable> informationControlManager) {
			Assert.isLegal(informationControlManager != null);
			this.owner = informationControlManager;
		}

		@Override
		protected InformationControl<ILocatable> doCreateInformationControl(
				Shell parent) {
			final LocatableInformationControl<ILocatable> control = new LocatableInformationControl<ILocatable>(
					parent);

			control.setPostProcessor(new IPostProcessor<ILocatable>() {
				@Override
				public void postProcess(ILocatable element, Composite root,
						ToolBarManager toolBarManager,
						InformationControl<ILocatable> control) {
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

					if (toolBarManager != null) {
						for (IInformationToolBarContributionsProvider informationToolBarContributionsProvider : InformationPresenterService.this.informationToolBarContributionsProviders) {
							informationToolBarContributionsProvider.fill(
									element, toolBarManager, control,
									OwnerInformationControlCreator.this.owner);
						}
					}
				}
			});

			return control;
		}
	}

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
	public void addInformationToolBarContributionProvider(
			IInformationToolBarContributionsProvider informationToolBarContributionsProvider) {
		this.informationToolBarContributionsProviders
				.add(informationToolBarContributionsProvider);
	}

	@Override
	public void removeInformationToolBarContributionProvider(
			IInformationToolBarContributionsProvider informationToolBarContributionsProvider) {
		this.informationToolBarContributionsProviders
				.remove(informationToolBarContributionsProvider);
	}

	@Override
	public <CONTROL extends Control> InformationControlManager<CONTROL, ILocatable> enable(
			CONTROL control,
			ISubjectInformationProvider<CONTROL, ILocatable> subjectInformationProvider) {
		OwnerInformationControlCreator x = new OwnerInformationControlCreator();
		InformationControlManager<CONTROL, ILocatable> informationControlManager = new InformationControlManager<CONTROL, ILocatable>(
				x, subjectInformationProvider);
		x.setOwner(informationControlManager);
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
