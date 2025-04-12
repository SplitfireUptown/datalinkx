
package com.datalinkx.rpc.config;

import com.datalinkx.rpc.client.xxljob.XxlLoginClient;
import com.datalinkx.rpc.util.ApplicationContextUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;


@Slf4j
public class XxlClientLoginInterceptor implements RequestInterceptor {
	private static final String HEADER_COOKIE = "Cookie";
	private static final String SET_COOKIE_HEADER = "Set-Cookie";

	private static String xxlJobCookie = "";


	@Value("${xxl-job.username}")
	private String username;

	@Value("${xxl-job.password}")
	private String passwd;


	@Override
	public void apply(RequestTemplate requestTemplate) {

		XxlLoginClient xxlLoginClient = ApplicationContextUtil.getBean(XxlLoginClient.class);

		if (StringUtils.isEmpty(xxlJobCookie)) {

			feign.Response loginResponse = xxlLoginClient.login(username, passwd);
            xxlJobCookie = loginResponse.headers().get(SET_COOKIE_HEADER).stream().findFirst().orElse("");
		}

		requestTemplate.header(HEADER_COOKIE, xxlJobCookie);
	}
}
