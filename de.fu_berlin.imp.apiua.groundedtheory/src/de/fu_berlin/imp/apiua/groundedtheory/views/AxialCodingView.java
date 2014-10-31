package de.fu_berlin.imp.apiua.groundedtheory.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
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
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.SWTUtils;
import com.bkahlert.nebula.utils.SelectionProviderDelegator;
import com.bkahlert.nebula.utils.selection.SelectionUtils;
import com.bkahlert.nebula.widgets.itemlist.ItemList;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.model.IAxialCodingModel;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.preferences.SUAGTPreferenceUtil;

public class AxialCodingView extends ViewPart {

	static final Logger LOGGER = Logger.getLogger(AxialCodingView.class);

	public static final String ID = "de.fu_berlin.imp.apiua.groundedtheory.views.AxialCodingView";

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
		this.getSite().setSelectionProvider(this.selectionProviderDelegator);
		SelectionUtils.getSelectionService(this.getSite().getWorkbenchWindow())
				.addPostSelectionListener(this.selectionListener);
	}

	@Override
	public void dispose() {
		SelectionUtils.getSelectionService(this.getSite().getWorkbenchWindow())
				.removePostSelectionListener(this.selectionListener);
		this.saveAll();
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0)
				.create());
		ItemList modelList = new AxialCodingViewModelList(parent, SWT.NONE);
		modelList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		this.axialCodingCompositesContainer = new SashForm(parent,
				SWT.HORIZONTAL);
		this.axialCodingCompositesContainer.setLayoutData(new GridData(
				SWT.FILL, SWT.FILL, true, true));

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
						AxialCodingView.this.saveAll();
						AxialCodingView.this.disposeAll();
						for (int i = 0; i < uris.length; i++) {
							final AxialCodingComposite axialCodingComposite = new AxialCodingComposite(
									AxialCodingView.this.axialCodingCompositesContainer,
									SWT.NONE);
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
	 * Saves the currently opened {@link URI}s. The {@link Control}s's content
	 * is immediately saved, so it is save to call this method while disposition
	 * takes place. The axial coding models are written on the disk not until
	 * {@link Future#isDone()} returns true.
	 * 
	 * @UIThread
	 * 
	 * @return
	 */
	public Future<Void> saveAll() {
		final Map<URI, Future<Void>> success = new HashMap<URI, Future<Void>>();
		for (final AxialCodingComposite axialCodingComposite : this
				.getAxialCodingComposites()) {
			success.put(axialCodingComposite.getOpenedURI(),
					axialCodingComposite.save());
		}
		return ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				Exception ex = null;
				for (URI uri : success.keySet()) {
					try {
						success.get(uri).get();
						LOGGER.info("Successfully saved "
								+ IAxialCodingModel.class.getSimpleName() + " "
								+ uri);
					} catch (Exception e) {
						if (ex == null) {
							ex = e;
						}
						LOGGER.error(
								"Error saving "
										+ IAxialCodingModel.class
												.getSimpleName() + " " + uri, e);
					}
				}
				if (ex != null) {
					throw ex;
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

	public List<URI> getOpenedURIs() {
		List<AxialCodingComposite> axialCodingComposites = this
				.getAxialCodingComposites();
		List<URI> uris = new ArrayList<URI>(axialCodingComposites.size());
		for (final AxialCodingComposite axialCodingComposite : axialCodingComposites) {
			uris.add(axialCodingComposite.getOpenedURI());
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
	 * Removes the given {@link ICode}s from the currently loaded
	 * {@link IAxialCodingModel}s.
	 * 
	 * @param codes
	 */
	public void remove(List<ICode> codes) {
		for (final AxialCodingComposite axialCodingComposite : this
				.getAxialCodingComposites()) {
			axialCodingComposite.remove(codes);
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

}
