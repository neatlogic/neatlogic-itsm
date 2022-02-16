package codedriver.module.process.api.workcenter;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.batch.BatchRunner;
import codedriver.framework.common.config.Config;
import codedriver.framework.common.util.CommonUtil;
import codedriver.framework.dto.AuthenticationInfoVo;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.auth.WORKCENTER_MODIFY;
import codedriver.framework.process.auth.WORKCENTER_NEW_TYPE;
import codedriver.framework.process.constvalue.ProcessWorkcenterInitType;
import codedriver.framework.process.constvalue.ProcessWorkcenterType;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.workcenter.dto.WorkcenterUserProfileVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.service.AuthenticationInfoService;
import codedriver.module.process.service.NewWorkcenterService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class WorkcenterListApi extends PrivateApiComponentBase {
    private static final Logger logger = LoggerFactory.getLogger(WorkcenterListApi.class);
    @Resource
    WorkcenterMapper workcenterMapper;

    @Resource
    private AuthenticationInfoService authenticationInfoService;

    @Autowired
    NewWorkcenterService newWorkcenterService;

    @Override
    public String getToken() {
        return "workcenter/list";
    }

    @Override
    public String getName() {
        return "获取工单中心分类列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
    })
    @Output({
            @Param(name = "workcenter", explode = WorkcenterVo.class, desc = "分类信息")
    })
    @Description(desc = "获取工单中心分类列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject workcenterJson = new JSONObject();
        String userUuid = UserContext.get().getUserUuid(true);
        AuthenticationInfoVo authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
        int isHasModifiedAuth = AuthActionChecker.check(WORKCENTER_MODIFY.class) ? 1 : 0;
        int isHasNewTypeAuth = AuthActionChecker.check(WORKCENTER_NEW_TYPE.class) ? 1 : 0;
        List<WorkcenterVo> workcenterList = new ArrayList<>();
        String viewType = "table";//默认table展示
        //根据用户（用户、组、角色）授权、支持设备和是否拥有工单中心管理权限，查出工单分类列表
        List<String> workcenterUuidList = workcenterMapper.getAuthorizedWorkcenterUuidList(userUuid, authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList(), CommonUtil.getDevice(), isHasModifiedAuth, isHasNewTypeAuth);
        if (CollectionUtils.isNotEmpty(workcenterUuidList)) {
            workcenterList = workcenterMapper.getAuthorizedWorkcenterListByUuidList(workcenterUuidList);
            WorkcenterUserProfileVo userProfile = workcenterMapper.getWorkcenterUserProfileByUserUuid(userUuid);
            Map<String, Integer> workcenterUserSortMap = new HashMap<String, Integer>();
            boolean isWorkcenterManager = AuthActionChecker.check(WORKCENTER_MODIFY.class.getSimpleName());
            if (userProfile != null) {
                JSONObject userConfig = JSONObject.parseObject(userProfile.getConfig());
                if (userConfig.containsKey("viewType")) {
                    viewType = userConfig.getString("viewType");
                }
                if (userConfig.containsKey("workcenterList")) {
                    JSONArray workcenterSortList = userConfig.getJSONArray("workcenterList");
                    for (Object workcenterSort : workcenterSortList) {
                        JSONObject workcenterSortJson = (JSONObject) workcenterSort;
                        workcenterUserSortMap.put(workcenterSortJson.getString("uuid"), workcenterSortJson.getInteger("sort"));
                    }
                }
            }
            BatchRunner<WorkcenterVo> runner = new BatchRunner<>();
            runner.execute(workcenterList, 3, workcenter -> {
                if (workcenter.getType().equals(ProcessWorkcenterType.FACTORY.getValue())) {
                    workcenter.setIsCanEdit(0);
                    if (Arrays.asList(ProcessWorkcenterInitType.ALL_PROCESSTASK.getValue(), ProcessWorkcenterInitType.DRAFT_PROCESSTASK.getValue(), ProcessWorkcenterInitType.DONE_OF_MINE_PROCESSTASK.getValue()).contains(workcenter.getUuid()) && isWorkcenterManager) {
                        workcenter.setIsCanRole(1);
                    }
                }
                if (workcenter.getType().equals(ProcessWorkcenterType.SYSTEM.getValue()) && isWorkcenterManager) {
                    workcenter.setIsCanEdit(1);
                    workcenter.setIsCanRole(1);
                } else if (workcenter.getType().equals(ProcessWorkcenterType.CUSTOM.getValue())) {
                    if (UserContext.get().getUserUuid(true).equalsIgnoreCase(workcenter.getOwner())) {
                        workcenter.setIsCanEdit(1);
                        if (AuthActionChecker.check(WORKCENTER_MODIFY.class.getSimpleName())) {
                            workcenter.setIsCanRole(1);
                        } else {
                            workcenter.setIsCanRole(0);
                        }
                    } else {
                        workcenter.setIsCanEdit(0);
                        workcenter.setIsCanRole(0);
                    }
                }

                //查询代办工单数量
                if (!StringUtils.equals(workcenter.getUuid(), ProcessWorkcenterInitType.DRAFT_PROCESSTASK.getValue()) && !StringUtils.equals(workcenter.getUuid(), ProcessWorkcenterInitType.DONE_OF_MINE_PROCESSTASK.getValue())) {
                    try {
                        JSONObject conditionJson = JSONObject.parseObject(workcenter.getConditionConfig());
                        JSONObject conditionConfig = conditionJson.getJSONObject("conditionConfig");
                        conditionConfig.put("isProcessingOfMine", 1);
                        conditionJson.put("expectOffsetRowNum", 100);
                        WorkcenterVo wcProcessingOfMine = new WorkcenterVo(conditionJson);
                        Integer ProcessingOfMineCount = newWorkcenterService.doSearchLimitCount(wcProcessingOfMine);
                        workcenter.setProcessingOfMineCount(ProcessingOfMineCount > 99 ? "99+" : ProcessingOfMineCount.toString());
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                }
                workcenter.getHandlerType();
                workcenter.setConditionConfig(null);
                //排序 用户设置的排序优先
                if (workcenterUserSortMap.containsKey(workcenter.getUuid())) {
                    workcenter.setSort(workcenterUserSortMap.get(workcenter.getUuid()));
                }
                //去除返回前端的多余字段
                workcenter.setConditionGroupList(null);
                workcenter.setConditionGroupRelList(null);
                workcenter.setIsProcessingOfMine(null);
            }, "WORKCENTER-LIST-SEARCHER");
        }
        workcenterJson.put("mobileIsOnline", Config.MOBILE_IS_ONLINE());
        workcenterJson.put("viewType", viewType);
        workcenterJson.put("workcenterList", workcenterList.stream().sorted(Comparator.comparing(WorkcenterVo::getSort)).collect(Collectors.toList()));
        return workcenterJson;
    }
}
