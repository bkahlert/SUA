package de.fu_berlin.imp.apiua.groundedtheory.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.SWTUtils;
import com.bkahlert.nebula.utils.SelectionProviderDelegator;
import com.bkahlert.nebula.utils.selection.SelectionUtils;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.groundedtheory.AxialCodingModelLocatorProvider;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.IAxialCodingModel;
import de.fu_berlin.imp.apiua.groundedtheory.model.JointJSAxialCodingModel;
import de.fu_berlin.imp.apiua.groundedtheory.preferences.SUAGTPreferenceUtil;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceAdapter;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreReadException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreWriteException;
import de.fu_berlin.imp.apiua.groundedtheory.views.AxialCodingViewModelList.IListener;

public class AxialCodingView extends ViewPart {

	static final Logger LOGGER = Logger.getLogger(AxialCodingView.class);

	public static final String ID = "de.fu_berlin.imp.apiua.groundedtheory.views.AxialCodingView";

	private static final ILabelProviderService LABEL_PROVIDER_SERVICE = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);

	private static final ICodeService CODE_SERVICE = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	private final ICodeServiceListener codeServiceListener = new CodeServiceAdapter() {
	};

	private final ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (part == AxialCodingView.this) {
				return;
			}

			List<URI> uris = SelectionUtils.getAdaptableObjects(selection,
					URI.class);
			AxialCodingView.this.highlight(uris);
		}
	};

	private final SelectionProviderDelegator selectionProviderDelegator;
	private SashForm axialCodingCompositesContainer;
	private AxialCodingComposite activeAxialCodingComposite = null;

	public AxialCodingView() {
		this.selectionProviderDelegator = new SelectionProviderDelegator();
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		CODE_SERVICE.addCodeServiceListener(this.codeServiceListener);
		this.getSite().setSelectionProvider(this.selectionProviderDelegator);
		SelectionUtils.getSelectionService(this.getSite().getWorkbenchWindow())
				.addPostSelectionListener(this.selectionListener);
	}

	@Override
	public void dispose() {
		SelectionUtils.getSelectionService(this.getSite().getWorkbenchWindow())
				.removePostSelectionListener(this.selectionListener);
		CODE_SERVICE.removeCodeServiceListener(this.codeServiceListener);
		super.dispose();
	}

	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0)
				.create());

		final AxialCodingViewModelList modelList = new AxialCodingViewModelList(
				parent, SWT.NONE);
		modelList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		modelList.addListener(new IListener() {

			@Override
			public void createClicked() {
				try {
					final URI uri = AxialCodingModelLocatorProvider
							.createUniqueAxialCodingModelURI();
					CODE_SERVICE
							.addAxialCodingModel(new JointJSAxialCodingModel(
									uri,
									"{\"cells\":[],\"title\":\"New Model\"}"));

					ExecUtils.logException(AxialCodingView.this.open(uri));
					modelList.setOpened(new HashSet<URI>(Arrays.asList(uri)));
				} catch (CodeStoreWriteException e) {
					AxialCodingView.LOGGER.error("Error creating new "
							+ IAxialCodingModel.class.getSimpleName(), e);
				}
			}

			@Override
			public void copyClicked(URI uri) {
				try {
					final URI copy = AxialCodingModelLocatorProvider
							.createUniqueAxialCodingModelURI();
					JointJSAxialCodingModel newAcm = new JointJSAxialCodingModel(
							copy, CODE_SERVICE.getAxialCodingModel(uri)
									.serialize());
					newAcm.setTitle(newAcm.getTitle() + " - Copy");
					CODE_SERVICE.addAxialCodingModel(newAcm);
					ExecUtils.logException(AxialCodingView.this.open(uri));
					modelList.setOpened(new HashSet<URI>(Arrays.asList(uri)));
				} catch (CodeStoreWriteException | CodeStoreReadException e) {
					AxialCodingView.LOGGER.error("Error copying "
							+ IAxialCodingModel.class.getSimpleName() + " "
							+ uri, e);
				}
			}

			@Override
			public void renameClicked(URI uri) {
				try {
					AxialCodingViewRenameDialog renameDialog = new AxialCodingViewRenameDialog(
							parent.getShell(), LABEL_PROVIDER_SERVICE
									.getLabelProvider(uri).getText(uri));
					renameDialog.create();
					if (renameDialog.open() == Window.OK) {
						if (AxialCodingView.this.getOpenedURIs().keySet()
								.contains(uri)) {
							AxialCodingView.this.getOpenedURIs().get(uri)
									.setTitle(renameDialog.getTitle());
						} else {
							IAxialCodingModel axialCodingModel = CODE_SERVICE
									.getAxialCodingModel(uri);
							if (axialCodingModel != null) {
								Map<String, Object> map = new HashMap<>();
								map.put("title", renameDialog.getTitle());
								axialCodingModel = axialCodingModel
										.createCopy(map);
								LocatorService.INSTANCE.uncache(uri);
							}
							CODE_SERVICE.addAxialCodingModel(axialCodingModel);
						}
					}
				} catch (Exception e) {
					AxialCodingView.LOGGER.error("Error renaming " + uri, e);
				}
			}

			@Override
			public void openClicked(Set<URI> uris) {
				ExecUtils.logException(AxialCodingView.this.open(uris
						.toArray(new URI[0])));
			}

			@Override
			public void deleteClicked(URI uri) {
				try {
					int pos = CODE_SERVICE.getAxialCodingModels().indexOf(uri);
					CODE_SERVICE.removeAxialCodingModel(uri);
					Set<URI> stillOpen = new HashSet<URI>(AxialCodingView.this
							.getOpenedURIs().keySet());
					stillOpen.remove(uri);
					if (stillOpen.isEmpty()) {
						List<URI> uris = CODE_SERVICE.getAxialCodingModels();
						if (uris.size() > 0) {
							pos = Math.max(0, pos - 1);
							stillOpen.add(uris.get(pos));
						}
					}
					ExecUtils.logException(AxialCodingView.this.open(stillOpen
							.toArray(new URI[0])));
					modelList.setOpened(stillOpen);
				} catch (CodeStoreWriteException e) {
					AxialCodingView.LOGGER.error("Error removing "
							+ IAxialCodingModel.class.getSimpleName() + " "
							+ uri);
				} catch (CodeStoreReadException e) {
					AxialCodingView.LOGGER.error("Error reading "
							+ IAxialCodingModel.class.getSimpleName() + "s");
				}
			}
		});

		this.axialCodingCompositesContainer = new SashForm(parent,
				SWT.HORIZONTAL);
		this.axialCodingCompositesContainer.setLayoutData(new GridData(
				SWT.FILL, SWT.FILL, true, true));

		Set<URI> lastOpenedModels = new HashSet<URI>(
				new SUAGTPreferenceUtil().getLastOpenedAxialCodingModels());
		if (lastOpenedModels.size() > 0) {
			modelList.setOpened(lastOpenedModels);
			ExecUtils.logException(AxialCodingView.this.open(lastOpenedModels
					.toArray(new URI[0])));
		}
	}

	private List<AxialCodingComposite> getAxialCodingComposites() {
		List<AxialCodingComposite> axialCodingComposites = new ArrayList<AxialCodingComposite>();
		if (!this.axialCodingCompositesContainer.isDisposed()) {
			for (Control control : this.axialCodingCompositesContainer
					.getChildren()) {
				if (control instanceof AxialCodingComposite) {
					axialCodingComposites.add((AxialCodingComposite) control);
				}
			}
		}
		return axialCodingComposites;
	}

	private void activateMenu() {
		MenuManager menuManager = new MenuManager("#PopupMenu");
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new Separator(
						IWorkbenchActionConstants.MB_ADDITIONS));
			}
		});
		for (AxialCodingComposite axialCodingComposite : this
				.getAxialCodingComposites()) {
			Menu menu = menuManager.createContextMenu(axialCodingComposite);
			this.getSite().registerContextMenu(menuManager,
					axialCodingComposite);
			axialCodingComposite.setMenu(menu);
		}
	}

	private void trackActiveAxialCodingComposite() {
		List<AxialCodingComposite> axialCodingComposites = this
				.getAxialCodingComposites();
		for (final AxialCodingComposite axialCodingComposite : axialCodingComposites) {
			axialCodingComposite.getJointjs().addFocusListener(
					new FocusAdapter() {
						@Override
						public void focusGained(FocusEvent e) {
							AxialCodingView.this
									.activateAxialCodingComposite(axialCodingComposite);
						}
					});
		}
		this.activateAxialCodingComposite(axialCodingComposites.size() > 0 ? axialCodingComposites
				.get(0) : null);
	}

	private void activateAxialCodingComposite(
			AxialCodingComposite activeAxialCodingComposite) {
		if (this.activeAxialCodingComposite == activeAxialCodingComposite) {
			return;
		}

		AxialCodingView.this.activeAxialCodingComposite = activeAxialCodingComposite;
		AxialCodingView.this.selectionProviderDelegator
				.setSelectionProvider(activeAxialCodingComposite);
	}

	public Future<Void> open(final URI... uris) {
		final Future<List<AxialCodingComposite>> ui = ExecUtils
				.asyncExec(new Callable<List<AxialCodingComposite>>() {
					@Override
					public List<AxialCodingComposite> call() throws Exception {
						AxialCodingView.this.disposeAll();
						for (int i = 0; i < uris.length; i++) {
							final AxialCodingComposite axialCodingComposite = new AxialCodingComposite(
									AxialCodingView.this.axialCodingCompositesContainer,
									SWT.NONE);
							axialCodingComposite
									.addModifyListener(new ModifyListener() {
										@Override
										public void modifyText(ModifyEvent e) {
											axialCodingComposite.save();
										}
									});
							axialCodingComposite.setBackground(Display
									.getCurrent().getSystemColor(
											SWT.COLOR_LIST_BACKGROUND));
							axialCodingComposite.setLayoutData(new GridData(
									SWT.FILL, SWT.FILL, true, true));
						}
						AxialCodingView.this.axialCodingCompositesContainer
								.setWeights(SWTUtils
										.getEvenWeights(uris.length));
						AxialCodingView.this.axialCodingCompositesContainer
								.layout(true);
						AxialCodingView.this.activateMenu();
						AxialCodingView.this.trackActiveAxialCodingComposite();
						return AxialCodingView.this.getAxialCodingComposites();
					}
				});

		return ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				List<AxialCodingComposite> axialCodingComposites = ui.get();
				for (int i = 0; i < uris.length; i++) {
					axialCodingComposites.get(i).open(uris[i]).get();
				}
				return null;
			}
		});
	}

	/**
	 * Disposes the all opened {@link AxialCodingComposite}s without saving.
	 *
	 * @UIThread
	 */
	private void disposeAll() {
		for (final AxialCodingComposite axialCodingComposite : this
				.getAxialCodingComposites()) {
			axialCodingComposite.dispose();
		}
	}

	public Map<URI, AxialCodingComposite> getOpenedURIs() {
		List<AxialCodingComposite> axialCodingComposites = this
				.getAxialCodingComposites();
		Map<URI, AxialCodingComposite> uris = new HashMap<URI, AxialCodingComposite>(
				axialCodingComposites.size());
		for (final AxialCodingComposite axialCodingComposite : axialCodingComposites) {
			uris.put(axialCodingComposite.getOpenedURI(), axialCodingComposite);
		}
		return uris;
	}

	public Future<Void> highlight(final List<URI> uris) {
		return ExecUtils.asyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				for (final AxialCodingComposite axialCodingComposite : AxialCodingView.this
						.getAxialCodingComposites()) {
					axialCodingComposite.highlight(uris);
				}
				return null;
			}
		});
	}

	@Override
	public void setFocus() {
		this.axialCodingCompositesContainer.setFocus();
	}

	/**
	 * Removes the given {@link URI}s from the currently loaded
	 * {@link IAxialCodingModel}s.
	 *
	 * @param uris
	 */
	public void remove(List<URI> uris) {
		for (final AxialCodingComposite axialCodingComposite : this
				.getAxialCodingComposites()) {
			axialCodingComposite.remove(uris);
		}
	}

	public void autoLayoutFocussedACM() {
		if (this.activeAxialCodingComposite != null
				&& !this.activeAxialCodingComposite.isDisposed()) {
			this.activeAxialCodingComposite.autoLayout();
		}
	}

	public void zoomOutFocussedACM() {
		if (this.activeAxialCodingComposite != null
				&& !this.activeAxialCodingComposite.isDisposed()) {
			this.activeAxialCodingComposite.zoomOut();
		}
	}

	public void zoomInFocussedACM() {
		if (this.activeAxialCodingComposite != null
				&& !this.activeAxialCodingComposite.isDisposed()) {
			this.activeAxialCodingComposite.zoomIn();
		}
	}

	public void fitOnScreenFocussedACM() {
		if (this.activeAxialCodingComposite != null
				&& !this.activeAxialCodingComposite.isDisposed()) {
			this.activeAxialCodingComposite.fitOnScreen();
		}
	}

}
