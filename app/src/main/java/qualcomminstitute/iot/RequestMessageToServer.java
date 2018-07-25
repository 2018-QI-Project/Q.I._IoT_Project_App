package qualcomminstitute.iot;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RequestMessageToServer extends AsyncTask<String, Void, String> implements NetworkInterface {
    private final String SERVER_FILE_NAME = "index.php";

    @Override
    protected void onPostExecute(String retrieveMessage) {
        super.onPostExecute(retrieveMessage);
    }

    @Override
    protected String doInBackground(String... strings) {
        HttpURLConnection serverConnection = null;
        String serverURL = "http://" + SERVER_ADDRESS + SERVER_FILE_NAME;
        try {
            // GET 형식으로 메세지 보내기
            serverURL += strings[0];
            URL url = new URL(serverURL);

            // URL을 통한 서버와의 연결 설정
            serverConnection = (HttpURLConnection)url.openConnection();
            serverConnection.setRequestMethod("GET");
            serverConnection.setDoInput(true);

            // 요청 결과
            InputStream is = serverConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String readLine;
            StringBuilder response = new StringBuilder();
            while ((readLine = br.readLine()) != null) {
                response.append(readLine);
            }
            br.close();

            return response.toString();
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
