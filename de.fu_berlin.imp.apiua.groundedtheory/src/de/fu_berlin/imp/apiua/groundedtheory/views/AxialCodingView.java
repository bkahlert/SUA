package de.fu_berlin.imp.apiua.groundedtheory.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.nebula.information.ISubjectInformationProvider;
import com.bkahlert.nebula.utils.CompletedFuture;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.widgets.browser.listener.IDropListener;
import com.bkahlert.nebula.widgets.browser.listener.IMouseListener;
import com.bkahlert.nebula.widgets.itemlist.ItemList;
import com.bkahlert.nebula.widgets.jointjs.JointJS;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.IImportanceService;
import de.fu_berlin.imp.apiua.core.services.IImportanceService.Importance;
import de.fu_berlin.imp.apiua.core.services.IImportanceServiceListener;
import de.fu_berlin.imp.apiua.core.services.IUriPresenterService;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.IAxialCodingModel;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.JointJSAxialCodingModel;
import de.fu_berlin.imp.apiua.groundedtheory.preferences.SUAGTPreferenceUtil;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceAdapter;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreReadException;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.AxialCodingLabelProvider;

public class AxialCodingView extends ViewPart {

	static final Logger LOGGER = Logger.getLogger(AxialCodingView.class);

	public static final String ID = "de.fu_berlin.imp.apiua.groundedtheory.views.AxialCodingView";

