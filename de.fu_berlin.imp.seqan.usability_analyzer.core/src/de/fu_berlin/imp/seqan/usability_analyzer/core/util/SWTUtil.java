package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class SWTUtil {
	public static void runSync(Runnable runnable) {
		Display.getCurrent().syncExec(runnable);
	}

	public static void runASync(Runnable runnable) {
		Display.getCurrent().asyncExec(runnable);
	}

	/**
	 * Disposes all child {@link Control}s of the given {@link Composite}.
	 * 
	 * @param control
	 */
	public static void clearControl(Composite composite) {
		for (Control control : composite.getChildren())
			if (!control.isDisposed())
				control.dispose();
	}
}
