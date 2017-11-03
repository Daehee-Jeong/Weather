package kr.co.daehee.weather.activity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import kr.co.daehee.weather.model.DataGetterSetters;
import kr.co.daehee.weather.model.DongDataObject;
import kr.co.daehee.weather.model.DongInfo;
import kr.co.daehee.weather.model.GunguDataObject;
import kr.co.daehee.weather.model.GunguInfo;
import kr.co.daehee.weather.util.MySQLiteOpenHelper;
import kr.co.daehee.weather.model.SidoDataObject;
import kr.co.daehee.weather.model.SidoInfo;

/**
 * Created by dehee on 2016-01-27.
 */
public class SelectActivity extends AppCompatActivity{
    Context context;
    TextView textView01;
    TextView textView02;
    TextView textView03;
    Toolbar toolbar;
    Button buttonOk;
    Button buttonNo;
    Bundle bundle;
    Intent intent;

    private ArrayList<DataGetterSetters> dataList;
    private DataListAdapter adapter;

    ArrayList<SidoDataObject> sidoArray;
    ArrayList<GunguDataObject> gunguArray;
    ArrayList<DongDataObject> dongArray;

    SidoAdapter sidoAdapter;
    GunguAdapter gunguAdapter;
    DongAdapter dongAdapter;

    ListView sidoList;
    ListView gunguList;
    ListView dongList;
    ListView weatherList;

    String sidoCode;
    String gunguCode;
    String dongCode;

    String url = "http://www.kma.go.kr/DFSROOT/POINT/DATA/top.json.txt";
    String urlGungu = "http://www.kma.go.kr/DFSROOT/POINT/DATA/mdl."+sidoCode+".json.txt";
    String urlDong = "http://www.kma.go.kr/DFSROOT/POINT/DATA/leaf."+gunguCode+".json.txt";
    String urlXml = "http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone="+dongCode;

    private MySQLiteOpenHelper helper;
    String dbName = "local_data.db";
    int dbVersion = 1;
    private SQLiteDatabase db;
    String tag = "SQLite";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(kr.co.daehee.weather.R.layout.activity_select);
        context = getApplicationContext();

        getSupportActionBar().setTitle("지역 선택");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textView01 = (TextView)findViewById(kr.co.daehee.weather.R.id.textView01);
        textView02 = (TextView)findViewById(kr.co.daehee.weather.R.id.textView02);
        textView03 = (TextView)findViewById(kr.co.daehee.weather.R.id.textView03);
        buttonNo = (Button)findViewById(kr.co.daehee.weather.R.id.button_cancel);
        buttonOk = (Button)findViewById(kr.co.daehee.weather.R.id.button_ok);
        buttonNo.setOnClickListener(myClickListener);
        buttonOk.setOnClickListener(myClickListener);

        sidoArray = new ArrayList<SidoDataObject>();
        gunguArray = new ArrayList<GunguDataObject>();
        dongArray = new ArrayList<DongDataObject>();

        sidoList = (ListView)findViewById(kr.co.daehee.weather.R.id.sidoList);
        gunguList = (ListView)findViewById(kr.co.daehee.weather.R.id.gunguList);
        dongList = (ListView)findViewById(kr.co.daehee.weather.R.id.dongList);

        bundle = new Bundle();
        intent = new Intent();

        helper = new MySQLiteOpenHelper(SelectActivity.this, dbName, null, dbVersion);
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

