package com.dtstack.flinkx.hdfs.writer;

import com.dtstack.flinkx.util.StringUtil;
import io.minio.MinioClient;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import scala.math.Ordering;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.text.MessageFormat;
import java.util.Random;
import java.util.UUID;

public class MinioUtil {

    public static MinioClient getMinioClient(Map<String, Object> minioConfigMap)
            throws InvalidEndpointException, InvalidPortException {
        if(minioConfigMap == null || minioConfigMap.size() == 0) return null;
        String minioEndpoint = (String) minioConfigMap.get("minioEndpoint");
        String minioAccessKey = (String) minioConfigMap.get("minioAccessKey");
        String minioSecretKey = (String) minioConfigMap.get("minioSecretKey");
        MinioClient minioClient = new MinioClient(minioEndpoint, minioAccessKey, minioSecretKey);
        return minioClient;
    }
    public static String putObject(MinioClient minioClient,String tbName, InputStream streamObject, String bucketName, String suffix)  {
        String contentType = "application/octet-stream";
        if (streamObject == null) {
            return null;
        }
        String path = MessageFormat.format("/{0}/{1}_{2}.{3}",
                tbName,
                new SimpleDateFormat("yyyyMMddHHmmssSSS").format(System.currentTimeMillis()),
                getUUID(),
                suffix);

        try {
            minioClient.putObject(bucketName, path, streamObject, contentType);
            return MessageFormat.format("/{0}{1}", bucketName, path);
        } catch (InternalException e) {
            try {
                Thread.sleep(10 * 1000);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return putObject(minioClient, tbName, streamObject, bucketName, suffix);
        } catch (Exception e) {
            return  null;
        }
    }

    public static String getUUID(){
        String chars = "abcdefghijklmnopqrstuvwxyz";
        UUID uuid = java.util.UUID.randomUUID();
        String result = uuid.toString().toLowerCase().replace("-", "");
        Random random = new java.util.Random();
        char firstChar = chars.toCharArray()[random.nextInt(chars.length())];
        return  firstChar + result;
    }
}
