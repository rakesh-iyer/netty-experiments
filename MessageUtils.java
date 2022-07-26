import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.GenericFutureListener;

import java.nio.ByteBuffer;

public class MessageUtils {
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

    // Allocate
    static ByteBuf prepareMessage(ByteBufAllocator byteBufAllocator, byte[] message) throws Exception {
        ByteBuf byteBuf = byteBufAllocator.buffer();
        byteBuf.writeInt(message.length);
        byteBuf.writeBytes(message);

        return byteBuf;
    }

    // Allocate
    static ByteBuf prepareMessage(ByteBufAllocator byteBufAllocator, ByteBuffer message) throws Exception {
        message.flip();
        ByteBuf byteBuf = byteBufAllocator.buffer();
        byteBuf.writeInt(message.remaining());
        byteBuf.writeBytes(message);

        return byteBuf;
    }

    static byte[] deserializeMessage(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
        int length = byteBuf.readInt();
        byte[] messageBytes = new byte[length];
        byteBuf.readBytes(messageBytes);

        return messageBytes;
    }
}
