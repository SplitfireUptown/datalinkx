package com.datalinkx.dataserver.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageVo<T> {
	//当前查询的页码
	private Integer pageNo;
	//每页条数
	private Integer pageSize;
	//总数据量
	private Long total;
	private Integer totalPage;
	private T data;
}

