package de.fu_berlin.imp.apiua;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class TimelinePerspective implements IPerspectiveFactory {

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
					.addView("de.fu_berlin.imp.apiua.timeline.ui.views.TimelineView");
			folderLayout
					.addView("de.fu_berlin.imp.apiua.diff.views.DiffView");
		}
		{
			IFolderLayout folderLayout = layout.createFolder("folder_1",
					IPageLayout.BOTTOM, 0.7f, IPageLayout.ID_EDITOR_AREA);
			folderLayout
					.addView("de.fu_berlin.imp.apiua.entity.views.EntityView");
		}
		layout.addView(
				"de.fu_berlin.imp.apiua.core.views.TimeZoneDateRangeView",
				IPageLayout.RIGHT, 0.61f, "folder_1");
		layout.addView("org.eclipse.ui.views.ProgressView", IPageLayout.BOTTOM,
				0.5f,
				"de.fu_berlin.imp.apiua.core.views.TimeZoneDateRangeView");
		layout.addView(
				"de.fu_berlin.imp.apiua.groundedtheory.views.CodeInstancesView",
				IPageLayout.RIGHT, 0.52f, IPageLayout.ID_EDITOR_AREA);
		layout.addView(
				"de.fu_berlin.imp.apiua.groundedtheory.views.PinnableMemoView",
				IPageLayout.BOTTOM,
				0.5f,
				"de.fu_berlin.imp.apiua.groundedtheory.views.CodeInstancesView");
		{
			IFolderLayout folderLayout = layout
					.createFolder(
							"folder",
							IPageLayout.RIGHT,
							0.46f,
							"de.fu_berlin.imp.apiua.groundedtheory.views.CodeInstancesView");
			folderLayout
					.addView("de.fu_berlin.imp.apiua.groundedtheory.views.CodeView");
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
