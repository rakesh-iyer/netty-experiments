import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

public class NettyServer {
    static Logger logger = LogManager.getLogger(NettyServer.class.getName());
    static int SERVER_PORT = 3333;
    static String SERVER_HOST = "localhost";
    static boolean enableSecurity = true;
    static String READ_MESSAGE = "READ";
    static AtomicLong savedPreviousTime = new AtomicLong();
    static AtomicLong savedPreviousMetric = new AtomicLong();
    static NettyChannelDecryptionInboundHandler nettyChannelDecryptionInboundHandler = new NettyChannelDecryptionInboundHandler();
    static NettyChannelEncryptionOutboundHandler nettyChannelEncryptionOutboundHandler = new NettyChannelEncryptionOutboundHandler();
    static NettyServerChannelProcessingInboundHandler nettyServerChannelProcessingInboundHandler = new NettyServerChannelProcessingInboundHandler();
    static NettyServerChannelProcessingInboundHandler.SendPacketListener sendPacketListener = new NettyServerChannelProcessingInboundHandler.SendPacketListener();

    static class NettyServerAcceptChannelHandler extends NettyChannelHandler {
        @Override
        public void handlerAdded(ChannelHandlerContext channelHandlerContext) throws Exception {
            logger.info("NettyServerAcceptChannelHandler::handlerAdded");
            channelHandlerContext.channel().pipeline().addLast(new NettyMessageDecoder());
            if (enableSecurity) {
                channelHandlerContext.channel().pipeline().addLast(nettyChannelDecryptionInboundHandler);
                channelHandlerContext.channel().pipeline().addLast(nettyChannelEncryptionOutboundHandler);
            }
            channelHandlerContext.channel().pipeline().addLast(nettyServerChannelProcessingInboundHandler);
        }
    }

    static class NettyServerChannelProcessingInboundHandler extends NettyChannelInboundHandler {
        static AtomicLong channelRead = new AtomicLong();
        static byte [] fileData = new byte[28000];

        static class NettyFileReadMessage {
            String messageType;
            String fileName;

            NettyFileReadMessage(String m, String f) {
                messageType = m;
                fileName = f;
            }
        }

        static class SendPacketListener implements GenericFutureListener<ChannelFuture> {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    logger.debug("SendPacketListener:: operation is successful.");
                } else {
                    channelFuture.cause().printStackTrace();
                    channelFuture.channel().close();
                }
            }
        }

        // Allocate
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

        // Allocate

        // we could have a map of metrics to rate.
        void logMetrics(long currentMetric) {
            long currentTime = System.currentTimeMillis();
            long previousTime = savedPreviousTime.get();
            // print and store metrics only every second.
            if (currentTime - previousTime > 1000) {
                double ratio = 1000/ ((double)currentTime - previousTime);
                double rate = (currentMetric - savedPreviousMetric.get()) * ratio;
                savedPreviousTime.set(currentTime);
                savedPreviousMetric.set(currentMetric);
                logger.info("NettyServerChannelProcessingInboundHandler:: is receiving at rate of " + rate + " messages/s.");
            }
        }

        boolean isReadRequest(String messageType) {
            return messageType.equals(READ_MESSAGE);
        }

        NettyFileReadMessage getNettyFileReadMessage(byte[] message) {
            String [] messageComponents = new String(message).split(" ");
            if (messageComponents.length != 2) {
                logger.info("Incorrect number of message components for " + message + ":" + messageComponents.length);
                return null;
            }

            // expect a message of the type READ filename, i.e. READ a.txt
            if (isReadRequest(messageComponents[0]) && FileUtils.isValidFileName(messageComponents[1])) {
                return new NettyFileReadMessage(messageComponents[0], messageComponents[1]);
            }  else if (!isReadRequest(messageComponents[0])) {
                logger.info("This is not a read request");
            } else {
                logger.info(messageComponents[1] + " does not exist.");
            }

            return null;
        }

        // Free
        public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
            logger.debug("NettyServerChannelProcessingInboundHandler::channelRead");
            byte[] message = MessageUtils.deserializeMessage(channelHandlerContext, (ByteBuf) object);
            ReferenceCountUtil.release(object);

            logMetrics(channelRead.incrementAndGet());
            NettyFileReadMessage nettyFileReadMessage = getNettyFileReadMessage(message);
            // expect a message of the type READ filename, i.e. READ a.txt
            if (nettyFileReadMessage != null) {
                ByteBuffer fileData = FileUtils.readFile(nettyFileReadMessage.fileName);
                // send the data back to the client
                MessageUtils.sendMessage(channelHandlerContext, fileData, sendPacketListener);
            }
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
