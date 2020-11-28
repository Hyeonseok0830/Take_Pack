package com.example.takepack;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.Response;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private final long FINISH_INTERVAL_TIME = 2000;
    private long   backPressedTime = 0;


    LoginActivity lg = new LoginActivity();
    String m_ip = lg.mip;
    private GpsTracker gpsTracker;
    //map 부분
    private FragmentManager fragmentManager;
    private MapFragment mapFragment;
    private String title = "";
    private GoogleMap mMap;


    private MarkerOptions mop = new MarkerOptions();

    String add_name;
    int insert_count;
    String add_item_list;
    double add_lat;
    double add_lng;


    String user_id;

    public int getitem_count;
    public String[] location_name;
    public String[] item_name;
    public double[] marker_lat;
    public double[] marker_lng;
    String item_temp = "";
    LatLng c_location;

    String[] result_items;

    LinkedHashMap hash;
    LinkedHashMap hash2;
    ArrayList<Pair<Double, Double>> pairs;

    //list 부분
    public List<String> ListItems;
    CharSequence[] items;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    private LocationManager locationManager;
    private static final int REQUEST_CODE_LOCATION = 2;
    double current_lng, current_lat;
    // 쓰레드
    Thread t;
    Handler mHandler = null;

    private boolean stopped = false;
    public void stop() {
        stopped = true;
    }
    public void handler_start() {stopped = false; }
    private int second = 0;

    //진동
    boolean vib = false;
    Vibrator v;
    MarkerOptions markerOptions = new MarkerOptions();
    String dummy;

    boolean location_in = false;
    String msg = "잊은 물건이 있습니까?";

    Intent foreground_intent ;

    public void startVibrate() {
        v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(msg)
                .setMessage(dummy.substring(dummy.indexOf("#") + 1))
                .setPositiveButton("확인", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Log.i("알람", "종료");
                        stopVibrate();

                    }
                })
                .setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void stopVibrate() {
        v.cancel();
        //handler_start();



    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        String sid=pref.getString("id_save", "");
//        System.out.println("&&****"+sid);
//        if(false) {
//            Intent Mintent = getIntent();
//            user_id = Mintent.getExtras().getString("uid");
//            if(user_id.equals(null))
//            user_id = sid;
//        }
        user_id= sid;

        foreground_intent= new Intent(this, ForegroundService.class);

        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(foreground_intent);
        } else {
            startService(foreground_intent);
        }
        hash2 = new LinkedHashMap<String, String>();
        new init_Marker_Get().execute(m_ip + "/marker?id=" + user_id);

        super.onCreate(savedInstanceState);

        //사용자의 현재 위치
        setContentView(R.layout.activity_main);
        mapload();

        gpsTracker = new GpsTracker(MainActivity.this);
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i("Thread", "시작");
                while (!stopped) {
                    second+=3;
                    Log.i("Thread", "작동중 "+second+ "초"); //배포시 삭제
                    current_lat = gpsTracker.getLatitude();
                    current_lng = gpsTracker.getLongitude();
                    dummy = dis(current_lat, current_lng);
                    Log.i("Thread", "" + current_lat + "," + current_lng);
                    Log.i("dis전체 결과", dummy);
                    String[] s = dummy.split("$");
                  //  Log.i("dis결과 result는?", dummy.substring(0, 3));
                    if (dummy.startsWith("iin")) { // 들어왔을때
                        msg = "미리 등록한 소지품들을 챙겼습니까?";
                        startVibrate();
                        stop();
                        location_in = true;
                    } else if (dummy.startsWith("out") && location_in) { // 나갔을때
                        msg = "잊으신 물건은 없습니까?";
                        startVibrate();
                        stop();
                        location_in = false;
                    } else if ((dummy.startsWith("out") || dummy.startsWith("nul")) && location_in) { // 아무것도 아닐때
                        location_in = false;
                    }
                    try {
                        Thread.sleep(3000);//3초 실제 배포 시 30초로 바꾸기
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 10000); //5초 뒤 쓰레드 시작

        fragmentManager = getFragmentManager();
        mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(this);

        pairs = new ArrayList<>();
        hash = new LinkedHashMap<String, String>();

        Button listmode = (Button) findViewById(R.id.listMode);
        ListItems = new ArrayList<>();
        listmode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ItemListActivity.class);
                intent.putExtra("user_id", user_id);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

    }
    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime)
        {
            //연속 두번 backbtn 눌렀을 때 (INTERVAL 2초)
            super.onBackPressed();
            mHandler.removeCallbacksAndMessages(null);
            this.stopService(foreground_intent);
            android.os.Process.killProcess(android.os.Process.myPid());

        }
        else
        {
            backPressedTime = tempTime;
        }
    }

    //마커와 내 위치들 간 거리 비교
    public String dis(double my_lat, double my_lng) {
        String result = "null";
        String in_location_name = "";
        String in_item_name = "";
        if (getitem_count > 0) {
            String[] result_t = new String[getitem_count];
            for (int i = 0; i < location_name.length; i++) {
                result_t[i] = (distance(my_lat,my_lng,marker_lat[i],marker_lng[i])<10.0) ? "in" : "out"; //5m 범위 내 들어 왔을 시 in, 이탈 했을 시 out
                if (result_t[i].equals("in")) {
                    in_location_name = "$" + location_name[i] + "#";
                    in_item_name += item_name[i] + ",";
                } else if (result_t[i].equals("out")) {
                    in_location_name = "$" + location_name[i] + "#";
                   // System.out.println(hash2.get(location_name[i]).toString());
                    in_item_name += item_name[i] + ",";
                }
            }
            int incount = 0;
            int outcount = 0;
            for (int i = 0; i < location_name.length; i++) {
                if (result_t[i].equals("in")) {
                    incount++;
                    result = "iin";
                } else if (result_t[i].equals("out")) {
                    outcount++;
                    result = "out";
                }
            }
//            if (location_name.length == incount)
//                result = "iin";
//            else if (location_name.length == outcount)
//                result = "out";

            return result + in_location_name + in_item_name;
        } else
            return result + "$" + in_location_name + "#" + in_item_name;
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) +
                Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                        Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1609.344; // m 단위로 변경
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0/Math.PI);
    }

    public void mapload() {
        new list_Get().execute(m_ip + "/list?id="+user_id);
    }

    public void mainload() {
        new init_Marker_Get().execute(m_ip + "/marker?id=" + user_id);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        // mainload();
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng point) {
                //mapload();

                c_location = new LatLng(point.latitude, point.longitude); //커스텀 위치
                add_lat = point.latitude;
                add_lng = point.longitude;
                final MarkerOptions mop = new MarkerOptions();
                items = ListItems.toArray(new String[ListItems.size()]);

                final List SelectedItems = new ArrayList();
                final EditText edittext = new EditText(MainActivity.this);

                edittext.setHint("장소이름 추가");
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                if (ListItems.size() == 0)
                {
                    builder.setTitle("장소추가(아이템을 추가하면 함께 등록할 수 있습니다.)");
                }
                else {
                    builder.setTitle("장소추가");
                }
                builder.setView(edittext);

                builder.setMultiChoiceItems(items, null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    //사용자가 체크한 경우 리스트에 추가
                                    SelectedItems.add(which);
                                } else if (SelectedItems.contains(which)) {
                                    //이미 리스트에 들어있던 아이템이면 제거
                                    SelectedItems.remove(Integer.valueOf(which));
                                }
                            }
                        });
                builder.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String msg = "";
                                String temp = "";
                                for (int i = 0; i < SelectedItems.size(); i++) {
                                    int index = (int) SelectedItems.get(i);
                                    msg = msg + "\n" + (i + 1) + " : " + ListItems.get(index);
                                    temp += ListItems.get(index) + ",";
                                }
                                mop.title(edittext.getText().toString());
                                if(temp.length()>0)
                                    temp = temp.substring(0, temp.length() - 1);
                                else {
                                    Toast.makeText(getApplicationContext(), "아이템을 선택해 주세요", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                mop.snippet(temp);
                                mop.position(c_location);
                                googleMap.addMarker(mop);
                                add_name = edittext.getText().toString();
                                insert_count = SelectedItems.size();
                                add_item_list = temp;//nodejs에서 split후 string[] 에 담아 insert사용
                                Toast.makeText(getApplicationContext(),
                                        "Total " + SelectedItems.size() + " Items Selected.\n" + msg, Toast.LENGTH_LONG)
                                        .show();
                                new add_Marker_Post().execute(m_ip + "/add_marker");
                            }
                        });
                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.show();
            }

        });
        for (int i = 0; i < getitem_count; i++) {
            if (hash.containsKey(location_name[i]))//장소이름이 중복될 경우
            {
                item_temp += item_name[i] + ",";
                hash.put(location_name[i], item_temp);
            } else//새로운 장소를 추가하는 경우
            {
                item_temp = "";
                item_temp += item_name[i] + ",";
                hash.put(location_name[i], item_temp);
                pairs.add(new Pair<>(marker_lat[i], marker_lng[i]));

            }
        }
        MarkerOptions m = new MarkerOptions();
        Set<Map.Entry<String, String>> entries = hash.entrySet();
        int x = 0;
        for (Map.Entry<String, String> entry : entries) {
            System.out.print("key: " + entry.getKey());
            System.out.println(", Value: " + entry.getValue());
            m.title(entry.getKey())
                    .snippet(entry.getValue().substring(0, entry.getValue().length() - 1))
                    .position(new LatLng(pairs.get(x).first, pairs.get(x).second));
            googleMap.addMarker(m);
            x++;
        }
        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();
        current_lat = latitude;
        current_lng = longitude;

        LatLng location = new LatLng(current_lat,current_lng); //현재 내 위치
        markerOptions.title("현재 내 위치");
