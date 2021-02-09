package codedriver.module.process.service;

import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @Title: NewWorkcenterService
 * @Package: codedriver.module.process.service
 * @Description: TODO
 * @Author: 89770
 * @Date: 2021/1/19 20:08
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
public interface NewWorkcenterService {

    /**
     * @Description: 搜索工单
     * @Author: 89770
     * @Date: 2021/1/19 20:09
     * @Params: [workcenterVo]
     * @Returns: com.alibaba.fastjson.JSONObject
     **/
    JSONObject doSearch(WorkcenterVo workcenterVo);

    /**
     * @Description: 搜索工单数
     * @Author: 89770
     * @Date: 2021/1/19 20:11
     * @Params: [workcenterVo]
     * @Returns: java.lang.Integer
     **/
    Integer doSearchLimitCount(WorkcenterVo workcenterVo);


    /**
     * @Description: 搜索工单号、标题、内容
     * @Author: 89770
     * @Date: 2021/1/19 20:09
     * @Params: [workcenterVo]
     * @Returns: com.alibaba.fastjson.JSONObject
     **/
    public List<ProcessTaskVo> doSearchKeyword(WorkcenterVo workcenterVo);

    /**
     * @Description: 根据标题 id获取所有过滤选项 pc端
     * @Author: 89770
     * @Date: 2021/2/4 18:59
     * @Params: [keyword, pageSize]
     * @Returns: com.alibaba.fastjson.JSONArray
     **/
    public JSONArray getKeywordOptionsPCNew(WorkcenterVo workcenterVo);

}
