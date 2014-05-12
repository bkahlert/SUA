package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.nebula.viewer.jointjs.JointJSContentProvider;
import com.bkahlert.nebula.viewer.jointjs.JointJSLabelProvider;
import com.bkahlert.nebula.viewer.jointjs.JointJSViewer;
import com.bkahlert.nebula.widgets.jointjs.JointJS;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.SelectiveCodingContentProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.SelectiveCodingLabelProvider;

public class SelectiveCodingView extends ViewPart {

	private JointJS jointjs = null;
	private JointJSViewer jointjsViewer = null;

	public SelectiveCodingView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		this.jointjs = new JointJS(parent, SWT.NONE, "sua://code/",
				"sua://code-link");
		JointJSContentProvider contentProvider = new SelectiveCodingContentProvider();
		JointJSLabelProvider labelProvider = new SelectiveCodingLabelProvider();
		this.jointjsViewer = new JointJSViewer(this.jointjs, contentProvider,
				labelProvider);

		this.jointjsViewer.setInput(PlatformUI.getWorkbench().getService(
				ICodeService.class));
		this.jointjsViewer.refresh();
	}

	public JointJS getJointjs() {
		return this.jointjs;
	}

	@Override
	public void setFocus() {
		this.jointjs.setFocus();
	}

}
