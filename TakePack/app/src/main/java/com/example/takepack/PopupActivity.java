package com.example.takepack;


import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.MapFragment;

import java.util.ArrayList;

public class PopupActivity extends AppCompatActivity{


    ArrayList<String> Item;
    ArrayList<String> result;
    ArrayAdapter<String> Adap;
    ListView plistView;

    private String location_name;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_activity);


        Item = new ArrayList<String>();
        result = new ArrayList<String>();
        Adap = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice, Item);
        plistView=(ListView)findViewById(R.id.list_item);
        plistView.setAdapter(Adap);
        plistView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        Item.add("test1");
        Item.add("test2");
        Item.add("test3");

        final EditText etName = (EditText)findViewById(R.id.edtext);

        Button pb = (Button) findViewById(R.id.check);
        Button nb = (Button) findViewById(R.id.cancel);
        Button kh = (Button) findViewById(R.id.keyhide);

        kh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

                imm.hideSoftInputFromWindow(etName.getWindowToken(), 0); //숨기기

            }
        });
        pb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //확인 버튼
                Intent intent = getIntent();

                location_name=etName.getText().toString();
                SparseBooleanArray checkedItems = plistView.getCheckedItemPositions();
                int count = Adap.getCount(); //전체 몇개인지 세기

                if(checkedItems.size()!=0){
                    for(int i=count-1; i>=0; i--){
                        if(checkedItems.get(i)) { //희소 논리 배열의 해당 인덱스가 선택되어 있다면
                            result.add(Item.get(i)); //arrayList에 추가하기

                            Log.d("데이터확인", String.valueOf(checkedItems.get(i)));
                        }

                        intent.putExtra("Name",location_name);
                      //  intent.putExtra("Itemlist",result.toArray()); //체크박스로 선택한 객체만 넘어오게 해야 함


                        Log.d("데이터 전달",location_name);

                    }
                    setResult(RESULT_OK,intent);
                    finish();
                    overridePendingTransition(0, 0);
                } else {
                    Toast.makeText(PopupActivity.this, "소지품을 한개 이상 등록해주세요", Toast.LENGTH_SHORT).show();

                }
                plistView.clearChoices() ;
                Adap.notifyDataSetChanged();




            }
        });


    }

}
