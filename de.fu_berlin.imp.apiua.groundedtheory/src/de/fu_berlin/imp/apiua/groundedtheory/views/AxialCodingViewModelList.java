package de.fu_berlin.imp.apiua.groundedtheory.views;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ViewerUtils;
import com.bkahlert.nebula.utils.selection.ArrayUtils;
import com.bkahlert.nebula.utils.selection.SelectionUtils;
import com.bkahlert.nebula.widgets.SimpleIllustratedComposite;
import com.bkahlert.nebula.widgets.SimpleIllustratedComposite.IllustratedText;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.IAxialCodingModel;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceAdapter;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreReadException;
import de.fu_berlin.imp.apiua.groundedtheory.ui.GTLabelProvider;

class AxialCodingViewModelList extends Composite {

	private static final Logger LOGGER = Logger
			.getLogger(AxialCodingViewModelList.class);

	public static interface IListener {
		/**
		 * User chose to open the given {@link URI}.
		 *
		 * @param uris
		 */
		public void openClicked(Set<URI> uris);

		/**
		 * User chose to make a copy the given {@link URI}.
		 *
		 * @param uri
		 */
		public void copyClicked(URI uri);

		/**
		 * User chose to rename the given {@link URI}.
		 *
		 * @param uri
		 */
		public void renameClicked(URI uri);

		/**
		 * User chose to delete the given {@link URI}.
		 *
		 * @param uri
		 */
		public void deleteClicked(URI uri);

		/**
		 * User chose to create a new {@link URI}.
		 */
		public void createClicked();

		/**
		 * User chose to refresh from the models origin.
		 *
		 * @param uri
		 */
		public void refreshClicked(URI uri);

	}

