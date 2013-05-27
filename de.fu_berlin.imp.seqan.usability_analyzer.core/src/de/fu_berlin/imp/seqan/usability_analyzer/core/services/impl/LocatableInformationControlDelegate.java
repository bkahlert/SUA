package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite;
import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite.IllustratedText;
import com.bkahlert.nebula.information.EnhanceableInformationControl.Delegate;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IInformationPresenterService.IInformationLabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IInformationPresenterService.IInformationLabelProvider.IDetailEntry;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.SWTUtil;

/**
 * Instances of this class are created by {@link LocatableInformationControl}
 * and creates the controls needed to display the loaded data.
 * 
 * @author bkahlert
 * 
 * @param <T>
 */
public class LocatableInformationControlDelegate<T extends ILocatable>
		implements Delegate<T> {
	private ILabelProviderService labelProviderService = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);
	private Composite l0;
	private Composite l1Left;
	private Composite l2TitleArea;
	private Composite l2CustomComposite;
	private Label l2CustomCompositeSeparator;
	private Composite l2DetailComposite;
	private LocatableInformationControl.IPostProcessor<T> postProcessor = null;

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

		this.l2TitleArea = new Composite(this.l0, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false)
				.applyTo(this.l2TitleArea);

		this.addSeparator();

		this.l2CustomComposite = new Composite(this.l0, SWT.NONE);
		this.l2CustomComposite.setLayoutData(GridDataFactory.fillDefaults()
				.create());
		this.l2CustomComposite.setLayout(new FillLayout());

		this.l2CustomCompositeSeparator = this.addSeparator();

		this.l2DetailComposite = new Composite(this.l0, SWT.NONE);
		this.l2DetailComposite.setLayout(new GridLayout(2, false));
		this.l2DetailComposite.setLayoutData(GridDataFactory.fillDefaults()
				.create());

		return l0;
	}

	@Override
	public boolean load(T input, ToolBarManager toolBarManager) {
		ILabelProvider labelProvider = this.labelProviderService
				.getLabelProvider(input);
		if (labelProvider == null) {
			return false;
		}

		if (!IInformationLabelProvider.class.isInstance(labelProvider)) {
			return false;
		}

		final IInformationLabelProvider informationLabelProvider = (IInformationLabelProvider) labelProvider;
		if (!informationLabelProvider.hasInformation(input)) {
			return false;
		}

		List<IllustratedText> metaInformation = informationLabelProvider
				.getMetaInformation(input);

		List<IDetailEntry> detailInformation = informationLabelProvider
				.getDetailInformation(input);

		this.loadMetaInformation(metaInformation);
		this.loadDetailInformation(detailInformation);

		SWTUtil.clearControl(this.l2CustomComposite);
		Control control = informationLabelProvider.fillInformation(input,
				this.l2CustomComposite);
		this.l2CustomComposite.layout();
		this.l2CustomCompositeSeparator.setVisible(control != null);

		if (toolBarManager != null) {
			informationLabelProvider.fill(input, toolBarManager);
		}

		if (this.postProcessor != null) {
			this.postProcessor.postProcess(input, this.l1Left);
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
		SWTUtil.clearControl(this.l2TitleArea);
		this.l2TitleArea.setLayout(RowLayoutFactory.fillDefaults()
				.type(SWT.HORIZONTAL).margins(7, 3).spacing(3).create());
		for (IllustratedText metaEntry : metaInformation) {
			SimpleIllustratedComposite metaCompositeEntry = new SimpleIllustratedComposite(
					this.l2TitleArea, SWT.CENTER | SWT.BOLD);

			metaCompositeEntry.setBackgroundMode(SWT.INHERIT_DEFAULT);
			metaCompositeEntry.setSpacing(3);
			metaCompositeEntry.setContent(metaEntry);
		}
		this.l2TitleArea.layout();
	}

	public void loadDetailInformation(List<IDetailEntry> detailInformation) {
		SWTUtil.clearControl(this.l2DetailComposite);
		for (Entry<String, String> detailEntry : detailInformation) {
			Label keyLabel = new Label(this.l2DetailComposite, SWT.NONE);
			keyLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			keyLabel.setText(detailEntry.getKey());

			Label valueLabel = new Label(this.l2DetailComposite, SWT.NONE);
			valueLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,
					false));
			valueLabel.setText(detailEntry.getValue());
		}
		this.l2DetailComposite.layout();
	}

	public void setPostProcessor(
			LocatableInformationControl.IPostProcessor<T> postProcessor) {
		this.postProcessor = postProcessor;
	}
}