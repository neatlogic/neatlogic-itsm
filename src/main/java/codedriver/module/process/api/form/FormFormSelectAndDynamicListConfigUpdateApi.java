/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.form;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.form.dto.FormAttributeVo;
import codedriver.framework.form.dto.FormVersionVo;
import codedriver.framework.matrix.dto.MatrixColumnVo;
import codedriver.framework.process.auth.PROCESS_MODIFY;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dto.ProcessTaskFormVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author linbq
 * @since 2022/3/8 16:29
 **/
@Service
@AuthAction(action = PROCESS_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class FormFormSelectAndDynamicListConfigUpdateApi extends PrivateApiComponentBase {

    @Resource
    private FormMapper formMapper;

    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    /**
     * 接口唯一标识，也是访问URI
     *
     * @return 接口唯一地址
     */
    @Override
    public String getToken() {
        return "form/formselectanddynamiclist/config/update";
    }
    /**
     * 接口中文名
     *
     * @return 中文名
     */
    @Override
    public String getName() {
        return "表单下拉框和表格选择组件config更新";
    }

    /**
     * 额外配置
     *
     * @return 配置json
     */
    @Override
    public String getConfig() {
        return null;
    }

    @Input({})
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "更新情况")
    })
    @Description(desc="表单下拉框和表格选择组件config更新")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        List<String> resultList = new ArrayList<>();
        int a = 0;
        int b = 0;
        int c = 0;
        int d = 0;
        int e = 0;
        //保存已经更新过的config生成的hash
        Set<String> newFormConfigHashSet = new HashSet<>();
        //第一步
        //在form_version表中找出有表格输入组件的表单版本
        List<FormVersionVo> formVersionList =  formMapper.getFormVersionList();
        for (FormVersionVo formVersionVo : formVersionList) {
            String oldFormConfig = formVersionVo.getFormConfig();
            String oldFormConfigHash = DigestUtils.md5DigestAsHex(oldFormConfig.getBytes());
            List<String> attributeUuidList = new ArrayList<>();
            String newFormConfig = updateConfig(oldFormConfig, formVersionVo.getUuid(), attributeUuidList);
            //如果更新前后相同，则说明已经转换过了(重复执行本接口)
            if (Objects.equals(newFormConfig, oldFormConfig)) {
                continue;
            }
//            System.out.println("a=" + formVersionVo.getUuid());
            String newFormConfigHash = DigestUtils.md5DigestAsHex(newFormConfig.getBytes());
            newFormConfigHashSet.add(newFormConfigHash);
            FormVersionVo newFormVersionVo = new FormVersionVo();
            newFormVersionVo.setUuid(formVersionVo.getUuid());
            newFormVersionVo.setFormConfig(newFormConfig);
            formMapper.updateFormVersionConfigByUuid(newFormVersionVo);
            a++;
            //如果当前表单版本是激活版本，则需要更新form_attribute表中对应表格输入组件的config
            if (Objects.equals(formVersionVo.getIsActive(), 1) && CollectionUtils.isNotEmpty(attributeUuidList)) {
                newFormVersionVo.setVersion(formVersionVo.getVersion());
                newFormVersionVo.setIsActive(formVersionVo.getIsActive());
                newFormVersionVo.setFormUuid(formVersionVo.getFormUuid());
                List<FormAttributeVo> formAttributeList = newFormVersionVo.getFormAttributeList();
                for (FormAttributeVo formAttributeVo : formAttributeList) {
                    if (attributeUuidList.contains(formAttributeVo.getUuid())) {
                        formMapper.updateFormAttributeConfig(formAttributeVo);
                        b++;
                    }
//                    if ("formdynamiclist".equals(formAttributeVo.getHandler())) {
//                        formMapper.updateFormAttributeConfig(formAttributeVo);
//                        b++;
//                    } else if ("formselect".equals(formAttributeVo.getHandler())) {
//                        formMapper.updateFormAttributeConfig(formAttributeVo);
//                        b++;
//                    }
                }
            }
            //判断当前表单版本是否存在被工单绑定的副本，如果存在，则
            // 1.向processtask_form_content表插入更新后的newFormConfigHash和newFormConfig
            // 2.将processtask_form表中form_content_hash字段的值是oldFormConfigHash的更新成newFormConfigHash
            // 3.删除processtask_form_content表中hash字段值是oldFormConfigHash的数据
            if (selectContentByHashMapper.getProcessTaskFromContentCountByHash(oldFormConfigHash) > 0) {
                ProcessTaskFormVo processTaskFormVo = new ProcessTaskFormVo();
                processTaskFormVo.setFormContent(newFormConfig);
                processTaskFormVo.setFormContentHash(newFormConfigHash);
                processTaskMapper.insertIgnoreProcessTaskFormContent(processTaskFormVo);
                c++;
                int updateCount = processTaskMapper.updateProcessTaskFormFormContentHashByFormContentHash(oldFormConfigHash, newFormConfigHash);
                d += updateCount;
                processTaskMapper.deleteProcessTaskFormContentByHash(oldFormConfigHash);
                e++;
            }
        }
        resultList.add("form_version表更新" + a + "条数据。");
        resultList.add("form_attribute表更新" + b + "条数据。");
