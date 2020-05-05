package com.example.takepack;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;



public class LoginActivity extends AppCompatActivity{
    public static final int REQUEST_CODE_MENU=101;
    String userid="identity";
    String userpw="password";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_CODE_MENU) {
//            if (resultCode == RESULT_OK) {
//                String menu = data.getExtras().getString("menu");
//                Toast.makeText(getApplicationContext(), "응답으로 전달된 menu :" + menu, Toast.LENGTH_LONG).show();
//            }
//        }
//    }

    public void login(View v){
        EditText id=(EditText)findViewById(R.id.eid);
        EditText pw=(EditText)findViewById(R.id.epw);

        if(id.getText().toString().equals(userid)){
            if (pw.getText().toString().equals(userpw)){
                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                startActivityForResult(intent,REQUEST_CODE_MENU);
            }
            else Toast.makeText(LoginActivity.this,"로그인 실패",Toast.LENGTH_LONG).show();
        }
        else Toast.makeText(LoginActivity.this,"로그인 실패",Toast.LENGTH_LONG).show();
    }

}
