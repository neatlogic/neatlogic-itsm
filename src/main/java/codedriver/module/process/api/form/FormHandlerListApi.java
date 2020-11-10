package codedriver.module.process.api.form;

import java.util.Set;

import org.reflections.Reflections;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.IProcessFormHandlerType;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class FormHandlerListApi extends PrivateApiComponentBase {
    /** 标记是否未初始化数据，只初始化一次 **/
    private static volatile boolean isUninitialized = true;
    private static JSONArray handlerList = new JSONArray();

    @Override
    public String getToken() {
        return "process/form/handler/list";
    }

    @Override
    public String getName() {
        return "获取表单组件列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({@Param(name = "handler", type = ApiParamType.STRING, desc = "处理器"),
        @Param(name = "name", type = ApiParamType.STRING, desc = "处理器中文名"),
        @Param(name = "icon", type = ApiParamType.STRING, desc = "图标"),
        @Param(name = "type", type = ApiParamType.ENUM, desc = "分类，form（表单组件）|control（控制组件）")})
    @Description(desc = "获取表单组件列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        return handlerList;
    }

    static {
        if (isUninitialized) {
            synchronized (IProcessFormHandlerType.class) {
                if (isUninitialized) {
                    Reflections reflections = new Reflections("codedriver");
                    Set<Class<? extends IProcessFormHandlerType>> classSet =
                        reflections.getSubTypesOf(IProcessFormHandlerType.class);
                    for (Class<? extends IProcessFormHandlerType> c : classSet) {
                        try {
                            for (IProcessFormHandlerType type : c.getEnumConstants()) {
                                JSONObject jsonObj = new JSONObject();
                                jsonObj.put("handler", type.getHandler());
                                jsonObj.put("name", type.getHandlerName());
                                jsonObj.put("icon", type.getIcon());
                                jsonObj.put("type", type.getType());
                                handlerList.add(jsonObj);
                            }
                        } catch (Exception e) {

                        }
                    }
                    isUninitialized = false;
                }
            }
        }
    }

}
