package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.dialogs.PopupDialog;
import com.bkahlert.devel.nebula.utils.ExecutorUtil;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDetailPopupService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService.IDetailedLabelProvider;

public class DetailPopupService implements IDetailPopupService {

	private ILabelProviderService labelProviderService = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);
	private PopupDialog popup = null;

	@Override
	public boolean showDetailPopup(final ILocatable locatable) {
		Assert.isNotNull(locatable);
		ILabelProvider labelProvider = this.labelProviderService
				.getLabelProvider(locatable);
		if (labelProvider == null) {
			return false;
		}

		if (labelProvider instanceof IDetailedLabelProvider) {
			final IDetailedLabelProvider detailedLabelProvider = (IDetailedLabelProvider) labelProvider;
			if (detailedLabelProvider.canFillPopup(locatable)) {
				this.popup = new PopupDialog() {
					@Override
					protected Control createControls(Composite parent) {
						return detailedLabelProvider.fillPopup(locatable,
								parent);
					};
				};
				ExecutorUtil.syncExec(new Runnable() {
					@Override
					public void run() {
						DetailPopupService.this.popup.open();
					}
				});
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public void hideDetailPopup() {
		if (this.popup != null) {
			ExecutorUtil.syncExec(new Runnable() {
				@Override
				public void run() {
					DetailPopupService.this.popup.close();
				}
			});
			this.popup = null;
		}
	}

}
