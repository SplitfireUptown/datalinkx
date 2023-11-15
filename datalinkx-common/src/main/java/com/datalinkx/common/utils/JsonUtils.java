package com.datalinkx.common.utils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.apache.commons.lang3.StringUtils;

public final class JsonUtils {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	static {
		OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	/**
	 * 防止反射调用构造器创建对象
	 */
	private JsonUtils() {
		throw new AssertionError();
	}

	/**
	 * 自定义日期反序列化处理类
	 * LocalDate
	 * jdk8 support
	 */
	public static class JsonLocalDateDeserializer extends JsonDeserializer<LocalDate> {
		@Override
		public LocalDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
			String str = jsonParser.getText().trim();
			return LocalDate.parse(str, DateTimeFormatter.ISO_DATE);
		}
	}

	/**
	 * 自定义日期序列化类
	 * LocalDate
	 * jdk8 support
	 */
	public static class JsonLocalDateSerializer extends JsonSerializer<LocalDate> {
		@Override
		public void serialize(LocalDate localDate, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
			String localdateStr = localDate.format(DateTimeFormatter.ISO_DATE);
			jsonGenerator.writeString(localdateStr);
		}
	}

	/**
	 * json数据转pojo
	 *
	 * @param jsonStr json字符串
	 * @param cls     映射类型
	 * @param <T>     推导类型
	 * @return 推导类型json对象
	 */
	public static <T> T toObject(String jsonStr, Class<T> cls) {
		T object = null;
		try {
			if (StringUtils.isEmpty(jsonStr)) {
				return cls.newInstance();
			}
			object = OBJECT_MAPPER.readValue(jsonStr, cls);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return object;
	}


	/**
	 * json数据转PojoList
	 *
	 * @param jsonArray json数据
	 * @param cls     类型
	 * @param <T>     推导类型
	 * @return pojoList
	 */
	public static <T> List<T> toList(String jsonArray, Class<T> cls) {
		List<T> pojoList = null;
		try {
			CollectionType listType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, cls);
			pojoList = OBJECT_MAPPER.readValue(jsonArray, listType);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return pojoList;
	}


	/**
	 * pojo转json
	 *
	 * @param obj pojo
	 * @return json字符串
	 */
	public static String toJson(Object obj) {
		String jsonStr = "";
		try {
			jsonStr = OBJECT_MAPPER.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		return jsonStr;
	}

	/**
	 * json转listMap
	 *
	 * @param jsonArray jsonArray字符串
	 * @return Listmap对象
	 */
	public static List<Map<String, Object>> toListMap(String jsonArray) {
		List<Map<String, Object>> convertedListMap = null;
		try {
			convertedListMap = OBJECT_MAPPER.readValue(jsonArray, new TypeReference<List<Map<String, Object>>>() {
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return convertedListMap;
	}

	/**
	 * json转map
	 *
	 * @param json json字符串
	 * @return map对象
	 */
	public static Map<Integer, Map<String, Object>> toInnerMap(String json) {
		Map<Integer, Map<String, Object>> convertedMap = null;
		try {
			convertedMap = OBJECT_MAPPER.readValue(json, new TypeReference<Map<Integer, Map<String, Object>>>() {
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return convertedMap;
	}

	/**
	 * json转map
	 *
	 * @param json json字符串
	 * @return map对象
	 */
	public static Map<String, Object> json2Map(String json) {
		Map<String, Object> convertedMap = null;
		try {
			convertedMap = OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return convertedMap;
	}

	/**
	 * json转map
	 *
	 * @param json json字符串
	 * @return map对象
	 */
	public static Map<Integer, Map<String, Object>> json2MapMap(String json) {
		Map<Integer, Map<String, Object>> convertedMap = null;
		try {
			convertedMap = OBJECT_MAPPER.readValue(json, new TypeReference<Map<Integer, Map<String, Object>>>() {
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return convertedMap;
	}


	/**
	 * listMap转json
	 *
	 * @param listMap listMap
	 * @return
	 */
	public static String listMap2JsonArray(List<Map<String, Object>> listMap) {
		String jsonStr = "";
		try {
			jsonStr = OBJECT_MAPPER.writeValueAsString(listMap);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		return jsonStr;
	}

	/**
	 * 将json转为JsonNode对象
	 * @param json
	 * @return
	 */
	public static JsonNode toJsonNode(String json) {
		JsonNode jsonNode = null;
		try {
			jsonNode = OBJECT_MAPPER.readTree(json);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		return jsonNode;
	}

	/**
	 * 获取 ObjectNode
	 * @return
	 */
	public static ObjectNode getObjectNode() {
		return OBJECT_MAPPER.createObjectNode();
	}

	/**
	 * 获取 ArrayNode
	 * @return
	 */
	public static ArrayNode getArrayNode() {
		return OBJECT_MAPPER.createArrayNode();
	}

	public static String map2Json(Map<String, Object> map) {
		String jsonStr = "";
		try {
			jsonStr = OBJECT_MAPPER.writeValueAsString(map);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return jsonStr;
	}

	public static String list2Json(List<?> list) {
		String jsonStr = "";
		try {
			jsonStr = OBJECT_MAPPER.writeValueAsString(list);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return jsonStr;
	}

	public static <T> List<T> json2List(String json) {
		List<T> convertedList = null;
		try {
			convertedList = OBJECT_MAPPER.readValue(json, new TypeReference<List<T>>() {
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return convertedList;
	}
}
