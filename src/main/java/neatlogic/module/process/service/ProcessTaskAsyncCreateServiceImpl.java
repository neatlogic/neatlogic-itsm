/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.process.service;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.queue.NeatLogicBlockingQueue;
import neatlogic.framework.asynchronization.thread.NeatLogicThread;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.dao.mapper.TenantMapper;
import neatlogic.framework.dto.TenantVo;
import neatlogic.framework.process.crossover.IProcessTaskAsyncCreateCrossoverService;
import neatlogic.framework.process.crossover.IProcessTaskCreatePublicCrossoverService;
import neatlogic.framework.process.dto.ProcessTaskAsyncCreateVo;
import neatlogic.framework.process.dto.ProcessTaskCreateVo;
import neatlogic.framework.util.SnowflakeUtil;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskAsyncCreateMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

@Service
public class ProcessTaskAsyncCreateServiceImpl implements ProcessTaskAsyncCreateService, IProcessTaskAsyncCreateCrossoverService {
    private static final Logger logger = LoggerFactory.getLogger(ProcessTaskAsyncCreateServiceImpl.class);

    private final static NeatLogicBlockingQueue<Long> blockingQueue = new NeatLogicBlockingQueue<>(new LinkedBlockingQueue<>());

    @Resource
    private ProcessTaskAsyncCreateMapper processTaskAsyncCreateMapper;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private TenantMapper tenantMapper;

