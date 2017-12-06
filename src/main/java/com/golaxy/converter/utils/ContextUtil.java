package com.golaxy.converter.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * 获取容器bean工具类
 */
public class ContextUtil implements ApplicationContextAware{

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        context = applicationContext;
    }

    public static ApplicationContext getContext(){
        return context;
    }

    public static Object getBean(String beanName) {
        return context.getBean(beanName);
    }

}