//        System.out.println("form_version表更新" + a + "条数据。");
//        System.out.println("form_attribute表更新" + b + "条数据。");
        //第二步
        //在processtask_form_content中找出有表格输入组件的表单配置副本
        List<ProcessTaskFormVo> processTaskFormList = processTaskMapper.getProcessTaskFormContentList();
        for (ProcessTaskFormVo processTaskFormVo : processTaskFormList) {
            String oldFormConfigHash = processTaskFormVo.getFormContentHash();
            //第一步已经更新过了
            if (newFormConfigHashSet.contains(oldFormConfigHash)) {
                continue;
            }
            String oldFormConfig = processTaskFormVo.getFormContent();
            List<String> attributeUuidList = new ArrayList<>();
            String newFormConfig = updateConfig(oldFormConfig, processTaskFormVo.getFormName(), attributeUuidList);
            //如果更新前后相同，则说明已经转换过了(重复执行本接口)
            if (Objects.equals(newFormConfig, oldFormConfig)) {
                continue;
            }
            String newFormConfigHash = DigestUtils.md5DigestAsHex(newFormConfig.getBytes());
            ProcessTaskFormVo newProcessTaskFormVo = new ProcessTaskFormVo();
            newProcessTaskFormVo.setFormContent(newFormConfig);
            newProcessTaskFormVo.setFormContentHash(newFormConfigHash);
            processTaskMapper.insertIgnoreProcessTaskFormContent(newProcessTaskFormVo);
            c++;
            int updateCount = processTaskMapper.updateProcessTaskFormFormContentHashByFormContentHash(oldFormConfigHash, newFormConfigHash);
            d += updateCount;
            processTaskMapper.deleteProcessTaskFormContentByHash(oldFormConfigHash);
            e++;
        }
        resultList.add("processtask_form_content表插入" + c + "条数据。");
        resultList.add("processtask_form_content表删除" + e + "条数据。");
        resultList.add("processtask_form表更新" + d + "条数据。");
