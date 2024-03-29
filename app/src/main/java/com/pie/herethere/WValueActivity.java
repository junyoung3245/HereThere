package com.pie.herethere;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.pie.herethere.App.AppKey;
import com.pie.herethere.Model.Forecast3day;
import com.pie.herethere.Model.ForecastInfo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class WValueActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView BackImage, BackGroundImg;
    TextView ToolbarText;

    Document document;
    String Title, Img, ContentId, lat, lon;

    AppKey appKey = new AppKey();
    WValue_Service Service;

    ImageView img[] = new ImageView[3];
    TextView text[] = new TextView[3];
    TextView ondo[] = new TextView[3];
    Integer idGroup[] = {R.id.wvalue_img1,  R.id.wvalue_img2,  R.id.wvalue_img3,
                         R.id.wvalue_text1, R.id.wvalue_text2, R.id.wvalue_text3,
                         R.id.wvalue_ondo1, R.id.wvalue_ondo2, R.id.wvalue_ondo3};

    public void Declaration() {
        BackImage = (ImageView) findViewById(R.id.wvalue_back);
        BackImage.setOnClickListener(this);

        BackGroundImg = (ImageView) findViewById(R.id.wvalue_img);
        ToolbarText = (TextView) findViewById(R.id.wvalue_toolbar);

        for (int i = 0; i < 3; i++) {
            img[i] = (ImageView) findViewById(idGroup[i]);
            text[i] = (TextView) findViewById(idGroup[i + 3]);
            ondo[i] = (TextView) findViewById(idGroup[i + 6]);
        }
    }

    public void GetData() {
        Intent intent = getIntent();

        Img = intent.getStringExtra("img");
        Glide
                .with(getApplicationContext())
                .load(Img)
                .into(BackGroundImg);

        Title = intent.getStringExtra("title");
        ToolbarText.setText(Title);

        ContentId = intent.getStringExtra("id");
        Service = WValue_Service.retrofit.create(WValue_Service.class);
        try {
            URL url = new URL(appKey.getAppURL() + "searchKeyword?ServiceKey=" + appKey.getAppKey() + "&keyword=" + URLEncoder.encode(Title, "utf-8") +
                    "&areaCode=&sigunguCode=&cat1=&cat2=&cat3=&listYN=Y&MobileOS=ETC&MobileApp=TourAPI3.0_Guide&arrange=A&numOfRows=10&pageNo=1");

            Getlocation getlocation = new Getlocation();
            getlocation.execute(String.valueOf(url));
        } catch (Exception e) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wvalue);
        Declaration();
        GetData();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.wvalue_back) {
            finish();
            overridePendingTransition(0, R.anim.anim_left);
        }
    }

    private class Getlocation extends AsyncTask<String, Void, Document> {
        @Override
        protected Document doInBackground(String... urls) {
            URL url;
            try {
                url = new URL(urls[0]);
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                document = documentBuilder.parse(new InputSource(url.openStream()));
                document.getDocumentElement().normalize();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return document;
        }

        @Override
        protected void onPostExecute(Document document) {
            try {
                NodeList nodeList = document.getElementsByTagName("item");

                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    Element fstElmnt = (Element) node;

                    NodeList conList = fstElmnt.getElementsByTagName("contentid");
                    Element conElement = (Element) conList.item(0);
                    conList = conElement.getChildNodes();
                    String contentId = conList.item(0).getNodeValue().toString();

                    if (contentId.equals(ContentId)) {
                        NodeList lonList = fstElmnt.getElementsByTagName("mapx");
                        NodeList latList = fstElmnt.getElementsByTagName("mapy");
                        Element lonElement = (Element) lonList.item(0);
                        Element latElement = (Element) latList.item(0);
                        lonList = lonElement.getChildNodes();
                        latList = latElement.getChildNodes();
                        lon = lonList.item(0).getNodeValue().toString();
                        lat = latList.item(0).getNodeValue().toString();

                        StartThread(Double.parseDouble(lat), Double.parseDouble(lon));
                    }
                }
                super.onPostExecute(document);
            } catch (Exception e) { }
        }
    }

    public void StartThread(final double lat, final double lon) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getForecase3Days(lat, lon);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

    void getForecase3Days(final double latitude, final double longitude) {
        Service.getForecase3Days(1, String.valueOf(latitude), String.valueOf(longitude)).enqueue(new Callback<ForecastInfo>() {
            @Override
            public void onResponse(Call<ForecastInfo> call, Response<ForecastInfo> response) {
                if (response.body() == null)
                    return;
                ForecastInfo info = response.body();
                if (info.weather == null || info.weather.forecast3days.isEmpty())
                    return;

                final ArrayList<WValue_ListData> forecastInfo = new ArrayList<>();
                int offsetHout = 3;
                int maxHour = 49;

                Date today = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("MM월 dd일 a hh시");

                for (int hour = 4; hour <= maxHour; hour += offsetHout) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(today);
                    calendar.add(Calendar.HOUR, hour);

                    String time = sdf.format(calendar.getTime());
                    String sky = "";
                    String temp = "";

                    try {
                        Forecast3day forecast = info.weather.forecast3days.get(0);

                        Field skyField = forecast.fcst3hour.sky.getClass().getField("name" + hour + "hour");
                        sky = skyField.get(forecast.fcst3hour.sky).toString();

                        Field tempField = forecast.fcst3hour.temperature.getClass().getField("temp" + hour + "hour");
                        temp = tempField.get(forecast.fcst3hour.temperature).toString();

                        if (temp.isEmpty() == false) {
                            temp = String.valueOf((int) Double.parseDouble(temp));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (hour == 4 || hour == 28 || hour == 49) {
                        forecastInfo.add(new WValue_ListData(lat, lon, sky, time, temp));
                    }
                }
                for (int i = 0; i < 3; i++) {
                    text[i].setText(forecastInfo.get(i).sky);
                    ondo[i].setText(forecastInfo.get(i).temp + "℃");

                    switch (text[i].getText().toString()) {
                        case "맑음" :
                            img[i].setImageResource(R.drawable.s_1);
                            break;
                        case "구름조금" :
                        case "구름많음" :
                            img[i].setImageResource(R.drawable.s_2);
                            break;
                        case "흐림" :
                            img[i].setImageResource(R.drawable.s_3);
                            break;
                        case "흐리고 비" :
                        case "뇌우, 비" :
                            img[i].setImageResource(R.drawable.s_4);
                            break;
                        default :
                            img[i].setImageResource(R.drawable.s_5);
                    }
                }
            }
            @Override
            public void onFailure(Call<ForecastInfo> call, Throwable t) { }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, R.anim.anim_left);
    }
}