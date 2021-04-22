package hk.edu.cuhk.ie.iems5722.a2_1155148594;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hk.edu.cuhk.ie.iems5722.a2_1155148594.R;

import static android.content.Context.MODE_PRIVATE;

public class MessageListAdapter extends BaseAdapter {

    private List<Message> messageList;
    private Context context;
    private String user_id;


    public MessageListAdapter(Context context) {
        this.messageList = new ArrayList<>();
        this.context = context;
        SharedPreferences pref = this.context.getSharedPreferences("personal_info", MODE_PRIVATE);
        user_id = pref.getString("user_id", "");
    }

    public void clearAdapter() {
        messageList.clear();
        notifyDataSetChanged();
    }

    public void add(Message message){
        this.messageList.add(message);
        notifyDataSetChanged();
    }

    public void addList(List<Message> list) {
        this.messageList.addAll(list);
    }

    public void addListToFirst(List<Message> list) {
        this.messageList.addAll(0, list);
    }
    public void addToFirst(Message message) {
        this.messageList.add(0, message);
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageViewHolder holder = new MessageViewHolder();
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Message message = messageList.get(position);

        convertView = messageInflater.inflate(R.layout.layout_message, null);
        if (!String.valueOf(message.getUser_id()).trim().equals(user_id)) {
            LinearLayout message_form = convertView.findViewById(R.id.message_form);
            message_form.setBackgroundResource(R.drawable.from_message_shape);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)message_form.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,0);
            message_form.setLayoutParams(params);

        }
        holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
        holder.messageTime = (TextView) convertView.findViewById(R.id.message_time);
        holder.messageUsername = (TextView) convertView.findViewById(R.id.message_username);
        holder.messageBody.setText(message.getMessage());

        try {
            SimpleDateFormat sdf3 = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
            holder.messageTime.setText(new SimpleDateFormat("MM-dd HH:mm")
                    .format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(message.getMessage_time())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.messageUsername.setText("user: " + message.getName());
        return convertView;
    }
}

class MessageViewHolder {
    public TextView messageBody;
    public TextView messageTime;
    public TextView messageUsername;
}
