package de.fu_berlin.imp.seqan.usability_analyzer.stats.model;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSourceInvalidException;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;

public class StatsFile extends File {

	private static final long serialVersionUID = 5159431028889474742L;
	public static final String PATTERN = "([A-Za-z\\d]+)_stats.txt";

	private ID id;
	private String node; // e.g. wensicia.local
	private String platform; // e.g. win32
	private String system; // e.g. Windows
	private String release; // e.g. 7
	private String version; // e.g. 6.1.7600
	private String platformLong; // e.g. Windows-7-6.1.7600

	public StatsFile(String filename) throws DataSourceInvalidException {
		super(filename);

		Matcher matcher = Pattern.compile(PATTERN).matcher(filename);
		if (matcher.find()) {
			this.id = new ID(matcher.group(1));
		}

		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readValue(this, JsonNode.class);

			JsonNode os = root.get("os");

			this.node = os.get("node").getTextValue();
			this.platform = os.get("sys.platform").getTextValue();
			this.system = os.get("system").getTextValue();
			this.release = os.get("release").getTextValue();
			this.version = os.get("version").getTextValue();
			this.platformLong = os.get("platform").getTextValue();
		} catch (Exception e) {
			throw new DataSourceInvalidException("Could no read stats file", e);
		}
	}

	public ID getId() {
		return id;
	}

	public String getNode() {
		return node;
	}

	public String getPlatform() {
		return platform;
	}

	public String getSystem() {
		return system;
	}

	public String getRelease() {
		return release;
	}

	public String getVersion() {
		return version;
	}

	public String getPlatformLong() {
		return platformLong;
	}

}
