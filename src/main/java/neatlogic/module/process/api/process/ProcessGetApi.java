package neatlogic.module.process.api.process;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_MODIFY;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.dto.ProcessVo;
import neatlogic.framework.process.exception.process.ProcessNotFoundEditTargetException;
import neatlogic.framework.process.stephandler.core.ProcessMessageManager;
import neatlogic.framework.process.util.ProcessConfigUtil;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
@AuthAction(action = PROCESS_MODIFY.class)
public class ProcessGetApi extends PrivateApiComponentBase {

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

    @Override
    public String getToken() {
        return "process/get";
    }

    @Override
    public String getName() {
        return "nmpap.processgetapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
			@Param(name = "uuid", type = ApiParamType.STRING, desc = "term.itsm.processuuid"),
            @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "term.itsm.processtaskid")
    })
    @Output({
			@Param(explode = ProcessVo.class)
    })
    @Description(desc = "nmpap.processgetapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        if (processTaskId != null) {
            ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoByIdIncludeIsDeleted(processTaskId);
            if (processTaskVo != null) {
                String configStr = selectContentByHashMapper.getProcessTaskConfigStringByHash(processTaskVo.getConfigHash());
                ProcessVo processVo = processMapper.getProcessByUuid(processTaskVo.getProcessUuid());
                if (processVo == null) {
                    processVo = new ProcessVo();
                    processVo.setUuid(processTaskVo.getProcessUuid());
                    processVo.setName("");
                    processVo.setIsActive(1);
                }
                processVo.setName(processVo.getName() + "【工单：" + processTaskVo.getTitle()+ "】");
                JSONObject config = JSON.parseObject(configStr);
//                processVo.setConfig(configStr);
                try {
                    ProcessMessageManager.setOperationType(OperationTypeEnum.SEARCH);
                    config = ProcessConfigUtil.regulateProcessConfig(config);
                } finally {
                    ProcessMessageManager.release();
                }
                processVo.setConfig(config);
                processVo.setReferenceCount(1);
                return processVo;
            }
        } else {
            String uuid = jsonObj.getString("uuid");
            if (StringUtils.isNotBlank(uuid)) {
                ProcessVo processVo = processMapper.getProcessByUuid(uuid);
                if (processVo == null) {
                    throw new ProcessNotFoundEditTargetException(uuid);
                }
                try {
                    ProcessMessageManager.setOperationType(OperationTypeEnum.SEARCH);
                    processVo.setConfig(ProcessConfigUtil.regulateProcessConfig(processVo.getConfig()));
                } finally {
                    ProcessMessageManager.release();
                }
                int count = processMapper.getProcessReferenceCount(uuid);
                processVo.setReferenceCount(count);
                return processVo;
            }
        }
        return null;
    }
}
