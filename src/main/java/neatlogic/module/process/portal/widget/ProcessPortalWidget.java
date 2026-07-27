/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.process.portal.widget;

import neatlogic.framework.portal.widget.core.IPortalWidget;
import neatlogic.framework.portal.widget.core.IPortalWidgetGroup;

public enum ProcessPortalWidget implements IPortalWidget {
    allProcessTask("allProcessTask", "所有工单", 1, ProcessPortalWidgetGroup.group4),
    processingOfMineProcessTask("processingOfMineProcessTask", "我的待办", 2, ProcessPortalWidgetGroup.group4),
    doneOfMineProcessTask("doneOfMineProcessTask", "我的已办", 3, ProcessPortalWidgetGroup.group4),
    draftProcessTask("draftProcessTask", "我的草稿", 4, ProcessPortalWidgetGroup.group4),
    favoritedServices("favoritedServices", "收藏服务", 5, ProcessPortalWidgetGroup.group4),
    recentTaskList("recentTaskList", "最近工单", 6),
    approvalTodoList("approvalTodoList", "审批待办", 7),
    focusTaskList("focusTaskList", "我的关注", 8),
    recentOperationList("recentOperationList", "最近操作", 9),
    quickMyTodo("quickMyTodo", "我的待办入口", 10),
    trendPanel("trendPanel", "工单趋势", 11),
    workbenchTable("workbenchTable", "服务概览", 12),
    taskTypeDistribution("taskTypeDistribution", "工单类型分布", 13),
    slaRiskList("slaRiskList", "SLA 风险", 14),
    efficiencyPanel("efficiencyPanel", "处理效率", 15),
    metricFavorite("metricFavorite", "服务收藏", 16),
    serviceDistribution("serviceDistribution", "服务分布", 17),
    quickActionGrid("quickActionGrid", "快捷操作", 18),
    serviceCatalogEntry("serviceCatalogEntry", "服务目录入口", 19),
    healthRing("healthRing", "健康状态", 20),
    teamLoad("teamLoad", "团队负载", 21),
    changeCalendar("changeCalendar", "变更日历", 22),
    systemNoticeList("systemNoticeList", "系统通知", 23),
    knowledgeHelp("knowledgeHelp", "知识帮助", 24),
    announcementPanel("announcementPanel", "公告栏", 25),
    supportPanel("supportPanel", "帮助支持", 26),
    faqList("faqList", "常见问题", 27),
    heroBanner("heroBanner", "首页横幅", 28),
    metricTodo("metricTodo", "待办指标", 29),
    metricOverdue("metricOverdue", "超时指标", 30),
    metricDone("metricDone", "今日完成", 31),
    metricSatisfaction("metricSatisfaction", "满意度", 32),
    testPortalWidget1("testPortalWidget1", "测试小部件1", 1, ProcessPortalWidgetGroup.group1),
    testPortalWidget2("testPortalWidget2", "测试小部件2", 2, ProcessPortalWidgetGroup.group1),
    testPortalWidget3("testPortalWidget3", "测试小部件3", 3, ProcessPortalWidgetGroup.group1),
    testPortalWidget4("testPortalWidget4", "测试小部件4", 4, ProcessPortalWidgetGroup.group1),
    testPortalWidget5("testPortalWidget5", "测试小部件5", 5, ProcessPortalWidgetGroup.group1),
    testPortalWidget6("testPortalWidget6", "测试小部件6", 6, ProcessPortalWidgetGroup.group1),
    testPortalWidget7("testPortalWidget7", "测试小部件7", 7, ProcessPortalWidgetGroup.group1),
    testPortalWidget8("testPortalWidget8", "测试小部件8", 8, ProcessPortalWidgetGroup.group1),
    testPortalWidget9("testPortalWidget9", "测试小部件9", 9, ProcessPortalWidgetGroup.group1),
    testPortalWidget10("testPortalWidget10", "测试小部件10", 10, ProcessPortalWidgetGroup.group1),
    testPortalWidget11("testPortalWidget11", "测试小部件11", 11, ProcessPortalWidgetGroup.group2),
    testPortalWidget12("testPortalWidget12", "测试小部件12", 12, ProcessPortalWidgetGroup.group2),
    testPortalWidget13("testPortalWidget13", "测试小部件13", 13, ProcessPortalWidgetGroup.group2),
    testPortalWidget14("testPortalWidget14", "测试小部件14", 14, ProcessPortalWidgetGroup.group2),
    testPortalWidget15("testPortalWidget15", "测试小部件15", 15, ProcessPortalWidgetGroup.group2),
    testPortalWidget16("testPortalWidget16", "测试小部件16", 16, ProcessPortalWidgetGroup.group2),
    testPortalWidget17("testPortalWidget17", "测试小部件17", 17, ProcessPortalWidgetGroup.group2),
    testPortalWidget18("testPortalWidget18", "测试小部件18", 18, ProcessPortalWidgetGroup.group2),
    testPortalWidget19("testPortalWidget19", "测试小部件19", 19, ProcessPortalWidgetGroup.group2),
    testPortalWidget20("testPortalWidget20", "测试小部件20", 20, ProcessPortalWidgetGroup.group2),
    testPortalWidget21("testPortalWidget21", "测试小部件21", 21, ProcessPortalWidgetGroup.group3),
    testPortalWidget22("testPortalWidget22", "测试小部件22", 22, ProcessPortalWidgetGroup.group3),
    testPortalWidget23("testPortalWidget23", "测试小部件23", 23, ProcessPortalWidgetGroup.group3),
    testPortalWidget24("testPortalWidget24", "测试小部件24", 24, ProcessPortalWidgetGroup.group3),
    testPortalWidget25("testPortalWidget25", "测试小部件25", 25, ProcessPortalWidgetGroup.group3),
    testPortalWidget26("testPortalWidget26", "测试小部件26", 26, ProcessPortalWidgetGroup.group3),
    testPortalWidget27("testPortalWidget27", "测试小部件27", 27, ProcessPortalWidgetGroup.group3),
    testPortalWidget28("testPortalWidget28", "测试小部件28", 28, ProcessPortalWidgetGroup.group3),
    testPortalWidget29("testPortalWidget29", "测试小部件29", 29, ProcessPortalWidgetGroup.group3),
    ;

    private final String value;
    private final String text;
    private final Integer sort;
    private final IPortalWidgetGroup group;
//    ProcessPortalWidget(String value, String text) {
//        this.value = value;
//        this.text = text;
//    }
    ProcessPortalWidget(String value, String text, Integer sort) {
        this.value = value;
        this.text = text;
        this.sort = sort;
        this.group = null;
    }
    ProcessPortalWidget(String value, String text, Integer sort, IPortalWidgetGroup group) {
        this.value = value;
        this.text = text;
        this.sort = sort;
        this.group = group;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public Integer getSort() {
        return this.sort;
    }

    @Override
    public IPortalWidgetGroup getGroup() {
        return this.group;
    }
}
