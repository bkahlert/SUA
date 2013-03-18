package de.fu_berlin.imp.seqan.usability_analyzer.core.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.bkahlert.devel.nebula.utils.information.ISubjectInformationProvider;
import com.bkahlert.devel.nebula.utils.information.InformationControl;
import com.bkahlert.devel.nebula.utils.information.InformationControlManager;
import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite.IllustratedText;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;

/**
 * This service can display {@link PopupDialog}s that show detailed information
 * about the requested object.
 * 
 * @author bkahlert
 */
public interface IInformationPresenterService {

	/**
	 * Instances of this class provide a background color for the popup
	 * information window.
	 * 
	 * @author bkahlert
	 */
	public static interface IInformationBackgroundProvider {
		public Color getBackground(Object element);
	}

	/**
	 * Instances of this class provide contributions for the
	 * {@link ToolBarManager}.
	 * 
	 * @author bkahlert
	 */
	public static interface IInformationToolBarContributionsProvider {
		public void fill(Object element, ToolBarManager toolBarManager,
				InformationControl<?> informationControl,
				InformationControlManager<?, ?> informationControlManager);
	}

/**
	 * Instances of this class can provide callers of
	 * {@link ILabelProviderService#getLabelProvider(ILocatable) with further information.
	 * @author bkahlert
	 */
	public static interface IInformationLabelProvider extends ILabelProvider {

		public static interface IDetailEntry extends Map.Entry<String, String> {
		}

		public static class DetailEntry implements IDetailEntry {
			private final String key;
			private String value;

			public DetailEntry(String key, String value) {
				super();
				this.key = key;
				this.value = value;
			}

			@Override
			public String getKey() {
				return key;
			}

			@Override
			public String getValue() {
				return value;
			}

			@Override
			public String setValue(String value) {
				String old = this.value;
				this.value = value;
				return old;
			}
		}

		/**
		 * Returns true if this {@link IInformationLabelProvider} can provide
		 * further information for the given element.
		 * 
		 * @param element
		 * @return
		 */
		public boolean hasInformation(Object element);

		public List<IllustratedText> getMetaInformation(Object element);

		public List<IDetailEntry> getDetailInformation(Object element);

		/**
		 * Fills the given {@link Composite} with detailed information.
		 * 
		 * @param element
		 * @param composite
		 * @return the main control; null if no popup should be displayed.
		 */
		public Control fillInformation(Object element, Composite composite);

		/**
		 * Fills the given {@link ToolBarManager} with custom
		 * {@link IContributionItem}s and {@link IAction}s.
		 * 
		 * @param element
		 * @param toolBarManager
		 */
		public void fill(Object element, ToolBarManager toolBarManager);

	}

	/**
	 * Default implemention of {@link IInformationLabelProvider}.
	 * 
	 * @author bkahlert
	 * 
	 */
	public static class InformationLabelProvider extends LabelProvider
			implements IInformationLabelProvider {

		@Override
		public boolean hasInformation(Object element) {
			return false;
		}

		@Override
		public Control fillInformation(Object element, Composite composite) {
			return null;
		}

		@Override
		public List<IllustratedText> getMetaInformation(Object element) {
			return new ArrayList<IllustratedText>();
		}

		@Override
		public List<IDetailEntry> getDetailInformation(Object element) {
			return new ArrayList<IDetailEntry>();
		}

		@Override
		public void fill(Object element, ToolBarManager toolBarManager) {
			return;
		}

	}

	/**
	 * Adds a {@link IInformationBackgroundProvider} to the pool of consulted
	 * {@link IInformationBackgroundProvider}s.
	 * <p>
	 * The added {@link IInformationBackgroundProvider} works independently of
	 * the {@link IInformationLabelProvider} used to determine the popup's
	 * content.
	 * 
	 * @param informationBackgroundProvider
	 */
	public void addInformationBackgroundProvider(
			IInformationBackgroundProvider informationBackgroundProvider);

	/**
	 * Removes a {@link IInformationBackgroundProvider} from the pool of
	 * consulted {@link IInformationBackgroundProvider}s.
	 * 
	 * @param informationBackgroundProvider
	 */
	public void removeInformationBackgroundProvider(
			IInformationBackgroundProvider informationBackgroundProvider);

	/**
	 * Adds a {@link IInformationToolBarContributionsProvider} to the pool of
	 * consulted {@link IInformationToolBarContributionsProvider}s.
	 * <p>
	 * The added {@link IInformationToolBarContributionsProvider} works
	 * independently of the {@link IInformationLabelProvider} used to determine
	 * the popup's content.
	 * 
	 * @param informationBackgroundProvider
	 */
	public void addInformationToolBarContributionProvider(
			IInformationToolBarContributionsProvider informationToolBarContributionsProvider);

	/**
	 * Removes a {@link IInformationToolBarContributionsProvider} from the pool
	 * of consulted {@link IInformationToolBarContributionsProvider}s.
	 * 
	 * @param informationBackgroundProvider
	 */
	public void removeInformationToolBarContributionProvider(
			IInformationToolBarContributionsProvider informationToolBarContributionsProvider);

	/**
	 * Installs a new {@link InformationControlManager} on the given control.
	 * <p>
	 * Every time the mouse stops moving the following steps are done:
	 * <ol>
	 * <li>the given {@link ISubjectInformationProvider} is requested to provide
	 * a {@link ILocatable}</li>
	 * <li>if a {@link ILocatable} is returned the
	 * {@link IInformationPresenterService} uses the
	 * {@link ILabelProviderService} to retrieve a {@link ILabelProvider} that
	 * can provide the corresponding information</li>
	 * <li>if a responsible {@link ILabelProvider} can provide information a
	 * popup displaying these information is shown</li>
	 * </ol>
	 * 
	 * @param control
	 * @param subjectInformationProvider
	 * @return the generated {@link InformationControlManager}
	 */
	public <CONTROL extends Control> InformationControlManager<CONTROL, ILocatable> enable(
			CONTROL control,
			ISubjectInformationProvider<CONTROL, ILocatable> subjectInformationProvider);

	/**
	 * Deinstalls an the installed {@link InformationControlManager} from the
	 * given control.
	 * 
	 * @param control
	 */
	public <CONTROL extends Control> void disable(CONTROL control);

}
