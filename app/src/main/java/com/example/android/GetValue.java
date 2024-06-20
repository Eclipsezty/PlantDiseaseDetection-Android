package com.example.android;

import static java.lang.Integer.parseInt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class GetValue extends AppCompatActivity {

    private EditText ip,port;
    private Button over;
    static String ipAddress = " 192.168.224.217";
    static String portNumber = "6666";
    public String ipText;
    public String portText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_value);
        initView();
        initEvent();

    }

    public void initView() {
        ip=(EditText) findViewById(R.id.ip);
        port=(EditText) findViewById(R.id.port);
        over=(Button) findViewById(R.id.over);
    }

    public void initEvent(){
        ipText=ip.getText().toString();
        portText=port.getText().toString();

        over.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //MainActivity.setIP(ipText);
                //MainActivity.setPort((parseInt(portText)));
                initEvent();
                ipAddress=ipText;
                portNumber=portText;
                Toast.makeText(getApplicationContext(), "Change Success", Toast.LENGTH_LONG).show();
                Intent it=new Intent(GetValue.this,MainActivity.class);//启动MainActivity
                it.putExtra("ip", ipAddress);
                it.putExtra("port", portNumber);
                startActivity(it);
            }
        });
    }
}