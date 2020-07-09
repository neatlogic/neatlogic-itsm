package codedriver.module.process.api.catalog;

import java.util.List;

import codedriver.framework.transaction.util.TransactionUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.AuthorityVo;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.exception.catalog.CatalogNameRepeatException;
import codedriver.framework.process.exception.catalog.CatalogNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.CatalogService;

@Service
@Transactional
public class CatalogSaveApi extends ApiComponentBase {

	@Autowired
	private CatalogMapper catalogMapper;

	@Autowired
	private CatalogService catalogService;

	@Autowired
	private TransactionUtil transactionUtil;
	
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
		@Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", isRequired= true, maxLength = 50, desc = "服务目录名称"),
		@Param(name = "parentUuid", type = ApiParamType.STRING, isRequired= true, desc = "父级uuid"),
		@Param(name = "isActive", type = ApiParamType.ENUM, isRequired= true, desc = "是否激活", rule = "0,1"),
		@Param(name = "icon", type = ApiParamType.STRING, isRequired= false, desc = "图标"),
		@Param(name = "color", type = ApiParamType.STRING, isRequired= false, desc = "颜色"),
		@Param(name = "desc", type = ApiParamType.STRING, isRequired= false, desc = "描述", maxLength = 200, xss = true),
		@Param(name = "authorityList", type = ApiParamType.JSONARRAY, desc = "授权对象，可多选，格式[\"user#userUuid\",\"team#teamUuid\",\"role#roleUuid\"]")
		})
	@Output({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired= true, desc = "服务目录uuid")
		})
	@Description(desc = "服务目录保存信息接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		TransactionStatus transactionStatus = transactionUtil.openTx();
		//根据catalog表中的count和最大的右编码进行比较
		if(!catalogService.checkLeftRightCodeIsExists()) {
			catalogService.rebuildLeftRightCode(CatalogVo.ROOT_PARENTUUID, 0);
		}
		//构造一个虚拟的root节点
		CatalogVo rootCatalogVo = catalogService.buildRootCatalog();
		CatalogVo catalogVo = JSON.toJavaObject(jsonObj, CatalogVo.class);
		//获取父级信息
		String parentUuid = catalogVo.getParentUuid();
		CatalogVo parentCatalog;
		//如果parentUuid为0，则表明其目标父目录为root
		if("0".equals(parentUuid)){
			parentCatalog = rootCatalogVo;
		}else{
			parentCatalog = catalogMapper.getCatalogByUuid(parentUuid);
		}
		if(parentCatalog == null) {
			throw new CatalogNotFoundException(parentUuid);
		}
		if(catalogMapper.checkCatalogNameIsRepeat(catalogVo) > 0) {
			throw new CatalogNameRepeatException(catalogVo.getName());
		}

		String uuid = catalogVo.getUuid();
		if(CatalogVo.UNCATEGORIZED_CATALOG_UUID.equals(uuid)) {
			return uuid;
		}
		CatalogVo existedCatalog = catalogMapper.getCatalogByUuid(uuid);
		if(existedCatalog == null) {//新增
			catalogVo.setUuid(null);
			catalogVo.setLft(parentCatalog.getRht());
			catalogVo.setRht(catalogVo.getLft() + 1);
			//更新插入位置右边的左右编码值
			catalogMapper.batchUpdateCatalogLeftCode(catalogVo.getLft(), 2);
			catalogMapper.batchUpdateCatalogRightCode(catalogVo.getLft(), 2);
		}else {//修改
			catalogMapper.deleteCatalogAuthorityByCatalogUuid(uuid);
			catalogVo.setLft(existedCatalog.getLft());
			catalogVo.setRht(existedCatalog.getRht());
		}

		catalogMapper.replaceCatalog(catalogVo);
		List<AuthorityVo> authorityList = catalogVo.getAuthorityVoList();
		if(CollectionUtils.isNotEmpty(authorityList)) {
			for(AuthorityVo authorityVo : authorityList) {
				catalogMapper.insertCatalogAuthority(authorityVo,catalogVo.getUuid());
			}
		}
		transactionUtil.commitTx(transactionStatus);
		return catalogVo.getUuid();
	}

}
