/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.api.dashboard;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.Expression;
import neatlogic.framework.condition.core.ConditionHandlerFactory;
import neatlogic.framework.condition.core.IConditionHandler;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.condition.core.IProcessTaskCondition;
import neatlogic.framework.process.constvalue.ProcessWorkcenterField;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;

@Service
@Transactional
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class DashboardGetConditionApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "dashboard/condition/get";
	}

	@Override
	public String getName() {
		return "获取仪表板条件接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		
	})
	@Output({
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "组件uuid"),
		@Param(name = "handler", type = ApiParamType.STRING, desc = "处理器"),
		@Param(name = "handlerName", type = ApiParamType.STRING, desc = "处理器名"),
		@Param(name = "handlerType", type = ApiParamType.STRING, desc = "控件类型 select|input|radio|userselect|date|area|time"),
		@Param(name = "type", type = ApiParamType.STRING, desc = "类型  form|common"),
		@Param(name = "expressionList[0].expression", type = ApiParamType.STRING, desc = "表达式"),
		@Param(name = "expressionList[0].expressionName", type = ApiParamType.STRING, desc = "表达式名")
	})
	@Description(desc = "获取仪表板条件接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONArray resultArray = new JSONArray();
		//固定字段条件
		for(IConditionHandler condition : ConditionHandlerFactory.getConditionHandlerList()) {
			if(condition instanceof IProcessTaskCondition && ProcessWorkcenterField.getValue(condition.getName()) != null) {
				JSONObject commonObj = new JSONObject();
				commonObj.put("handler", condition.getName());
				commonObj.put("handlerName", condition.getDisplayName());
				commonObj.put("handlerType", condition.getHandler(FormConditionModel.CUSTOM));
				if(condition.getConfig() != null) {
					commonObj.put("isMultiple",condition.getConfig().getBoolean("isMultiple"));
				}
				commonObj.put("conditionModel", condition.getHandler(FormConditionModel.CUSTOM));
				commonObj.put("type", condition.getType());
				commonObj.put("config", condition.getConfig());
				commonObj.put("defaultExpression", condition.getParamType().getDefaultExpression().getExpression());
				commonObj.put("sort", condition.getSort());
				JSONArray expressionArray = new JSONArray();
				for(Expression expression:condition.getParamType().getExpressionList()) {
					JSONObject expressionObj = new JSONObject();
					expressionObj.put("expression", expression.getExpression());
					expressionObj.put("expressionName", expression.getExpressionName());
					expressionArray.add(expressionObj);
					commonObj.put("expressionList", expressionArray);
				}
				resultArray.add(commonObj);
			}			
		}
		Collections.sort(resultArray, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				try {
					JSONObject obj1 = (JSONObject) o1;
					JSONObject obj2 = (JSONObject) o2;
					return obj1.getIntValue("sort") - obj2.getIntValue("sort");
				} catch (Exception ex) {

				}
				return 0;
			}
		});
		return resultArray;
	}

}
