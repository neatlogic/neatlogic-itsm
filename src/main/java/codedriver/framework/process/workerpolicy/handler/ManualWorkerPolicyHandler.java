package codedriver.framework.process.workerpolicy.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.module.process.constvalue.WorkerPolicy;

@Service
public class ManualWorkerPolicyHandler implements IWorkerPolicyHandler {

	@Override
	public String getType() {
		return WorkerPolicy.MANUAL.getValue();
	}

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private TeamMapper teamMapper;

	@Autowired
	private RoleMapper roleMapper;

	@Override
	public List<ProcessTaskStepWorkerVo> execute(ProcessTaskStepWorkerPolicyVo workerPolicyVo, ProcessTaskStepVo currentProcessTaskStepVo) {
		List<ProcessTaskStepWorkerVo> workerList = new ArrayList<>();
		if (workerPolicyVo.getConfigObjList() != null && workerPolicyVo.getConfigObjList().size() > 0) {
			for (int i = 0; i < workerPolicyVo.getConfigObjList().size(); i++) {
				JSONObject userObj = workerPolicyVo.getConfigObjList().getJSONObject(i);
				String userId = userObj.getString("userid");
				if (StringUtils.isNotBlank(userId)) {
					if (userId.startsWith("user.")) {
						String uid = userId.substring(5);
						if (userMapper.getUserByUserId(uid) != null) {
							ProcessTaskStepWorkerVo workerVo = new ProcessTaskStepWorkerVo();
							workerVo.setUserId(uid);
							workerVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
							workerVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
							if (!workerList.contains(workerVo)) {
								workerList.add(workerVo);
							}
						}
					} else if (userId.startsWith("team.")) {
						String tid = null;
						try {
							tid = userId.substring(5);
						} catch (Exception ex) {

						}
						if (tid != null && teamMapper.getTeamByUuid(tid) != null) {
							ProcessTaskStepWorkerVo workerVo = new ProcessTaskStepWorkerVo();
							workerVo.setTeamUuid(tid);
							workerVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
							workerVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
							if (!workerList.contains(workerVo)) {
								workerList.add(workerVo);
							}
						}
					} else if (userId.startsWith("role.")) {
						String rname = userId.substring(5);
						if (roleMapper.getRoleByRoleName(rname) != null) {
							ProcessTaskStepWorkerVo workerVo = new ProcessTaskStepWorkerVo();
							workerVo.setRoleName(rname);
							workerVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
							workerVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
							if (!workerList.contains(workerVo)) {
								workerList.add(workerVo);
							}
						}
					}
				}
			}

		}
		return workerList;
	}
}
