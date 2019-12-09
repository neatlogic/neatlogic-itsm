package codedriver.framework.process.stephandler.core;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;

import codedriver.framework.common.RootComponent;
import codedriver.framework.process.dto.ProcessStepHandlerVo;
import codedriver.framework.process.mapper.ProcessStepHandlerMapper;

@RootComponent
@Order(10)
public class ProcessStepHandlerFactory implements ApplicationListener<ContextRefreshedEvent> {
	private static Map<String, IProcessStepHandler> componentMap = new HashMap<String, IProcessStepHandler>();

	@Autowired
	private ProcessStepHandlerMapper processStepHandlerMapper;

	@PostConstruct
	public void init() {
		processStepHandlerMapper.resetProcessStepHandler();
	}

	public static IProcessStepHandler getHandler(String handler) {
		if (!componentMap.containsKey(handler) || componentMap.get(handler) == null) {
			throw new RuntimeException("找不到类型为：" + handler + "的流程组件");
		}
		return componentMap.get(handler);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ApplicationContext context = event.getApplicationContext();
		Map<String, IProcessStepHandler> myMap = context.getBeansOfType(IProcessStepHandler.class);
		for (Map.Entry<String, IProcessStepHandler> entry : myMap.entrySet()) {
			IProcessStepHandler component = entry.getValue();
			if (component.getType() != null) {
				componentMap.put(component.getType(), component);
				ProcessStepHandlerVo processStepHandlerVo = new ProcessStepHandlerVo();
				processStepHandlerVo.setType(component.getType());
				processStepHandlerVo.setName(component.getName());
				processStepHandlerVo.setIcon(component.getIcon());
				processStepHandlerVo.setSort(component.getSort());
				processStepHandlerVo.setIsActive(1);
				processStepHandlerMapper.replaceProcessStepHandler(processStepHandlerVo);
			}
		}
	}
}
