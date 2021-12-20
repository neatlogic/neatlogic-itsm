/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.workcenter.init;

import codedriver.framework.applicationlistener.core.ModuleInitializedListenerBase;
import codedriver.framework.asynchronization.thread.CodeDriverThread;
import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadpool.CachedThreadPool;
import codedriver.framework.bootstrap.CodedriverWebApplicationContext;
import codedriver.framework.common.constvalue.DeviceType;
import codedriver.framework.dao.mapper.TenantMapper;
import codedriver.framework.dto.TenantVo;
import codedriver.framework.process.constvalue.ProcessWorkcenterInitType;
import codedriver.framework.process.constvalue.ProcessWorkcenterType;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class WorkcenterInit extends ModuleInitializedListenerBase {
    public List<WorkcenterVo> workcenterList = new ArrayList<>();
    private List<TenantVo> tenantList = new ArrayList<>();

    @Resource
    private TenantMapper tenantMapper;

    @Resource
    private WorkcenterMapper workcenterMapper;

    /**
     * @Description:
     * @Author: 89770
     * @Date: 2021/1/5 18:01
     * @Params: * @param null:
     * @Returns: * @return: null
     **/
    private WorkcenterVo all() {
        WorkcenterVo workcenterVo = new WorkcenterVo();
        workcenterVo.setUuid(ProcessWorkcenterInitType.ALL_PROCESSTASK.getValue());
        workcenterVo.setName("所有工单");
        workcenterVo.setConditionConfig("{\n" +
                "    \"handlerType\":\"simple\",\n" +
                "    \"conditionConfig\":{\n" +
                "        \"conditionGroupList\":[\n" +
                "\n" +
                "        ],\n" +
                "        \"conditionGroupRelList\":[\n" +
                "\n" +
                "        ],\n" +
                "        \"startTimeCondition\":{\n" +
                "            \"timeRange\":\"1\",\n" +
                "            \"timeUnit\":\"year\"\n" +
                "        }\n" +
                "    }\n" +
                "}");
        workcenterVo.setType(ProcessWorkcenterType.FACTORY.getValue());
        workcenterVo.setSupport(DeviceType.ALL.getValue());
        workcenterVo.setSort(1);
        return workcenterVo;
    }

    private WorkcenterVo draft() {
        WorkcenterVo workcenterVo = new WorkcenterVo();
        workcenterVo.setUuid(ProcessWorkcenterInitType.DRAFT_PROCESSTASK.getValue());
        workcenterVo.setName("我的草稿");
        workcenterVo.setConditionConfig("{\n" +
                "    \"valueList\":[\n" +
                "        \"common#alluser\"\n" +
                "    ],\n" +
                "    \"name\":\"我的草稿\",\n" +
                "    \"type\":\"system\",\n" +
                "    \"conditionConfig\":{\n" +
                "        \"handlerType\":\"simple\",\n" +
                "        \"startTimeCondition\":{\n" +
                "            \"timeRange\":\"1\",\n" +
                "            \"timeUnit\":\"year\"\n" +
                "        },\n" +
                "        \"conditionGroupList\":[\n" +
                "            {\n" +
                "                \"conditionList\":[\n" +
                "                    {\n" +
                "                        \"expression\":\"include\",\n" +
                "                        \"valueList\":[\n" +
                "                            \"common#loginuser\"\n" +
                "                        ],\n" +
                "                        \"name\":\"owner\",\n" +
                "                        \"type\":\"common\",\n" +
                "                        \"uuid\":\"26dd6599b26c44779df7d504ffd681e9\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"expression\":\"include\",\n" +
                "                        \"valueList\":[\n" +
                "                            \"draft\"\n" +
                "                        ],\n" +
                "                        \"name\":\"status\",\n" +
                "                        \"type\":\"common\",\n" +
                "                        \"uuid\":\"fb487d5c48d4466688258b9c89236c5c\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"channelUuidList\":[\n" +
                "\n" +
                "                ],\n" +
                "                \"conditionRelList\":[\n" +
                "                    {\n" +
                "                        \"joinType\":\"and\",\n" +
                "                        \"from\":\"26dd6599b26c44779df7d504ffd681e9\",\n" +
                "                        \"to\":\"fb487d5c48d4466688258b9c89236c5c\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"uuid\":\"7a244b1161ff4aa58d0499652dcaeb27\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"isProcessingOfMine\":0,\n" +
                "        \"searchContent\":{\n" +
                "            \"firstConditionList\":[\n" +
                "\n" +
                "            ],\n" +
                "            \"searchContent\":[\n" +
                "                {\n" +
                "                    \"handler\":{\n" +
                "                        \"handler\":\"userselect\",\n" +
                "                        \"expressionList\":[\n" +
                "                            {\n" +
                "                                \"expression\":\"include\",\n" +
                "                                \"expressionName\":\"包括\"\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"expression\":\"exclude\",\n" +
                "                                \"expressionName\":\"不包括\"\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"expression\":\"is-null\",\n" +
                "                                \"expressionName\":\"为空\"\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"expression\":\"is-not-null\",\n" +
                "                                \"expressionName\":\"不为空\"\n" +
                "                            }\n" +
                "                        ],\n" +
                "                        \"data\":{\n" +
                "                            \"handler\":\"owner\",\n" +
                "                            \"expressionList\":[\n" +
                "                                {\n" +
                "                                    \"expression\":\"include\",\n" +
                "                                    \"expressionName\":\"包括\"\n" +
                "                                },\n" +
                "                                {\n" +
                "                                    \"expression\":\"exclude\",\n" +
                "                                    \"expressionName\":\"不包括\"\n" +
                "                                },\n" +
                "                                {\n" +
                "                                    \"expression\":\"is-null\",\n" +
                "                                    \"expressionName\":\"为空\"\n" +
                "                                },\n" +
                "                                {\n" +
                "                                    \"expression\":\"is-not-null\",\n" +
                "                                    \"expressionName\":\"不为空\"\n" +
                "                                }\n" +
                "                            ],\n" +
                "                            \"handlerType\":\"userselect\",\n" +
                "                            \"defaultExpression\":\"include\",\n" +
                "                            \"conditionModel\":\"userselect\",\n" +
                "                            \"handlerName\":\"上报人\",\n" +
                "                            \"isMultiple\":true,\n" +
                "                            \"sort\":4,\n" +
                "                            \"type\":\"common\",\n" +
                "                            \"config\":{\n" +
                "                                \"includeList\":[\n" +
                "                                    \"common#vipuser\",\n" +
                "                                    \"common#loginuser\"\n" +
                "                                ],\n" +
                "                                \"excludeList\":[\n" +
                "                                    \"common#alluser\"\n" +
                "                                ],\n" +
                "                                \"multiple\":true,\n" +
                "                                \"isMultiple\":true,\n" +
                "                                \"groupList\":[\n" +
                "                                    \"common\",\n" +
                "                                    \"user\"\n" +
                "                                ],\n" +
                "                                \"initConfig\":{\n" +
                "                                    \"includeList\":[\n" +
                "                                        \"common#vipuser\",\n" +
                "                                        \"common#loginuser\"\n" +
                "                                    ],\n" +
                "                                    \"excludeList\":[\n" +
                "                                        \"common#alluser\"\n" +
                "                                    ],\n" +
                "                                    \"groupList\":[\n" +
                "                                        \"common\",\n" +
                "                                        \"user\"\n" +
                "                                    ]\n" +
                "                                },\n" +
                "                                \"type\":\"userselect\"\n" +
                "                            },\n" +
                "                            \"isShow\":false\n" +
                "                        },\n" +
                "                        \"label\":\"上报人\",\n" +
                "                        \"config\":{\n" +
                "                            \"includeList\":[\n" +
                "                                \"common#vipuser\",\n" +
                "                                \"common#loginuser\"\n" +
                "                            ],\n" +
                "                            \"excludeList\":[\n" +
                "                                \"common#alluser\"\n" +
                "                            ],\n" +
                "                            \"multiple\":true,\n" +
                "                            \"width\":\"100%\",\n" +
                "                            \"isMultiple\":true,\n" +
                "                            \"groupList\":[\n" +
                "                                \"common\",\n" +
                "                                \"user\"\n" +
                "                            ],\n" +
                "                            \"initConfig\":{\n" +
                "                                \"includeList\":[\n" +
                "                                    \"common#vipuser\",\n" +
                "                                    \"common#loginuser\"\n" +
                "                                ],\n" +
                "                                \"excludeList\":[\n" +
                "                                    \"common#alluser\"\n" +
                "                                ],\n" +
                "                                \"groupList\":[\n" +
                "                                    \"common\",\n" +
                "                                    \"user\"\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            \"type\":\"userselect\",\n" +
                "                            \"value\":[\n" +
                "                                \"common#loginuser\"\n" +
                "                            ]\n" +
                "                        },\n" +
                "                        \"key\":\"上报人\"\n" +
                "                    },\n" +
                "                    \"closeable\":true,\n" +
                "                    \"expression\":\"include\",\n" +
                "                    \"split\":\"|\",\n" +
                "                    \"name\":\"owner\",\n" +
                "                    \"label\":\"上报人\",\n" +
                "                    \"type\":\"common\",\n" +
                "                    \"uuid\":\"26dd6599b26c44779df7d504ffd681e9\",\n" +
                "                    \"value\":[\n" +
                "                        \"当前登录人\"\n" +
                "                    ],\n" +
                "                    \"itemCloseable\":true,\n" +
                "                    \"key\":\"上报人\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"handler\":{\n" +
                "                        \"handler\":\"checkbox\",\n" +
                "                        \"expressionList\":[\n" +
                "                            {\n" +
                "                                \"expression\":\"include\",\n" +
                "                                \"expressionName\":\"包括\"\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"expression\":\"exclude\",\n" +
                "                                \"expressionName\":\"不包括\"\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"expression\":\"is-null\",\n" +
                "                                \"expressionName\":\"为空\"\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"expression\":\"is-not-null\",\n" +
                "                                \"expressionName\":\"不为空\"\n" +
                "                            }\n" +
                "                        ],\n" +
                "                        \"data\":{\n" +
                "                            \"handler\":\"status\",\n" +
                "                            \"expressionList\":[\n" +
                "                                {\n" +
                "                                    \"expression\":\"include\",\n" +
                "                                    \"expressionName\":\"包括\"\n" +
                "                                },\n" +
                "                                {\n" +
                "                                    \"expression\":\"exclude\",\n" +
                "                                    \"expressionName\":\"不包括\"\n" +
                "                                },\n" +
                "                                {\n" +
                "                                    \"expression\":\"is-null\",\n" +
                "                                    \"expressionName\":\"为空\"\n" +
                "                                },\n" +
                "                                {\n" +
                "                                    \"expression\":\"is-not-null\",\n" +
                "                                    \"expressionName\":\"不为空\"\n" +
                "                                }\n" +
                "                            ],\n" +
                "                            \"handlerType\":\"checkbox\",\n" +
                "                            \"defaultExpression\":\"include\",\n" +
                "                            \"conditionModel\":\"checkbox\",\n" +
                "                            \"handlerName\":\"工单状态\",\n" +
                "                            \"isMultiple\":true,\n" +
                "                            \"sort\":7,\n" +
                "                            \"type\":\"common\",\n" +
                "                            \"config\":{\n" +
                "                                \"search\":false,\n" +
                "                                \"defaultValue\":\"\",\n" +
                "                                \"dataList\":[\n" +
                "                                    {\n" +
                "                                        \"text\":\"处理中\",\n" +
                "                                        \"value\":\"running\",\n" +
                "                                        \"isShow\":false\n" +
                "                                    },\n" +
                "                                    {\n" +
                "                                        \"text\":\"已取消\",\n" +
                "                                        \"value\":\"aborted\",\n" +
                "                                        \"isShow\":false\n" +
                "                                    },\n" +
                "                                    {\n" +
                "                                        \"text\":\"异常\",\n" +
                "                                        \"value\":\"failed\",\n" +
                "                                        \"isShow\":false\n" +
                "                                    },\n" +
                "                                    {\n" +
                "                                        \"text\":\"已完成\",\n" +
                "                                        \"value\":\"succeed\",\n" +
                "                                        \"isShow\":false\n" +
                "                                    },\n" +
                "                                    {\n" +
                "                                        \"text\":\"未提交\",\n" +
                "                                        \"value\":\"draft\",\n" +
                "                                        \"isShow\":false\n" +
                "                                    },\n" +
                "                                    {\n" +
                "                                        \"text\":\"已评分\",\n" +
                "                                        \"value\":\"scored\",\n" +
                "                                        \"isShow\":false\n" +
                "                                    }\n" +
                "                                ],\n" +
                "                                \"multiple\":true,\n" +
                "                                \"isMultiple\":true,\n" +
                "                                \"type\":\"select\",\n" +
                "                                \"value\":\"\",\n" +
                "                                \"newListData\":[\n" +
                "                                    {\n" +
                "                                        \"text\":\"处理中\",\n" +
                "                                        \"value\":\"running\",\n" +
                "                                        \"isShow\":false\n" +
                "                                    },\n" +
                "                                    {\n" +
                "                                        \"text\":\"已取消\",\n" +
                "                                        \"value\":\"aborted\",\n" +
                "                                        \"isShow\":false\n" +
                "                                    },\n" +
                "                                    {\n" +
                "                                        \"text\":\"异常\",\n" +
                "                                        \"value\":\"failed\",\n" +
                "                                        \"isShow\":false\n" +
                "                                    },\n" +
                "                                    {\n" +
                "                                        \"text\":\"已完成\",\n" +
                "                                        \"value\":\"succeed\",\n" +
                "                                        \"isShow\":false\n" +
                "                                    },\n" +
                "                                    {\n" +
                "                                        \"text\":\"未提交\",\n" +
                "                                        \"value\":\"draft\",\n" +
                "                                        \"isShow\":false\n" +
                "                                    },\n" +
                "                                    {\n" +
                "                                        \"text\":\"已评分\",\n" +
                "                                        \"value\":\"scored\",\n" +
                "                                        \"isShow\":false\n" +
                "                                    }\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            \"isShow\":true\n" +
                "                        },\n" +
                "                        \"label\":\"工单状态\",\n" +
                "                        \"config\":{\n" +
                "                            \"search\":false,\n" +
                "                            \"defaultValue\":\"\",\n" +
                "                            \"dataList\":[\n" +
                "                                {\n" +
                "                                    \"text\":\"处理中\",\n" +
                "                                    \"value\":\"running\",\n" +
                "                                    \"isShow\":false\n" +
                "                                },\n" +
                "                                {\n" +
                "                                    \"text\":\"已取消\",\n" +
                "                                    \"value\":\"aborted\",\n" +
                "                                    \"isShow\":false\n" +
                "                                },\n" +
                "                                {\n" +
                "                                    \"text\":\"异常\",\n" +
                "                                    \"value\":\"failed\",\n" +
                "                                    \"isShow\":false\n" +
                "                                },\n" +
                "                                {\n" +
                "                                    \"text\":\"已完成\",\n" +
                "                                    \"value\":\"succeed\",\n" +
                "                                    \"isShow\":false\n" +
                "                                },\n" +
                "                                {\n" +
                "                                    \"text\":\"未提交\",\n" +
                "                                    \"value\":\"draft\",\n" +
                "                                    \"isShow\":false\n" +
                "                                },\n" +
                "                                {\n" +
                "                                    \"text\":\"已评分\",\n" +
                "                                    \"value\":\"scored\",\n" +
                "                                    \"isShow\":false\n" +
                "                                }\n" +
                "                            ],\n" +
                "                            \"multiple\":true,\n" +
                "                            \"width\":\"100%\",\n" +
                "                            \"isMultiple\":true,\n" +
                "                            \"type\":\"select\",\n" +
                "                            \"value\":[\n" +
                "                                \"draft\"\n" +
                "                            ],\n" +
                "                            \"newListData\":[\n" +
                "                                {\n" +
                "                                    \"text\":\"处理中\",\n" +
                "                                    \"value\":\"running\",\n" +
                "                                    \"isShow\":false\n" +
                "                                },\n" +
                "                                {\n" +
                "                                    \"text\":\"已取消\",\n" +
                "                                    \"value\":\"aborted\",\n" +
                "                                    \"isShow\":false\n" +
                "                                },\n" +
                "                                {\n" +
                "                                    \"text\":\"异常\",\n" +
                "                                    \"value\":\"failed\",\n" +
                "                                    \"isShow\":false\n" +
                "                                },\n" +
                "                                {\n" +
                "                                    \"text\":\"已完成\",\n" +
                "                                    \"value\":\"succeed\",\n" +
                "                                    \"isShow\":false\n" +
                "                                },\n" +
                "                                {\n" +
                "                                    \"text\":\"未提交\",\n" +
                "                                    \"value\":\"draft\",\n" +
                "                                    \"isShow\":false\n" +
                "                                },\n" +
                "                                {\n" +
                "                                    \"text\":\"已评分\",\n" +
                "                                    \"value\":\"scored\",\n" +
                "                                    \"isShow\":false\n" +
                "                                }\n" +
                "                            ]\n" +
                "                        },\n" +
                "                        \"key\":\"工单状态\"\n" +
                "                    },\n" +
                "                    \"closeable\":true,\n" +
                "                    \"expression\":\"include\",\n" +
                "                    \"split\":\"|\",\n" +
                "                    \"name\":\"status\",\n" +
                "                    \"label\":\"工单状态\",\n" +
                "                    \"type\":\"common\",\n" +
                "                    \"uuid\":\"fb487d5c48d4466688258b9c89236c5c\",\n" +
                "                    \"value\":[\n" +
                "                        \"未提交\"\n" +
                "                    ],\n" +
                "                    \"itemCloseable\":true,\n" +
                "                    \"key\":\"工单状态\"\n" +
                "                }\n" +
                "            ]\n" +
                "        },\n" +
                "        \"conditionGroupRelList\":[\n" +
                "\n" +
                "        ]\n" +
                "    }\n" +
                "}");
        workcenterVo.setType(ProcessWorkcenterType.FACTORY.getValue());
        workcenterVo.setSupport(DeviceType.ALL.getValue());
        workcenterVo.setSort(2);
        return workcenterVo;
    }

    @Override
    public void onInitialized(CodedriverWebApplicationContext context) {
        for (TenantVo tenantVo : tenantList) {
            CachedThreadPool.execute(new InsertWorkcenterRunner(tenantVo.getUuid()));
        }
    }

    class InsertWorkcenterRunner extends CodeDriverThread {
        private final String tenantUuid;

        public InsertWorkcenterRunner(String tenantUuid) {
            super("WORKCENTER-INIT-" + tenantUuid);
            this.tenantUuid = tenantUuid;
        }

        @Override
        protected void execute() {
            // 切换租户数据源
            TenantContext.get().switchTenant(tenantUuid).setUseDefaultDatasource(false);
            List<String> initWorkcenterUUidList = Stream.of(ProcessWorkcenterInitType.values()).map(ProcessWorkcenterInitType::getValue).collect(Collectors.toList());
            List<WorkcenterVo> oldVoList = workcenterMapper.getWorkcenterVoListByUuidList(initWorkcenterUUidList);
            Map<String, WorkcenterVo> workcenterVoMap=new HashMap<>();
            if (CollectionUtils.isNotEmpty(oldVoList)) {
                workcenterVoMap = oldVoList.stream().collect(Collectors.toMap(e -> e.getUuid(), e -> e));
            }
            if (CollectionUtils.isNotEmpty(workcenterList)) {
                for (WorkcenterVo workcenterVo : workcenterList) {
                    //TODO 刚开始需要控制授权
                    String uuid = workcenterVo.getUuid();
                    if (workcenterVoMap.containsKey(uuid)) {
                        WorkcenterVo tmpWorkcenterVo = workcenterVoMap.get(uuid);
                        if (StringUtils.isNotBlank(tmpWorkcenterVo.getSupport())) {
                            workcenterVo.setSupport(tmpWorkcenterVo.getSupport());
                        }
                    }

                    workcenterMapper.insertWorkcenter(workcenterVo);
                }
            }
        }
    }

    @Override
    protected void myInit() {
        tenantList = tenantMapper.getAllActiveTenant();
        workcenterList.add(all());
        workcenterList.add(draft());
    }
}
