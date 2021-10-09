/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.processtask.fulltextindex;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.fulltextindex.core.FullTextIndexHandlerFactory;
import codedriver.framework.fulltextindex.core.IFullTextIndexHandler;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.fulltextindex.ProcessFullTextIndexType;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.process.auth.PROCESSTASK_MODIFY;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lvzk
 * @since 2021/3/23 17:23
 **/
@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESSTASK_MODIFY.class)
public class ProcessTaskFulltextIndexRebuildApi extends PrivateApiComponentBase {
    @Resource
    ProcessTaskMapper processTaskMapper;

    @Override
    public String getName() {
        return "重建工单索引";
    }

    @Override
    public String getConfig() {
        return null;
    }
    @Input({@Param(name = "idList", type = ApiParamType.JSONARRAY, desc = "工单idList") })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray idArray = jsonObj.getJSONArray("idList");
        List<Long> idList = null;
        //创建全文检索索引
        IFullTextIndexHandler handler = FullTextIndexHandlerFactory.getHandler(ProcessFullTextIndexType.PROCESSTASK);
        if (handler != null) {
            if(CollectionUtils.isNotEmpty(idArray)){
                idList = JSONObject.parseArray(idArray.toJSONString(), Long.class);
            }else{
                idList = new ArrayList<>();
            }
            for(Long idObj : idList ){
                handler.createIndex(idObj);
            }
        }
        return null;
    }

    @Override
    public String getToken() {
        return "/processtask/fulltext/index/rebuild";
    }
}
