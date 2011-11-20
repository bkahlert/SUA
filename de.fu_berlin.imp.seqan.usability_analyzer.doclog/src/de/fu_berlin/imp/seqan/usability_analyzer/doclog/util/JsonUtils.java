package de.fu_berlin.imp.seqan.usability_analyzer.doclog.util;

import java.io.IOException;
import java.io.StringWriter;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.util.DefaultPrettyPrinter;

import com.bkahlert.devel.nebula.widgets.timeline.Timeline.Decorator;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.LocalDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogAction;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogScreenshot.Status;

public class JsonUtils {

	public static Logger logger = Logger.getLogger(JsonUtils.class);

	public static String generateJSON(List<DoclogRecord> doclogRecords,
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
			for (DoclogRecord doclogRecord : doclogRecords) {

				generator.writeStartObject();
				generator.writeFieldName("title");
				generator
						.writeString(doclogRecord.getShortUrl()
								+ ((doclogRecord.getAction() == DoclogAction.LINK) ? "<br/>→ "
										+ doclogRecord.getActionParameter()
										: ""));

				generator.writeFieldName("classname");
				generator.writeString(doclogRecord.getAction().toString());

				LocalDateRange dateRange = doclogRecord.getDateRange();
				if (dateRange.getStartDate() != null) {
					generator.writeFieldName("start");
					generator.writeString(dateRange.getStartDate().toISO8601());
				}
				if (dateRange.getEndDate() != null) {
					generator.writeFieldName("end");
					generator.writeString(dateRange.getEndDate().toISO8601());
				}
				if (dateRange.getStartDate() != null
						&& dateRange.getEndDate() != null) {
					generator.writeFieldName("durationEvent");
					generator.writeBoolean(true);

					// TODO
					if (dateRange.getStartDate().after(dateRange.getEndDate())) {
						System.err.println("ERROR" + doclogRecord.getAction());
					}
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
					generator.writeString("sua://doclog/"
							+ System.identityHashCode(doclogRecord));
				}

				generator.writeEndObject();
			}
			generator.writeEndArray();
			generator.writeEndObject();
			generator.close();
			String generated = writer.toString();
			writer.close();
			// System.err.println(generated);
			return generated;
		} catch (IOException e) {
			logger.fatal("Error using " + StringWriter.class.getSimpleName(), e);
		}
		return null;
	}

	public static String jsonDecoratorList(List<Decorator> decorators,
			boolean pretty) {
		JsonFactory factory = new JsonFactory();
		StringWriter writer = new StringWriter();
		JsonGenerator generator;
		try {
			generator = factory.createJsonGenerator(writer);
			generator.setCodec(new ObjectMapper());
			if (pretty)
				generator.setPrettyPrinter(new DefaultPrettyPrinter());

			generator.writeObject(decorators);

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
