package codedriver.framework.process.workcenter.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import codedriver.module.process.workcenter.dto.WorkcenterVo;

public interface WorkcenterMapper {
	
	public List<WorkcenterVo> getWorkcenter(WorkcenterVo workcenterVo);
	
	public Map<String,String> getWorkcenterConditionConfig();
	
	public Integer deleteWorkcenterByUuid(@Param("workcenterUuid")String workcenterUuid);
	
	public Integer deleteWorkcenterRoleByUuid(@Param("workcenterUuid")String workcenterUuid);
	
	public Integer insertWorkcenter(WorkcenterVo workcenterVo);
	
	public Integer insertWorkcenterRole(@Param("workcenterUuid") String workcenterUuid,@Param("roleName") String roleName);
	
	public Integer updateWorkcenter(WorkcenterVo workcenterVo);
}
