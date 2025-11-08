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

package neatlogic.module.process.api.workcenter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.config.ConfigManager;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.auth.WORKCENTER_MODIFY;
import neatlogic.framework.process.constvalue.ItsmTenantConfig;
import neatlogic.framework.process.constvalue.ProcessWorkcenterType;
import neatlogic.framework.process.exception.workcenter.WorkcenterCustomCountLimitException;
import neatlogic.framework.process.exception.workcenter.WorkcenterNoModifyAuthException;
import neatlogic.framework.process.exception.workcenter.WorkcenterParamException;
import neatlogic.framework.process.workcenter.dto.WorkcenterAuthorityVo;
import neatlogic.framework.process.workcenter.dto.WorkcenterCatalogVo;
import neatlogic.framework.process.workcenter.dto.WorkcenterVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.RegexUtils;
import neatlogic.module.process.dao.mapper.workcenter.WorkcenterMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
        return "nmpaw.saveworkcenterapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "common.typeuuid"),
            @Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, desc = "nmpaw.editworkcenterapi.input.param.desc.name", xss = true),
            @Param(name = "type", type = ApiParamType.STRING, desc = "nmpaw.saveworkcenterapi.input.param.desc.type，system|custom 默认custom"),
            @Param(name = "catalogName", type = ApiParamType.STRING, desc = "nmpaw.editworkcenterapi.input.param.desc.catalogname"),
            @Param(name = "support", type = ApiParamType.ENUM, rule = "all,mobile,pc", desc = "使用范围，all|pc|mobile，默认值是：all"),
            @Param(name = "conditionConfig", type = ApiParamType.JSONOBJECT, desc = "nmpaw.saveworkcenterapi.input.param.desc.conditionconfig", isRequired = true),
            @Param(name = "authList", type = ApiParamType.JSONARRAY, desc = "nmpaw.saveworkcenterapi.input.param.desc.authlist"),
            @Param(name = "theadList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "nmpaw.editworkcenterapi.input.param.desc.workcentertheadlist"),
            @Param(name = "isShowTotal", type = ApiParamType.INTEGER, desc = "nmpaw.editworkcenterapi.input.param.desc.isshowtotal")
    })
    @Output({@Param(type = ApiParamType.STRING, desc = "common.typeuuid")})
    @Description(desc = "nmpaw.saveworkcenterapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        WorkcenterVo workcenterVo = JSON.toJavaObject(jsonObj, WorkcenterVo.class);
        List<String> systemAuthList = Arrays.asList(ProcessWorkcenterType.FACTORY.getValue(), ProcessWorkcenterType.SYSTEM.getValue());
        if (systemAuthList.contains(workcenterVo.getType())) {
            //判断是否有管理员权限
            if (Boolean.FALSE.equals(AuthActionChecker.check(WORKCENTER_MODIFY.class.getSimpleName()))) {
                throw new WorkcenterNoModifyAuthException();
            }
            if (CollectionUtils.isEmpty(workcenterVo.getAuthList())) {
                throw new WorkcenterParamException("valueList");
            }
            //角色
            for (String value : workcenterVo.getAuthList()) {
                WorkcenterAuthorityVo authorityVo = new WorkcenterAuthorityVo(value);
                authorityVo.setWorkcenterUuid(workcenterVo.getUuid());
                workcenterMapper.insertWorkcenterAuthority(authorityVo);
            }
        } else {
            workcenterMapper.insertWorkcenterOwner(UserContext.get().getUserUuid(true), workcenterVo.getUuid());
            //如果是个人工单分类，则需要校验数量限制
            if (Objects.equals(workcenterVo.getType(), ProcessWorkcenterType.CUSTOM.getValue()) && Boolean.FALSE.equals(AuthActionChecker.check(WORKCENTER_MODIFY.class.getSimpleName()))) {
                int count = workcenterMapper.getCustomWorkcenterCountByOwner(UserContext.get().getUserUuid(true));
                String workcenterCustomLimit = ConfigManager.getConfig(ItsmTenantConfig.WORKCENTER_CUSTOM_LIMIT);
                if (StringUtils.isNotBlank(workcenterCustomLimit)) {
                    int countLimit = Integer.parseInt(workcenterCustomLimit);
                    if (count >= countLimit) {
                        throw new WorkcenterCustomCountLimitException(countLimit);
                    }
                }
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
        workcenterMapper.insertWorkcenter(workcenterVo);

        //update workcenter_thead_config
        if (StringUtils.isNotBlank(workcenterVo.getTheadConfigStr())) {
            workcenterMapper.insertWorkcenterTheadConfig(workcenterVo.getTheadConfigHash(), workcenterVo.getTheadConfigStr());
        }
        return workcenterVo.getUuid();
    }

}