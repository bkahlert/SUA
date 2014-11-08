package de.fu_berlin.imp.apiua.core.services.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.information.EnhanceableInformationControl.Delegate;
import com.bkahlert.nebula.information.extender.IInformationControlExtender;
import com.bkahlert.nebula.utils.FontUtils;
import com.bkahlert.nebula.utils.SWTUtils;
import com.bkahlert.nebula.widgets.SimpleIllustratedComposite;
import com.bkahlert.nebula.widgets.SimpleIllustratedComposite.IllustratedText;

import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.IInformationPresenterService.IInformationLabelProvider.DetailEntry;
import de.fu_berlin.imp.apiua.core.services.IInformationPresenterService.IInformationLabelProvider.IDetailEntry;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService.ILabelProvider;
import de.fu_berlin.imp.apiua.core.services.IUriPresenterService.IUriLabelProvider;
import de.fu_berlin.imp.apiua.core.services.impl.UriInformationControl.IPostProcessor;
import de.fu_berlin.imp.apiua.core.ui.viewer.filters.HasDateRange;

/**
 * Instances of this class are created by {@link UriInformationControl} and
 * creates the controls needed to display the loaded data.
 * 
 * @author bkahlert
 * 
 * @param <T>
 */
public class UriInformationControlDelegate implements Delegate<URI> {

	private static final Logger LOGGER = Logger
			.getLogger(UriInformationControlDelegate.class);

