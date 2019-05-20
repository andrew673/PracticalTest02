package ro.pub.cs.systems.pdsd.practicaltest02;

import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;

public class CommunicationThread extends Thread {

    private ServerThread serverThread;
    private Socket socket;
    private TextView textView;

    public CommunicationThread(ServerThread serverThread, Socket socket, TextView textView) {
        this.serverThread = serverThread;
        this.socket = socket;
        this.textView = textView;
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e("tag", "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e("tag", "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }
            Log.i("tag", "[COMMUNICATION THREAD] Waiting for parameters from client (city / information type!");
            String cuvant = bufferedReader.readLine();
            if (cuvant == null || cuvant.isEmpty()) {
                Log.e("tag", "[COMMUNICATION THREAD] Error receiving parameters from client (cuvant)");
                return;
            }
            ArrayList<ContainerClass> data = serverThread.getData();
            ContainerClass weatherForecastInformation = null;

            Log.i("tag", "[COMMUNICATION THREAD] Getting the information from the webservice...");
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet("http://services.aonaware.com/DictService/DictService.asmx/Define?word=" + cuvant);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String pageSourceCode = httpClient.execute(httpGet, responseHandler);
            if (pageSourceCode == null) {
                Log.e("abc", "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                return;
            }
            Document document = Jsoup.parse(pageSourceCode);
            Element element = document.child(0);
            Elements elements = element.getElementsByTag("WordDefinition");
            Log.d("abc", elements.text());
            ContainerClass responseData = new ContainerClass(elements.text());
            serverThread.setData(responseData);
            printWriter.println(elements.text());
            printWriter.flush();
        } catch (IOException ioException) {
            Log.e("tag", "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e("tag", "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                }
            }
        }
    }

}
