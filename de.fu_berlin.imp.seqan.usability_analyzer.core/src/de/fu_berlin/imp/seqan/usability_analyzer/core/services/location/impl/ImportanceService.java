package de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.impl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IImportanceService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IImportanceServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ImportanceServiceException;

public class ImportanceService implements IImportanceService {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(ImportanceService.class);

	private final File file;
	private final File backupFile;
	private Map<URI, Importance> uris;

	private final List<IImportanceServiceListener> importanceServiceListeners = new ArrayList<IImportanceServiceListener>();

	public ImportanceService(File file) throws IOException {
		this.file = file;
		this.backupFile = new File(file.getAbsolutePath() + ".bak");
		this.load();
	}

	@SuppressWarnings("unchecked")
	private void load() throws IOException {
		this.uris = new HashMap<URI, Importance>();
		if (this.file.length() > 0) {
			Map<String, String> map = (Map<String, String>) SerializationUtils
					.deserialize(FileUtils.readFileToByteArray(this.file));
			for (Entry<String, String> entry : map.entrySet()) {
				URI uri = new URI(entry.getKey());
				Importance importance = Importance.valueOf(entry.getValue());
				this.uris.put(uri, importance);
			}
		}
	}

	private void save() throws IOException {
		File file = File.createTempFile("SUA", ".importance");

		Map<String, String> map = new HashMap<String, String>();
		for (Entry<URI, Importance> entry : this.uris.entrySet()) {
			map.put(entry.getKey().toString(), entry.getValue().toString());
		}
		FileUtils.writeByteArrayToFile(file,
				SerializationUtils.serialize((Serializable) map));

		if (this.backupFile.exists()) {
			this.backupFile.delete();
		}
		if (this.file.exists()) {
			FileUtils.moveFile(this.file, this.backupFile);
		}
		FileUtils.moveFile(file, this.file);
	}

	@Override
	public void setImportance(Collection<URI> uris, Importance importance) {
		Assert.isNotNull(uris);
		Assert.isNotNull(importance);
		Set<URI> affected = new HashSet<URI>();
		for (URI uri : uris) {
			Importance oldImportance = this.uris.get(uri);
			if (oldImportance == null) {
				oldImportance = Importance.DEFAULT;
			}

			if (oldImportance != importance) {
				if (importance != Importance.DEFAULT) {
					this.uris.put(uri, importance);
				} else {
					this.uris.remove(uri);
				}
				affected.add(uri);
			}
		}
		try {
			this.save();
		} catch (IOException e) {
			throw new ImportanceServiceException(e);
		}
		for (IImportanceServiceListener importanceServiceListener : this.importanceServiceListeners) {
			importanceServiceListener.importanceChanged(affected, importance);
		}
	}

	@Override
	public void setImportance(URI uri, Importance importance) {
		Assert.isNotNull(uri);
		Assert.isNotNull(importance);
		this.setImportance(Arrays.asList(uri), importance);
	}

	@Override
	public Importance getImportance(URI uri) {
		Importance importance = this.uris.get(uri);
		if (importance == null) {
			importance = Importance.DEFAULT;
		}
		return importance;
	}

	@Override
	public void addImportanceServiceListener(
			IImportanceServiceListener importanceServiceListener) {
		this.importanceServiceListeners.add(importanceServiceListener);
	}

	@Override
	public void removeImportanceServiceListener(
			IImportanceServiceListener importanceServiceListener) {
		this.importanceServiceListeners.remove(importanceServiceListener);
	}

}
