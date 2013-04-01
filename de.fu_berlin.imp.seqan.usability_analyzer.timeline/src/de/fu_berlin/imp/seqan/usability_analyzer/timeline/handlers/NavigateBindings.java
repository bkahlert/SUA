package de.fu_berlin.imp.seqan.usability_analyzer.timeline.handlers;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

import com.bkahlert.nebula.information.InformationControl;
import com.bkahlert.nebula.information.extender.IInformationControlExtender;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;

public class NavigateBindings implements
		IInformationControlExtender<ILocatable> {

	private static final Logger LOGGER = Logger
			.getLogger(NavigateBindings.class);

	private Listener listener = new Listener() {
		private IHandlerService handlerService = (IHandlerService) PlatformUI
				.getWorkbench().getService(IHandlerService.class);

		@Override
		public void handleEvent(Event e) {
			if (!NavigateBindings.this.shell.isVisible()) {
				return;
			}

			if (e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT) {
				try {
					this.handlerService.executeCommand(
							"de.fu_berlin.imp.seqan.usability_analyzer.timeline.navigate"
									+ (e.keyCode == SWT.ARROW_LEFT ? "Back"
											: "Forward"), null);
					e.doit = false;
				} catch (Exception e1) {
					LOGGER.error("Error navigating", e1);
				}
			}
		}
	};

	private Shell shell = null;

	@Override
	public void extend(InformationControl<ILocatable> informationControl,
			Composite parent) {
		this.shell = parent.getShell();
		Display.getCurrent().addFilter(SWT.KeyDown, this.listener);
		this.shell.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				Display.getCurrent().removeFilter(SWT.KeyDown,
						NavigateBindings.this.listener);
			}
		});
	}

	@Override
	public void extend(InformationControl<ILocatable> informationControl,
			ILocatable information) {
		return;
	}

}
