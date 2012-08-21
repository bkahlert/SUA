package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.bkahlert.devel.nebula.wizards.dialogs.CenteredWizardDialog;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

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
			final AtomicInteger rt = new AtomicInteger();
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					WizardDialog wizardDialog = new CenteredWizardDialog(
							parentShell, wizard, initialSize);
					wizardDialog.setHelpAvailable(false);
					rt.set(wizardDialog.open());
				}
			});
			return rt.get();
		} catch (Exception e) {
			log.warn("Error opening wizard " + wizard.getWindowTitle(), e);
		}
		return null;
	}

	/**
	 * Open a wizard in the SWT thread and returns the {@link WizardDialog}'s
	 * reference to the {@link Wizard} in case of success.
	 * 
	 * @param wizard
	 * @param initialSize
	 * 
	 * @return the wizard if it was successfully finished; null otherwise
	 */
	public static <W extends Wizard> W openWizardSuccessfully(
			final Shell parentShell, final W wizard, final Point initialSize) {
		Integer returnCode = openWizard(parentShell, wizard, initialSize);
		return (returnCode != null && returnCode == Window.OK) ? wizard : null;
	}

	/**
	 * Open a wizard in the SWT thread and returns the {@link WizardDialog}'s
	 * reference to the {@link Wizard} in case of success.
	 * 
	 * @param wizard
	 * @param initialSize
	 * 
	 * @return the wizard if it was successfully finished; null otherwise
	 */
	public static <W extends Wizard> W openWizardSuccessfully(final W wizard,
			final Point initialSize) {
		return openWizardSuccessfully(null, wizard, initialSize);
	}

	/**
	 * Opens a {@link AddCodeWizard} in the SWT thread and returns the displayed
	 * instance in case of success.
	 */
	public static AddCodeWizard openAddCodeWizard(List<ICodeable> codeables) {
		return openWizardSuccessfully(new AddCodeWizard(codeables), new Point(
				800, 600));
	}

	/**
	 * Opens a {@link AddCodeWizard} in the SWT thread and returns the displayed
	 * instance in case of success.
	 * 
	 * @param codeable
	 * @return
	 */
	@SuppressWarnings("serial")
	public static AddCodeWizard openAddCodeWizard(final ICodeable codeable) {
		return openWizardSuccessfully(new AddCodeWizard(
				new ArrayList<ICodeable>() {
					{
						add(codeable);
					}
				}), new Point(800, 600));
	}

	/**
	 * Opens a {@link CreateCodeWizard} in the SWT thread and returns the
	 * displayed instance in case of success.
	 * 
	 * @param parentCode
	 * @return
	 */
	public static CreateCodeWizard openNewCodeWizard(ICode parentCode) {
		return openWizardSuccessfully(new CreateCodeWizard(parentCode),
				new Point(500, 300));
	}

	/**
	 * Opens a {@link AddEpisodeWizard} in the SWT thread and returns the
	 * displayed instance in case of success.
	 * 
	 * @param id
	 * @param range
	 * @return
	 */
	public static AddEpisodeWizard openAddEpisodeWizard(ID id,
			TimeZoneDateRange range, RGB initialRgb) {
		return openWizardSuccessfully(new AddEpisodeWizard(id, range,
				initialRgb), new Point(500, 220));
	}

	/**
	 * Opens a {@link AddEpisodeWizard} in the SWT thread and returns the
	 * displayed instance in case of success.
	 * 
	 * @param fingerprint
	 * @param range
	 * @return
	 */
	public static AddEpisodeWizard openAddEpisodeWizard(
			Fingerprint fingerprint, TimeZoneDateRange range, RGB initialRgb) {
		return openWizardSuccessfully(new AddEpisodeWizard(fingerprint, range,
				initialRgb), new Point(500, 220));
	}
}
