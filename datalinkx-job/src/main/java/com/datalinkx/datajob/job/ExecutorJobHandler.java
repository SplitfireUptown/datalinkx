package com.datalinkx.datajob.job;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.common.utils.ProcessStreamHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ExecutorJobHandler {

	@Value("${flinkx.path}")
	String flinkXHomePath;

	@Value("${flinkx.syncplugins.path:${flinkx.path}}")
	private String syncPluginsPath;

	@Value("${flinkx.flinkconf.path:${flinkx.path}}")
	private String flinkConf;

	@Value("${reserve.job_graph:false}")
	Boolean reserveJobGraph;


	public String execute(String jobId, String reader, String writer, Map<String, Object> otherSetting) throws Exception {

		StringBuffer errorRet = new StringBuffer();
		StringBuffer successRet = new StringBuffer();

		String jobSettings = this.generateJobSetting("classpath:job_setting.json", otherSetting);
		String jobJsonFile = this.generateJobJsonFile(jobId, reader, writer, jobSettings);

		try {
			String cmdStr = this.generateFlinkCmd(jobId, jobJsonFile, otherSetting);
			try {
				log.info("job_id: {}, execute job command: {}", jobId, cmdStr);
				Process process = Runtime.getRuntime().exec(cmdStr);
				InputStreamReader ir = new InputStreamReader(process.getErrorStream());
				LineNumberReader errorInput = new LineNumberReader(ir);
				Thread threadError = new Thread(new ProcessStreamHandler(errorInput, errorRet));
				threadError.start();

				InputStreamReader ii = new InputStreamReader(process.getInputStream());
				LineNumberReader successInput = new LineNumberReader(ii);
				Thread threadSuccess = new Thread(new ProcessStreamHandler(successInput, successRet));
				threadSuccess.start();

				process.waitFor();
				threadSuccess.join();
				threadError.join();
			} catch (IOException e) {
				log.error("IOException ", e);
				errorRet.append("IOException ").append(e.getMessage());
				throw new Exception(errorRet.toString());
			}
		} catch (Exception e) {
			log.error("flink任务提交异常", e);
			throw new Exception(e);
		} finally {
			// 删除临时文件
			if (FileUtil.exist(jobJsonFile) && !reserveJobGraph) {
				FileUtil.del(new File(jobJsonFile));
			}
		}
		String pattern = "Received response \\{\"jobUrl\":\"/jobs/.+\"}";
		Pattern rg = Pattern.compile(pattern);
		Matcher matcher = rg.matcher(successRet.toString());

		if (!matcher.find()) {
			log.error(errorRet.toString());
			log.error(successRet.toString());
			throw new Exception(errorRet.toString());
		}

		String received = matcher.group();
		String receivedStr = received.substring("Received response ".length());
		Map map = JsonUtils.toObject(receivedStr, Map.class);
		String jobUrl = (String) map.get("jobUrl");
		return jobUrl.substring("/jobs/".length());
	}

	public String generateFlinkCmd(String jobId, String jobJsonFile, Map<String, Object> otherSetting) {
		String javaHome = System.getenv("JAVA_HOME");
		String os = System.getProperty("os.name").toLowerCase();

		return String.format(
				"%s -cp %s com.dtstack.flinkx.launcher.Launcher -mode standalone -jobid %s  -job %s  -pluginRoot %s -flinkconf %s",
				javaHome + (os.contains("win") ? "\\bin\\java" : "/bin/java"),
				flinkXHomePath + (os.contains("win") ? "lib\\*" : "lib/*"),
				jobId,
				jobJsonFile,
				syncPluginsPath.equals(flinkXHomePath) ? syncPluginsPath + "syncplugins" : syncPluginsPath,
				flinkConf.equals(flinkXHomePath) ? flinkConf + "flinkconf" : flinkConf
		);
	}

	public String generateJobJsonFile(String jobId, String reader, String writer, String setting) {
		String jobJson = String.format("{\"job\":{\"content\":[{\"reader\":%s,\"writer\":%s}],\"setting\":%s}}", reader, writer, setting);
		log.info("flink job_id: {} graph: {}", jobId, jobJson);
		String jsonPath = flinkXHomePath;

		if (!FileUtil.exist(jsonPath)) {
			FileUtil.mkdir(jsonPath);
		}

		String tmpFilePath = jsonPath + "jobTmp-" + IdUtil.simpleUUID() + ".json";
		try (PrintWriter ptWriter = new PrintWriter(tmpFilePath, "UTF-8")) {
			ptWriter.println(jobJson);
		} catch (Exception e) {
			log.error("job任务临时文件写入异常: " + e.getMessage(), e);
		}
		return tmpFilePath;
	}

	/*@SneakyThrows
	private String generateJobSetting() {
		File jsonFile = ResourceUtils.getFile("classpath:job_setting.json");
		return FileUtils.readFileToString(jsonFile,"UTF-8");
	}*/

    @SneakyThrows
	public String generateJobSetting(String jobSettingPath, Map<String, Object> commonSettings) {
        Resource resource = new DefaultResourceLoader().getResource(jobSettingPath);

        log.info(resource.toString());

        InputStream inputStream = resource.getInputStream();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        }
    }
}
