/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.workcenter.init;

import codedriver.framework.common.constvalue.DeviceType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.constvalue.UserType;
import codedriver.framework.process.constvalue.ProcessWorkcenterInitType;
import codedriver.framework.process.constvalue.ProcessWorkcenterType;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.workcenter.dto.WorkcenterAuthorityVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.startup.StartupBase;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Title: 工单中心默认分类
 * @Package codedriver.module.process.workcenter.init
 * @Description: 工单中心默认分类
 * @Author: 89770
 * @Date: 2021/1/5 17:55
 **/
@Component
public class WorkcenterInit extends StartupBase {
    public List<WorkcenterVo> workcenterList = new ArrayList<>();

    @Resource
    private WorkcenterMapper workcenterMapper;

    private WorkcenterVo all() {
        WorkcenterVo workcenterVo = new WorkcenterVo();
        workcenterVo.setUuid(ProcessWorkcenterInitType.ALL_PROCESSTASK.getValue());
        workcenterVo.setName(ProcessWorkcenterInitType.ALL_PROCESSTASK.getName());
        workcenterVo.setConditionConfigStr("{\n" +
                "        \"conditionGroupList\": [],\n" +
                "        \"conditionGroupRelList\": [],\n" +
                "        \"startTimeCondition\": {\n" +
                "            \"timeRange\": \"1\",\n" +
                "            \"timeUnit\": \"year\"\n" +
                "        },\n" +
                "        \"handlerType\": \"simple\",\n" +
                "        \"isProcessingOfMine\": 0,\n" +
                "        \"uuid\": \"allProcessTask\"\n" +
                "    }");
        workcenterVo.setType(ProcessWorkcenterType.FACTORY.getValue());
        workcenterVo.setSupport(DeviceType.ALL.getValue());
        workcenterVo.setSort(1);
        return workcenterVo;
    }

    private WorkcenterVo draft() {
        WorkcenterVo workcenterVo = new WorkcenterVo();
        workcenterVo.setUuid(ProcessWorkcenterInitType.DRAFT_PROCESSTASK.getValue());
        workcenterVo.setName(ProcessWorkcenterInitType.DRAFT_PROCESSTASK.getName());
        workcenterVo.setConditionConfigStr("{\n" +
                "        \"handlerType\": \"simple\",\n" +
                "        \"startTimeCondition\": {\n" +
                "            \"timeRange\": \"1\",\n" +
                "            \"timeUnit\": \"year\"\n" +
                "        },\n" +
                "        \"conditionGroupList\": [\n" +
                "            {\n" +
                "                \"uuid\": \"05158b6b22d544b39181ef0a1a38c776\",\n" +
                "                \"conditionList\": [\n" +
                "                    {\n" +
                "                        \"uuid\": \"77d6733ddfdb47e2acb6f11f5dd3f2f5\",\n" +
                "                        \"type\": \"common\",\n" +
                "                        \"name\": \"status\",\n" +
                "                        \"valueList\": [\n" +
                "                            \"draft\"\n" +
                "                        ],\n" +
                "                        \"expression\": \"include\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"uuid\": \"822cbdec2ec44f0e8ae7af4e065b4d48\",\n" +
                "                        \"type\": \"common\",\n" +
                "                        \"name\": \"owner\",\n" +
                "                        \"valueList\": [\n" +
                "                            \"common#loginuser\"\n" +
                "                        ],\n" +
                "                        \"expression\": \"include\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"conditionRelList\": [\n" +
                "                    {\n" +
                "                        \"from\": \"77d6733ddfdb47e2acb6f11f5dd3f2f5\",\n" +
                "                        \"to\": \"822cbdec2ec44f0e8ae7af4e065b4d48\",\n" +
                "                        \"joinType\": \"and\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"channelUuidList\": []\n" +
                "            }\n" +
                "        ],\n" +
                "        \"isProcessingOfMine\": 0,\n" +
                "        \"conditionGroupRelList\": [],\n" +
                "        \"uuid\": \"draftProcessTask\"\n" +
                "    }");
        workcenterVo.setType(ProcessWorkcenterType.FACTORY.getValue());
        workcenterVo.setSupport(DeviceType.ALL.getValue());
        workcenterVo.setSort(4);
        return workcenterVo;
    }

