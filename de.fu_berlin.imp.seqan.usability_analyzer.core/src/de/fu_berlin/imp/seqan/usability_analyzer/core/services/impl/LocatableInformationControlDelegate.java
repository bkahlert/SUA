package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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

public class LocatableInformationControlDelegate<T extends ILocatable>
		implements Delegate<T> {
	private ILabelProviderService labelProviderService = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);
	private Composite composite;
	private Composite titleArea;
	private Composite customComposite;
	private Label customCompositeSeparator;
	private Composite detailComposite;
	private LocatableInformationControl.IPostProcessor<T> postProcessor = null;

	@Override
	public void build(Composite parent) {
		this.composite = parent;
		this.composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(this.composite);

		this.titleArea = new Composite(this.composite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false)
				.applyTo(this.titleArea);

		this.addSeparator();

		this.customComposite = new Composite(this.composite, SWT.NONE);
		this.customComposite.setLayoutData(GridDataFactory.swtDefaults()
				.create());
		this.customComposite.setLayout(new FillLayout());

		this.customCompositeSeparator = this.addSeparator();

		this.detailComposite = new Composite(this.composite, SWT.NONE);
		this.detailComposite.setLayout(new GridLayout(2, false));
		this.detailComposite.setLayoutData(GridDataFactory.fillDefaults()
				.create());

		this.composite.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_RIGHT) {
					// TimelineDetailDialog.this.nextScreenshot();
				} else if (e.keyCode == SWT.ARROW_LEFT) {
					// TimelineDetailDialog.this.prevScreenshot();
				} else if (e.keyCode == SWT.ESC || e.keyCode == SWT.CR) {
					// TimelineDetailDialog.this.close();
				}
			}
		});
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

		SWTUtil.clearControl(this.customComposite);
		Control control = informationLabelProvider.fillInformation(input,
				this.customComposite);
		this.customComposite.layout();
		this.customCompositeSeparator.setVisible(control != null);

		if (toolBarManager != null) {
			informationLabelProvider.fill(input, toolBarManager);
		}

		if (this.postProcessor != null) {
			this.postProcessor.postProcess(input, this.detailComposite);
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
		Label separator = new Label(this.composite, SWT.SEPARATOR
				| SWT.HORIZONTAL);
		separator.setLayoutData(GridDataFactory.fillDefaults()
				.align(SWT.FILL, SWT.CENTER).create());
		return separator;
	}

	public void loadMetaInformation(List<IllustratedText> metaInformation) {
		SWTUtil.clearControl(this.titleArea);
		this.titleArea.setLayout(RowLayoutFactory.fillDefaults()
				.type(SWT.HORIZONTAL).margins(7, 3).spacing(3).create());
		for (IllustratedText metaEntry : metaInformation) {
			SimpleIllustratedComposite metaCompositeEntry = new SimpleIllustratedComposite(
					this.titleArea, SWT.CENTER | SWT.BOLD);
			// metaCompositeEntry.setBackground(this.titleArea.getBackground());

			metaCompositeEntry.setBackgroundMode(SWT.INHERIT_DEFAULT);
			metaCompositeEntry.setSpacing(3);
			metaCompositeEntry.setContent(metaEntry);
		}
		this.titleArea.layout();
	}

	public void loadDetailInformation(List<IDetailEntry> detailInformation) {
		SWTUtil.clearControl(this.detailComposite);
		for (Entry<String, String> detailEntry : detailInformation) {
			Label keyLabel = new Label(this.detailComposite, SWT.NONE);
			keyLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			keyLabel.setText(detailEntry.getKey());

			Label valueLabel = new Label(this.detailComposite, SWT.NONE);
			valueLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,
					false));
			valueLabel.setText(detailEntry.getValue());
		}
		this.detailComposite.layout();
	}

	public void setPostProcessor(
			LocatableInformationControl.IPostProcessor<T> postProcessor) {
		this.postProcessor = postProcessor;
	}
}