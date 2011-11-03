package de.fu_berlin.imp.seqan.usability_analyzer.stats.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;

public class CMakeCacheFile extends File {

	private static final long serialVersionUID = 5159431028889474742L;
	public static final String PATTERN = "([A-Za-z\\d]+)_stats_CMakeCache.txt";

	private ID id;
	private HashMap<String, String> values;

	public CMakeCacheFile(String filename) {
		super(filename);

		Matcher matcher = Pattern.compile(PATTERN).matcher(filename);
		if (matcher.find()) {
			this.id = new ID(matcher.group(1));
		}

		values = new HashMap<String, String>();
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(filename));
			Set<Object> keys = properties.keySet();
			for (Object key : keys) {
				String value = properties.getProperty((String) key);
				this.values.put((String) key, value);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ID getId() {
		return id;
	}

	public String getGenerator() {
		String generator = this.values.get("CMAKE_GENERATOR");
		String extraGenerator = this.values.get("CMAKE_EXTRA_GENERATOR");
		List<String> generators = new ArrayList<String>();
		if (generator != null)
			generators.add(generator);
		if (extraGenerator != null)
			generators.add(extraGenerator);
		return StringUtils.join(generators, ";");
	}

}
