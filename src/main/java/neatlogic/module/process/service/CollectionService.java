package neatlogic.module.process.service;

import neatlogic.framework.process.dto.collection.CollectionVo;

public interface CollectionService {
    void saveCollection(CollectionVo collectionVo, String action);
}