//        markerOptions.snippet("스니펫");
        markerOptions.position(location);
//        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.myposition);
//        Bitmap b=bitmapdraw.getBitmap();
//        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 75, 75, false);
//        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
//       // markerOptions.alpha(0.8f);
//        googleMap.addMarker(markerOptions);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
    }

    public class list_Get extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... urls) {

            HttpURLConnection con = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(urls[0]);
                con = (HttpURLConnection)url.openConnection();
                con.connect();
                InputStream stream = con.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) buffer.append(line);
                return buffer.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                con.disconnect();
                try {
                    if(reader != null)
                        reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                //String code = jsonObject.getString("code");
                String r_item = jsonObject.getString("item");
                result_items = r_item.split("#");
                ListItems.clear();
                for (int a = 1; a < result_items.length; a++) {
                    if (!ListItems.contains(result_items[a]))
                        ListItems.add(result_items[a]);
                }
//                Adapter.notifyDataSetChanged();

//                if (code.equals("200")) {
//                    Toast.makeText(getApplicationContext(), r_item, Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getApplicationContext(), "실패", Toast.LENGTH_SHORT).show();
//                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
    //서버 통신 부분
    public class init_Marker_Get extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", user_id);
                HttpURLConnection con = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL(urls[0]);//url을 가져온다.
                    con = (HttpURLConnection) url.openConnection();
                    con.connect();//연결 수행
                    //입력 스트림 생성
                    InputStream stream = con.getInputStream();

                    //속도를 향상시키고 부하를 줄이기 위한 버퍼를 선언한다.
                    reader = new BufferedReader(new InputStreamReader(stream));

                    //실제 데이터를 받는곳
                    StringBuffer buffer = new StringBuffer();

                    //line별 스트링을 받기 위한 temp 변수
                    String line = "";

                    //아래라인은 실제 reader에서 데이터를 가져오는 부분이다. 즉 node.js서버로부터 데이터를 가져온다.
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }

                    //다 가져오면 String 형변환을 수행한다. 이유는 protected String doInBackground(String... urls) 니까
                    return buffer.toString();

                    //아래는 예외처리 부분이다.
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    //종료가 되면 disconnect메소드를 호출한다.
                    if (con != null) {
                        con.disconnect();
                    }
                    try {
                        //버퍼를 닫아준다.
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }//finally 부분
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String code = jsonObject.getString("code");
                String msg = jsonObject.getString("message");
                String itemlist = jsonObject.getString("item");
                JSONArray getitem = new JSONArray(itemlist);

                getitem_count = getitem.length();

                location_name = new String[getitem_count];
                item_name = new String[getitem_count];
                marker_lat = new double[getitem_count];
                marker_lng = new double[getitem_count];
                for (int i = 0; i < getitem_count; i++) {
                    JSONObject itemobject = getitem.getJSONObject(i);
                    location_name[i] = itemobject.getString("name");
                    item_name[i] = itemobject.getString("item_name");
                    marker_lat[i] = itemobject.getDouble("lat");
                    marker_lng[i] = itemobject.getDouble("lng");
                    System.out.println(item_name[i]);
                }

                if (code.equals("200")) {
                    Toast.makeText(getApplicationContext(), "마커정보받아옴", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getApplicationContext(), "실패", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class add_Marker_Post extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                JSONObject jsonObject = new JSONObject();
                System.out.println(user_id+","+add_name+","+add_item_list+","+add_lat+","+add_lng+","+insert_count);
                jsonObject.put("id", user_id);
                jsonObject.put("name", add_name);
                jsonObject.put("item_list", add_item_list);
                jsonObject.put("lat", add_lat);
                jsonObject.put("lng", add_lng);
                jsonObject.put("count", insert_count);
                HttpURLConnection con = null;
                BufferedReader reader = null;

                try {
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
            try {
                JSONObject jsonObject = new JSONObject(result);
                System.out.println("json" + jsonObject);
                String code = jsonObject.getString("code");
                System.out.println("code" + code);
                String msg = jsonObject.getString("message");
                System.out.println("msg" + msg);
                if (code.equals("200")) {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                } else {
                    // Toast.makeText(getApplicationContext(), "실패", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}
