package com.datalinkx.common.utils;

import java.io.UnsupportedEncodingException;
import java.util.Base64;


public final class Base64Utils {
	private Base64Utils() {
	}
	public static String encodeBase64(byte[] src) {

		return Base64.getEncoder().encodeToString(src);
	}

	public static byte[] decodeBase64(String src) throws UnsupportedEncodingException {
		return Base64.getDecoder().decode(src);
	}
}
