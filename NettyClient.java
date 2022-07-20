import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class NettyClient {
    static int SERVER_PORT = 3333;
    static int CLIENT_PORT = 1234;
    static String SERVER_HOST = "localhost";
    static String CLIENT_HOST = "localhost";

    static class NettyClientChannelProcessingInboundHandler extends NettyChannelInboundHandler {
        ByteBuf sendMessage(ChannelHandlerContext channelHandlerContext, byte[] byteArray) throws Exception {
            ByteBuf byteBuf = channelHandlerContext.alloc().buffer();
            byteBuf.writeInt(byteArray.length);
            byteBuf.writeBytes(byteArray);

            return byteBuf;
        }

        public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
            System.out.println("NettyClientChannelProcessingInboundHandler::channelActive");
            byte[] sendPacketBuf = "SEND_CLIENT_PACKET".getBytes(StandardCharsets.UTF_8);
            ByteBuf byteBuf = sendMessage(channelHandlerContext, sendPacketBuf);
            ChannelFuture writeAndFlushFuture = channelHandlerContext.writeAndFlush(byteBuf);
            writeAndFlushFuture.get();
            channelHandlerContext.fireChannelActive();
        }

        String deserializeMessage(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
            int length = byteBuf.readInt();
            byte[] messageBytes = new byte[length];
            byteBuf.readBytes(messageBytes);
            return new String(messageBytes);
        }

        public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
            System.out.println("NettyClientChannelInboundHandler::channelRead");
            String message = deserializeMessage(channelHandlerContext, (ByteBuf) object);
            System.out.println(message);
            channelHandlerContext.fireChannelRead(object);
        }
    }

    static class NettyClientChannelFactory implements ChannelFactory<Channel> {
        @Override
        public Channel newChannel() {
            return new NioSocketChannel();
        }
    }

    public static void main(String args[]) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.localAddress(CLIENT_HOST, CLIENT_PORT);
        bootstrap.group(new NioEventLoopGroup());
        bootstrap.channelFactory(new NettyClientChannelFactory());
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new NettyChannelDecryptionInboundHandler());
                socketChannel.pipeline().addLast(new NettyChannelEncryptionOutboundHandler());
                socketChannel.pipeline().addLast(new NettyClientChannelProcessingInboundHandler());
            }
        });
        ChannelFuture connectFuture = bootstrap.connect(SERVER_HOST, SERVER_PORT).sync();
        connectFuture.channel().closeFuture().sync();
    }
}
