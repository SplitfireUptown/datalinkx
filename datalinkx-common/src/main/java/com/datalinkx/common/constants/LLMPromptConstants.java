package com.datalinkx.common.constants;

public class LLMPromptConstants {

    public static final String JOB_LIST_SCHEMA_PROMPT = "- job_name：任务名称\n" +
            "- type：任务类型，0 批式任务 1 流式任务 2 计算任务";


    public static final String JOB_INFO_SCHEMA_PROMPT = "- job_name：任务名称\n" +
            "- type：任务类型，0 批式任务 1 流式任务 2 计算任务\n" +
            "- from_tb_name: 来源数据表\n" +
            "- to_tb_name：目标数据表\n" +
            "- scheduler_conf：定时策略\n" +
            "- cover：数据覆盖，0关闭，1开启\n" +
            "- field_mappings：字段映射关系\n" +
            "- sync_mode：json结构同步设置，overwrite全量更新，increment增量更新，increate_field增量字段，increate_value增量字段值";


    public static final String DS_LIST_SCHEMA_PROMPT = "- name：数据源名称\n" +
            "- type：数据类型，0 批式任务 1 流式任务 2 计算任务\n" +
            "- host: 数据源连接地址\n" +
            "- port：端口";


    public static final String DS_INFO_SCHEMA_PROMPT = "- name：数据源名称\n" +
            "- type：数据类型，0 批式任务 1 流式任务 2 计算任务\n" +
            "- host: 数据源连接地址\n" +
            "- port：端口\n" +
            "- username：用户名\n" +
            "- config：通用配置\n" +
            "- ctime：创建时间\n";
}
