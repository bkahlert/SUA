package de.fu_berlin.imp.seqan.usability_analyzer.entity.viewer;

import java.net.URI;
import java.util.ArrayList;
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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.viewer.SortableTableViewer;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdentifierFactory;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.Token;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.DataServiceAdapter;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.IBoldViewer;
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

	private IDataService dataService = (IDataService) PlatformUI.getWorkbench()
			.getService(IDataService.class);
	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);

	IDataServiceListener dataServiceListener = new DataServiceAdapter() {
		public void dataDirectoriesLoaded(
				List<? extends IBaseDataContainer> baseDataContainers) {
			setBold(null);
		};
	};

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

		dataService.addDataServiceListener(dataServiceListener);
		table.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				dataService
						.removeDataServiceListener(dataServiceListener);
			}
		});
	}

	private void createColumns() {
		this.createColumn("ID", 150).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new StyledColumnLabelProvider() {
							@Override
							public StyledString getStyledText(Object element) {
								Entity entity = (Entity) element;

								ID id = entity.getId();
								StyledString styledString = new StyledString(
										(id != null) ? id.toString() : "",
										(boldObjects != null
												&& boldObjects
														.contains(element) ? boldStyler
												: null));
								return styledString;
							}

							@Override
							public Image getImage(Object element) {
								Entity entity = (Entity) element;
								try {
									if (codeService.getCodes(entity).size() > 0) {
										return codeService.isMemo(entity) ? ImageManager.ENTITY_CODED_MEMO
												: ImageManager.ENTITY_CODED;
									} else {
										for (URI id : codeService.getCodedIDs()) {
											String[] parts = id.getPath()
													.split("/");
											if (parts.length > 0) {
												String key = parts[1];
												if (ID.isValid(key)
														&& entity.getId() != null
														&& entity.getId()
																.equals(IdentifierFactory
																		.createFrom(key))) {
													return codeService
															.isMemo(entity) ? ImageManager.ENTITY_PARTIALLY_CODED_MEMO
															: ImageManager.ENTITY_PARTIALLY_CODED;
												}
												if (Fingerprint.isValid(key)
														&& entity
																.getFingerprints()
																.contains(
																		IdentifierFactory
																				.createFrom(key))) {
													return codeService
															.isMemo(entity) ? ImageManager.ENTITY_PARTIALLY_CODED_MEMO
															: ImageManager.ENTITY_PARTIALLY_CODED;
												}
											}
										}

										return codeService.isMemo(entity) ? ImageManager.ENTITY_MEMO
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
								Entity entity = (Entity) element;
								List<Fingerprint> secondaryFingerprints = entity
										.getFingerprints();
								StyledString styledString = new StyledString(
										(secondaryFingerprints != null) ? StringUtils
												.join(secondaryFingerprints,
														", ") : "",
										(boldObjects != null
												&& boldObjects
														.contains(element) ? boldStyler
												: null));
								return styledString;
							}
						}));

		this.createColumn("Token", 45).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new StyledColumnLabelProvider() {
							@Override
							public StyledString getStyledText(Object element) {
								Entity entity = (Entity) element;
								Token token = entity.getToken();
								StyledString styledString = new StyledString(
										(token != null) ? token.toString() : "",
										(boldObjects != null
												&& boldObjects
														.contains(element) ? boldStyler
												: null));
								return styledString;
							}
						}));

		this.createColumn("OS", 100).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new StyledColumnLabelProvider() {
							@Override
							public StyledString getStyledText(Object element) {
								Entity entity = (Entity) element;
								StatsFile statsFile = entity.getStatsFile();
								StyledString styledString = new StyledString(
										(statsFile != null) ? statsFile
												.getPlatformLong() : "",
										(boldObjects != null
												&& boldObjects
														.contains(element) ? boldStyler
												: null));
								return styledString;
							}
						}));

		this.createColumn("Generator", 100).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new StyledColumnLabelProvider() {
							@Override
							public StyledString getStyledText(Object element) {
								Entity entity = (Entity) element;
								CMakeCacheFile cMakeCacheFile = entity
										.getCMakeCacheFile();
								StyledString styledString = new StyledString(
										(cMakeCacheFile != null) ? cMakeCacheFile
												.getGenerator() : "",
										(boldObjects != null
												&& boldObjects
														.contains(element) ? boldStyler
												: null));
								return styledString;
							}
						}));

		this.createColumn("Earliest Entry", 180).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new StyledColumnLabelProvider() {
							@Override
							public StyledString getStyledText(Object element) {
								Entity entity = (Entity) element;
								TimeZoneDate earliestDate = entity
										.getEarliestEntryDate();
								StyledString styledString = new StyledString(
										(earliestDate != null) ? earliestDate
												.format(preferenceUtil
														.getDateFormat()) : "",
										(boldObjects != null
												&& boldObjects
														.contains(element) ? boldStyler
												: null));
								return styledString;
							}
						}));

		this.createColumn("Latest Entry", 180).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new StyledColumnLabelProvider() {
							@Override
							public StyledString getStyledText(Object element) {
								Entity entity = (Entity) element;
								TimeZoneDate lastestDate = entity
										.getLatestEntryDate();
								StyledString styledString = new StyledString(
										(lastestDate != null) ? lastestDate
												.format(preferenceUtil
														.getDateFormat()) : "",
										(boldObjects != null
												&& boldObjects
														.contains(element) ? boldStyler
												: null));
								return styledString;
							}
						}));
	}

	public void setBold(Object boldObject) {
		this.setBold(Arrays.asList(boldObject));
	}

	public void setBold(Collection<?> boldObjects) {
		if (this.boldObjects != boldObjects) {
			List<Object> update = new ArrayList<Object>();
			if (this.boldObjects != null)
				update.addAll(this.boldObjects);
			if (boldObjects != null)
				update.addAll(boldObjects);

			this.boldObjects = boldObjects;
			this.update(update.toArray(), null);
		}
	}
}
