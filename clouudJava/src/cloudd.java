import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class cloudd {

    public static HashMap<String, User> userDatabase = new HashMap<>();
    public static EncryptDecrypt encryptDecrypt = new EncryptDecrypt();
    public static HashMap<String, Group> groupDatabase = new HashMap<>();
    private static Object AESUtil;

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, BadPaddingException, InvalidKeyException {
        User currentUser = null;
        Group currentGroup = null;
        Scanner scanner = new Scanner(System.in);
        System.out.println("L - login /// C - create account");
        String input = scanner.next();

        if(input.equalsIgnoreCase("l"))
        {
            currentUser = userLogin(scanner);
        }
        else if (input.equalsIgnoreCase("c"))
        {
            createNewUser(scanner);
            System.out.println("\nNow login ");
            currentUser = userLogin(scanner);
        }

        /*boolean ingroup = false;
        while(!ingroup) {*/
            System.out.println("C - create group /// J - join group");
            input = scanner.next();
            if (input.equalsIgnoreCase("c")) {
                currentGroup = createGroup(scanner, currentUser);
                //ingroup = true;
            } else if (input.equalsIgnoreCase("j")) {
                currentGroup = joinGroup(scanner, currentUser);
                //ingroup = true;
            }

            boolean quit = false;
            while (!quit) {
                System.out.println("U - upload file /// D - download file // Q - move to another group ");
                input = scanner.next();
                if (input.equalsIgnoreCase("u")) {
                    uploadFile(scanner, currentUser, currentGroup);
                    //scanner.next();
                } else if (input.equalsIgnoreCase("d")) {
                    downloadFile(scanner, currentUser, currentGroup);
                    //scanner.next();
                } else {
                    quit = true;
                    //ingroup= false;
                }
            }
        //}

    }

    public static void createNewUser(Scanner scanner) throws NoSuchAlgorithmException {
        System.out.println("\nEnter username: ");
        String username = scanner.next();

        System.out.println("\nEnter password: ");
        String password = scanner.next();

        User u1 = new User(username, password);

        userDatabase.put(username, u1);
    }

    public static User userLogin(Scanner scanner){
        System.out.println("\nEnter username: ");
        String username = scanner.next();

        if(userDatabase.containsKey(username))
        {
            System.out.println("\nEnter password: ");
            String password = scanner.next();
            if(password.equalsIgnoreCase(userDatabase.get(username).getPassword()))
            {
                System.out.println("\nLogged in successfully");
                return userDatabase.get(username);
            }
            else
            {
                System.out.println("\nIncorrect password");
            }
        }
        else {
            System.out.println("\nUser does not exist");
        }
        return null;
    }

    public static void uploadFile(Scanner scanner, User user, Group group) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        System.out.println("Enter file name: ");
        String filename = scanner.next();
        File file = new File(filename);

        System.out.print("Enter something to put in file : ");
        String contents = scanner.nextLine() + scanner.nextLine();
//        while(scanner.hasNextLine()){
//            contents += scanner.nextLine() + " ";
//        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(contents);
        writer.close();

        SecretKey key = group.privateKey;
        String algorithm = "AES/CBC/PKCS5Padding";
        IvParameterSpec iv = group.ivParameterSpec;
        File encryptedFile = new File(filename + "_encrypted");
        encryptDecrypt.encryptFile(algorithm, key, iv, file, encryptedFile);

        group.addFile(encryptedFile);

        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(encryptedFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        System.out.println("\n Encrypted file contents:");
        System.out.println(resultStringBuilder.toString());
    }

    public static void downloadFile(Scanner scanner, User user, Group group) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        System.out.println("What file would you like to download: ");
        group.listFiles();
        String file = scanner.next();

        File encrypted = group.getEncrypted(file);
        String algorithm = "AES/CBC/PKCS5Padding";
        SecretKey key = group.privateKey;
        IvParameterSpec iv = group.ivParameterSpec;
        File decrypted = new File(file + "_decrypted");

        encryptDecrypt.decryptFile(algorithm, key, iv, encrypted, decrypted);

        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(decrypted))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        System.out.println("\n File contents:");
        System.out.println(resultStringBuilder.toString());
    }

    public static Group createGroup(Scanner scanner, User user) throws NoSuchAlgorithmException {
        System.out.println("Enter name for group: ");
        String name = scanner.next();
        Group newGroup = new Group(name);
        newGroup.addMembers(user);
        groupDatabase.put(name, newGroup);
        System.out.println("\nSuccessfully created and joined group");
        return newGroup;
    }

    public static Group joinGroup(Scanner scanner, User user){
        System.out.println("Enter name of group to join: ");
        String name = scanner.next();

        if(groupDatabase.containsKey(name))
        {
            Group thisGroup = groupDatabase.get(name);
            thisGroup.addMembers(user);
            System.out.println("\nSuccessfully joined group");
            return thisGroup;
        }
        else
        {
            System.out.println("\n No such group exists");
            return null;
        }

    }

}
