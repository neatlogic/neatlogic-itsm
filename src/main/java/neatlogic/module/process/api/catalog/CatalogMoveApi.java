/*
Copyright(c) $today.year NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package neatlogic.module.process.api.catalog;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.lrcode.LRCodeManager;
import neatlogic.framework.lrcode.constvalue.MoveType;
import neatlogic.framework.process.dao.mapper.CatalogMapper;
import neatlogic.framework.process.dto.CatalogVo;
import neatlogic.framework.process.exception.catalog.CatalogNameRepeatException;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.process.auth.CATALOG_MODIFY;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = CATALOG_MODIFY.class)
public class CatalogMoveApi extends PrivateApiComponentBase {

	@Resource
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
		@Param(name = "targetUuid", type = ApiParamType.STRING, isRequired = true, desc = "目标节点uuid"),
		@Param(name = "moveType", type = ApiParamType.ENUM, rule = "inner,prev,next", isRequired = true, desc = "移动类型")
	})
	@Description(desc = "服务目录移动位置接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		String moveType = jsonObj.getString("moveType");
		String targetUuid = jsonObj.getString("targetUuid");
		LRCodeManager.moveTreeNode("catalog", "uuid", "parent_uuid", uuid, MoveType.getMoveType(moveType), targetUuid);
		String parentUuid = catalogMapper.getParentUuidByUuid(uuid);
		CatalogVo catalogVo = catalogMapper.getCatalogByUuid(uuid);
		catalogVo.setParentUuid(parentUuid);
		//判断移动后相同目录下是否有同名目录
		if(catalogMapper.checkCatalogNameIsRepeat(catalogVo) > 0) {
			throw new CatalogNameRepeatException(catalogVo.getName());
		}
		return null;
	}
//	private Object backup(JSONObject jsonObj) throws Exception {
//		catalogMapper.getCatalogCountOnLock();
//		if(catalogMapper.checkLeftRightCodeIsWrong() > 0) {
//			catalogService.rebuildLeftRightCode();
//		}
//		String uuid = jsonObj.getString("uuid");
//		CatalogVo moveCatalog = catalogMapper.getCatalogByUuid(uuid);
//		//判断被移动的服务目录是否存在
//		if(moveCatalog == null) {
//			throw new CatalogNotFoundException(uuid);
//		}
//		String parentUuid = jsonObj.getString("parentUuid");
//		//判断移动后的父级服务目录是否存在
//		CatalogVo parentCatalog = null;
//		//如果parentUuid为0，则表明其目标父目录为root，那么就构建一个虚拟的root
//		if(CatalogVo.ROOT_UUID.equals(parentUuid)){
//			parentCatalog = catalogService.buildRootCatalog();
//		}else {
//			parentCatalog = catalogMapper.getCatalogByUuid(parentUuid);
//			if(parentCatalog == null) {
//				throw new CatalogNotFoundException(parentUuid);
//			}
//		}
//		//目录只能移动到目录前面，不能移动到通道前面
//		//目录只能移动到目录后面，不能移动到通道后面
//		//目录只能移进目录里面，不能移进通道里面
//		//所以目标节点只能是目录
//		if(Objects.equal(uuid, parentUuid)) {
//			throw new CatalogIllegalParameterException("移动后的父节点不可以是当前节点");
//		}
//
//		//将被移动块中的所有节点的左右编码值设置为<=0
//		catalogMapper.batchUpdateCatalogLeftRightCodeByLeftRightCode(moveCatalog.getLft(), moveCatalog.getRht(), -moveCatalog.getRht());
//		//计算被移动块右边的节点移动步长
//		int step = moveCatalog.getRht() - moveCatalog.getLft() + 1;
//		//更新旧位置右边的左右编码值
//		catalogMapper.batchUpdateCatalogLeftCode(moveCatalog.getLft(), -step);
//		catalogMapper.batchUpdateCatalogRightCode(moveCatalog.getLft(), -step);
//
//		//找出被移动块移动后左编码值
//		int lft = 0;
//		String moveType = jsonObj.getString("moveType");
//		//目标节点uuid
//		String targetUuid = jsonObj.getString("targetUuid");
//		if(parentUuid.equals(targetUuid)) {//移动到末尾
//			lft = parentCatalog.getRht();
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
//			if("prev".equals(moveType)) {
//				lft = targetCatalog.getLft();
//			}else {
//				lft = targetCatalog.getRht() + 1;
//			}
//		}
//		if(parentUuid.equals(moveCatalog.getParentUuid())) {
//			if(Objects.equal(moveCatalog.getLft(), lft)) {
//				return null;//没有移动
//			}
//		}else {
//			//判断移动后的父节点是否在当前节点的后代节点中
//			if(catalogMapper.checkCatalogIsExistsByLeftRightCode(parentUuid, moveCatalog.getLft(), moveCatalog.getRht()) > 0) {
//				throw new CatalogIllegalParameterException("移动后的父节点不可以是当前节点的后代节点");
//			}
//			moveCatalog.setParentUuid(parentUuid);
//			catalogMapper.updateCatalogParentUuidByUuid(moveCatalog);
//		}
//
//		//更新新位置右边的左右编码值
//		catalogMapper.batchUpdateCatalogLeftCode(lft, step);
//		catalogMapper.batchUpdateCatalogRightCode(lft, step);
//
//		//更新被移动块中节点的左右编码值
//		catalogMapper.batchUpdateCatalogLeftRightCodeByLeftRightCode(moveCatalog.getLft() - moveCatalog.getRht(), moveCatalog.getRht() - moveCatalog.getRht(), lft - moveCatalog.getLft() + moveCatalog.getRht());
//		//-------------------------------------------------------------------------------------------------------
//		return null;
//	}
}
