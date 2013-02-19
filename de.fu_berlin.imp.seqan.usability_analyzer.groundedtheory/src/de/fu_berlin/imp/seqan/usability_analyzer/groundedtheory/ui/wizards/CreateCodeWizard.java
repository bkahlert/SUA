package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.colors.RGB;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards.pages.CreateCodeWizardPage;

public class CreateCodeWizard extends Wizard {
	private static final Logger LOGGER = Logger
			.getLogger(CreateCodeWizard.class);

	public static final String TITLE = "Create Code";
	public static final ImageDescriptor IMAGE = ImageManager.WIZBAN_CREATE_CODE;

	protected final CreateCodeWizardPage createCodeWizardPage;
	protected final ICode parentCode;
	private ICode createdCode;

	public CreateCodeWizard(ICode parentCode, RGB initialColor) {
		this.setWindowTitle(TITLE);
		this.setDefaultPageImageDescriptor(IMAGE);
		this.setNeedsProgressMonitor(false);
		this.parentCode = parentCode;
		this.createCodeWizardPage = new CreateCodeWizardPage(initialColor);
	}

	@Override
	public void addPages() {
		this.addPage(createCodeWizardPage);
	}

	@Override
	public boolean performFinish() {
		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);
		String codeCaption = this.createCodeWizardPage.getNewCodeCaption();
		RGB codeColor = this.createCodeWizardPage.getNewCodeColor();
		try {
			createdCode = codeService.createCode(codeCaption, codeColor);
			codeService.setParent(createdCode, parentCode);
		} catch (CodeServiceException e) {
			LOGGER.error("Could not create " + ICode.class.getSimpleName()
					+ " with name " + codeCaption);
		}
		return true;
	}

	public ICode getCreatedCode() {
		return createdCode;
	}
}
