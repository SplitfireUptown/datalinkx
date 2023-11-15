package com.datalinkx.common.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


/**
 * Gzip压缩与解压
 *
 * @author uptown
 * @create 05 21, 2021
 * @since 1.0.0
 */

public final class GzipUtils  {

    public static final String GZIP_ENCODE_UTF_8 = "UTF-8";
    public static final String GZIP_ENCODE_ISO_8859_1 = "ISO-8859-1";

    private static final int BYTENUM = 256;

    private GzipUtils() {
    }

    public static String compress2Str(String str, String altchars) throws UnsupportedEncodingException {
        String s = Base64Utils.encodeBase64((ZLibUtils.compress(str.getBytes("UTF-8"))));
        if (!ObjectUtils.isEmpty(altchars)) {
            s = s.replace('+', altchars.charAt(0));
            s = s.replace('/', altchars.charAt(1));
        }
        return s;
    }

    public static String uncompress2Str(String str, String altchars) throws IOException {
        if (!ObjectUtils.isEmpty(altchars)) {
            str = str.replace(altchars.charAt(0), '+');
            str = str.replace(altchars.charAt(1), '/');
        }
        byte[] s = Base64Utils.decodeBase64(str);

        return ZLibUtils.decompress(s);
    }
}
