

package retrofit2.converter;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import retrofit2.Converter;

final class JacksonParamConverter implements Converter<Object, String> {

	private final ObjectMapper mapper;

	JacksonParamConverter(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public String convert(Object value) throws IOException {
		return mapper.writeValueAsString(value);
	}
}
