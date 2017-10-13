package com.matcha.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

@RunWith(Parameterized.class)
public class TestPackageSplicing
{
    private String hostName;
    private int port;
    private String charSetName;

    private Charset charset;
    private InetSocketAddress socketAddress;

    public TestPackageSplicing(String hostName, int port, String charSetName)
    {
        this.hostName = hostName;
        this.port = port;
        this.charSetName = charSetName;
        this.charset = Charset.forName(charSetName);
        this.socketAddress = new InetSocketAddress(hostName, port);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data()
    {
        List<Object[]> params = new ArrayList<>();
        params.add(new Object[]{"localhost", 8080, "UTF-8"});
        return params;
    }

    @Test
    public void testOnePackage() throws Exception
    {
        /**
         * 关于Tomcat(正确的说是Tomcat的Http1.1的实现)来说：
         * 1、半包问题：
         *      如果是请求头没读完，那其就把之前读取的数据放在内存中不释放，再注册到Selector上，可以读了再读。
         *      如果是请求体没读完，那么会把Channel注册到NioBlockingSelector中的Selector(SharedSelector)上，然后
         *      阻塞在NioSocketWrapper上面的一个CountDownLatch上，如果超时了还读取不到数据那就抛出异常
         * 2、粘包问题：
         *      请求头中有一个Content-Length字段，其实是根据这个Content-Length来确定请求体的长度。如果不穿这个字段，那么
         *      Tomcat会默认这个请求没有请求体，如果这个时候恰好请求头和请求体分成两个包到了，有可能Tomcat会把发送过来的请求
         *      体当作一个新的请求来处理，那么此时就会遇到问题
         */

        try(SocketChannel socketChannel = SocketChannel.open())
        {
            socketChannel.connect(socketAddress);
            socketChannel.finishConnect();
            socketChannel.configureBlocking(false);
            sendPackage(
                    1,
                    byteBuffer -> {
                        try
                        {
                            socketChannel.write(byteBuffer);
                        }
                        catch (IOException e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
            );
        }
    }

    private void sendPackage(int packageCount,
                             Consumer<ByteBuffer> sender)
    {
        CharBuffer charBuffer = CharBuffer.allocate(1024);
        Optional<CharBuffer> charBufferOptional = Optional.ofNullable(charBuffer);
        IntStream.rangeClosed(1, packageCount)
                .mapToObj(num -> createPackage(charBufferOptional))
                .forEach(sender::accept);
    }

    private ByteBuffer createPackage(Optional<CharBuffer> charBufferOptional)
    {
        CharBuffer charBuffer = charBufferOptional.orElse(CharBuffer.allocate(1024));
        String[] packageContent = new String[]{
                "POST /LearnTomcat/upload HTTP/1.1\r\n",
                "cache-control: no-cache\r\n",
                "Postman-Token: ab8a7122-e4a2-4d8e-9524-c91a31df82d6\r\n",
                "Content-Type: text/plain\r\n",
                "User-Agent: PostmanRuntime/6.4.0\r\n",
                "Accept: */*\r\n",
                "Host: localhost:8080\r\n",
                "accept-encoding: gzip, deflate\r\n",
                "Connection: keep-alive\r\n",
                "\r\n",
                "ABCDEFGHIJ",
                "ABCDEFGHIJ",
                "ABCDEFGHIJ",
                "ABCDEFGHIJ",
                "ABC\r\n\r\n"
        };
        Arrays.stream(packageContent).forEach(charBuffer::append);
        charBuffer.flip();
        ByteBuffer byteBuffer = charset.encode(charBuffer);
        charBuffer.clear();
        return byteBuffer;
    }
}
