package com.matcha.common.servlet;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;

public class SlashCountTwoServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException
    {
        System.out.println("SlashCountTwoServlet -- doGet");
        testParameter(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp) throws ServletException, IOException
    {
        System.out.println("SlashCountTwoServlet -- doPost");
        testParameter(req, resp);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        System.out.println("SlashCountTwoServlet -- service");
        super.service(req, resp);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException
    {
        super.service(req, res);
    }

    private void testParameter(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String printStr = req.getParameter("print");
        boolean print = Boolean.parseBoolean(printStr);
        if(!print) return;
        Charset charset = Charset.forName("UTF-8");
        String name = req.getParameter("name");
        String ageStr = req.getParameter("age");
        StringBuffer outputBuffer = new StringBuffer();
        outputBuffer.append(name).append(", ").append(ageStr);
        String outputStr = outputBuffer.toString();
        byte[] outputBytes = outputStr.getBytes(charset);
        ByteBuffer buffer = ByteBuffer.allocate(outputBytes.length);
        try(
                OutputStream outputStream = resp.getOutputStream();
                WritableByteChannel writableByteChannel = Channels.newChannel(outputStream);
        )
        {
            buffer.put(outputBytes);
            buffer.flip();
            writableByteChannel.write(buffer);
        }
    }
}
