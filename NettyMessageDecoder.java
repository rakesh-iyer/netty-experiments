import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class NettyMessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < Integer.BYTES) {
            // you want <= as supporting a 0 byte message maybe unnecessary.
            return;
        }

        // the recommended way to get the length without impacting the readerIndex.
        int length = byteBuf.getInt(byteBuf.readerIndex());
        if (byteBuf.readableBytes() < length + Integer.BYTES) {
            // is there enough data for a message.
            return;
        }

        // can we not avoid this dual copy. or can we do this more efficiently.
        ByteBuf messageBuf = channelHandlerContext.alloc().buffer();
        // unnecessary but looks cleaner way to updating the read index.
        length = byteBuf.readInt();
        ByteBuf messageEncapsulatingByteBuf = byteBuf.readBytes(length);
        messageBuf.writeInt(length);
        messageBuf.writeBytes(messageEncapsulatingByteBuf);

        list.add(messageBuf);
        // need to learn how to do netty bytebuf management carefully and correctly.
        messageEncapsulatingByteBuf.release();
    }
}
