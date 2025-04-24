package com.datalinkx.dataserver.controller.formatter;

import com.datalinkx.common.exception.DatalinkXServerException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;



@Slf4j
public class UserGenericConverter implements GenericConverter {

	private static ObjectMapper objectMapper;

	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		Set<ConvertiblePair> pairs = new HashSet<>();
		//将字符串转为数组
		pairs.add(new ConvertiblePair(String.class, Collection.class));
		//将字符转转为指定类型
		pairs.add(new ConvertiblePair(String.class, Object.class));
		return pairs;
	}

	@Override
	public Object convert(Object o, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (o == null) {
			return null;
		}
		if (objectMapper == null) {
			objectMapper = new ObjectMapper();
		}
		//字符串转数组的转换逻辑
		if (Collection.class.isAssignableFrom(targetType.getResolvableType().resolve())) {
			try {
				TypeFactory typeFactory = objectMapper.getTypeFactory();
				//获取目标数组的泛型类型
				ParameterizedType type = (ParameterizedType) targetType.getResolvableType().getType();
				CollectionType collectionType = typeFactory.constructCollectionType(
						(Class<? extends Collection>) targetType.getType(),
						(Class<?>) type.getActualTypeArguments()[0]);
				return objectMapper.readValue(String.valueOf(o), collectionType);
			} catch (Exception e) {
				log.error("自定义类型转换【字符串转数组】失败:", e);
				throw new DatalinkXServerException("自定义类型转换【字符串转数组】失败", e);
			}
		}

		//如果不是数组类型，剩下的将执行默认转换逻辑
		try {
			return objectMapper.readValue(String.valueOf(o), targetType.getResolvableType().resolve());
		} catch (JsonProcessingException e) {
			if (sourceType.getName().equals(targetType.getName())) {
				return o;
			} else {
				log.error("自定义类型转换【字符串转对象】失败", e);
				throw new DatalinkXServerException("自定义类型转换【字符串转对象】失败", e);
			}
		}
	}
}
