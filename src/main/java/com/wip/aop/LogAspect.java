package com.wip.aop;

import com.wip.model.LogDomain;
import com.wip.model.UserDomain;
import com.wip.service.log.LogService;
import com.wip.utils.GsonUtils;
import com.wip.utils.HttpContextUtils;
import com.wip.utils.TaleUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;


/**
 * @author yingxiu.zty
 * @createTime on 2023/10/23
 */
@Aspect
@Component
public class LogAspect {
    @Resource
    private LogService logService;
    //定义切点 切点表达式指向SysLog注解，我们再业务方法上可以加上SysLog注解，然后所标注
    //的方法都能进行日志记录
    @Pointcut("@annotation(com.wip.aop.BlogLog)")
    public void logPointCut() {

    }
    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        LogDomain logDomain=new LogDomain();
        //注解value
        BlogLog blogLog=method.getAnnotation(BlogLog.class);
        if(blogLog!=null){
            logDomain.setAction(blogLog.value());
        }

        long beginTime = System.currentTimeMillis();
        //执行方法
        Object result = point.proceed();
        //执行时长(毫秒)
        long time = System.currentTimeMillis() - beginTime;
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        UserDomain user= TaleUtils.getLoginUser(request);
        logDomain.setIp(request.getRemoteAddr());
        logDomain.setData(GsonUtils.toJsonString(user));
        logDomain.setCreated(new Date());
        logDomain.setCost(time);
        logDomain.setAuthorId(user.getUid());
        //请求的方法名
        String className = point.getTarget().getClass().getName();
        String methodName = signature.getName();
        logDomain.setMethod(className + "：" + methodName + "()");
        //保存日志
        logService.saveLog(logDomain);

        return result;
    }
}
