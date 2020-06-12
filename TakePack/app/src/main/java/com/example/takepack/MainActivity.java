package com.example.takepack;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
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

    private GpsTracker gpsTracker;
    //map 부분
    private FragmentManager fragmentManager;
    private MapFragment mapFragment;
    private String title = "";
    private GoogleMap mMap;
    //    private CameraPosition mCameraPosition;
//
//    private GeoDataClient mGeoDataClient;
//    private PlaceDetectionClient mPlaceDetectionClient;
//
//    private FusedLocationProviderClient mFusedLocationClient;
//
//    private final LatLng mDefaultLocation = new LatLng(35.229626, 128.578007);
//    private static final int DEFAULT_ZOOM=16;
//    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION=1;
//    private boolean mLocationPermissionGranted;
//
//    private Location mLastKnownLocation;
//
//    private static final String KEY_CAMERA_POSITION = "camera_position";
//    private static final String KEY_LOCATION="location";
//
//    private static final int M_MAX_ENTRIES=5;
//    private String[] mLikelyPlaceNames;
//    private String[] mLikelyPlaceAddress;
//    private String[] getmLikelyPlaceAttribution;
//    private LatLng[] mLikelyPlaceLatLngs;
    private final int MY_PERMISSIONS_REQUEST_LOCATION = 1001;


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
    public double[] lat;
    public double[] lng;
    String location_temp = "";
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent Mintent = getIntent();
        user_id = Mintent.getExtras().getString("uid");
        new init_Marker_Get().execute("http://192.168.219.101:3000/marker?id=" + user_id);
        super.onCreate(savedInstanceState);
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // GPS 프로바이더 사용가능여부
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 네트워크 프로바이더 사용가능여부
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Log.d("Main", "isGPSEnabled=" + isGPSEnabled);
        Log.d("Main", "isNetworkEnabled=" + isNetworkEnabled);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                m_lat = lat;
                m_lng = lng;
                 Log.d("Main1", "longtitude=" + m_lng + ", latitude=" + m_lat);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                //logView.setText("onStatusChanged");
            }

            public void onProviderEnabled(String provider) {
                //logView.setText("onProviderEnabled");
            }

            public void onProviderDisabled(String provider) {
                //  logView.setText("onProviderDisabled");
            }
        };

        // Register the listener with the Location Manager to receive location updates
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
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (android.location.LocationListener) locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (android.location.LocationListener) locationListener);

        // 수동으로 위치 구하기
        String locationProvider = LocationManager.GPS_PROVIDER;
        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        if (lastKnownLocation != null) {
            double lng = lastKnownLocation.getLatitude();
            double lat = lastKnownLocation.getLatitude();
            m_lng = lng;
            m_lat = lat;
            Log.d("Main2", "longtitude=" + lng + ", latitude=" + lat);
        }
//        int permssionCheck = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION);
//
//
//        if (permssionCheck!= PackageManager.PERMISSION_GRANTED) {
//
//            Toast.makeText(this,"권한 승인이 필요합니다",Toast.LENGTH_LONG).show();
//
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.ACCESS_FINE_LOCATION)) {
//                Toast.makeText(this,"000부분 사용을 위해 카메라 권한이 필요합니다.",Toast.LENGTH_LONG).show();
//            } else {
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                        MY_PERMISSIONS_REQUEST_LOCATION);
//                Toast.makeText(this,"000부분 사용을 위해 카메라 권한이 필요합니다.",Toast.LENGTH_LONG).show();
//
//            }
//        }

//        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//
//        if ( Build.VERSION.SDK_INT >= 23 &&
//                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
//            ActivityCompat.requestPermissions( MainActivity.this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
//                    0 );
//        }
//        else{
//            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            String provider = location.getProvider();
//            double latitude = location.getLatitude();
//            double longitude = location.getLongitude();
//            double altitude = location.getAltitude();
//            m_lat = latitude;
//            m_lng = longitude;
//            System.out.println(m_lat + " , " + m_lng);
//            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                    1000,
//                    1,
//                    (android.location.LocationListener) gpsLocationListener);
//            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
//                    1000,
//                    1,
//                    (android.location.LocationListener) gpsLocationListener);
//        }


//        if(savedInstanceState!=null)
//        {
//            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
//            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
//        }
        mapload();
        setContentView(R.layout.activity_main);

//        mGeoDataClient = Places.getGeoDataClient(this,null);
//        mPlaceDetectionClient = Places.getPlaceDetectionClient(this,null);
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fragmentManager=getFragmentManager();
        mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(this);

        pairs = new ArrayList<>();
        hash = new LinkedHashMap<String,String>();
        Button listmode = (Button) findViewById(R.id.listMode);
        ListItems = new ArrayList<>();
        listmode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ItemListActivity.class);
                intent.putExtra("user_id",user_id);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

    }
//    final LocationListener gpsLocationListener = new LocationListener() {
//        public void onLocationChanged(Location location) {
//
//            String provider = location.getProvider();
//            double longitude = location.getLongitude();
//            double latitude = location.getLatitude();
//            double altitude = location.getAltitude();
//
//        }
//
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//        }
//
//        public void onProviderEnabled(String provider) {
//        }
//
//        public void onProviderDisabled(String provider) {
//        }
//    };


