package de.fu_berlin.imp.seqan.usability_analyzer;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class MemoPerspective implements IPerspectiveFactory {

	/**
	 * Creates the initial layout for a page.
	 */
	@SuppressWarnings("unused")
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		addFastViews(layout);
		addViewShortcuts(layout);
		addPerspectiveShortcuts(layout);
		{
			IFolderLayout folderLayout = layout.createFolder("folder_2",
					IPageLayout.LEFT, 0.43f, IPageLayout.ID_EDITOR_AREA);
			folderLayout
					.addView("de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.views.TimelineView");
			folderLayout
					.addView("de.fu_berlin.imp.seqan.usability_analyzer.diff.views.DiffExplorerView");
		}
		layout.addView(
				"de.fu_berlin.imp.seqan.usability_analyzer.diff.views.CompilerOutputView",
				IPageLayout.BOTTOM, 0.62f, "folder_2");
		layout.addView(
				"de.fu_berlin.imp.seqan.usability_analyzer.diff.views.ExecutionOutputView",
				IPageLayout.RIGHT, 0.5f,
				"de.fu_berlin.imp.seqan.usability_analyzer.diff.views.CompilerOutputView");
		layout.addView("org.eclipse.ui.views.ProgressView", IPageLayout.BOTTOM,
				0.5f,
				"de.fu_berlin.imp.seqan.usability_analyzer.core.views.TimeZoneDateRangeView");
		{
			IFolderLayout folderLayout = layout.createFolder("folder",
					IPageLayout.RIGHT, 0.5f, IPageLayout.ID_EDITOR_AREA);
			folderLayout
					.addView("de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.PinnableMemoView");
			folderLayout
					.addView("de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.CodeView");
			folderLayout
					.addView("de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.CodeInstancesView");
		}
		{
			IFolderLayout folderLayout = layout.createFolder("folder_1",
					IPageLayout.BOTTOM, 0.81f, "folder");
			folderLayout
					.addView("de.fu_berlin.imp.seqan.usability_analyzer.entity.views.EntityView");
			folderLayout.addView("org.eclipse.ui.views.ProgressView");
		}
	}

	/**
	 * Add fast views to the perspective.
	 */
	private void addFastViews(IPageLayout layout) {
	}

	/**
	 * Add view shortcuts to the perspective.
	 */
	private void addViewShortcuts(IPageLayout layout) {
	}

	/**
	 * Add perspective shortcuts to the perspective.
	 */
	private void addPerspectiveShortcuts(IPageLayout layout) {
	}

}
