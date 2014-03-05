package de.fu_berlin.imp.seqan.usability_analyzer.entity.filters;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSource;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.Entity;

public class DataSourceFilter extends ViewerFilter {

	private static final Logger LOGGER = Logger
			.getLogger(DataSourceFilter.class);

	private static final ILocatorService LOCATOR_SERVICE = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);

	private final DataSource dataSource;
	private final DoclogDataContainer doclogDataContainer;

	public DataSourceFilter(DataSource dataSource) {
		this.dataSource = dataSource;
		this.doclogDataContainer = Activator.getDefault().getDoclogContainer();
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (URI.class.isInstance(element)) {
			URI uri = (URI) element;
			ILocatable locatable = null;
			try {
				locatable = LOCATOR_SERVICE.resolve(uri, null).get();
			} catch (Exception e) {
				LOGGER.error("Error filtering data", e);
			}
			if (Entity.class.isInstance(locatable)) {
				Entity entity = (Entity) locatable;
				switch (this.dataSource) {
				case DIFFS:
					return entity.getId() != null; // TODO: diffFiles prüfen
				case DOCLOG:
					ID id = entity.getId();
					if (this.doclogDataContainer.getFile(id) != null) {
						return true;
					}
					for (Fingerprint fingerprint : entity.getFingerprints()) {
						if (this.doclogDataContainer.getFile(fingerprint) != null) {
							return true;
						}
					}
					return false;
				case SURVEYRECORD:
					return entity.getSurveyRecord() != null;
				}
			}
		}
		return true;
	}
}
