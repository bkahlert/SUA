package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IDataSetInfo;

public class DataSetInfo extends File implements IDataSetInfo {

	private static final long serialVersionUID = 2526847064745924065L;

	public static final String FILENAME = "__dataset.txt";

	private String name;
	private TimeZoneDate startDate;
	private TimeZoneDate endDate;

	public DataSetInfo(File file) {
		super(file.toString());

		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(file));
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
}
