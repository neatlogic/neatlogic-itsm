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
import neatlogic.framework.util.SnowflakeUtil;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskAsyncCreateMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

@Service
public class ProcessTaskAsyncCreateServiceImpl implements ProcessTaskAsyncCreateService, IProcessTaskAsyncCreateCrossoverService {
    private static final Logger logger = LoggerFactory.getLogger(ProcessTaskAsyncCreateServiceImpl.class);

    private final static BlockingQueue<ProcessTaskAsyncCreateVo> blockingQueue = new LinkedBlockingQueue<>();

    @Resource
    private ProcessTaskAsyncCreateMapper processTaskAsyncCreateMapper;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private TenantMapper tenantMapper;

    @PostConstruct
    public void init() throws InterruptedException {
        // 启动服务器时加载数据库中`processtask_async_create`表status为doing，server_id为Config.SCHEDULE_SERVER_ID的数据到blockingQueue中
        TenantContext.get().setUseDefaultDatasource(true);
        List<TenantVo> tenantList = tenantMapper.getAllActiveTenant();
        for (TenantVo tenantVo : tenantList) {
            TenantContext.get().switchTenant(tenantVo.getUuid());
            ProcessTaskAsyncCreateVo searchVo = new ProcessTaskAsyncCreateVo();
            searchVo.setStatus("doing");
            searchVo.setServerId(Config.SCHEDULE_SERVER_ID);
            int rowNum = processTaskAsyncCreateMapper.getProcessTaskAsyncCreateCount(searchVo);
            if (rowNum > 0) {
                searchVo.setRowNum(rowNum);
                searchVo.setPageSize(100);
                Integer pageCount = searchVo.getPageCount();
                List<ProcessTaskAsyncCreateVo> doneList = new ArrayList<>();
                for (int currentPage = 1; currentPage <= pageCount; currentPage++) {
                    searchVo.setCurrentPage(currentPage);
                    List<ProcessTaskAsyncCreateVo> list = processTaskAsyncCreateMapper.getProcessTaskAsyncCreateList(searchVo);
                    List<Long> processTaskIdList = list.stream().map(ProcessTaskAsyncCreateVo::getProcessTaskId).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(processTaskIdList)) {
                        processTaskIdList = processTaskMapper.checkProcessTaskIdListIsExists(processTaskIdList);
                    }
                    for (ProcessTaskAsyncCreateVo processTaskAsyncCreateVo : list) {
                        if (processTaskIdList.contains(processTaskAsyncCreateVo.getProcessTaskId())) {
                            processTaskAsyncCreateVo.setStatus("done");
                            doneList.add(processTaskAsyncCreateVo);
                        } else {
                            processTaskAsyncCreateVo.setTenantUuid(tenantVo.getUuid());
                            blockingQueue.put(processTaskAsyncCreateVo);
                        }
                    }
                }
                for (ProcessTaskAsyncCreateVo processTaskAsyncCreateVo : doneList) {
                    processTaskAsyncCreateMapper.updateProcessTaskAsyncCreate(processTaskAsyncCreateVo);
                }
            }
        }
        TenantContext.get().setUseDefaultDatasource(true);

        Thread t = new Thread(new NeatLogicThread("ASYNC-CREATE-PROCESSTASK-MANAGER") {
            @Override
            protected void execute() {
                IProcessTaskCreatePublicCrossoverService processTaskCreatePublicCrossoverService = CrossoverServiceFactory.getApi(IProcessTaskCreatePublicCrossoverService.class);
                while (!Thread.currentThread().isInterrupted()) {
                    ProcessTaskAsyncCreateVo processTaskAsyncCreateVo = null;
                    try {
                        processTaskAsyncCreateVo = blockingQueue.take();
                        TenantContext.get().switchTenant(processTaskAsyncCreateVo.getTenantUuid());
                        processTaskCreatePublicCrossoverService.createProcessTask(processTaskAsyncCreateVo.getConfig());
                        processTaskAsyncCreateVo.setStatus("done");
                    } catch (InterruptedException e) {
                        if (processTaskAsyncCreateVo != null) {
                            processTaskAsyncCreateVo.setStatus("failed");
                            processTaskAsyncCreateVo.setError(ExceptionUtils.getStackTrace(e));
                        }
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        if (processTaskAsyncCreateVo != null) {
                            processTaskAsyncCreateVo.setStatus("failed");
                            processTaskAsyncCreateVo.setError(ExceptionUtils.getStackTrace(e));
                        }
                        logger.error(e.getMessage(), e);
                    } finally {
                        if (processTaskAsyncCreateVo != null) {
                            processTaskAsyncCreateMapper.updateProcessTaskAsyncCreate(processTaskAsyncCreateVo);
                        }
                    }
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @Override
    public Long addNewProcessTaskAsyncCreate(ProcessTaskAsyncCreateVo processTaskAsyncCreateVo) throws InterruptedException {
        JSONObject config = processTaskAsyncCreateVo.getConfig();
        if (MapUtils.isEmpty(config)) {
            return null;
        }
        Long processTaskId = config.getLong("newProcessTaskId");
        if (processTaskId != null) {
            if (processTaskMapper.getProcessTaskById(processTaskId) != null) {
                processTaskId = null;
            }
        }
        if (processTaskId == null) {
            processTaskId = SnowflakeUtil.uniqueLong();
            config.put("newProcessTaskId", processTaskId);
        }
        processTaskAsyncCreateVo.setProcessTaskId(processTaskId);
        processTaskAsyncCreateVo.setTenantUuid(TenantContext.get().getTenantUuid());
        processTaskAsyncCreateVo.setTitle(config.getString("title"));
        processTaskAsyncCreateVo.setStatus("doing");
        processTaskAsyncCreateVo.setFcu(UserContext.get().getUserUuid());
        processTaskAsyncCreateVo.setServerId(Config.SCHEDULE_SERVER_ID);
        processTaskAsyncCreateMapper.insertProcessTaskAsyncCreate(processTaskAsyncCreateVo);
        blockingQueue.put(processTaskAsyncCreateVo);
        return processTaskId;
    }

    @Override
    public Long addRedoProcessTaskAsyncCreate(ProcessTaskAsyncCreateVo processTaskAsyncCreateVo) throws InterruptedException {
        processTaskAsyncCreateVo.setTenantUuid(TenantContext.get().getTenantUuid());
        blockingQueue.put(processTaskAsyncCreateVo);
        return processTaskAsyncCreateVo.getProcessTaskId();
    }
}
