package codedriver.module.process.api.catalog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.exception.catalog.CatalogIllegalParameterException;
import codedriver.framework.process.exception.catalog.CatalogNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.CatalogVo;
import codedriver.module.process.dto.ITree;

@Service
@Transactional
public class CatalogMoveApi extends ApiComponentBase {

	@Autowired
	private CatalogMapper catalogMapper;
	
	@Override
	public String getToken() {
		return "process/catalog/move";
	}

	@Override
	public String getName() {
		return "服务目录移动位置接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "被移动的服务目录uuid"),
		@Param(name = "parentUuid", type = ApiParamType.STRING, isRequired = true, desc = "移动后的父级uuid"),
		@Param(name = "targetUuid", type = ApiParamType.STRING, isRequired = true, desc = "目标节点uuid")
	})
	@Description(desc = "服务目录移动位置接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");		
		CatalogVo moveCatalog = catalogMapper.getCatalogByUuid(uuid);
		//判断被移动的服务目录是否存在
		if(moveCatalog == null) {
			throw new CatalogNotFoundException(uuid);
		}		
		String parentUuid = jsonObj.getString("parentUuid");
		//判断移动后的父级服务目录是否存在
		if(catalogMapper.checkCatalogIsExists(parentUuid) == 0) {
			throw new CatalogNotFoundException(parentUuid);
		}
		//目录只能移动到目录前面，不能移动到通道前面
		//目录只能移动到目录后面，不能移动到通道后面
		//目录只能移进目录里面，不能移进通道里面
		//所以目标节点只能是目录
		//目标节点uuid
		String targetUuid = jsonObj.getString("targetUuid");
		CatalogVo targetCatalog = catalogMapper.getCatalogByUuid(targetUuid);
		//判断目标节点服务目录是否存在
		if(targetCatalog == null) {
			throw new CatalogNotFoundException(targetUuid);
		}
		if(!parentUuid.equals(targetCatalog.getParentUuid())) {
			throw new CatalogIllegalParameterException("服务目录：'" + targetUuid + "'不是服务目录：'" + parentUuid + "'的子目录");
		}
		Integer newSort;
		//移动操作类型
//		String movetype = jsonObj.getString("movetype");	
//		if("prev".equals(movetype)) {
//			//移动到目标节点前面
//			newSort = targetCatalog.getSort();
//		}else if("next".equals(movetype)){
//			//移动到目标节点后面
//			newSort = targetCatalog.getSort() + 1;
//		}else {
//			//移进目标节点里面
//			newSort = catalogMapper.getMaxSortByParentUuid(parentUuid) + 1;
//		}
		if(parentUuid.equals(targetUuid)) {
			//移进目标节点里面
			newSort = catalogMapper.getMaxSortByParentUuid(parentUuid) + 1;
		}else {
			//移动到目标节点后面
			newSort = targetCatalog.getSort();
		}
		Integer oldSort = moveCatalog.getSort();
		
		//判断是否是在相同目录下移动
		if(parentUuid.equals(moveCatalog.getParentUuid())) {
			//相同目录下移动
			if(oldSort.compareTo(newSort) == 1) {//往前移动, 移动前后两个位置直接的服务目录序号加一
				catalogMapper.updateSortIncrement(parentUuid, newSort, oldSort - 1);
			}else if(oldSort.compareTo(newSort) == -1) {//往后移动, 移动前后两个位置直接的服务目录序号减一
				catalogMapper.updateSortDecrement(parentUuid, oldSort + 1, newSort);
			}
		}else {
			//不同同目录下移动
			//旧目录，被移动目录后面的兄弟节点序号减一
			catalogMapper.updateSortDecrement(moveCatalog.getParentUuid(), oldSort + 1, null);
			//新目录，目标目录后面的兄弟节点序号加一
			catalogMapper.updateSortIncrement(parentUuid, newSort, null);
		}
		
//		if(oldSort.compareTo(newSort) == 1) {//往前移动, 移动前后两个位置直接的服务目录序号加一
//			catalogMapper.updateSortIncrement(newSort, oldSort - 1);
//			channelMapper.updateSortIncrement(newSort, oldSort - 1);
//		}else if(oldSort.compareTo(newSort) == -1) {//往后移动, 移动前后两个位置直接的服务目录序号减一
//			catalogMapper.updateSortDecrement(oldSort + 1, newSort);
//			channelMapper.updateSortDecrement(oldSort + 1, newSort);
//		}
		
		moveCatalog.setSort(newSort);
		moveCatalog.setParentUuid(parentUuid);
		catalogMapper.updateCatalogForMove(moveCatalog);
		
		//判断移动后是否会脱离根目录
		String parentUuidTemp = parentUuid;
		do {
			CatalogVo parent = catalogMapper.getCatalogByUuid(parentUuidTemp);
			if(parent == null) {
				throw new CatalogIllegalParameterException("将服务目录：" + uuid + "的parentUuid设置为：" + parentUuid + "会导致该目录脱离根目录");
			}
			parentUuidTemp = parent.getParentUuid();
			if(parentUuid.equals(parentUuidTemp)) {
				throw new CatalogIllegalParameterException("将服务目录：" + uuid + "的parentUuid设置为：" + parentUuid + "会导致该目录脱离根目录");
			}
		}while(!ITree.ROOT_UUID.equals(parentUuidTemp));		
		return null;
	}

}
