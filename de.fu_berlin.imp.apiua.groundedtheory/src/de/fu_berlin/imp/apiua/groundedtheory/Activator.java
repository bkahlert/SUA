package de.fu_berlin.imp.apiua.groundedtheory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.bkahlert.nebula.utils.ExecUtils;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.DataServiceAdapter;
import de.fu_berlin.imp.apiua.core.services.IDataService;
import de.fu_berlin.imp.apiua.core.services.IDataServiceListener;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService.ILabelProvider;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService.ILabelProviderFactory;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceAdapter;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.apiua.groundedtheory.ui.DimensionValuesDialog;
import de.fu_berlin.imp.apiua.groundedtheory.ui.GTLabelProvider;

public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "de.fu_berlin.imp.apiua.groundedtheory"; //$NON-NLS-1$
	private static final Logger LOGGER = Logger.getLogger(Activator.class);
	private static Activator plugin;

	private ILabelProviderService labelProviderService = null;
	private final ILabelProviderFactory labelProviderFactory = new ILabelProviderService.LocatablePathLabelProviderFactory(
			0,
			new String[] {
					"code",
					"codeinstance",
					"episode",
					RelationLocatorProvider.RELATION_NAMESPACE,
					RelationInstanceLocatorProvider.RELATION_INSTANCE_NAMESPACE,
					AxialCodingModelLocatorProvider.AXIAL_CODING_MODEL_NAMESPACE }) {
		@Override
		protected ILabelProvider create() {
			return new GTLabelProvider();
		}
	};

	private final IDataService dataService = (IDataService) PlatformUI
			.getWorkbench().getService(IDataService.class);
	private final IDataServiceListener dataServiceListener = new DataServiceAdapter() {
		@Override
		public void export() {
			File exportDir = new File("~/Desktop/export");
			exportDir.mkdirs();

			File htmlFile = new File(exportDir, "seqan-design-decisions.html");
			StringBuffer sb = new StringBuffer();
			sb.append("<html>");
			sb.append("<head>");
			sb.append("<title>Theorie über die Entstehung und Auswirkungen von Entwurfsentscheidungen in SeqAn</title>");
			sb.append("</head>");
			sb.append("</html>");

			try {
				FileUtils.write(htmlFile, sb.toString());
			} catch (IOException e) {
				LOGGER.error("Error exporting", e);
			}
		};
	};

	private ICodeService codeService = null;
	private final ICodeServiceListener codeServiceListener = new CodeServiceAdapter() {
		@Override
		public void codesAssigned(
				java.util.List<de.fu_berlin.imp.apiua.groundedtheory.model.ICode> codes,
				java.util.List<de.fu_berlin.imp.apiua.core.model.URI> uris) {
			for (URI uri : uris) {
				if (Activator.this.codeService.getAllInstances(uri).size() > 0) {
					List<ICodeInstance> codeInstances = Activator.this.codeService
							.getAllInstances(uri);
					final AtomicReference<ICodeInstance> created = new AtomicReference<ICodeInstance>();
					for (ICodeInstance codeInstance : codeInstances) {
						if (codes.contains(codeInstance.getCode())) {
							created.set(codeInstance);
							break;
						}
					}
					try {
						if (created.get() != null
								&& Activator.this.codeService
										.getDimensionValues(created.get())
										.size() > 0) {
							ExecUtils.asyncExec(new Callable<Void>() {
								@Override
								public Void call() throws Exception {
									DimensionValuesDialog renameDialog = new DimensionValuesDialog(
											null, created.get());
									renameDialog.create();
									if (renameDialog.open() == Window.OK) {
									}
									return null;
								}
							}).get();
						}
					} catch (Exception e) {
					}
				}
			}
		}
	};

	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		URL confURL = context.getBundle().getEntry("log4j.xml");
		DOMConfigurator.configure(FileLocator.toFileURL(confURL).getFile());

		this.labelProviderService = (ILabelProviderService) PlatformUI
				.getWorkbench().getService(ILabelProviderService.class);
		this.labelProviderService
				.addLabelProviderFactory(this.labelProviderFactory);

		this.dataService.addDataServiceListener(this.dataServiceListener);

		this.codeService = (ICodeService) PlatformUI.getWorkbench().getService(
				ICodeService.class);
		this.codeService.addCodeServiceListener(this.codeServiceListener);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		this.codeService.removeCodeServiceListener(this.codeServiceListener);

		this.dataService.removeDataServiceListener(this.dataServiceListener);

		this.labelProviderService
				.removeLabelProviderFactory(this.labelProviderFactory);
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
}
