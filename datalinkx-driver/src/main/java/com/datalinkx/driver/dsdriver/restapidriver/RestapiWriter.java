package com.datalinkx.driver.dsdriver.restapidriver;

import com.datalinkx.driver.dsdriver.base.writer.AbstractWriter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class RestapiWriter extends AbstractWriter {

    private String url;

    private String method = "post";

    @Builder.Default
    private List<Map<String, Object>> header = new ArrayList<>();

    @Builder.Default
    private List<Map<String, Object>> body = new ArrayList<>();

    @Builder.Default
    private Map<String, Object> params = new HashMap<>();

    @Builder.Default
    List<String> column = new ArrayList<>();

    private Integer batchInterval = 10000;

}
