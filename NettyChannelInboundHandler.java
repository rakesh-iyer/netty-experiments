import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NettyChannelInboundHandler extends NettyChannelHandler implements ChannelInboundHandler {
    static Logger logger = LogManager.getLogger(NettyChannelInboundHandler.class.getName());

    public void channelRegistered(ChannelHandlerContext channelHandlerContext) throws Exception {
        logger.info("NettyChannelInboundHandler::channelRegistered");
        channelHandlerContext.fireChannelRegistered();
    }

    public void channelUnregistered(ChannelHandlerContext channelHandlerContext) throws Exception {
        logger.info("NettyChannelInboundHandler::channelUnregistered");
        channelHandlerContext.fireChannelUnregistered();
    }

    public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
        logger.info("NettyChannelInboundHandler::channelActive");
        channelHandlerContext.fireChannelActive();
    }

    public void channelInactive(ChannelHandlerContext channelHandlerContext) throws Exception {
        logger.info("NettyChannelInboundHandler::channelInactive");
        channelHandlerContext.fireChannelInactive();
    }

    public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
        logger.debug("NettyChannelInboundHandler::channelRead");
        channelHandlerContext.fireChannelRead(object);
    }

    public void channelReadComplete(ChannelHandlerContext channelHandlerContext) throws Exception {
        logger.debug("NettyChannelInboundHandler::channelReadComplete");
        channelHandlerContext.fireChannelReadComplete();
    }

    public void userEventTriggered(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
        logger.debug("NettyChannelInboundHandler::userEventTriggered");
        channelHandlerContext.fireUserEventTriggered(object);
    }

    public void channelWritabilityChanged(ChannelHandlerContext channelHandlerContext) throws Exception {
        logger.debug("NettyChannelInboundHandler::channelWritabilityChanged");
        channelHandlerContext.fireChannelWritabilityChanged();
    }

    /*
     In most cases, the caught exception should be logged and its associated channel should be closed here,
     although the implementation of this method can be different depending on what you want to do to deal
     with an exceptional situation.
     For example, you might want to send a response message with an error code before closing the connection.
     */
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) throws Exception {
        logger.info("NettyChannelInboundHandler::exceptionCaught " + throwable.getCause());
    }
}
