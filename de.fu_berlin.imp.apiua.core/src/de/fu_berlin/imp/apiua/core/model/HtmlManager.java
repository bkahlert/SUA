package de.fu_berlin.imp.apiua.core.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.imp.apiua.core.model.data.IData;
import de.fu_berlin.imp.apiua.core.model.data.IDataContainer;

/**
 * An instance of this class is responsible for managing a
 * {@link IDataContainer} containing {@link IHtmlDocument}s.
 *
 * @author bkahlert
 *
 */
public class HtmlManager {

	private static final Logger LOGGER = Logger.getLogger(HtmlManager.class);

	private IDataContainer dataContainer;
	private String baseUri;
	private List<IHtmlDocument> documents;

	public HtmlManager(IDataContainer dataContainer, String baseUri) {
		this.dataContainer = dataContainer;
		this.baseUri = baseUri;
	}

	public void scan(IProgressMonitor monitor) {
		List<IData> datas = this.dataContainer.getResources();

		SubMonitor progress = SubMonitor.convert(monitor, datas.size());
		this.documents = new LinkedList<>();

		for (IData data : this.dataContainer.getResources()) {
			if (FilenameUtils.getExtension(data.getName()).equals("html")) {
				IData properties = this.dataContainer.getResource(data
						.getName().substring(
								0,
								data.getName().length()
										- FilenameUtils.getExtension(
												data.getName()).length())
						+ "properties");
				try {
					Properties props = new Properties();
					props.load(IOUtils.toInputStream(properties.read()));
					this.documents.add(new HtmlDocument(data, props
							.getProperty("cssQuery"), this.baseUri));
				} catch (Exception e) {
					LOGGER.error(
							"Couldn't scan "
									+ IHtmlDocument.class.getSimpleName() + " "
									+ data.getName(), e);
				}
			}
			progress.worked(1);
		}

		progress.done();
	}

	public Collection<IHtmlDocument> getDocuments() {
		return new LinkedList<>(this.documents);
	}

	public IHtmlDocument getDocument(URI uri) {
		Assert.isNotNull(uri);
		for (IHtmlDocument document : this.documents) {
			if (document.getUri().equals(uri)) {
				return document;
			}
		}
		return null;
	}
}
