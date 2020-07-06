package codedriver.module.process.service;

import java.util.List;

public interface CatalogService {

	public boolean checkLeftRightCodeIsExists();

	public Integer rebuildLeftRightCode(String parentUuid, int parentLft);

	public List<String> getCurrentUserAuthorizedChannelUuidList();
	
	public boolean channelIsAuthority(String channelUuid);
}
