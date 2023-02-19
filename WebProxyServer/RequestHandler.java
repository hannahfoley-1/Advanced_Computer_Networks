import java.io.*;
import java.net.*;

public class RequestHandler implements Runnable {
    Socket clientSocket;
    BufferedReader proxyToClientBr;
    BufferedWriter proxyToClientBw;

    private Thread httpsClientToServer;

    public RequestHandler(Socket clientSocket){
        this.clientSocket = clientSocket;
        try{
            this.clientSocket.setSoTimeout(2000);
            proxyToClientBr = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            proxyToClientBw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String request;
        try{
            request = proxyToClientBr.readLine();
            System.out.println("Request received " + request);

            if(request != null) {
                //request type
                String requestType = request.substring(0, request.indexOf(' '));
                System.out.println("Request type: " + requestType);

                //get url string
                String url = request.substring(request.indexOf('/') + 1);
                url = url.substring(0, url.indexOf(' '));
                System.out.println("url: " + url);
                if (!url.startsWith("http")) {
                    String http = "http://";
                    url = http + url;
                }

                //if request type is get, check if blocked
                if (Proxy.isBlocked(url)) {
                    System.out.println("Blocked site");
                    //requestForUrl(url);
                    requestForBlockedSite();
                    return;
                }

                //check if in cache
                if (Proxy.isCached(url)) {
                    System.out.println("Cached site");
                    requestForCached(url);
                    return;
                }

                //send request
                requestForUrl(url);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println("Error reading request from client");
            return;
        }

    }

    private void requestForBlockedSite() throws IOException {
        try{
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            String line = "HTTP/1.0 403 Access Forbidden \n" +
                    "User-Agent: ProxyServer/1.0\n" +
                    "\r\n";
            bufferedWriter.write(line);
            bufferedWriter.flush();
        } catch(IOException e){
            System.out.println("Error writing to client when requested a blocked site");
            e.printStackTrace();
        }
    }

    private void requestForUrl(String url) throws IOException {
        URL site = new URL(url);
//        URLConnection connection = site.openConnection();
//        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//        String inputLine;
//        while ((inputLine = in.readLine()) != null){
//            System.out.println(inputLine);
//        }
//        in.close();
        HttpURLConnection proxyToServerConnection = (HttpURLConnection) site.openConnection();
        //proxyToServerConnection.setRequestMethod("GET");
        int responseCode = proxyToServerConnection.getResponseCode();
        String responseMessage = proxyToServerConnection.getResponseMessage();
        String response = "Response code : " + responseCode + " " + responseMessage;
        System.out.println(response);
        Proxy.cache.put(url, response);


        return;
    }

    private void requestForCached(String url)
    {
        System.out.println(Proxy.cache.get(url));
        return;
    }
}
