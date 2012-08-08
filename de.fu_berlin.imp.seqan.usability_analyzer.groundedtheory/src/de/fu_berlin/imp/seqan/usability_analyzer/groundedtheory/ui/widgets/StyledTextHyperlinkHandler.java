package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.widgets;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

class StyledTextHyperlinkHandler implements Listener {

	private static final Logger LOGGER = Logger
			.getLogger(StyledTextHyperlinkHandler.class);

	public static void addListenerTo(StyledText styledText) {
		assert styledText != null;
		styledText.addListener(SWT.MouseUp, new StyledTextHyperlinkHandler());
	}

	@Override
	public void handleEvent(Event event) {
		if (!(event.widget instanceof StyledText))
			return;

		StyledText text = (StyledText) event.widget;
		String str = text.getText().replace('\n', ' ');
		int offset;
		try {
			offset = text.getOffsetAtLocation(new Point(event.x, event.y));
		} catch (IllegalArgumentException e) {
			// TODO: when does this occur? how to get rid of it?
			return;
		}
		while (str.charAt(offset) != ' ' && offset > 0)
			offset--;
		final String href = str.substring(offset == 0 ? 0 : offset + 1).split(
				" ")[0];
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					final URI uri = new URI(href);
					if (uri.getScheme() == null)
						return;
					if (uri.getScheme().equalsIgnoreCase("SUA")) {
						ICodeService codeService = (ICodeService) PlatformUI
								.getWorkbench().getService(ICodeService.class);
						if (!codeService.showCodedObjectInWorkspace(uri)) {
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									MessageDialog.openInformation(PlatformUI
											.getWorkbench()
											.getActiveWorkbenchWindow()
											.getShell(), "Artefact not found",
											"The artefact " + uri.toString()
													+ " could not be found.");
								}
							});
						}
					} else {
						try {
							IWebBrowser browser = PlatformUI.getWorkbench()
									.getBrowserSupport().getExternalBrowser();
							browser.openURL(new URL(uri.toString()));
						} catch (PartInitException e) {
							LOGGER.error("Can't open external browser to open "
									+ uri.toString(), e);
						} catch (MalformedURLException e) {
							LOGGER.error("Can't convert "
									+ URI.class.getSimpleName() + " to "
									+ URL.class.getSimpleName() + ": "
									+ uri.toString());
						}
					}
				} catch (URISyntaxException e) {
					LOGGER.fatal("Invalid URI in "
							+ MemoComposer.class.getSimpleName() + ": " + href);
				}
			}
		}, StyledTextHyperlinkHandler.class.getSimpleName()).start();
	}
}