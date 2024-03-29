package com.pie.herethere;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class WeatherActivity extends AppCompatActivity {
    ListView listView;
    ArrayList<Weather_ListData> list = new ArrayList<>();
    Weather_ListAdapter adapter;

    ImageView back;
    TextView er;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        listView = (ListView) findViewById(R.id.weather_list);
        back = (ImageView) findViewById(R.id.weather_back);

        er = (TextView) findViewById(R.id.weather_er);

        getFile();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(0, R.anim.anim_left);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(WeatherActivity.this, WValueActivity.class);
                intent.putExtra("title", list.get(i).getTitle());
                intent.putExtra("img", list.get(i).getImg());
                intent.putExtra("id", list.get(i).getContentId());
                startActivity(intent);
                overridePendingTransition(R.anim.anim_right, R.anim.anim_hold);
            }
        });
    }

    public void getFile() {
        try {
            File directory = new File("/data/data/com.pie.herethere/files");
            File[] files = directory.listFiles();

            for (int i = 0; i < files.length; i++) {
                try {
                    String fileName = files[i].getName();
                    String sp[] = fileName.split(" ");

                    FileInputStream fis = openFileInput(fileName);
                    BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));

                    String title = br.readLine();
                    String img = br.readLine();

                    if (!title.equals("해제")) {
                        list.add(new Weather_ListData(title, img, sp[1]));
                    }

                    br.close();
                    fis.close();
                } catch (Exception e) {
                }
            }
            adapter = new Weather_ListAdapter(getLayoutInflater(), list);
            listView.setAdapter(adapter);
        } catch (Exception e) { }

        if (list.size() != 0) {
            er.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, R.anim.anim_left);
    }
}