	private static final IImportanceService IMPORTANCE_SERVICE = (IImportanceService) PlatformUI
			.getWorkbench().getService(IImportanceService.class);
	private static final ICodeService CODE_SERVICE = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);
	private static final IUriPresenterService PRESENTER_SERVICE = (IUriPresenterService) PlatformUI
			.getWorkbench().getService(IUriPresenterService.class);

	private final IImportanceServiceListener importanceServiceListener = new IImportanceServiceListener() {
		@Override
		public void importanceChanged(Set<URI> uris, Importance importance) {
			for (URI uri : uris) {
				try {
					AxialCodingView.this.update(uri);
				} catch (Exception e) {
					LOGGER.error("Error refreshing importance of " + uri, e);
				}
			}
		}
	};

	private final ICodeServiceListener codeServiceListener = new CodeServiceAdapter() {

		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
			try {
				AxialCodingView.this.update(code.getUri());
			} catch (Exception e) {
				LOGGER.error("Error refreshing " + code.getUri() + " in "
						+ AxialCodingView.class, e);
			}
		}

		@Override
		public void codeRecolored(ICode code, RGB oldColor, RGB newColor) {
			try {
				AxialCodingView.this.update(code.getUri());
			} catch (Exception e) {
				LOGGER.error("Error refreshing " + code.getUri() + " in "
						+ AxialCodingView.class, e);
			}
		}

		@Override
		public void codeMoved(final ICode code, ICode oldParentCode,
				ICode newParentCode) {
			try {
				ExecUtils.nonUIAsyncExec(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						AxialCodingView.this.deleteIsALinks(code.getUri())
								.get();
						AxialCodingView.this.createIsALinks(code.getUri())
								.get();
						return null;
					}
				});
			} catch (Exception e) {
				LOGGER.error("Error refreshing " + code.getUri() + " in "
						+ AxialCodingView.class, e);
			}
		}

		@Override
		public void codeDeleted(ICode code) {
			try {
				AxialCodingView.this.jointjs.remove(code.getUri().toString());
			} catch (Exception e) {
				LOGGER.error("Error refreshing " + code.getUri() + " in "
						+ AxialCodingView.class, e);
			}
		}

		@Override
		public void axialCodingModelUpdated(URI uri) {
			try {
				IAxialCodingModel axialCodingModel = CODE_SERVICE
						.getAxialCodingModel(uri);
				if (AxialCodingView.this.openedUri != null
						&& AxialCodingView.this.openedUri.equals(uri)) {
					AxialCodingView.this.jointjs.setTitle(axialCodingModel
							.getTitle());
				}
			} catch (CodeStoreReadException e) {
				LOGGER.error("Error setting title of " + uri, e);
			}
		}

		@Override
		public void axialCodingModelRemoved(final URI uri) {
			if (AxialCodingView.this.openedUri != null
					&& AxialCodingView.this.openedUri.equals(uri)) {
				AxialCodingView.this.openedUri = null;
				final Future<Void> success = AxialCodingView.this.open(null);
				ExecUtils.nonUIAsyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							success.get();
						} catch (Exception e) {
							LOGGER.error("Error removing "
									+ IAxialCodingModel.class.getSimpleName()
									+ " " + uri);
						}
					}
				});
			}
		};
	};

	private JointJS jointjs = null;
	private final AxialCodingLabelProvider labelProvider = new AxialCodingLabelProvider();

	private URI openedUri = null;

	public AxialCodingView() {
		IMPORTANCE_SERVICE
				.addImportanceServiceListener(this.importanceServiceListener);
		CODE_SERVICE.addCodeServiceListener(this.codeServiceListener);
	}

	@Override
	public void dispose() {
		CODE_SERVICE.removeCodeServiceListener(this.codeServiceListener);
		IMPORTANCE_SERVICE
				.removeImportanceServiceListener(this.importanceServiceListener);
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0)
				.create());
		ItemList modelList = new AxialCodingViewModelList(parent, SWT.NONE);
		modelList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		this.jointjs = new JointJS(parent, SWT.BORDER, "apiua://code/",
				"apiua://code-link", new IConverter<String, URI>() {
					@Override
					public URI convert(String returnValue) {
						if (returnValue.contains("|")) {
							return null;
						}
						if (returnValue.startsWith("apiua://")) {
							return new URI(returnValue);
						}
						return null;
					}
				});
		this.jointjs
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.jointjs.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				final Future<Void> success = AxialCodingView.this.save();
				ExecUtils.nonUIAsyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							success.get();
							LOGGER.info("Successfully saved "
									+ IAxialCodingModel.class.getSimpleName()
									+ " " + AxialCodingView.this.openedUri);
						} catch (Exception e) {
							LOGGER.error("Error saving "
									+ IAxialCodingModel.class.getSimpleName()
									+ " " + AxialCodingView.this.openedUri, e);
						}
					}
				});
			}
		});

		this.jointjs.setEnabled(false);
		this.activateMenu();
		this.activateDropSupport();
		this.activateInformationSupport();

		List<URI> lastOpenedModels = new SUAGTPreferenceUtil()
				.getLastOpenedAxialCodingModels();
		if (lastOpenedModels.size() > 0) {
			final URI uri = lastOpenedModels.get(0);
			final Future<Void> success = this.open(uri);
			ExecUtils.nonUIAsyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						success.get();
					} catch (Exception e) {
						LOGGER.error("Error opening "
								+ IAxialCodingModel.class.getSimpleName() + " "
								+ uri);
					}
				}
			});
		}
	}

	private void activateInformationSupport() {
		PRESENTER_SERVICE.enable(this.jointjs,
				new ISubjectInformationProvider<Control, URI>() {
					private URI hovered = null;
					private boolean isMouseDown = false;

					private final JointJS.IJointJSListener jointJsListener = new JointJS.JointJSListener() {
						@Override
						public void hovered(String id, boolean hoveredIn) {
							if (hoveredIn && id != null && !id.contains("|")
									&& id.startsWith("apiua://")) {
								hovered = new URI(id);
							} else {
								hovered = null;
							}
						}
					};

					private final IMouseListener mouseListener = new IMouseListener() {
						@Override
						public void mouseMove(double x, double y) {
						}

						@Override
						public void mouseDown(double x, double y) {
							isMouseDown = true;
						}

						@Override
						public void mouseUp(double x, double y) {
							isMouseDown = false;
						}
					};

					@Override
					public void register(Control subject) {
						AxialCodingView.this.jointjs
								.addJointJSListener(this.jointJsListener);
						AxialCodingView.this.jointjs
								.addMouseListener(this.mouseListener);
						this.hovered = null;
						this.isMouseDown = false;
					}

					@Override
					public void unregister(Control subject) {
						AxialCodingView.this.jointjs
								.removeMouseListener(this.mouseListener);
						AxialCodingView.this.jointjs
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

		this.jointjs.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				PRESENTER_SERVICE.disable(AxialCodingView.this.jointjs);
			}
		});
	}

	private void activateMenu() {
		this.getSite().setSelectionProvider(this.jointjs);

		MenuManager menuManager = new MenuManager("#PopupMenu");
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new Separator(
						IWorkbenchActionConstants.MB_ADDITIONS));
			}
		});
		Menu menu = menuManager.createContextMenu(this.jointjs);
		this.getSite().registerContextMenu(menuManager, this.jointjs);
		this.jointjs.setMenu(menu);
	}

	private void activateDropSupport() {
		this.jointjs.addDropListener(new IDropListener() {
			@Override
			public void drop(final long offsetX, final long offsetY,
					final String data) {
				if (data == null || data.isEmpty()
						|| AxialCodingView.this.openedUri == null) {
					return;
				}

				ExecUtils.nonUIAsyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							Point pan = AxialCodingView.this.jointjs.getPan()
									.get();
							for (final String uriString : data.split("\\|")) {
								URI uri = new URI(uriString);
								AxialCodingView.this.createNode(uri, new Point(
										(int) offsetX - pan.x - 10,
										(int) offsetY - pan.y - 10));
							}
						} catch (Exception e) {
							LOGGER.error("Error dropping " + data, e);
						}
					}
				});

			}
		});
	}

	public JointJS getJointjs() {
		return this.jointjs;
	}

	public Future<Void> open(final URI uri) {
		return ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				AxialCodingView.this.save().get();
				if (uri == null) {
					AxialCodingView.this.jointjs
							.load("{ \"cells\": [], \"title\": \"\" }");
					AxialCodingView.this.jointjs.setEnabled(false);
					AxialCodingView.this.jointjs = null;
					new SUAGTPreferenceUtil()
							.setLastOpenedAxialCodingModels(new ArrayList<URI>());
				} else {
					try {
						IAxialCodingModel axialCodingModel = CODE_SERVICE
								.getAxialCodingModel(uri);
						if (axialCodingModel == null) {
							return AxialCodingView.this.open(null).get();
						}
						AxialCodingView.this.openedUri = uri;
						new SUAGTPreferenceUtil()
								.setLastOpenedAxialCodingModels(Arrays
										.asList(uri));
						AxialCodingView.this.jointjs.load(
								axialCodingModel.serialize()).get();
						AxialCodingView.this.syncModel();
						AxialCodingView.this.jointjs.setEnabled(true);
					} catch (CodeStoreReadException e) {
						throw new IllegalArgumentException(e);
					} catch (Exception e) {
						LOGGER.error("Error refreshing the axial coding model "
								+ uri);
					}
				}
				return null;
			}
		});
	}

	/**
	 * Saves the currently opened {@link URI}
	 * 
	 * @return
	 */
	public Future<Void> save() {
		if (this.openedUri == null) {
			return new CompletedFuture<Void>(null, null);
		}

		final URI uri = this.openedUri;
		final Future<String> json = this.jointjs.save();
		return ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				IAxialCodingModel axialCodingModel = new JointJSAxialCodingModel(
						uri, json.get());
				CODE_SERVICE.addAxialCodingModel(axialCodingModel);
				return null;
			}
		});
	}

	public Object getOpenedURI() {
		return this.openedUri;
	}

	public Future<List<URI>> getModelCodes() {
		return ExecUtils.nonUIAsyncExec(new Callable<List<URI>>() {
			@Override
			public List<URI> call() throws Exception {
				List<URI> uris = new LinkedList<URI>();
				for (String id : AxialCodingView.this.jointjs.getNodes().get()) {
					uris.add(new URI(id));
				}
				return uris;
			}
		});
	}

	/**
	 * Updates non-structural information all existing nodes based on the
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
	 * @param uri
	 * @return
	 */
	public void update() throws Exception {
		for (URI uri : this.getModelCodes().get()) {
			this.update(uri);
		}
	}

	/**
	 * Updates non-structural information of the given {@link URI} based on the
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
	 * @param uri
	 * @return
	 */
	public Point update(URI uri) throws Exception {
		this.jointjs.setNodeTitle(uri.toString(),
				this.labelProvider.getText(uri));
		this.jointjs.setNodeContent(uri.toString(),
				this.labelProvider.getContent(uri));
		this.jointjs.setColor(uri.toString(), this.labelProvider.getColor(uri));
		this.jointjs.setBackgroundColor(uri.toString(),
				this.labelProvider.getBackgroundColor(uri));
		this.jointjs.setBorderColor(uri.toString(),
				this.labelProvider.getBorderColor(uri));

		Point size = this.labelProvider.getSize(uri);
		if (size != null) {
			this.jointjs.setSize(uri.toString(), size.x, size.y);
		}
		return size;
	}

	/**
	 * Synchronized structural information with the internally saved
	 * information.
	 * <p>
	 * <ul>
	 * <li>No more existing {@link ICode} get removed.</li>
	 * <li>Permanent "is a" links are recreated.
	 * 
	 * @return
	 */
	public Future<Void> syncModel() {
		return ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				List<URI> uris = AxialCodingView.this.removeAllInvalidNodes()
						.get();
				AxialCodingView.this.deleteAllExistingIsALinks().get();
				for (URI uri : uris) {
					AxialCodingView.this.createIsALinks(uri);
				}
				return null;
			}

		});
	}

	/**
	 * Removes all nodes that symbolize a no more existing {@link ICode}.
	 * 
	 * @return
	 */
	private Future<List<URI>> removeAllInvalidNodes() {
		return ExecUtils.nonUIAsyncExec(new Callable<List<URI>>() {
			@Override
			public List<URI> call() throws Exception {
				List<URI> uris = AxialCodingView.this.getModelCodes().get();
				for (Iterator<URI> iterator = uris.iterator(); iterator
						.hasNext();) {
					URI uri = iterator.next();
					ICode code = LocatorService.INSTANCE.resolve(uri,
							ICode.class, null).get();
					if (code == null) {
						AxialCodingView.this.jointjs.remove(uri.toString())
								.get();
						iterator.remove();
					}
				}
				return uris;
			}
		});
	}

	private void createNode(URI uri, Point position) throws Exception {
		String title = this.labelProvider.getText(uri);
		String content = this.labelProvider.getContent(uri);
		Point size = this.labelProvider.getSize(uri);

		if (position == null) {
			position = new Point(10, 10);
		}

		String id = null;
		try {
			id = this.jointjs.createNode(uri.toString(), title, content,
					position, size).get();
		} catch (Exception e) {
			LOGGER.error("Error creating node " + id, e);
		}

		if (id == null) {
			LOGGER.error("ID missing for created/updated node!");
		}

		this.createIsALinks(uri);
		this.update(uri);
	}

	/**
	 * Creates in and outgoing "is a" permanent links for the given {@link URI}
	 * without touching any existing links.
	 * 
	 * @param uri
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private Future<Void> createIsALinks(final URI uri)
			throws InterruptedException, ExecutionException {
		return ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				List<URI> existingCodes = AxialCodingView.this.getModelCodes()
						.get();
				ICode code = LocatorService.INSTANCE.resolve(uri, ICode.class,
						null).get();
				ICode parent = code;
				while (true) {
					parent = CODE_SERVICE.getParent(parent);
					if (parent == null) {
						break;
					}
					AxialCodingView.this.createIsALink(parent.getUri(),
							code.getUri()).get();
				}
				for (ICode child : CODE_SERVICE.getChildren(code)) {
					if (existingCodes.contains(child.getUri())) {
						AxialCodingView.this.createIsALink(code.getUri(),
								child.getUri()).get();
					}
				}
				return null;
			}
		});
	}

	/**
	 * Creates a "is a" permanent link between the given parent and child node.
	 * 
	 * @param parent
	 * @param child
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private Future<Void> createIsALink(final URI parent, final URI child)
			throws InterruptedException, ExecutionException {
		return ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				String id = parent.toString() + "|" + child.toString();

				id = AxialCodingView.this.jointjs.createPermanentLink(id,
						parent.toString(), child.toString()).get();

				String[] texts = new String[] { "is a" };
				for (int i = 0; texts != null && i < texts.length; i++) {
					AxialCodingView.this.jointjs.setText(id, i, texts[i]);
				}
				return null;
			}
		});
	}

	/**
	 * Removes all incoming and outgoing "is a" permanent links of the specified
	 * {@link URI}.
	 * 
	 * @return
	 */
	private Future<Void> deleteIsALinks(final URI uri) {
		return ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				List<String> linkIds = AxialCodingView.this.jointjs
						.getConnectedPermanentLinks(uri.toString()).get();
				for (String linkId : linkIds) {
					AxialCodingView.this.jointjs.remove(linkId).get();
				}
				return null;
			}
		});
	}

	/**
	 * Removes all "is a" permanent links from the graph.
	 * 
	 * @return
	 */
	private Future<Void> deleteAllExistingIsALinks() {
		return ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				List<String> linkIds = AxialCodingView.this.jointjs
						.getPermanentLinks().get();
				for (String linkId : linkIds) {
					AxialCodingView.this.jointjs.remove(linkId).get();
				}
				return null;
			}
		});
	}

	@Override
	public void setFocus() {
		if (this.jointjs != null) {
			this.jointjs.setFocus();
		}
	}

}