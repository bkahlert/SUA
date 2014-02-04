package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import java.net.URI;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.bkahlert.nebula.information.EnhanceableInformationControl;

public class UriInformationControl extends
		EnhanceableInformationControl<URI, UriInformationControlDelegate> {

	public static interface IPostProcessor {
		public void postProcess(URI uri, Composite root);
	}

	static final int borderWidth = 5;

	public UriInformationControl(Shell parentShell) {
		super(UriInformationControl.class.getClassLoader(), URI.class,
				parentShell,
				new DelegateFactory<UriInformationControlDelegate>() {
					@Override
					public UriInformationControlDelegate create() {
						return new UriInformationControlDelegate();
					}
				});
	}

	public void setPostProcessor(IPostProcessor postProcessor) {
		for (UriInformationControlDelegate delegate : this.getDelegates()) {
			delegate.setPostProcessor(postProcessor);
		}
	}

}