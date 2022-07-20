import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class KeyUtils {
    static String KEY_FILE = "/Users/riyer/key.txt";
    static SecretKey readSecretKey() throws Exception {
        FileInputStream fileInputStream = new FileInputStream(KEY_FILE);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

        return (SecretKey) objectInputStream.readObject();
    }

    // this will be invoked once to setup the keyfile, and the file will be shared between the client and server.
    // For future:: Perform key exchange using Public Key crypto.
    static void writeSecretKey() throws Exception {
        SecretKey secretKey = KeyGenerator.getInstance("AES").generateKey();
        FileOutputStream fileOutputStream = new FileOutputStream(KEY_FILE);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(secretKey);
        objectOutputStream.close();
    }
 }
