package de.fu_berlin.imp.seqan.usability_analyzer.survey.perspectives;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class CognitiveDimensionsPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);

		layout.addView(
				"de.fu_berlin.imp.seqan.usability_analyzer.survey.views.CDView",
				IPageLayout.LEFT, 0.34f, IPageLayout.ID_EDITOR_AREA);
		layout.addView(
				"de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.CodeView",
				IPageLayout.TOP, 0.5f, IPageLayout.ID_EDITOR_AREA);
		layout.addView(
				"de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.PinnableMemoView",
				IPageLayout.RIGHT, 0.39f,
				"de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.CodeView");
		layout.addView(
				"de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.CodeInstancesView",
				IPageLayout.BOTTOM,
				0.5f,
				"de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.PinnableMemoView");
	}

}