    @PostConstruct
    public void init() {
        // 启动服务器时加载数据库中`processtask_async_create`表status为doing，server_id为Config.SCHEDULE_SERVER_ID的数据到blockingQueue中
        TenantContext.get().setUseDefaultDatasource(true);
        List<TenantVo> tenantList = tenantMapper.getAllActiveTenant();
        for (TenantVo tenantVo : tenantList) {
            TenantContext.get().switchTenant(tenantVo.getUuid());
            List<Long> doneIdList = new ArrayList<>();
            List<Long> doingIdList = new ArrayList<>();
            ProcessTaskAsyncCreateVo searchVo = new ProcessTaskAsyncCreateVo();
            searchVo.setStatus("doing");
            searchVo.setServerId(Config.SCHEDULE_SERVER_ID);
            int rowNum = processTaskAsyncCreateMapper.getProcessTaskAsyncCreateCount(searchVo);
            if (rowNum > 0) {
                searchVo.setRowNum(rowNum);
                searchVo.setPageSize(100);
                Integer pageCount = searchVo.getPageCount();
                for (int currentPage = 1; currentPage <= pageCount; currentPage++) {
                    searchVo.setCurrentPage(currentPage);
                    List<ProcessTaskAsyncCreateVo> list = processTaskAsyncCreateMapper.getProcessTaskAsyncCreateList(searchVo);
                    List<Long> processTaskIdList = list.stream().map(ProcessTaskAsyncCreateVo::getProcessTaskId).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(processTaskIdList)) {
                        processTaskIdList = processTaskMapper.checkProcessTaskIdListIsExists(processTaskIdList);
                    }
                    for (ProcessTaskAsyncCreateVo processTaskAsyncCreateVo : list) {
                        if (processTaskIdList.contains(processTaskAsyncCreateVo.getProcessTaskId())) {
                            doneIdList.add(processTaskAsyncCreateVo.getId());
                        } else {
                            doingIdList.add(processTaskAsyncCreateVo.getId());
                        }
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(doneIdList)) {
                processTaskAsyncCreateMapper.deleteProcessTaskAsyncCreateByIdList(doneIdList);
            }
            doingIdList.sort(Long::compareTo);
            for (Long id : doingIdList) {
                boolean offer = blockingQueue.offer(id);
                if (!offer && logger.isDebugEnabled()) {
                    logger.debug("异步创建工单数据加入队列失败, id: " + id);
                }
            }
        }
        TenantContext.get().setUseDefaultDatasource(true);

        Thread t = new Thread(new NeatLogicThread("ASYNC-CREATE-PROCESSTASK-MANAGER") {
            @Override
            protected void execute() {
                IProcessTaskCreatePublicCrossoverService processTaskCreatePublicCrossoverService = CrossoverServiceFactory.getApi(IProcessTaskCreatePublicCrossoverService.class);
                while (!Thread.currentThread().isInterrupted()) {
                    Long id = null;
                    try {
                        id = blockingQueue.take();
                        ProcessTaskAsyncCreateVo processTaskAsyncCreate = processTaskAsyncCreateMapper.getProcessTaskAsyncCreateById(id);
                        if (processTaskAsyncCreate != null) {
                            processTaskCreatePublicCrossoverService.createProcessTask(processTaskAsyncCreate.getConfig());
                        }
                        processTaskAsyncCreateMapper.deleteProcessTaskAsyncCreateById(id);
                    } catch (InterruptedException e) {
                        if (id != null) {
                            ProcessTaskAsyncCreateVo processTaskAsyncCreateVo = new ProcessTaskAsyncCreateVo();
                            processTaskAsyncCreateVo.setId(id);
                            processTaskAsyncCreateVo.setStatus("failed");
                            processTaskAsyncCreateVo.setError(ExceptionUtils.getStackTrace(e));
                            processTaskAsyncCreateMapper.updateProcessTaskAsyncCreate(processTaskAsyncCreateVo);
                        }
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        if (id != null) {
                            ProcessTaskAsyncCreateVo processTaskAsyncCreateVo = new ProcessTaskAsyncCreateVo();
                            processTaskAsyncCreateVo.setId(id);
                            processTaskAsyncCreateVo.setStatus("failed");
                            processTaskAsyncCreateVo.setError(ExceptionUtils.getStackTrace(e));
                            processTaskAsyncCreateMapper.updateProcessTaskAsyncCreate(processTaskAsyncCreateVo);
                        }
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @Override
    public Long addNewProcessTaskAsyncCreate(ProcessTaskCreateVo processTaskCreateVo) {
        if (processTaskCreateVo == null) {
            return null;
        }
        Long processTaskId = processTaskCreateVo.getNewProcessTaskId();
        if (processTaskId != null) {
            if (processTaskMapper.getProcessTaskById(processTaskId) != null) {
                processTaskId = null;
            }
        }
        if (processTaskId == null) {
            processTaskId = SnowflakeUtil.uniqueLong();
            processTaskCreateVo.setNewProcessTaskId(processTaskId);
        }
        ProcessTaskAsyncCreateVo processTaskAsyncCreateVo = new ProcessTaskAsyncCreateVo();
        processTaskAsyncCreateVo.setProcessTaskId(processTaskId);
        processTaskAsyncCreateVo.setTitle(processTaskCreateVo.getTitle());
        processTaskAsyncCreateVo.setStatus("doing");
        processTaskAsyncCreateVo.setFcu(UserContext.get().getUserUuid());
        processTaskAsyncCreateVo.setServerId(Config.SCHEDULE_SERVER_ID);
        processTaskAsyncCreateVo.setConfig(processTaskCreateVo);
        processTaskAsyncCreateMapper.insertProcessTaskAsyncCreate(processTaskAsyncCreateVo);
        boolean offer = blockingQueue.offer(processTaskAsyncCreateVo.getId());
        if (!offer && logger.isDebugEnabled()) {
            logger.debug("异步创建工单数据加入队列失败, processTaskAsyncCreateVo: " + JSONObject.toJSONString(processTaskAsyncCreateVo));
        }
        return processTaskId;
    }

    @Override
    public void addRedoProcessTaskAsyncCreate(Long id) {
        if (id == null) {
            return;
        }
        processTaskAsyncCreateMapper.updateProcessTaskAsyncCreateStatusToDoingById(id);
        boolean offer = blockingQueue.offer(id);
        if (!offer && logger.isDebugEnabled()) {
            logger.debug("异步创建工单数据加入队列失败, id: " + id);
        }
    }
}
