package com.wab.im.ui.login;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wab.im.IMClient;
import com.wab.im.R;
import com.wab.im.message.Message;

public class ChatActivity extends AppCompatActivity {
    private static ChatActivity chatActivity;
    TextView textView;
    IMClient client;
    TextView messageview;
    Button sendBtn;
    EditText editText;
    Button cleanBtn;

    public ChatActivity() {
        chatActivity = this;
    }

    public static ChatActivity getMainActivity() {
        return chatActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        textView = findViewById(R.id.connect_status);
        messageview = findViewById(R.id.chat_content);
        sendBtn = findViewById(R.id.send);
        editText = findViewById(R.id.msg_input);
        cleanBtn = findViewById(R.id.clean);

        cleanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageview.setText("");
                Toast.makeText(ChatActivity.this, "清空完成", Toast.LENGTH_SHORT).show();
            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = editText.getText().toString();
                if (msg == null || msg.length() < 1) {
                    Toast.makeText(ChatActivity.this, "请输入你要发送的消息", Toast.LENGTH_SHORT).show();
                } else {
                    Message.IMMessage.Builder builder = Message.IMMessage.newBuilder();
                    Message.IMChatMessage chartMSG = Message.IMChatMessage.newBuilder()
                            .setMsgId(System.currentTimeMillis() + "")
                            .setBody(msg)
                            .setFrom(ConfigUtil.FROM_USERID)
                            .setType(Message.MessageType.TextMessage)
                            .setTo(ConfigUtil.TO_USERID)
                            .setNick("李德").build();
                    System.out.println("发送聊天消息 ---> " + chartMSG);
                    Message.IMMessage imMessage = builder.setDataType(Message.IMMessage.DataType.IMChatMessage).setChatMessage(chartMSG).build();
                    send(imMessage);
                    Toast.makeText(ChatActivity.this, "发送完成", Toast.LENGTH_SHORT).show();
                    editText.setText("");
                }
            }
        });

        Bundle bundle = getIntent().getExtras();
        String person = bundle.getString("person");
        if (person != null) {
            new Thread(() -> {
                client = new IMClient("106.13.25.41", 5280);
                try {
                    client.start();
                    //认证
                    client.auth(
                            "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1ODk5NTEwMDEsInVzZXJfbmFtZSI6ImFkbWluIiwiYXV0aG9yaXRpZXMiOlsiUk9PVCIsIlNZU1RFTSIsIlVTRVIiXSwianRpIjoiMGQ0ODU4NGYtNzIwNC00N2VkLThiMDItYTU0ZDA4YTVjYWE0IiwiY2xpZW50X2lkIjoic3lzdGVtX3Jvb3RfYXBwY2xpZW50Iiwic2NvcGUiOlsib3BlbmlkIl19.H1Ic0jlTCCjwL_psbxJXlbeiqezN226F2KlR1NB1jR5m1bIeNe0Ho64WAWGY3zxlKbeo4gcF2A-hy5whw9LjpBE4HNocM4FvnQwZvDZMaWMYmRbCooaTg8wVtav-tfHlz1yhmcyuDqAZ_pEG9Lrx2EhXSEFKQa6jxqDW2owLcITXRK6oK2Wl8KCV_-fiShKwt8eHoiqf-OxXzP4rhinKflKOCkywP7w3geWi1NcUAg71YgJBQ9fquzkB1nEaHtGAZP3qxA5OfVf-mvrcQdnP_5zt3AeMsTjJiaBX4Z74-AaxdAPf-Pm4cJEeSnzR4iMHEE06OsZy7tFC1KsTO6fjEA",
                            ConfigUtil.USER_ID
                    );
                    textView.setText("连接状态：已连接");
                } catch (Exception e) {
                    textView.setText("连接状态：连接失败 ---> " + e.getLocalizedMessage());
                }
            }).start();

        }
    }

    public void send(Message.IMMessage imMessage) {
        client.getChannel().writeAndFlush(imMessage);
    }

    private Handler msghandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 1) {
                String oldMsg = messageview.getText().toString() + "\n";
                String newMsg = ((Message.IMChatMessage) (msg.getData().getSerializable("msg"))).getBody();
                String from = ((Message.IMChatMessage) (msg.getData().getSerializable("msg"))).getFrom();
                messageview.setText(oldMsg + "从[" + from + "] 发来 [" + newMsg + "]");
            } else if (msg.what == 2) {
                String oldMsg = messageview.getText().toString() + "\n";
                String ackMsgId = ((Message.IMChatMessageACK) (msg.getData().getSerializable("msg"))).getAckMsgId();
                messageview.setText(oldMsg + "服务器已接收 --> [" + ackMsgId + "]");
            }

        }
    };

    public Handler getMsghandler() {
        return msghandler;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
