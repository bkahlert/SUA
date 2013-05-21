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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.bkahlert.devel.nebula.colors.RGB;
import com.bkahlert.devel.nebula.wizards.dialogs.CenteredWizardDialog;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;

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
	public static AddCodeWizard openAddCodeWizard(List<ILocatable> codeables,
			RGB initialRGB) {
		return openWizardSuccessfully(new AddCodeWizard(codeables, initialRGB),
				new Point(800, 600));
	}

	/**
	 * Opens a {@link AddCodeWizard} in the SWT thread and returns the displayed
	 * instance in case of success.
	 * 
	 * @param codeable
	 * @return
	 */
	@SuppressWarnings("serial")
	public static AddCodeWizard openAddCodeWizard(final ILocatable codeable,
			RGB initialRGB) {
		return openWizardSuccessfully(new AddCodeWizard(
				new ArrayList<ILocatable>() {
					{
						this.add(codeable);
					}
				}, initialRGB), new Point(800, 600));
	}

	/**
	 * Opens a {@link CreateCodeWizard} in the SWT thread and returns the
	 * displayed instance in case of success.
	 * 
	 * @param parentCode
	 * @param initialColor
	 * @return
	 */
	public static CreateCodeWizard openNewCodeWizard(ICode parentCode,
			RGB initialColor) {
		return openWizardSuccessfully(new CreateCodeWizard(parentCode,
				initialColor), new Point(500, 300));
	}

	/**
	 * Opens a {@link AddEpisodeWizard} in the SWT thread and returns the
	 * displayed instance in case of success.
	 * 
	 * @param identifier
	 * @param range
	 * @return
	 */
	public static AddEpisodeWizard openAddEpisodeWizard(IIdentifier identifier,
			TimeZoneDateRange range) {
		return openWizardSuccessfully(new AddEpisodeWizard(identifier, range),
				new Point(500, 220));
	}

}
