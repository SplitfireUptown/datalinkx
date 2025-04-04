package com.datalinkx.dataserver.config.filter;


import com.datalinkx.common.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Component;

/**
 * SpringBean过滤加载逻辑
 */
@Component
public class SpringBeanFilter implements TypeFilter {

    private final Environment environment;

    @Autowired
    public SpringBeanFilter(Environment environment) {
        this.environment = environment;
    }

    public final String COPILOT_PACKAGE = "com.datalinkx.copilot";

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) {
        // 如果配置了大模型配置，则加载
        if (metadataReader.getClassMetadata().getClassName().startsWith(COPILOT_PACKAGE) && ObjectUtils.isEmpty(environment.getProperty("llm.embedding"))) {
            return true;
        }
        return false;
    }
}
