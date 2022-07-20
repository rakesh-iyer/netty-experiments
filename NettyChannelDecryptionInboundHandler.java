import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;


public class NettyChannelDecryptionInboundHandler extends NettyChannelInboundHandler {
    String algorithm = "AES";
    SecretKey key;

    NettyChannelDecryptionInboundHandler() throws Exception {
        key = KeyUtils.readSecretKey();
    }

    byte[] decryptMessage(ByteBuf byteBuf) throws Exception {
        int length = byteBuf.readableBytes();
        byte[] messageBytes = new byte[length];
        byteBuf.readBytes(messageBytes);

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted = cipher.doFinal(messageBytes);

        return decrypted;
    }

    public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
        System.out.println("NettyChannelDecryptionInboundHandler::channelRead");
        byte[] decrypted = decryptMessage((ByteBuf) object);
        ByteBuf byteBuf = channelHandlerContext.alloc().buffer();
        byteBuf.writeInt(decrypted.length);
        byteBuf.writeBytes(decrypted);

        channelHandlerContext.fireChannelRead(byteBuf);
    }
}