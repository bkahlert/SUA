package de.fu_berlin.imp.apiua.groundedtheory.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.information.ISubjectInformationProvider;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.IModifiable;
import com.bkahlert.nebula.utils.IReflexiveConverter;
import com.bkahlert.nebula.utils.ImageUtils;
import com.bkahlert.nebula.utils.Pair;
import com.bkahlert.nebula.utils.StringUtils;
import com.bkahlert.nebula.widgets.browser.BrowserUtils;
import com.bkahlert.nebula.widgets.browser.extended.html.IElement;
import com.bkahlert.nebula.widgets.browser.listener.IDNDListener;
import com.bkahlert.nebula.widgets.browser.listener.IMouseListener;
import com.bkahlert.nebula.widgets.browser.listener.MouseAdapter;
import com.bkahlert.nebula.widgets.jointjs.JointJS;
import com.bkahlert.nebula.widgets.jointjs.JointJS.JointJSListener;
import com.bkahlert.nebula.widgets.jointjs.JointJSCell;
import com.bkahlert.nebula.widgets.jointjs.JointJSModel;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.IImportanceService;
import de.fu_berlin.imp.apiua.core.services.IImportanceServiceListener;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.core.services.IUriPresenterService;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.IAxialCodingModel;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelationInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.ImplicitRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.JointJSAxialCodingModel;
import de.fu_berlin.imp.apiua.groundedtheory.model.ProposedRelation;
import de.fu_berlin.imp.apiua.groundedtheory.preferences.SUAGTPreferenceUtil;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceAdapter;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreReadException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreWriteException;
import de.fu_berlin.imp.apiua.groundedtheory.ui.ImageManager;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.AxialCodingLabelProvider;

/**
 * Can load, edit and save a {@link IAxialCodingModel}.
 *
 * @author bkahlert
 *
 */
