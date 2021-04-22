package hk.edu.cuhk.ie.iems5722.a2_1155155009;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoadLatestMessagesTask extends AsyncTask<Void, Integer, List<String>> {
    private ProgressDialog dialog;
    private String url;
    private Activity activity;
    private String chatroom_id;
    private int pageNumber;

    public LoadLatestMessagesTask(Activity activity, String url, String chatroom_id,
                                  int pageNumber) {
        super();
        this.activity = activity;
        this.url = url;
        this.chatroom_id = chatroom_id;
        this.pageNumber = pageNumber;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<String> doInBackground(Void... voids) {
        OkHttpClient client = new OkHttpClient();
        Response response = httpRequest(client, url, this.chatroom_id, "1");
        List<String> result_list = new ArrayList<>();
        try {
            if (response != null) {
                String result = response.body().string();
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.getString("status").equalsIgnoreCase("OK")) {
                    result_list.add(result);
                    int total_pages = jsonObject.getJSONObject("data").getInt("total_pages");
                    for (int i = 2; i <= total_pages - pageNumber + 1; i++) {
                        Response r = httpRequest(client, url, this.chatroom_id, String.valueOf(i));
                        String res = r.body().string();
                        JSONObject jObj = new JSONObject(res);
                        if (jObj.getString("status").equalsIgnoreCase("OK")) {
                            result_list.add(res);
                        }
                    }
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return result_list;
    }

    private Response httpRequest(OkHttpClient client, String url, String chatroom_id, String pagenum) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        urlBuilder.addQueryParameter("chatroom_id", chatroom_id);
        urlBuilder.addQueryParameter("page", pagenum);

        Request request = new Request.Builder()
                .url(urlBuilder.build().toString())
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    protected void onPostExecute(List<String> result_list) {
        if (result_list != null) {
            //((ChatActivity) activity).parseJsonListResponse(result_list);
        }
        //dialog.dismiss();
    }


}
