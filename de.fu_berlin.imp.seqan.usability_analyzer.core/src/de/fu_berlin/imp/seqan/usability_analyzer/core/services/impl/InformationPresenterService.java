package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wb.swt.SWTResourceManager;

import com.bkahlert.devel.nebula.utils.information.ISubjectInformationProvider;
import com.bkahlert.devel.nebula.utils.information.ITypedInformationControlCreator;
import com.bkahlert.devel.nebula.utils.information.TypedInformationControl;
import com.bkahlert.devel.nebula.utils.information.TypedInformationControlManager;
import com.bkahlert.devel.nebula.widgets.RoundedComposite;
import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite;
import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite.IllustratedText;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IInformationPresenterService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IInformationPresenterService.IInformationLabelProvider.IDetailEntry;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.SWTUtil;

public class InformationPresenterService implements
		IInformationPresenterService {

	public static class LocatableInformationControl<T extends ILocatable>
			extends TypedInformationControl<T> {

		private static final int borderWidth = 5;

		private ILabelProviderService labelProviderService = (ILabelProviderService) PlatformUI
				.getWorkbench().getService(ILabelProviderService.class);

		private Composite composite;
		private RoundedComposite metaComposite;
		private Composite customComposite;
		private RoundedComposite detailComposite;

		public LocatableInformationControl(Shell parentShell, boolean resizable) {
			super(parentShell, resizable);
		}

		@Override
		protected void createContent(Composite composite) {
			this.composite = composite;
			this.composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
			GridLayout gridLayout = new GridLayout(2, false);
			gridLayout.marginWidth = borderWidth;
			gridLayout.marginHeight = borderWidth;
			gridLayout.horizontalSpacing = borderWidth;
			this.composite.setLayout(gridLayout);

			this.metaComposite = new RoundedComposite(composite, SWT.BORDER);
			this.metaComposite.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM,
					false, false, 1, 1));
			this.metaComposite.setBackground(SWTResourceManager
					.getColor(SWT.COLOR_LIST_BACKGROUND));
			this.metaComposite.setLayout(RowLayoutFactory.fillDefaults()
					.margins(7, 3).type(SWT.VERTICAL).spacing(3).create());

			this.customComposite = new Composite(composite, SWT.NONE);
			this.customComposite.setLayoutData(GridDataFactory.swtDefaults()
					.span(1, 2).create());
			this.customComposite.setLayout(new FillLayout());

			this.detailComposite = new RoundedComposite(composite, SWT.BORDER);
			this.detailComposite.setBackground(SWTResourceManager
					.getColor(SWT.COLOR_LIST_BACKGROUND));
			this.detailComposite.setLayout(new GridLayout(2, false));
			this.detailComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
					false, false, 1, 1));

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
		public boolean setTypedInput(T input) {
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
			Color backgroundColor = informationLabelProvider
					.getBackground(input);

			// TODO background color = gelb
			/*
			 * TODO etwas in einen interceptor programmieren, der nochmal die
			 * background-color setzen kann TODO actions hinzufügen TODO focus
			 * geben können FIXME boolean isIntersected = false;
			 * 
			 * if (element instanceof HasDateRange) { TimeZoneDateRange
			 * dateRange = ((HasDateRange) element) .getDateRange();
			 * 
			 * // FIXME // if (timeline.getDecorators() != null) { // for
			 * (IDecorator t : timeline.getDecorators()) { // if (new
			 * TimeZoneDateRange( // t.getStartDate() != null ? new
			 * TimeZoneDate( // t.getStartDate()) : null, // t.getEndDate() !=
			 * null ? new TimeZoneDate(t // .getEndDate()) : null) //
			 * .isIntersected(dateRange)) { // isIntersected = true; // break;
			 * // } // } // }
			 * 
			 * return isIntersected ? Activator.COLOR_HIGHLIGHT :
			 * Activator.COLOR_STANDARD; }
			 * 
			 * return Activator.COLOR_STANDARD;
			 */
			// this.composite.setBackground(backgroundColor);
			// this.composite.getParent().setBackground(backgroundColor);

			if (backgroundColor == null) {
				backgroundColor = SWTResourceManager
						.getColor(SWT.COLOR_INFO_BACKGROUND);
			}
			this.composite.setBackground(backgroundColor);

			this.loadMetaInformation(metaInformation);
			this.loadDetailInformation(detailInformation);

			SWTUtil.clearControl(this.customComposite);
			informationLabelProvider.fillInformation(input,
					this.customComposite);
			this.customComposite.layout();

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
				TimeZoneDateRange dateRange = ((HasDateRange) event)
						.getDateRange();
				TimeZoneDate center = dateRange != null ? dateRange
						.getStartDate() != null ? dateRange.getStartDate()
						: dateRange.getEndDate() : null;
				if (center != null) {
					// this.timeline.setCenterVisibleDate(center.getCalendar());
				}
			}
		}

		public void loadMetaInformation(List<IllustratedText> metaInformation) {
			SWTUtil.clearControl(this.metaComposite);
			for (IllustratedText metaEntry : metaInformation) {
				SimpleIllustratedComposite metaCompositeEntry = new SimpleIllustratedComposite(
						this.metaComposite, SWT.CENTER | SWT.BOLD);
				metaCompositeEntry.setBackground(this.metaComposite
						.getBackground());
				metaCompositeEntry.setSpacing(3);
				metaCompositeEntry.setContent(metaEntry);
			}
			this.metaComposite.layout();
		}

		public void loadDetailInformation(List<IDetailEntry> detailInformation) {
			SWTUtil.clearControl(this.detailComposite);
			for (Entry<String, String> detailEntry : detailInformation) {
				Label keyLabel = new Label(this.detailComposite, SWT.NONE);
				keyLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,
						false));
				keyLabel.setText(detailEntry.getKey());

				Label valueLabel = new Label(this.detailComposite, SWT.NONE);
				valueLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,
						false));
				valueLabel.setText(detailEntry.getValue());
			}
			this.detailComposite.layout();
		}

		public void nextScreenshot() {
			// Object successor =
			// this.timeline.getSuccessor(this.event);
			// this.load(successor);
			// this.centerOnEvent(this.event);
		}

		public void prevScreenshot() {
			// Object predecessor =
			// this.timeline.getPredecessor(this.event);
			// this.load(predecessor);
			// this.centerOnEvent(this.event);
		}

		public void openURL(String url) {
			if (url != null) {
				org.eclipse.swt.program.Program.launch(url);
			}
		}
	}

	private Map<Control, TypedInformationControlManager<?, ?>> informationControlManagers = new HashMap<Control, TypedInformationControlManager<?, ?>>();

	private ITypedInformationControlCreator<ILocatable> informationControlCreator = new ITypedInformationControlCreator<ILocatable>() {
		@Override
		public TypedInformationControl<ILocatable> createInformationControl(
				Shell parent) {
			return new LocatableInformationControl<ILocatable>(parent, false);
		}
	};

	@Override
	public <CONTROL extends Control> void enable(
			CONTROL control,
			ISubjectInformationProvider<CONTROL, ILocatable> subjectInformationProvider) {
		TypedInformationControlManager<CONTROL, ILocatable> informationControlManager = new TypedInformationControlManager<CONTROL, ILocatable>(
				this.informationControlCreator, subjectInformationProvider);
		informationControlManager.install(control);
		this.informationControlManagers.put(control, informationControlManager);
	}

	@Override
	public <CONTROL extends Control> void disable(CONTROL control) {
		if (this.informationControlManagers.containsKey(control)) {
			this.informationControlManagers.get(control).dispose();
			this.informationControlManagers.remove(control);
		}
	};

}
