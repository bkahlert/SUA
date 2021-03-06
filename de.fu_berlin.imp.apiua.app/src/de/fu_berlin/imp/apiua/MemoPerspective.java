package de.fu_berlin.imp.apiua;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class MemoPerspective implements IPerspectiveFactory {

	/**
	 * Creates the initial layout for a page.
	 */
	@Override
	@SuppressWarnings("unused")
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		this.addFastViews(layout);
		this.addViewShortcuts(layout);
		this.addPerspectiveShortcuts(layout);
		{
			IFolderLayout folderLayout = layout.createFolder("folder_2",
					IPageLayout.LEFT, 0.43f, IPageLayout.ID_EDITOR_AREA);
			folderLayout
					.addView("de.fu_berlin.imp.apiua.timeline.ui.views.TimelineView");
			folderLayout
					.addView("de.fu_berlin.imp.apiua.diff.views.DiffView");
		}
		layout.addView(
				"de.fu_berlin.imp.apiua.diff.views.CompilerOutputView",
				IPageLayout.BOTTOM, 0.62f, "folder_2");
		layout.addView(
				"de.fu_berlin.imp.apiua.diff.views.ExecutionOutputView",
				IPageLayout.RIGHT, 0.5f,
				"de.fu_berlin.imp.apiua.diff.views.CompilerOutputView");
		layout.addView("org.eclipse.ui.views.ProgressView", IPageLayout.BOTTOM,
				0.5f,
				"de.fu_berlin.imp.apiua.core.views.TimeZoneDateRangeView");
		{
			IFolderLayout folderLayout = layout.createFolder("folder",
					IPageLayout.RIGHT, 0.5f, IPageLayout.ID_EDITOR_AREA);
			folderLayout
					.addView("de.fu_berlin.imp.apiua.groundedtheory.views.PinnableMemoView");
		}
		{
			IFolderLayout folderLayout = layout.createFolder("folder_1",
					IPageLayout.BOTTOM, 0.81f, "folder");
			folderLayout
					.addView("de.fu_berlin.imp.apiua.entity.views.EntityView");
			folderLayout.addView("org.eclipse.ui.views.ProgressView");
		}
		{
			IFolderLayout folderLayout = layout.createFolder("folder_3",
					IPageLayout.BOTTOM, 0.78f, "folder");
			folderLayout
					.addView("de.fu_berlin.imp.apiua.groundedtheory.views.CodeView");
			folderLayout
					.addView("de.fu_berlin.imp.apiua.groundedtheory.views.CodeInstancesView");
			folderLayout
					.addView("de.fu_berlin.imp.apiua.groundedtheory.views.EpisodeView");
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
