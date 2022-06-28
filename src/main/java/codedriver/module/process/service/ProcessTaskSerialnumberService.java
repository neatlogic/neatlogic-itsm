/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.service;

import codedriver.framework.process.dto.ProcessTaskSerialNumberPolicyVo;
import com.alibaba.fastjson.JSONArray;

import java.text.DateFormat;

public interface ProcessTaskSerialnumberService {

    /**
     * 工单号策略配置
     *
     * @param startDigit 工单号位数起始值
     * @param endDigit   工单号位数结束值
     * @return
     */
    JSONArray makeupFormAttributeList(int startDigit, int endDigit);

    /**
     * 生成工单号
     *
     * @param processTaskSerialNumberPolicyVo
     * @return
     */
    String genarate(ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo, DateFormat dateFormat);
}
