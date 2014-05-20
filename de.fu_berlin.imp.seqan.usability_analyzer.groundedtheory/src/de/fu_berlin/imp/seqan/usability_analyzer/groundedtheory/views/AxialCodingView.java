package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.nebula.viewer.jointjs.JointJSContentProvider;
import com.bkahlert.nebula.viewer.jointjs.JointJSLabelProvider;
import com.bkahlert.nebula.viewer.jointjs.JointJSViewer;
import com.bkahlert.nebula.widgets.browser.listener.IDropListener;
import com.bkahlert.nebula.widgets.itemlist.ItemList;
import com.bkahlert.nebula.widgets.jointjs.JointJS;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService.ILabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.AxialCodingModelLocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.LocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IAxialCodingModel;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.JointJSAxialCodingModel;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceAdapter;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreReadException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreWriteException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.AxialCodingContentProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.AxialCodingLabelProvider;

public class AxialCodingView extends ViewPart {

	private static final Logger LOGGER = Logger
			.getLogger(AxialCodingView.class);

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.AxialCodingView";

	private static class RenameDialog extends TitleAreaDialog {

		private Text titleText;

		private String title;

		public RenameDialog(Shell parentShell, String title) {
			super(parentShell);
			this.title = title;
		}

		@Override
		public void create() {
			super.create();
			this.setTitle("Rename");
			this.setMessage("Please enter the new title for this model",
					IMessageProvider.INFORMATION);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite area = (Composite) super.createDialogArea(parent);
			Composite container = new Composite(area, SWT.NONE);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			container.setLayout(new FillLayout());

			this.titleText = new Text(container, SWT.BORDER);
			this.titleText.setText(this.title);
			return area;
		}

		@Override
		protected boolean isResizable() {
			return true;
		}

		private void saveInput() {
			this.title = this.titleText.getText();
		}

		@Override
		protected void okPressed() {
			this.saveInput();
			super.okPressed();
		}

		public String getTitle() {
			return this.title;
		}

	}

	private static class ModelList extends ItemList {

		private static final ICodeService CODE_SERVICE = (ICodeService) PlatformUI
				.getWorkbench().getService(ICodeService.class);

		private static final ILabelProviderService LABEL_PROVIDER_SERVICE = (ILabelProviderService) PlatformUI
				.getWorkbench().getService(ILabelProviderService.class);

		private static final String CREATE_ID = "create";

		private final ICodeServiceListener codeServiceListener = new CodeServiceAdapter() {
			@Override
			public void axialCodingModelAdded(URI uri) {
				ModelList.this.refresh();
			}

			@Override
			public void axialCodingModelUpdated(URI uri) {
				ModelList.this.refresh();
			}

			@Override
			public void axialCodingModelRemoved(URI uri) {
				ModelList.this.refresh();
			}
		};

		public ModelList(Composite parent, int style) {
			super(parent, style);
			CODE_SERVICE.addCodeServiceListener(this.codeServiceListener);
			this.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					CODE_SERVICE
							.removeCodeServiceListener(ModelList.this.codeServiceListener);
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
							LOGGER.error("Error creating new "
									+ IAxialCodingModel.class.getSimpleName(),
									e);
						}
					}
				}

				@Override
				public void itemClicked(String key, int i) {
					URI uri = new URI(key);
					switch (i) {
					case 1:
						try {
							CODE_SERVICE.removeAxialCodingModel(uri);
						} catch (CodeStoreWriteException e) {
							LOGGER.error("Error removing "
									+ IAxialCodingModel.class.getSimpleName()
									+ " " + uri);
						}
						break;
					default:
						try {
							RenameDialog renameDialog = new RenameDialog(
									ModelList.this.getShell(),
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
							LOGGER.error("Error renaming " + uri, e);
						}
						break;
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
				LOGGER.error("Error getting available "
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
				LOGGER.warn("Could not retrieve title of " + uri);
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

	private static final ICodeService CODE_SERVICE = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	private JointJS jointjs = null;
	private JointJSViewer jointjsViewer = null;

	private URI openedUri = null;

	public AxialCodingView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, true));
		ItemList modelList = new ModelList(parent, SWT.BORDER);
		modelList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		this.jointjs = new JointJS(parent, SWT.NONE, "sua://code/",
				"sua://code-link");
		this.jointjs
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		JointJSContentProvider contentProvider = new AxialCodingContentProvider();
		JointJSLabelProvider labelProvider = new AxialCodingLabelProvider();
		this.jointjsViewer = new JointJSViewer(this.jointjs, contentProvider,
				labelProvider);

		this.jointjsViewer.setInput(PlatformUI.getWorkbench().getService(
				ICodeService.class));
		this.jointjsViewer.refresh();

		this.activateDropSupport();
	}

	private void activateDropSupport() {
		this.jointjs.addDropListener(new IDropListener() {
			@Override
			public void drop(long offsetX, long offsetY, String data) {
				if (data == null || data.isEmpty()) {
					return;
				}

				List<URI> uris = new LinkedList<URI>();
				for (String uri : data.split("\\|")) {
					try {
						uris.add(new URI(uri));
					} catch (Exception e) {
						LOGGER.error("Error dropping " + uri);
					}
				}
			}
		});
	}

	public JointJS getJointjs() {
		return this.jointjs;
	}

	public void open(URI uri) {
		try {
			IAxialCodingModel axialCodingModel = CODE_SERVICE
					.getAxialCodingModel(uri);
			this.jointjsViewer.setInput(axialCodingModel);
			this.openedUri = uri;
		} catch (CodeStoreReadException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public Object getOpenedURI() {
		return this.openedUri;
	}

	@Override
	public void setFocus() {
		this.jointjs.setFocus();
	}

}
