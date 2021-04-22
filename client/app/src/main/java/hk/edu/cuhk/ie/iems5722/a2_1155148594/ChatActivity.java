package hk.edu.cuhk.ie.iems5722.a2_1155148594;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class ChatActivity extends AppCompatActivity {
    private EditText editText_message;
    private ListView listView;
    private String chatroom_id;
    private boolean isLoading = true;
    private int page_number = 1;
    private MessageListAdapter adapter;
    private ProgressDialog progressDialog;
    private Socket socket;
    private long user_id;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        SharedPreferences pref = getSharedPreferences("personal_info", MODE_PRIVATE);
        user_id = pref.getLong("user_id", 0);
        username = pref.getString("name", "");

        listView = findViewById(R.id.listview_message_list);
        editText_message = findViewById(R.id.edit_text_message);
        ImageButton button_send = findViewById(R.id.button_message_send);
        chatroom_id = getIntent().getStringExtra("c_id");

        // set up title for action bar and back button
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(getIntent().getStringExtra("c_name"));
        actionBar.setDisplayHomeAsUpEnabled(true);

        // connect to server through socket_io
        try {
            socket = IO.socket("http://118.195.180.134:9001");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        socket.on("my response", onMyResponse); //listen to "my response" event
        socket.connect();
        socket.emit("join", chatroom_id); //join room

        // set up adapter for list view
        adapter =new MessageListAdapter(this);
        listView.setAdapter(adapter);

        // retrieve latest message
        refreshClicked();

        // set up send button to send message
        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editText_message.getText().toString();
                if (message.length() > 0 && message.length() <= 200) {
                    new SendMessagesTask(ChatActivity.this
                            ,"http://118.195.180.134:9000/api/a3/send_message"
                            , chatroom_id, message, user_id, username).execute();
                }
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Log.d("chatact", firstVisibleItem + " " + visibleItemCount + " " + totalItemCount);
                if(firstVisibleItem == 0 && totalItemCount != 0 && !isLoading) {
                    isLoading = true;
                    loadMessage(false);
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        socket.emit("leave", chatroom_id);
        socket.disconnect();
        socket.off("my response", onMyResponse);
        super.onDestroy();
    }

    private final Emitter.Listener onMyResponse = new Emitter.Listener() {

        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            try {
                 if (data.getString("status").equalsIgnoreCase("OK")) {
                    Log.d("Websocket", data.toString());
                    Gson gson = new Gson();
                    final Message m = gson.fromJson(data.getString("data"), Message.class);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(ChatActivity.this)
                            .setSmallIcon(R.drawable.ic_message)
                            .setContentTitle("New Message")
                            .setContentText(m.getName() + " send you a message!")
                            .setAutoCancel(true);
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setComponent(new ComponentName(ChatActivity.this, ChatActivity.class));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    PendingIntent pendingIntent = PendingIntent
                            .getActivity(ChatActivity.this, 0, intent, 0);
                    builder.setContentIntent(pendingIntent);
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(0, builder.build());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.add(m);
                            scrollToBottom();
                        }
                    });

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    private void loadMessage(boolean isRefresh) {
        new LoadMessagesTask(
                this,
                "http://118.195.180.134:9000/api/a3/get_messages",
                chatroom_id,
                page_number, isRefresh).execute();
    }

    public void updateListView(List<Message> list) {
        int firstVisibleItem = listView.getFirstVisiblePosition();
        int oldCount = adapter.getCount();
        View view = listView.getChildAt(0);
        int pos = (view == null ? 0 : view.getBottom());
        adapter.addListToFirst(list);
        listView.setSelectionFromTop(firstVisibleItem + adapter.getCount() - oldCount + 1, pos);
        isLoading = false;
        page_number++;
    }

    public void parseSendJsonResponse(String result) {
        if (result == null ){
            Toast.makeText(this, "Failed to Send Message", Toast.LENGTH_SHORT).show();
        } else {
            editText_message.getText().clear();
        }
    }

    public void refresh(String result) {
        adapter.clearAdapter();
        parseJsonResponse(result);
        progressDialog.dismiss();
    }

    public void parseJsonResponse(String result){
        try {
            if (result == null) {
                Toast.makeText(this, "Fail to Load Messages", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("parseJsonResponse", result);
            JSONObject jsonObject = new JSONObject(result);
            Log.d("chatAct", jsonObject.toString());
            if (jsonObject.getString("status").equalsIgnoreCase("OK")) {
                JSONObject data = jsonObject.getJSONObject("data");
                JSONArray messages = data.getJSONArray("messages");
                Gson gson = new Gson();
                List<Message> list = new ArrayList<>();
                for (int i = 0; i < messages.length(); i++) {
                    Log.d("LoadMessage", messages.getString(i));
                    final Message m = gson.fromJson(messages.getString(i), Message.class);
                    m.setName(m.getName());
                    list.add(0, m);
                }
                updateListView(list);
            } else {
                Toast.makeText(this, "No More Earlier Messages", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void scrollToBottom(){
        //I think this is supposed to run on the UI thread
        listView.setSelection(adapter.getCount() - 1);
    }

    private void refreshClicked() {
        progressDialog = new ProgressDialog(ChatActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        adapter.clearAdapter();
        page_number = 1;
        loadMessage(true);
    }
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.button_refresh:
                refreshClicked();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_button, menu);
        return true;
    }
}
