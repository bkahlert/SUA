package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IDataSetInfo;

public class DataSetInfo implements IDataSetInfo {

	@SuppressWarnings("unused")
	private static Logger LOGGER = Logger.getLogger(DataSetInfo.class);

	private String name;
	private TimeZoneDate startDate;
	private TimeZoneDate endDate;
	private Map<String, String> unknownProperties = new HashMap<String, String>();

	public DataSetInfo(IData data) {
		Properties properties = new Properties();
		try {
			properties.load(new StringReader(data.read()));
			Set<Object> keys = properties.keySet();
			for (Object key_ : keys) {
				String key = (String) key_;
				if (key.equals("name")) {
					this.name = properties.getProperty(key);
				} else if (key.equals("start")) {
					this.startDate = new TimeZoneDate(
							properties.getProperty(key));
				} else if (key.equals("end")) {
					this.endDate = new TimeZoneDate(properties.getProperty(key));
				} else {
					unknownProperties.put(key, properties.getProperty(key));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public TimeZoneDateRange getDateRange() {
		return new TimeZoneDateRange(startDate, endDate);
	}

	@Override
	public Map<String, String> getUnknownProperties() {
		return unknownProperties;
	}
}
