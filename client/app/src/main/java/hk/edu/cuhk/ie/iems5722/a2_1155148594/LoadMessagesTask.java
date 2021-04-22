package hk.edu.cuhk.ie.iems5722.a2_1155148594;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;


import java.io.IOException;
import java.util.Objects;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoadMessagesTask extends AsyncTask<Void, Integer, String> {
    private final String url;
    @SuppressLint("StaticFieldLeak")
    private final Activity activity;
    private final String chatroom_id;
    private final int pageNumber;
    private final boolean isRefresh;

    public LoadMessagesTask(Activity activity, String url, String chatroom_id, int pageNumber, boolean isRefresh) {
        super();
        this.activity = activity;
        this.url = url;
        this.chatroom_id = chatroom_id;
        this.pageNumber = pageNumber;
        this.isRefresh = isRefresh;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Create a progress dialog
        //dialog = new ProgressDialog(activity);
        // Set progress dialog title
        //dialog.setTitle("ListView Load More Tutorial");
        // Set progress dialog message
        //dialog.setMessage("Loading more...");
        //dialog.setIndeterminate(false);
        // Show progress dialog
        //dialog.show();
    }

    @Override
    protected String doInBackground(Void... voids) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        urlBuilder.addQueryParameter("chatroom_id", this.chatroom_id);
        urlBuilder.addQueryParameter("page", String.valueOf(pageNumber));

        Request request = new Request.Builder()
                .url(urlBuilder.build().toString())
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();

            assert response.body() != null;
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    protected void onPostExecute(String result) {
        if (isRefresh) {
            ((ChatActivity) activity).refresh(result);
        } else {
            ((ChatActivity) activity).parseJsonResponse(result);
        }
    }
}
