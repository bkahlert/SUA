package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.colors.RGB;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards.pages.AddCodeWizardPage;

public class AddCodeWizard extends Wizard {
	private static final Logger LOGGER = Logger.getLogger(AddCodeWizard.class);

	public static final String TITLE = "Add Code";
	public static final ImageDescriptor IMAGE = ImageManager.WIZBAN_ADD_CODE;

	protected final AddCodeWizardPage addCodeWizardPage;

	protected List<ICode> affectedCodes;

	private List<ILocatable> locatables;

	public AddCodeWizard(List<ILocatable> locatables, RGB initialRGB) {
		this.setWindowTitle(TITLE);
		this.setDefaultPageImageDescriptor(IMAGE);
		this.setNeedsProgressMonitor(false);
		this.locatables = locatables;
		this.addCodeWizardPage = new AddCodeWizardPage(initialRGB);
	}

	@Override
	public void addPages() {
		this.addPage(addCodeWizardPage);
	}

	@Override
	public boolean performFinish() {
		affectedCodes = new LinkedList<ICode>();
		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);

		if (this.addCodeWizardPage.getCreateCode()) {
			String codeCaption = this.addCodeWizardPage.getNewCodeCaption();
			RGB rgb = this.addCodeWizardPage.getNewCodeRGB();
			ICode createdCode = null;
			for (ILocatable locatable : locatables) {
				try {
					if (createdCode == null) {
						createdCode = codeService.addCode(codeCaption, rgb,
								locatable);
						affectedCodes.add(createdCode);
					} else {
						codeService.addCode(createdCode, locatable);
					}
					LOGGER.info("Code " + createdCode + " added to " + locatable);
				} catch (CodeServiceException e) {
					LOGGER.error("Code " + codeCaption
							+ " couldn't be added to " + locatable, e);
				}
			}
		} else {
			List<ICode> codes = this.addCodeWizardPage.getExistingCodes();
			try {
				codeService.addCodes(codes, locatables);
				LOGGER.info(ICode.class.getSimpleName() + "s \""
						+ StringUtils.join(codes, "\", \"") + "\" added to \""
						+ StringUtils.join(locatables, "\", \"") + "\"");
			} catch (CodeServiceException e) {
				LOGGER.error(
						ICode.class.getSimpleName() + "s \""
								+ StringUtils.join(codes, "\", \"")
								+ "\" could not be added to \""
								+ StringUtils.join(locatables, "\", \"") + "\"",
						e);
			}
			affectedCodes.addAll(codes);
		}

		return true;
	}

	public List<ICode> getAffectedCodes() {
		return affectedCodes;
	}
}
