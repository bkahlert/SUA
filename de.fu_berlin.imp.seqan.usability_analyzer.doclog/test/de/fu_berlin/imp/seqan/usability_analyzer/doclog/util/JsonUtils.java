package de.fu_berlin.imp.seqan.usability_analyzer.doclog.util;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.util.DefaultPrettyPrinter;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogFile;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecordList;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogScreenshot.Status;

public class JsonUtils {

	public static Logger logger = Logger.getLogger(JsonUtils.class);

	/**
	 * e.g. May 10 1961 00:00:00 GMT-0600
	 */
	public static DateFormat dateFormat = new SimpleDateFormat(
			"MMM d yyyy HH:mm:ss 'GMT'Z");

	public static String formatDate(Date date) {
		return dateFormat.format(date);
	}

	public static String generateJSON(DoclogFile doclogFile,
			Map<String, Object> options, boolean pretty) {
		JsonFactory factory = new JsonFactory();
		StringWriter writer = new StringWriter();
		JsonGenerator generator;
		try {
			generator = factory.createJsonGenerator(writer);
			generator.setCodec(new ObjectMapper());
			if (pretty)
				generator.setPrettyPrinter(new DefaultPrettyPrinter());

			generator.writeStartObject();

			if (options != null) {
				generator.writeFieldName("options");
				generator.writeStartObject();
				for (String option : options.keySet()) {
					generator.writeFieldName(option);
					Object optionValue = options.get(option);
					generator.writeObject(optionValue);
					// generator.writeString(optionValue.toString());
				}
				generator.writeEndObject();
			}

			generator.writeFieldName("events");
			generator.writeStartArray();
			DoclogRecordList doclogRecords = doclogFile.getDoclogRecords();
			for (int i = 0, m = doclogRecords.size(); i < m; i++) {
				DoclogRecord doclogRecord = doclogRecords.get(i);

				generator.writeStartObject();
				generator.writeFieldName("title");
				generator.writeString(doclogRecord.getAction().toString()
						+ " - " + doclogRecord.getUrl());

				DateRange dateRange = doclogRecord.getDateRange();
				if (dateRange.getStartDate() != null) {
					generator.writeFieldName("start");
					generator.writeString(formatDate(dateRange.getStartDate()));
				}
				if (dateRange.getEndDate() != null) {
					generator.writeFieldName("end");
					generator.writeString(formatDate(dateRange.getEndDate()));
				}
				if (dateRange.getStartDate() != null
						&& dateRange.getEndDate() != null) {
					generator.writeFieldName("durationEvent");
					generator.writeBoolean(true);
				}

				/*
				 * IMPORTANT: Because Mac OS returns filenames in a decomposed
				 * form
				 * (http://loopkid.net/articles/2011/03/19/groking-hfs-character
				 * -encoding) we need to convert them to composed form
				 * (http://download
				 * .oracle.com/javase/6/docs/api/java/text/Normalizer.html).
				 * This is the only form where we can be sure it is compatible
				 * with the outer world (e.g. a browser).
				 */
				String filename = Normalizer.normalize(doclogRecord
						.getScreenshot().calculateFilename(), Form.NFC);
				generator.writeFieldName("icon");
				generator.writeString("file://" + filename.replace("%", "%25"));

				if (doclogRecord.getScreenshot().getStatus() == Status.OK
						|| doclogRecord.getScreenshot().getStatus() == Status.DIRTY) {
					generator.writeFieldName("image");
					generator.writeString("sua://doclog/" + i);
				}

				generator.writeEndObject();
			}
			generator.writeEndArray();
			generator.writeEndObject();
			generator.close();
			String generated = writer.toString();
			writer.close();
			return generated;
		} catch (IOException e) {
			logger.fatal("Error using " + StringWriter.class.getSimpleName(), e);
		}
		return null;
	}
}
