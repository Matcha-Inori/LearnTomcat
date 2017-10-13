package com.matcha.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@RunWith(Parameterized.class)
public class TestHalfPackage
{
    private String hostName;
    private int port;
    private String charSetName;

    private Charset charset;
    private InetSocketAddress socketAddress;

    public TestHalfPackage(String hostName, int port, String charSetName)
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
    public void test() throws Exception
    {
        try(SocketChannel socketChannel = SocketChannel.open())
        {
            socketChannel.connect(socketAddress);
            socketChannel.finishConnect();
            socketChannel.configureBlocking(false);
            sendPackage((ByteBuffer byteBuffer) -> socketChannel.write(byteBuffer), Function.identity());
        }
    }

    @Test
    public void testWithBIO() throws Exception
    {
        try(
                Socket socket = new Socket(hostName, port);
                OutputStream outputStream = socket.getOutputStream()
                //想测试缓冲区不能使用Buffered，使用了明显有一个缓冲区
                /*BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)*/
        )
        {
            sendPackage(
                    (byte[] bytes) -> outputStream.write(bytes),
                    byteBuffer -> {
                        int remaining = byteBuffer.remaining();
                        byte[] bytes = new byte[remaining];
                        byteBuffer.get(bytes);
                        return bytes;
                    });
        }
    }

    private <MESSAGE_TYPE> void sendPackage(Sender<MESSAGE_TYPE> sender,
                                            Function<ByteBuffer, MESSAGE_TYPE> changer)
    {
        List<String[]> packageContentList = createPackageContentList();
        CharBuffer charBuffer = CharBuffer.allocate(1024);
        packageContentList.stream()
                .map(packageContent -> {
                    Arrays.stream(packageContent).forEach(charBuffer::append);
                    charBuffer.flip();
                    ByteBuffer byteBuffer = charset.encode(charBuffer);
                    charBuffer.clear();
                    return byteBuffer;
                })
                .forEach(byteBuffer -> {
                    try
                    {
                        MESSAGE_TYPE message = changer.apply(byteBuffer);
                        sender.sendPackage(message);
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                });
    }

    private List<String[]> createPackageContentList()
    {
        return Arrays.asList(
                new String[]{
                        "POST /LearnTomcat/upload HTTP/1.1\r\n",
                        "cache-control: no-cache\r\n",
                        "Postman-Token: ab8a7122-e4a2-4d8e-9524-c91a31df82d6\r\n",
                        "Content-Type: text/plain\r\n",
                        "User-Agent: PostmanRuntime/6.4.0\r\n"
                },
                new String[]{
                        "Accept: */*\r\n",
                        "Host: localhost:8080\r\n",
                        "accept-encoding: gzip, deflate\r\n",
//                        "content-length: 47\r\n",
                        "Connection: keep-alive\r\n",
                        "\r\n"
                },
                new String[]{
                        "ABCDEFGHIJ",
                        "ABCDEFGHIJ"
                },
                new String[]{
                        "ABCDEFGHIJ",
                        "ABCDEFGHIJ",
                        "ABC\r\n\r\n"
                }
        );
    }
}

@FunctionalInterface
interface Sender<MESSAGE_TYPE>
{
    void sendPackage(MESSAGE_TYPE message) throws IOException;
}