package de.fu_berlin.imp.seqan.usability_analyzer.entity.viewer;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.IBoldViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.SortableTableViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.StyledColumnLabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FontUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.Entity;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.ui.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.model.CMakeCacheFile;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.model.StatsFile;

public class EntityViewer extends SortableTableViewer implements IBoldViewer {

	private static final Logger LOGGER = Logger.getLogger(EntityViewer.class);

	private SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();

	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);

	private Styler boldStyler = null;
	private Collection<?> boldObjects = new LinkedList<Object>();

	public EntityViewer(Table table) {
		super(table);

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
		this.createColumn("ID", 150).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new StyledColumnLabelProvider() {
							@Override
							public StyledString getStyledText(Object element) {
								Entity entity = (Entity) element;

								ID id = entity.getID();
								StyledString styledString = new StyledString(
										(id != null) ? id.toString() : "",
										(boldObjects.contains(element) ? boldStyler
												: null));
								return styledString;
							}

							@Override
							public Image getImage(Object element) {
								Entity person = (Entity) element;
								try {
									if (codeService.getCodes(person).size() > 0) {
										return codeService.isMemo(person) ? ImageManager.ENTITY_CODED_MEMO
												: ImageManager.ENTITY_CODED;
									} else {
										for (URI id : codeService.getCodedIDs()) {
											String[] parts = id.getPath()
													.split("/");
											if (parts.length > 0) {
												String key = parts[1];
												if (ID.isValid(key)
														&& person.getID() != null
														&& person.getID()
																.equals(new ID(
																		key))) {
													return codeService
															.isMemo(person) ? ImageManager.ENTITY_PARTIALLY_CODED_MEMO
															: ImageManager.ENTITY_PARTIALLY_CODED;
												}
												if (Fingerprint.isValid(key)
														&& person
																.getFingerprints()
																.contains(
																		new Fingerprint(
																				key))) {
													return codeService
															.isMemo(person) ? ImageManager.ENTITY_PARTIALLY_CODED_MEMO
															: ImageManager.ENTITY_PARTIALLY_CODED;
												}
											}
										}

										return codeService.isMemo(person) ? ImageManager.ENTITY_MEMO
												: ImageManager.ENTITY;
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
										(boldObjects.contains(element) ? boldStyler
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
										(boldObjects.contains(element) ? boldStyler
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
										(boldObjects.contains(element) ? boldStyler
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
										(boldObjects.contains(element) ? boldStyler
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
										(boldObjects.contains(element) ? boldStyler
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
										(boldObjects.contains(element) ? boldStyler
												: null));
								return styledString;
							}
						}));
	}

	public void setBold(Object boldObject) {
		this.boldObjects = Arrays.asList(boldObject);
	}

	public void setBold(Collection<?> boldObjects) {
		if (this.boldObjects != boldObjects) {
			this.boldObjects = boldObjects;
		}
	}
}
