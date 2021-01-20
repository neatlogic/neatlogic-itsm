package codedriver.module.process.service;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.dto.FormAttributeVo;
import codedriver.framework.process.workcenter.dto.WorkcenterTheadVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.module.process.workcenter.core.SqlBuilder;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Title: NewWorkcenterServiceImpl
 * @Package: codedriver.module.process.service
 * @Description: TODO
 * @Author: 89770
 * @Date: 2021/1/19 20:09
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Service
public class NewWorkcenterServiceImpl implements NewWorkcenterService{

    Logger logger = LoggerFactory.getLogger(WorkcenterServiceImpl.class);

    @Autowired
    WorkcenterMapper workcenterMapper;

    @Autowired
    FormMapper formMapper;

    @Override
    public JSONObject doSearch(WorkcenterVo workcenterVo) {

        JSONArray sortColumnList = new JSONArray();
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        //thead
        List<WorkcenterTheadVo> theadList = getWorkcenterTheadList(workcenterVo, columnComponentMap, sortColumnList);
        theadList = theadList.stream().sorted(Comparator.comparing(WorkcenterTheadVo::getSort)).collect(Collectors.toList());

        SqlBuilder sb = new SqlBuilder(workcenterVo, SqlBuilder.FieldTypeEnum.FIELD);
        System.out.println(sb.build());
        return null;
    }

    @Override
    public Integer doSearchCount(WorkcenterVo workcenterVo) {
        return null;
    }

    /**
     * @Description: 获取用户工单中心table column theadList
     * @Author: 89770
     * @Date: 2021/1/19 20:38
     * @Params: [workcenterVo, columnComponentMap, sortColumnList]
     * @Returns: java.util.List<codedriver.framework.process.workcenter.dto.WorkcenterTheadVo>
     **/
    public List<WorkcenterTheadVo> getWorkcenterTheadList(WorkcenterVo workcenterVo, Map<String, IProcessTaskColumn> columnComponentMap, JSONArray sortColumnList) {
        List<WorkcenterTheadVo> theadList = workcenterMapper.getWorkcenterThead(new WorkcenterTheadVo(workcenterVo.getUuid(), UserContext.get().getUserUuid()));
        // 矫正theadList 或存在表单属性或固定字段增删
        // 多删
        ListIterator<WorkcenterTheadVo> it = theadList.listIterator();
        while (it.hasNext()) {
            WorkcenterTheadVo thead = it.next();
            if (thead.getType().equals(ProcessFieldType.COMMON.getValue())) {
                if (!columnComponentMap.containsKey(thead.getName())) {
                    it.remove();
                } else {
                    thead.setDisabled(columnComponentMap.get(thead.getName()).getDisabled() ? 1 : 0);
                    thead.setDisplayName(columnComponentMap.get(thead.getName()).getDisplayName());
                    thead.setClassName(columnComponentMap.get(thead.getName()).getClassName());
                    thead.setIsExport(columnComponentMap.get(thead.getName()).getIsExport() ? 1 : 0);
                    thead.setIsShow(columnComponentMap.get(thead.getName()).getIsShow() ? 1 : 0);
                }
            } else {
                List<String> channelUuidList = workcenterVo.getChannelUuidList();
                if (CollectionUtils.isNotEmpty(channelUuidList)) {
                    List<FormAttributeVo> formAttrList =
                            formMapper.getFormAttributeListByChannelUuidList(channelUuidList);
                    List<FormAttributeVo> theadFormList = formAttrList.stream()
                            .filter(attr -> attr.getUuid().equals(thead.getName())).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(theadFormList)) {
                        it.remove();
                    } else {
                        thead.setDisplayName(theadFormList.get(0).getLabel());
                    }
                }
            }
        }
        // 少补
        for (Map.Entry<String, IProcessTaskColumn> entry : columnComponentMap.entrySet()) {
            IProcessTaskColumn column = entry.getValue();
            if (column.getIsShow() && CollectionUtils.isEmpty(theadList.stream()
                    .filter(data -> column.getName().endsWith(data.getName())).collect(Collectors.toList()))) {
                theadList.add(new WorkcenterTheadVo(column));
            }
            // 如果需要排序
            if (sortColumnList != null && column.getIsSort()) {
                sortColumnList.add(column.getName());
            }
        }
        return theadList;
    }
}
