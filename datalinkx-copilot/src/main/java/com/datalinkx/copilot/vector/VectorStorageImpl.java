package com.datalinkx.copilot.vector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import com.datalinkx.common.constants.MetaConstants;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public abstract class VectorStorageImpl implements VectorStorage, ApplicationContextAware {
    public Map<String, VectorStorage> vectorStorageEngine = new ConcurrentHashMap<>();

    private ApplicationContext applicationContext;


    @PostConstruct
    public void init() {
        this.vectorStorageEngine.put(MetaConstants.CopilotConstant.VECTOR_ES_ENGINE, applicationContext.getBean(ElasticSearchVectorStorage.class));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
