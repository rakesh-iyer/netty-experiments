import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GenericFutureListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;


public class NettyClient {
    static Logger logger = LogManager.getLogger(NettyClient.class.getName());
    static int SERVER_PORT = 3333;
    static int CLIENT_PORT = 1234;
    static String SERVER_HOST = "localhost";
    static String CLIENT_HOST = "localhost";
    static boolean enableSecurity = true;
    static ChannelHandlerContext activeChannelHandlerContext;
    static NettyClientChannelProcessingInboundHandler.SendPacketListener sendPacketListener = new NettyClientChannelProcessingInboundHandler.SendPacketListener();
    static List<String> fileNamesList = Arrays.asList("small.txt", "medium.txt", "large.txt");
    static String READ_MESSAGE = "READ %s";

    static class NettyClientChannelProcessingInboundHandler extends NettyChannelInboundHandler {
        static class SendPacketListener implements GenericFutureListener<ChannelFuture> {
            // start with index = 1, as index = 0 has already been taken care of.
            int index = 1;
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    MessageSender.sendMessage(activeChannelHandlerContext,
                            String.format(READ_MESSAGE, fileNamesList.get(index)).getBytes(StandardCharsets.UTF_8),
                            sendPacketListener);
                } else {
                    channelFuture.cause().printStackTrace();
                    channelFuture.channel().close();
                }
                // rotate request amongst the files.
                index = (index + 1) % fileNamesList.size();
            }
        }

        public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
            logger.debug("NettyClientChannelProcessingInboundHandler::channelActive");
            activeChannelHandlerContext = channelHandlerContext;
            MessageSender.sendMessage(activeChannelHandlerContext,
                    String.format(READ_MESSAGE, fileNamesList.get(0)).getBytes(StandardCharsets.UTF_8),
                    sendPacketListener);
        }

        String deserializeMessage(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
            int length = byteBuf.readInt();
            byte[] messageBytes = new byte[length];
            byteBuf.readBytes(messageBytes);

            ReferenceCountUtil.release(byteBuf);
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
        bootstrap.group(new NioEventLoopGroup());
        bootstrap.channelFactory(new NettyClientChannelFactory());
        bootstrap.handler(new NettyClientChannelInitializer());
        ChannelFuture connectFuture = bootstrap.connect(SERVER_HOST, SERVER_PORT).sync();
        connectFuture.channel().closeFuture().sync();
    }
}
