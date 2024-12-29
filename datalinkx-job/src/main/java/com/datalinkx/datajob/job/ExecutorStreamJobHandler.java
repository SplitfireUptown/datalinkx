package com.datalinkx.datajob.job;

import java.util.Map;

import com.datalinkx.common.utils.ObjectUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExecutorStreamJobHandler extends ExecutorJobHandler {

    public String execute(String jobId, String reader, String writer, Map<String, String> otherSetting) throws Exception {
        return super.execute(jobId, reader, writer, otherSetting);
    }


    @Override
    public String generateFlinkCmd(String jobId, String jobJsonFile, Map<String, String> otherSetting) {
        String javaHome = System.getenv("JAVA_HOME");
        String os = System.getProperty("os.name").toLowerCase();

        String executeCmd = String.format(
                "%s -cp %s com.dtstack.flinkx.launcher.Launcher -mode standalone -jobid %s" +
                        "  -job %s  -pluginRoot %s -flinkconf %s",
                javaHome + (os.contains("win") ? "\\bin\\java" : "/bin/java"),
                flinkXHomePath + (os.contains("win") ? "lib\\*" : "lib/*"),
                jobId,
                jobJsonFile,
                flinkXHomePath + "syncplugins",
                flinkXHomePath + "flinkconf"
        );


        if (!ObjectUtils.isEmpty(otherSetting.get("savePointPath"))) {
            executeCmd += " -confProp \"{\"flink.checkpoint.interval\":60000}\" ";
            executeCmd = executeCmd + " -s " + otherSetting.get("savePointPath");
        }

        return executeCmd;
    }

    @SneakyThrows
    @Override
    public String generateJobSetting(String jobSettingPath) {
        return super.generateJobSetting("classpath:stream_setting.json");
    }
}
