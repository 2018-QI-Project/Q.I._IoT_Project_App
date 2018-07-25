package qualcomminstitute.iot;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class SendMessageToServer extends AsyncTask<String, Void, Void> implements NetworkInterface {
    private final String SERVER_FILE_NAME = "index.php";

    @Override
    protected Void doInBackground(String... strings) {
        HttpURLConnection serverConnection = null;
        String serverURL = "http://" + SERVER_ADDRESS + SERVER_FILE_NAME;
        try {
            URL url = new URL(serverURL);
            // POST 데이터 전송을 위한 자료구조
            Map<String,Object> params = new LinkedHashMap<>();
            params.put("KEY", "VALUE");

            // POST 데이터들을 UTF-8로 인코딩
            StringBuilder postData = new StringBuilder();
            for(Map.Entry<String,Object> param : params.entrySet()) {
                if(postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            // URL을 통한 서버와의 연결 설정
            serverConnection = (HttpURLConnection)url.openConnection();
            serverConnection.setRequestMethod("POST");
            serverConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            serverConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

            // POST 데이터를 설정
            serverConnection.setDoOutput(true);
            serverConnection.getOutputStream().write(postDataBytes);
        }
        catch(MalformedURLException e) {
            Log.e(this.getClass().getName(), "URL ERROR!");
        }
        catch(IOException e) {
            Log.e(this.getClass().getName(), "IO ERROR!");
        }
        finally {
            if(serverConnection != null) {
                serverConnection.disconnect();
            }
        }

        return null;
    }
}
