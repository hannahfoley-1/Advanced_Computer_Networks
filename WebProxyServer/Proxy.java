import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Proxy implements Runnable {

    public static void main(String[] args) throws IOException {
        Proxy proxy = new Proxy(12345);
        proxy.listen();
    }

    static HashMap<String, String> cache;
    static HashMap<String, String> blockedUrls;
    private ServerSocket serverSocket;
    private boolean running = true;
    static ArrayList<Thread> inProgress;


    public Proxy(int port) throws IOException {
        cache = new HashMap<>();
        blockedUrls = new HashMap<>();
        inProgress = new ArrayList<>();
        new Thread(this).start();

        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("Waiting for client on port " + serverSocket.getLocalPort());
        } catch (SocketException e) {
            System.out.println("Socket exception");
        }
    }

    public void listen() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                Thread thread = new Thread(new RequestHandler(socket));
                inProgress.add(thread);
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run(){
            Scanner scanner = new Scanner(System.in);
            String action;
            while(running)
            {
                System.out.println("Press a key");
                System.out.println("b = block a site");
                System.out.println("q = quit server");
                System.out.println("or make request in browser at local host 12345");
                action = scanner.next();

                if(action.equalsIgnoreCase("b")){
                    String urlToBlock;
                    System.out.println("Enter site to block");
                    urlToBlock = scanner.next();
                    blockUrl(urlToBlock);
                }
                else if(action.equalsIgnoreCase("q")){
                    System.out.println("Bye bye");
                }
            }
            scanner.close();
        }

        /*
            Checks if the URL is in the blocked URL hashmap and returns boolean
         */
        public static boolean isBlocked(String url){
            if(blockedUrls.get(url) != null)
            {
                return true;
            }
            return false;
        }

        /*
            Blocks url passed in as a parameter by adding it to the blocked url hashmap
         */
        public static void blockUrl(String url)
        {
            if(isBlocked(url))
            {
                System.out.println("The site " + url + " is already blocked");
            }
            else
            {
                if (!url.startsWith("http")) {
                    String http = "http://";
                    url = http + url;
                }
                blockedUrls.put(url,url);
                System.out.println("Site added to blocked list successfully");
            }
        }

    /*
        Checks if the URL is in the `cached` URL hashmap and returns boolean
     */
    public static boolean isCached(String url){
        if(cache.get(url) != null)
        {
            return true;
        }
        return false;
    }

    /*
            Caches url passed in as a parameter by adding it to the cached url hashmap
         */
    public static void cacheUrl(String url)
    {
        if(isCached(url))
        {
            System.out.println("The site " + url + " is already cached");
        }
        else
        {
            cache.put(url,url);
            System.out.println("Site added to cached list successfully");
        }
    }

}

