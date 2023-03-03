package neatlogic.module.process.message.handler;

import neatlogic.framework.message.core.MessageHandlerBase;
import neatlogic.framework.notify.dto.NotifyVo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Title: ProcessTaskMessageHandler
 * @Package neatlogic.framework.process.message.handler
 * @Description: 工单消息处理器
 * @Author: linbq
 * @Date: 2020/12/31 14:28
 **/
@Service
public class ProcessTaskMessageHandler extends MessageHandlerBase {

    @Override
    public String getName() {
        return "IT服务";
    }

    @Override
    public String getDescription() {
        return "实时显示待处理工单信息，支持快速审批";
    }

    @Override
    public boolean getNeedCompression() {
        return false;
    }

    @Override
    public NotifyVo compress(List<NotifyVo> notifyVoList) {
        return null;
    }
}
