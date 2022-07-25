import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.GenericFutureListener;

import java.nio.ByteBuffer;

public class MessageSender {
    static void sendMessage(ChannelHandlerContext channelHandlerContext,
                            byte[] message,
                            GenericFutureListener<ChannelFuture> listener) throws Exception {
        ByteBuf byteBuf = prepareMessage(channelHandlerContext.alloc(), message);
        ChannelFuture writeAndFlushFuture = channelHandlerContext.writeAndFlush(byteBuf);
        writeAndFlushFuture.addListener(listener);
    }

    static void sendMessage(ChannelHandlerContext channelHandlerContext,
                            ByteBuffer message,
                            GenericFutureListener<ChannelFuture> listener) throws Exception {
        ByteBuf byteBuf = prepareMessage(channelHandlerContext.alloc(), message);
        ChannelFuture writeAndFlushFuture = channelHandlerContext.writeAndFlush(byteBuf);
        writeAndFlushFuture.addListener(listener);
    }

    static ByteBuf prepareMessage(ByteBufAllocator byteBufAllocator, byte[] message) throws Exception {
        ByteBuf byteBuf = byteBufAllocator.buffer();
        byteBuf.writeInt(message.length);
        byteBuf.writeBytes(message);

        return byteBuf;
    }

    static ByteBuf prepareMessage(ByteBufAllocator byteBufAllocator, ByteBuffer message) throws Exception {
        message.flip();
        ByteBuf byteBuf = byteBufAllocator.buffer();
        byteBuf.writeInt(message.remaining());
        byteBuf.writeBytes(message);

        return byteBuf;
    }
}
