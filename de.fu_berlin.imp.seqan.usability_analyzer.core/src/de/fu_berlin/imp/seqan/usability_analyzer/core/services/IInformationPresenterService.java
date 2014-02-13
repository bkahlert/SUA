package de.fu_berlin.imp.seqan.usability_analyzer.core.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite.IllustratedText;
import com.bkahlert.nebula.information.ISubjectInformationProvider;
import com.bkahlert.nebula.information.InformationControlManager;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService.ILabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService.LabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService.StyledLabelProvider;

/**
 * This service can display {@link PopupDialog}s that show detailed information
 * about the requested object.
 * 
 * @author bkahlert
 */
public interface IInformationPresenterService<INFORMATION> {

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
	 * Instances of this class can provide callers of
	 * {@link ILabelProviderService#getLabelProvider(URI) with further information.
	 * @author bkahlert
	 */
	public static interface IInformationLabelProvider<INFORMATION> extends
			ILabelProvider {

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
		 * @param object
		 * @return
		 */
		public boolean hasInformation(INFORMATION object) throws Exception;

		public List<IllustratedText> getMetaInformation(INFORMATION object)
				throws Exception;

		public List<IDetailEntry> getDetailInformation(INFORMATION object)
				throws Exception;

		/**
		 * Fills the given {@link Composite} with detailed information.
		 * 
		 * @param uri
		 * @param composite
		 * @return the main control; null if no popup should be displayed.
		 */
		public Control fillInformation(INFORMATION object, Composite composite)
				throws Exception;

		/**
		 * Fills the given {@link ToolBarManager} with custom
		 * {@link IContributionItem}s and {@link IAction}s.
		 * 
		 * @param uri
		 * @param toolBarManager
		 */
		public void fill(INFORMATION object, ToolBarManager toolBarManager)
				throws Exception;

	}

	/**
	 * Default implemention of {@link IInformationLabelProvider}.
	 * 
	 * @author bkahlert
	 * 
	 */
	public static class InformationLabelProvider<INFORMATION> extends
			LabelProvider implements IInformationLabelProvider<INFORMATION> {

		@Override
		public boolean hasInformation(INFORMATION uri) throws Exception {
			return false;
		}

		@Override
		public Control fillInformation(INFORMATION uri, Composite composite)
				throws Exception {
			return null;
		}

		@Override
		public List<IllustratedText> getMetaInformation(INFORMATION uri)
				throws Exception {
			return new ArrayList<IllustratedText>();
		}

		@Override
		public List<IDetailEntry> getDetailInformation(INFORMATION uri)
				throws Exception {
			return new ArrayList<IDetailEntry>();
		}

		@Override
		public void fill(INFORMATION uri, ToolBarManager toolBarManager)
				throws Exception {
			return;
		}

	}

	/**
	 * Default implemention of {@link IInformationLabelProvider}.
	 * 
	 * @author bkahlert
	 * 
	 */
	public static class StyledInformationLabelProvider<INFORMATION> extends
			StyledLabelProvider implements
			IInformationLabelProvider<INFORMATION> {

		@Override
		public boolean hasInformation(INFORMATION uri) throws Exception {
			return false;
		}

		@Override
		public Control fillInformation(INFORMATION uri, Composite composite)
				throws Exception {
			return null;
		}

		@Override
		public List<IllustratedText> getMetaInformation(INFORMATION uri)
				throws Exception {
			return new ArrayList<IllustratedText>();
		}

		@Override
		public List<IDetailEntry> getDetailInformation(INFORMATION uri)
				throws Exception {
			return new ArrayList<IDetailEntry>();
		}

		@Override
		public void fill(INFORMATION uri, ToolBarManager toolBarManager)
				throws Exception {
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
	 * Installs a new {@link InformationControlManager} on the given control.
	 * <p>
	 * Every time the mouse stops moving the following steps are done:
	 * <ol>
	 * <li>the given {@link ISubjectInformationProvider} is requested to provide
	 * an object</li>
	 * <li>if an object is returned the {@link IInformationPresenterService}
	 * uses the {@link ILabelProviderService} to retrieve a
	 * {@link ILabelProvider} that can provide the corresponding information</li>
	 * <li>if a responsible {@link ILabelProvider} can provide information a
	 * popup displaying these information is shown</li>
	 * </ol>
	 * 
	 * @param control
	 * @param informationClass
	 * @param subjectInformationProvider
	 * @return the generated {@link InformationControlManager}
	 */
	public <CONTROL extends Control> InformationControlManager<CONTROL, INFORMATION> enable(
			CONTROL control,
			Class<INFORMATION> informationClass,
			ISubjectInformationProvider<CONTROL, INFORMATION> subjectInformationProvider);

	/**
	 * Deinstalls an the installed {@link InformationControlManager} from the
	 * given control.
	 * 
	 * @param control
	 */
	public <CONTROL extends Control> void disable(CONTROL control);

}
