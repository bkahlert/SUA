package de.fu_berlin.imp.seqan.usability_analyzer.stats.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.ID;

public class StatsFile {

	public static final Pattern PATTERN = Pattern
			.compile("([A-Za-z\\d]+)_stats.txt");

	private ID id;
	private String node; // e.g. wensicia.local
	private String platform; // e.g. win32
	private String system; // e.g. Windows
	private String release; // e.g. 7
	private String version; // e.g. 6.1.7600
	private String platformLong; // e.g. Windows-7-6.1.7600

	public StatsFile(IData resource) {
		Matcher matcher = PATTERN.matcher(resource.getName());
		if (matcher.find()) {
			this.id = new ID(matcher.group(1));
		}

		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readValue(resource.read(), JsonNode.class);

			JsonNode os = root.get("os");

			this.node = os.get("node").getTextValue();
			this.platform = os.get("sys.platform").getTextValue();
			this.system = os.get("system").getTextValue();
			this.release = os.get("release").getTextValue();
			this.version = os.get("version").getTextValue();
			this.platformLong = os.get("platform").getTextValue();
		} catch (Exception e) {
			throw new RuntimeException("Could no read stats file", e);
		}
	}

	public ID getId() {
		return this.id;
	}

	public String getNode() {
		return this.node;
	}

	public String getPlatform() {
		return this.platform;
	}

	public String getSystem() {
		return this.system;
	}

	public String getRelease() {
		return this.release;
	}

	public String getVersion() {
		return this.version;
	}

	public String getPlatformLong() {
		return this.platformLong;
	}

}
