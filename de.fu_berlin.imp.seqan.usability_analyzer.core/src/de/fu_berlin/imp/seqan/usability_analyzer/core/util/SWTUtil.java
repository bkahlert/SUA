package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import org.eclipse.swt.widgets.Display;

public class SWTUtil {
	public static void runSync(Runnable runnable) {
		Display.getCurrent().syncExec(runnable);
	}

	public static void runASync(Runnable runnable) {
		Display.getCurrent().asyncExec(runnable);
	}
}
