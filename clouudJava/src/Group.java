import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Group {

    String name;
    ArrayList<User> members;
    SecretKey privateKey;
    SecretKey publicKey;
    IvParameterSpec ivParameterSpec;

    public static HashMap<String, File> uploadedFiles;
    //HashMap<File, SecretKey> fileKeyPairs = new HashMap<>();


    Group(String name) throws NoSuchAlgorithmException {
        this.name = name;
        members = new ArrayList<>();
        privateKey = generateKey(128);
        publicKey = generateKey(128);
        ivParameterSpec = generateIv();
        uploadedFiles = new HashMap<>();
    }

    public static File getEncrypted(String filename){
        return uploadedFiles.get(filename);
    }

    public static SecretKey generateKey(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(n);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    void addMembers(User member){
        members.add(member);
        member.addGroup(this);
    }

    void removeMember(User member){
        members.remove(member);
    }

    public ArrayList<User> getMembers() {
        return members;
    }

    public void addFile(File file){
        uploadedFiles.put(file.getName(), file);
    }

    public void listFiles()
    {
        if(uploadedFiles != null) {
            List<String> names = new ArrayList<>(uploadedFiles.keySet());
            for (int i = 0; i < names.size(); i++) {
                System.out.println(names.get(i));
            }
        }
    }

}