//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_LOCATION: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    Toast.makeText(this, "승인이 허가되어 있습니다.", Toast.LENGTH_LONG).show();
//
//                } else {
//                    Toast.makeText(this, "아직 승인받지 않았습니다.", Toast.LENGTH_LONG).show();
//                }
//                return;
//            }
//
//        }
//
//    }

    public void mapload()
    {
        new list_Post().execute("http://192.168.219.101:3000/list");
    }
    public void mainload()
    {
        new init_Marker_Get().execute("http://192.168.219.101:3000/marker?id="+user_id);
    }
    @Override
    public void onMapReady(final GoogleMap googleMap) {
       // mainload();

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng point) {
                //mapload();

                c_location = new LatLng(point.latitude,point.longitude); //커스텀 위치
                add_lat=point.latitude;
                add_lng=point.longitude;
                final MarkerOptions mop = new MarkerOptions();

                items = ListItems.toArray(new String[ ListItems.size()]);
                //listActivity.ListItem; List형식의 ItemListActivity 의 List
                if(ListItems.size()==0)
                    System.out.println("리스트 비어있음");

                final List SelectedItems  = new ArrayList();
                final EditText edittext = new EditText(MainActivity.this);
          //      System.out.println("리스트1");
                edittext.setHint("정보입력");
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("정보입력");
                builder.setView(edittext);
         //       System.out.println("리스트2");
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
             //   System.out.println("리스트3");
                builder.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String msg="";
                                String temp="";
                                for (int i = 0; i < SelectedItems.size(); i++) {
                                    int index = (int) SelectedItems.get(i);
                                    msg=msg+"\n"+(i+1)+" : " +ListItems.get(index);
                                    temp+=ListItems.get(index)+",";

                                }

                                mop.title(edittext.getText().toString());
                                temp = temp.substring(0,temp.length()-1);
                                mop.snippet(temp);
                                mop.position(c_location);
                                googleMap.addMarker(mop);
                                add_name=edittext.getText().toString();
                                insert_count = SelectedItems.size();
                                add_item_list = temp;//nodejs에서 split후 string[] 에 담아 insert사용

//                                Log.d("temp값",temp);
                                Toast.makeText(getApplicationContext(),
                                        "Total "+ SelectedItems.size() +" Items Selected.\n"+ msg , Toast.LENGTH_LONG)
                                        .show();
                                new add_Maker_Post().execute("http://192.168.219.101:3000/add_marker");

                            }
                        });
                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                            }
                        });
                builder.show();
             //   System.out.println("리스트4");
          }

        });

        if(getitem_count==0) {
            System.out.println("getitem_count이 0이다");
        }
        System.out.println("아아악!!보다 먼저 실행되면 안됨");
        for(int i=0;i<getitem_count;i++)
        {
            if(hash.containsKey(location_name[i]))//장소이름이 중복될 경우
            {
                item_temp += item_name[i] + ",";
                hash.put(location_name[i], item_temp);
            }
            else//새로운 장소를 추가하는 경우
            {
                item_temp="";
                item_temp += item_name[i] + ",";
                hash.put(location_name[i], item_temp);
                pairs.add(new Pair<>(lat[i],lng[i]));

            }
        }
        MarkerOptions m = new MarkerOptions();

        Set<Map.Entry<String, String>> entries = hash.entrySet();


        int x=0;


        for (Map.Entry<String, String> entry : entries) {
            //포문이 왜 안될까
            System.out.println("문제가 생기는 부분!!!!!!!!!!!!!!"+x);
            System.out.print("key: "+ entry.getKey());
            System.out.println(", Value: "+ entry.getValue());

            m.title(entry.getKey())
                    .snippet(entry.getValue().substring(0,entry.getValue().length()-1))
            .position(new LatLng(pairs.get(x).first,pairs.get(x).second));
            googleMap.addMarker(m);
            x++;

        }



        // 디비에서 받아오는 거 까지 완료 하였으니 받아온 내용을 바탕으로 마커 추가하는 작업 필요
//        for(int i=0;i<getitem_count;i++)
//        {
//            MarkerOptions mop = new MarkerOptions();
//            if(test!=location_name[i]) {
//                if(test!="")
//                {
//                    googleMap.addMarker(mop);
//                }
//                item_snippet="";
//                test=location_name[i];
//                mop.title(location_name[i]);
//                mop.position(new LatLng(lat[i], lng[i]));
//                item_snippet+=item_name[i]+",";
//            }
//            else
//            {
//                item_snippet+=item_name[i]+",";
//            }
//        }
        //gpsTracker = new GpsTracker(MainActivity.this);



        LatLng location = new LatLng(m_lat,m_lng); //현재 내 위치
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title("현재 내 위치");
//        markerOptions.snippet("스니펫");
        markerOptions.position(location);
        googleMap.addMarker(markerOptions);
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
                    while((line = reader.readLine()) != null){
                        buffer.append(line);
                    }

                    //다 가져오면 String 형변환을 수행한다. 이유는 protected String doInBackground(String... urls) 니까
                    return buffer.toString();

                    //아래는 예외처리 부분이다.
                } catch (MalformedURLException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    //종료가 되면 disconnect메소드를 호출한다.
                    if(con != null){
                        con.disconnect();
                    }
                    try {
                        //버퍼를 닫아준다.
                        if(reader != null){
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

                getitem_count=getitem.length();

                location_name = new String[getitem_count];
                item_name = new String[getitem_count];
                lat = new double[getitem_count];
                lng = new double[getitem_count];
                for(int i=0;i<getitem_count;i++)
                {
                    JSONObject itemobject = getitem.getJSONObject(i);
                    location_name[i]=itemobject.getString("name");
                    item_name[i]=itemobject.getString("item_name");
                    lat[i]=itemobject.getDouble("lat");
                    lng[i]=itemobject.getDouble("lng");
                    System.out.println(item_name[i]);
                    System.out.println("아아아악!!!!!");
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
                for(int a=0;a<result_items.length;a++) {
                   // System.out.println("ListItems에 "+ result_items[a]+" 를 넣음");
                    if(!ListItems.contains(result_items[a]))//삭제후 마커추가시 오류
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


}
