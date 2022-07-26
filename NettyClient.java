import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NettyClient {
    static Logger logger = LogManager.getLogger(NettyClient.class.getName());
    static int SERVER_PORT = 3333;
    static int CLIENT_PORT = 1234;
    static String SERVER_HOST = "localhost";
    static String CLIENT_HOST = "localhost";
    static boolean enableSecurity = true;


    static class NettyClientChannelFactory implements ChannelFactory<Channel> {
        @Override
        public Channel newChannel() {
            return new NioSocketChannel();
        }
    }

    static class NettyClientChannelInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            socketChannel.pipeline().addLast(new NettyMessageDecoder());
            if (enableSecurity) {
                socketChannel.pipeline().addLast(new NettyChannelDecryptionInboundHandler());
                socketChannel.pipeline().addLast(new NettyChannelEncryptionOutboundHandler());
            }
            socketChannel.pipeline().addLast(new NettyClientChannelProcessingInboundHandler());
        }
    }

    public static void main(String args[]) throws InterruptedException {
        logger.info("Starting high throughput netty client.");
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup());
        bootstrap.channelFactory(new NettyClientChannelFactory());
        bootstrap.handler(new NettyClientChannelInitializer());
        ChannelFuture connectFuture = bootstrap.connect(SERVER_HOST, SERVER_PORT).sync();
        connectFuture.channel().closeFuture().sync();
    }
}
