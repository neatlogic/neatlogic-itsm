package codedriver.module.process.notify.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-12 10:20
 **/
public class EmailNoServerException extends ApiRuntimeException {
    public EmailNoServerException(String msg){
        super(msg);
    }
}
