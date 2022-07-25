import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;

public class FileServer {
    static ByteBuffer readFile(String fileName) throws Exception {
        Path filePath = Paths.get(fileName);
        FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.READ);

        long fileSize = fileChannel.size();
        if (fileSize > Integer.MAX_VALUE) {
            throw new ReadOnlyFileSystemException();
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate((int)fileSize);
        int remainingBytes = (int)fileSize;
        int readBytes;
        do {
            // you can read as much is the capacity of the bytebuffer.
            readBytes = fileChannel.read(byteBuffer);
            remainingBytes -= readBytes;
        } while (remainingBytes > 0);

        fileChannel.close();
        return byteBuffer;
    }
}
