package kr.co.daehee.weather.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import kr.co.daehee.weather.model.DataGetterSetters;
import kr.co.daehee.weather.util.DataHandler;
import kr.co.daehee.weather.util.MySQLiteOpenHelper;

public class MainActivity extends AppCompatActivity {
    private static final int GET_REGION = 0;
    Toolbar toolbar;
    FloatingActionButton fab;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    ListView drawerItemList;
    LinearLayout addRegion;
    ArrayList<String> regionArr;
    LocalListAdapter regionAdapter;
    Intent intent;
    Context context;
    String dongCode;

    TextView curTemp;
    TextView curRain;
    TextView curWind;
    TextView lastUpdateView;
    TextView regionView;
    TextView weatherKorView;
    TextView tempView;
    ListView weatherList;
    private ArrayList<DataGetterSetters> dataList;
    private DataListAdapter adapter;
    String urlXml = "http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone=";

    private MySQLiteOpenHelper helper;
    String dbName = "local_data.db";
    int dbVersion = 1;
    private SQLiteDatabase db;
    String tag = "SQLite";
    String[][] local;


    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(kr.co.daehee.weather.R.layout.activity_main);

        LayoutInflater li = getLayoutInflater();
        LinearLayout linear = (LinearLayout)li.inflate(kr.co.daehee.weather.R.layout.layout_header, null); // 레이아웃에 맞게!

        context = getApplicationContext();
        toolbar = (Toolbar) findViewById(kr.co.daehee.weather.R.id.toolbar);
        setSupportActionBar(toolbar);

        weatherList = (ListView)findViewById(kr.co.daehee.weather.R.id.wearther_item_list);
        weatherList.addHeaderView(linear);

        curTemp = (TextView)findViewById(kr.co.daehee.weather.R.id.cur_temp);
        curRain = (TextView)findViewById(kr.co.daehee.weather.R.id.cur_rain);
        curWind = (TextView)findViewById(kr.co.daehee.weather.R.id.cur_wind);
        lastUpdateView = (TextView)findViewById(kr.co.daehee.weather.R.id.tv_last_update); //addHeaderView() 가 호출되기 이전에 헤더뷰안의 항목 접근시 오류난다.
        regionView = (TextView)findViewById(kr.co.daehee.weather.R.id.tv_view_region);
        weatherKorView = (TextView)findViewById(kr.co.daehee.weather.R.id.tv_weather_kor);
        tempView = (TextView)findViewById(kr.co.daehee.weather.R.id.tv_temp_num);

        fab = (FloatingActionButton) findViewById(kr.co.daehee.weather.R.id.fab);
        fab.setBackgroundColor(Color.parseColor("#B2EBF4"));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "차트 기능은 추가 될 예정입니다.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        drawer = (DrawerLayout) findViewById(kr.co.daehee.weather.R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, kr.co.daehee.weather.R.string.navigation_drawer_open, kr.co.daehee.weather.R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        addRegion = (LinearLayout)findViewById(kr.co.daehee.weather.R.id.tv_add_region);
        addRegion.setOnClickListener(myClickListener);
        drawerItemList = (ListView)findViewById(kr.co.daehee.weather.R.id.drawer_item_list);
        regionArr = new ArrayList<String>();
//        regionAdapter= new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, regionArr);
        regionAdapter= new LocalListAdapter(this, kr.co.daehee.weather.R.layout.simple_list_item_2, regionArr);
        drawerItemList.setAdapter(regionAdapter);
        drawerItemList.setOnItemClickListener(localItemClickListener);

        helper = new MySQLiteOpenHelper(MainActivity.this, dbName, null, dbVersion);
        try {
//         // 데이터베이스 객체를 얻어오는 다른 간단한 방법
//         db = openOrCreateDatabase(dbName,  // 데이터베이스파일 이름
//                          Context.MODE_PRIVATE, // 파일 모드
//                          null);    // 커서 팩토리
//
//         String sql = "create table mytable(id integer primary key autoincrement, name text);";
//        db.execSQL(sql);

            db = helper.getWritableDatabase(); // 읽고 쓸수 있는 DB
            //db = helper.getReadableDatabase(); // 읽기 전용 DB select문
        } catch (SQLiteException e) {
            e.printStackTrace();
            Log.e(tag, "데이터베이스를 얻어올 수 없음");
            finish(); // 액티비티 종료
        }
        refreshList(1);

