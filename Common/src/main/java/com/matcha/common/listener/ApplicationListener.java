package com.matcha.common.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ApplicationListener implements ServletContextListener
{
    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        System.out.println("ApplicationListener -- contextInitialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
        System.out.println("ApplicationListener -- contextDestroyed");
    }
}
