import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GenericFutureListener;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

class NettyClientChannelProcessingInboundHandler extends NettyChannelInboundHandler {
    static NettyClientChannelProcessingInboundHandler.SendPacketListener sendPacketListener = new NettyClientChannelProcessingInboundHandler.SendPacketListener();
    static List<String> fileNamesList = Arrays.asList("small.txt", "medium.txt", "large.txt");
    static String READ_MESSAGE = "READ %s";
    static ChannelHandlerContext activeChannelHandlerContext;

    static class SendPacketListener implements GenericFutureListener<ChannelFuture> {
        // start with index = 1, as index = 0 has already been taken care of.
        int index = 1;
        @Override
        public void operationComplete(ChannelFuture channelFuture) throws Exception {
            if (channelFuture.isSuccess()) {
                MessageUtils.sendMessage(activeChannelHandlerContext,
                        String.format(READ_MESSAGE, fileNamesList.get(index)).getBytes(StandardCharsets.UTF_8),
                        sendPacketListener);
            } else {
                channelFuture.cause().printStackTrace();
                channelFuture.channel().close();
            }
            // rotate request amongst the files.
//                index = (index + 1) % fileNamesList.size();
        }
    }

    public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
        logger.debug("NettyClientChannelProcessingInboundHandler::channelActive");
        activeChannelHandlerContext = channelHandlerContext;
        MessageUtils.sendMessage(activeChannelHandlerContext,
                String.format(READ_MESSAGE, fileNamesList.get(0)).getBytes(StandardCharsets.UTF_8),
                sendPacketListener);
    }

    public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
        logger.debug("NettyClientChannelInboundHandler::channelRead");
        byte[] message = MessageUtils.deserializeMessage(channelHandlerContext, (ByteBuf) object);
        ReferenceCountUtil.release(object);
    }
}
