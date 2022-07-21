import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NettyChannelHandler implements ChannelHandler {
    static Logger logger = LogManager.getLogger(NettyChannelHandler.class.getName());

    @Override
    public void handlerAdded(ChannelHandlerContext channelHandlerContext) throws Exception {
        logger.info("NettyChannelHandler::handlerAdded");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext channelHandlerContext) throws Exception {
        logger.info("NettyChannelHandler::handlerRemoved");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) throws Exception {
        logger.info("NettyChannelHandler::exceptionCaught");
    }
}
