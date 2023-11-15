package com.datalinkx.dataio.config;

import com.datalinkx.dataclient.client.DatalinkXClientUtils;
import com.datalinkx.dataio.client.datalinkxserver.DatalinkXServerClient;
import com.datalinkx.dataio.client.flink.FlinkClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebClientConfig implements WebMvcConfigurer {
	@Bean
	public DatalinkXServerClient datalinkXServerClient(ClientProperties clientProperties) {
		return DatalinkXClientUtils.createClient("dataserver",
				clientProperties.getDataserver(), DatalinkXServerClient.class);
	}

	@Bean
	@ConditionalOnProperty(prefix = "client.flink", name = "url")
	public FlinkClient flinkClient(ClientProperties clientProperties) {
		return DatalinkXClientUtils.createClient("flink", clientProperties.getFlink(), FlinkClient.class);
	}
}
