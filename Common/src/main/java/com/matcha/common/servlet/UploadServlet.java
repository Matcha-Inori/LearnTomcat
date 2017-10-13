package com.matcha.common.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UploadServlet extends HttpServlet
{
    private SimpleDateFormat simpleDateFormat;

    public UploadServlet()
    {
        simpleDateFormat = new SimpleDateFormat("hh-mm-ss");
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp) throws ServletException, IOException
    {
        ServletContext servletContext = req.getServletContext();
        String path = servletContext.getRealPath("/upload");
        Path uploadPath = Paths.get(path);
        if(!Files.exists(uploadPath))
            Files.createDirectories(uploadPath);
        String fileName = simpleDateFormat.format(new Date());
        Path fileNamePath = Paths.get(fileName);
        Path filePath = uploadPath.resolve(fileNamePath);
        filePath = Files.createFile(filePath);

        try(
                InputStream inputStream = req.getInputStream();
                ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
                SeekableByteChannel seekableByteChannel = Files.newByteChannel(
                        filePath,
                        StandardOpenOption.WRITE
                )
        )
        {
            ByteBuffer byteBuffer = ByteBuffer.allocate(2048);
            while(readableByteChannel.read(byteBuffer) >= 0)
            {
                byteBuffer.flip();
                seekableByteChannel.write(byteBuffer);
                byteBuffer.clear();
            }
        }
    }
}
