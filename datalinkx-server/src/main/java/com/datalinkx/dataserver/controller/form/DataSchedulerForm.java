package com.datalinkx.dataserver.controller.form;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


/**
 * 任务相关参数
 *
 * @author uptown
 * @create 01 27, 2021
 * @since 1.0.0
 */
@Data
public class DataSchedulerForm {

    @Data
    public static class RetrieveForm {
        @JsonProperty("user_id")
        private String userId;

        @JsonProperty("scheduler_id")
        private String schedulerId;
    }

    @Data
    public static class ListForm {
        @JsonProperty("user_id")
        private String userId;
        private Integer limit;
        private String keyword;
        private Integer page;
        private String status;
        private String startDate;
        private String endDate;
        private String owner;
    }

    @Data
    public static class CreateForm  extends  ChangeBaseForm {
    }

    @Data
    public static class UpdateForm extends ChangeBaseForm {
        @JsonProperty("scheduler_id")
        private String schedulerId;
    }

    @Data
    public static class ChangeBaseForm {
        @JsonProperty("user_id")
        private String userId;
        @JsonProperty("scheduler_name")
        private String schedulerName;
        @JsonProperty("scheduler_desc")
        private String schedulerDesc;
        private List<String> tables;

//        private Object timing;
    }

    @Data
    public static class DeleteForm {
        @JsonProperty("user_id")
        private String userId;
        @JsonProperty("scheduler_id")
        private String schedulerId;
    }

    @Data
    public static class TriggerForm {
        @JsonProperty("user_id")
        private String userId;
        @JsonProperty("scheduler_id")
        private String schedulerId;
        private List<String> tables;
        private Integer full;

    }

    @Data
    public static class StartForm {
        @JsonProperty("user_id")
        private String userId;
        @JsonProperty("scheduler_id")
        private String schedulerId;
    }

    @Data
    public static class StopForm {
        @JsonProperty("user_id")
        private String userId;
        @JsonProperty("scheduler_id")
        private String schedulerId;
    }

}
