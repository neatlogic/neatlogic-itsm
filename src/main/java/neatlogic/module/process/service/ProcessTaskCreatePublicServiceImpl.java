package neatlogic.module.process.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.thread.NeatLogicThread;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.asynchronization.threadpool.CachedThreadPool;
import neatlogic.framework.common.constvalue.SystemUser;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dao.mapper.region.RegionMapper;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.dto.JwtVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.dto.region.RegionVo;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.form.attribute.core.FormAttributeHandlerFactory;
import neatlogic.framework.form.attribute.core.FormHandlerBase;
import neatlogic.framework.form.constvalue.FormHandler;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.form.exception.FormAttributeHandlerNotFoundException;
import neatlogic.framework.form.exception.FormAttributeNotFoundException;
import neatlogic.framework.process.constvalue.ProcessFlowDirection;
import neatlogic.framework.process.crossover.IProcessTaskCreatePublicCrossoverService;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.process.dto.PriorityVo;
import neatlogic.framework.process.dto.ProcessFormVo;
import neatlogic.framework.process.exception.channel.ChannelNotFoundException;
import neatlogic.framework.process.exception.priority.PriorityNotFoundException;
import neatlogic.framework.process.exception.process.ProcessNotFoundException;
import neatlogic.framework.process.exception.processtask.ProcessTaskNextStepIllegalException;
import neatlogic.framework.process.exception.processtask.ProcessTaskNextStepOverOneException;
import neatlogic.framework.service.AuthenticationInfoService;
import neatlogic.framework.service.RegionService;
import neatlogic.framework.util.SnowflakeUtil;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.module.process.dao.mapper.catalog.PriorityMapper;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class ProcessTaskCreatePublicServiceImpl implements ProcessTaskCreatePublicService, IProcessTaskCreatePublicCrossoverService {

    private Logger logger = LoggerFactory.getLogger(ProcessTaskCreatePublicServiceImpl.class);
    @Resource
    private ChannelMapper channelMapper;

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private FormMapper formMapper;

    @Resource
    private PriorityMapper priorityMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private AuthenticationInfoService authenticationInfoService;

    @Resource
    private FileMapper fileMapper;

    @Resource
    RegionMapper regionMapper;

    @Resource
    RegionService regionService;

    /**
     * 创建工单
     *
     * @param paramObj
     * @return
     * @throws Exception
     */
    @Override
    public JSONObject createProcessTask(JSONObject paramObj) throws Exception {
        JSONObject result = new JSONObject();
        //上报人，支持上报人uuid和上报人id入参
        String owner = paramObj.getString("owner");
        UserVo userVo = userMapper.getUserByUuid(owner);
        if (userVo == null) {
            userVo = userMapper.getUserByUserId(owner);
            if (userVo == null) {
                throw new UserNotFoundException(owner);
            }
            paramObj.put("owner", userVo.getUuid());
        }
        //地域
        String region = paramObj.getString("region");
        Long regionId = null;
        if(StringUtils.isNotBlank(region)){
            RegionVo regionVo = regionMapper.getRegionByUpwardNamePath(region);
            if(regionVo == null){
                regionVo = regionMapper.getRegionById(region);
                if(regionVo != null) {
                    regionId = regionVo.getId();
                }
            }else{
                regionId = regionVo.getId();
            }
        }

        if(regionId == null) {
            List<Long> regionIdList = regionService.getRegionIdListByUserUuid(userVo.getUuid());
            if(CollectionUtils.isNotEmpty(regionIdList)){
                regionId = regionIdList.get(0);
            }
        }

        paramObj.put("regionId", regionId);
        //处理channel，支持channelUuid和channelName入参
        String channel = paramObj.getString("channel");
        ChannelVo channelVo = channelMapper.getChannelByUuid(channel);
        if (channelVo == null) {
            channelVo = channelMapper.getChannelByName(channel);
            if (channelVo == null) {
                throw new ChannelNotFoundException(channel);
            }
        }
        paramObj.put("channelUuid", channelVo.getUuid());
        //优先级
        String priority = paramObj.getString("priority");
        if (StringUtils.isNotBlank(priority)) {
            PriorityVo priorityVo = priorityMapper.getPriorityByUuid(priority);
            if (priorityVo == null) {
                priorityVo = priorityMapper.getPriorityByName(priority);
                if (priorityVo == null) {
                    throw new PriorityNotFoundException(priority);
                }
            }
            paramObj.put("priorityUuid", priorityVo.getUuid());
        }
        // 附件传递文件路径
        JSONArray filePathList = paramObj.getJSONArray("filePathList");
        if( filePathList != null && filePathList.size() > 0  ){
            String filePathPrefix = paramObj.getString("filePathPrefix");
            JSONArray fileIdList  = new JSONArray();
//            MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
            for (Object filePath: filePathList ) {
                FileVo fileVo = new FileVo();
                fileVo.setName(filePath.toString().substring(filePath.toString().lastIndexOf("/")+1 , filePath.toString().length()));
                fileVo.setSize(100*1024*1024L);
                fileVo.setUserUuid(userVo.getUuid());
                fileVo.setType("itsm"); //itsm
//                fileVo.setContentType(mimeTypesMap.getContentType(filePath.toString()));
                fileVo.setPath(filePathPrefix + filePath);
                fileMapper.insertFile(fileVo);
                fileIdList.add(fileVo.getId());
            }
            paramObj.put("fileIdList" , fileIdList);
        }
        //流程
        String processUuid = channelMapper.getProcessUuidByChannelUuid(channelVo.getUuid());
        if (processMapper.checkProcessIsExists(processUuid) == 0) {
            throw new ProcessNotFoundException(processUuid);
        }
        //如果表单属性数据列表，使用的唯一标识是label时，需要转换成attributeUuid
        JSONArray formAttributeDataList = paramObj.getJSONArray("formAttributeDataList");
        if (CollectionUtils.isNotEmpty(formAttributeDataList)) {
            int count = 0;
            for (int i = 0; i < formAttributeDataList.size(); i++) {
                JSONObject formAttributeData = formAttributeDataList.getJSONObject(i);
                if (MapUtils.isNotEmpty(formAttributeData)) {
                    String attributeUuid = formAttributeData.getString("attributeUuid");
                    String label = formAttributeData.getString("label");
                    String key = formAttributeData.getString("key");
                    if (StringUtils.isBlank(attributeUuid) && (StringUtils.isNotBlank(label) || StringUtils.isNotBlank(key))) {
                        count++;
                    }
                }
            }
            if (count > 0) {
                ProcessFormVo processFormVo = processMapper.getProcessFormByProcessUuid(processUuid);
                if (processFormVo != null) {
                    FormVersionVo actionFormVersionVo = formMapper.getActionFormVersionByFormUuid(processFormVo.getFormUuid());
                    String mainSceneUuid = actionFormVersionVo.getFormConfig().getString("uuid");
                    actionFormVersionVo.setSceneUuid(mainSceneUuid);
                    List<FormAttributeVo> formAttributeVoList = actionFormVersionVo.getFormAttributeList();
                    if (CollectionUtils.isNotEmpty(formAttributeVoList)) {
                        List<String> dataTypeNotArrayHandlerList = new ArrayList<>();
                        dataTypeNotArrayHandlerList.add(FormHandler.FORMDATE.getHandler());
                        dataTypeNotArrayHandlerList.add(FormHandler.FORMTIME.getHandler());
                        dataTypeNotArrayHandlerList.add(FormHandler.FORMRATE.getHandler());
                        dataTypeNotArrayHandlerList.add(FormHandler.FORMCKEDITOR.getHandler());
                        dataTypeNotArrayHandlerList.add(FormHandler.FORMTEXTAREA.getHandler());
                        dataTypeNotArrayHandlerList.add(FormHandler.FORMTEXT.getHandler());
                        dataTypeNotArrayHandlerList.add(FormHandler.FORMNUMBER.getHandler());
                        dataTypeNotArrayHandlerList.add(FormHandler.FORMPASSWORD.getHandler());
                        dataTypeNotArrayHandlerList.add(FormHandler.FORMTREESELECT.getHandler());
                        dataTypeNotArrayHandlerList.add(neatlogic.framework.cmdb.enums.FormHandler.FORMPROTOCOL.getHandler());
                        Map<String, FormAttributeVo> labelAttributeMap = new HashMap<>();
                        Map<String, FormAttributeVo> keyAttributeMap = new HashMap<>();
                        for (FormAttributeVo formAttributeVo : formAttributeVoList) {
                            labelAttributeMap.put(formAttributeVo.getLabel(), formAttributeVo);
                            keyAttributeMap.put(formAttributeVo.getKey(), formAttributeVo);
                        }
                        for (int i = 0; i < formAttributeDataList.size(); i++) {
                            JSONObject formAttributeData = formAttributeDataList.getJSONObject(i);
                            if (MapUtils.isNotEmpty(formAttributeData)) {
                                String attributeUuid = formAttributeData.getString("attributeUuid");
                                String label = formAttributeData.getString("label");
                                String key = formAttributeData.getString("key");
                                if (StringUtils.isBlank(attributeUuid) && (StringUtils.isNotBlank(label) || StringUtils.isNotBlank(key))) {
                                    FormAttributeVo formAttributeVo = null;
                                    if (StringUtils.isNotBlank(key)) {
                                        formAttributeVo = keyAttributeMap.get(key);
                                        if (formAttributeVo == null) {
                                            throw new FormAttributeNotFoundException(key);
                                        }
                                    } else if (StringUtils.isNotBlank(label)) {
                                        formAttributeVo = labelAttributeMap.get(label);
                                        if (formAttributeVo == null) {
                                            throw new FormAttributeNotFoundException(label);
                                        }
                                    }
                                    FormHandlerBase formAttributeHandler = (FormHandlerBase) FormAttributeHandlerFactory.getHandler(formAttributeVo.getHandler());
                                    if (formAttributeHandler == null) {
                                        throw new FormAttributeHandlerNotFoundException(formAttributeVo.getHandler());
                                    }
                                    JSONObject config = formAttributeVo.getConfig();
                                    formAttributeData.put("attributeUuid", formAttributeVo.getUuid());
                                    formAttributeData.put("handler", formAttributeVo.getHandler());
                                    Object dataObj = formAttributeData.get("dataList");
                                    Object dataList = formAttributeHandler.getStandardValueBySimpleValue(dataObj, config);
                                    if (dataTypeNotArrayHandlerList.contains(formAttributeVo.getHandler())) {
                                        if (dataList instanceof List) {
                                            formAttributeData.put("dataList", ((List) dataList).get(0));
                                        }
                                    } else {
                                        formAttributeData.put("dataList", dataList);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //代报人，支持代报人uuid和代报人id入参
        String reporter = paramObj.getString("reporter");
        if (StringUtils.isNotBlank(reporter)) {
            UserVo reporterUserVo = userMapper.getUserByUuid(reporter);
            if (reporterUserVo == null) {
                reporterUserVo = userMapper.getUserByUserId(reporter);
                if (reporterUserVo == null) {
                    throw new UserNotFoundException(reporter);
                }
                paramObj.put("reporter", reporterUserVo.getUuid());
                reporterUserVo = userMapper.getUserByUuid(reporterUserVo.getUuid());
            }
            AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(userVo.getUuid());
            JwtVo jwtVo = UserContext.get().getJwtVo();
            UserContext.get().init(reporterUserVo, authenticationInfoVo, SystemUser.SYSTEM.getTimezone()).setJwtVo(jwtVo);
        } else {
            AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(userVo.getUuid());
            JwtVo jwtVo = UserContext.get().getJwtVo();
            UserContext.get().init(userVo, authenticationInfoVo, SystemUser.SYSTEM.getTimezone()).setJwtVo(jwtVo);
        }

        Long processTaskId = null;
        Integer isAsync = paramObj.getInteger("isAsync");
        if (Objects.equals(isAsync, 1)) {
            Long newProcessTaskId = SnowflakeUtil.uniqueLong();
            NeatLogicThread neatLogicThread = new NeatLogicThread("PUBLIC_CREATE_PROCESSTASK_" + newProcessTaskId, true) {
                @Override
                protected void execute() {
                    try {
                        //暂存
                        //TODO isNeedValid 参数是否需要？？？
                        paramObj.put("isNeedValid", 1);
                        JSONObject saveResultObj = processTaskService.saveProcessTaskDraft(paramObj, newProcessTaskId);

                        //查询可执行下一 步骤
                        Long processTaskId = saveResultObj.getLong("processTaskId");
                        List<Long> nextStepIdList = processTaskMapper.getToProcessTaskStepIdListByFromIdAndType(saveResultObj.getLong("processTaskStepId"), ProcessFlowDirection.FORWARD.getValue());
                        if (nextStepIdList.isEmpty()) {
                            throw new ProcessTaskNextStepIllegalException(processTaskId);
                        }
                        if (nextStepIdList.size() != 1) {
                            throw new ProcessTaskNextStepOverOneException(processTaskId);
                        }
                        saveResultObj.put("nextStepId", nextStepIdList.get(0));

                        //流转
                        processTaskService.startProcessProcessTask(saveResultObj);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            };
            CachedThreadPool.execute(neatLogicThread);
            processTaskId = newProcessTaskId;
        } else {
            //暂存
            //TODO isNeedValid 参数是否需要？？？
            paramObj.put("isNeedValid", 1);
            Long newProcessTaskId = paramObj.getLong("newProcessTaskId");
            JSONObject saveResultObj = processTaskService.saveProcessTaskDraft(paramObj, newProcessTaskId);

            //查询可执行下一 步骤
            processTaskId = saveResultObj.getLong("processTaskId");
            List<Long> nextStepIdList = processTaskMapper.getToProcessTaskStepIdListByFromIdAndType(saveResultObj.getLong("processTaskStepId"), ProcessFlowDirection.FORWARD.getValue());
            if (nextStepIdList.isEmpty()) {
                throw new ProcessTaskNextStepIllegalException(processTaskId);
            }
            if (nextStepIdList.size() != 1) {
                throw new ProcessTaskNextStepOverOneException(processTaskId);
            }
            saveResultObj.put("nextStepId", nextStepIdList.get(0));

            //流转
            processTaskService.startProcessProcessTask(saveResultObj);
        }

        result.put("processTaskId", processTaskId);
        return result;
    }
}
