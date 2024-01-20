//CHECKSTYLE:OFF
package retrofit2.converter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.ObjectMapper;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.http.JsonBeanParam;


public final class JacksonParamConverterFactory extends Converter.Factory {
	/**
	 * Create an instance using a default {@link ObjectMapper} instance for
	 * conversion.
	 */
	public static JacksonParamConverterFactory create() {
		return create(new ObjectMapper());
	}

	/** Create an instance using {@code mapper} for conversion. */
	public static JacksonParamConverterFactory create(ObjectMapper mapper) {
		if (mapper == null) {
			throw new NullPointerException("mapper == null");
		}
		return new JacksonParamConverterFactory(mapper);
	}

	private final ObjectMapper mapper;

	private JacksonParamConverterFactory(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public Converter<?, String> stringConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
		if (existJsonParamAnnotation(annotations)) {
			return new JacksonParamConverter(mapper);
		}
		return null;
	}

	public static boolean existJsonParamAnnotation(Annotation[] annotations) {
		for (Annotation annotation : annotations) {
			if (JsonBeanParam.class.equals(annotation.annotationType())) {
				return true;
			}
		}
		return false;
	}
}

