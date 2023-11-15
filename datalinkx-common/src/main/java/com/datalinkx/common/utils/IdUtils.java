package com.datalinkx.common.utils;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;


public final class IdUtils {
	private IdUtils() {
	}

	
	/**
	 * 生成业务ID：folder_xxx， tb_xxx
	 * 
	 * @param prefix
	 * @return
	 */
	public static String genKey(String prefix) {
		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		return String.format("%s_%s", prefix, uuid);
	}

	public static String genKey() {
		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		return uuid;
	}

	/**
	 * 随机生成底层表名，即storageId。
	 * 为了避免一些引擎无法处理数字开头的表名，storageId，已d字母开头，f字母结尾，d\f无特殊含义
	 */
	public static String createStorageId() {
		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		String sortUuid = uuid.substring(1, uuid.length() - 1);
		return String.format("d%sf", sortUuid);
	}

	public static String generateUniqueId() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	// 生成uniq_id
	public static String uniqId(String prefix) {
		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		if (StringUtils.isEmpty(prefix)) {
			return uuid;
		}
		return String.format("%s_%s", prefix, uuid);
	}
}
