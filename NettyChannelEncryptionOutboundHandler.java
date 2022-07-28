import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;


public class NettyChannelEncryptionOutboundHandler extends NettyChannelOutboundHandler {
    static Logger logger = LogManager.getLogger(NettyChannelEncryptionOutboundHandler.class.getName());
    String algorithm = "AES";
    SecretKey key;
    static NettyChannelEncryptionListener nettyChannelEncryptionListener = new NettyChannelEncryptionListener();

    NettyChannelEncryptionOutboundHandler() {
        try {
            key = KeyUtils.readSecretKey();
        } catch (Exception e) {
            key = null;
        }
    }

    static class NettyChannelEncryptionListener implements GenericFutureListener<ChannelFuture> {
        @Override
        public void operationComplete(ChannelFuture channelFuture) throws Exception {
            if (!channelFuture.isSuccess()) {
                channelFuture.channel().close();
            }
        }
    }

    // No netty allocations, only java allocs that should be auto collected.
    byte[] encryptMessage(ByteBuf byteBuf) throws Exception {
        int length = byteBuf.readInt();
        byte[] messageBytes = new byte[length];
        byteBuf.readBytes(messageBytes);

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(messageBytes);
        logger.debug("Encrypted message is " + encrypted.length + " bytes");

        return encrypted;
    }

    // Allocate + Free
    @Override
    public void write(ChannelHandlerContext channelHandlerContext, Object object, ChannelPromise channelPromise) throws Exception {
        logger.debug("NettyChannelEncryptionOutboundHandler::write");
        byte[] encrypted = encryptMessage((ByteBuf) object);
        ReferenceCountUtil.release(object);

        // this allocation will be freed by Netty
        // https://netty.io/wiki/reference-counted-objects.html#outbound-messages
        ByteBuf byteBuf = channelHandlerContext.alloc().buffer();
        byteBuf.writeInt(encrypted.length);
        byteBuf.writeBytes(encrypted);

        ChannelFuture writeAndFlushFuture = channelHandlerContext.writeAndFlush(byteBuf, channelPromise);
        writeAndFlushFuture.addListener(nettyChannelEncryptionListener);
    }
}