package codedriver.module.process.api.catalog;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.exception.CatalogDuplicateNameException;
import codedriver.framework.process.exception.CatalogNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.CatalogVo;

@Service
@Transactional
public class CatalogSaveApi extends ApiComponentBase {

	@Autowired
	private CatalogMapper catalogMapper;
	
	@Override
	public String getToken() {
		return "process/catalog/save";
	}

	@Override
	public String getName() {
		return "服务目录保存信息接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "服务目录uuid"),
		@Param(name = "name", type = ApiParamType.STRING, isRequired= true, desc = "服务目录名称", length = 30, xss = true),
		@Param(name = "parentUuid", type = ApiParamType.STRING, isRequired= true, desc = "父级uuid"),
		@Param(name = "isActive", type = ApiParamType.ENUM, isRequired= true, desc = "是否激活", rule = "0,1"),
		@Param(name = "icon", type = ApiParamType.STRING, isRequired= false, desc = "图标"),
		@Param(name = "color", type = ApiParamType.STRING, isRequired= false, desc = "颜色"),
		@Param(name = "desc", type = ApiParamType.STRING, isRequired= false, desc = "描述"),
		@Param(name = "roleNameList", type = ApiParamType.JSONARRAY, isRequired= false, desc = "角色列表"),
		@Param(name = "roleNameList[0]", type = ApiParamType.STRING, isRequired= false, desc = "角色名")
		})
	@Output({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired= true, desc = "服务目录uuid")
		})
	@Description(desc = "服务目录保存信息接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		CatalogVo catalogVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<CatalogVo>() {});
		//获取父级信息
		String parentUuid = catalogVo.getParentUuid();
		if(catalogMapper.checkCatalogIsExists(parentUuid) == 0) {
			throw new CatalogNotFoundException(parentUuid);
		}
		if(catalogMapper.checkCatalogIsDuplicateName(catalogVo) > 0) {
			throw new CatalogDuplicateNameException(catalogVo.getName());
		}
		int sort;
		String uuid = catalogVo.getUuid();
		CatalogVo existedCatalog = catalogMapper.getCatalogByUuid(uuid);
		if(existedCatalog == null) {//新增
			catalogVo.setUuid(null);
			sort = catalogMapper.getMaxSortByParentUuid(parentUuid) + 1;
		}else {//修改
			catalogMapper.deleteCatalogRoleByUuid(uuid);
			sort = existedCatalog.getSort();
		}
		catalogVo.setSort(sort);
		catalogMapper.replaceCatalog(catalogVo);
		List<String> roleNameList = catalogVo.getRoleNameList();
		if(roleNameList != null && roleNameList.size() > 0) {
			for(String roleName : roleNameList) {
				//TODO linbq判断角色是否存在
				catalogMapper.insertCatalogRole(uuid, roleName);
			}
		}
		return catalogVo.getUuid();
	}

}