        if (local.length > 0) {
            regionView.setText(local[0][0]);
            dongCode = local[0][1];
            MyTask t = new MyTask();
            t.execute();
        }
        else {
            Toast.makeText(getApplicationContext(), "등록된 지역이 없습니다\r\n  지역을 추가하세요", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(kr.co.daehee.weather.R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(kr.co.daehee.weather.R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == kr.co.daehee.weather.R.id.action_refresh) {
            MyTask t = new MyTask();
            t.execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    View.OnClickListener myClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v.getId() == kr.co.daehee.weather.R.id.tv_add_region) {
                intent = new Intent(MainActivity.this, SelectActivity.class);
                startActivityForResult(intent, GET_REGION);
//                Log.e("터치", "터치");
//                regionArr.add("asd" + count);
//                count++;
//                regionAdapter.notifyDataSetChanged();
//                int h = regionArr.size()*getPx(51);
//                Toast.makeText(getApplicationContext(), h+"", Toast.LENGTH_SHORT).show();
//                ViewGroup.LayoutParams lp = drawerItemList.getLayoutParams();
//                lp.height = h;
//                drawerItemList.setLayoutParams(lp);
//                for (int i = 0; i < regionArr.size(); i++) {
//                    Log.e("regionArr["+i+"]", regionArr.get(i));
//                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case GET_REGION: // requestCode가 B_ACTIVITY인 케이스
                if(resultCode == RESULT_OK){ //B_ACTIVITY에서 넘겨진 resultCode가 OK일때만 실행
                    if (data != null) {
                        refreshList(1);
                        Toast.makeText(getApplicationContext(), "추가되었습니다.", Toast.LENGTH_SHORT).show();
                    }

                }
        }
    }

    public int getPx(int dimensionDp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dimensionDp * density + 0.5f);
    }

    public void refreshList(int num) {
        if (num == 1) { //드로어 안에있는 리스트 높이 재조정
            regionAdapter.clear();
            Cursor c = db.query("mytable", null, null, null, null, null, null);
            int count = c.getCount();
            local = new String[count][2];
            int i=0;
            Log.d(tag, "레코드 갯수:" + count);
            while(c.moveToNext()) {
                int _id = c.getInt(0);
                String name = c.getString(1);
                String code = c.getString(2);
                local[i][0] = name;
                local[i][1] = code;
                i++;
                Log.d(tag, "_id:" + _id + ",name:" + name + ",code:" + code);
                regionArr.add(name);
                regionAdapter.notifyDataSetChanged();
            }
            //ListView 높이 재설정
            int h = regionArr.size()*getPx(51);
            ViewGroup.LayoutParams lp = drawerItemList.getLayoutParams();
            lp.height = h;
            drawerItemList.setLayoutParams(lp);

            for (int a=0; a < local.length; a++) {
                for(int b=0; b < 2; b++) {
                    Log.e("local["+a+"]"+"["+b+"]", local[a][b]); //DB변경사항이 배열에도 잘 반영되었는지 로그확인
                }
            }
        }
        else if (num == 2) { //날씨 아이템 리스트 높이 재조정
            int h = dataList.size()*getPx(90);
            ViewGroup.LayoutParams lp = weatherList.getLayoutParams();
            lp.height = h;
            weatherList.setLayoutParams(lp);
        }

    }

    AdapterView.OnItemClickListener localItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor c = db.query("mytable", null, null, null, null, null, null);
            c.move(position + 1);
            Log.d(tag, "name:" + c.getString(1) + " / " + "code:" + c.getString(2));
            dongCode = local[position][1];
            regionView.setText(local[position][0]);
            MyTask t = new MyTask();
            t.execute();
            drawer.closeDrawers();
        }
    };

    class LocalListAdapter extends ArrayAdapter<String> {
        private LayoutInflater inflater;
        private View adapterView;
        private LocalViewHolder viewHolder;

        public LocalListAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            inflater = LayoutInflater.from(context);
        }

        public View getView(final int position, View convertView, android.view.ViewGroup parent) {

            if (convertView == null) {
                adapterView = inflater.inflate(kr.co.daehee.weather.R.layout.simple_list_item_2, null);
                viewHolder = new LocalViewHolder();
                viewHolder.textView = (TextView)adapterView.findViewById(kr.co.daehee.weather.R.id.textView);
                viewHolder.imageView = (ImageView)adapterView.findViewById(kr.co.daehee.weather.R.id.delete_btn);
                adapterView.setTag(viewHolder);
            }else {
                Log.e("Position", "else=>" +  position);
                adapterView = convertView;
                viewHolder = (LocalViewHolder)adapterView.getTag();
            }
            viewHolder.textView.setText(local[position][0]);
            viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                    alert.setTitle("삭제");
                    alert.setMessage("정말로 삭제 하시겠습니까?");
                    alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.e("삭제 처리", "position : " + position + " / name : " + local[position][0] + " / " + "code : " + local[position][1]);
                            Cursor c = db.query("mytable", null, null, null, null, null, null);
                            c.move(position + 1);
                            db.execSQL("delete from mytable where id=" + c.getInt(0) + ";");
                            refreshList(1);
                            Toast.makeText(getApplicationContext(), "삭제되었습니다", Toast.LENGTH_SHORT).show();
                        }
                    });
                    alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int arg1) {
                            dialog.dismiss();
                        }
                    });
                    alert.show();
                }
            });
            return adapterView;
        }
    }

    class MyTask extends AsyncTask<String, Integer, Boolean> {
        DataHandler myDataHandler;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("로딩중입니다.");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try{
                Log.e("doInBackground() 시작", "doInBackground() 시작");
                //------ 메인 파싱 구간 시작 ------//
                SAXParserFactory saxPF = SAXParserFactory.newInstance();
                SAXParser saxParser = saxPF.newSAXParser();
                XMLReader xmlReader = saxParser.getXMLReader();
//				URL url = new URL("http://rss.hankyung.com/new/news_industry.xml");
                URL url = new URL("http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone=" + dongCode);
                myDataHandler = new DataHandler();
                xmlReader.setContentHandler(myDataHandler);
                xmlReader.parse(new InputSource(url.openStream()));
                //------ 메인 파싱 구간 종료 ------//

                // load parsing data & View
                dataList = myDataHandler.getData();
                Thread.sleep(500);
                Log.e("doInBackground() 완료", "doInBackground() 완료");
            }catch(Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.e("onPostExecute() 시작", "onPostExecute() 시작");
            adapter = new DataListAdapter(context, 0, dataList);
            if (dataList == null) {
                Log.e("데이터리스트 null", "데이터리스트 null");
            }
            else { //dataList가 null이면 adapter연결 X (강제종료 방지)
                lastUpdateView.setText("마지막 업데이트 : " + myDataHandler.lastUpdate);
                Log.e("최종 업데이트 : ", myDataHandler.lastUpdate);
                Log.e("위치 : ", myDataHandler.myLocation);
                SwingBottomInAnimationAdapter animationAdapter = new SwingBottomInAnimationAdapter(adapter);
                animationAdapter.setAbsListView(weatherList);
                weatherList.setAdapter(animationAdapter);
//                weatherList.setAdapter(adapter);

                switch (dataList.get(0).getWfKor()) {
                    case 0:
                        weatherKorView.setText("맑음");
                        break;
                    case 1:
                        weatherKorView.setText("구름 조금");
                        break;
                    case 2:
                        weatherKorView.setText("구름 많음");
                        break;
                    case 3:
                        weatherKorView.setText("흐림");
                        break;
                    case 4:
                        weatherKorView.setText("비");
                        break;
                    case 5:
                        weatherKorView.setText("눈/비");
                        break;
                    case 6:
                        weatherKorView.setText("눈");
                        break;
                }
                tempView.setText(dataList.get(0).getTemp()+"℃");
                for (int i=0; i < dataList.size(); i++) {
                    if (dataList.get(i).getTmx() == -999.0) {
                        i++;
                    }
                    else {
                        if (i != 0) { Toast.makeText(getApplicationContext(), "최고최저 온도현황 내일것 불러옴", Toast.LENGTH_SHORT).show(); }
                        curTemp.setText(dataList.get(i).getTmx() + " / " + dataList.get(i).getTmn() + "℃");
                        break;
                    }
                }
                curRain.setText(dataList.get(0).getPop() + "% / " + dataList.get(0).getR12() + "mm");
                curWind.setText(dataList.get(0).getWs() + "m/s");
            }
            Log.e("onPostExecute() 완료", "onPostExecute() 완료");
            progressDialog.dismiss();
            super.onPostExecute(result);
        }

    }

    class DataListAdapter extends ArrayAdapter<DataGetterSetters> {
        private LayoutInflater inflater;
        private View adapterView;
        private MyViewHolder viewHolder;

        public DataListAdapter(Context context, int resource, List<DataGetterSetters> objects) {
            super(context, resource, objects);
            inflater = LayoutInflater.from(context);
        }

        public View getView(int position, View convertView, android.view.ViewGroup parent) {

            DataGetterSetters data = dataList.get(position);

            if (convertView == null) {
                adapterView = inflater.inflate(kr.co.daehee.weather.R.layout.weather_item, null);
                viewHolder = new MyViewHolder();
                viewHolder.dayView = (TextView)adapterView.findViewById(kr.co.daehee.weather.R.id.day_view);
                viewHolder.hourView = (TextView)adapterView.findViewById(kr.co.daehee.weather.R.id.hour_view);
                viewHolder.tempView = (TextView)adapterView.findViewById(kr.co.daehee.weather.R.id.temp_view);
                viewHolder.imageView = (ImageView)adapterView.findViewById(kr.co.daehee.weather.R.id.weather_image);
                viewHolder.rehView = (TextView)adapterView.findViewById(kr.co.daehee.weather.R.id.reh_view);
                viewHolder.popView = (TextView)adapterView.findViewById(kr.co.daehee.weather.R.id.pop_view);
                viewHolder.wsView = (TextView)adapterView.findViewById(kr.co.daehee.weather.R.id.ws_view);
                adapterView.setTag(viewHolder);
            }else {
                Log.e("Position", "else=>" +  position);
                adapterView = convertView;
                viewHolder = (MyViewHolder)adapterView.getTag();
            }
            if (data != null) {
                viewHolder.dayView.setText(data.getDay()+"");
                viewHolder.hourView.setText(data.getHour()-3+"시");
                viewHolder.tempView.setText(data.getTemp()+"℃");
                switch (data.getWfKor()) {
                    case 0:
                        viewHolder.imageView.setImageResource(kr.co.daehee.weather.R.drawable.sunny);
                        break;
                    case 1:
                        viewHolder.imageView.setImageResource(kr.co.daehee.weather.R.drawable.partly_cloud);
                        break;
                    case 2:
                        viewHolder.imageView.setImageResource(kr.co.daehee.weather.R.drawable.mostly_cloud);
                        break;
                    case 3:
                        viewHolder.imageView.setImageResource(kr.co.daehee.weather.R.drawable.cloud);
                        break;
                    case 4:
                        viewHolder.imageView.setImageResource(kr.co.daehee.weather.R.drawable.rain);
                        break;
                    case 5:
                        viewHolder.imageView.setImageResource(kr.co.daehee.weather.R.drawable.snow);
                        break;
                    case 6:
                        viewHolder.imageView.setImageResource(kr.co.daehee.weather.R.drawable.snow);
                        break;
                }
                viewHolder.popView.setText("강수확률 : " + data.getPop() + "%");
                viewHolder.wsView.setText("풍속 : " + data.getWs() + "m/s");
                viewHolder.rehView.setText("습도 : " + data.getReh() + "%");
            }
            return adapterView;
        }
    }

    class LocalViewHolder {
        TextView textView;
        ImageView imageView;
    }

    class MyViewHolder {
        TextView dayView;
        TextView hourView;
        TextView tempView;
        ImageView imageView;
        TextView rehView;
        TextView popView;
        TextView wsView;
    }
}
