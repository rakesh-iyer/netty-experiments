import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;

public class FileUtils {
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
            // if remaining is 0, you will always get 0 back, as EOF will not get a chance to be exercised.
            readBytes = fileChannel.read(byteBuffer);
            remainingBytes -= readBytes;
        } while (remainingBytes > 0);

        fileChannel.close();
        return byteBuffer;
    }

    static boolean isValidFileName(String fileName) {
        Path path = Paths.get(fileName);
        return Files.exists(path);
    }
}
