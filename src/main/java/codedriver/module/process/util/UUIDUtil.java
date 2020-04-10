package codedriver.module.process.util;

import java.util.UUID;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-04-07 14:43
 **/
public class UUIDUtil {
    public static String getUUID(){
        return UUID.randomUUID().toString().replace("-", "");
    }
}
