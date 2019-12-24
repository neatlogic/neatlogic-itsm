package codedriver.framework.process.timeoutpolicy.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.RootComponent;
import codedriver.module.process.dto.TimeoutPolicyVo;

@RootComponent
public class TimeoutPolicyHandlerFactory implements ApplicationListener<ContextRefreshedEvent> {

	private static Map<String, ITimeoutPolicyHandler> componentMap = new HashMap<String, ITimeoutPolicyHandler>();

	private static List<TimeoutPolicyVo> timePolicyList = new ArrayList<>();
	
	public static ITimeoutPolicyHandler getHandler(String name) {
		if (!componentMap.containsKey(name) || componentMap.get(name) == null) {
			throw new RuntimeException("找不到类型为：" + name + "的超时策略");
		}
		return componentMap.get(name);
	}
	
	public static List<TimeoutPolicyVo> getAllActiveTimeoutPolicy(){
		return timePolicyList;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ApplicationContext context = event.getApplicationContext();
		Map<String, ITimeoutPolicyHandler> myMap = context.getBeansOfType(ITimeoutPolicyHandler.class);
		for (Map.Entry<String, ITimeoutPolicyHandler> entry : myMap.entrySet()) {
			ITimeoutPolicyHandler component = entry.getValue();
			if (component.getType() != null) {
				componentMap.put(component.getType(), component);
				TimeoutPolicyVo timeoutPolicy = new TimeoutPolicyVo();
				timeoutPolicy.setType(component.getType());
				timeoutPolicy.setName(component.getName());
				timeoutPolicy.setModuleId(context.getId());
				timePolicyList.add(timeoutPolicy);
			}
		}
	}
}
