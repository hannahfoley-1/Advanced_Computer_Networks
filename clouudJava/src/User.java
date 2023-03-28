import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.ArrayList;

public class User {
    String username;
    String password;
    SecretKey privateKey;
    SecretKey publicKey;
    ArrayList<Group> groups;

    User(String username, String password) throws NoSuchAlgorithmException {
        this.username = username;
        this.password = password;
        privateKey = generateKey(128);
        publicKey = generateKey(128);
        this.groups = new ArrayList<>();
    }


    public String getPassword() {
        return password;
    }

    public SecretKey getPrivateKey() {
        return privateKey;
    }

    public SecretKey getPublicKey() {
        return publicKey;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPrivateKey(SecretKey privateKey) {
        this.privateKey = privateKey;
    }

    public void setPublicKey(SecretKey publicKey) {
        this.publicKey = publicKey;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public static SecretKey generateKey(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(n);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    public void addGroup(Group group)
    {
        groups.add(group);
    }


}
