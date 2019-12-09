package codedriver.framework.process.stephandler.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.RootComponent;
import codedriver.framework.dto.ModuleVo;
import codedriver.module.process.dto.ProcessStepHandlerVo;

@RootComponent
@Order(10)
public class ProcessStepHandlerFactory implements ApplicationListener<ContextRefreshedEvent> {
	private static Map<String, IProcessStepHandler> componentMap = new HashMap<String, IProcessStepHandler>();
	private static List<ProcessStepHandlerVo> processStepHandlerList = new ArrayList<>();

	// @PostConstruct
	// public void init() {
	// processStepHandlerMapper.resetProcessStepHandler();
	// }

	public static IProcessStepHandler getHandler(String handler) {
		if (!componentMap.containsKey(handler) || componentMap.get(handler) == null) {
			throw new RuntimeException("找不到类型为：" + handler + "的流程组件");
		}
		return componentMap.get(handler);
	}

	public static List<ProcessStepHandlerVo> getActiveProcessStepHandler() {
		TenantContext tenantContext = TenantContext.get();
		List<ModuleVo> moduleList =tenantContext.getActiveModuleList();
		List<ProcessStepHandlerVo> returnProcessStepHandlerList = new ArrayList<>();
		for (ProcessStepHandlerVo processStepHandler : processStepHandlerList) {
			for(ModuleVo moduleVo : moduleList) {
				if(moduleVo.getId().equalsIgnoreCase(processStepHandler.getModuleId())) {
					returnProcessStepHandlerList.add(processStepHandler);
					break;
				}
			}
		}
		Collections.sort(returnProcessStepHandlerList);
		return returnProcessStepHandlerList;
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
				processStepHandlerVo.setModuleId(context.getId());
				processStepHandlerVo.setIsActive(1);
				processStepHandlerList.add(processStepHandlerVo);
			}
		}
	}
}
