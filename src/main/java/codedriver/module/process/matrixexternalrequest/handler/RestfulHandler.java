package codedriver.module.process.matrixexternalrequest.handler;

import codedriver.framework.process.constvalue.AuthType;
import codedriver.framework.process.constvalue.EncodingType;
import codedriver.framework.process.constvalue.RestfulType;
import codedriver.framework.process.matrixrexternal.core.MatrixExternalRequestBase;
import codedriver.module.process.util.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-04-03 14:32
 **/
@Component
public class RestfulHandler extends MatrixExternalRequestBase {
    @Override
    public String getName() {
        return "Restful";
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
        restfulTypes.add(RestfulType.PUT);
        return restfulTypes;
    }

    @Override
    public List<AuthType> authType() {
        List<AuthType> authTypes = new ArrayList<>();
        authTypes.add(AuthType.BASIC);
        return authTypes;
    }

    @Override
    public String myHandler(String url, String authType, String restfulType, String encodingType ,JSONObject config){
        String accessKey = "";
        String accessPassword = "";
        if (StringUtils.isNotBlank(authType)){
            if (AuthType.BASIC.getValue().equals(authType)){
                accessKey = config.getString("accessKey");
                accessPassword = config.getString("accessPassword");
            }
        }
       return HttpUtil.getHttpConnectionData(url, authType, accessKey, accessPassword, restfulType, encodingType);
    }
}
