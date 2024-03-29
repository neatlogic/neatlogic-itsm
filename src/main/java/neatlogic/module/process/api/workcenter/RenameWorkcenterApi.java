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
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.auth.WORKCENTER_MODIFY;
import neatlogic.framework.process.constvalue.ProcessWorkcenterType;
import neatlogic.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import neatlogic.framework.process.exception.workcenter.WorkcenterNoModifyAuthException;
import neatlogic.framework.process.exception.workcenter.WorkcenterNotFoundException;
import neatlogic.framework.process.workcenter.dto.WorkcenterCatalogVo;
import neatlogic.framework.process.workcenter.dto.WorkcenterVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.RegexUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

@Transactional
@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class RenameWorkcenterApi extends PrivateApiComponentBase {

    @Resource
    WorkcenterMapper workcenterMapper;

    @Override
    public String getToken() {
        return "workcenter/rename";
    }

    @Override
    public String getName() {
        return "修改工单中心分类名称";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "分类uuid", isRequired = true),
            @Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, desc = "分类名", xss = true),
            @Param(name = "catalogName", type = ApiParamType.STRING, desc = "菜单分类"),
            @Param(name = "isShowTotal", type = ApiParamType.INTEGER, desc = "是否显示总数，默认0：显示待办数")
    })
    @Description(desc = "修改工单中心分类名称接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        WorkcenterVo workcenterVo = JSONObject.toJavaObject(jsonObj, WorkcenterVo.class);
        String uuid = jsonObj.getString("uuid");
        Set<String> systemAuthSet = new HashSet<String>() {{
            this.add(ProcessWorkcenterType.FACTORY.getValue());
            this.add(ProcessWorkcenterType.SYSTEM.getValue());
        }};
        WorkcenterVo oldWorkcenterVo = workcenterMapper.getWorkcenterByUuid(uuid);
        if (oldWorkcenterVo == null) {
            throw new WorkcenterNotFoundException(uuid);
        }
        workcenterVo.setType(oldWorkcenterVo.getType());
        workcenterVo.setSupport(oldWorkcenterVo.getSupport());
        if (systemAuthSet.contains(oldWorkcenterVo.getType()) && !AuthActionChecker.check(WORKCENTER_MODIFY.class.getSimpleName())) {
            throw new WorkcenterNoModifyAuthException();
        }
        if (StringUtils.isNotBlank(workcenterVo.getCatalogName())) {
            WorkcenterCatalogVo workcenterCatalogVo = workcenterMapper.getWorkcenterCatalogByName(workcenterVo.getCatalogName());
            if (workcenterCatalogVo == null) {
                workcenterCatalogVo = new WorkcenterCatalogVo();
                workcenterCatalogVo.setName(workcenterVo.getCatalogName());
                workcenterMapper.insertWorkcenterCatalog(workcenterCatalogVo);
            }
            workcenterVo.setCatalogId(workcenterCatalogVo.getId());
            workcenterMapper.updateWorkcenter(workcenterVo);
        } else {
            //先更新，再清除没有用的catalog
            workcenterMapper.updateWorkcenter(workcenterVo);
            if (oldWorkcenterVo.getCatalogId() != null) {
                if (workcenterMapper.checkWorkcenterCatalogIsUsed(oldWorkcenterVo.getCatalogId()) == 0) {
                    workcenterMapper.deleteWorkcenterCatalogById(oldWorkcenterVo.getCatalogId());
                }
            }
        }
        return null;
    }

}