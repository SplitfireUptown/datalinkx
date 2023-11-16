// CHECKSTYLE:OFF
package com.datalinkx.dataio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.datalinkx.*.**"})
public class DataJobApplication {

	public static void main(String[] args) {
        SpringApplication.run(DataJobApplication.class, args);
	}
}
