package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.bkahlert.devel.nebula.utils.information.EnhanceableInformationControl;
import com.bkahlert.devel.nebula.utils.information.InformationControl;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;

public class LocatableInformationControl<T extends ILocatable>
		extends
		EnhanceableInformationControl<T, LocatableInformationControlDelegate<T>> {

	public static interface IPostProcessor<T extends ILocatable> {
		public void postProcess(T element, Composite root,
				ToolBarManager toolBarManager, InformationControl<T> control);
	}

	static final int borderWidth = 5;

	public LocatableInformationControl(Shell parentShell) {
		super(parentShell,
				new DelegateFactory<LocatableInformationControlDelegate<T>>() {
					@Override
					public LocatableInformationControlDelegate<T> create() {
						return new LocatableInformationControlDelegate<T>();
					}
				});
	}

	public void setPostProcessor(IPostProcessor<T> postProcessor) {
		for (LocatableInformationControlDelegate<T> delegate : this
				.getDelegates()) {
			delegate.setPostProcessor(postProcessor);
		}
	}

}