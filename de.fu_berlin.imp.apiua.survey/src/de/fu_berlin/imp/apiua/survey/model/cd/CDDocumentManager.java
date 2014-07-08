package de.fu_berlin.imp.apiua.survey.model.cd;

import java.io.IOException;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.model.data.IData;
import de.fu_berlin.imp.apiua.core.model.data.IDataContainer;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

/**
 * An instance of this class is responsible for managing one survey represented
 * as CSV file.
 * 
 * @author bkahlert
 * 
 */
public class CDDocumentManager {

	private static final Logger LOGGER = Logger
			.getLogger(CDDocumentManager.class);

	public static final int HEADING_LINE = 0;

	private IDataContainer dataContainer;
	private String lang;
	private Map<URI, CDDocument> documents;

	public CDDocumentManager(IDataContainer dataContainer, String lang) {
		this.dataContainer = dataContainer;
		this.lang = lang;
	}

	public void scan(IProgressMonitor monitor) {
		List<IData> datas = this.dataContainer.getResources();

		SubMonitor progress = SubMonitor.convert(monitor, datas.size());
		this.documents = new HashMap<URI, CDDocument>();

		for (IData data : this.dataContainer.getResources()) {
			if (!FilenameUtils.getExtension(data.getName())
					.equals("properties")) {
				try {
					CDDocument document = new CDDocument(data, this.lang);
					this.documents.put(document.getUri(), document);
				} catch (IOException e) {
					LOGGER.warn("Can't read " + data + ". Skipping.");
				}
			}
			progress.worked(1);
		}

		progress.done();
	}

	public Collection<CDDocument> getDocuments() {
		return this.documents.values();
	}

	public CDDocument getDocument(URI uri) {
		Assert.isNotNull(uri);
		return this.documents.get(uri);
	}
}
