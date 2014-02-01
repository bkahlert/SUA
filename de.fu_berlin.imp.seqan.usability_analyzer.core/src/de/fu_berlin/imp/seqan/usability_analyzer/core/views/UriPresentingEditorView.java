package de.fu_berlin.imp.seqan.usability_analyzer.core.views;

import java.net.URI;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.views.EditorView;
import com.bkahlert.devel.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.devel.nebula.widgets.browser.listener.AnkerAdaptingListener;
import com.bkahlert.devel.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.devel.nebula.widgets.browser.listener.URIAdapter;
import com.bkahlert.devel.nebula.widgets.composer.Composer.ToolbarSet;
import com.bkahlert.devel.nebula.widgets.editor.Editor;
import com.bkahlert.nebula.information.ISubjectInformationProvider;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
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

	private IUriPresenterService informationPresenterService = (IUriPresenterService) PlatformUI
			.getWorkbench().getService(IUriPresenterService.class);

	public UriPresentingEditorView(long delayChangeEventUpTo,
			ToolbarSet toolbarSet, boolean autosave) {
		super(delayChangeEventUpTo, toolbarSet, autosave);
	}

	@Override
	public void postInit() {
		super.postInit();
		this.informationPresenterService.enable(this.getEditor(),
				new ISubjectInformationProvider<Editor<URI>, URI>() {
					private URI hoveredUri = null;

					private IAnkerListener ankerListener = new AnkerAdaptingListener(
							new URIAdapter() {
								@Override
								public void uriHovered(URI uri, boolean entered) {
									if (entered) {
										hoveredUri = uri;
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

	@Override
	public void dispose() {
		this.informationPresenterService.disable(this.getEditor());
		super.dispose();
	}

}