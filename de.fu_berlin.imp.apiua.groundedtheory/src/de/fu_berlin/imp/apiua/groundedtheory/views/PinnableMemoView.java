package de.fu_berlin.imp.apiua.groundedtheory.views;

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.selection.SelectionUtils;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.preferences.SUAGTPreferenceUtil;

public class PinnableMemoView extends AbstractMemoView {

	public static final String ID = "de.fu_berlin.imp.apiua.groundedtheory.views.PinnableMemoView";

	private ISelection lastSelection;
	private boolean pin = false;

	private final ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (part == PinnableMemoView.this) {
				return;
			}
			PinnableMemoView.this.lastSelection = selection;
			if (!PinnableMemoView.this.pin) {
				PinnableMemoView.this.load(selection);
			}
		}
	};

	public PinnableMemoView() {

	}

	@Override
	public void postInit() {
		super.postInit();

		List<URI> uris = new SUAGTPreferenceUtil().getLastOpenedMemos();
		PinnableMemoView.this.loadAndClearHistory(uris.toArray(new URI[0]));

		ExecUtils.asyncExec(new Runnable() {
			@Override
			public void run() {
				SelectionUtils.getSelectionService(
						PinnableMemoView.this.getSite().getWorkbenchWindow())
						.addPostSelectionListener(
								PinnableMemoView.this.selectionListener);
			}
		}, 1000);
	}

	@Override
	public void dispose() {
		SelectionUtils.getSelectionService(this.getSite().getWorkbenchWindow())
				.removePostSelectionListener(this.selectionListener);
		super.dispose();
	}

	public void load(ISelection selection) {
		final List<URI> uris = SelectionUtils.getAdaptableObjects(selection,
				URI.class);

		new SUAGTPreferenceUtil().setLastOpenedMemos(uris);
		PinnableMemoView.this.loadAndClearHistory(uris.toArray(new URI[0]));
	}

	/**
	 * Defines if the currently loaded memo should be pinned.
	 * <p>
	 * A pinned memo stays open although another object with memo support is
	 * selected.
	 * 
	 * @param pin
	 */
	public void setPin(boolean pin) {
		this.pin = pin;
		if (!pin) {
			this.load(this.lastSelection);
		}
	}

}
