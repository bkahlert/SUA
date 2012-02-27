package de.fu_berlin.imp.seqan.usability_analyzer.entity.viewer;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.SortableTableViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.Entity;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.model.CMakeCacheFile;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.model.StatsFile;

public class EntityTableViewer extends SortableTableViewer {

	private static final Logger LOGGER = Logger
			.getLogger(EntityTableViewer.class);

	private SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();

	public EntityTableViewer(Composite parent, int style) {
		super(parent, style);

		final Table table = getTable();
		table.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.create());
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		createColumns();
		sort(1);
	}

	private void createColumns() {
		final ICodeService codeService = (ICodeService) PlatformUI
				.getWorkbench().getService(ICodeService.class);
		if (codeService != null) {
			this.createColumn("Code", 50).setLabelProvider(
					new ColumnLabelProvider() {
						@Override
						public String getText(Object element) {
							Entity person = (Entity) element;
							try {
								List<ICode> codes = codeService
										.getCodes(person);
								return StringUtils.join(codes, ", ");
							} catch (CodeServiceException e) {
								return "error";
							}
						}
					});
		} else {
			LOGGER.info("Deactivated Grounded Theory support");
		}
		this.createColumn("ID", 150).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Entity person = (Entity) element;
						ID id = person.getId();
						return (id != null) ? id.toString() : "";
					}
				});

		this.createColumn("Fingerprints", 300).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Entity person = (Entity) element;
						List<Fingerprint> secondaryFingerprints = person
								.getFingerprints();
						return (secondaryFingerprints != null) ? StringUtils
								.join(secondaryFingerprints, ", ") : "";
					}
				});

		this.createColumn("Token", 45).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Entity person = (Entity) element;
						Token token = person.getToken();
						return (token != null) ? token.toString() : "";

					}
				});

		this.createColumn("OS", 100).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Entity person = (Entity) element;
						StatsFile statsFile = person.getStatsFile();
						return (statsFile != null) ? statsFile
								.getPlatformLong() : "";

					}
				});

		this.createColumn("Generator", 100).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Entity person = (Entity) element;
						CMakeCacheFile cMakeCacheFile = person
								.getCMakeCacheFile();
						return (cMakeCacheFile != null) ? cMakeCacheFile
								.getGenerator() : "";

					}
				});

		this.createColumn("Earliest Entry", 180).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Entity person = (Entity) element;
						TimeZoneDate earliestDate = person
								.getEarliestEntryDate();
						return (earliestDate != null) ? earliestDate
								.format(preferenceUtil.getDateFormat()) : "";

					}
				});

		this.createColumn("Latest Entry", 180).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Entity person = (Entity) element;
						TimeZoneDate lastestDate = person.getLatestEntryDate();
						return (lastestDate != null) ? lastestDate
								.format(preferenceUtil.getDateFormat()) : "";
					}
				});
	}

}
