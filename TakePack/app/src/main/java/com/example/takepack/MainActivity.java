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
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import java.util.Set;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {


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
    public double m_lat = 0.0;
    public double m_lng = 0.0;
    LatLng c_location;

    String[] result_items;

    LinkedHashMap hash;
    ArrayList<Pair<Double, Double>> pairs;

    //list 부분
    public List<String> ListItems;
    CharSequence[] items;
//gps
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};


    private LocationManager locationManager;
    private static final int REQUEST_CODE_LOCATION = 2;
    double current_lng, current_lat;
// 쓰레드
    private boolean stopped = false;
    public void stop()
    {
        stopped=true;
    }
    public void n_stop()
    {
        stopped=false;

    }

//진동
    boolean vib = false;
    Vibrator v;
    MarkerOptions markerOptions = new MarkerOptions();
    String dummy;

    boolean location_in = false;
    String msg = "이거 챙겼어?";

    public void startVibrate(){
        v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(msg)
                .setMessage(dummy.substring(dummy.indexOf("#")+1))
                .setPositiveButton("확인", new AlertDialog.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        Log.i("알람" ,"종료");
                        stopVibrate();

                    }
                })
                .setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void stopVibrate(){
        v.cancel();
        n_stop();
        recreate();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        Intent Mintent = getIntent();
        user_id = Mintent.getExtras().getString("uid");
        

        SharedPreferences pref=getSharedPreferences("pref", Activity.MODE_PRIVATE);
        user_id=pref.getString("id_save", "");



        new init_Marker_Get().execute("http://" + m_ip + "/marker?id=" + user_id);
        super.onCreate(savedInstanceState);


        //사용자의 현재 위치

        mapload();
        setContentView(R.layout.activity_main);
        if (!checkLocationServicesStatus()) {

            showDialogForLocationServiceSetting();
        } else {

            checkRunTimePermission();
        }
        gpsTracker = new GpsTracker(MainActivity.this);

        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(new Runnable() {
    //    Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("Thread", "시작");
                while (!stopped) {
                    current_lat = gpsTracker.getLatitude();
                    current_lng = gpsTracker.getLongitude();
                    dummy = dis(current_lat, current_lng);
                    Log.i("Thread", "" + current_lat + "," + current_lng);
                    Log.i("dis전체 결과", dummy);
                    String[] s = dummy.split("$");
                    Log.i("dis결과 result는?", dummy.substring(0, 3));
                    if (dummy.substring(0, 3).equals("iin")) { // 들어왔을때
                        //Log.i("위치이름", s[1] + "에 ");
                        //Log.i("아이템", s[2] + ": 이거 챙겨왔냐?");
                        msg = "이거 챙겨왔어?";
                        startVibrate();
                        stop();
                        location_in = true;

                    } else if (dummy.substring(0, 3).equals("out") && location_in == false) { // 나갔을때

                        //챙겼냐?
                        //Log.i("위치이름", s[1] + "에서 ");
                        //Log.i("아이템", s[2] + ": 이거 챙겼냐?");
                        msg = "이거 챙겼어?";
                        startVibrate();
                        stop();
                        location_in = false;
                    } else if ((dummy.substring(0, 3).equals("out")||dummy.substring(0, 3).equals("nul")) && location_in == false) { // 아무것도 아닐때
                        //아무일도 일어나지 않음
                        //Log.i("위치이름", s[1] + "에서 ");
                        Log.i("아이템", " 이거 챙겼냐?");
                        location_in = false;
                    }
                    try {
                       Thread.sleep(5000);//3초 실제 배포 시 30초로 바꾸기
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        },50000);


        Intent intent = new Intent(this, ForegroundService.class);

        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
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
        Log.i("Thread", "진짜시작" + getitem_count);
      //  th.start();
    }

    //마커와 내 위치가 가깝나?
    public String dis(double my_lat, double my_lng) {
        String result = "null";
        String in_location_name = "";
        String in_item_name = "";
        if (getitem_count > 0) {
            Log.i("되나?","아이템 갯수 받아와지나?");
            String[] result_t = new String[getitem_count];
            for (int i = 0; i < location_name.length; i++) {
                result_t[i] = (Math.abs(marker_lat[i] - my_lat) < 0.0000001 || Math.abs(marker_lng[i] - my_lng) < 0.00000001) ? "in" : "out";
                if (result_t[i].equals("in")) {
                    in_location_name = "$"+location_name[i]+"#";
                    in_item_name += item_name[i] + ",";
                }
                else if(result_t[i].equals("out"))
                {
                    in_location_name = "$"+location_name[i]+"#";
                    in_item_name += item_name[i] + ",";
                }
            }
            int incount = 0;
            int outcount = 0;
            for (int i = 0; i < location_name.length; i++) {
                if (result_t[i].equals("in")) {
                    incount++;
                    result="iin" ;
                }
                else if (result_t[i].equals("out")) {
                    outcount++;
                    result="out";
                }
            }
//            if (location_name.length == incount)
//                result = "iin";
//            else if (location_name.length == outcount)
//                result = "out";

            return result  + in_location_name  + in_item_name;
        }
        else
            return  result + "$" + in_location_name + "#" + in_item_name;
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {

                //위치 값을 가져올 수 있음
                ;
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                } else {

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    void checkRunTimePermission() {

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음


        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void mapload() {
        new list_Post().execute("http://" + m_ip + "/list");
    }

    public void mainload() {
        new init_Marker_Get().execute("http://" + m_ip + "/marker?id=" + user_id);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        // mainload();
        mMap = googleMap;
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
                if (ListItems.size() == 0)
                    System.out.println("리스트 비어있음");

                final List SelectedItems = new ArrayList();
                final EditText edittext = new EditText(MainActivity.this);
                edittext.setHint("장소이름 추가");
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("장소추가");
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
                                temp = temp.substring(0, temp.length() - 1);
                                mop.snippet(temp);
                                mop.position(c_location);
                                googleMap.addMarker(mop);
                                add_name = edittext.getText().toString();
                                insert_count = SelectedItems.size();
                                add_item_list = temp;//nodejs에서 split후 string[] 에 담아 insert사용

                                Toast.makeText(getApplicationContext(),
                                        "Total " + SelectedItems.size() + " Items Selected.\n" + msg, Toast.LENGTH_LONG)
                                        .show();
                                new add_Maker_Post().execute("http://" + m_ip + "/add_marker");

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

        if (getitem_count == 0) {
            System.out.println("getitem_count이 0이다");
        }
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
                    System.out.println(url);
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

    public class list_Post extends AsyncTask<String, String, String> {
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
                result_items = r_item.split("#");
                ListItems.clear();
                //  System.out.println("리스트 포맷");
                for (int a = 0; a < result_items.length; a++) {
                    if (!ListItems.contains(result_items[a]))
                        ListItems.add(result_items[a]);
                }
//                Adapter.notifyDataSetChanged();
                if (code.equals("200")) {
                    Toast.makeText(getApplicationContext(), r_item, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "실패", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class add_Maker_Post extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", user_id);
                jsonObject.put("name", add_name);
                jsonObject.put("item_list", add_item_list);
                jsonObject.put("lat", add_lat);
                jsonObject.put("lng", add_lng);
                jsonObject.put("count", insert_count);
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
