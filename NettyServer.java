import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.concurrent.ExecutionException;

public class NettyServer {
    static Logger logger = LogManager.getLogger(NettyServer.class.getName());
    static int SERVER_PORT = 3333;
    static String SERVER_HOST = "localhost";
    static boolean enableSecurity = true;

    static NettyChannelDecryptionInboundHandler nettyChannelDecryptionInboundHandler = new NettyChannelDecryptionInboundHandler();
    static NettyChannelEncryptionOutboundHandler nettyChannelEncryptionOutboundHandler = new NettyChannelEncryptionOutboundHandler();
    static NettyServerChannelProcessingInboundHandler nettyServerChannelProcessingInboundHandler = new NettyServerChannelProcessingInboundHandler();

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