	private static final ILabelProviderService LABEL_PROVIDER_SERVICE = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);

	private static final ICodeService CODE_SERVICE = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	private final ICodeServiceListener codeServiceListener = new CodeServiceAdapter() {
		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
			AxialCodingViewModelList.this.refresh();
		};

		@Override
		public void codeDeleted(ICode code) {
			AxialCodingViewModelList.this.refresh();
		};

		@Override
		public void axialCodingModelAdded(URI uri) {
			AxialCodingViewModelList.this.refresh();
		}

		@Override
		public void axialCodingModelUpdated(URI uri) {
			AxialCodingViewModelList.this.refresh();
		}

		@Override
		public void axialCodingModelRemoved(URI uri) {
			AxialCodingViewModelList.this.refresh();
		}
	};

	private final List<IListener> listeners = new ArrayList<IListener>();
	/**
	 * true if no listeners should be notified
	 */
	private boolean mute = false;

	private Combo acmCombo;
	private ComboViewer acmComboViewer;
	private URI selected;

	private Composite actionComposite;
	private Button copyButton;
	private Button renameButton;
	private Button deleteButton;

	private Composite originComposite;
	private SimpleIllustratedComposite caption;
	private Button refreshButton;

	public AxialCodingViewModelList(Composite parent, int style) {
		super(parent, style);
		CODE_SERVICE.addCodeServiceListener(this.codeServiceListener);
		this.addDisposeListener(e -> CODE_SERVICE
				.removeCodeServiceListener(AxialCodingViewModelList.this.codeServiceListener));

		this.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0)
				.numColumns(2).create());

		this.acmCombo = new Combo(this, SWT.DROP_DOWN | SWT.BORDER
				| SWT.READ_ONLY);
		this.acmCombo.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, false).create());

		this.acmComboViewer = new ComboViewer(this.acmCombo);
		this.acmComboViewer
				.setLabelProvider(new ILabelProviderService.StyledLabelProvider() {
					private final GTLabelProvider gtLabelProvider = new GTLabelProvider();

					@Override
					public StyledString getStyledText(URI element)
							throws Exception {
						StyledString s = this.gtLabelProvider
								.getStyledText(element);
						return s;
					}
				});
		this.acmComboViewer.setContentProvider(ArrayContentProvider
				.getInstance());
		this.acmComboViewer
				.addSelectionChangedListener(event -> {
					IStructuredSelection selection = (IStructuredSelection) event
							.getSelection();
					if (selection.size() > 0) {
						AxialCodingViewModelList.this.selected = (URI) selection
								.getFirstElement();
						AxialCodingViewModelList.this.copyButton
								.setEnabled(true);
						AxialCodingViewModelList.this.renameButton
								.setEnabled(true);
						AxialCodingViewModelList.this.deleteButton
								.setEnabled(true);
						if (!AxialCodingViewModelList.this.mute) {
							Set<URI> set = new HashSet<URI>(ArrayUtils
									.getAdaptableObjects(selection.toArray(),
											URI.class));
							for (IListener listener1 : AxialCodingViewModelList.this.listeners) {
								listener1.openClicked(set);
							}
						}
					} else {
						AxialCodingViewModelList.this.selected = null;
						AxialCodingViewModelList.this.copyButton
								.setEnabled(false);
						AxialCodingViewModelList.this.renameButton
								.setEnabled(false);
						AxialCodingViewModelList.this.deleteButton
								.setEnabled(false);
						if (!AxialCodingViewModelList.this.mute) {
							for (IListener listener2 : AxialCodingViewModelList.this.listeners) {
								listener2.openClicked(null);
							}
						}
					}
				});

		GridDataFactory gridDataFactory = GridDataFactory.fillDefaults();
		this.actionComposite = new Composite(this, SWT.NONE);
		this.actionComposite.setLayoutData(gridDataFactory.create());
		this.actionComposite.setLayout(GridLayoutFactory.fillDefaults()
				.numColumns(4).create());

		this.copyButton = new Button(this.actionComposite, SWT.PUSH);
		this.copyButton.setLayoutData(gridDataFactory.create());
		this.copyButton.setText("Make Copy");
		this.copyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!AxialCodingViewModelList.this.mute) {
					for (IListener listener : AxialCodingViewModelList.this.listeners) {
						listener.copyClicked(AxialCodingViewModelList.this.selected);
					}
				}
			}
		});

		this.renameButton = new Button(this.actionComposite, SWT.PUSH);
		this.renameButton.setLayoutData(gridDataFactory.create());
		this.renameButton.setText("Rename");
		this.renameButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!AxialCodingViewModelList.this.mute) {
					for (IListener listener : AxialCodingViewModelList.this.listeners) {
						listener.renameClicked(AxialCodingViewModelList.this.selected);
					}
				}
			}
		});

		this.deleteButton = new Button(this.actionComposite, SWT.PUSH);
		this.deleteButton.setLayoutData(gridDataFactory.create());
		this.deleteButton.setText("Delete");
		this.deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!AxialCodingViewModelList.this.mute) {
					for (IListener listener : AxialCodingViewModelList.this.listeners) {
						listener.deleteClicked(AxialCodingViewModelList.this.selected);
					}
				}
			}
		});

		Button createButton = new Button(this.actionComposite, SWT.PUSH);
		createButton.setLayoutData(gridDataFactory.create());
		createButton.setText("Create");
		createButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!AxialCodingViewModelList.this.mute) {
					for (IListener listener : AxialCodingViewModelList.this.listeners) {
						listener.createClicked();
					}
				}
			}
		});

		LocatorService.INSTANCE.getClass();
		AxialCodingViewModelList.this.refresh();
	}

	public void addListener(IListener listener) {
		this.listeners.add(listener);
	}

	public void removeListener(IListener listener) {
		this.listeners.remove(listener);
	}

	/**
	 * Show the given {@link URI}s as opened.
	 *
	 * @param uris
	 */
	public void setOpened(Set<URI> uris) {
		this.mute = true;
		this.acmComboViewer.setSelection(new StructuredSelection(
				new ArrayList<URI>(uris)));
		this.refreshOrigin();
		this.mute = false;
	}

	public void refreshOrigin() {
		List<URI> uris = SelectionUtils.getAdaptableObjects(
				this.acmComboViewer.getSelection(), URI.class);
		IAxialCodingModel acm = null;
		try {
			acm = uris.size() > 0 ? CODE_SERVICE.getAxialCodingModel(uris
					.iterator().next()) : null;
		} catch (CodeStoreReadException e1) {
			LOGGER.error("Could not find "
					+ IAxialCodingModel.class.getSimpleName() + " for "
					+ uris.iterator().next());
		}
		URI origin = acm != null ? acm.getOrigin() : null;

		if (origin != null) {
			if (this.originComposite == null) {
				this.originComposite = new Composite(this, SWT.NONE);
				this.originComposite.setLayoutData(GridDataFactory
						.fillDefaults().span(2, 1).create());
				this.originComposite.setLayout(GridLayoutFactory.fillDefaults()
						.numColumns(2).create());

				this.refreshButton = new Button(this.originComposite, SWT.PUSH);
				this.refreshButton.setLayoutData(GridDataFactory.fillDefaults()
						.create());
				this.refreshButton.setText("Refresh from origin");
				this.refreshButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (!AxialCodingViewModelList.this.mute) {
							for (IListener listener : AxialCodingViewModelList.this.listeners) {
								listener.refreshClicked(AxialCodingViewModelList.this.selected);
							}
						}
					}
				});

				this.caption = new SimpleIllustratedComposite(
						this.originComposite, SWT.NONE);
				this.caption.setLayoutData(GridDataFactory.fillDefaults()
						.grab(true, false).create());
				this.caption.setSpacing(0);
			}

			this.caption.setContent(new IllustratedText(LABEL_PROVIDER_SERVICE
					.getImage(origin), LABEL_PROVIDER_SERVICE.getText(origin)));
			this.layout(true, true);
		} else {
			if (this.originComposite != null
					&& !this.originComposite.isDisposed()) {
				this.originComposite.dispose();
				this.originComposite = null;
				this.layout(true, true);
			}
		}
	}

	protected void refresh() {
		// implicitly preload
		LocatorService.class.getName();
		this.mute = true;
		ISelection selection = this.acmComboViewer.getSelection();
		try {
			List<URI> models = CODE_SERVICE.getAxialCodingModels();
			this.acmComboViewer.setInput(models.toArray());
		} catch (CodeStoreReadException e) {
			LOGGER.error("Error loading axial coding models", e);
		}
		this.acmComboViewer.setSelection(selection);
		ViewerUtils.refresh(this.acmComboViewer);
		this.mute = false;
	}

}