public class AxialCodingComposite extends Composite implements
		ISelectionProvider, IModifiable {

	private static final Logger LOGGER = Logger
			.getLogger(AxialCodingComposite.class);

	private static final IImportanceService IMPORTANCE_SERVICE = (IImportanceService) PlatformUI
			.getWorkbench().getService(IImportanceService.class);
	private static final ICodeService CODE_SERVICE = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);
	private static final IUriPresenterService PRESENTER_SERVICE = (IUriPresenterService) PlatformUI
			.getWorkbench().getService(IUriPresenterService.class);
	private static final ILabelProviderService LABEL_PROVIDER_SERVICE = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);

	private final IImportanceServiceListener importanceServiceListener = (uris,
			importance) -> ExecUtils.logException(AxialCodingComposite.this
			.refresh());

	private final ICodeServiceListener codeServiceListener = new CodeServiceAdapter() {

		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
			ExecUtils.logException(AxialCodingComposite.this.refresh());
		}

		@Override
		public void codesRecolored(List<ICode> codes) {
			ExecUtils.logException(AxialCodingComposite.this.refresh());
		}

		@Override
		public void codeMoved(final ICode code, ICode oldParentCode,
				ICode newParentCode) {
			ExecUtils.logException(AxialCodingComposite.this.refresh());
		}

		@Override
		public void codeDeleted(ICode code) {
			ExecUtils.logException(AxialCodingComposite.this.refresh());
		}

		@Override
		public void codesAssigned(java.util.List<ICode> codes,
				java.util.List<URI> uris) {
			ExecUtils.logException(AxialCodingComposite.this.refresh());
		};

		@Override
		public void relationsAdded(java.util.Set<IRelation> relations) {
			ExecUtils.logException(AxialCodingComposite.this.refresh());
		};

		@Override
		public void relationsRenamed(java.util.Set<IRelation> relations) {
			ExecUtils.logException(AxialCodingComposite.this.refresh());
		};

		@Override
		public void relationsDeleted(java.util.Set<IRelation> relations) {
			ExecUtils.logException(AxialCodingComposite.this.refresh());
		};

		@Override
		public void relationInstancesAdded(
				java.util.Set<IRelationInstance> relations) {
			ExecUtils.logException(AxialCodingComposite.this.refresh());
		};

		@Override
		public void relationInstancesDeleted(
				java.util.Set<IRelationInstance> relations) {
			ExecUtils.logException(AxialCodingComposite.this.refresh());
		};

		@Override
		public void memoAdded(URI uri) {
			ExecUtils.logException(AxialCodingComposite.this.refresh());
		};

		@Override
		public void memoModified(URI uri) {
			ExecUtils.logException(AxialCodingComposite.this.refresh());
		};

		@Override
		public void memoRemoved(URI uri) {
			ExecUtils.logException(AxialCodingComposite.this.refresh());
		};
	};

	private final List<ModifyListener> modifyListeners = new ArrayList<ModifyListener>();

	private JointJS jointjs = null;
	private final AxialCodingLabelProvider labelProvider = new AxialCodingLabelProvider();

	private URI openedUri = null;

	public AxialCodingComposite(Composite parent, int style) {
		super(parent, style);

		IMPORTANCE_SERVICE
				.addImportanceServiceListener(this.importanceServiceListener);
		CODE_SERVICE.addCodeServiceListener(this.codeServiceListener);
		this.setLayout(new FillLayout());

		this.addDisposeListener(e -> {
			CODE_SERVICE
					.removeCodeServiceListener(AxialCodingComposite.this.codeServiceListener);
			IMPORTANCE_SERVICE
					.removeImportanceServiceListener(AxialCodingComposite.this.importanceServiceListener);
		});

		this.jointjs = new JointJS(this, SWT.BORDER, "apiua://code/",
				"apiua://relation/", new IReflexiveConverter<String, Object>() {
					@Override
					public Object convert(String returnValue) {
						// default selection
						if (returnValue == null) {
							return AxialCodingComposite.this.openedUri;
						}
						if (returnValue.contains("|")) {
							return null;
						}
						if (returnValue.startsWith("apiua://")) {
							return new URI(returnValue);
						}
						return null;
					}

					@Override
					public String convertBack(Object object) {
						return object.toString();
					}
				});
		this.jointjs.addJointJSListener(new JointJSListener() {
			@Override
			public void modified(JointJSModel model) {
				Event event = new Event();
				event.display = Display.getCurrent();
				event.widget = AxialCodingComposite.this;
				event.text = model.serialize();
				event.data = model;
				ModifyEvent modifyEvent = new ModifyEvent(event);
				for (ModifyListener modifyListener : AxialCodingComposite.this.modifyListeners) {
					modifyListener.modifyText(modifyEvent);
				}
			}

			@Override
			public void doubleClicked(JointJSCell cell) {
				if (LocatorService.INSTANCE != null) {
					URI uri = new URI(cell.getId());
					List<URI> uris = new LinkedList<>();
					uris.add(uri);

					try {
						ProposedRelation proposedRelation = LocatorService.INSTANCE
								.resolve(uri, ProposedRelation.class, null)
								.get();
						if (proposedRelation != null) {
							uris.add(proposedRelation.getFrom());
							uris.add(proposedRelation.getTo());
						}
					} catch (InterruptedException | ExecutionException e) {
						LOGGER.error(e);
					}

					LocatorService.INSTANCE.showInWorkspace(
							uris.toArray(new URI[0]), false, null);
				} else {
					LOGGER.error("Could not retrieve "
							+ ILocatorService.class.getSimpleName());
				}
			}
		});

		this.jointjs.injectCssFile(BrowserUtils.getFileUrl(
				AxialCodingComposite.class, this.getClass().getSimpleName()
						+ ".css"));

		// this.jointjs
		// .injectCss("[droppable].over rect { stroke:black; stroke-width: 4px; stroke-dasharray:5,5;");
		this.jointjs.setEnabled(false);

		this.activateDropSupport();
		this.activateInformationSupport();
	}

	private void activateInformationSupport() {
		PRESENTER_SERVICE.enable(this.jointjs,
				new ISubjectInformationProvider<Control, URI>() {
					private URI hovered = null;
					private boolean isMouseDown = false;

					private final JointJS.IJointJSListener jointJsListener = new JointJS.JointJSListener() {

						@Override
						public void hovered(JointJSCell cell, boolean hoveredIn) {
							if (hoveredIn && cell != null
									&& cell.getId() != null
									&& !cell.getId().contains("|")
									&& cell.getId().startsWith("apiua://")) {
								hovered = new URI(cell.getId());
							} else {
								hovered = null;
							}
						}
					};

					private final IMouseListener mouseListener = new MouseAdapter() {
						@Override
						public void mouseDown(double x, double y,
								IElement element) {
							isMouseDown = true;
						}

						@Override
						public void mouseUp(double x, double y, IElement element) {
							isMouseDown = false;
						}
					};

					@Override
					public void register(Control subject) {
						AxialCodingComposite.this.jointjs
								.addJointJSListener(this.jointJsListener);
						AxialCodingComposite.this.jointjs
								.addMouseListener(this.mouseListener);
						this.hovered = null;
						this.isMouseDown = false;
					}

					@Override
					public void unregister(Control subject) {
						AxialCodingComposite.this.jointjs
								.removeMouseListener(this.mouseListener);
						AxialCodingComposite.this.jointjs
								.removeJointJSListener(this.jointJsListener);
						this.hovered = null;
						this.isMouseDown = false;
					}

					@Override
					public Point getHoverArea() {
						return new Point(10, 10);
					}

					@Override
					public URI getInformation() {
						return !this.isMouseDown ? this.hovered : null;
					}
				});

		this.jointjs.addDisposeListener(e -> PRESENTER_SERVICE
				.disable(AxialCodingComposite.this.jointjs));
	}

	private void activateDropSupport() {
		ExecUtils.logException(this.jointjs
				.run("$('.jointjs svg').attr('droppable', true)"));
		this.jointjs.addDNDListener(new IDNDListener() {
			@Override
			public void dragStart(long offsetX, long offsetY, IElement element,
					String mimeType, String data) {
			}

			@Override
			public void drop(final long offsetX, final long offsetY,
					final IElement element, String mimeType, final String data) {
				if (data == null || data.isEmpty()
						|| AxialCodingComposite.this.openedUri == null) {
					return;
				}

				ExecUtils.nonUIAsyncExec(() -> {
					try {
						if (element.getAttribute("model-id") != null) {
							// already there
							List<URI> elements = AxialCodingComposite.this
									.getElements().get();
							for (final String uriString1 : data.split("\\|")) {
								URI uri1 = new URI(uriString1);
								if (LocatorService.INSTANCE.resolve(uri1, null)
										.get() != null
										&& !elements.contains(uri1)) {
									AxialCodingComposite.this.replaceElement(
											new URI(element
													.getAttribute("model-id")),
											uri1);
									break; // only replace using the first
									// element
								}
							}
						} else {
							// new element
							Point pan = AxialCodingComposite.this.jointjs
									.getPan().get();
							Double zoom = AxialCodingComposite.this.jointjs
									.getZoom().get();
							int dropX = (int) ((offsetX / zoom) - pan.x);
							int dropY = (int) ((offsetY / zoom) - pan.y);
							for (final String uriString2 : data.split("\\|")) {
								URI uri2 = new URI(uriString2);
								Point size = AxialCodingComposite.this.labelProvider
										.getSize(uri2);
								if (size != null) {
									dropX -= size.x / 2;
									dropY -= size.y / 2;
								}
								if (LocatorService.INSTANCE.resolve(uri2, null)
										.get() != null) {
									AxialCodingComposite.this.createElements(Arrays
											.asList(new Pair<URI, Point>(uri2,
													new Point(dropX, dropY))));
									AxialCodingComposite.this.refresh();
									AxialCodingComposite.this.jointjs
											.run("$('.jointjs svg .element').attr('droppable', true)");
								}
							}
						}
					} catch (Exception e) {
						LOGGER.error("Error dropping " + data, e);
					}
				});

			}
		});
	}

	public Future<Void> open(final URI uri) {
		Assert.isNotNull(uri);
		return ExecUtils
				.nonUIAsyncExec((Callable<Void>) () -> {
					try {
						IAxialCodingModel axialCodingModel = CODE_SERVICE
								.getAxialCodingModel(uri);
						if (axialCodingModel == null) {
							return AxialCodingComposite.this.open(null).get();
						}
						AxialCodingComposite.this.openedUri = uri;
						new SUAGTPreferenceUtil()
								.setLastOpenedAxialCodingModels(Arrays
										.asList(uri));
						AxialCodingComposite.this.jointjs.load(
								axialCodingModel.serialize()).get();
						AxialCodingComposite.this.refresh().get();
						AxialCodingComposite.this.jointjs
								.run("$('.jointjs svg .element').attr('droppable', true)");
						AxialCodingComposite.this.jointjs.setEnabled(true);
					} catch (CodeStoreReadException e1) {
						throw new IllegalArgumentException(e1);
					} catch (Exception e2) {
						LOGGER.error("Error refreshing the axial coding model "
								+ uri, e2);
					}
					return null;
				});
	}

	/**
	 * Saves the currently opened {@link URI}.
	 *
	 * @return
	 */
	public void save() {
		if (this.openedUri == null) {
			return;
		}

		final URI uri = this.openedUri;
		final IAxialCodingModel axialCodingModel = new JointJSAxialCodingModel(
				uri, this.jointjs.getModel());
		try {
			CODE_SERVICE.addAxialCodingModel(axialCodingModel);
		} catch (CodeStoreWriteException e) {
			LOGGER.error("Error saving "
					+ IAxialCodingModel.class.getSimpleName() + " " + uri);
		}
	}

	public URI getOpenedURI() {
		return this.openedUri;
	}

	public URI getOrigin() {
		try {
			IAxialCodingModel acm = CODE_SERVICE
					.getAxialCodingModel(this.openedUri);
			return acm.getOrigin();
		} catch (CodeStoreReadException e) {
			LOGGER.error("Can't retrieve origin", e);
		}
		return null;
	}

	public Set<URI> getOriginCells() {
		Set<URI> uris = new HashSet<>();
		try {
			URI origin = this.getOrigin();
			if (origin != null) {
				uris.add(origin);
				ILocatable locatable = LocatorService.INSTANCE.resolve(origin,
						null).get();
				if (locatable instanceof IRelation) {
					uris.add(((IRelation) locatable).getFrom());
					uris.add(((IRelation) locatable).getTo());
				} else if (locatable instanceof IRelationInstance) {
					uris.add(((IRelationInstance) locatable).getRelation()
							.getUri());
					uris.add(((IRelationInstance) locatable).getRelation()
							.getFrom());
					uris.add(((IRelationInstance) locatable).getRelation()
							.getTo());
					uris.add(((IRelationInstance) locatable).getPhenomenon());
				}
			}
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error("Can't retrieve origin cells", e);
		}
		return uris;
	}

	public Future<List<URI>> getElements() {
		return ExecUtils.nonUIAsyncExec((Callable<List<URI>>) () -> {
			List<URI> uris = new LinkedList<URI>();
			for (String id : AxialCodingComposite.this.jointjs.getElements()
					.get()) {
				uris.add(new URI(id));
			}
			return uris;
		});
	}

	public Future<List<URI>> getRelations() {
		return ExecUtils
				.nonUIAsyncExec((Callable<List<URI>>) () -> {
					List<URI> uris = new LinkedList<URI>();
					for (String id : AxialCodingComposite.this.jointjs
							.getLinks().get()) {
						if (id.contains("|")) {
							continue;
						}
						uris.add(new URI(id));
					}
					return uris;
				});
	}

	public Future<List<URI>> getRelations(String customClass) {
		return ExecUtils.nonUIAsyncExec((Callable<List<URI>>) () -> {
			List<URI> uris = new LinkedList<URI>();
			for (String id : AxialCodingComposite.this.jointjs.getLinks(
					customClass).get()) {
				if (id.contains("|")) {
					continue;
				}
				uris.add(new URI(id));
			}
			return uris;
		});
	}

	public Future<List<URI>> getIsARelations() {
		return ExecUtils.nonUIAsyncExec((Callable<List<URI>>) () -> {
			List<URI> uris = new LinkedList<URI>();
			for (String id : AxialCodingComposite.this.jointjs
					.getPermanentLinks().get()) {
				uris.add(new URI(id));
			}
			return uris;
		});
	}

	public void setTitle(String title) {
		ExecUtils.logException(this.jointjs.setTitle(title));
	}

	public Future<Void> highlight(List<URI> uris) {
		List<String> ids = new LinkedList<String>();
		if (ids != null) {
			for (URI uri : uris) {
				ids.add(uri.toString());
			}
		}
		return this.jointjs.highlight(ids);
	}

	/**
	 *
	 * @param uri
	 * @param position
	 * @throws Exception
	 *             <p>
	 *             Important: You need to call {@link #refresh()} manually.
	 *
	 * @NonUIThread
	 */
	public void createElements(List<Pair<URI, Point>> urisAndPositions)
			throws Exception {
		StringBuilder js = new StringBuilder();
		for (Pair<URI, Point> uriAndPosition : urisAndPositions) {
			URI uri = uriAndPosition.getFirst();
			Point position = uriAndPosition.getSecond();
			Assert.isLegal(uri != null);

			String title = this.labelProvider.getText(uri);
			String content = this.labelProvider.getContent(uri);
			Point size = this.labelProvider.getSize(uri);

			if (position == null) {
				position = new Point(10, 10);
			}

			js.append(JointJS.createElementStatement(uri.toString(), title,
					content, position, size));
		}
		this.jointjs.run(js.toString()).get();
	}

	/**
	 *
	 * @param oldUri
	 * @param newUri
	 * @throws Exception
	 *
	 * @NonUIThread
	 */
	private void replaceElement(URI oldUri, URI newUri) throws Exception {
		this.deleteIsARelations(oldUri).get();
		String updatedJson = this.jointjs.save().get()
				.replace(oldUri.toString(), newUri.toString());
		this.jointjs.load(updatedJson).get();
		this.refresh().get();
	}

	/**
	 *
	 * @param uri
	 * @param position
	 * @return
	 * @throws Exception
	 *
	 * @NonUIThread
	 */
	public static String createRelationStatement(URI uri) {
		IRelation relation = CODE_SERVICE.getRelation(uri);
		if (relation == null) {
			return "";
		}

		return JointJS.createLinkStatement(uri.toString(), relation.getFrom()
				.toString(), relation.getTo().toString());
	}

	public void createRelations(List<URI> relations) {
		StringBuilder js = new StringBuilder();
		for (URI relation : relations) {
			js.append(createRelationStatement(relation));
		}
		ExecUtils.logException(this.jointjs.run(js.toString()));
	}

	/**
	 * Creates in and outgoing "is a" permanent relations for the given
	 * {@link URI} without touching any existing relations.
	 *
	 * @param uri
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 *
	 * @NonUIThread
	 */
	private String createIsARelationsStatement(final URI uri,
			List<URI> existingElements) throws InterruptedException,
			ExecutionException {
		StringBuilder js = new StringBuilder();

		ICode code = LocatorService.INSTANCE.resolve(uri, ICode.class, null)
				.get();
		if (code == null) {
			LOGGER.warn(uri + " is no valid code");
			return js.toString();
		}
		ICode parent = code;
		while (true) {
			parent = CODE_SERVICE.getParent(parent);
			if (parent == null) {
				break;
			}
			if (existingElements.contains(parent.getUri())) {
				js.append(AxialCodingComposite.this.createIsARelationStatement(
						parent.getUri(), code.getUri()));
			}
		}
		for (ICode child : CODE_SERVICE.getDescendents(code)) {
			if (existingElements.contains(child.getUri())) {
				js.append(AxialCodingComposite.this.createIsARelationStatement(
						code.getUri(), child.getUri()));
			}
		}

		return js.toString();
	}

	/**
	 * Creates a "is a" permanent relation between the given parent and child
	 * element.
	 *
	 * @param parent
	 * @param child
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 *
	 * @NonUIThread
	 */
	private String createIsARelationStatement(final URI parent, final URI child)
			throws InterruptedException, ExecutionException {
		LOGGER.info("Creating Is-A-Relation from "
				+ LocatorService.INSTANCE.resolve(child, ICode.class, null)
						.get().getCaption()
				+ " to "
				+ LocatorService.INSTANCE.resolve(parent, ICode.class, null)
						.get().getCaption());

		StringBuilder js = new StringBuilder();
		String id = parent.toString() + "|" + child.toString();

		js.append(JointJS.createPermanentLinkStatement(id, parent.toString(),
				child.toString()));

		String[] texts = new String[] { "is a" };
		for (int i = 0; texts != null && i < texts.length; i++) {
			js.append(JointJS.setTextStatement(id, i, texts[i]));
		}
		return js.toString();
	}

	/**
	 * Removes all incoming and outgoing "is a" permanent relations of the
	 * specified {@link URI}.
	 *
	 * @return
	 *
	 * @NonUIThread
	 */
	private Future<Void> deleteIsARelations(final URI uri) {
		return ExecUtils.nonUIAsyncExec((Callable<Void>) () -> {
			List<String> relationIds = AxialCodingComposite.this.jointjs
					.getConnectedPermanentLinks(uri.toString()).get();
			for (String relationId : relationIds) {
				AxialCodingComposite.this.jointjs.remove(relationId).get();
			}
			return null;
		});
	}

	/**
	 * Removes all outdated "is a" permanent relations from the graph.
	 *
	 * @return
	 * @throws ExecutionException
	 * @throws InterruptedException
	 *
	 * @NonUIThread
	 */
	private String deleteAllOutdatedIsARelations() throws InterruptedException,
			ExecutionException {
		StringBuilder js = new StringBuilder();
		for (String relationId : this.jointjs.getPermanentLinks().get()) {
			URI parentURI = new URI(relationId.split("\\|")[0]);
			URI subURI = new URI(relationId.split("\\|")[1]);

			ICode parentCode = LocatorService.INSTANCE.resolve(parentURI,
					ICode.class, null).get();
			ICode subCode = LocatorService.INSTANCE.resolve(subURI,
					ICode.class, null).get();
			if (parentCode != null
					&& subCode != null
					&& !CODE_SERVICE.getDescendents(parentCode).contains(
							subCode)) {
				js.append(this.jointjs.remove(relationId).get());
			}
		}
		return js.toString();
	}

	public Future<Void> refresh() {
		return ExecUtils.nonUIAsyncExec((Callable<Void>) () -> {
			List<URI> element = AxialCodingComposite.this.getElements().get();
			StringBuilder js = new StringBuilder();
			Pair<String, List<URI>> validElements = this
					.createRefreshElementsStatement();
			js.append(validElements.getFirst());

			// is-a relations
				js.append(AxialCodingComposite.this
						.deleteAllOutdatedIsARelations());
				for (URI uri : validElements.getSecond()) {
					js.append(AxialCodingComposite.this
							.createIsARelationsStatement(uri, element));
				}

				// proposed relations
				this.jointjs
						.run(this
								.createRefreshProposedRelationsStatement(validElements
										.getSecond())).get();

				js.append(AxialCodingComposite.this.refreshRelationsStatements(
						this.getRelations().get()).getFirst());
				ExecUtils.logException(this.jointjs.run(js.toString()));
				return null;
			});
	}

	/**
	 * Creates a statement that create all {@link ProposedRelation}s that should
	 * be present in the ACM and that deletes all the ones that don't exist
	 * anymore.
	 *
	 * @param validElements
	 * @param relations
	 *            to be considered existent in the ACM
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private String createRefreshProposedRelationsStatement(
			List<URI> validElements) throws InterruptedException,
			ExecutionException {
		StringBuilder proposedRelationsJs = new StringBuilder();
		List<URI> proposedRelations = CODE_SERVICE
				.getProposedRelation(validElements, validElements).stream()
				.map(r -> r.getUri()).collect(Collectors.toList());
		List<URI> existingProposedRelations = this.getRelations("proposed")
				.get();
		for (URI proposedRelation : proposedRelations) {
			if (!existingProposedRelations.contains(proposedRelation)) {
				proposedRelationsJs.append(AxialCodingComposite
						.createRelationStatement(proposedRelation));
			}
		}
		for (URI existingProposedRelation : existingProposedRelations) {
			if (!proposedRelations.contains(existingProposedRelation)) {
				proposedRelationsJs.append(JointJS
						.removeStatement(existingProposedRelation.toString()));
			}
		}
		return proposedRelationsJs.toString();
	}

	/**
	 * Updates non-structural information all existing elements based on the
	 * internal information and returns the new size.
	 * <p>
	 * Updated information are:
	 * <ul>
	 * <li>title</li>
	 * <li>content</li>
	 * <li>colors</li>
	 * <li>size</li>
	 * </ul>
	 *
	 * @return
	 * @throws Exception
	 *
	 * @NonUIThread
	 */
	public Pair<String, List<URI>> createRefreshElementsStatement()
			throws Exception {
		return this.createRefreshElementsStatement(this.getElements().get());
	}

	/**
	 * Creates a JS script that updates non-structural information of the given
	 * {@link URI}s based on the internal information.
	 * <p>
	 * Updated information are:
	 * <ul>
	 * <li>title</li>
	 * <li>content</li>
	 * <li>colors</li>
	 * <li>size</li>
	 * </ul>
	 *
	 * @param uri
	 *
	 * @NonUIThread
	 */
	private Pair<String, List<URI>> createRefreshElementsStatement(
			final List<URI> uris) {
		try {
			StringBuilder js = new StringBuilder();
			URI origin = this.getOrigin();
			Set<URI> originCells = this.getOriginCells();

			List<URI> validUris = new ArrayList<>();
			for (URI uri : uris) {
				Pair<String, Boolean> rs = AxialCodingComposite
						.createRefreshElementStatement(uri, origin,
								originCells, this.labelProvider);
				js.append(rs.getFirst());
				if (rs.getSecond()) {
					validUris.add(uri);
				}
			}
			return new Pair<>(js.toString(), validUris);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates a JS script that updates the non-structural information of the
	 * given {@link URI}.
	 *
	 * @param uri
	 *            in question
	 * @param origin
	 *            the {@link URI} this ACM is based on
	 * @param originCells
	 *            the {@link URI}s to be formated is if directly connected to
	 *            the origin.
	 * @param labelProvider
	 * @return
	 * @throws Exception
	 */
	private static Pair<String, Boolean> createRefreshElementStatement(URI uri,
			URI origin, Set<URI> originCells,
			AxialCodingLabelProvider labelProvider) throws Exception {
		boolean isValid = true;
		StringBuilder js = new StringBuilder();

		if (LocatorService.INSTANCE.resolve(uri, null).get() == null) {
			js.append(JointJS.addCustomClassStatement(
					Arrays.asList(uri.toString()), "invalid"));
			isValid = false;
		} else {
			js.append(JointJS.removeCustomClassStatement(
					Arrays.asList(uri.toString()), "invalid"));
		}

		List<Pair<Image, String>> memos = new ArrayList<>();
		if (CODE_SERVICE.isMemo(uri)) {
			memos.add(new Pair<>(ImageManager.MEMO, CODE_SERVICE
					.loadMemoPlain(uri)));
		}

		if (origin != null) {
			if (LocatorService.INSTANCE.getType(origin) == IRelationInstance.class) {
				origin = LocatorService.INSTANCE
						.resolve(origin, IRelationInstance.class, null).get()
						.getPhenomenon();
			}
			List<ICodeInstance> codeInstances = CODE_SERVICE
					.getAllInstances(origin).stream()
					.filter(i -> i.getCode().getUri().equals(uri))
					.collect(Collectors.toList());
			for (ICodeInstance codeInstance : codeInstances) {
				if (CODE_SERVICE.isMemo(codeInstance.getUri())) {
					memos.add(new Pair<>(LABEL_PROVIDER_SERVICE
							.getImage(codeInstance.getUri()), CODE_SERVICE
							.loadMemoPlain(codeInstance.getUri())));
				}
			}
		}

		StringBuilder memo = new StringBuilder();
		if (memos.size() > 0) {
			for (Pair<Image, String> m : memos) {
				memo.append("<div class=\"memo\">");
				memo.append("<img src=\""
						+ ImageUtils.createUriFromImage(m.getFirst()) + "\">");
				memo.append(StringUtils
						.shorten(m.getSecond().replace("\n", "")));
				memo.append("</div>");
			}
		}

		js.append(JointJS.setElementTitleStatement(uri.toString(),
				labelProvider.getText(uri)
						+ (memo != null ? "<div class=\"details\">" + memo
								+ "</div>" : "")));
		js.append(JointJS.setElementContentStatement(uri.toString(),
				labelProvider.getContent(uri)));
		js.append(JointJS.setColorStatement(uri.toString(),
				labelProvider.getColor(uri)));
		js.append(JointJS.setBackgroundColorStatement(uri.toString(),
				labelProvider.getBackgroundColor(uri)));
		js.append(JointJS.setBorderColorStatement(uri.toString(),
				labelProvider.getBorderColor(uri)));

		Point size = labelProvider.getSize(uri);
		if (size != null) {
			if (originCells.contains(uri)) {
				js.append(JointJS.addCustomClassStatement(
						Arrays.asList(uri.toString()), "origin"));
				size.x += 100;
				size.y += 5;
			} else {
				js.append(JointJS.removeCustomClassStatement(
						Arrays.asList(uri.toString()), "origin"));
			}
			js.append(JointJS.setSizeStatement(uri.toString(), size.x, size.y));
		}

		return new Pair<String, Boolean>(js.toString(), isValid);
	}

	/**
	 * Update all given {@link IRelation}s in the loaded
	 * {@link IAxialCodingModel}.
	 *
	 * @param from
	 * @param to
	 * @return the valid {@link IRelation}s
	 *
	 * @throws InterruptedException
	 * @throws ExecutionException
	 *
	 * @NonUIThread
	 */
	public Pair<String, List<URI>> refreshRelationsStatements(
			List<URI> relations) throws Exception {
		StringBuilder js = new StringBuilder();
		List<URI> validRelations = new ArrayList<>();
		Set<URI> originCells = this.getOriginCells();
		for (URI relation : relations) {
			Pair<String, Boolean> rs = this.refreshRelationStatement(relation,
					relations, originCells);
			js.append(rs.getFirst());
			if (rs.getSecond()) {
				validRelations.add(relation);
			}
		}
		return new Pair<String, List<URI>>(js.toString(), validRelations);
	}

	/**
	 * Update the given {@link IRelation} in the loaded
	 * {@link IAxialCodingModel}.
	 *
	 * @param from
	 * @param to
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 *
	 * @NonUIThread
	 */
	public Pair<String, Boolean> refreshRelationStatement(URI uri,
			List<URI> relations, Set<URI> originCells) {
		try {
			IRelation relation = CODE_SERVICE.getRelation(uri);
			if (relation == null) {
				return new Pair<String, Boolean>(
						JointJS.addCustomClassStatement(
								Arrays.asList(uri.toString()), "invalid"),
						false);
			}

			StringBuilder js = new StringBuilder();

			js.append(JointJS.removeCustomClassStatement(
					Arrays.asList(uri.toString()), "invalid"));
			if (originCells.contains(uri)) {
				js.append(JointJS.addCustomClassStatement(
						Arrays.asList(uri.toString()), "origin"));
			} else {
				js.append(JointJS.removeCustomClassStatement(
						Arrays.asList(uri.toString()), "origin"));
			}
			if (relation instanceof ImplicitRelation) {
				js.append(JointJS.addCustomClassStatement(
						Arrays.asList(uri.toString()), "implicit"));
			} else {
				js.append(JointJS.removeCustomClassStatement(
						Arrays.asList(uri.toString()), "implicit"));
			}
			if (relation instanceof ProposedRelation) {
				js.append(JointJS.addCustomClassStatement(
						Arrays.asList(uri.toString()), "proposed"));
			} else {
				js.append(JointJS.removeCustomClassStatement(
						Arrays.asList(uri.toString()), "proposed"));
			}

			int groundingAll = CODE_SERVICE.getAllRelationInstances(relation)
					.size();
			int groundingImmediate = CODE_SERVICE.getExplicitRelationInstances(
					relation).size();

			StringBuffer caption = new StringBuffer();
			caption.append(relation.getName());
			caption.append("\\n");
			caption.append(groundingAll + " (" + groundingImmediate + ")");

			List<Pair<Image, String>> memos = new ArrayList<>();
			if (CODE_SERVICE.isMemo(uri)) {
				memos.add(new Pair<>(ImageManager.MEMO, CODE_SERVICE
						.loadMemoPlain(uri)));
			}
			URI origin = this.getOrigin();
			if (origin != null) {
				if (LocatorService.INSTANCE.getType(origin) == IRelationInstance.class) {
					origin = LocatorService.INSTANCE
							.resolve(origin, IRelationInstance.class, null)
							.get().getPhenomenon();
				}
				List<IRelationInstance> relationInstances = CODE_SERVICE
						.getAllRelationInstances(origin).stream()
						.filter(i -> i.getRelation().getUri().equals(uri))
						.collect(Collectors.toList());
				for (IRelationInstance relationInstance : relationInstances) {
					if (CODE_SERVICE.isMemo(relationInstance.getUri())) {
						memos.add(new Pair<>(LABEL_PROVIDER_SERVICE
								.getImage(relationInstance.getUri()),
								CODE_SERVICE.loadMemoPlain(relationInstance
										.getUri())));
					}
				}
			}

			String memo = null;
			if (memos.size() > 0) {
				memo = "";
				for (Pair<Image, String> m : memos) {
					memo += ""
							+ StringUtils.shorten(
									m.getSecond().replace("\n", ""), 15) + "";
				}
			}

			if (memo != null) {
				caption.append("\\n");
				caption.append(memo);
			}

			String[] texts = new String[] { caption.toString() };
			for (int i = 0; texts != null && i < texts.length; i++) {
				js.append(JointJS.setTextStatement(uri.toString(), i, texts[i]));
			}
			return new Pair<String, Boolean>(js.toString(), true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean setFocus() {
		return this.jointjs.setFocus();
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		if (this.jointjs != null && !this.jointjs.isDisposed()) {
			this.jointjs.addSelectionChangedListener(listener);
		}
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		if (this.jointjs != null && !this.jointjs.isDisposed()) {
			this.jointjs.removeSelectionChangedListener(listener);
		}
	}

	@Override
	public ISelection getSelection() {
		if (this.jointjs != null && !this.jointjs.isDisposed()) {
			return this.jointjs.getSelection();
		}
		return new StructuredSelection();
	}

	@Override
	public void setSelection(ISelection selection) {
		if (this.jointjs != null && !this.jointjs.isDisposed()) {
			this.jointjs.setSelection(selection);
		}
	}

	public JointJS getJointjs() {
		return this.jointjs;
	}

	/**
	 * Removes the given {@link ICode}s from the currently loaded
	 * {@link IAxialCodingModel}.
	 *
	 * @param uris
	 */
	public void remove(List<URI> uris) {
		StringBuilder js = new StringBuilder();
		for (URI uri : uris) {
			js.append(JointJS.removeStatement(uri.toString()));
		}
		ExecUtils.logException(this.jointjs.run(js.toString()));
	}

	public void autoLayout() {
		this.jointjs.autoLayout();
	}

	public void zoomOut() {
		this.jointjs.zoomOut();
	}

	public void zoomIn() {
		this.jointjs.zoomIn();
	}

	public void fitOnScreen() {
		this.jointjs.fitOnScreen();
	}

	@Override
	public void addModifyListener(ModifyListener modifyListener) {
		this.modifyListeners.add(modifyListener);
	}

	@Override
	public void removeModifyListener(ModifyListener modifyListener) {
		this.modifyListeners.remove(modifyListener);
	}

	public void setShowMemos(boolean showMemos) {
		if (showMemos) {
			this.jointjs
					.run("document.getElementsByTagName(\"html\")[0].classList.remove(\"detailsHidden\");");
		} else {
			this.jointjs
					.run("document.getElementsByTagName(\"html\")[0].classList.add(\"detailsHidden\");");
		}
	}

}
