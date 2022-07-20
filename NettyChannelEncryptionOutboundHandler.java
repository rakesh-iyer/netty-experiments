import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.security.SecureRandom;

public class NettyChannelEncryptionOutboundHandler extends NettyChannelOutboundHandler {
    String algorithm = "AES";
    SecretKey key;


    NettyChannelEncryptionOutboundHandler() throws Exception {
//        key = KeyGenerator.getInstance(algorithm).generateKey();
        key = KeyUtils.readSecretKey();
    }

    byte[] encryptMessage(ByteBuf byteBuf) throws Exception {
        int length = byteBuf.readInt();
        byte[] messageBytes = new byte[length];
        byteBuf.readBytes(messageBytes);

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(messageBytes);

        return encrypted;
    }

    @Override
    public void write(ChannelHandlerContext channelHandlerContext, Object object, ChannelPromise channelPromise) throws Exception {
        System.out.println("NettyChannelEncryptionOutboundHandler::write");
        byte[] encrypted = encryptMessage((ByteBuf) object);
        ByteBuf byteBuf = channelHandlerContext.alloc().buffer();
        byteBuf.writeBytes(encrypted);

        ChannelFuture writeAndFlushFuture = channelHandlerContext.writeAndFlush(byteBuf);
        writeAndFlushFuture.get();

        channelHandlerContext.write(object, channelPromise);
    }
}