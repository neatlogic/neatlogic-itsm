/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.process.api.workcenter;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.auth.WORKCENTER_MODIFY;
import neatlogic.framework.process.constvalue.ProcessWorkcenterType;
import neatlogic.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import neatlogic.framework.process.exception.workcenter.WorkcenterNoAuthException;
import neatlogic.framework.process.exception.workcenter.WorkcenterNotFoundException;
import neatlogic.framework.process.exception.workcenter.WorkcenterParamException;
import neatlogic.framework.process.workcenter.dto.WorkcenterAuthorityVo;
import neatlogic.framework.process.workcenter.dto.WorkcenterCatalogVo;
import neatlogic.framework.process.workcenter.dto.WorkcenterVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.RegexUtils;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Transactional
@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class SaveWorkcenterApi extends PrivateApiComponentBase {

    @Resource
    WorkcenterMapper workcenterMapper;

    @Override
    public String getToken() {
        return "workcenter/save";
    }

    @Override
    public String getName() {
        return "工单中心分类保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "分类uuid"),
            @Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, desc = "分类名", xss = true),
            @Param(name = "type", type = ApiParamType.STRING, desc = "分类类型，system|custom 默认custom"),
            @Param(name = "catalogName", type = ApiParamType.STRING, desc = "菜单分类"),
            @Param(name = "support", type = ApiParamType.ENUM, rule = "all,mobile,pc", desc = "使用范围，all|pc|mobile，默认值是：all"),
            @Param(name = "conditionConfig", type = ApiParamType.JSONOBJECT, desc = "分类过滤配置，json格式", isRequired = true),
            @Param(name = "authList", type = ApiParamType.JSONARRAY, desc = "授权列表，如果type是system,则必填"),
            @Param(name = "isShowTotal", type = ApiParamType.INTEGER, desc = "是否显示总数，默认0：显示待办数")
    })
    @Output({@Param(type = ApiParamType.STRING, desc = "分类uuid")})
    @Description(desc = "工单中心分类新增接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        WorkcenterVo workcenterVo = JSONObject.toJavaObject(jsonObj, WorkcenterVo.class);
        String uuid = jsonObj.getString("uuid");
        WorkcenterVo oldWorkcenterVo = null;
        if (StringUtils.isNotBlank(uuid)) {
            oldWorkcenterVo = workcenterMapper.getWorkcenterByUuid(uuid);
            if (oldWorkcenterVo != null) {
                if (Objects.equals(oldWorkcenterVo.getType(), ProcessWorkcenterType.FACTORY.getValue())) {//如果是出厂类型，则不允许修改类型
                    workcenterVo.setType(ProcessWorkcenterType.FACTORY.getValue());
                }
            } else {
                throw new WorkcenterNotFoundException(uuid);
            }
        }
        Set<String> systemAuthSet = new HashSet<String>() {{
            this.add(ProcessWorkcenterType.FACTORY.getValue());
            this.add(ProcessWorkcenterType.SYSTEM.getValue());
        }};
        if (systemAuthSet.contains(workcenterVo.getType()) || (oldWorkcenterVo != null && systemAuthSet.contains(oldWorkcenterVo.getType()))) {
            //判断是否有管理员权限
            if (!AuthActionChecker.check(WORKCENTER_MODIFY.class.getSimpleName())) {
                throw new WorkcenterNoAuthException("管理");
            }
            workcenterMapper.deleteWorkcenterAuthorityByUuid(workcenterVo.getUuid());
        }
        if (systemAuthSet.contains(workcenterVo.getType())) {
            if (CollectionUtils.isEmpty(workcenterVo.getAuthList())) {
                throw new WorkcenterParamException("valueList");
            }
            //更新角色
            for (String value : workcenterVo.getAuthList()) {
                WorkcenterAuthorityVo authorityVo = new WorkcenterAuthorityVo(value);
                authorityVo.setWorkcenterUuid(workcenterVo.getUuid());
                workcenterMapper.insertWorkcenterAuthority(authorityVo);
            }
        } else {
            if (StringUtils.isBlank(uuid)) {
                workcenterMapper.insertWorkcenterOwner(UserContext.get().getUserUuid(true), workcenterVo.getUuid());
            }
        }
        if (StringUtils.isNotBlank(workcenterVo.getCatalogName())) {
            WorkcenterCatalogVo workcenterCatalogVo = workcenterMapper.getWorkcenterCatalogByName(workcenterVo.getCatalogName());
            if (workcenterCatalogVo == null) {
                workcenterCatalogVo = new WorkcenterCatalogVo();
                workcenterCatalogVo.setName(workcenterVo.getCatalogName());
                workcenterMapper.insertWorkcenterCatalog(workcenterCatalogVo);
            }
            workcenterVo.setCatalogId(workcenterCatalogVo.getId());
        }
        if (StringUtils.isBlank(uuid)) {
            workcenterMapper.insertWorkcenter(workcenterVo);
        } else {
            workcenterMapper.updateWorkcenter(workcenterVo);
        }
        return workcenterVo.getUuid();
    }

}