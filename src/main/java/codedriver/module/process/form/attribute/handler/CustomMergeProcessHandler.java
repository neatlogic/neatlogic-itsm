/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.form.attribute.handler;

import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.form.attribute.core.FormAttributeHandlerFactory;
import codedriver.framework.form.attribute.core.FormHandlerBase;
import codedriver.framework.form.attribute.core.IFormAttributeHandler;
import codedriver.framework.form.constvalue.FormConditionModel;
import codedriver.framework.form.dto.AttributeDataVo;
import codedriver.framework.form.dto.FormAttributeVo;
import codedriver.framework.form.dto.FormVersionVo;
import codedriver.framework.form.exception.AttributeValidException;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.restful.core.MyApiComponent;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentFactory;
import codedriver.framework.restful.dto.ApiVo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author linbq
 * @since 2021/8/18 17:03
 **/
@Component
public class CustomMergeProcessHandler extends FormHandlerBase {

    private final static Logger logger = LoggerFactory.getLogger(CustomMergeProcessHandler.class);
    private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final static JSONArray theadList = new JSONArray();
    static {
        JSONObject selection = new JSONObject();
        selection.put("title", "");
        selection.put("key", "selection");
        theadList.add(selection);
        JSONObject serialNumber = new JSONObject();
        serialNumber.put("title", "工单号");
        serialNumber.put("key", "serialNumber");
        theadList.add(serialNumber);
        JSONObject title = new JSONObject();
        title.put("title", "标题");
        title.put("key", "title");
        theadList.add(title);
        JSONObject ownerName = new JSONObject();
        ownerName.put("title", "上报人");
        ownerName.put("key", "ownerName");
        theadList.add(ownerName);
        JSONObject startTime = new JSONObject();
        startTime.put("title", "上报时间");
        startTime.put("key", "startTime");
        theadList.add(startTime);
    }

    @Resource
    private UserMapper userMapper;

    @Override
    public String getHandler() {
        return "custommergeprocess";
    }

    @Override
    public String getHandlerName() {
        return "批量合并上报流程";
    }

    @Override
    public String getHandlerType(FormConditionModel model) {
        return null;
    }

    @Override
    public String getIcon() {
        return "tsfont-certificate";
    }

    @Override
    public ParamType getParamType() {
        return null;
    }

    @Override
    public String getDataType() {
        return null;
    }

    @Override
    public boolean isAudit() {
        return true;
    }

    @Override
    public boolean isConditionable() {
        return false;
    }

    @Override
    public boolean isShowable() {
        return true;
    }

    @Override
    public boolean isValueable() {
        return false;
    }

    @Override
    public boolean isFilterable() {
        return false;
    }

    @Override
    public boolean isExtendable() {
        return false;
    }

    @Override
    public boolean isForTemplate() {
        return false;
    }

    @Override
    public String getModule() {
        return "framework";
    }

    @Override
    public JSONObject valid(AttributeDataVo attributeDataVo, JSONObject configObj) throws AttributeValidException {
        return null;
    }

    @Override
    public Object valueConversionText(AttributeDataVo attributeDataVo, JSONObject configObj) {
        return "已更新";
    }

