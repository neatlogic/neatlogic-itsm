/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.api.workcenter;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.auth.WORKCENTER_MODIFY;
import neatlogic.framework.process.constvalue.ProcessWorkcenterType;
import neatlogic.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import neatlogic.framework.process.exception.workcenter.WorkcenterNoModifyAuthException;
import neatlogic.framework.process.exception.workcenter.WorkcenterNotFoundException;
import neatlogic.framework.process.exception.workcenter.WorkcenterParamException;
import neatlogic.framework.process.workcenter.dto.WorkcenterAuthorityVo;
import neatlogic.framework.process.workcenter.dto.WorkcenterVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
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
public class WorkcenterSaveAuthApi extends PrivateApiComponentBase {

    @Resource
    WorkcenterMapper workcenterMapper;

    @Override
    public String getToken() {
        return "/workcenter/auth/save";
    }

    @Override
    public String getName() {
        return "修改工单中心分类授权";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "分类uuid", isRequired = true),
            @Param(name = "type", type = ApiParamType.STRING, desc = "分类类型，system|custom 默认custom", isRequired = true),
            @Param(name = "support", type = ApiParamType.ENUM, rule = "all,mobile,pc", desc = "使用范围，all|pc|mobile，默认值是：all", isRequired = true),
            @Param(name = "authList", type = ApiParamType.JSONARRAY, desc = "授权列表，如果type是system,则必填")
    })
    @Output({@Param(type = ApiParamType.STRING, desc = "分类uuid")})
    @Description(desc = "修改工单中心分类授权接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        WorkcenterVo workcenterVo = JSONObject.toJavaObject(jsonObj, WorkcenterVo.class);
        String uuid = jsonObj.getString("uuid");
        WorkcenterVo oldWorkcenterVo = workcenterMapper.getWorkcenterByUuid(uuid);
        if (oldWorkcenterVo != null) {
            if (Objects.equals(oldWorkcenterVo.getType(), ProcessWorkcenterType.FACTORY.getValue())) {//如果是出厂类型，则不允许修改类型
                workcenterVo.setType(ProcessWorkcenterType.FACTORY.getValue());
            }
        } else {
            throw new WorkcenterNotFoundException(uuid);
        }
        workcenterVo.setCatalogId(oldWorkcenterVo.getCatalogId());
        workcenterVo.setName(oldWorkcenterVo.getName());
        Set<String> systemAuthSet = new HashSet<String>() {{
            this.add(ProcessWorkcenterType.FACTORY.getValue());
            this.add(ProcessWorkcenterType.SYSTEM.getValue());
        }};
        if (systemAuthSet.contains(workcenterVo.getType()) || systemAuthSet.contains(oldWorkcenterVo.getType())) {
            //判断是否有管理员权限
            if (!AuthActionChecker.check(WORKCENTER_MODIFY.class.getSimpleName())) {
                throw new WorkcenterNoModifyAuthException();
            }
            workcenterMapper.deleteWorkcenterAuthorityByUuid(uuid);
        }
        if (systemAuthSet.contains(workcenterVo.getType())) {
            if (CollectionUtils.isEmpty(workcenterVo.getAuthList())) {
                throw new WorkcenterParamException("authList");
            }
            //更新授权列表
            for (String value : workcenterVo.getAuthList()) {
                WorkcenterAuthorityVo authorityVo = new WorkcenterAuthorityVo(value);
                authorityVo.setWorkcenterUuid(workcenterVo.getUuid());
                workcenterMapper.insertWorkcenterAuthority(authorityVo);
            }
            workcenterMapper.deleteWorkcenterOwnerByUuid(uuid);
        } else {
            workcenterMapper.deleteWorkcenterOwnerByUuid(uuid);
            workcenterMapper.insertWorkcenterOwner(UserContext.get().getUserUuid(true), workcenterVo.getUuid());
        }
        //更新support
        workcenterMapper.updateWorkcenter(workcenterVo);

        return null;
    }

}