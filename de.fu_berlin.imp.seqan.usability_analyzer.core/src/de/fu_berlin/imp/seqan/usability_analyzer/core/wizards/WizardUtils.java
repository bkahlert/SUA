package de.fu_berlin.imp.seqan.usability_analyzer.core.wizards;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.wizards.dialogs.CenteredWizardDialog;

/**
 * Utility class for {@link IWizard}s
 */
public class WizardUtils {
	private static final Logger log = Logger.getLogger(WizardUtils.class
			.getName());

	/**
	 * Open a wizard in the SWT thread and returns the {@link WizardDialog}'s
	 * return code.
	 * 
	 * @param parentShell
	 * @param wizard
	 * @param initialSize
	 * @return
	 */
	public static Integer openWizard(final Shell parentShell,
			final Wizard wizard, final Point initialSize) {
		try {
			ExecUtils.syncExec(new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					WizardDialog wizardDialog = new CenteredWizardDialog(
							parentShell, wizard, initialSize);
					wizardDialog.setHelpAvailable(false);
					return wizardDialog.open();
				}
			});
		} catch (Exception e) {
			log.warn("Error opening wizard " + wizard.getWindowTitle(), e);
		}
		return null;
	}

	public static ShowArtefactWizard openShowArtefactWizard() {
		ShowArtefactWizard wizard = new ShowArtefactWizard();
		openWizard(null, wizard, new Point(500, 320));
		return wizard;
	}

}
