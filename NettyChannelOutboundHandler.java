import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;

public class NettyChannelOutboundHandler extends NettyChannelHandler implements ChannelOutboundHandler {
    @Override
    public void bind(ChannelHandlerContext channelHandlerContext, SocketAddress socketAddress, ChannelPromise channelPromise) throws Exception {
        System.out.println("NettyChannelOutboundHandler::bind");
        channelHandlerContext.bind(socketAddress, channelPromise);
    }

    @Override
    public void connect(ChannelHandlerContext channelHandlerContext, SocketAddress socketAddress, SocketAddress socketAddress1, ChannelPromise channelPromise) throws Exception {
        System.out.println("NettyChannelOutboundHandler::connect");
        channelHandlerContext.connect(socketAddress, socketAddress1, channelPromise);
    }

    @Override
    public void disconnect(ChannelHandlerContext channelHandlerContext, ChannelPromise channelPromise) throws Exception {
        System.out.println("NettyChannelOutboundHandler::disconnect");
        channelHandlerContext.disconnect(channelPromise);
    }

    @Override
    public void close(ChannelHandlerContext channelHandlerContext, ChannelPromise channelPromise) throws Exception {
        System.out.println("NettyChannelOutboundHandler::close");
        channelHandlerContext.close(channelPromise);
    }

    @Override
    public void deregister(ChannelHandlerContext channelHandlerContext, ChannelPromise channelPromise) throws Exception {
        System.out.println("NettyChannelOutboundHandler::deregister");
        channelHandlerContext.deregister(channelPromise);
    }

    @Override
    public void read(ChannelHandlerContext channelHandlerContext) throws Exception {
        System.out.println("NettyChannelOutboundHandler::read");
        channelHandlerContext.read();
    }

    @Override
    public void write(ChannelHandlerContext channelHandlerContext, Object o, ChannelPromise channelPromise) throws Exception {
        System.out.println("NettyChannelOutboundHandler::write");
        channelHandlerContext.write(o, channelPromise);
    }

    @Override
    public void flush(ChannelHandlerContext channelHandlerContext) throws Exception {
        System.out.println("NettyChannelOutboundHandler::flush");
        channelHandlerContext.flush();
    }
}