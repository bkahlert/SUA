package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards.pages;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import com.bkahlert.devel.nebula.colors.RGB;
import com.bkahlert.devel.nebula.widgets.ColorPicker;
import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.CodeViewer;

/**
 * Allows the user to enter a {@link JID}.
 * <p>
 * The wizard page is not supposed to show an error as long as the user did not
 * type the {@link JID} correctly at least once.
 * <p>
 * This conforms to Eclipse Usability Guideline 5.3: Start the wizard with a
 * prompt, not an error message.
 * 
 * @see <a
 *      href="http://wiki.eclipse.org/User_Interface_Guidelines#Wizards">Eclipse
 *      User Interface Guidelines</a>
 * @author bkahlert
 */
public class AddCodeWizardPage extends ORWizardPage {
	private static final String DESCRIPTION = "Choose an existing code to add or create a new one.";
	private Text newCodeCaption;
	private ColorPicker colorPicker;
	private final RGB initialRGB;
	private boolean createCode = false;
	private CodeViewer codeViewer;

	public AddCodeWizardPage(RGB rgb) {
		super(AddCodeWizardPage.class.getName(), 2);
		setTitle("Add Code");
		setDescription(DESCRIPTION);
		this.initialRGB = rgb;
	}

	@Override
	public void fillContent(Composite... contentComposites) {
		fillLeftColumn(contentComposites[0]);
		fillRightColumn(contentComposites[1]);
	}

	protected void fillLeftColumn(Composite composite) {
		composite.setLayout(new FillLayout());
		Group group = new Group(composite, SWT.NONE);
		group.setLayout(GridLayoutFactory.swtDefaults().create());
		group.setText("New Code");

		Composite centerWrapper = new Composite(group, SWT.NONE);
		centerWrapper.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true,
				true));
		centerWrapper.setLayout(new GridLayout(2, false));

		this.newCodeCaption = new Text(centerWrapper, SWT.BORDER);
		this.newCodeCaption.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, false).hint(200, SWT.DEFAULT).create());
		this.newCodeCaption.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateCompletion(newCodeCaption.getText());
			}
		});

		colorPicker = new ColorPicker(centerWrapper,
				this.initialRGB.toClassicRGB());
		colorPicker.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false));
	}

	protected void fillRightColumn(Composite composite) {
		composite.setLayout(new FillLayout());
		Group group = new Group(composite, SWT.NONE);
		group.setLayout(new GridLayout(1, false));
		group.setText("Existing Code");

		this.codeViewer = new CodeViewer(group, SWT.NONE, false);
		this.codeViewer.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, true).create());
		this.codeViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						updatePageCompletion(SelectionUtils
								.getAdaptableObjects(event.getSelection(),
										ICode.class));
					}
				});
		this.codeViewer.getViewer().expandAll();
	}

	private void updateCompletion(String newCodeCaption) {
		if (!newCodeCaption.isEmpty()) {
			this.setMessage(DESCRIPTION);
			this.setErrorMessage(null);
			createCode = true;
			setPageComplete(true);
		} else {
			this.setErrorMessage("A new code must not be empty!");
			this.setPageComplete(false);
		}
	}

	private void updatePageCompletion(List<ICode> newCodes) {
		if (newCodes.size() > 0) {
			this.setMessage(DESCRIPTION);
			this.setErrorMessage(null);
			createCode = false;
			setPageComplete(true);
		} else {
			this.setErrorMessage("You must select at least one code!");
			this.setPageComplete(false);
		}
	}

	public boolean getCreateCode() {
		return this.createCode;
	}

	public String getNewCodeCaption() {
		return this.newCodeCaption.getText();
	}

	public RGB getNewCodeRGB() {
		return new RGB(this.colorPicker.getRGB());
	}

	public List<ICode> getExistingCodes() {
		return SelectionUtils.getAdaptableObjects(
				this.codeViewer.getSelection(), ICode.class);
	}
}