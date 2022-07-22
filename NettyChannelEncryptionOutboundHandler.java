import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.security.SecureRandom;

public class NettyChannelEncryptionOutboundHandler extends NettyChannelOutboundHandler {
    static Logger logger = LogManager.getLogger(NettyChannelEncryptionOutboundHandler.class.getName());
    String algorithm = "AES";
    SecretKey key;


    NettyChannelEncryptionOutboundHandler() throws Exception {
        key = KeyUtils.readSecretKey();
    }

    static class NettyChannelEncryptionListener implements GenericFutureListener<ChannelFuture> {
        ByteBuf byteBuf;

        NettyChannelEncryptionListener(ByteBuf bb) {
            byteBuf = bb;
        }

        @Override
        public void operationComplete(ChannelFuture channelFuture) throws Exception {
            if (!channelFuture.isSuccess()) {
                channelFuture.channel().close();
            }
        }
    }

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

    @Override
    public void write(ChannelHandlerContext channelHandlerContext, Object object, ChannelPromise channelPromise) throws Exception {
        logger.debug("NettyChannelEncryptionOutboundHandler::write");
        byte[] encrypted = encryptMessage((ByteBuf) object);

        ByteBuf byteBuf = channelHandlerContext.alloc().buffer();
        byteBuf.writeInt(encrypted.length);
        byteBuf.writeBytes(encrypted);

        ChannelFuture writeAndFlushFuture = channelHandlerContext.writeAndFlush(byteBuf, channelPromise);

        // this will slow down the progress. lets fix this.
        writeAndFlushFuture.addListener(new NettyChannelEncryptionListener(byteBuf));
        ReferenceCountUtil.release(object);
    }
}