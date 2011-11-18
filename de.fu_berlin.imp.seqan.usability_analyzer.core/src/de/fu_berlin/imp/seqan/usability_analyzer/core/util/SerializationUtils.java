package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.log4j.Logger;

public class SerializationUtils {

	private static Logger logger = Logger.getLogger(SerializationUtils.class);

	public static <T extends Serializable> String serialize(T serializable) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(serializable);
			oos.close();
			return baos.toString();
		} catch (IOException e) {
			logger.error("Could not serialize " + serializable, e);
		}
		return null;
	}

	public static Object deserialize(String serialized) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(
					serialized.getBytes());
			ObjectInputStream ois = new ObjectInputStream(bais);
			Object object = ois.readObject();
			ois.close();
			return object;
		} catch (Exception e) {
			logger.error("Could not deserialize the object " + serialized, e);
		}
		return null;
	}
}
