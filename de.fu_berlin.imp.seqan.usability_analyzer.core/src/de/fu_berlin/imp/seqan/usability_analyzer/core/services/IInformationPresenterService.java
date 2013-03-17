package de.fu_berlin.imp.seqan.usability_analyzer.core.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.bkahlert.devel.nebula.utils.information.ISubjectInformationProvider;
import com.bkahlert.devel.nebula.utils.information.TypedInformationControlManager;
import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite.IllustratedText;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;

/**
 * This service can display {@link PopupDialog}s that show detailed information
 * about the requested object.
 * 
 * @author bkahlert
 */
public interface IInformationPresenterService {

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
	 * Instances of this class can provide callers of
	 * {@link ILabelProviderService#getLabelProvider(ILocatable) with further information.
	 * @author bkahlert
	 *
	 */
	public static interface IInformationLabelProvider extends ILabelProvider {
		/**
		 * Returns true if this {@link IInformationLabelProvider} can provide
		 * further information for the given element.
		 * 
		 * @param element
		 * @return
		 */
		public boolean hasInformation(Object element);

		public List<IllustratedText> getMetaInformation(Object element);

		public List<Map.Entry<String, String>> getDetailInformation(
				Object element);

		/**
		 * Fills the given {@link Composite} with detailed information.
		 * 
		 * @param element
		 * @param composite
		 * @return the main control; null if no popup should be displayed.
		 */
		public Control fillInformation(Object element, Composite composite);

		/**
		 * Returns the color to used as the background of the popup window
		 * presenting further information of the element.
		 * 
		 * @param element
		 * @return
		 */
		public Color getBackground(Object element);
	}

	/**
	 * Default implemention of {@link IInformationLabelProvider}.
	 * 
	 * @author bkahlert
	 * 
	 */
	public static class InformationLabelProvider extends LabelProvider implements
			IInformationLabelProvider {

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
		public List<Map.Entry<String, String>> getDetailInformation(
				Object element) {
			return new ArrayList<Map.Entry<String, String>>();
		}

		@Override
		public Color getBackground(Object element) {
			return null;
		}

	}

	/**
	 * Installs a new {@link TypedInformationControlManager} on the given
	 * control.
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
	 */
	public <CONTROL extends Control> void enable(
			CONTROL control,
			ISubjectInformationProvider<CONTROL, ILocatable> subjectInformationProvider);

	/**
	 * Deinstalls an the installed {@link TypedInformationControlManager} from
	 * the given control.
	 * 
	 * @param control
	 */
	public <CONTROL extends Control> void disable(CONTROL control);

}
