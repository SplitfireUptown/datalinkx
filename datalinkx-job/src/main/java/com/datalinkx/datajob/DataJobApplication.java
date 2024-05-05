// CHECKSTYLE:OFF
package com.datalinkx.datajob;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@ComponentScan(basePackages = {"com.datalinkx.*.**"})
public class DataJobApplication {

	public static void main(String[] args) {
        SpringApplication.run(DataJobApplication.class, args);
	}
}
