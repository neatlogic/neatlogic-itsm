/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.process.stephandler.regulatehandler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.exception.file.FileTypeHandlerNotFoundException;
import neatlogic.framework.file.core.FileTypeHandlerFactory;
import neatlogic.framework.file.core.IFileTypeHandler;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.IRegulateHandler;
import neatlogic.framework.process.stephandler.core.ProcessMessageManager;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/** 保留节点配置的附件类型处理器，空值由调用方沿用所属节点的原附件类型。 */
@Service
public class FileTypeHandlerRegulateHandler implements IRegulateHandler {

    /** 配置名称与节点 stepConfig 中的字段名称一致，供规整工厂注册。 */
    @Override
    public String getName() {
        return "fileTypeHandler";
    }

    /** 去除首尾空白并保留处理器名称大小写，未配置或清空时统一保存空字符串。 */
    @Override
    public void regulateConfig(IProcessStepInternalHandler processStepInternalHandler, JSONObject oldConfigObj, JSONObject newConfigObj) {
        String fileTypeHandler = oldConfigObj == null ? null : oldConfigObj.getString("fileTypeHandler");
        if (ProcessMessageManager.getOperationType() == OperationTypeEnum.UPDATE) {
            if (StringUtils.isNotBlank(fileTypeHandler)) {
                IFileTypeHandler handler = FileTypeHandlerFactory.getHandler(fileTypeHandler);
                if (handler == null) {
                    throw new FileTypeHandlerNotFoundException(fileTypeHandler);
                }
            }
        }
        newConfigObj.put("fileTypeHandler", fileTypeHandler == null ? "" : fileTypeHandler.trim());
    }
}
