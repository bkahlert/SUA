package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class JAXBUtils {

	public static String marshall(Object obj) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(obj.getClass());

		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		StringWriter w = new StringWriter();
		m.marshal(obj, w);

		return w.toString();
	}

	public static File marshall(Object obj, File file) throws IOException,
			JAXBException {
		if (!file.exists() && !file.createNewFile())
			throw new IOException("Can't create " + file);
		if (!file.canWrite())
			throw new IOException("Can't write to " + file);
		JAXBContext context = JAXBContext.newInstance(obj.getClass());

		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		FileWriter w = new FileWriter(file);
		m.marshal(obj, w);
		w.close();

		return file;
	}

	@SuppressWarnings("unchecked")
	public static <CLAZZ> CLAZZ unmarshall(Class<CLAZZ> clazz, String string)
			throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(clazz);
		Unmarshaller um = context.createUnmarshaller();
		return (CLAZZ) um.unmarshal(new StringReader(string));
	}

	@SuppressWarnings("unchecked")
	public static <CLAZZ> CLAZZ unmarshall(Class<CLAZZ> clazz, File xml)
			throws JAXBException, FileNotFoundException {
		JAXBContext context = JAXBContext.newInstance(clazz);
		Unmarshaller um = context.createUnmarshaller();
		return (CLAZZ) um.unmarshal(new FileReader(xml));
	}
}
