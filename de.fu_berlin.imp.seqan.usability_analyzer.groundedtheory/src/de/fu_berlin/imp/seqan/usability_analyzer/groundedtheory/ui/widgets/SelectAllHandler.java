package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class SelectAllHandler implements Listener {

	public static void addListenerTo(Text text) {
		assert text != null;
		text.addListener(SWT.KeyUp, new SelectAllHandler());
	}

	public static void addListenerTo(StyledText styledText) {
		assert styledText != null;
		styledText.addListener(SWT.KeyUp, new SelectAllHandler());
	}

	public void handleEvent(Event event) {
		if ((event.stateMask == SWT.CTRL || event.stateMask == SWT.COMMAND)
				&& event.keyCode == 'a') {
			if (event.widget instanceof Text)
				((Text) event.widget).selectAll();
			if (event.widget instanceof StyledText)
				((StyledText) event.widget).selectAll();
		}
	}
}
