package de.fu_berlin.imp.apiua.core.util;

import java.util.Collection;

import com.bkahlert.nebula.utils.IConverter;

import de.fu_berlin.imp.apiua.core.model.URI;

public class SerializationUtils extends
		com.bkahlert.nebula.utils.SerializationUtils {

	public static IConverter<URI, String> uriToStringConverter = new IConverter<URI, String>() {
		@Override
		public String convert(URI uri) {
			return uri.toString();
		}
	};

	public static IConverter<String, URI> stringToURIConverter = new IConverter<String, URI>() {
		@Override
		public URI convert(String string) {
			return new URI(string);
		}
	};

	public static String serialize(Collection<URI> collection) {
		return serialize(collection, uriToStringConverter);
	}

	public static Collection<URI> deserialize(String serialized) {
		return deserialize(serialized, stringToURIConverter);
	}
}
