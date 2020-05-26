package codedriver.module.process.api.catalog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Objects;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.dto.ITree;
import codedriver.framework.process.exception.catalog.CatalogIllegalParameterException;
import codedriver.framework.process.exception.catalog.CatalogNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.CatalogService;

@Service
@Transactional
public class CatalogMoveApi extends ApiComponentBase {

	@Autowired
	private CatalogMapper catalogMapper;
	
	@Autowired
	private CatalogService catalogService;
	
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
		@Param(name = "targetUuid", type = ApiParamType.STRING, isRequired = true, desc = "目标节点uuid"),
		@Param(name = "moveType", type = ApiParamType.ENUM, rule = "inner,prev,next", isRequired = true, desc = "移动类型")
	})
	@Description(desc = "服务目录移动位置接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {		
		catalogMapper.getCatalogLockByUuid(ITree.ROOT_UUID);
		if(!catalogService.checkLeftRightCodeIsExists()) {
			catalogService.rebuildLeftRightCode(ITree.ROOT_PARENTUUID, 0);
		}
		String uuid = jsonObj.getString("uuid");		
		CatalogVo moveCatalog = catalogMapper.getCatalogByUuid(uuid);
		//判断被移动的服务目录是否存在
		if(moveCatalog == null) {
			throw new CatalogNotFoundException(uuid);
		}		
		String parentUuid = jsonObj.getString("parentUuid");
		//判断移动后的父级服务目录是否存在
		CatalogVo parentCatalog = catalogMapper.getCatalogByUuid(parentUuid);
		if(parentCatalog == null) {
			throw new CatalogNotFoundException(parentUuid);
		}
		//目录只能移动到目录前面，不能移动到通道前面
		//目录只能移动到目录后面，不能移动到通道后面
		//目录只能移进目录里面，不能移进通道里面
		//所以目标节点只能是目录
		//目标节点uuid
		String targetUuid = jsonObj.getString("targetUuid");
		
//		Integer targetSort;
//		Integer newSort;
//
//		if(parentUuid.equals(targetUuid)) {
//			//移进目标节点里面
//			targetSort = catalogMapper.getMaxSortByParentUuid(parentUuid);
//		}else {
//			//移动到目标节点前面或后面
//			CatalogVo targetCatalog = catalogMapper.getCatalogByUuid(targetUuid);
//			//判断目标节点服务目录是否存在
//			if(targetCatalog == null) {
//				throw new CatalogNotFoundException(targetUuid);
//			}
//			if(!parentUuid.equals(targetCatalog.getParentUuid())) {
//				throw new CatalogIllegalParameterException("服务目录：'" + targetUuid + "'不是服务目录：'" + parentUuid + "'的子目录");
//			}
//			targetSort = targetCatalog.getSort();
//		}
//		Integer oldSort = moveCatalog.getSort();
//		String moveType = jsonObj.getString("moveType");
//		//判断是否是在相同目录下移动
//		if(parentUuid.equals(moveCatalog.getParentUuid())) {
//			//相同目录下移动
//			if(oldSort.compareTo(targetSort) == -1) {//往后移动, 移动前后两个位置直接的服务目录序号减一
//				if("prev".equals(moveType)) {
//					newSort = targetSort - 1;
//				}else{
//					newSort = targetSort;
//				}
//				catalogMapper.updateSortDecrement(parentUuid, oldSort + 1, newSort);
//			}else {//往前移动, 移动前后两个位置直接的服务目录序号加一
//				if("prev".equals(moveType)) {
//					newSort = targetSort;
//				}else{
//					newSort = targetSort + 1;
//				}
//				catalogMapper.updateSortIncrement(parentUuid, newSort, oldSort - 1);
//			}
//		}else {
//			//不同同目录下移动
//			if("prev".equals(moveType)) {
//				newSort = targetSort;
//			}else{
//				newSort = targetSort + 1;
//			}
//			//旧目录，被移动目录后面的兄弟节点序号减一
//			catalogMapper.updateSortDecrement(moveCatalog.getParentUuid(), oldSort + 1, null);
//			//新目录，目标目录后面的兄弟节点序号加一
//			catalogMapper.updateSortIncrement(parentUuid, newSort, null);
//		}
//		
//		moveCatalog.setSort(newSort);
//		moveCatalog.setParentUuid(parentUuid);
//		catalogMapper.updateCatalogForMove(moveCatalog);
//		//判断移动后是否会脱离根目录
//		String parentUuidTemp = parentUuid;
//		while(!ITree.ROOT_UUID.equals(parentUuidTemp)) {
//			CatalogVo parent = catalogMapper.getCatalogByUuid(parentUuidTemp);
//			if(parent == null) {
//				throw new CatalogIllegalParameterException("将服务目录：" + uuid + "的parentUuid设置为：" + parentUuid + "会导致该目录脱离根目录");
//			}
//			parentUuidTemp = parent.getParentUuid();
//			if(parentUuid.equals(parentUuidTemp)) {
//				throw new CatalogIllegalParameterException("将服务目录：" + uuid + "的parentUuid设置为：" + parentUuid + "会导致该目录脱离根目录");
//			}
//		}
		//-------------------------------------------------------------------------------------------------------
		if(Objects.equal(uuid, parentUuid)) {
        	throw new CatalogIllegalParameterException("移动后的父节点不可以是当前节点");
        }
        //找出被移动块移动后左编码值     	
		int lft = 0;
		String moveType = jsonObj.getString("moveType");
		if(parentUuid.equals(targetUuid)) {//移动到末尾
			lft = parentCatalog.getRht();
 		}else {
 			//移动到目标节点前面或后面
			CatalogVo targetCatalog = catalogMapper.getCatalogByUuid(targetUuid);
			//判断目标节点服务目录是否存在
			if(targetCatalog == null) {
				throw new CatalogNotFoundException(targetUuid);
			}
			if(!parentUuid.equals(targetCatalog.getParentUuid())) {
				throw new CatalogIllegalParameterException("服务目录：'" + targetUuid + "'不是服务目录：'" + parentUuid + "'的子目录");
			}
 			if("prev".equals(moveType)) {
 				lft = targetCatalog.getLft();
 			}else {
 				lft = targetCatalog.getRht() + 1;
 			}
 		}
        if(parentUuid.equals(moveCatalog.getParentUuid())) {
        	if(Objects.equal(moveCatalog.getLft(), lft)) {
        		return null;//没有移动
        	}
        }else {
        	//判断移动后的父节点是否在当前节点的后代节点中
            if(catalogMapper.checkCatalogIsExistsByLeftRightCode(parentUuid, moveCatalog.getLft(), moveCatalog.getRht()) > 0) {
            	throw new CatalogIllegalParameterException("移动后的父节点不可以是当前节点的后代节点");
            }
            moveCatalog.setParentUuid(parentUuid);
            catalogMapper.updateCatalogParentUuidByUuid(moveCatalog);
        }

 		//将被移动块中的所有节点的左右编码值设置为<=0
        catalogMapper.batchUpdateCatalogLeftRightCodeByLeftRightCode(moveCatalog.getLft(), moveCatalog.getRht(), -moveCatalog.getRht());
 		//计算被移动块右边的节点移动步长
 		int step = moveCatalog.getRht() - moveCatalog.getLft() + 1;
 		//更新旧位置右边的左右编码值
 		catalogMapper.batchUpdateCatalogLeftCode(moveCatalog.getLft(), -step);
 		catalogMapper.batchUpdateCatalogRightCode(moveCatalog.getLft(), -step);
		
		//更新新位置右边的左右编码值
 		catalogMapper.batchUpdateCatalogLeftCode(lft, step);
 		catalogMapper.batchUpdateCatalogRightCode(lft, step);
		
		//更新被移动块中节点的左右编码值
 		catalogMapper.batchUpdateCatalogLeftRightCodeByLeftRightCode(moveCatalog.getLft() - moveCatalog.getRht(), moveCatalog.getRht() - moveCatalog.getRht(), lft - moveCatalog.getLft() + moveCatalog.getRht());
		//-------------------------------------------------------------------------------------------------------
		return null;
	}

}
