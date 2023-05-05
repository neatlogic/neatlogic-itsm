package neatlogic.module.process.api.commenttemplate;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.auth.PROCESS_COMMENT_TEMPLATE_MODIFY;
import neatlogic.framework.process.dao.mapper.ProcessCommentTemplateMapper;
import neatlogic.framework.process.dto.ProcessCommentTemplateAuthVo;
import neatlogic.framework.process.dto.ProcessCommentTemplateVo;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessCommentSystemTemplateSearchApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessCommentTemplateMapper commentTemplateMapper;


    @Override
    public String getToken() {
        return "process/comment/system/template/search";
    }

    @Override
    public String getName() {
        return "查询系统回复模版";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", explode = ProcessCommentTemplateVo.class, desc = "回复模版集合")
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
//        ProcessCommentTemplateVo vo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ProcessCommentTemplateVo>() {
//        });
////        vo.setType(ProcessCommentTemplateVo.TempalteType.SYSTEM.getValue());
//
//        //根据当前用户所在组、角色筛选其能看到的模版
//        List<String> uuidList = UserContext.get().getUuidList();
//        uuidList.add(UserType.ALL.getValue());
//        vo.setAuthList(uuidList);
//
//        JSONObject returnObj = new JSONObject();
//        if (vo.getNeedPage()) {
//            int rowNum = commentTemplateMapper.searchTemplateCount(vo);
//            returnObj.put("pageSize", vo.getPageSize());
//            returnObj.put("currentPage", vo.getCurrentPage());
//            returnObj.put("rowNum", rowNum);
//            returnObj.put("pageCount", PageUtil.getPageCount(rowNum, vo.getPageSize()));
//        }
//        List<ProcessCommentTemplateVo> tbodyList = commentTemplateMapper.searchTemplate(vo);
//        if(CollectionUtils.isNotEmpty(tbodyList)){
//            //有系统模版管理权限才能编辑系统模版
//            tbodyList.forEach(o -> {
//                if((ProcessCommentTemplateVo.TempalteType.SYSTEM.getValue().equals(o.getType())
//                        && AuthActionChecker.check(PROCESS_COMMENT_TEMPLATE_MODIFY.class.getSimpleName()))
//                        || ProcessCommentTemplateVo.TempalteType.CUSTOM.getValue().equals(o.getType())){
//                    o.setIsEditable(1);
//                }else{
//                    o.setIsEditable(0);
//                }
//            });
//        }
//        returnObj.put("tbodyList", tbodyList);
//        return returnObj;
        BasePageVo searchVo = jsonObj.toJavaObject(BasePageVo.class);
        List<Long> customTemplateIdList = commentTemplateMapper.getCustomTemplateIdListByUserUuid(UserContext.get().getUserUuid());
        if (CollectionUtils.isNotEmpty(customTemplateIdList)) {
            JSONArray defaultValue = new JSONArray();
            for (Long id : customTemplateIdList) {
                defaultValue.add(id);
            }
            searchVo.setDefaultValue(defaultValue);
        }
        int rowNum = commentTemplateMapper.searchCommentTemplateCount(searchVo);
        if (rowNum == 0) {
            return TableResultUtil.getResult(new ArrayList(), searchVo);
        }
        searchVo.setRowNum(rowNum);
        List<ProcessCommentTemplateVo> tbodyList = commentTemplateMapper.searchCommentTemplateList(searchVo);
        List<Long> systemTemplateIdList = tbodyList.stream().filter(e -> ProcessCommentTemplateVo.TempalteType.SYSTEM.getValue().equals(e.getType())).map(ProcessCommentTemplateVo::getId).collect(Collectors.toList());
        Map<Long, List<ProcessCommentTemplateAuthVo>> authMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(systemTemplateIdList)) {
            List<ProcessCommentTemplateAuthVo> authVoList = commentTemplateMapper.getProcessCommentTemplateAuthListByCommentTemplateIdList(systemTemplateIdList);
            for (ProcessCommentTemplateAuthVo authVo : authVoList) {
                Long commentTemplateId = authVo.getCommentTemplateId();
                authMap.computeIfAbsent(commentTemplateId, key -> new ArrayList<>()).add(authVo);
            }
        }
        if(CollectionUtils.isNotEmpty(tbodyList)){
            //有系统模版管理权限才能编辑系统模版
            for (ProcessCommentTemplateVo processCommentTemplateVo : tbodyList) {
                if(ProcessCommentTemplateVo.TempalteType.SYSTEM.getValue().equals(processCommentTemplateVo.getType())){
                    List<ProcessCommentTemplateAuthVo> authVoList = authMap.get(processCommentTemplateVo.getId());
                    if (CollectionUtils.isNotEmpty(authVoList)) {
                        List<String> authList = new ArrayList<>(authVoList.size());
                        for (ProcessCommentTemplateAuthVo authVo : authVoList) {
                            authList.add(authVo.getType() + "#" + authVo.getUuid());
                        }
                        processCommentTemplateVo.setAuthList(authList);
                    }
                    if (AuthActionChecker.check(PROCESS_COMMENT_TEMPLATE_MODIFY.class.getSimpleName())) {
                        processCommentTemplateVo.setIsEditable(1);
                    } else{
                        processCommentTemplateVo.setIsEditable(0);
                    }
                } else if (ProcessCommentTemplateVo.TempalteType.CUSTOM.getValue().equals(processCommentTemplateVo.getType())) {
                    processCommentTemplateVo.setIsEditable(1);
                }
            }
        }
        return TableResultUtil.getResult(tbodyList, searchVo);
    }
}
