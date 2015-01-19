package de.fu_berlin.imp.apiua.survey.viewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.information.ISubjectInformationProvider;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.utils.Pair;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.utils.selection.SelectionUtils;
import com.bkahlert.nebula.widgets.browser.Browser;
import com.bkahlert.nebula.widgets.browser.BrowserUtils;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser;
import com.bkahlert.nebula.widgets.browser.extended.ISelector;
import com.bkahlert.nebula.widgets.browser.extended.extensions.BrowserExtension;
import com.bkahlert.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.nebula.widgets.browser.extended.html.IElement;
import com.bkahlert.nebula.widgets.browser.listener.AnkerAdapter;
import com.bkahlert.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.nebula.widgets.browser.listener.IDNDListener;
import com.bkahlert.nebula.widgets.browser.listener.IFocusListener;
import com.bkahlert.nebula.widgets.browser.listener.IMouseListener;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.IUriPresenterService;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelationInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IDimension;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.apiua.groundedtheory.ui.Utils;
import de.fu_berlin.imp.apiua.survey.model.groupdiscussion.GroupDiscussionDocumentField;

public class HtmlCodingComposite extends Composite implements
		ISelectionProvider {

	private static final ICodeService CODE_SERVICE = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);
	private ICodeServiceListener codeServiceListener = new ICodeServiceListener() {

		@Override
		public void codesAdded(List<ICode> codes) {
			HtmlCodingComposite.this.refreshAnnotations();
		}

		@Override
		public void codesAssigned(List<ICode> codes, List<URI> uris) {
			HtmlCodingComposite.this.refreshAnnotations();
		}

		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
			HtmlCodingComposite.this.refreshAnnotations();
		}

		@Override
		public void codeRecolored(ICode code, RGB oldColor, RGB newColor) {
			HtmlCodingComposite.this.refreshAnnotations();
		}

		@Override
		public void codesRemoved(List<ICode> removedCodes, List<URI> uris) {
			HtmlCodingComposite.this.refreshAnnotations();
		}

		@Override
		public void codeMoved(ICode code, ICode oldParentCode,
				ICode newParentCode) {
			HtmlCodingComposite.this.refreshAnnotations();
		}

		@Override
		public void codeDeleted(ICode code) {
			HtmlCodingComposite.this.refreshAnnotations();
		}

		@Override
		public void relationsAdded(Set<IRelation> relations) {
			HtmlCodingComposite.this.refreshAnnotations();
		}

		@Override
		public void relationsDeleted(Set<IRelation> relations) {
			HtmlCodingComposite.this.refreshAnnotations();
		}

		@Override
		public void relationsRenamed(Set<IRelation> relations) {
			HtmlCodingComposite.this.refreshAnnotations();
		}

		@Override
		public void relationInstancesAdded(Set<IRelationInstance> relations) {
			HtmlCodingComposite.this.refreshAnnotations();
		}

		@Override
		public void relationInstancesDeleted(Set<IRelationInstance> relations) {
			HtmlCodingComposite.this.refreshAnnotations();
		}

		@Override
		public void memoAdded(URI uri) {
			HtmlCodingComposite.this.refreshAnnotations();
		}

		@Override
		public void memoModified(URI uri) {
			HtmlCodingComposite.this.refreshAnnotations();
		}

		@Override
		public void memoRemoved(URI uri) {
			HtmlCodingComposite.this.refreshAnnotations();
		}

		@Override
		public void episodeAdded(IEpisode episode) {
			HtmlCodingComposite.this.refreshAnnotations();
		}

		@Override
		public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
			HtmlCodingComposite.this.refreshAnnotations();
		}

		@Override
		public void episodesDeleted(Set<IEpisode> deletedEpisodes) {
			HtmlCodingComposite.this.refreshAnnotations();
		}

		@Override
		public void dimensionChanged(URI uri, IDimension oldDimension,
				IDimension newDimension) {
			HtmlCodingComposite.this.refreshAnnotations();
		}

		@Override
		public void dimensionValueChanged(URI uri, String oldValue, String value) {
			HtmlCodingComposite.this.refreshAnnotations();
		}

		@Override
		public void propertiesChanged(URI uri, List<URI> addedProperties,
				List<URI> removedProperties) {
			HtmlCodingComposite.this.refreshAnnotations();
		}

		@Override
		public void axialCodingModelAdded(URI uri) {
			HtmlCodingComposite.this.refreshAnnotations();
		}

		@Override
		public void axialCodingModelUpdated(URI uri) {
			HtmlCodingComposite.this.refreshAnnotations();
		}

		@Override
		public void axialCodingModelRemoved(URI uri) {
			HtmlCodingComposite.this.refreshAnnotations();
		}
	};

	private static final IUriPresenterService PRESENTER_SERVICE = (IUriPresenterService) PlatformUI
			.getWorkbench().getService(IUriPresenterService.class);

	private List<ISelectionChangedListener> selectionChangedListeners = new LinkedList<>();
	private ISelection selection = null;
	private BootstrapBrowser browser;

	public HtmlCodingComposite(Composite parent, int style, String uri) {
		super(parent, style);
		this.setLayout(new FillLayout());

		this.browser = new BootstrapBrowser(this, SWT.NONE,
				new BrowserExtension[] { new BrowserExtension("custom",
						"return window.custominit;", Arrays.asList(BrowserUtils
								.getFile(
										HtmlCodingComposite.class,
										HtmlCodingComposite.class
												.getSimpleName() + ".js")),
						Arrays.asList(BrowserUtils.getFileUrl(
								HtmlCodingComposite.class,
								HtmlCodingComposite.class.getSimpleName()
										+ ".css")), null) {
					@Override
					public String getVerificationScript() {
						return super.getVerificationScript();
					};
				} }) {
		};
		this.browser.deactivateNativeMenu();
		this.browser.addFocusListener(new IFocusListener() {
			@Override
			public void focusLost(IElement element) {
				HtmlCodingComposite.this.selection = null;
			}

			@Override
			public void focusGained(IElement element) {
				if (Arrays.asList(element.getClasses()).contains("codeable")) {
					URI selectedUri = new URI(element.getAttribute("id"));
					HtmlCodingComposite.this.selection = new StructuredSelection(
							selectedUri);
					HtmlCodingComposite.this
							.fireSelectionChanged(new SelectionChangedEvent(
									HtmlCodingComposite.this,
									HtmlCodingComposite.this.selection));
				}
			}
		});
		this.browser.addMouseListener(new IMouseListener() {
			@Override
			public void mouseUp(double x, double y, IElement element) {
			}

			@Override
			public void mouseMove(double x, double y) {
			}

			@Override
			public void mouseDown(double x, double y, IElement element) {
			}

			@Override
			public void clicked(double x, double y, IElement element) {
				if (element.getData("workspace") != null) {
					LocatorService.INSTANCE.showInWorkspace(
							new URI(element.getData("workspace")), false, null);
				}
			}
		});

		CODE_SERVICE.addCodeServiceListener(this.codeServiceListener);
		parent.addDisposeListener(e -> CODE_SERVICE
				.removeCodeServiceListener(HtmlCodingComposite.this.codeServiceListener));

		PRESENTER_SERVICE.enable(this.browser,
				new ISubjectInformationProvider<Control, URI>() {
					private URI hovered = null;

					private final IAnkerListener ankerListener = new AnkerAdapter() {
						@Override
						public void ankerHovered(IAnker anker, boolean entered) {
							URI uri = null;
							try {
								uri = new URI(anker.getAttribute("id"));
								if (LocatorService.INSTANCE.getType(uri) == GroupDiscussionDocumentField.class) {
									uri = null;
								}
							} catch (Exception e) {
								try {
									uri = new URI(anker.getAttribute("href"));
								} catch (Exception e1) {

								}
							}

							if (uri != null && entered) {
								hovered = uri;
							} else {
								hovered = null;
							}
						}
					};

					@Override
					public void register(Control subject) {
						HtmlCodingComposite.this.browser
								.addAnkerListener(this.ankerListener);
					}

					@Override
					public void unregister(Control subject) {
						HtmlCodingComposite.this.browser
								.removeAnkerListener(this.ankerListener);
					}

					@Override
					public Point getHoverArea() {
						return new Point(20, 10);
					}

					@Override
					public URI getInformation() {
						return this.hovered;
					}
				});

		this.browser
				.addDisposeListener((DisposeListener) e -> PRESENTER_SERVICE
						.disable(this.browser));

		// FIXME: Couldn't get TextTransfer running (data always null);
		// emulating LocalTransfer
		this.browser.addDNDListener(new IDNDListener() {
			@Override
			public void dragStart(long offsetX, long offsetY, IElement element,
					String mimeType, String data) {
				if (data != null) {
					LocalSelectionTransfer.getTransfer().setSelection(
							new StructuredSelection(new URI(data)));
				}
			}

			@Override
			public void drop(long offsetX, long offsetY, IElement element,
					String mimeType, String data) {
			}
		});

		this.browser.open(uri, 60000);
	}

	protected void refreshAnnotations() {
		ExecUtils.logException(ExecUtils
				.nonUIAsyncExec((Callable<Void>) () -> {
					List<Pair<String, String>> content = new ArrayList<>();
					for (String id : HtmlCodingComposite.this.getIds().get()) {
						ILocatable locatable = LocatorService.INSTANCE.resolve(
								new URI(id), null).get();
						content.add(new Pair<>(id, locatable != null ? Utils
								.createAnnotations(locatable) : null));
					}
					this.setCodeMarkups(content);
					return null;
				}));
	}

	public void setCodeMarkups(List<Pair<String, String>> content) {
		StringBuffer js = new StringBuffer();
		for (Pair<String, String> c : content) {
			String id = c.getFirst();
			String html = c.getSecond();
			js.append("setCodeMarkup(\""
					+ id
					+ "\", "
					+ (html != null && !html.isEmpty() ? "\""
							+ Browser.escape(html) + "\"" : "null") + ");");
		}
		this.browser.run(js.toString(), 500, "setCodeMarkups");
	}

	private Future<List<String>> getIds() {
		return this.browser.run("return getCodeableIds()",
				IConverter.CONVERTER_STRINGLIST);
	}

	@Override
	public ISelection getSelection() {
		return this.selection;
	}

	@Override
	public void setSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			List<URI> uris = SelectionUtils.getAdaptableObjects(selection,
					URI.class);
			for (URI uri : uris) {
				this.browser.scrollTo(new ISelector.IdSelector(uri.toString()));
			}
		}
	}

	/**
	 * Notifies any selection changed listeners that the viewer's selection has
	 * changed. Only listeners registered at the time this method is called are
	 * notified.
	 *
	 * @param event
	 *            a selection changed event
	 *
	 * @see ISelectionChangedListener#selectionChanged
	 */
	protected void fireSelectionChanged(final SelectionChangedEvent event) {
		for (int i = 0; i < this.selectionChangedListeners.size(); ++i) {
			final ISelectionChangedListener l = this.selectionChangedListeners
					.get(i);
			SafeRunnable.run(new SafeRunnable() {
				@Override
				public void run() {
					l.selectionChanged(event);
				}
			});
		}
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		this.selectionChangedListeners.add(listener);
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		this.selectionChangedListeners.remove(listener);
	}
}
