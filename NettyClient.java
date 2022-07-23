import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GenericFutureListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.nio.charset.StandardCharsets;

public class NettyClient {
    static Logger logger = LogManager.getLogger(NettyClient.class.getName());
    static int SERVER_PORT = 3333;
    static int CLIENT_PORT = 1234;
    static String SERVER_HOST = "localhost";
    static String CLIENT_HOST = "localhost";
    static boolean enableSecurity = true;
    static ChannelHandlerContext activeChannelHandlerContext;

    static class NettyClientChannelProcessingInboundHandler extends NettyChannelInboundHandler {
        static ByteBuf sendMessage(byte[] message) throws Exception {
            ByteBuf byteBuf = activeChannelHandlerContext.alloc().buffer();
            byteBuf.writeInt(message.length);
            byteBuf.writeBytes(message);

            return byteBuf;
        }

        static class SendPacketListener implements GenericFutureListener<ChannelFuture> {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    sendPacket();
                } else {
                    channelFuture.cause().printStackTrace();
                    channelFuture.channel().close();
                }
            }
        }

        static void sendPacket() throws Exception {
            byte[] sendPacketBuf = "SEND_CLIENT_PACKET".getBytes(StandardCharsets.UTF_8);
            ByteBuf byteBuf = sendMessage(sendPacketBuf);
            ChannelFuture writeAndFlushFuture = activeChannelHandlerContext.writeAndFlush(byteBuf);
            writeAndFlushFuture.addListener(new SendPacketListener());
        }

        public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
            logger.debug("NettyClientChannelProcessingInboundHandler::channelActive");
            activeChannelHandlerContext = channelHandlerContext;
            sendPacket();
        }

        String deserializeMessage(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
            int length = byteBuf.readInt();
            byte[] messageBytes = new byte[length];
            byteBuf.readBytes(messageBytes);
            return new String(messageBytes);
        }

        public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
            logger.debug("NettyClientChannelInboundHandler::channelRead");
            String message = deserializeMessage(channelHandlerContext, (ByteBuf) object);
            logger.debug(message);
        }
    }

    static class NettyClientChannelFactory implements ChannelFactory<Channel> {
        @Override
        public Channel newChannel() {
            return new NioSocketChannel();
        }
    }

    static class NettyClientChannelInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            socketChannel.pipeline().addLast(new NettyMessageDecoder());
            if (enableSecurity) {
                socketChannel.pipeline().addLast(new NettyChannelDecryptionInboundHandler());
                socketChannel.pipeline().addLast(new NettyChannelEncryptionOutboundHandler());
            }
            socketChannel.pipeline().addLast(new NettyClientChannelProcessingInboundHandler());
        }
    }

    public static void main(String args[]) throws InterruptedException {
        logger.info("Starting high throughput netty client.");
        Bootstrap bootstrap = new Bootstrap();
//        bootstrap.localAddress(CLIENT_HOST, CLIENT_PORT);
        bootstrap.group(new NioEventLoopGroup());
        bootstrap.channelFactory(new NettyClientChannelFactory());
        bootstrap.handler(new NettyClientChannelInitializer());
        ChannelFuture connectFuture = bootstrap.connect(SERVER_HOST, SERVER_PORT).sync();
        connectFuture.channel().closeFuture().sync();
    }
}
