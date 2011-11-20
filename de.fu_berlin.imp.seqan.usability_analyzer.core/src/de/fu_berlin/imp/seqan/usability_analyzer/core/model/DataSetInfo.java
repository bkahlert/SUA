package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

public class DataSetInfo extends File {

	private static final long serialVersionUID = 2526847064745924065L;

	public static final String FILENAME = "__dataset.txt";

	private String name;
	private TimeZoneDate startDate;
	private TimeZoneDate endDate;

	public DataSetInfo(String pathname) {
		super(pathname);

		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(pathname));
			Set<Object> keys = properties.keySet();
			for (Object key_ : keys) {
				String key = (String) key_;
				if (key.equals("name")) {
					this.name = properties.getProperty(key);
				} else if (key.equals("start")) {
					this.startDate = new TimeZoneDate(properties.getProperty(key));
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

	public String getName() {
		return this.name;
	}

	public TimeZoneDate getStartDate() {
		return this.startDate;
	}

	public TimeZoneDate getEndDate() {
		return this.endDate;
	}
}
