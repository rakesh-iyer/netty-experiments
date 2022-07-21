import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.channel.ChannelPromise;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.SocketAddress;

public class NettyChannelOutboundHandler extends NettyChannelHandler implements ChannelOutboundHandler {
    static Logger logger = LogManager.getLogger(NettyChannelInboundHandler.class.getName());

    @Override
    public void bind(ChannelHandlerContext channelHandlerContext, SocketAddress socketAddress, ChannelPromise channelPromise) throws Exception {
        logger.info("NettyChannelOutboundHandler::bind");
        channelHandlerContext.bind(socketAddress, channelPromise);
    }

    @Override
    public void connect(ChannelHandlerContext channelHandlerContext, SocketAddress socketAddress, SocketAddress socketAddress1, ChannelPromise channelPromise) throws Exception {
        logger.info("NettyChannelOutboundHandler::connect");
        channelHandlerContext.connect(socketAddress, socketAddress1, channelPromise);
    }

    @Override
    public void disconnect(ChannelHandlerContext channelHandlerContext, ChannelPromise channelPromise) throws Exception {
        logger.info("NettyChannelOutboundHandler::disconnect");
        channelHandlerContext.disconnect(channelPromise);
    }

    @Override
    public void close(ChannelHandlerContext channelHandlerContext, ChannelPromise channelPromise) throws Exception {
        logger.info("NettyChannelOutboundHandler::close");
        channelHandlerContext.close(channelPromise);
    }

    @Override
    public void deregister(ChannelHandlerContext channelHandlerContext, ChannelPromise channelPromise) throws Exception {
        logger.info("NettyChannelOutboundHandler::deregister");
        channelHandlerContext.deregister(channelPromise);
    }

    @Override
    public void read(ChannelHandlerContext channelHandlerContext) throws Exception {
        logger.debug("NettyChannelOutboundHandler::read");
        channelHandlerContext.read();
    }

    @Override
    public void write(ChannelHandlerContext channelHandlerContext, Object o, ChannelPromise channelPromise) throws Exception {
        logger.debug("NettyChannelOutboundHandler::write");
        channelHandlerContext.write(o, channelPromise);
    }

    @Override
    public void flush(ChannelHandlerContext channelHandlerContext) throws Exception {
        logger.debug("NettyChannelOutboundHandler::flush");
        channelHandlerContext.flush();
    }
}