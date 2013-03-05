package de.fu_berlin.imp.seqan.usability_analyzer.core.commands;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.IParameterValues;

public class BooleanValues implements IParameterValues {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map getParameterValues() {
		Map params = new HashMap();
		params.put("null", null);
		params.put(Boolean.TRUE.toString(), Boolean.TRUE);
		params.put(Boolean.FALSE.toString(), Boolean.FALSE);
		return params;
	}

}
