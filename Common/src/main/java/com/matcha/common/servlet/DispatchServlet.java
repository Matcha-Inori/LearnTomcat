package com.matcha.common.servlet;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DispatchServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException
    {
        System.out.println("DispatchServlet -- doGet");
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp) throws ServletException, IOException
    {
        System.out.println("DispatchServlet -- doPost");
    }

    @Override
    protected void service(HttpServletRequest req,
                           HttpServletResponse resp) throws ServletException, IOException
    {
        System.out.println("DispatchServlet -- service");
        super.service(req, resp);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException
    {
        super.service(req, res);
    }
}
