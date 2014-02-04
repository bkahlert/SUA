package de.fu_berlin.imp.seqan.usability_analyzer.entity.viewer;

import java.net.URI;
import java.util.ArrayList;
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

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
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
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.IBoldViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FontUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.Entity;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.ui.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.model.CMakeCacheFile;
import de.fu_berlin.imp.seqan.usability_analyzer.stats.model.StatsFile;

public class EntityViewer extends SortableTableViewer implements
		IBoldViewer<URI> {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(EntityViewer.class);

	private final SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();

	private final IDataService dataService = (IDataService) PlatformUI
			.getWorkbench().getService(IDataService.class);
	private final ICodeService codeService = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	IDataServiceListener dataServiceListener = new DataServiceAdapter() {
		@Override
		public void dataDirectoriesLoaded(
				List<? extends IBaseDataContainer> baseDataContainers) {
			EntityViewer.this.setBold(null);
		};
	};

	private Styler boldStyler = null;
	private Collection<URI> boldObjects = new LinkedList<URI>();

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

		this.createColumns();
		this.sort(0);

		this.dataService.addDataServiceListener(this.dataServiceListener);
		table.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				EntityViewer.this.dataService
						.removeDataServiceListener(EntityViewer.this.dataServiceListener);
			}
		});
	}

	private void createColumns() {
		final ILocatorService locatorService = (ILocatorService) PlatformUI
				.getWorkbench().getService(ILocatorService.class);

		this.createColumn("ID", 150).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new ILabelProviderService.StyledColumnLabelProvider() {

							@Override
							public StyledString getStyledText(URI uri)
									throws Exception {
								ILocatable locatable = locatorService.resolve(
										uri, null).get();

								Entity entity = (Entity) locatable;

								ID id = entity.getId();
								if (id == null) {
									return new StyledString("");
								}

								StyledString styledString = new StyledString(id
										.toString(),
										(boldObjects.contains(uri) ? boldStyler
												: null));
								return styledString;
							}

							@Override
							public Image getImage(URI uri) throws Exception {
								ILocatable locatable = locatorService.resolve(
										uri, null).get();

								Entity entity = (Entity) locatable;

								if (EntityViewer.this.codeService.getCodes(
										entity.getUri()).size() > 0) {
									return EntityViewer.this.codeService
											.isMemo(entity.getUri()) ? ImageManager.ENTITY_CODED_MEMO
											: ImageManager.ENTITY_CODED;
								} else {
									for (URI id : EntityViewer.this.codeService
											.getCodedIDs()) {
										String[] parts = id.getPath()
												.split("/");
										if (parts.length > 1) {
											String key = parts[1];
											if (ID.isValid(key)
													&& entity.getId() != null
													&& entity
															.getId()
															.equals(IdentifierFactory
																	.createFrom(key))) {
												return EntityViewer.this.codeService
														.isMemo(entity.getUri()) ? ImageManager.ENTITY_PARTIALLY_CODED_MEMO
														: ImageManager.ENTITY_PARTIALLY_CODED;
											}
											if (Fingerprint.isValid(key)
													&& entity
															.getFingerprints()
															.contains(
																	IdentifierFactory
																			.createFrom(key))) {
												return EntityViewer.this.codeService
														.isMemo(entity.getUri()) ? ImageManager.ENTITY_PARTIALLY_CODED_MEMO
														: ImageManager.ENTITY_PARTIALLY_CODED;
											}
										}
									}

									return EntityViewer.this.codeService
											.isMemo(entity.getUri()) ? ImageManager.ENTITY_MEMO
											: ImageManager.ENTITY;
								}
							}
						}));

		this.createColumn("Fingerprints", 300).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new ILabelProviderService.StyledColumnLabelProvider() {
							@Override
							public StyledString getStyledText(URI uri)
									throws Exception {
								ILocatable locatable = locatorService.resolve(
										uri, null).get();

								Entity entity = (Entity) locatable;
								List<Fingerprint> secondaryFingerprints = entity
										.getFingerprints();
								StyledString styledString = new StyledString(
										(secondaryFingerprints != null) ? StringUtils
												.join(secondaryFingerprints,
														", ") : "", boldObjects
												.contains(uri) ? boldStyler
												: null);
								return styledString;
							}
						}));

		this.createColumn("Token", 45).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new ILabelProviderService.StyledColumnLabelProvider() {
							@Override
							public StyledString getStyledText(URI uri)
									throws Exception {
								ILocatable locatable = locatorService.resolve(
										uri, null).get();

								Entity entity = (Entity) locatable;
								Token token = entity.getToken();
								StyledString styledString = new StyledString(
										(token != null) ? token.toString() : "",
										boldObjects.contains(uri) ? boldStyler
												: null);
								return styledString;
							}
						}));

		this.createColumn("OS", 100).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new ILabelProviderService.StyledColumnLabelProvider() {
							@Override
							public StyledString getStyledText(URI uri)
									throws Exception {
								ILocatable locatable = locatorService.resolve(
										uri, null).get();

								Entity entity = (Entity) locatable;
								StatsFile statsFile = entity.getStatsFile();
								StyledString styledString = new StyledString(
										(statsFile != null) ? statsFile
												.getPlatformLong() : "",
										boldObjects.contains(uri) ? boldStyler
												: null);
								return styledString;
							}
						}));

		this.createColumn("Generator", 100).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new ILabelProviderService.StyledColumnLabelProvider() {
							@Override
							public StyledString getStyledText(URI uri)
									throws Exception {
								ILocatable locatable = locatorService.resolve(
										uri, null).get();

								Entity entity = (Entity) locatable;
								CMakeCacheFile cMakeCacheFile = entity
										.getCMakeCacheFile();
								StyledString styledString = new StyledString(
										(cMakeCacheFile != null) ? cMakeCacheFile
												.getGenerator() : "",
										boldObjects.contains(uri) ? boldStyler
												: null);
								return styledString;
							}
						}));

		this.createColumn("Earliest Entry", 180).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new ILabelProviderService.StyledColumnLabelProvider() {
							@Override
							public StyledString getStyledText(URI uri)
									throws Exception {
								ILocatable locatable = locatorService.resolve(
										uri, null).get();

								Entity entity = (Entity) locatable;
								TimeZoneDate earliestDate = entity
										.getEarliestEntryDate();
								StyledString styledString = new StyledString(
										(earliestDate != null) ? earliestDate
												.format(EntityViewer.this.preferenceUtil
														.getDateFormat())
												: "",
										boldObjects.contains(uri) ? boldStyler
												: null);
								return styledString;
							}
						}));

		this.createColumn("Latest Entry", 180).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new ILabelProviderService.StyledColumnLabelProvider() {
							@Override
							public StyledString getStyledText(URI uri)
									throws Exception {
								ILocatable locatable = locatorService.resolve(
										uri, null).get();

								Entity entity = (Entity) locatable;
								TimeZoneDate lastestDate = entity
										.getLatestEntryDate();
								StyledString styledString = new StyledString(
										(lastestDate != null) ? lastestDate
												.format(EntityViewer.this.preferenceUtil
														.getDateFormat())
												: "",
										boldObjects.contains(uri) ? boldStyler
												: null);
								return styledString;
							}
						}));
	}

	@Override
	public void setBold(Collection<URI> boldObjects) {
		if (boldObjects == null) {
			boldObjects = new LinkedList<URI>();
		}
		if (!this.boldObjects.equals(boldObjects)) {
			List<Object> objectsToBeUpdated = new ArrayList<Object>();
			objectsToBeUpdated.addAll(this.boldObjects);
			objectsToBeUpdated.addAll(boldObjects);

			this.boldObjects = boldObjects;
			this.update(objectsToBeUpdated.toArray(), null);
		}
	}
}
