
package retrofit2;


import static retrofit2.Utils.parameterError;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;


public class QueryBeanParameterHandler<T> extends ParameterHandler<T> {
	private final Method method;
	private final int p;
	private final boolean encoded;
	private Retrofit retrofit;

	QueryBeanParameterHandler(Method method, int p, Retrofit retrofit, boolean encoded) {
		this.method = method;
		this.p = p;
		this.encoded = encoded;
		this.retrofit = retrofit;
	}

	@Override
	void apply(RequestBuilder builder, @Nullable T value) throws IOException {
		if (value == null) {
			throw parameterError(method, p, "Query map was null");
		}

		// 获取包括父类的全部属性，不处理重名属性
		// 提取所有方法，为后续通过get方法获取属性值准备
		Class cls = value.getClass();
		List<java.lang.reflect.Field> allFields = new ArrayList<>();
		HashMap<String, Method> methods = new HashMap<>();
		while (cls != null) {
			for (Method m : cls.getDeclaredMethods()) {
				if (m.getName().startsWith("get")) {
					methods.put(cls.getName() + "." + m.getName(), m);
				}
			}
			allFields.addAll(new ArrayList(Arrays.asList(cls.getDeclaredFields())));

			cls = cls.getSuperclass();
		}

		for (java.lang.reflect.Field field : allFields) {
			String name = field.getName();
			field.setAccessible(true);
			Object v = null;
			try {
				// 若属性有get方法则通过get方法获取其值
				int startIndex = 0;
				if (name.charAt(0) == '_')
					startIndex = 1;
				String getMethodName = field.getDeclaringClass().getName() + ".get"
						+ name.substring(startIndex, startIndex + 1).toUpperCase() + name.substring(startIndex + 1);
				if (methods.containsKey(getMethodName)) {
					v = methods.get(getMethodName).invoke(value, new Object[] {});
				} else {
					v = field.get(value);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (v != null) {
				Converter<Object, String> converter = retrofit.stringConverter(field.getType(), field.getAnnotations());
				String convertValue = converter.convert(v);
				retrofit2.http.Field fieldx = field.getAnnotation(retrofit2.http.Field.class);
				if (fieldx != null) {
					if (fieldx.value() != null)
						name = fieldx.value();
					builder.addFormField(name, convertValue, encoded);
					continue;
				}
				retrofit2.http.Header header = field.getAnnotation(retrofit2.http.Header.class);
				if (header != null) {
					if (header.value() != null)
						name = header.value();

					builder.addHeader(name, convertValue);
					continue;
				}

				retrofit2.http.Query query = field.getAnnotation(retrofit2.http.Query.class);
				if (query != null && query.value() != null) {
					name = query.value();
				}
				builder.addQueryParam(name, convertValue, encoded);

				retrofit2.http.QueryMap queryMap = field.getAnnotation(retrofit2.http.QueryMap.class);
				if (queryMap != null) {
					Type type = field.getType();

					validateResolvableType(p, type);
					Class<?> rawParameterType = Utils.getRawType(type);
					if (!Map.class.isAssignableFrom(rawParameterType)) {
						throw parameterError(method, p, "@QueryMap parameter type must be Map.");
					}
					Type mapType = Utils.getSupertype(type, rawParameterType, Map.class);
					if (!(mapType instanceof ParameterizedType)) {
						throw parameterError(method, p, "Map must include generic types (e.g., Map<String, String>)");
					}
					ParameterizedType parameterizedType = (ParameterizedType) mapType;
					Type keyType = Utils.getParameterUpperBound(0, parameterizedType);
					if (String.class != keyType) {
						throw parameterError(method, p, "@QueryMap keys must be of type String: " + keyType);
					}
					Type valueType = Utils.getParameterUpperBound(1, parameterizedType);
					Converter<Object, String> valueConverter = retrofit.stringConverter(valueType,
							new Annotation[] { queryMap });

					if (value == null) {
						throw parameterError(method, p, "Query map was null");
					}

					for (Map.Entry<String, Object> entry : ((Map<String, Object>) v).entrySet()) {
						String entryKey = entry.getKey();
						if (entryKey == null) {
							throw parameterError(method, p, "Query map contained null key.");
						}
						Object entryValue = entry.getValue();
						if (entryValue == null) {
							throw parameterError(method, p,
									"Query map contained null value for key '" + entryKey + "'.");
						}

						String convertedEntryValue = valueConverter.convert(entryValue);
						if (convertedEntryValue == null) {
							throw parameterError(method, p,
									"Query map value '" + entryValue + "' converted to null by "
											+ valueConverter.getClass().getName() + " for key '" + entryKey + "'.");
						}
						builder.addQueryParam(entryKey, convertedEntryValue, encoded);
					}
					builder.addQueryParam(name, converter.convert(v), encoded);
				}
			}
		}
	}

	private void validateResolvableType(int p, Type type) {
		if (Utils.hasUnresolvableType(type)) {
			throw parameterError(method, p, "Parameter type must not include a type variable or wildcard: %s", type);
		}
	}
}
