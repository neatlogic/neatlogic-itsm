package codedriver.module.process.service;

public interface CatalogService {

	public boolean checkLeftRightCodeIsExists();

	public Integer rebuildLeftRightCode(String parentUuid, int parentLft);

}
