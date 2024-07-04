package com.datalinkx.dataserver;

import com.datalinkx.dataserver.config.SpringBeanFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;



@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
@EnableJpaRepositories(basePackages = {"com.datalinkx.dataserver.*"})
@EntityScan(basePackages = {"com.datalinkx.dataserver.*"})
@ComponentScan(
        basePackages = {"com.datalinkx.*.**"},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.CUSTOM, classes = { SpringBeanFilter.class })
        }
)
public class DataServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataServerApplication.class, args);
    }
}
