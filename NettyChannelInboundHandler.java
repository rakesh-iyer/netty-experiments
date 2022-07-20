import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;

public class NettyChannelInboundHandler extends NettyChannelHandler implements ChannelInboundHandler {
    public void channelRegistered(ChannelHandlerContext channelHandlerContext) throws Exception {
        System.out.println("NettyChannelInboundHandler::channelRegistered");
        channelHandlerContext.fireChannelRegistered();
    }

    public void channelUnregistered(ChannelHandlerContext channelHandlerContext) throws Exception {
        System.out.println("NettyChannelInboundHandler::channelUnregistered");
        channelHandlerContext.fireChannelUnregistered();
    }

    public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
        System.out.println("NettyChannelInboundHandler::channelActive");
        channelHandlerContext.fireChannelActive();
    }

    public void channelInactive(ChannelHandlerContext channelHandlerContext) throws Exception {
        System.out.println("NettyChannelInboundHandler::channelInactive");
        channelHandlerContext.fireChannelInactive();
    }

    public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
        System.out.println("NettyChannelInboundHandler::channelRead");
        channelHandlerContext.fireChannelRead(object);
    }

    public void channelReadComplete(ChannelHandlerContext channelHandlerContext) throws Exception {
        System.out.println("NettyChannelInboundHandler::channelReadComplete");
        channelHandlerContext.fireChannelReadComplete();
    }

    public void userEventTriggered(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
        System.out.println("NettyChannelInboundHandler::userEventTriggered");
        channelHandlerContext.fireUserEventTriggered(object);
    }

    public void channelWritabilityChanged(ChannelHandlerContext channelHandlerContext) throws Exception {
        System.out.println("NettyChannelInboundHandler::channelWritabilityChanged");
        channelHandlerContext.fireChannelWritabilityChanged();
    }

    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) throws Exception {
        System.out.println("NettyChannelInboundHandler::exceptionCaught " + throwable.getCause());
    }
}
