package de.fu_berlin.imp.apiua.entity.viewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.DNDUtils;
import com.bkahlert.nebula.utils.DNDUtils.Oracle;
import com.bkahlert.nebula.utils.DistributionUtils.AbsoluteWidth;
import com.bkahlert.nebula.utils.DistributionUtils.RelativeWidth;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.Stylers;
import com.bkahlert.nebula.viewer.SortableTableViewer;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.apiua.core.model.identifier.Fingerprint;
import de.fu_berlin.imp.apiua.core.model.identifier.ID;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.core.model.identifier.Token;
import de.fu_berlin.imp.apiua.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.apiua.core.services.DataServiceAdapter;
import de.fu_berlin.imp.apiua.core.services.IDataService;
import de.fu_berlin.imp.apiua.core.services.IDataServiceListener;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorService;
import de.fu_berlin.imp.apiua.core.services.location.URIUtils;
import de.fu_berlin.imp.apiua.core.ui.viewer.IBoldViewer;
import de.fu_berlin.imp.apiua.entity.model.Entity;
import de.fu_berlin.imp.apiua.entity.ui.ImageManager;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.ui.EpisodeRenderer;
import de.fu_berlin.imp.apiua.stats.model.CMakeCacheFile;
import de.fu_berlin.imp.apiua.stats.model.StatsFile;

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

	private Collection<URI> boldObjects = new LinkedList<URI>();

	public EntityViewer(Table table) {
		super(table);

		table.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.create());
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		this.createColumns();

		DNDUtils.addLocalDragSupport(this, new Oracle() {
			@Override
			public boolean allowDND() {
				return EntityViewer.this.getControl().getData(
						EpisodeRenderer.CONTROL_DATA_STRING) == null;
			}
		}, URI.class);

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

		this.createColumn("ID", new RelativeWidth(.35, 170)).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new ILabelProviderService.StyledLabelProvider() {

							@Override
							public StyledString getStyledText(URI uri)
									throws Exception {
								ILocatable locatable = locatorService.resolve(
										uri, null).get();

								Entity entity = (Entity) locatable;
								if (entity == null) {
									throw new RuntimeException("No entity for "
											+ uri + " found.");
								}

								ID id = entity.getId();
								if (id == null) {
									return new StyledString("");
								}

								StyledString styledString = new StyledString(
										id.toString(),
										(EntityViewer.this.boldObjects
												.contains(uri) ? Stylers.BOLD_STYLER
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
										IIdentifier identifier = URIUtils
												.getIdentifier(id);
										if (identifier instanceof ID
												&& entity.getId() != null
												&& entity.getId().equals(
														identifier)) {
											return EntityViewer.this.codeService
													.isMemo(entity.getUri()) ? ImageManager.ENTITY_PARTIALLY_CODED_MEMO
													: ImageManager.ENTITY_PARTIALLY_CODED;
										}
										if (identifier instanceof Fingerprint
												&& entity.getFingerprints()
														.contains(identifier)) {
											return EntityViewer.this.codeService
													.isMemo(entity.getUri()) ? ImageManager.ENTITY_PARTIALLY_CODED_MEMO
													: ImageManager.ENTITY_PARTIALLY_CODED;
										}
									}

									return EntityViewer.this.codeService
											.isMemo(entity.getUri()) ? ImageManager.ENTITY_MEMO
											: ImageManager.ENTITY;
								}
							}
						}));

		this.createColumn("Fingerprints", new RelativeWidth(.65, 300))
				.setLabelProvider(
						new DelegatingStyledCellLabelProvider(
								new ILabelProviderService.StyledLabelProvider() {
									@Override
									public StyledString getStyledText(URI uri)
											throws Exception {
										ILocatable locatable = locatorService
												.resolve(uri, null).get();

										Entity entity = (Entity) locatable;
										List<Fingerprint> secondaryFingerprints = entity
												.getFingerprints();
										StyledString styledString = new StyledString(
												(secondaryFingerprints != null) ? StringUtils
														.join(secondaryFingerprints,
																", ")
														: "",
												EntityViewer.this.boldObjects
														.contains(uri) ? Stylers.BOLD_STYLER
														: null);
										return styledString;
									}
								}));

		this.createColumn("Token", new AbsoluteWidth(45)).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new ILabelProviderService.StyledLabelProvider() {
							@Override
							public StyledString getStyledText(URI uri)
									throws Exception {
								ILocatable locatable = locatorService.resolve(
										uri, null).get();

								Entity entity = (Entity) locatable;
								Token token = entity.getToken();
								StyledString styledString = new StyledString(
										(token != null) ? token.toString() : "",
										EntityViewer.this.boldObjects
												.contains(uri) ? Stylers.BOLD_STYLER
												: null);
								return styledString;
							}
						}));

		this.createColumn("Earliest Entry", new AbsoluteWidth(0))
				.setLabelProvider(
						new DelegatingStyledCellLabelProvider(
								new ILabelProviderService.StyledLabelProvider() {
									@Override
									public StyledString getStyledText(URI uri)
											throws Exception {
										ILocatable locatable = locatorService
												.resolve(uri, null).get();

										Entity entity = (Entity) locatable;
										TimeZoneDate earliestDate = entity
												.getEarliestEntryDate();
										StyledString styledString = new StyledString(
												(earliestDate != null) ? earliestDate
														.format(EntityViewer.this.preferenceUtil
																.getDateFormat())
														: "",
												EntityViewer.this.boldObjects
														.contains(uri) ? Stylers.BOLD_STYLER
														: null);
										return styledString;
									}
								}));

		this.createColumn("Latest Entry", new AbsoluteWidth(0))
				.setLabelProvider(
						new DelegatingStyledCellLabelProvider(
								new ILabelProviderService.StyledLabelProvider() {
									@Override
									public StyledString getStyledText(URI uri)
											throws Exception {
										ILocatable locatable = locatorService
												.resolve(uri, null).get();

										Entity entity = (Entity) locatable;
										TimeZoneDate lastestDate = entity
												.getLatestEntryDate();
										StyledString styledString = new StyledString(
												(lastestDate != null) ? lastestDate
														.format(EntityViewer.this.preferenceUtil
																.getDateFormat())
														: "",
												EntityViewer.this.boldObjects
														.contains(uri) ? Stylers.BOLD_STYLER
														: null);
										return styledString;
									}
								}));

		this.createColumn("OS", new AbsoluteWidth(160)).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new ILabelProviderService.StyledLabelProvider() {
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
										EntityViewer.this.boldObjects
												.contains(uri) ? Stylers.BOLD_STYLER
												: null);
								return styledString;
							}
						}));

		this.createColumn("Generator", new AbsoluteWidth(210))
				.setLabelProvider(
						new DelegatingStyledCellLabelProvider(
								new ILabelProviderService.StyledLabelProvider() {
									@Override
									public StyledString getStyledText(URI uri)
											throws Exception {
										ILocatable locatable = locatorService
												.resolve(uri, null).get();

										Entity entity = (Entity) locatable;
										CMakeCacheFile cMakeCacheFile = entity
												.getCMakeCacheFile();
										StyledString styledString = new StyledString(
												(cMakeCacheFile != null) ? cMakeCacheFile
														.getGenerator() : "",
												EntityViewer.this.boldObjects
														.contains(uri) ? Stylers.BOLD_STYLER
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
			final List<Object> objectsToBeUpdated = new ArrayList<Object>();
			objectsToBeUpdated.addAll(this.boldObjects);
			objectsToBeUpdated.addAll(boldObjects);

			this.boldObjects = boldObjects;
			ExecUtils.asyncExec(new Runnable() {

				@Override
				public void run() {
					EntityViewer.this.update(objectsToBeUpdated.toArray(), null);
				}
			});
		}
	}
}
