package codedriver.module.process.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dao.mapper.ModuleMapper;
import codedriver.framework.dto.ModuleVo;

public class TestProcessFilter extends OncePerRequestFilter {

	@Autowired
	ModuleMapper moduleMapper;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		TenantContext tenantContext = TenantContext.init();
		List<ModuleVo> activeModuleList = new ArrayList<ModuleVo>();
		ModuleVo module1 = new ModuleVo();
		module1.setName("framework");
		module1.setId("framework");
		ModuleVo module2 = new ModuleVo();
		module2.setName("process");
		module2.setId("process");
		activeModuleList.add(module1);
		activeModuleList.add(module2);
		tenantContext.setActiveModuleList(activeModuleList);

		tenantContext.setTenantUuid("techsure");
		tenantContext.setUseDefaultDatasource(false);

		JSONObject jsonObj = new JSONObject();
		jsonObj.put("userid", "lvzk");
		jsonObj.put("username", "lvzk");
		jsonObj.put("tenant", "techsure");
		JSONArray roleList = new JSONArray();
		roleList.add("R_ADMIN");
		jsonObj.put("rolelist", roleList);
		UserContext.init(jsonObj, "+8:00", request, response);
		System.out.println(UserContext.get().getUserId());
		filterChain.doFilter(request, response);
	}

	/**
	 * @see Filter#destroy()
	 */
	@Override
	public void destroy() {
	}
}
