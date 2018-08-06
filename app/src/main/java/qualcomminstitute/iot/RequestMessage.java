package qualcomminstitute.iot;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static qualcomminstitute.iot.NetworkInterface.SERVER_ADDRESS;

public class RequestMessage extends Thread {
    private String strRequest;
    private String strMethod;
    private JSONObject jsonMessage;
    private Handler handler;

    public RequestMessage(String strRequest, String strMethod, JSONObject jsonMessage, Handler handler) {
        this.strRequest = strRequest;
        this.strMethod = strMethod;
        this.jsonMessage = jsonMessage;
        this.handler = handler;
    }

    @Override
    public void run() {
        HttpURLConnection serverConnection = null;
        String serverURL = "http://" + SERVER_ADDRESS + strRequest;
        try {
            URL url = new URL(serverURL);

            byte[] postDataBytes = jsonMessage.toString().getBytes(NetworkInterface.ENCODE);

            // URL을 통한 서버와의 연결 설정
            serverConnection = (HttpURLConnection)url.openConnection();
            serverConnection.setRequestMethod(strMethod);
            serverConnection.setRequestProperty("Content-Type", NetworkInterface.JSON_HEADER);
            serverConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

            // 서버의 입력 설정 및 데이터 추가
            serverConnection.setDoOutput(true);
            serverConnection.getOutputStream().write(postDataBytes);

            // 요청 결과
            InputStream is = serverConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String readLine;
            StringBuilder response = new StringBuilder();
            while ((readLine = br.readLine()) != null) {
                response.append(readLine);
            }
            br.close();

            Message returnMessage = handler.obtainMessage(NetworkInterface.REQUEST_SUCCESS);
            Bundle bundle = new Bundle();
            bundle.putString(NetworkInterface.RESPONSE_DATA, response.toString());
            returnMessage.setData(bundle);
            handler.sendMessage(returnMessage);
        }
        catch(MalformedURLException e) {
            Log.e(this.getClass().getName(), "URL ERROR!");
            Message returnMessage = handler.obtainMessage(NetworkInterface.REQUEST_FAIL);
            handler.sendMessage(returnMessage);
        }
        catch(IOException e) {
            Log.e(this.getClass().getName(), "IO ERROR!");
            Message returnMessage = handler.obtainMessage(NetworkInterface.REQUEST_FAIL);
            handler.sendMessage(returnMessage);
        }
        finally {
            if(serverConnection != null) {
                serverConnection.disconnect();
            }
        }
    }
}
