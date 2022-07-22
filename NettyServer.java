import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.concurrent.ExecutionException;

public class NettyServer {
    static Logger logger = LogManager.getLogger(NettyServer.class.getName());
    static int SERVER_PORT = 3333;
    static String SERVER_HOST = "localhost";
    static boolean enableSecurity = true;
    static long previousTime;
    static double previousMetric;

    static class NettyServerAcceptChannelHandler extends NettyChannelHandler {
        @Override
        public void handlerAdded(ChannelHandlerContext channelHandlerContext) throws Exception {
            logger.info("NettyServerAcceptChannelHandler::handlerAdded");
            channelHandlerContext.channel().pipeline().addLast(new NettyMessageDecoder());
            if (enableSecurity) {
                channelHandlerContext.channel().pipeline().addLast(new NettyChannelDecryptionInboundHandler());
                channelHandlerContext.channel().pipeline().addLast(new NettyChannelEncryptionOutboundHandler());
            }
            channelHandlerContext.channel().pipeline().addLast(new NettyServerChannelProcessingInboundHandler());
        }
    }

    static class NettyServerChannelProcessingInboundHandler extends NettyChannelInboundHandler {
        long channelRead;
        static int LIMIT = 1000;

        ByteBuf serializeMessage(ChannelHandlerContext channelHandlerContext, byte[] byteArray) throws Exception {
            ByteBuf byteBuf = channelHandlerContext.alloc().buffer();
            byteBuf.writeInt(byteArray.length);
            byteBuf.writeBytes(byteArray);

            return byteBuf;
        }

        public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
            logger.debug("NettyServerChannelProcessingInboundHandler::channelActive");
            byte[] sendPacketBuf = "SEND_SERVER_PACKET".getBytes(StandardCharsets.UTF_8);
            ByteBuf byteBuf = serializeMessage(channelHandlerContext, sendPacketBuf);
            ChannelFuture writeAndFlushFuture = channelHandlerContext.writeAndFlush(byteBuf);
            writeAndFlushFuture.get();
        }

        String deserializeMessage(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
            int length = byteBuf.readInt();
            byte[] messageBytes = new byte[length];
            byteBuf.readBytes(messageBytes);

            return new String(messageBytes);
        }

        // we could have a map of metrics to rate.
        void logMetrics(long currentMetric) {
            long currentTime = System.currentTimeMillis();
            // print and store metrics only every second.
            if (currentTime - previousTime > 1000) {
                double ratio = 1000/ ((double)currentTime - previousTime);
                double rate = (currentMetric - previousMetric) * ratio;
                previousTime = currentTime;
                previousMetric = currentMetric;
                logger.info("NettyServerChannelProcessingInboundHandler:: is receiving at rate of " + rate + " messages/s.");
            }
        }

        public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
            logger.debug("NettyServerChannelProcessingInboundHandler::channelRead");
            String message = deserializeMessage(channelHandlerContext, (ByteBuf) object);
            channelRead++;
            logMetrics(channelRead);
            logger.debug(message);
            ReferenceCountUtil.release(object);
        }
    }

    static class NettyServerChannelFactory implements ChannelFactory<ServerChannel> {
        @Override
        public ServerChannel newChannel() {
            return new NioServerSocketChannel();
        }
    }


    public static void main(String args[]) throws ExecutionException, InterruptedException {
        logger.info("Starting high throughput netty server.");
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.localAddress(SERVER_HOST, SERVER_PORT);
        serverBootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup());
        serverBootstrap.channelFactory(new NettyServerChannelFactory());
        serverBootstrap.childHandler(new NettyServerAcceptChannelHandler());
        ChannelFuture bindFuture = serverBootstrap.bind().sync();
        bindFuture.channel().closeFuture().sync();
    }
}
