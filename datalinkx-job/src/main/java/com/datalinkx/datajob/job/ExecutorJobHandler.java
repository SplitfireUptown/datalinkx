package com.datalinkx.datajob.job;


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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
		Process process = null;
		InputStreamReader ir = null;
		LineNumberReader errorInput = null;
		InputStreamReader ii = null;
		LineNumberReader successInput = null;

		try {
			String cmdStr = this.generateFlinkCmd(jobId, jobJsonFile, otherSetting);
			log.info("job_id: {}, execute job command: {}", jobId, cmdStr);
			process = Runtime.getRuntime().exec(cmdStr);
			ir = new InputStreamReader(process.getErrorStream());
			errorInput = new LineNumberReader(ir);
			Thread threadError = new Thread(new ProcessStreamHandler(errorInput, errorRet));
			threadError.start();

			ii = new InputStreamReader(process.getInputStream());
			successInput = new LineNumberReader(ii);
			Thread threadSuccess = new Thread(new ProcessStreamHandler(successInput, successRet));
			threadSuccess.start();

			threadSuccess.join();
			threadError.join();
			process.waitFor();

		} catch (Exception e) {
			errorRet.append("flink任务提交失败: ").append(e.getMessage());
			log.error("flink任务提交异常", e);
			throw new Exception(errorRet.toString(), e); // 明确异常原因
		} finally {
			// 删除临时文件
			if (FileUtil.exist(jobJsonFile) && !reserveJobGraph) {
				FileUtil.del(new File(jobJsonFile));
			}

			if (process != null) {
				process.destroy(); // 确保销毁进程避免资源泄漏
			}

            if (successInput != null) {
                successInput.close();
            }
            if (errorInput != null) {
                errorInput.close();
            }
            if (ii != null) {
                ii.close();
            }
            if (ir != null) {
                ir.close();
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
