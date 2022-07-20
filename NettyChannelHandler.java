import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

public class NettyChannelHandler implements ChannelHandler {
    @Override
    public void handlerAdded(ChannelHandlerContext channelHandlerContext) throws Exception {
        System.out.println("NettyChannelHandler::handlerAdded");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext channelHandlerContext) throws Exception {
        System.out.println("NettyChannelHandler::handlerRemoved");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) throws Exception {
        System.out.println("NettyChannelHandler::exceptionCaught");
    }
}
