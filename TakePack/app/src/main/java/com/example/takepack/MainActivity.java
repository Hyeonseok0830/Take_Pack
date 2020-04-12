package com.example.takepack;

import androidx.appcompat.app.AppCompatActivity;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FragmentManager fragmentManager;
    private MapFragment mapFragment;

    private String title;
    private Button hidebtn;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager=getFragmentManager();
        mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng point) {
                final Dialog ad = new Dialog(MainActivity.this); // 다이얼로그 객체 생성
                ad.setTitle("다이얼로그의 제목");
                ad.setContentView(R.layout.popup_activity); // 다이얼로그 화면 등록
                Button pb = (Button) ad.findViewById(R.id.check);
                Button nb = (Button) ad.findViewById(R.id.cancel);

                Button kh = (Button) ad.findViewById(R.id.keyhide);

                final EditText et = (EditText) ad.findViewById(R.id.edtext);
                kh.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

                        imm.hideSoftInputFromWindow(et .getWindowToken(), 0); //숨기기

                    }
                });

                pb.setOnClickListener(new View.OnClickListener() { // 확인버튼
                    @Override
                    public void onClick(View v) {
                        title = et.getText().toString();
                        MarkerOptions mOption = new MarkerOptions();
                        mOption.title(title);
                        Double latitude = point.latitude; // 위도
                        Double longitude = point.longitude; // 경도
                        mOption.snippet("엄청이뻐 ㅋ");
                        mOption.position(new LatLng(latitude, longitude));
                        googleMap.addMarker(mOption);
                        ad.dismiss();
                    }
                });
                nb.setOnClickListener(new View.OnClickListener() { //취소버튼
                    @Override
                    public void onClick(View v) {
                        ad.dismiss();
                    }
                });
                ad.show();

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
