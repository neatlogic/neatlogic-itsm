/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.process.api.collection;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.collection.CollectionVo;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.CollectionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.SEARCH)
@AuthAction(action = PROCESS_BASE.class)
public class SaveCollectionApi extends PrivateApiComponentBase {


    @Override
    public String getToken() {
        return "processtask/collection/save";
    }

    @Override
    public String getName() {
        return "保存集合";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Resource
    private CollectionService collectionService;

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "id"),
            @Param(name = "title", type = ApiParamType.STRING, desc = "标题", isRequired = true),
            @Param(name = "content", type = ApiParamType.STRING, desc = "描述"),
            @Param(name = "phaseList", type = ApiParamType.JSONARRAY, desc = "阶段列表", isRequired = true)})
    @Output({})
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        CollectionVo collectionVo = JSON.toJavaObject(jsonObj, CollectionVo.class);
        collectionService.saveCollection(collectionVo, jsonObj.getLong("id") == null ? "insert" : "update");
        return null;
    }
}
