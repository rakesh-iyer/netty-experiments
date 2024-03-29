import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;


public class NettyChannelDecryptionInboundHandler extends NettyChannelInboundHandler {
    static Logger logger = LogManager.getLogger(NettyChannelDecryptionInboundHandler.class.getName());
    String algorithm = "AES";
    SecretKey key;

    NettyChannelDecryptionInboundHandler() {
        try {
            key = KeyUtils.readSecretKey();
        } catch (Exception e) {
            key = null;
        }
    }

    byte[] decryptMessage(ByteBuf byteBuf) throws Exception {
        int length = byteBuf.readableBytes();
        byte[] messageBytes = new byte[length];
        byteBuf.readBytes(messageBytes);

        logger.debug("Trying to decrypt " + length + " bytes");

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted = cipher.doFinal(messageBytes);

        return decrypted;
    }

    // Allocate + Free
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
        logger.debug("NettyChannelDecryptionInboundHandler::channelRead");
        byte[] decrypted = decryptMessage((ByteBuf) object);
        ReferenceCountUtil.release(object);

        // should be freed by reader layer.
        ByteBuf byteBuf = channelHandlerContext.alloc().buffer();
        byteBuf.writeInt(decrypted.length);
        byteBuf.writeBytes(decrypted);

        channelHandlerContext.fireChannelRead(byteBuf);
    }
}