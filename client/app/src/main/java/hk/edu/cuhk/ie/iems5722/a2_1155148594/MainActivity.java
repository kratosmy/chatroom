package hk.edu.cuhk.ie.iems5722.a2_1155148594;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hk.edu.cuhk.ie.iems5722.a2_1155148594.Util.HttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private String user_id;
    private final String URL = "http://118.195.180.134:9000/api/a3/";
    private final String GET_CHATROOM_URL = URL + "get_chatrooms";
    private SharedPreferences pref;
    private List<String> conversationList;
    private List<String> conversation_id_list;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = getSharedPreferences("personal_info", MODE_PRIVATE);
        user_id = pref.getString("user_id", "");
        ListView listview_conversationlist = findViewById(R.id.listview_conversationlist);

        conversationList = new ArrayList<>();
        conversation_id_list = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(this,
                        R.layout.layout_coversation, R.id.conversation_name, conversationList);
        listview_conversationlist.setAdapter(arrayAdapter);
        listview_conversationlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("c_name", conversationList.get(position));
                intent.putExtra("c_id", conversation_id_list.get(position));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchChatRoom();
    }

    private void fetchChatRoom() {
        HttpUtil.sendOkHttpGetRequest(GET_CHATROOM_URL
                , new HashMap<String, String>() {{ put("user_id", user_id); }}
                , new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,
                                        "Cannot Retrieve Chatroom List", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        try {
                            assert response.body() != null;
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            Log.d("MainActivity","chatroom list: " + jsonObject.toString());
                            if (jsonObject.getString("status").equalsIgnoreCase("OK")) {
                                JSONArray jsonArray = new JSONArray(jsonObject.getString("data"));
                                conversationList.clear();
                                conversation_id_list.clear();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject obj = jsonArray.getJSONObject(i);
                                    conversationList.add(obj.getString("name"));
                                    conversation_id_list.add(obj.getString("id"));
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        arrayAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
