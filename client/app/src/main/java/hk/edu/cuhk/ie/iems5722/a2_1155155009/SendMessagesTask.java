package hk.edu.cuhk.ie.iems5722.a2_1155155009;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendMessagesTask extends AsyncTask<Void, Integer, String> {
    private ProgressDialog dialog;
    private String url;
    private Activity activity;
    private String chatroom_id;
    private String message;
    private long userID;
    private String username;

    public SendMessagesTask(Activity activity, String url, String chatroom_id, String message, long userID, String username) {
        super();
        this.activity = activity;
        this.url = url;
        this.chatroom_id = chatroom_id;
        this.message = message;
        this.userID = userID;
        this.username = username;

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
        RequestBody requestBody = new FormBody.Builder()
                .add("chatroom_id", chatroom_id)
                .add("user_id", String.valueOf(userID))
                .add("name", username)
                .add("message", message)
                .build();
        Request request = new Request.Builder()
                .url(this.url)
                .post(requestBody)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();

            String result = response.body().string();
            Log.d("LoadMessagesTask", result);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    protected void onPostExecute(String result) {
        ((ChatActivity) activity).parseSendJsonResponse(result);
        //dialog.dismiss();
    }


}
