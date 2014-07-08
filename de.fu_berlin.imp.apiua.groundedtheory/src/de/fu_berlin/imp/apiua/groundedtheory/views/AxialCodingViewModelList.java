package de.fu_berlin.imp.apiua.groundedtheory.views;

import java.util.Arrays;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.widgets.itemlist.ItemList;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService.ILabelProvider;
import de.fu_berlin.imp.apiua.core.util.WorkbenchUtils;
import de.fu_berlin.imp.apiua.groundedtheory.AxialCodingModelLocatorProvider;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.IAxialCodingModel;
import de.fu_berlin.imp.apiua.groundedtheory.model.JointJSAxialCodingModel;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceAdapter;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreReadException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreWriteException;

class AxialCodingViewModelList extends ItemList {

	private static final Logger LOGGER = Logger
			.getLogger(AxialCodingViewModelList.class);

	private static final ICodeService CODE_SERVICE = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	private static final ILabelProviderService LABEL_PROVIDER_SERVICE = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);

	private static final String CREATE_ID = "create";

	private final ICodeServiceListener codeServiceListener = new CodeServiceAdapter() {
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

	public AxialCodingViewModelList(Composite parent, int style) {
		super(parent, style);
		CODE_SERVICE.addCodeServiceListener(this.codeServiceListener);
		this.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				CODE_SERVICE
						.removeCodeServiceListener(AxialCodingViewModelList.this.codeServiceListener);
			}
		});

		this.addListener(new ItemListAdapter() {
			@Override
			public void itemClicked(String key) {
				if (key.equals(CREATE_ID)) {
					try {
						CODE_SERVICE
								.addAxialCodingModel(new JointJSAxialCodingModel(
										AxialCodingModelLocatorProvider
												.createUniqueURI(),
										"{\"cells\":[],\"title\":\"New Model\"}"));
					} catch (CodeStoreWriteException e) {
						AxialCodingView.LOGGER.error("Error creating new "
								+ IAxialCodingModel.class.getSimpleName(), e);
					}
				} else {
					final URI uri = new URI(key);
					AxialCodingView view = (AxialCodingView) WorkbenchUtils
							.getView(AxialCodingView.ID);
					if (view != null) {
						final Future<Void> success = view.open(uri);
						ExecUtils.nonUIAsyncExec(new Runnable() {
							@Override
							public void run() {
								try {
									success.get();
								} catch (Exception e) {
									LOGGER.error(
											"Error opening "
													+ IAxialCodingModel.class
															.getSimpleName()
													+ " " + uri, e);
								}
							}
						});
					}
				}
			}

			@Override
			public void itemClicked(String key, int i) {
				if (key.equals(CREATE_ID)) {
				} else {
					URI uri = new URI(key);
					switch (i) {
					case 1:
						try {
							CODE_SERVICE.removeAxialCodingModel(uri);
						} catch (CodeStoreWriteException e) {
							AxialCodingView.LOGGER.error("Error removing "
									+ IAxialCodingModel.class.getSimpleName()
									+ " " + uri);
						}
						break;
					default:
						try {
							AxialCodingViewRenameDialog renameDialog = new AxialCodingViewRenameDialog(
									AxialCodingViewModelList.this.getShell(),
									LABEL_PROVIDER_SERVICE
											.getLabelProvider(uri).getText(uri));
							renameDialog.create();
							if (renameDialog.open() == Window.OK) {
								IAxialCodingModel axialCodingModel = CODE_SERVICE
										.getAxialCodingModel(uri);
								if (axialCodingModel instanceof JointJSAxialCodingModel) {
									((JointJSAxialCodingModel) axialCodingModel)
											.setTitle(renameDialog.getTitle());
									LocatorService.INSTANCE.uncache(uri);
								}
								CODE_SERVICE
										.addAxialCodingModel(axialCodingModel);
							}
						} catch (Exception e) {
							AxialCodingView.LOGGER.error("Error renaming "
									+ uri, e);
						}
						break;
					}
				}
			}
		});

		this.setSpacing(5);
		this.refresh();

	}

	protected void refresh() {
		this.clear();
		this.addExistingModels();
		this.addCreateItem();

		this.getParent().layout();
	}

	public void addExistingModels() {
		try {
			for (URI uri : CODE_SERVICE.getAxialCodingModels()) {
				this.addModel(uri);
			}
		} catch (CodeStoreReadException e1) {
			AxialCodingView.LOGGER.error("Error getting available "
					+ IAxialCodingModel.class + "s");
		}
	}

	public void addModel(URI uri) {
		ILabelProvider labelProvider = LABEL_PROVIDER_SERVICE
				.getLabelProvider(uri);
		String title = uri.toString();
		try {
			title = labelProvider.getText(uri);
		} catch (Exception e) {
			AxialCodingView.LOGGER.warn("Could not retrieve title of " + uri);
		}
		this.addItem(uri.toString(), title, ButtonOption.PRIMARY,
				ButtonSize.EXTRA_SMALL, ButtonStyle.HORIZONTAL,
				Arrays.asList("R", "X"));
	}

	public void addCreateItem() {
		this.addItem(CREATE_ID, "Create New Model", ButtonOption.INFO,
				ButtonSize.EXTRA_SMALL, ButtonStyle.HORIZONTAL, null);
	}
}