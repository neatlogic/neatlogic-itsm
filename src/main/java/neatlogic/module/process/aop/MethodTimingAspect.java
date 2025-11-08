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

package neatlogic.module.process.aop;

import com.alibaba.fastjson.JSON;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.util.UuidUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

//@Aspect
//@Component
public class MethodTimingAspect {

    private final static Logger logger = LoggerFactory.getLogger(MethodTimingAspect.class);

    @Around("execution(* neatlogic.module.process.api..*(..))")
    public Object logExecutionTime0(ProceedingJoinPoint joinPoint) throws Throwable {
        return log(joinPoint);
    }
    @Around("execution(* neatlogic.module.process.service..*(..))")
    public Object logExecutionTime1(ProceedingJoinPoint joinPoint) throws Throwable {
        return log(joinPoint);
    }
    @Around("execution(* neatlogic.module.process.dao.mapper..*(..))")
    public Object logExecutionTime2(ProceedingJoinPoint joinPoint) throws Throwable {
        return log(joinPoint);
    }
    @Around("execution(* neatlogic.module.process.stephandler..*(..))")
    public Object logExecutionTime3(ProceedingJoinPoint joinPoint) throws Throwable {
        return log(joinPoint);
    }
    @Around("execution(* neatlogic.framework.process.stephandler..*(..))")
    public Object logExecutionTime4(ProceedingJoinPoint joinPoint) throws Throwable {
        return log(joinPoint);
    }
    @Around("execution(* neatlogic.framework.process.operationauth..*(..))")
    public Object logExecutionTime5(ProceedingJoinPoint joinPoint) throws Throwable {
        return log(joinPoint);
    }
    @Around("execution(* neatlogic.module.process.operationauth..*(..))")
    public Object logExecutionTime6(ProceedingJoinPoint joinPoint) throws Throwable {
        return log(joinPoint);
    }

    private Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!Config.ENABLE_METHOD_TIMING_ASPECT()) {
            return joinPoint.proceed();
        }
        String uuid = UuidUtil.randomUuid();
        Instant start = Instant.now();
        Object[] args = joinPoint.getArgs();
        logger.error("           " + uuid + " " + joinPoint.getSignature().toShortString() + " 参数: " + JSON.toJSONString(Arrays.asList(args)));
        Object result = joinPoint.proceed(); // 继续执行目标方法
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        String format = (timeElapsed < 100 ? "  " : "* ") + "% 5d ms %s %s -> %s";
        logger.error(String.format(format, timeElapsed, uuid, joinPoint.getSignature().toShortString(), joinPoint.getSignature().toLongString()));
        return result;
    }
}
