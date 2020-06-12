package codedriver.module.process.condition.handler;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.condition.ConditionVo;

@Component
public class ProcessTaskContentCondition extends ProcessTaskConditionBase implements IProcessTaskCondition{
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Override
	public String getName() {
		return "content";
	}

	@Override
	public String getDisplayName() {
		return "上报内容";
	}

	@Override
	public String getHandler(String processWorkcenterConditionType) {
		return FormHandlerType.INPUT.toString();
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

	@Override
	public JSONObject getConfig() {
		return null;
	}

	@Override
	public Integer getSort() {
		return 3;
	}

	@Override
	public ParamType getParamType() {
		return ParamType.STRING;
	}

	@Override
	public boolean predicate(ProcessTaskStepVo currentProcessTaskStepVo, ConditionVo conditionVo) {
		if(Expression.LIKE.getExpression().equals(conditionVo.getExpression())) {
			List<String> valueList = conditionVo.getValueList();
			if(CollectionUtils.isEmpty(valueList)) {
				return false;
			}
			String content = conditionVo.getValueList().get(0);
			if(StringUtils.isBlank(content)) {
				return false;
			}
			//获取开始步骤id
			List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(currentProcessTaskStepVo.getProcessTaskId(), ProcessStepType.START.getValue());
			Long startProcessTaskStepId = processTaskStepList.get(0).getId();
			//获取上报描述内容
			List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentProcessTaskStepId(startProcessTaskStepId);
			if(processTaskStepContentList.isEmpty()) {
				return false;
			}
			ProcessTaskContentVo processTaskContentVo = processTaskMapper.getProcessTaskContentByHash(processTaskStepContentList.get(0).getContentHash());
			if(processTaskContentVo == null) {
				return false;
			}
			
			return processTaskContentVo.getContent().contains(content);
		}else {
			return false;
		}
	}
	
	@Override
	protected String getMyEsWhere(Integer index,List<ConditionVo> conditionList) {
		ConditionVo condition = conditionList.get(index);
		String where = "(";
		if(condition.getValueList().size() == 1) {
			Object value = condition.getValueList().get(0);
			where += String.format(Expression.getExpressionEs(condition.getExpression()),ProcessWorkcenterField.getConditionValue(ProcessWorkcenterField.CONTENT.getValue()),String.format("'%s'",  value));
		}else {
			List<String> keywordList = condition.getValueList();
			for(int i=0;i<keywordList.size();i++) {
				if(i!=0) {
					where += " or ";
				}
				where += String.format(Expression.getExpressionEs(condition.getExpression()),ProcessWorkcenterField.getConditionValue(ProcessWorkcenterField.CONTENT.getValue()),String.format("'%s'",  keywordList.get(i)));
			}
		}
		return where+")";
	}
}
