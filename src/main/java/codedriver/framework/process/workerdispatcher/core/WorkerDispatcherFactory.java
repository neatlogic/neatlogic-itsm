package codedriver.framework.process.workerdispatcher.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.RootComponent;
import codedriver.framework.dto.ModuleVo;
import codedriver.module.process.dto.WorkerDispatcherVo;

@RootComponent
public class WorkerDispatcherFactory implements ApplicationListener<ContextRefreshedEvent> {

	private static Map<String, IWorkerDispatcher> componentMap = new HashMap<String, IWorkerDispatcher>();
	private static List<WorkerDispatcherVo> workerDispatcherList = new ArrayList<>();

	public static IWorkerDispatcher getDispatcher(String name) {
		if (!componentMap.containsKey(name) || componentMap.get(name) == null) {
			throw new RuntimeException("找不到类型为：" + name + "的处理人分派组件");
		}
		return componentMap.get(name);
	}

	public static List<WorkerDispatcherVo> getAllActiveWorkerDispatcher() {
		TenantContext tenantContext = TenantContext.get();
		List<ModuleVo> moduleList = tenantContext.getActiveModuleList();
		List<WorkerDispatcherVo> returnWorkerDispatcherList = new ArrayList<>();
		for (WorkerDispatcherVo workerDispatcherVo : workerDispatcherList) {
			for (ModuleVo moduleVo : moduleList) {
				if (moduleVo.getId().equalsIgnoreCase(workerDispatcherVo.getModuleId())) {
					returnWorkerDispatcherList.add(workerDispatcherVo);
					break;
				}
			}
		}
		return returnWorkerDispatcherList;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ApplicationContext context = event.getApplicationContext();
		Map<String, IWorkerDispatcher> myMap = context.getBeansOfType(IWorkerDispatcher.class);
		for (Map.Entry<String, IWorkerDispatcher> entry : myMap.entrySet()) {
			IWorkerDispatcher component = entry.getValue();
			if (StringUtils.isNotBlank(component.getHandler())) {
				componentMap.put(component.getHandler(), component);
				WorkerDispatcherVo workerDispatcherVo = new WorkerDispatcherVo();
				workerDispatcherVo.setHandler(component.getHandler());
				workerDispatcherVo.setName(component.getName());
				workerDispatcherVo.setIsActive(1);
				workerDispatcherVo.setHelp(component.getHelp());
				workerDispatcherVo.setConfigPage(component.getConfigPage());
				workerDispatcherVo.setModuleId(context.getId());
				workerDispatcherList.add(workerDispatcherVo);
			}
		}
	}
}
