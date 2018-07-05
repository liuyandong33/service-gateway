package build.dream.gateway.aspects;

import build.dream.common.annotations.ApiRestAction;
import build.dream.common.annotations.ModelAndViewAction;
import build.dream.common.api.ApiRest;
import build.dream.common.exceptions.ApiException;
import build.dream.common.models.BasicModel;
import build.dream.common.utils.ApplicationHandler;
import build.dream.common.utils.GsonUtils;
import build.dream.common.utils.LogUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class CallActionAspect {
    @Autowired
    private ApplicationContext applicationContext;
    private ConcurrentHashMap<Class<?>, Object> serviceMap = new ConcurrentHashMap<Class<?>, Object>();

    private Object obtainService(Class<?> serviceClass) {
        if (!serviceMap.contains(serviceClass)) {
            serviceMap.put(serviceClass, applicationContext.getBean(serviceClass));
        }
        return serviceMap.get(serviceClass);
    }

    @Around(value = "execution(public * build.dream.gateway.controllers.*.*(..)) && @annotation(apiRestAction)")
    public Object callApiRestAction(ProceedingJoinPoint proceedingJoinPoint, ApiRestAction apiRestAction) {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        Object returnValue = null;

        Throwable throwable = null;
        try {
            returnValue = callAction(proceedingJoinPoint, requestParameters, apiRestAction.modelClass(), apiRestAction.serviceClass(), apiRestAction.serviceMethodName());
        } catch (InvocationTargetException e) {
            throwable = e.getTargetException();
        } catch (Throwable t) {
            throwable = t;
        }

        if (throwable != null) {
            LogUtils.error(apiRestAction.error(), proceedingJoinPoint.getTarget().getClass().getName(), proceedingJoinPoint.getSignature().getName(), throwable, requestParameters);
            if (throwable instanceof ApiException) {
                returnValue = GsonUtils.toJson(new ApiRest(throwable));
            } else {
                returnValue = GsonUtils.toJson(new ApiRest(apiRestAction.error()));
            }
        }
        return returnValue;
    }

    @Around(value = "execution(public * build.dream.gateway.controllers.*.*(..)) && @annotation(modelAndViewAction)")
    public Object callModelAndViewAction(ProceedingJoinPoint proceedingJoinPoint, ModelAndViewAction modelAndViewAction) {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        Object returnValue = null;

        Throwable throwable = null;
        try {
            returnValue = callAction(proceedingJoinPoint, requestParameters, modelAndViewAction.modelClass(), modelAndViewAction.serviceClass(), modelAndViewAction.serviceMethodName());
        } catch (InvocationTargetException e) {
            throwable = e.getTargetException();
        } catch (Throwable t) {
            throwable = t;
        }

        ModelAndView modelAndView = new ModelAndView();
        if (throwable != null) {
            LogUtils.error(modelAndViewAction.error(), proceedingJoinPoint.getTarget().getClass().getName(), proceedingJoinPoint.getSignature().getName(), throwable, requestParameters);
        } else {
            modelAndView.setViewName(modelAndViewAction.viewName());
            if (returnValue instanceof Map) {
                modelAndView.addAllObjects((Map<String, ?>) returnValue);
            }
        }
        return modelAndView;
    }

    public Object callAction(ProceedingJoinPoint proceedingJoinPoint, Map<String, String> requestParameters, Class<? extends BasicModel> modelClass, Class<?> serviceClass, String serviceMethodName) throws Throwable {
        Object returnValue = null;
        if (modelClass != BasicModel.class && serviceClass != Object.class && StringUtils.isNotBlank(serviceMethodName)) {
            BasicModel model = ApplicationHandler.instantiateObject(modelClass, requestParameters);
            model.validateAndThrow();

            Method method = serviceClass.getDeclaredMethod(serviceMethodName, modelClass);
            method.setAccessible(true);

            returnValue = method.invoke(obtainService(serviceClass), model);
            if (!(returnValue instanceof String)) {
                returnValue = GsonUtils.toJson(returnValue);
            }
        } else {
            returnValue = proceedingJoinPoint.proceed();
        }
        return returnValue;
    }
}
