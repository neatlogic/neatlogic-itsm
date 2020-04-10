package codedriver.module.process.matrixexternalrequest.handler;

import codedriver.framework.process.constvalue.AuthType;
import codedriver.framework.process.constvalue.EncodingType;
import codedriver.framework.process.constvalue.RestfulType;
import codedriver.framework.process.matrixrexternal.core.MatrixExternalRequestBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-04-03 14:40
 **/
@Component
public class UWinHandler extends MatrixExternalRequestBase {

    @Override
    public String getName() {
        return "优维插件";
    }

    @Override
    public List<EncodingType> encodingType() {
        List<EncodingType> types = new ArrayList<>();
        types.add(EncodingType.UTF_8);
        types.add(EncodingType.GBK);
        return types;
    }

    @Override
    public List<RestfulType> restfulType() {
        List<RestfulType> restfulTypes = new ArrayList<>();
        restfulTypes.add(RestfulType.POST);
        restfulTypes.add(RestfulType.GET);
        return restfulTypes;
    }

    @Override
    public List<AuthType> authType() {
        List<AuthType> authTypes = new ArrayList<>();
        authTypes.add(AuthType.BASIC);
        return authTypes;
    }

    @Override
    public String myHandler(String url, String authType, String restfulType, String encodingType, JSONObject config) {
        return null;
    }
}
