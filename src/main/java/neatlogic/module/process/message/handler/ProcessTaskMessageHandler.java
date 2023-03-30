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
        return "modulegroup.itsm";
    }

    @Override
    public String getDescription() {
        return "handler.message.itsm.description";
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
