package com.example.takepack;


import android.content.ClipData;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemListActivity extends AppCompatActivity {


    MainActivity list;

    ArrayAdapter<String> Adapter;
    ListView listView;
    Button btnAdd, btnDel;
    EditText editText;
    String user_id;
    String[] result_item ;
    String item_name;
    String del_item;
    ArrayList Items;
    List<String> ItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemlist);
        Intent i = getIntent();
        user_id = i.getExtras().getString("user_id");
        new Item_Post().execute("http://172.20.10.3:3000/list");


        list = new MainActivity();
        //list부분 메인부분으로 옮겨보기
        Items = new ArrayList<String>();
        Adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice, Items);
        listView = (ListView) findViewById(R.id.listView1);
        listView.setAdapter(Adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
//Main과 동시에 실행되어야함
//        for(int a=0;a<result_item.length;a++)
//            Items.add(result_item[a]);
//        Adapter.notifyDataSetChanged();

        editText = (EditText) findViewById(R.id.editText);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnDel = (Button) findViewById(R.id.btnDel);

        btnAdd.setOnClickListener(listener);
        btnDel.setOnClickListener(listener);

        ItemList= new ArrayList<>();


    }
    @Override
    public void onBackPressed() {

        super.onBackPressed();

        System.out.println("뒤로가기 버튼 누름");

        Intent intentHome = new Intent(this, MainActivity.class);
        intentHome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentHome.putExtra("uid",user_id);
        startActivity(intentHome);



//        new Item_Post().execute("http://192.168.219.121:3000/list");
//
//        for(int i=0;i<result_item.length;i++)
//        {
//            if(!ItemList.contains(result_item[i]))
//                ItemList.add(result_item[i]);
//        }
//        if(list.ListItems.size()!=ItemList.size())
//            System.out.println("TLqkf");
//        list.ListItems=ItemList;
//        System.out.println("리스트 -> Main 리스트 복사"+list.ListItems.get(1));
    }


    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnAdd:
                    //서버통신

                    item_name = editText.getText().toString();
                    if (item_name.length() != 0) {
                        Items.add(item_name);

                        editText.setText("");

                        Adapter.notifyDataSetChanged();

                        new add_Item_Post().execute("http://172.20.10.3:3000/add_item");
                    }
                    break;
                case R.id.btnDel:
                    //서버통신
                    int pos;
                    pos = listView.getCheckedItemPosition();
                    if (pos != ListView.INVALID_POSITION) {
                        del_item=Adapter.getItem(pos);
                        Toast.makeText(getApplicationContext(), del_item, Toast.LENGTH_SHORT).show();
                        new del_Item_Post().execute("http://172.20.10.3:3000/del_item");
                        Items.remove(pos);
                        listView.clearChoices();
                        Adapter.notifyDataSetChanged();

                    }
                    break;
            }
        }
    };
    public class add_Item_Post extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("item", item_name);
                jsonObject.put("id", user_id);

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
//          Log.d("reslut",result);
            //    String x = result.substring(result.indexOf(":")+1,result.indexOf(","));
            try {
                JSONObject jsonObject = new JSONObject(result);
                String code = jsonObject.getString("code");
                String msg = jsonObject.getString("message");
                if (code.equals("200")) {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "실패", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public class del_Item_Post extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("item", del_item);
                jsonObject.put("id", user_id);

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
//          Log.d("reslut",result);
            //    String x = result.substring(result.indexOf(":")+1,result.indexOf(","));
            try {
                JSONObject jsonObject = new JSONObject(result);
                String code = jsonObject.getString("code");
                String msg = jsonObject.getString("message");
                if (code.equals("200")) {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "실패", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public class Item_Post extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", user_id);
//                jsonObject.put("pw", pw.getText().toString());


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
//          Log.d("reslut",result);
            //    String x = result.substring(result.indexOf(":")+1,result.indexOf(","));
            try {
                JSONObject jsonObject = new JSONObject(result);
                String code = jsonObject.getString("code");
                String r_item = jsonObject.getString("item");
                result_item = r_item.split("#");
                for(int a=0;a<result_item.length;a++) {
                    {
                        if(!Items.contains(result_item[a]))
                            Items.add(result_item[a]);
                        //     list.ListItems.add(result_item[a]);
                    }
                }
                  Adapter.notifyDataSetChanged();
                if (code.equals("200")) {
                    //Toast.makeText(getApplicationContext(), r_item, Toast.LENGTH_SHORT).show();
                    System.out.println("아이템리스트 엑티비티 성공적으로 열었음");
                } else {
                    Toast.makeText(getApplicationContext(), "실패", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }



}
