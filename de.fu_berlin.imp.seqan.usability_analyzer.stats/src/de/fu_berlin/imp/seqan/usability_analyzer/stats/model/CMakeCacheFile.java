package de.fu_berlin.imp.seqan.usability_analyzer.stats.model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;

public class CMakeCacheFile {

	public static final Pattern PATTERN = Pattern
			.compile("([A-Za-z\\d]+)_stats_CMakeCache.txt");

	private ID id;
	private HashMap<String, String> values;

	public CMakeCacheFile(IData cMakeCacheFile) {
		Matcher matcher = PATTERN.matcher(cMakeCacheFile.getName());
		if (matcher.find()) {
			this.id = new ID(matcher.group(1));
		}

		values = new HashMap<String, String>();
		Properties properties = new Properties();
		try {
			properties.load(IOUtils.toInputStream(cMakeCacheFile.read()));
			Set<Object> keys = properties.keySet();
			for (Object key : keys) {
				String value = properties.getProperty((String) key);
				String[] valueParts = value.split("="); // ignore possibly
														// leading keys
				this.values
						.put((String) key, valueParts[valueParts.length - 1]);
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
		return StringUtils.join(generators, " + ");
	}

}