	private final ILabelProviderService labelProviderService = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);

	/** top-level element of the information control */
	private Composite l0;

	/** small vertical bar to the left */
	private Composite l1Left;

	/** small title bar to the top */
	private Composite l1TitleArea;

	/** area the responsible label provider is supposed to fill */
	private Composite l1CustomComposite;

	/** separator between custom area and details */
	private Label l1CustomCompositeSeparator;

	/** area the responsible label provider fills up with detail information */
	private Composite l1DetailComposite;

	/** separator between detail area and extensions */
	@SuppressWarnings("unused")
	private Label l1ExtensionCompositeSeparator;

	/** area all interested {@link IInformationControlExtender may fill */
	private Composite l1ExtensionComposite;

	private IPostProcessor postProcessor = null;

	@Override
	public Composite build(Composite l0) {
		this.l0 = l0;

		l0.setLayout(GridLayoutFactory.fillDefaults().numColumns(3)
				.margins(0, 0).spacing(0, 0).create());
		l0.setBackgroundMode(SWT.INHERIT_DEFAULT);

		this.l1Left = new Composite(l0, SWT.NONE);
		this.l1Left.setBackgroundMode(SWT.INHERIT_DEFAULT);
		this.l1Left.setLayoutData(GridDataFactory.fillDefaults()
				.grab(false, true).hint(10, SWT.DEFAULT).span(1, 999).create());

		Label l1Middle = new Label(l0, SWT.SEPARATOR | SWT.VERTICAL);
		l1Middle.setLayoutData(GridDataFactory.fillDefaults().grab(false, true)
				.hint(1, SWT.DEFAULT).span(1, 999).create());

		this.l1TitleArea = new Composite(this.l0, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false)
				.applyTo(this.l1TitleArea);

		this.addSeparator();

		this.l1CustomComposite = new Composite(this.l0, SWT.NONE);
		this.l1CustomComposite.setLayoutData(GridDataFactory.fillDefaults()
				.create());
		this.l1CustomComposite.setLayout(new FillLayout());

		this.l1CustomCompositeSeparator = this.addSeparator();

		this.l1DetailComposite = new Composite(this.l0, SWT.NONE);
		this.l1DetailComposite.setLayoutData(GridDataFactory.fillDefaults()
				.create());

		this.l1ExtensionCompositeSeparator = this.addSeparator();

		this.l1ExtensionComposite = new Composite(this.l0, SWT.NONE);
		this.l1ExtensionComposite.setLayoutData(GridDataFactory.fillDefaults()
				.minSize(0, 0).create());
		this.l1ExtensionComposite.setLayout(GridLayoutFactory.fillDefaults()
				.numColumns(2).margins(0, 0).spacing(0, 0).create());

		return this.l1ExtensionComposite;
	}

	@Override
	public boolean load(URI uri, ToolBarManager toolBarManager) {
		ILabelProvider labelProvider = this.labelProviderService
				.getLabelProvider(uri);
		if (labelProvider == null) {
			return false;
		}

		if (!IUriLabelProvider.class.isInstance(labelProvider)) {
			LOGGER.warn(labelProvider.getClass().getSimpleName()
					+ " is not of type "
					+ IUriLabelProvider.class.getSimpleName());
			return false;
		}

		final IUriLabelProvider uriLabelProvider = (IUriLabelProvider) labelProvider;
		try {
			if (!uriLabelProvider.hasInformation(uri)) {
				return false;
			}
		} catch (Exception e) {
			LOGGER.error("Error while checking if " + uri
					+ " has additional information to be display in a popup");
			return false;
		}

		List<IllustratedText> metaInformation;
		try {
			metaInformation = uriLabelProvider.getMetaInformation(uri);
		} catch (Exception e) {
			LOGGER.error("Error while checking what additional meta information to be display in a popup for "
					+ uri);
			metaInformation = new LinkedList<IllustratedText>();
			metaInformation.add(new IllustratedText("ERROR"));
		}

		List<IDetailEntry> detailInformation;
		try {
			detailInformation = uriLabelProvider.getDetailInformation(uri);
		} catch (Exception e) {
			LOGGER.error("Error while checking what additional detailed information to be display in a popup for "
					+ uri);
			detailInformation = new LinkedList<IDetailEntry>();
			detailInformation.add(new DetailEntry("ERROR", "ERROR"));
		}

		SWTUtils.clearControl(this.l1CustomComposite);
		try {
			Control control = uriLabelProvider.fillInformation(uri,
					this.l1CustomComposite);
			this.l1CustomComposite.layout();
			this.l1CustomCompositeSeparator.setVisible(control != null);
		} catch (Exception e) {
			LOGGER.error("Error while customizing popup for " + uri);
		}

		this.loadMetaInformation(metaInformation);
		this.loadDetailInformation(detailInformation);

		if (toolBarManager != null) {
			try {
				uriLabelProvider.fill(uri, toolBarManager);
			} catch (Exception e) {
				LOGGER.error("Error while filling toolbar in a popup for "
						+ uri, e);
			}
		}

		if (this.postProcessor != null) {
			this.postProcessor.postProcess(uri, this.l1Left);
		}

		return true;
	}

	/**
	 * Centers the timeline so that is shows the start of the given end.
	 * <p>
	 * If the event has no start the end is used.
	 * <p>
	 * If the event is null or has no start nor end date, nothing happens.
	 * 
	 * @param event
	 */
	public void centerOnEvent(Object event) {
		if (event instanceof HasDateRange) {
			TimeZoneDateRange dateRange = ((HasDateRange) event).getDateRange();
			TimeZoneDate center = dateRange != null ? dateRange.getStartDate() != null ? dateRange
					.getStartDate() : dateRange.getEndDate()
					: null;
			if (center != null) {
				// this.timeline.setCenterVisibleDate(center.getCalendar());
			}
		}
	}

	private Label addSeparator() {
		Label separator = new Label(this.l0, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(GridDataFactory.fillDefaults().minSize(1, 1)
				.align(SWT.FILL, SWT.CENTER).create());
		return separator;
	}

	public void loadMetaInformation(List<IllustratedText> metaInformation) {
		SWTUtils.clearControl(this.l1TitleArea);
		this.l1TitleArea.setLayout(RowLayoutFactory.fillDefaults()
				.type(SWT.HORIZONTAL).margins(7, 3).spacing(3).create());
		for (IllustratedText metaEntry : metaInformation) {
			SimpleIllustratedComposite metaCompositeEntry = new SimpleIllustratedComposite(
					this.l1TitleArea, SWT.CENTER | SWT.BOLD);

			metaCompositeEntry.setBackgroundMode(SWT.INHERIT_DEFAULT);
			metaCompositeEntry.setSpacing(3);
			metaCompositeEntry.setContent(metaEntry);
		}
		this.l1TitleArea.layout();
	}

	public void loadDetailInformation(List<IDetailEntry> detailInformation) {
		SWTUtils.clearControl(this.l1DetailComposite);

		Point overallSize = this.l0.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point leftSize = this.l1Left.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point contentSize = new Point(overallSize.x - leftSize.x, overallSize.y
				- leftSize.y);

		int numCols = 1;
		int maxCols = 3;
		do {
			this.loadDetailInformation(detailInformation, numCols);
			numCols++;
		} while (this.l1DetailComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).x <= contentSize.x
				&& numCols <= maxCols);
		this.loadDetailInformation(detailInformation, numCols - 1);
	}

	public void loadDetailInformation(List<IDetailEntry> detailInformation,
			int numCols) {
		SWTUtils.clearControl(this.l1DetailComposite);
		this.l1DetailComposite.setLayout(new GridLayout(numCols * 2, false));

		for (Entry<String, String> detailEntry : detailInformation) {
			Label keyLabel = new Label(this.l1DetailComposite, SWT.NONE);
			FontUtils.makeBold(keyLabel);
			keyLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			keyLabel.setText(detailEntry.getKey());

			Label valueLabel = new Label(this.l1DetailComposite, SWT.WRAP);
			valueLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,
					false));
			valueLabel.setText(detailEntry.getValue());
		}

		this.l1DetailComposite.layout();
	}

	public void setPostProcessor(IPostProcessor postProcessor) {
		this.postProcessor = postProcessor;
	}
}