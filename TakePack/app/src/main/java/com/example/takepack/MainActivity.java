package com.example.takepack;

import androidx.appcompat.app.AppCompatActivity;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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

    }

    private void showAlertDialog()
    {

    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {


        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override


            public void onMapLongClick(final LatLng point) {
                final ItemListActivity listActivity = new ItemListActivity();
                final ArrayList<String> testitem = listActivity.Items;

                final LatLng c_location = new LatLng(point.latitude,point.longitude); //커스텀 위치
                final MarkerOptions mop = new MarkerOptions();
                final List<String> ListItems = new ArrayList<>();
                for(int i=0;i<testitem.size();i++)
                {
                    ListItems.add(testitem.get(i));
                }

                final CharSequence[] items =  ListItems.toArray(new String[ ListItems.size()]);

                final List SelectedItems  = new ArrayList();

                final EditText edittext = new EditText(MainActivity.this);
                edittext.setHint("정보입력");
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("정보입력");
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
                                String msg="";
                                String temp="";
                                for (int i = 0; i < SelectedItems.size(); i++) {
                                    int index = (int) SelectedItems.get(i);
                                    mop.title(edittext.getText().toString());
                                    msg=msg+"\n"+(i+1)+" : " +ListItems.get(index);
                                    temp+=ListItems.get(index)+",";

                                }
                                temp = temp.substring(0,temp.length()-1);
                                mop.snippet(temp);
                                mop.position(c_location);
                                googleMap.addMarker(mop);
                                Log.d("temp값",temp);
                                Toast.makeText(getApplicationContext(),
                                        "Total "+ SelectedItems.size() +" Items Selected.\n"+ msg , Toast.LENGTH_LONG)
                                        .show();
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

        LatLng location = new LatLng(36.379064, 128.146616); //우리집 위치
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title("우리집");
        markerOptions.snippet("스니펫");
        markerOptions.position(location);
        googleMap.addMarker(markerOptions);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16));

    }
}