    private WorkcenterVo doneOfMine() {
        WorkcenterVo workcenterVo = new WorkcenterVo();
        workcenterVo.setUuid(ProcessWorkcenterInitType.DONE_OF_MINE_PROCESSTASK.getValue());
        workcenterVo.setName(ProcessWorkcenterInitType.DONE_OF_MINE_PROCESSTASK.getName());
        workcenterVo.setConditionConfigStr("{\n" +
                "        \"handlerType\": \"simple\",\n" +
                "        \"startTimeCondition\": {\n" +
                "            \"timeRange\": \"1\",\n" +
                "            \"timeUnit\": \"year\"\n" +
                "        },\n" +
                "        \"conditionGroupList\": [\n" +
                "            {\n" +
                "                \"uuid\": \"7b4d42e03ff4413483836c3289a938af\",\n" +
                "                \"conditionList\": [\n" +
                "                    {\n" +
                "                        \"uuid\": \"64b156e0c67c4755864e6b4c36e0f2ff\",\n" +
                "                        \"type\": \"common\",\n" +
                "                        \"name\": \"aboutme\",\n" +
                "                        \"valueList\": [\n" +
                "                            \"doneOfMine\"\n" +
                "                        ],\n" +
                "                        \"expression\": \"include\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"conditionRelList\": [],\n" +
                "                \"channelUuidList\": []\n" +
                "            }\n" +
                "        ],\n" +
                "        \"isProcessingOfMine\": 0,\n" +
                "        \"conditionGroupRelList\": [],\n" +
                "        \"isProcessing\": 0,\n" +
                "        \"uuid\": \"doneOfMineProcessTask\"\n" +
                "    }");
        workcenterVo.setType(ProcessWorkcenterType.FACTORY.getValue());
        workcenterVo.setSupport(DeviceType.ALL.getValue());
        workcenterVo.setSort(3);
        return workcenterVo;
    }

    private WorkcenterVo processingOfMine() {
        WorkcenterVo workcenterVo = new WorkcenterVo();
        workcenterVo.setUuid(ProcessWorkcenterInitType.PROCESSING_OF_MINE_PROCESSTASK.getValue());
        workcenterVo.setName(ProcessWorkcenterInitType.PROCESSING_OF_MINE_PROCESSTASK.getName());
        workcenterVo.setConditionConfigStr("{\n" +
                "        \"conditionGroupList\": [],\n" +
                "        \"conditionGroupRelList\": [],\n" +
                "        \"startTimeCondition\": {\n" +
                "            \"timeRange\": \"1\",\n" +
                "            \"timeUnit\": \"year\"\n" +
                "        },\n" +
                "        \"handlerType\": \"simple\",\n" +
                "        \"isProcessingOfMine\": 1,\n" +
                "        \"uuid\": \"allProcessTask\"\n" +
                "    }");
        workcenterVo.setType(ProcessWorkcenterType.FACTORY.getValue());
        workcenterVo.setSupport(DeviceType.ALL.getValue());
        workcenterVo.setSort(2);
        return workcenterVo;
    }

    {
        workcenterList.add(all());
        workcenterList.add(draft());
        workcenterList.add(doneOfMine());
        workcenterList.add(processingOfMine());
    }

    @Override
    public String getName() {
        return "初始化工单中心出厂分类";
    }

    @Override
    public void executeForCurrentTenant() {
        if (CollectionUtils.isNotEmpty(workcenterList)) {
            //获取工单中心需要初始化的分类的uuidList
            List<String> initWorkcenterUUidList = Stream.of(ProcessWorkcenterInitType.values()).map(ProcessWorkcenterInitType::getValue).collect(Collectors.toList());
            /*
              工单中心分类初始化分为分类初始化、分类权限初始化
             */
            List<WorkcenterVo> oldWorkcenterVoList = workcenterMapper.getWorkcenterVoListByUuidList(initWorkcenterUUidList);
            Map<String, WorkcenterVo> workcenterVoMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(oldWorkcenterVoList)) {
                workcenterVoMap = oldWorkcenterVoList.stream().collect(Collectors.toMap(WorkcenterVo::getUuid, e -> e));
            }
            List<WorkcenterAuthorityVo> oldAuthorityVoList = workcenterMapper.getWorkcenterAuthorityVoListByUuidList(initWorkcenterUUidList);
            for (WorkcenterVo workcenterVo : workcenterList) {
                String uuid = workcenterVo.getUuid();
                //初始化工单中心分类
                WorkcenterVo oldWorkcenterVo = workcenterVoMap.get(uuid);
                if (oldWorkcenterVo != null) {
                    if (StringUtils.isNotBlank(oldWorkcenterVo.getSupport())) {
                        workcenterVo.setSupport(oldWorkcenterVo.getSupport());
                    }
                }
                workcenterMapper.insertWorkcenter(workcenterVo);
                //初始化工单中心分类权限
                if (oldAuthorityVoList.stream().noneMatch(o -> Objects.equals(o.getWorkcenterUuid(), uuid))) {
                    workcenterMapper.insertWorkcenterAuthority(new WorkcenterAuthorityVo(uuid, GroupSearch.COMMON.getValue(), UserType.ALL.getValue()));
                }
            }
        }
    }

    @Override
    public void executeForAllTenant() {

    }

    @Override
    public int sort() {
        return 0;
    }
}
