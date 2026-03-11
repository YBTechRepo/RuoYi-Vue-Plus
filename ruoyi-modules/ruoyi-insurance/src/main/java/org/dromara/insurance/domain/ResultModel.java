package org.dromara.insurance.domain;

import lombok.Data;

@Data
public class ResultModel {
    private Integer code;
    private String msg;
    private Object data;

    public static ResultModel success(Object data) {
        ResultModel resultModel = new ResultModel();
        resultModel.setCode(0);
        resultModel.setMsg("success");
        resultModel.setData(data);
        return resultModel;
    }

    public static ResultModel error(String msg) {
        ResultModel resultModel = new ResultModel();
        resultModel.setCode(1);
        resultModel.setMsg(msg);
        return resultModel;
    }
}
