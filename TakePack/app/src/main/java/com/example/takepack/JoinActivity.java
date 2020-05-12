package com.example.takepack;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_CODE_MENU) {
//            if (resultCode == RESULT_OK) {
//                String menu = data.getExtras().getString("menu");
//                Toast.makeText(getApplicationContext(), "응답으로 전달된 menu :" + menu, Toast.LENGTH_LONG).show();
//            }
//        }
//    }
public class JoinActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_MENU = 101;
    String userid = "1";
    String userpw = "1";
    EditText id;
    EditText name;
    EditText pw;
    EditText pwc;
    EditText email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        id = (EditText) findViewById(R.id.jid);
        name = (EditText) findViewById(R.id.jname);
        pw = (EditText) findViewById(R.id.jpw);
        pwc = (EditText) findViewById(R.id.jpwc);
        email = (EditText) findViewById(R.id.jemail);
    }

    public void submit(View view) {
        if (pw.getText().toString().equals(pwc.getText().toString())) {
            new Post().execute("http://192.168.219.121:3000/user/join");
            finish();
        } else Toast.makeText(JoinActivity.this, "비밀번호를 확인해주세요.", Toast.LENGTH_LONG).show();
    }

    public void cancel(View view) {
        finish();
        Toast.makeText(JoinActivity.this, "cancel", Toast.LENGTH_LONG).show();
    }

//Toast.makeText(JoinActivity.this, "put", Toast.LENGTH_LONG).show();
    // 안스에서 노드js로 데이터 보내는 부분
    public class Post extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                JSONObject jsonObject = new JSONObject();
                /*jsonObject.put("user_id", "androidTest");
                jsonObject.put("name", "yun");*/

                jsonObject.put("id", id.getText().toString());
                jsonObject.put("name", name.getText().toString());
                jsonObject.put("pw", pw.getText().toString());
                jsonObject.put("email", email.getText().toString());

                HttpURLConnection con = null;
                BufferedReader reader = null;

                try {
                    //URL url = new URL("http://192.168.25.16:3000/users");
                    URL url = new URL(urls[0]);
                    //연결을 함
                    con = (HttpURLConnection) url.openConnection();

                    con.setRequestMethod("POST");//POST방식으로 보냄
                    con.setRequestProperty("Cache-Control", "no-cache");//캐시 설정
                    con.setRequestProperty("Content-Type", "application/json");//application JSON 형식으로 전송
                    con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                    con.setDoOutput(true);//Outstream으로 post 데이터를 넘겨주겠다는 의미
                    con.setDoInput(true);//Inputstream으로 서버로부터 응답을 받겠다는 의미
                    con.connect();

                    //서버로 보내기위해서 스트림 만듬
                    OutputStream outStream = con.getOutputStream();
                    //버퍼를 생성하고 넣음
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    writer.write(jsonObject.toString());
                    writer.flush();
                    writer.close();//버퍼를 받아줌

                    //서버로 부터 데이터를 받음
                    InputStream stream = con.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();

                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }

                    return buffer.toString();//서버로 부터 받은 값을 리턴해줌 아마 OK!!가 들어올것임

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                    try {
                        if (reader != null) {
                            reader.close();//버퍼를 닫아줌
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //  testpost.setText(result);//서버로 부터 받은 값을 출력해주는 부분
            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("code");

                if ( msg.equals("200")){
                    Toast.makeText(getApplicationContext(),"회원가입 성공",Toast.LENGTH_SHORT).show();
                    finish();
                }
                else {
                    Toast.makeText(getApplicationContext(),"회원가입 실패",Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            String x = result.substring(result.indexOf(":")+1,result.indexOf(","));
//            if(x.equals("404"))
//                Toast.makeText(getApplicationContext(), x, Toast.LENGTH_SHORT).show();
//            else
//                Toast.makeText(getApplicationContext(), x, Toast.LENGTH_SHORT).show();

        }


    }
}