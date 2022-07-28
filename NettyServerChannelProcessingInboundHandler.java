import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GenericFutureListener;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

class NettyServerChannelProcessingInboundHandler extends NettyChannelInboundHandler {
    static AtomicLong channelRead = new AtomicLong();
    static String READ_MESSAGE = "READ";
    static AtomicLong savedPreviousTime = new AtomicLong();
    static AtomicLong savedPreviousMetric = new AtomicLong();
    static SendPacketListener sendPacketListener = new SendPacketListener();

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
        byte[] sendPacketBuf = "SERVER_HELLO_PACKET".getBytes(StandardCharsets.UTF_8);
        ByteBuf byteBuf = serializeMessage(channelHandlerContext, sendPacketBuf);
        ChannelFuture writeAndFlushFuture = channelHandlerContext.writeAndFlush(byteBuf);
        writeAndFlushFuture.get();
    }

    // Allocate
    // we could have a map of metrics to rate.
    // needs to be in a utility class.
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