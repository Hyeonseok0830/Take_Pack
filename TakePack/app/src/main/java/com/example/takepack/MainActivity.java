package com.example.takepack;

import androidx.appcompat.app.AppCompatActivity;



import android.app.Dialog;
import android.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FragmentManager fragmentManager;
    private MapFragment mapFragment;
    private String title="";

    private MarkerOptions mop = new MarkerOptions();

    private double m_lat;
    private double m_lon;


    GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager=getFragmentManager();
        mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(this);
        Button listmode = (Button) findViewById(R.id.listMode);
        listmode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ItemListActivity.class);
                startActivity(intent);
            }
        });

 // 요부분이 문제

    }


    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {       super.onActivityResult(requestCode, resultCode, data);


        if(resultCode==RESULT_OK)
// 액티비티가 정상적으로 종료되었을 경우
        {
            if(requestCode==1)
// InformationInput에서 호출한 경우에만 처리합니다.
            {
// 받아온 이름과 전화번호를 InformationInput 액티비티에 표시합니다.
                title=data.getStringExtra("Name");
                //  setText(data.getStringExtra("listItem"));
                Log.d("intent로 정보 받아옴",title);

                mop.title(title);
                LatLng tl = new LatLng(m_lat,m_lon);
                mop.position(tl);

                Log.d("intent로 정보 받아옴",title);
              //이부분 다시
                onMapReady(mMap);
            }
        }
    }
    public void CreateMaker(String name,LatLng location)
    {

     //   onMapReady(mMap);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {


        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng point) {

                Intent intent = new Intent(getApplicationContext(),PopupActivity.class);
                m_lat = point.latitude; // 위도
                m_lon = point.longitude; // 경도
                intent.putExtra("title",title);
                startActivityForResult(intent, 1);


            }
        });


        LatLng location = new LatLng(36.892498, 126.628380); //우리집 위치
        MarkerOptions markerOptions=new MarkerOptions();
        markerOptions.title("우리집");
        markerOptions.snippet("스니펫");
        markerOptions.position(location);
        googleMap.addMarker(markerOptions);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16));


    }

}
