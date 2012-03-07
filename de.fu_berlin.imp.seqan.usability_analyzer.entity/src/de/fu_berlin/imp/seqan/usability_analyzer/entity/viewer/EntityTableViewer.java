package de.fu_berlin.imp.seqan.usability_analyzer.entity.viewer;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.SortableTableViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.StyledColumnLabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FontUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.Entity;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.ui.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.model.CMakeCacheFile;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.model.StatsFile;

public class EntityTableViewer extends SortableTableViewer {

	private static final Logger LOGGER = Logger
			.getLogger(EntityTableViewer.class);

	private SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();

	private Styler boldStyler = null;
	private Object boldObject = null;

	public EntityTableViewer(Composite parent, int style) {
		super(parent, style);

		final Table table = getTable();
		table.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.create());
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		LocalResourceManager resources = new LocalResourceManager(
				JFaceResources.getResources(), table);

		final Font boldFont = resources.createFont(FontDescriptor
				.createFrom(FontUtils.getModifiedFontData(table.getFont()
						.getFontData(), SWT.BOLD)));
		this.boldStyler = new StyledString.Styler() {
			@Override
			public void applyStyles(TextStyle textStyle) {
				textStyle.font = boldFont;
			}
		};

		createColumns();

		sort(0);
	}

	private void createColumns() {
		final ICodeService codeService = (ICodeService) PlatformUI
				.getWorkbench().getService(ICodeService.class);
		this.createColumn("ID", 150).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new StyledColumnLabelProvider() {
							@Override
							public StyledString getStyledText(Object element) {
								Entity entity = (Entity) element;

								ID id = entity.getId();
								StyledString styledString = new StyledString(
										(id != null) ? id.toString() : "",
										(element.equals(boldObject) ? boldStyler
												: null));
								return styledString;
							}

							@Override
							public Image getImage(Object element) {
								Entity person = (Entity) element;
								try {
									if (codeService.getCodes(person).size() > 0) {
										return ImageManager.ENTITY_CODED;
									} else {
										return ImageManager.ENTITY;
									}
								} catch (CodeServiceException e) {
									LOGGER.error("Can't access "
											+ ICodeService.class
													.getSimpleName());
								}
								return ImageManager.ENTITY;
							}
						}));

		this.createColumn("Fingerprints", 300).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new StyledColumnLabelProvider() {
							@Override
							public StyledString getStyledText(Object element) {
								Entity person = (Entity) element;
								List<Fingerprint> secondaryFingerprints = person
										.getFingerprints();
								StyledString styledString = new StyledString(
										(secondaryFingerprints != null) ? StringUtils
												.join(secondaryFingerprints,
														", ") : "",
										(element.equals(boldObject) ? boldStyler
												: null));
								return styledString;
							}
						}));

		this.createColumn("Token", 45).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new StyledColumnLabelProvider() {
							@Override
							public StyledString getStyledText(Object element) {
								Entity person = (Entity) element;
								Token token = person.getToken();
								StyledString styledString = new StyledString(
										(token != null) ? token.toString() : "",
										(element.equals(boldObject) ? boldStyler
												: null));
								return styledString;
							}
						}));

		this.createColumn("OS", 100).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new StyledColumnLabelProvider() {
							@Override
							public StyledString getStyledText(Object element) {
								Entity person = (Entity) element;
								StatsFile statsFile = person.getStatsFile();
								StyledString styledString = new StyledString(
										(statsFile != null) ? statsFile
												.getPlatformLong() : "",
										(element.equals(boldObject) ? boldStyler
												: null));
								return styledString;
							}
						}));

		this.createColumn("Generator", 100).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new StyledColumnLabelProvider() {
							@Override
							public StyledString getStyledText(Object element) {
								Entity person = (Entity) element;
								CMakeCacheFile cMakeCacheFile = person
										.getCMakeCacheFile();
								StyledString styledString = new StyledString(
										(cMakeCacheFile != null) ? cMakeCacheFile
												.getGenerator() : "",
										(element.equals(boldObject) ? boldStyler
												: null));
								return styledString;
							}
						}));

		this.createColumn("Earliest Entry", 180).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new StyledColumnLabelProvider() {
							@Override
							public StyledString getStyledText(Object element) {
								Entity person = (Entity) element;
								TimeZoneDate earliestDate = person
										.getEarliestEntryDate();
								StyledString styledString = new StyledString(
										(earliestDate != null) ? earliestDate
												.format(preferenceUtil
														.getDateFormat()) : "",
										(element.equals(boldObject) ? boldStyler
												: null));
								return styledString;
							}
						}));

		this.createColumn("Latest Entry", 180).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new StyledColumnLabelProvider() {
							@Override
							public StyledString getStyledText(Object element) {
								Entity person = (Entity) element;
								TimeZoneDate lastestDate = person
										.getLatestEntryDate();
								StyledString styledString = new StyledString(
										(lastestDate != null) ? lastestDate
												.format(preferenceUtil
														.getDateFormat()) : "",
										(element.equals(boldObject) ? boldStyler
												: null));
								return styledString;
							}
						}));
	}

	public void setBold(Object boldObject) {
		if (this.boldObject != boldObject) {
			this.boldObject = boldObject;
		}
	}
}