        GetTask task = new GetTask(url, 1);
        task.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(kr.co.daehee.weather.R.menu.main, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class GetTask extends AsyncTask<Integer, String, Boolean> {
        String urlStr = "";
        String resultStr = "";
        int type;

        public GetTask(String urlStr, int type) {
            this.urlStr = urlStr;
            this.type = type;
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            Log.e("상태", "doInBackground() 진입");
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);

                int resCode = conn.getResponseCode();
                if (resCode == HttpURLConnection.HTTP_OK) {
                    Log.e("OK", "OK");
                    BufferedReader instream = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuffer buffer = new StringBuffer();
                    while(true) {
                        String line = instream.readLine();
                        if (line == null) {
                            break;
                        }

                        buffer.append(line + "\n");
                    }
                    instream.close();

                    String input = buffer.toString();
                    Log.e("input", input);

                    Log.e("상태", "processResponse() 호출");
                    processResponse(input);
                }


            } catch(Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        public void processResponse(String input) {
            if (type == 1) {
                //시or도 얻어오기
                Gson gson = new Gson();

                JsonParser parser = new JsonParser();
                JsonArray jArray = parser.parse(input).getAsJsonArray();

                SidoInfo sidoInfo = new SidoInfo();
                sidoInfo.data = new ArrayList<SidoDataObject>();

                for(JsonElement obj : jArray )
                {
                    SidoDataObject dataObj = gson.fromJson( obj, SidoDataObject.class);
                    sidoInfo.data.add(dataObj);
                }

                Log.e("데이터 갯수", sidoInfo.data.size()+"");

                for (int i = 0; i < sidoInfo.data.size(); i++) {
                    SidoDataObject dataObj = sidoInfo.data.get(i);
                    sidoArray.add(dataObj);
                    resultStr += "   #" + i + " : " + dataObj.code+ ", " + dataObj.value+"\r\n";
                }
            }
            else if(type == 2) {
                //군or구 얻어오기
                Gson gson = new Gson();

                JsonParser parser = new JsonParser();
                JsonArray jArray = parser.parse(input).getAsJsonArray();

                GunguInfo divInfo = new GunguInfo();
                divInfo.data = new ArrayList<GunguDataObject>();

                for(JsonElement obj : jArray )
                {
                    GunguDataObject dataObj = gson.fromJson( obj , GunguDataObject.class);
                    divInfo.data.add(dataObj);
                }

                Log.e("데이터 갯수", divInfo.data.size()+"");
                gunguArray.clear(); //누를때 마다 데이터 새로고침이 아닌 축적되던 현상을 해결하기 위함

                for (int i = 0; i < divInfo.data.size(); i++) {
                    GunguDataObject dataObj = divInfo.data.get(i);
                    gunguArray.add(dataObj);
                    resultStr += "   #" + i + " : " + dataObj.code+ ", " + dataObj.value+"\r\n";
                }
            }
            else if(type == 3) {
                //동 얻어오기
                Gson gson = new Gson();

                JsonParser parser = new JsonParser();
                JsonArray jArray = parser.parse(input).getAsJsonArray();

                DongInfo dongInfo = new DongInfo();
                dongInfo.data = new ArrayList<DongDataObject>();

                for(JsonElement obj : jArray )
                {
                    DongDataObject dataObj = gson.fromJson(obj , DongDataObject.class);
                    dongInfo.data.add(dataObj);
                }

                Log.e("데이터 갯수", dongInfo.data.size()+"");
                dongArray.clear(); //누를때 마다 데이터 새로고침이 아닌 축적되던 현상을 해결하기 위함

                for (int i = 0; i < dongInfo.data.size(); i++) {
                    DongDataObject dataObj = dongInfo.data.get(i);
                    dongArray.add(dataObj);
                    resultStr += "   #" + i + " : " + dataObj.code+ ", " + dataObj.value+"\r\n";
                    Log.e(dongArray.get(i).code, dongArray.get(i).value);
                }
            }
        }

        protected void onPostExecute(Boolean result) {
            if (type == 1) {
                sidoAdapter= new SidoAdapter(context, 0, sidoArray);
                sidoList.setAdapter(sidoAdapter);
                sidoList.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        sidoCode = sidoArray.get(position).code;
                        textView01.setText(sidoArray.get(position).value + "  >  ");
                        textView02.setText("");
                        textView03.setText("");
                        Log.e("sidoCode", sidoCode);
                        bundle.putString("sido", sidoArray.get(position).value);

                        GetTask task = new GetTask("http://www.kma.go.kr/DFSROOT/POINT/DATA/mdl."+sidoCode+".json.txt", 2);
                        Log.e("URL", "http://www.kma.go.kr/DFSROOT/POINT/DATA/mdl."+sidoCode+".json.txt");
                        task.execute();
                    }
                });
                sidoAdapter.notifyDataSetChanged();
//				lastUpdateView.setText(resultStr);
            }
            else if (type == 2) {
                gunguAdapter= new GunguAdapter(context, 0, gunguArray);
                gunguList.setAdapter(gunguAdapter);
                gunguList.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        gunguCode = gunguArray.get(position).code;
                        textView02.setText(gunguArray.get(position).value + "  >  ");
                        Log.e("gunguCode", gunguCode);

                        bundle.putString("gungu", gunguArray.get(position).value);

                        GetTask task = new GetTask("http://www.kma.go.kr/DFSROOT/POINT/DATA/leaf."+gunguCode+".json.txt", 3);
                        Log.e("URL", "http://www.kma.go.kr/DFSROOT/POINT/DATA/leaf."+gunguCode+".json.txt");
                        task.execute();
                    }
                });
                gunguAdapter.notifyDataSetChanged();
