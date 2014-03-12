package de.fu_berlin.imp.seqan.usability_analyzer.core.views;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.information.ISubjectInformationProvider;
import com.bkahlert.nebula.views.EditorView;
import com.bkahlert.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.nebula.widgets.browser.listener.AnkerAdaptingListener;
import com.bkahlert.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.nebula.widgets.browser.listener.URIAdapter;
import com.bkahlert.nebula.widgets.composer.Composer.ToolbarSet;
import com.bkahlert.nebula.widgets.editor.Editor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IUriPresenterService;

/**
 * Instances of this class are {@link EditorView}s that check for hovered
 * {@link IAnker}s. <br>
 * If a contained link references a valid {@link ILocatable} corresponding
 * information are shown in a popup.
 * <p>
 * This class serves as an abstract base class that needs to be inherited.
 * Methods to be implemented mainly include those that load and save the
 * {@link Editor}'s content.
 * 
 * @param <INFORMATION>
 *            type of the objects that can be loaded the wrapped {@link Editor}
 * 
 * @author bkahlert
 */
public abstract class UriPresentingEditorView extends EditorView<URI> {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(UriPresentingEditorView.class);

	private final IUriPresenterService informationPresenterService = (IUriPresenterService) PlatformUI
			.getWorkbench().getService(IUriPresenterService.class);

	public UriPresentingEditorView(long delayChangeEventUpTo,
			ToolbarSet toolbarSet, boolean autosave) {
		super(delayChangeEventUpTo, toolbarSet, autosave);
	}

	@Override
	public void created(List<Editor<URI>> editors) {
		for (Editor<URI> editor : editors) {
			this.informationPresenterService.enable(editor,
					new ISubjectInformationProvider<Editor<URI>, URI>() {
						private URI hoveredUri = null;

						private final IAnkerListener ankerListener = new AnkerAdaptingListener(
								new URIAdapter() {
									@Override
									public void uriHovered(java.net.URI uri,
											boolean entered) {
										if (entered) {
											hoveredUri = new URI(uri);
										} else {
											hoveredUri = null;
										}
									};
								});

						@Override
						public void register(Editor<URI> editor) {
							editor.addAnkerListener(this.ankerListener);
						}

						@Override
						public void unregister(Editor<URI> editor) {
							editor.removeAnkerListener(this.ankerListener);
						}

						@Override
						public Point getHoverArea() {
							return new Point(50, 20);
						}

						@Override
						public URI getInformation() {
							return this.hoveredUri;
						}
					});
		}
	}

	@Override
	public void disposed(List<Editor<URI>> editors) {
		for (Editor<URI> editor : editors) {
			this.informationPresenterService.disable(editor);
		}
	}

	@Override
	public void dispose() {
		this.disposed(this.getEditors());
		super.dispose();
	}

}