    @Override
    public Object dataTransformationForEmail(AttributeDataVo attributeDataVo, JSONObject configObj) {
        JSONObject tableObj = new JSONObject();
        JSONObject dataObj = (JSONObject) attributeDataVo.getDataObj();
        List<Long> selectList = new ArrayList<>();
        List<Long> unSelectList = new ArrayList<>();
        if (MapUtils.isNotEmpty(dataObj)) {
            JSONArray selectArray = dataObj.getJSONArray("selectList");
            if (CollectionUtils.isNotEmpty(selectArray)) {
                selectList = selectArray.toJavaList(Long.class);
            }
            JSONArray unSelectArray = dataObj.getJSONArray("unSelectList");
            if (CollectionUtils.isNotEmpty(unSelectArray)) {
                unSelectList = unSelectArray.toJavaList(Long.class);
            }
        }
        List<Long> processTaskIdList = new ArrayList<>();
        processTaskIdList.addAll(selectList);
        processTaskIdList.addAll(unSelectList);
        if (CollectionUtils.isEmpty(processTaskIdList)) {
            return tableObj;
        }
        ApiVo api = PrivateApiComponentFactory.getApiByToken("suzhoubank/processtask/list/form");
        if (api != null) {
            MyApiComponent restComponent = (MyApiComponent) PrivateApiComponentFactory.getInstance(api.getHandler());
            if (restComponent != null) {
                JSONObject paramObj = new JSONObject();
                paramObj.put("processTaskIdList", processTaskIdList);
                try {
                    List<String> ownerList = new ArrayList<>();
                    JSONArray tbodyList = new JSONArray();
                    List<ProcessTaskVo> processTaskList = (List<ProcessTaskVo>) restComponent.myDoService(paramObj);
                    for (ProcessTaskVo processTaskVo : processTaskList) {
                        JSONObject tbody = new JSONObject();
                        Long id = processTaskVo.getId();
                        if (selectList.contains(id)) {
                            tbody.put("selection", 1);
                        } else {
                            tbody.put("selection", 0);
                        }
                        tbody.put("serialNumber", processTaskVo.getSerialNumber());
                        tbody.put("title", processTaskVo.getTitle());
                        String owner = processTaskVo.getOwner();
                        if (StringUtils.isNotBlank(owner)) {
                            tbody.put("owner", owner);
                            ownerList.add(owner);
                        }
                        Date startTime = processTaskVo.getStartTime();
                        tbody.put("startTime", SDF.format(startTime));
                        List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskVo.getProcessTaskFormAttributeDataList();
                        if (CollectionUtils.isNotEmpty(processTaskFormAttributeDataList)) {
                            Map<String, ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataMap = processTaskFormAttributeDataList.stream().collect(Collectors.toMap(e -> e.getAttributeUuid(), e -> e));
                            if (MapUtils.isNotEmpty(processTaskVo.getFormConfig())) {
                                FormVersionVo formVersionVo = new FormVersionVo();
                                formVersionVo.setFormConfig(processTaskVo.getFormConfig().toJSONString());
                                List<FormAttributeVo> formAttributeList = formVersionVo.getFormAttributeList();
                                for (FormAttributeVo formAttribute : formAttributeList) {
                                    ProcessTaskFormAttributeDataVo attributeData = processTaskFormAttributeDataMap.get(formAttribute.getUuid());
                                    if (attributeData != null) {
                                        attributeData.setLabel(formAttribute.getLabel());
                                        if (attributeData.getData() != null) {
                                            IFormAttributeHandler handler = FormAttributeHandlerFactory.getHandler(formAttribute.getHandler());
                                            if (handler != null) {
                                                Object value = handler.dataTransformationForEmail(attributeData, JSONObject.parseObject(formAttribute.getConfig()));
                                                attributeData.setDataObj(value);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        tbody.put("processTaskFormAttributeDataList", processTaskFormAttributeDataList);
                        tbodyList.add(tbody);
                    }
                    if (CollectionUtils.isNotEmpty(ownerList)) {
                        List<UserVo> userList = userMapper.getUserByUserUuidList(ownerList);
                        Map<String, String> userNameMap = userList.stream().collect(Collectors.toMap(e -> e.getUuid(), e -> e.getUserName()));
                        for (int i = 0; i < tbodyList.size(); i++) {
                            JSONObject tbody = tbodyList.getJSONObject(i);
                            String owner = tbody.getString("owner");
                            String ownerName = userNameMap.get(owner);
                            tbody.put("ownerName", ownerName);
                        }
                    }
                    tableObj.put("tbodyList", tbodyList);
                    tableObj.put("theadList", theadList);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return tableObj;
    }

    @Override
    public Object textConversionValue(List<String> values, JSONObject config) {
        return null;
    }
}
