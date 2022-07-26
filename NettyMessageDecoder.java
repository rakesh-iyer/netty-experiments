import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class NettyMessageDecoder extends ByteToMessageDecoder {
    // Allocate
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

        // unnecessary but looks cleaner way to updating the read index.
        length = byteBuf.readInt();
        // Should be freed by reader layers.
        ByteBuf messageEncapsulatingByteBuf = byteBuf.readBytes(length);
        list.add(messageEncapsulatingByteBuf);
    }
}
