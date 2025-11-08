/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.process.api.processtask.fulltextindex;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.fulltextindex.core.FullTextIndexHandlerFactory;
import neatlogic.framework.fulltextindex.core.IFullTextIndexHandler;
import neatlogic.framework.process.auth.PROCESSTASK_MODIFY;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.fulltextindex.ProcessFullTextIndexType;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
                for(Long idObj : idList ){
                    handler.createIndex(idObj);
                }
            }else{
                Integer count = processTaskMapper.getAllProcessTaskCount();
                ProcessTaskVo processTaskVo = new ProcessTaskVo();
                processTaskVo.getPageSize();
                processTaskVo.setRowNum(count);
                for (int i = 1; i <= processTaskVo.getPageCount(); i++) {
                    processTaskVo.setCurrentPage(i);
                    idList = processTaskMapper.getProcessTaskIdList(processTaskVo);
                    for(Long idObj : idList ){
                        handler.createIndex(idObj);
                    }
                }
            }

        }
        return null;
    }

    @Override
    public String getToken() {
        return "/processtask/fulltext/index/rebuild";
    }
}
