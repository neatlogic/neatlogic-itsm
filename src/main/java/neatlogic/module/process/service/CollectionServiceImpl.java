package neatlogic.module.process.service;

import neatlogic.framework.process.constvalue.collection.PhaseStatus;
import neatlogic.framework.process.dto.collection.CollectionPhaseVo;
import neatlogic.framework.process.dto.collection.CollectionVo;
import neatlogic.framework.process.exception.collection.PhaseNotFoundException;
import neatlogic.module.process.dao.mapper.collection.CollectionMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CollectionServiceImpl implements CollectionService {

    @Resource
    private CollectionMapper collectionMapper;

    @Override
    public void saveCollection(CollectionVo collectionVo, String action) {
        if (CollectionUtils.isEmpty(collectionVo.getPhaseList())) {
            throw new PhaseNotFoundException();
        }
        if (action.equals("insert")) {
            collectionMapper.insertCollection(collectionVo);
        }
        int sort = 0;
        for (CollectionPhaseVo phaseVo : collectionVo.getPhaseList()) {
            phaseVo.setCollectionId(collectionVo.getId());
            phaseVo.setSort(sort);
            phaseVo.setIsActive(0);
            phaseVo.setStatus(PhaseStatus.PENDING.getValue());
            collectionMapper.insertCollectionPhase(phaseVo);
            sort += 1;
        }
    }
}
