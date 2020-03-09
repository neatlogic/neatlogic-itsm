package codedriver.framework.process.workcenter.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import codedriver.module.process.workcenter.dto.WorkcenterRoleVo;
import codedriver.module.process.workcenter.dto.WorkcenterVo;

public interface WorkcenterMapper {
	
	public List<WorkcenterVo> getWorkcenter(WorkcenterVo workcenterVo);
	
	public List<WorkcenterVo> getWorkcenterByNameAndUuid(@Param("name")String workcenterName,@Param("uuid")String workcenterUuid);
	
	public Map<String,String> getWorkcenterConditionConfig();
	
	public Integer deleteWorkcenterByUuid(@Param("workcenterUuid")String workcenterUuid);
	
	public Integer deleteWorkcenterRoleByUuid(@Param("workcenterUuid")String workcenterUuid);
	
	public Integer deleteWorkcenterOwnerByUuid(@Param("workcenterUuid")String workcenterUuid);
	
	public Integer insertWorkcenter(WorkcenterVo workcenterVo);
	
	public Integer insertWorkcenterRole(WorkcenterRoleVo workcenterRoleVo); 
	
	public Integer insertWorkcenterOwner(@Param("userId")String owner,@Param("uuid")String workcenterUuid); 
	
	public Integer updateWorkcenter(WorkcenterVo workcenterVo);
}
