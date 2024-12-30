package com.datalinkx.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


	public static String map2Json(Map<String, Object> map) {
		String jsonStr = "";
		try {
			jsonStr = OBJECT_MAPPER.writeValueAsString(map);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return jsonStr;
	}



	public static Object touchJsonPath(String data, String jsonPath) {
		ObjectMapper mapper = new ObjectMapper();
        HashMap responseJson;
        try {
            responseJson = mapper.readValue (data, HashMap.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return JsonPath.read(responseJson, jsonPath);
	}
}
