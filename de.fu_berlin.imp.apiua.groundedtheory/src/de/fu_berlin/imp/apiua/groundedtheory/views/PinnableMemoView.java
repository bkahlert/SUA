package de.fu_berlin.imp.apiua.groundedtheory.views;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.utils.selection.SelectionUtils;
import com.bkahlert.nebula.widgets.editor.Editor;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.apiua.core.services.DataServiceAdapter;
import de.fu_berlin.imp.apiua.core.services.IDataService;
import de.fu_berlin.imp.apiua.groundedtheory.preferences.SUAGTPreferenceUtil;

public class PinnableMemoView extends AbstractMemoView {

	public static final String ID = "de.fu_berlin.imp.apiua.groundedtheory.views.PinnableMemoView";
	public static final RGB pinnedBackgroundColor = RGB.LIGHT_GREY;

	private List<URI> lastLoadedObjects;
	private List<URI> pinnedObjects = new LinkedList<URI>();

	private final ISelectionListener selectionListener = (part, selection) -> {
		if (part == PinnableMemoView.this) {
			return;
		}

		List<URI> toLoad = new ArrayList<URI>(
				PinnableMemoView.this.pinnedObjects);
		for (URI uri : SelectionUtils.getAdaptableObjects(selection, URI.class)) {
			if (!toLoad.contains(uri)) {
				toLoad.add(uri);
			}
		}

		PinnableMemoView.this.lastLoadedObjects = toLoad;
		PinnableMemoView.this.load(toLoad);
	};

	private final Runnable callback = () -> {
		for (Editor<URI> editor : PinnableMemoView.this.getEditors()) {
			RGB rgb = editor.getBackgroundRGB();
			if (rgb == null) {
				rgb = RGB.WHITE;
			}
			if (PinnableMemoView.this.pinnedObjects.contains(editor
					.getLoadedObject())) {
				// TODO also mix colors of indirectly loaded code instances
				editor.setBackground(pinnedBackgroundColor.mix(rgb, .5));
			}
		}
	};

	public PinnableMemoView() {
		IDataService dataService = (IDataService) PlatformUI.getWorkbench()
				.getService(IDataService.class);
		dataService.addDataServiceListener(new DataServiceAdapter() {
			@Override
			public void dataDirectoriesLoaded(
					List<? extends IBaseDataContainer> dataContainers) {
				PinnableMemoView.this.restore();
			}
		});
	}

	private boolean restored = false;

	private void restore() {
		if (this.restored) {
			return;
		}
		this.restored = true;
		List<URI> uris = new SUAGTPreferenceUtil().getLastOpenedMemos();
		PinnableMemoView.this.loadAndClearHistory(null,
				uris.toArray(new URI[0]));

		ExecUtils.asyncExec(
				() -> SelectionUtils.getSelectionService(
						PinnableMemoView.this.getSite().getWorkbenchWindow())
						.addPostSelectionListener(
								PinnableMemoView.this.selectionListener), 1000);
	}

	@Override
	public void postInit() {
		super.postInit();
	}

	@Override
	public void dispose() {
		SelectionUtils.getSelectionService(this.getSite().getWorkbenchWindow())
				.removePostSelectionListener(this.selectionListener);
		super.dispose();
	}

	public void load(List<URI> uris) {
		new SUAGTPreferenceUtil().setLastOpenedMemos(uris);
		PinnableMemoView.this.loadAndClearHistory(this.callback,
				uris.toArray(new URI[0]));
	}

	/**
	 * Defines if the currently loaded memo should be pinned.
	 * <p>
	 * A pinned memo stays open although another object with memo support is
	 * selected.
	 *
	 * @param pinnedObjects
	 */
	public void setPin(boolean pin) {
		this.pinnedObjects = pin ? this.lastLoadedObjects
				: new LinkedList<URI>();
		this.load(this.lastLoadedObjects);
	}

}
