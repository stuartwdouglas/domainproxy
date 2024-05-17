package io.github.stuartwdouglas.domainproxy;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class DomainProxyTest {

    @Test
    public void test()  throws Exception{
        try (ServerSocket socket = new ServerSocket(8080)) {
            while (true) {
                Socket s = socket.accept();
                UnixDomainSocketAddress address = UnixDomainSocketAddress.of("/tmp/domainserver");
                SocketChannel channel = SocketChannel.open(address);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int r;
                        byte[] buf = new byte[1024];
                        try {
                            while ((r = s.getInputStream().read(buf)) > 0) {
                                System.out.println(new String(buf, 0, r));
                                channel.write(ByteBuffer.wrap(buf, 0, r));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {

                            try {
                                channel.close();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            try {
                                s.close();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }).start();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ByteBuffer buf = ByteBuffer.allocate(1024);
                        buf.clear();
                        try {
                            while (channel.read(buf) > 0) {
                                buf.flip();
                                System.out.println(new String(buf.array(), buf.arrayOffset(), buf.remaining()));
                                s.getOutputStream().write(buf.array(), buf.arrayOffset(), buf.remaining());
                                buf.clear();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {

                            try {
                                channel.close();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            try {
                                s.close();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
        }

    }
}
