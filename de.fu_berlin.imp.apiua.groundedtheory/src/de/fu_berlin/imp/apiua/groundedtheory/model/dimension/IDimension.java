package de.fu_berlin.imp.apiua.groundedtheory.model.dimension;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;

/**
 * A {@link IDimension} that can be associated to some concept (e.g. a
 * {@link ICode}).
 * 
 * @author bkahlert
 * 
 */
public interface IDimension {

	public interface IDimensionListener {
		public void dimensionChanged(IDimension dimension);
	}

	public interface IDimensionValueListener {
		public void dimensionValueChanged(IDimension dimension, String newValue);
	}

	/**
	 * Checks if the given value is a legal value.
	 * 
	 * @param value
	 * @return
	 */
	public boolean isLegal(String value);

	/**
	 * Returns a string that represents the possible values of this dimension. A
	 * nominal dimension could return <code>value1, ..., value5</code> whereas a
	 * range dimension should return its extremes like <code>0.0 - 1.0</code>.
	 * 
	 * @return
	 */
	public String represent();

	/**
	 * Creates a {@link Control} in the given {@link Composite} that allows to
	 * edit a dimension.
	 * 
	 * @return
	 */
	public Control createEditControl(Composite parent,
			IDimensionListener dimensionListener);

	/**
	 * Creates a {@link Control} in the given {@link Composite} that allows to
	 * edit a dimension value.
	 * 
	 * @param parent
	 * @return
	 */
	public Control createValueEditControl(Composite parent,
			IDimensionValueListener dimensionValueListener);

	/**
	 * Refreshes the {@link Control} returned by
	 * {@link #createValueEditControl(Composite)} to reflect the given value.
	 * 
	 * @param control
	 * @param value
	 */
	public void setValueEditControlValue(Control control, String value);

/**
	 * Returns the string representation of the dimension value set in the {@link Control} previously returned by {@link #createValueEditControl(Composite).
	 * @param control
	 * @return
	 */
	public String getValueEditControlValue(Control control);

}
