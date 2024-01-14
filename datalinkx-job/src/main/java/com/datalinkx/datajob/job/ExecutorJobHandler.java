package com.datalinkx.datajob.job;


import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.datalinkx.common.utils.JsonUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;


@Slf4j
@Component
public class ExecutorJobHandler {

	@Value("${flinkx.path}")
	private String flinkXHomePath;

	@Value("${reserve.job_graph:false}")
	private Boolean reserveJobGraph;


	public String execute(String jobId, String reader, String writer) throws Exception {

		StringBuffer errorRet = new StringBuffer();
		StringBuffer successRet = new StringBuffer();

		String jobSettings = this.generateJobSetting();
		String jobJsonFile = this.generateJobJsonFile(jobId, reader, writer, jobSettings);

		try {
			String cmdStr = this.generateFlinkCmd(jobId, jobJsonFile);
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
			throw new Exception(errorRet.toString());
		}

		String received = matcher.group();
		String receivedStr = received.substring("Received response ".length());
		Map map = JsonUtils.toObject(receivedStr, Map.class);
		String jobUrl = (String) map.get("jobUrl");
		return jobUrl.substring("/jobs/".length());
	}

	private String generateFlinkCmd(String jobId, String jobJsonFile) {
		String javaHome = System.getenv("JAVA_HOME");

		return String.format(
				"%s -cp %s com.dtstack.flinkx.launcher.Launcher -mode standalone -jobid %s  -job %s  -pluginRoot %s -flinkconf %s",
				javaHome + "/bin/java",
				flinkXHomePath + ("windows".equals(System.getProperty("os.name")) ? "lib\\*" : "lib/*"),
				jobId,
				jobJsonFile,
				flinkXHomePath + "syncplugins",
				flinkXHomePath + "flinkconf"
		);
	}

	private String generateJobJsonFile(String jobId, String reader, String writer, String setting) {
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

	@SneakyThrows
	private String generateJobSetting() {
		File jsonFile = ResourceUtils.getFile("classpath:job_setting.json");
		return FileUtils.readFileToString(jsonFile,"UTF-8");
	}
}
