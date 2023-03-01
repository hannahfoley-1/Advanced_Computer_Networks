import java.io.*;
import java.net.*;

public class RequestHandler implements Runnable {
    Socket clientSocket;
    BufferedReader proxyToClientBr;
    BufferedWriter proxyToClientBw;

    public RequestHandler(Socket clientSocket){
        this.clientSocket = clientSocket;
        try{
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

                //get url string and correct it if needed
                String url = request.substring(request.indexOf('/') + 1);
                url = url.substring(0, url.indexOf(' '));
                if (!url.startsWith("http")) {
                    String http = "http://";
                    url = http + url;
                }

                //if request type is get, check if blocked
                if (Proxy.isBlocked(url)) {
                    System.out.println("Blocked site");
                    requestForBlockedSite();
                    return;
                }

                //check if in cache
                if (Proxy.isCached(url)) {
                    System.out.println("Cached site");
                    requestForCached(url);
                    return;
                }

                //send request to web server
                requestForUrl(url);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println("Error reading request from client on local host");
            return;
        }

    }

    private void requestForBlockedSite() throws IOException {
        try{
            //return negative response to client
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            String repsonseToClient = "HTTP/1.0 403 Access Forbidden \n" +
                    "Proxy-agent: ProxyServer/1.0\n" +
                    "\r\n";
            bw.write(repsonseToClient);
            proxyToClientBw.write(repsonseToClient);
            System.out.println(repsonseToClient);
            bw.flush();
            proxyToClientBw.flush();
        } catch(IOException e){
            System.out.println("Error letting client know site is blocked");
            e.printStackTrace();
        }
    }

    //this sends request to web server, un-cached url
    private void requestForUrl(String url) throws IOException {
        URL site = new URL(url);
        System.out.println("url requested: " + url);

        //request from web server
        HttpURLConnection proxyToServerConnection = (HttpURLConnection) site.openConnection();

        //ensure response code is OK
        int responseCode = proxyToServerConnection.getResponseCode();
        String responseMessage = proxyToServerConnection.getResponseMessage();
        String response = "Response code : " + responseCode + " " + responseMessage;
        System.out.println(response);

        if(responseCode == 200) {
            //return positive response to client
            String repsonseToClient = "HTTP/1.0 200 OK\n" +
                    "Proxy-agent: ProxyServer/1.0\n" +
                    "\r\n";
            proxyToClientBw.write(repsonseToClient);

            //read in the web page and display it to the client
            //create a new file where we will store page to put in cache

            BufferedReader readWebsite = new BufferedReader(new InputStreamReader(proxyToServerConnection.getInputStream()));
            String htmlLine = null;

            //create file to store in cache
            String fileName = url.substring(url.indexOf('/')+2 , url.indexOf('.'));
            fileName += ".html";
            File cachedFile = new File (fileName);
            cachedFile.createNewFile();
            BufferedWriter cacheFileWriter = new BufferedWriter(new FileWriter(cachedFile));

            while((htmlLine = readWebsite.readLine()) != null){
                // write the web page to the client
                proxyToClientBw.write(htmlLine);

                //cache this line of html to the file
                cacheFileWriter.write(htmlLine);
            }

        //display the html to the client
        proxyToClientBw.flush();

        cacheFileWriter.flush();

        if(readWebsite != null){
            readWebsite.close();
        }

        //create resource record from file
            ResponseRecord rr = new ResponseRecord(url, "CNAME", response, cachedFile);

            //add this response record to the cache
            Proxy.cacheUrl(url, rr);
        }

    }

    /*
        url already in cache so get the response message stored in cache
     */
    private void requestForCached(String url) throws IOException {
        ResponseRecord rr = Proxy.cache2.get(url);

        //check ttl on cache item
        if(rr.checkTTL() == false)
        {
            //if TTL has expired, remove from cache
            System.out.println("Item in cache expired");
            Proxy.removeUrlFromCache(url);

            //request for it from web server instead
            requestForUrl(url);
        }
        else
        {
            System.out.println(Proxy.cache2.get(url).data);

            //return positive response to client
            String repsonseToClient = "HTTP/1.0 200 OK\n" +
                    "Proxy-agent: ProxyServer/1.0\n" +
                    "\r\n";
            proxyToClientBw.write(repsonseToClient);

            //read file from cache
            FileReader readFileFromCache = new FileReader(Proxy.cache2.get(url).site);
            BufferedReader readFileFromCacheBR = new BufferedReader(readFileFromCache);

            String htmlLine = null;
            while((htmlLine = readFileFromCacheBR.readLine()) != null) {
                // write the web page to the client
                proxyToClientBw.write(htmlLine);
            }

            //display webpage
            proxyToClientBw.flush();
        }
    }
}