//				lastUpdateView.setText(resultStr);
            }
            else if (type == 3) {
                dongAdapter= new DongAdapter(context, 0, dongArray);
                dongList.setAdapter(dongAdapter);
                dongList.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        dongCode = dongArray.get(position).code;
                        textView03.setText(dongArray.get(position).value);
                        Log.e(dongArray.get(position).value, dongCode);
                        bundle.putString("dong", dongArray.get(position).value);
                        bundle.putString("code", dongArray.get(position).code);
                        urlXml = "http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone="+dongCode;
                    }
                });
                dongAdapter.notifyDataSetChanged();
//				lastUpdateView.setText(resultStr);
            }
        };
    }

    class SidoAdapter extends ArrayAdapter<SidoDataObject> {
        private LayoutInflater inflater;
        private View adapterView;
        private ViewHolder viewHolder;

        public SidoAdapter(Context context, int resource, List<SidoDataObject> objects) {
            super(context, resource, objects);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SidoDataObject data = sidoArray.get(position);

            if (convertView == null) {
                adapterView = inflater.inflate(kr.co.daehee.weather.R.layout.listview_item, null);
                viewHolder = new ViewHolder();
                viewHolder.textView = (TextView)adapterView.findViewById(kr.co.daehee.weather.R.id.textView);
                adapterView.setTag(viewHolder);
            }else {
                Log.e("Position", "else=>" +  position);
                adapterView = convertView;
                viewHolder = (ViewHolder)adapterView.getTag();
            }
            if (data != null) {
                viewHolder.textView.setText(data.value);
            }
            return adapterView;
        }
    }

    class GunguAdapter extends ArrayAdapter<GunguDataObject> {
        private LayoutInflater inflater;
        private View adapterView;
        private ViewHolder viewHolder;

        public GunguAdapter(Context context, int resource, List<GunguDataObject> objects) {
            super(context, resource, objects);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GunguDataObject data = gunguArray.get(position);

            if (convertView == null) {
                adapterView = inflater.inflate(kr.co.daehee.weather.R.layout.listview_item, null);
                viewHolder = new ViewHolder();
                viewHolder.textView = (TextView)adapterView.findViewById(kr.co.daehee.weather.R.id.textView);
                adapterView.setTag(viewHolder);
            }else {
                Log.e("Position", "else=>" +  position);
                adapterView = convertView;
                viewHolder = (ViewHolder)adapterView.getTag();
            }
            if (data != null) {
                viewHolder.textView.setText(data.value);
            }
            return adapterView;
        }
    }

    class DongAdapter extends ArrayAdapter<DongDataObject> {
        private LayoutInflater inflater;
        private View adapterView;
        private ViewHolder viewHolder;

        public DongAdapter(Context context, int resource, List<DongDataObject> objects) {
            super(context, resource, objects);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DongDataObject data = dongArray.get(position);

            if (convertView == null) {
                adapterView = inflater.inflate(kr.co.daehee.weather.R.layout.listview_item, null);
                viewHolder = new ViewHolder();
                viewHolder.textView = (TextView)adapterView.findViewById(kr.co.daehee.weather.R.id.textView);
                adapterView.setTag(viewHolder);
            }else {
                Log.e("Position", "else=>" +  position);
                adapterView = convertView;
                viewHolder = (ViewHolder)adapterView.getTag();
            }
            if (data != null) {
                viewHolder.textView.setText(data.value);
            }
            return adapterView;
        }
    }

    class ViewHolder {
        TextView textView;
    }

    class MyViewHolder {
        TextView dayView;
        TextView hourView;
        TextView tempView;
        TextView tmxView;
        TextView tmnView;
        TextView rehView;
        TextView wfKorView;
        TextView popView;
        TextView wsView;
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
                viewHolder.tmxView = (TextView)adapterView.findViewById(kr.co.daehee.weather.R.id.tmx_view);
                viewHolder.tmnView = (TextView)adapterView.findViewById(kr.co.daehee.weather.R.id.tmn_view);
                viewHolder.rehView = (TextView)adapterView.findViewById(kr.co.daehee.weather.R.id.reh_view);
                viewHolder.wfKorView = (TextView)adapterView.findViewById(kr.co.daehee.weather.R.id.wfKor_view);
                viewHolder.popView = (TextView)adapterView.findViewById(kr.co.daehee.weather.R.id.pop_view);
                viewHolder.wsView = (TextView)adapterView.findViewById(kr.co.daehee.weather.R.id.ws_view);
                adapterView.setTag(viewHolder);
            }else {
                Log.e("Position", "else=>" +  position);
                adapterView = convertView;
                viewHolder = (MyViewHolder)adapterView.getTag();
            }
            if (data != null) {
                viewHolder.dayView.setText("날짜 : " + data.getDay());
                viewHolder.hourView.setText("시간 : " + (data.getHour()-3)+"시 ~ "+data.getHour() + "시");
                viewHolder.tempView.setText("온도 : " + data.getTemp()+"℃");
                viewHolder.tmxView.setText("최고온도 : " + data.getTmx()+"℃");
                viewHolder.tmnView.setText("최저온도 : " + data.getTmn()+"℃");
                viewHolder.rehView.setText("습도 : " + data.getReh()+"%");
                viewHolder.wfKorView.setText("날씨 : " + data.getWfKor());
                viewHolder.popView.setText("강수확률 : " + data.getPop()+"%");
                viewHolder.wsView.setText("풍속 : " + data.getWs() + "m/s");
            }
            return adapterView;
        }
    }

    OnClickListener myClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == kr.co.daehee.weather.R.id.button_cancel) {
                SelectActivity.this.finish();
            }
            else if (v.getId() == kr.co.daehee.weather.R.id.button_ok) {
                if (bundle != null) {
                    String str = bundle.getString("sido") + " " + bundle.getString("gungu") + " " + bundle.getString("dong");
                    ContentValues values = new ContentValues();
                    // 키,값의 쌍으로 데이터 입력
                    values.put("name", str);
                    values.put("code", bundle.getString("code"));
                    db.insert("mytable", null, values);
                    select();
                    intent.putExtras(bundle);
                    SelectActivity.this.setResult(RESULT_OK, intent);
                    SelectActivity.this.finish();
                }

            }
        }
    };

    void select () {
        Cursor c = db.query("mytable", null, null, null, null, null, null);
        int count = c.getCount();
        Log.d(tag, "레코드 갯수:"+count);
        while(c.moveToNext()) {
            int _id = c.getInt(0);
            String name = c.getString(1);
            String code = c.getString(2);
            Log.d(tag, "_id:" + _id + ",name:" + name + ",code:" + code);
        }
    }
}
