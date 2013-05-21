package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views;

import java.net.URI;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.views.EditorView;
import com.bkahlert.devel.nebula.widgets.browser.IAnker;
import com.bkahlert.devel.nebula.widgets.browser.listener.AnkerAdaptingListener;
import com.bkahlert.devel.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.devel.nebula.widgets.browser.listener.URIAdapter;
import com.bkahlert.devel.nebula.widgets.composer.Composer.ToolbarSet;
import com.bkahlert.devel.nebula.widgets.editor.Editor;
import com.bkahlert.nebula.information.ISubjectInformationProvider;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IInformationPresenterService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;

/*
 * TODO this class used to be in the core plugin ... as much as the
 *            {@link ICodeService#getCodedObject(URI)}. So move the method and
 *            then this class.
 */
/**
 * Instances of this class are {@link EditorView}s check for hovered
 * {@link IAnker}s. <br>
 * If a contained link references a valid {@link ILocatable} corresponding
 * information are shown in a popup.
 * 
 * @param <T>
 * 
 * @author bkahlert
 */
public abstract class InformationPresentingEditorView<T> extends EditorView<T> {

	private static final Logger LOGGER = Logger
			.getLogger(InformationPresentingEditorView.class);

	private IInformationPresenterService informationPresenterService = (IInformationPresenterService) PlatformUI
			.getWorkbench().getService(IInformationPresenterService.class);
	private ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);

	public InformationPresentingEditorView(long delayChangeEventUpTo,
			ToolbarSet toolbarSet, boolean autosave) {
		super(delayChangeEventUpTo, toolbarSet, autosave);
	}

	@Override
	public void postInit() {
		super.postInit();
		this.informationPresenterService.enable(this.getEditor(),
				new ISubjectInformationProvider<Editor<T>, ILocatable>() {
					private ILocatable hoveredLocatable = null;

					private IAnkerListener ankerListener = new AnkerAdaptingListener(
							new URIAdapter() {
								@Override
								public void uriHovered(URI uri, boolean entered) {
									if (entered) {
										try {
											ILocatable codeable = InformationPresentingEditorView.this.locatorService
													.resolve(uri, null).get();
											hoveredLocatable = codeable;
										} catch (InterruptedException e) {
											LOGGER.error(e);
										} catch (ExecutionException e) {
											LOGGER.error(e);
										}
									} else {
										hoveredLocatable = null;
									}
								};
							});

					@Override
					public void register(Editor<T> editor) {
						editor.addAnkerListener(this.ankerListener);
					}

					@Override
					public void unregister(Editor<T> editor) {
						editor.removeAnkerListener(this.ankerListener);
					}

					@Override
					public Point getHoverArea() {
						return new Point(50, 20);
					}

					@Override
					public ILocatable getInformation() {
						return this.hoveredLocatable;
					}
				});
	}

	@Override
	public void dispose() {
		this.informationPresenterService.disable(this.getEditor());
		super.dispose();
	}

}