//        System.out.println("processtask_form_content表插入" + c + "条数据。");
//        System.out.println("processtask_form_content表删除" + e + "条数据。");
//        System.out.println("processtask_form表更新" + d + "条数据。");

        return resultList;
    }


    /**
     * 将旧配置信息转换成新的配置信息，主要是给表格输入组件的每一列加上attributeUuid，如果列的类型是table，把table里的每一列加上attributeUuid。
     * @param configStr 旧的配置信息
     * @return
     */
    private String updateConfig(String configStr, String formVersionUuid, List<String> attributeUuidList) {
        if (StringUtils.isBlank(configStr)) {
            return configStr;
        }
        JSONObject config = JSONObject.parseObject(configStr);
        if (MapUtils.isEmpty(config)) {
            return configStr;
        }
        boolean flag = false;
        Map<String, JSONObject> attributeUuidMap = new HashMap<>();
        JSONArray controllerList = config.getJSONArray("controllerList");
        if (CollectionUtils.isNotEmpty(controllerList)) {
            //遍历表单的所有组件
            for (int i = 0; i < controllerList.size(); i++) {
                JSONObject controllerObj = controllerList.getJSONObject(i);
                if (MapUtils.isEmpty(controllerObj)) {
                    continue;
                }
                String attributeUuid = controllerObj.getString("uuid");
                if(updateControllerConfig(controllerObj)){
                    attributeUuidList.add(attributeUuid);
                    flag = true;
//                    System.out.println("attribute1=" + controllerObj.getString("label"));
                    attributeUuidMap.put(attributeUuid, JSONObject.parseObject(controllerObj.toJSONString()));
                }
            }
        }
        if (flag) {
//            System.out.println("b=" + formVersionUuid);
        }
        flag = false;
        //sheetsConfig.tableList[x][x].component中表格输入组件
        JSONObject sheetsConfig = config.getJSONObject("sheetsConfig");
        if (MapUtils.isEmpty(sheetsConfig)) {
            return config.toJSONString();
        }
        JSONArray tableList = sheetsConfig.getJSONArray("tableList");
        if (CollectionUtils.isEmpty(tableList)) {
            return config.toJSONString();
        }
        for (int i = 0; i < tableList.size(); i++) {
            JSONArray row = tableList.getJSONArray(i);
            if (CollectionUtils.isEmpty(row)) {
                continue;
            }
            for (int j = 0; j < row.size(); j++) {
                Object cell = row.get(j);
                if (cell instanceof JSONObject) {
                    JSONObject cellObj = (JSONObject) cell;
                    if (MapUtils.isEmpty(cellObj)) {
                        continue;
                    }
                    JSONObject component = cellObj.getJSONObject("component");
                    if (MapUtils.isEmpty(component)) {
                        continue;
                    }
                    String attributeUuid = component.getString("uuid");
                    JSONObject newComponent = attributeUuidMap.get(attributeUuid);
                    if (newComponent != null) {
                        cellObj.put("component", newComponent);
                    }
//                    if(updateControllerConfig(component)) {
//                        flag = true;
//                        System.out.println("attribute2=" + component.getString("label"));
//                    }
                }
            }
        }
        if (flag) {
//            System.out.println("c=" + formVersionUuid);
        }
        return config.toJSONString();
    }

    private boolean updateControllerConfig(JSONObject controllerObj) {
        //只更新表格输入组件
        String handler = controllerObj.getString("handler");
        if (!"formdynamiclist".equals(handler) && !"formselect".equals(handler) && !"formcheckbox".equals(handler) && !"formradio".equals(handler)) {
            return false;
        }
        JSONObject controllerConfig = controllerObj.getJSONObject("config");
        if (MapUtils.isEmpty(controllerConfig)) {
            return false;
        }
        JSONArray filterList = controllerConfig.getJSONArray("filterList");
        if (CollectionUtils.isEmpty(filterList)) {
            if (filterList != null) {
                controllerConfig.remove("filterList");
                return true;
            }
            return false;
        }
        String dataSource = controllerConfig.getString("dataSource");
        if ("matrix".equals(dataSource)) {
            String matrixType = controllerConfig.getString("matrixType");
            if ("cmdbci".equals(matrixType)) {
                return false;
            }
        } else if ("integration".equals(dataSource)) {

        } else {
            if (filterList != null) {
                controllerConfig.remove("filterList");
                return true;
            }
            return false;
        }
        List<MatrixColumnVo> sourceColumnList = new ArrayList<>();
        for (int j = 0; j < filterList.size(); j++) {
            JSONObject filterObj = filterList.getJSONObject(j);
            if (MapUtils.isEmpty(filterObj)) {
                continue;
            }
            String column = filterObj.getString("uuid");
            if (StringUtils.isBlank(column)) {
                continue;
            }
            JSONArray valueArray = filterObj.getJSONArray("valueList");
            if (CollectionUtils.isEmpty(valueArray)) {
                continue;
            }
            List<String> valueList = valueArray.toJavaList(String.class);
            List<String> defaultValue = new ArrayList<>();
            for (String value : valueList) {
                defaultValue.add(value + "&=&" + value);
            }
            MatrixColumnVo sourceColumn = new MatrixColumnVo();
            sourceColumn.setColumn(column);
            sourceColumn.setValueList(valueList);
            sourceColumn.setIsFilterList(true);
            sourceColumn.setExpression("include");
            sourceColumn.setDefaultValue(defaultValue);
            sourceColumnList.add(sourceColumn);
        }
        controllerConfig.remove("filterList");
        controllerConfig.put("sourceColumnList", sourceColumnList);
        return true;
    }
}
