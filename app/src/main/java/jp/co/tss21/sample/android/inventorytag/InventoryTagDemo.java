package jp.co.tss21.sample.android.inventorytag;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * jp.co.tss21.uhfrfid.dotr_androidパッケージを参照します
 */
import jp.co.tss21.uhfrfid.dotr_android.EnChannel;
import jp.co.tss21.uhfrfid.dotr_android.EnMemoryBank;
import jp.co.tss21.uhfrfid.dotr_android.EnSession;
import jp.co.tss21.uhfrfid.dotr_android.EnTagAccessFlag;
import jp.co.tss21.uhfrfid.dotr_android.TagAccessParameter;
import jp.co.tss21.uhfrfid.tssrfid.TssRfidUtill;
import jp.co.tss21.uhfrfid.dotr_android.EnMaskFlag;
import jp.co.tss21.uhfrfid.dotr_android.OnDotrEventListener;

//import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.graphics.Paint;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;


/**
 * メイン
 *
 * @author Tohoku Systems Support.Co.,Ltd.
 */
public class InventoryTagDemo extends TabActivity implements View.OnClickListener,OnDotrEventListener {
    private final static String TAG = InventoryTagDemo.class.getSimpleName();

    private TabHost mTabHost;

    /** 設定タブ用 */
    private Button mBtnScan;
    private TextView mTxtDeviceAddr;
    private TextView mTxtDeviceName;
    private Button mBtnDevForward;
    private RadioGroup mRadioGroupMapSelect;
    private RadioGroup mRadioGroupSurfaceSelect;
    int inv_map_flg =0;
    /*
    0:学生室
    1:イノベ
    2:工場
     */
    private int surface_flg = 0;

    /*
    0:3面　
    1:4面
    */

    /** CALタブ用 */
    private Button mBtnOptBack;
    private Button mBtnOptForward;

    private RadioGroup mRadioGroupCalSelect;




    private double cal_rssi1 = -52;//RSSI基準値
    private double cal_rssi2 = -60;
    private double cal_dis1 = 2;//距離基準値
    private double cal_dis2 = 4;
    private double sub_const = 20;//減衰定数N

    double read_height =1;//読み取り高さ
    double add_height = 2.8;//アドレスRFIDタグの高さ

    int cal_flg = 0;
    /*
    1:1回目cal
    2:2回目cal
     */
    //20221014 距離計算手法切替用フラグ
    int ranging_method_flg = 0;
    /*
    0:2点CAL
    1:多点CAL
     */




    private final ArrayList<String> epc_cal = new ArrayList<String>();
    private Spinner CALEPC;



    /** 保管タブ用 */

    private Button mBtnMaskBack;
    private Button mBtnMaskForward;

    //初期値はマップ「研究室」対応
    private int dotm_x = 237;//1mあたりのピクセルの数(x) 20220217
    private int dotm_y = 237;//1mあたりのピクセルの数(y) 20220217
    private int origin_x = 290;//マップ上の原点x座標（左手系）20220223
    private int origin_y = 2955;//マップ上の原点y座標（左手系）20220223

    private String store_goods_epc = "";
    //アドレスRFIDタグ座標設定[m]
    private double add_1_x = 5.0;
    private double add_1_y = 3.0;
    private double add_2_x = 5.0;
    private double add_2_y = 6.0;
    private double add_3_x = 5.0;
    private double add_3_y = 11.0;
    //範囲円半径[m]
    private double range_x_m =3.0;
    private double range_y_m = 3.0;

    

    private Bitmap invMap;// = mapData(R.drawable.laboratory);

    private String epc_inv = "";



    int store_flg = 0;
    /*
    1:物品読み取り
    2:アドレスRFIDタグ読み取り
     */



    /** ログタブ用 */
    private Button mBtnLogClear;
    private Button mBtnConnect;
    private Button mBtnDisconnect;
    private Button mBtnCAL;
    private Button mBtnStore;
    private Button mBtnLog;
    private ListView mListLog;
    private ArrayList<HashMap<String, String>> mAarryLog;
    private BaseAdapter mAdapterLog;

    private TssRfidUtill mDotrUtil;

    /** 物品登録タブ用*/
    private Button mBtnGoodsRegi;
    private Button mBtnGoodsWrite;
    private int goods_flg = 0;
    /*
     1:書き込み対象のタグの読み取り
     2:タグへの書込み
     */
    private String write_mask_epc;
    private String txt_write;


    /**検索タブ用     */
    private Button mBtnSearch;

    private ArrayList<String> search_goods_name = new ArrayList<String>();//検索対象の名前
    private ArrayList<String> search_goods_epc = new ArrayList<String>();//検索対象のEPC
    private ArrayList<String> search_goods_lasttime = new ArrayList<String>();//検索対象の保管時の時間
    private ArrayList<String> search_goods_coorx = new ArrayList<String>();//検索対象のx座標
    private ArrayList<String> search_goods_coory = new ArrayList<String>();//検索対象のy座標
    private ArrayList<String> search_goods_map = new ArrayList<String>();//検索対象のマップ番号
    private Spinner SEARCH_GOODS_NAME;

    private int search_flg = 0;
    private int search_test = 0;//起動時の描画を防ぐフラグ20220929



    /** ADD登録タブ用*/

    /**public変数　20211129*/
    public String epcoc = ""; //InventoryEPCでのEPC番号

    public ArrayList addlist = new ArrayList();
    public ArrayList addlist_m = new ArrayList();
    public ArrayList epcadd= new ArrayList();

    public double setRSSI = 0;
    //public double setPhase = 0;
    public String RSSI = "";
    //public String Phase = "";
    public int flg = 0;
    /*
    CAL：1
    保管：2
    検索：3
    物品登録:4

     */

    /**テスト用 20220121**/
    private Color search = new Color();



    /**
     * Called when the activity is first created.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting:
                //
                Intent intent = new Intent(this, setting.class);
                startActivity(intent);

                //startActivity(new Intent(this, setting.class));

                return true;
            case R.id.goods_reg:

                return true;
            case R.id.add_reg:

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Resources res = getResources();
        setupPieChart();

        mTabHost = getTabHost();
        mTabHost.addTab(mTabHost.newTabSpec("config").setIndicator("設定", res.getDrawable(R.drawable.ic_volume_bluetooth_in_call)).setContent(R.id.tab1));
        mTabHost.addTab(mTabHost.newTabSpec("cal").setIndicator("CAL", res.getDrawable(R.drawable.ic_menu_mark)).setContent(R.id.tab2));
        mTabHost.addTab(mTabHost.newTabSpec("store").setIndicator("保管", res.getDrawable(R.drawable.ic_menu_myplaces)).setContent(R.id.tab3));
        mTabHost.addTab(mTabHost.newTabSpec("search").setIndicator("検索", res.getDrawable(R.drawable.ic_menu_info_details)).setContent(R.id.tab4));
        mTabHost.addTab(mTabHost.newTabSpec("goods_regi").setIndicator("物品登録", res.getDrawable(R.drawable.ic_menu_info_details)).setContent(R.id.tab5));
        mTabHost.addTab(mTabHost.newTabSpec("addtag_regi").setIndicator("ADD登録", res.getDrawable(R.drawable.ic_menu_info_details)).setContent(R.id.tab6));
        mTabHost.setCurrentTab(0);

        /** 設定タブ用 */
        mBtnScan = (Button) findViewById(R.id.btn_scan);
        mBtnScan.setOnClickListener(this);
        mTxtDeviceName = (TextView) findViewById(R.id.txt_device_name);
        mTxtDeviceAddr = (TextView) findViewById(R.id.txt_device_address);
        mTxtDeviceName.setText(getAutoConnectDeviceName());
        mTxtDeviceAddr.setText(getAutoConnectDeviceAddress());

        //マップ番号取得20221025
        mRadioGroupMapSelect = (RadioGroup)findViewById(R.id.rdgroup_map_select);
        mRadioGroupMapSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId != -1) {
                    // 選択されているラジオボタンの取得
                    RadioButton radioButton = (RadioButton) findViewById(checkedId);
                    inv_map_flg = 2131230831 - checkedId;
                    Log.d("マップ選択", String.valueOf(inv_map_flg));
                    if(inv_map_flg==0){//マップ「研究室」20211108

                        dotm_x = 237;
                        dotm_y = 237;
                        origin_x = 290;
                        origin_y = 2955;

                        add_1_x=5.0;
                        add_1_y=3.0;
                        add_2_x=5.0;
                        add_2_y=6.0;
                        add_3_x=5.0;
                        add_3_y=11.0;

                        invMap = mapData(R.drawable.laboratory);
                    }
                    else if(inv_map_flg==1){//マップ「E5棟8Fイノベーションルーム」20211108
                        dotm_x = 62;
                        dotm_y = 62;
                        origin_x = 40;
                        origin_y = 855;

                        add_1_x=6.0;
                        add_1_y=2.0;
                        add_2_x=12.0;
                        add_2_y=12.0;
                        add_3_x=18.0;
                        add_3_y=2.0;

                        invMap = mapData(R.drawable.e5_8f_inov);
                    }
                    else if(inv_map_flg==2){//マップ「工場」20211108
                        inv_map_flg=2;

                        dotm_x = 186;
                        dotm_y = 186;
                        origin_x = 560;
                        origin_y = 2510;

                        add_1_x=18.0;
                        add_1_y=0.0;
                        add_2_x=6.0;
                        add_2_y=10.0;
                        add_3_x=18.0;
                        add_3_y=10.0;

                        invMap = mapData(R.drawable.ibaden_factory);
                    }
                } else {
                    // 何も選択されていない場合の処理
                }
            }
        });
        //金属板の面数20221025
        mRadioGroupSurfaceSelect = (RadioGroup)findViewById(R.id.rdgroup_surface_select);
        mRadioGroupSurfaceSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId != -1){
                    surface_flg = checkedId - 2131230832;
                    Log.d("面数選択", String.valueOf(surface_flg));

                    setAddTag();
                }
                else{

                }
            }

        });
        setAddTag();

        /** CALタブ用 */
        mBtnCAL = (Button) findViewById(R.id.btn_cal);
        mBtnCAL.setOnClickListener(this);
        mRadioGroupCalSelect = (RadioGroup)findViewById(R.id.rdgroup_cal_select);
        mRadioGroupCalSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId != -1) {
                    ranging_method_flg = 2131230828 - checkedId;
                    Log.d("測距手法選択", String.valueOf(ranging_method_flg));
                } else {

                }
            }
        });


        //20220920
        //20220913 sp_cal_epc SpinnerによるCAL時のマスク設定
        File file_cal = new File("/data/data/" + this.getPackageName() + "/files/epc_add_4.csv");
        FileReader buff_cal = null;
        try {
            buff_cal = new FileReader(file_cal);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader fr_cal = new BufferedReader(buff_cal);
        String line_cal = null;
        try {
            line_cal = fr_cal.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (line_cal != null){
            String[] str = line_cal.split(",");
            epc_cal.add(str[1]);
            try {
                line_cal = fr_cal.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            fr_cal.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        //20220913 Spinner追加
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                epc_cal
        );
        CALEPC = (Spinner) findViewById(R.id.sp_cal_epc);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        CALEPC.setAdapter(adapter);
        CALEPC.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //　アイテムが選択された時
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                Spinner spinner = (Spinner)parent;
                String item = (String)spinner.getSelectedItem();
            }

            //　アイテムが選択されなかった
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });
        epc_cal.add("");//20220913






        /** 保管タブ用 */
        mBtnStore = (Button) findViewById(R.id.btn_store);
        mBtnStore.setOnClickListener(this);


        /** ログタブ用 */
        mBtnConnect = (Button) findViewById(R.id.btn_connect);
        mBtnConnect.setOnClickListener(this);
        mBtnDisconnect = (Button) findViewById(R.id.btn_disconnect);
        mBtnDisconnect.setOnClickListener(this);
        mBtnLogClear = (Button) findViewById(R.id.btn_log_clear);
        mBtnLogClear.setOnClickListener(this);
        mBtnLog = (Button) findViewById(R.id.btn_log);
        mBtnLog.setOnClickListener(this);

        /** 検索タブ用  */
        mBtnSearch = (Button) findViewById(R.id.btn_search);
        mBtnSearch.setOnClickListener(this);

        //Spinner追加（物品選択）20220920
        File file_goods = new File("/data/data/" + this.getPackageName() + "/files/epc_goods.csv");
        FileReader buff_goods = null;
        try {
            buff_goods = new FileReader(file_goods);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader fr_goods = new BufferedReader(buff_goods);
        String line_goods = null;
        try {
            line_goods = fr_goods.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SearchListClear();
        while (line_goods != null){
            String[] str = line_goods.split(",");
            search_goods_name.add(str[0]);
            search_goods_epc.add(str[1]);
            search_goods_lasttime.add(str[2]);
            search_goods_coorx.add(str[3]);
            search_goods_coory.add(str[4]);
            search_goods_map.add(str[5]);
            try {
                line_goods = fr_goods.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            fr_goods.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //20220913 Spinner追加
        final TextView goods_epc = (TextView) findViewById(R.id.txt_search_epc);//画面に物品のEPC表示
        final TextView goods_lasttime = (TextView) findViewById(R.id.txt_search_lasttime);//保管時の時間を表示
        ArrayAdapter<String> adapter_goods = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                search_goods_name
        );
        final ImageView search_map = (ImageView) this.findViewById(R.id.img_search);//検索マップ20220929

        SEARCH_GOODS_NAME = (Spinner) findViewById(R.id.sp_search_goods);
        adapter_goods.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SEARCH_GOODS_NAME.setAdapter(adapter_goods);
        SEARCH_GOODS_NAME.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //　アイテムが選択された時
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                Spinner spinner = (Spinner)parent;
                String item = (String)spinner.getSelectedItem();
                goods_epc.setText(search_goods_epc.get(spinner.getSelectedItemPosition()));
                goods_lasttime.setText(search_goods_lasttime.get(spinner.getSelectedItemPosition()));

                //検索マップ描画パラメータ
                int search_x = Integer.parseInt(search_goods_coorx.get(spinner.getSelectedItemPosition()));
                int search_y = Integer.parseInt(search_goods_coory.get(spinner.getSelectedItemPosition()));
                int search_dotm_x = 0;
                int search_dotm_y = 0;
                int search_map_flg = Integer.parseInt(search_goods_map.get(spinner.getSelectedItemPosition()));
                if(search_map_flg==0){
                    search_dotm_x = 237;
                    search_dotm_y = 237;
                }
                else if(search_map_flg==1){
                    search_dotm_x = 62;
                    search_dotm_y = 62;
                }
                else if(search_map_flg==2){
                    search_dotm_x = 186;
                    search_dotm_y = 186;
                }
                int search_r_x = (int) (range_x_m * search_dotm_x);
                int search_r_y = (int) (range_y_m * search_dotm_y);

                //描画リセット20220502
                Canvas canvas;
                canvas = new Canvas(invMap);
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                //マップ設定20220929
                // ImageViewに作成したBitmapをセット
                invMap = setMap(search_map_flg);
                search_map.setImageBitmap(invMap);
                Canvas canvas_search;
                canvas_search = new Canvas(invMap);

                Paint search_paint = new Paint();
                search_paint.setColor(Color.argb(128, 255, 255, 0));
                search_paint.setStyle(Paint.Style.FILL);

                canvas_search.drawArc(search_x-search_r_x, search_y-search_r_y, search_x+search_r_x, search_y+search_r_y, 0, 360, false, search_paint);
                //canvas_search.drawArc(0, 0, 1000, 1000, 0, 360, false, search_paint);

            }

            //　アイテムが選択されなかった
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });



        /** 物品登録タブ用*/
        mBtnGoodsRegi = (Button) findViewById(R.id.btn_goods_regi);
        mBtnGoodsRegi.setOnClickListener(this);
        mBtnGoodsWrite = (Button) findViewById(R.id.btn_goods_write);
        mBtnGoodsWrite.setOnClickListener(this);


        /** ADD登録タブ用*/



        initLog();
        //setEnable();

        //DOTR_Utilのインスタンスを生成します
        mDotrUtil = new TssRfidUtill();

        //ベントリスナーを設定する
        mDotrUtil.setOnDotrEventListener(this);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            finish();
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        }



        //物品読み取り時電力変更時のシークバーの挙動20220120
        final SeekBar sb0 = (SeekBar)findViewById(R.id.db_goods);
        final TextView tv0 = (TextView)findViewById(R.id.db_goods_value);
        // シークバーの初期値をTextViewに表示
        tv0.setText(sb0.getProgress() + "[dBm]");
        sb0.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progress, boolean fromUser) {
                        // ツマミをドラッグしたときに呼ばれる
                        tv0.setText(sb0.getProgress() + "[dBm]");
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // ツマミに触れたときに呼ばれる
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // ツマミを離したときに呼ばれる
                    }
                }
        );

        //物品読み取り時サイクル変更時のシークバーの挙動20220120
        final SeekBar sb1 = (SeekBar)findViewById(R.id.ms_goods);
        final TextView tv1 = (TextView)findViewById(R.id.ms_goods_value);
        // シークバーの初期値をTextViewに表示
        tv1.setText(sb1.getProgress()+40 + "[msec]");
        sb1.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progress, boolean fromUser) {
                        // ツマミをドラッグしたときに呼ばれる
                        tv1.setText(sb1.getProgress()+40 + "[msec]");
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // ツマミに触れたときに呼ばれる
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // ツマミを離したときに呼ばれる
                    }
                }
        );

        //アドレスタグ読み取り時電力変更時のシークバーの挙動20220120
        final SeekBar sb2= (SeekBar)findViewById(R.id.db_add);
        final TextView tv2 = (TextView)findViewById(R.id.db_add_value);
        // シークバーの初期値をTextViewに表示
        tv2.setText(sb2.getProgress() + "[dBm]");
        sb2.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progress, boolean fromUser) {
                        // ツマミをドラッグしたときに呼ばれる
                        tv2.setText(sb2.getProgress() + "[dBm]");
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // ツマミに触れたときに呼ばれる
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // ツマミを離したときに呼ばれる
                    }
                }
        );

        //アドレスタグ読み取り時サイクル変更時のシークバーの挙動20220120
        final SeekBar sb3 = (SeekBar)findViewById(R.id.ms_add);
        final TextView tv3 = (TextView)findViewById(R.id.ms_add_value);
        // シークバーの初期値をTextViewに表示
        tv3.setText(sb3.getProgress()+40 + "[msec]");
        sb3.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progress, boolean fromUser) {
                        // ツマミをドラッグしたときに呼ばれる
                        tv3.setText(sb3.getProgress()+40 + "[msec]");
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // ツマミに触れたときに呼ばれる
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // ツマミを離したときに呼ばれる
                    }
                }
        );
        //マップデフォルト（落ち防止）20220502
        invMap = mapData(R.drawable.laboratory);




    }




    /**
     * イベントハンドラを記載します
     */




    @Override

    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.btn_scan: //デバイス検索ボタン
                Intent intent = new Intent(getApplicationContext(), ListBtDeviceActivity.class);
                startActivityForResult(intent, ListBtDevice_Activity);
                break;
            case R.id.btn_connect:  //接続ボタン
                if(mDotrUtil.isConnect()) {
                    setting();
                } else {
                    // 接続前にcontextとデバイス名（リーダー名）を設定する。
                    mDotrUtil.initReader(this, getAutoConnectDeviceName());
                if (mDotrUtil.connect(getAutoConnectDeviceAddress())) {
                        setting();
                    } else {
                        showToastByOtherThread("接続に失敗しました。", Toast.LENGTH_SHORT);
                    }
                }
                break;
            case R.id.btn_disconnect:   //切断ボタン
                if(mDotrUtil.isConnect()){
                    if(!mDotrUtil.disconnect())
                        showToastByOtherThread("解除に失敗しました。", Toast.LENGTH_SHORT);
                }
                break;
            case R.id.btn_log_clear:    //ログ消去ボタン
                resetLog();
                break;
            case R.id.btn_cal:

                flg=1;//CALフラグ設定20220209
                cal_flg=1;//1回目読み取り設定
                store_flg=0;
                search_flg=0;
                goods_flg=0;

                SettingsClear();

                //20220216CAL時
                final SeekBar sb_db_add = (SeekBar)findViewById(R.id.db_add);
                final SeekBar sb_ms_add = (SeekBar)findViewById(R.id.ms_add);
                mDotrUtil.setRadioPower((int)(30-sb_db_add.getProgress()));
                mDotrUtil.setTxCycle((int)sb_ms_add.getProgress()+40);

                TextView tv = (TextView) findViewById(R.id.a_rssi_cal);
                tv.setBackgroundColor(Color.argb(40, 255, 255, 0));


                //20220913 sp_cal_epc SpinnerによるCAL時のマスク設定
                epc_cal.clear();
                File file_cal = new File("/data/data/" + this.getPackageName() + "/files/epc_add_4.csv");
                FileReader buff_cal = null;
                try {
                    buff_cal = new FileReader(file_cal);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                BufferedReader fr_cal = new BufferedReader(buff_cal);
                String line_cal = null;
                try {
                    line_cal = fr_cal.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (line_cal != null){
                    String[] str = line_cal.split(",");
                    epc_cal.add(str[1]);
                    try {
                        line_cal = fr_cal.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    fr_cal.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                break;
            case R.id.btn_store:
                flg=2;//物品読取フラグ設定20220209
                cal_flg=0;
                store_flg=1;//物品読取
                search_flg=0;
                goods_flg=0;

                SettingsClear();

                final SeekBar sb_db_goods = (SeekBar)findViewById(R.id.db_goods);
                final SeekBar sb_ms_goods = (SeekBar)findViewById(R.id.ms_goods);
                //20220216物品読み取り時
                mDotrUtil.setRadioPower((int)(30-sb_db_goods.getProgress()));
                mDotrUtil.setTxCycle((int)sb_ms_goods.getProgress()+40);

                //マップ設定20220502

                //画像描画20220216
                ImageView inv_sector = (ImageView) this.findViewById(R.id.img_inv);
                // ImageViewに作成したBitmapをセット
                inv_sector.setImageBitmap(invMap);
                Canvas canvas;
                canvas = new Canvas(invMap);

                //物品名色変更20220502
                TextView st_sakuban = (TextView) findViewById(R.id.txt_sakuban_store);
                TextView st_tyuban = (TextView) findViewById(R.id.txt_tyuban_store);
                st_sakuban.setBackgroundColor(Color.argb(40, 255, 255, 0));
                st_tyuban.setBackgroundColor(Color.argb(40, 255, 255, 0));
                //Paint paint = new Paint();
                //paint.setColor(Color.YELLOW);
                //canvas.drawRect(40, 115, 1510, 855, paint);

                break;

            case R.id.btn_search:;
                flg=3;//検索
                cal_flg=0;
                store_flg=0;
                search_flg=1;
                goods_flg=0;

                SettingsClear();

                //Spinner追加（物品選択）20220920
                ArrayAdapter<String> adapter_goods_reset = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        search_goods_name
                );
                adapter_goods_reset.clear();//リセット20221027




                File file_goods = new File("/data/data/" + this.getPackageName() + "/files/epc_goods.csv");
                FileReader buff_goods = null;
                try {
                    buff_goods = new FileReader(file_goods);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                BufferedReader fr_goods = new BufferedReader(buff_goods);
                String line_goods = null;
                try {
                    line_goods = fr_goods.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                SearchListClear();
                while (line_goods != null){
                    String[] str = line_goods.split(",");
                    search_goods_name.add(str[0]);
                    search_goods_epc.add(str[1]);
                    search_goods_lasttime.add(str[2]);
                    search_goods_coorx.add(str[3]);
                    search_goods_coory.add(str[4]);
                    search_goods_map.add(str[5]);
                    try {
                        line_goods = fr_goods.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    fr_goods.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //20220913 Spinner追加
                final TextView goods_epc = (TextView) findViewById(R.id.txt_search_epc);//画面に物品のEPC表示
                final TextView goods_lasttime = (TextView) findViewById(R.id.txt_search_lasttime);//保管時の時間を表示
                ArrayAdapter<String> adapter_goods = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        search_goods_name
                );
                final ImageView search_map = (ImageView) this.findViewById(R.id.img_search);//検索マップ20220929

                SEARCH_GOODS_NAME = (Spinner) findViewById(R.id.sp_search_goods);
                adapter_goods.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                SEARCH_GOODS_NAME.setAdapter(adapter_goods);






                break;
            case R.id.btn_goods_regi://物品保管時タグの読み取り20220620
                flg=4;//物品保管
                cal_flg=0;
                store_flg=0;
                search_flg=0;
                goods_flg=1;//読取

                SettingsClear();


                TextView target = (TextView) findViewById(R.id.txt_write_target);
                TextView sakuban = (TextView) findViewById(R.id.txt_sakuban_regi);
                TextView tyuban = (TextView) findViewById(R.id.txt_tyuban_regi);
                TextView write = (TextView) findViewById(R.id.txt_write_epc);
                TextView written = (TextView) findViewById(R.id.txt_written_goods);
                TextView order = (TextView) findViewById(R.id.txt_goods_regi_order);
                //テキストの背景色変更
                target.setBackgroundColor(Color.argb(40, 255, 255, 0));
                sakuban.setBackgroundColor(Color.argb(0, 255, 255, 0));
                tyuban.setBackgroundColor(Color.argb(0, 255, 255, 0));
                write.setBackgroundColor(Color.argb(0, 255, 255, 0));
                written.setBackgroundColor(Color.argb(0, 255, 255, 0));
                //指示文の表示1
                order.setText("登録対象のタグを読み取ってください");
                break;
            case R.id.btn_goods_write://物品タグへの書込み20220620
                flg=4;//物品保管
                goods_flg=2;//書き込み

                target = (TextView) findViewById(R.id.txt_write_target);
                sakuban = (TextView) findViewById(R.id.txt_sakuban_regi);
                tyuban = (TextView) findViewById(R.id.txt_tyuban_regi);
                write = (TextView) findViewById(R.id.txt_write_epc);
                written = (TextView) findViewById(R.id.txt_written_goods);
                order = (TextView) findViewById(R.id.txt_goods_regi_order);

                //テキストの背景色変更
                target.setBackgroundColor(Color.argb(0, 255, 255, 0));
                sakuban.setBackgroundColor(Color.argb(0, 255, 255, 0));
                tyuban.setBackgroundColor(Color.argb(0, 255, 255, 0));
                write.setBackgroundColor(Color.argb(40, 255, 255, 0));
                written.setBackgroundColor(Color.argb(0, 255, 255, 0));
                //指示文の表示3
                order.setText("再度、長めに登録対象のタグを読み取ってください");


                EditText et_sakuban = (EditText) findViewById(R.id.txt_sakuban_regi);
                final String txt_sakuban = et_sakuban.getText().toString();
                EditText et_tyuban = (EditText) findViewById(R.id.txt_tyuban_regi);
                final String txt_tyuban = et_tyuban.getText().toString();

                //文字列のエンコード
                String input_data = txt_sakuban + "/" + txt_tyuban;
                StringBuilder write_data = new StringBuilder();
                write_data.append("f");
                char c;
                for(int i=0;i<input_data.length();i++){
                    c=input_data.charAt(i);
                    if(Character.isDigit(c)){
                        write_data.append(c);
                    }
                    else{
                        File file = new File("/data/data/" + this.getPackageName() + "/files/char_allocation.csv");
                        FileReader buff = null;
                        try {
                            buff = new FileReader(file);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        String line_ = null;
                        BufferedReader fr = new BufferedReader(buff);
                        try {
                            line_ = fr.readLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        while (line_ != null) {
                            String[] data = line_.split(",");
                            if (String.valueOf(c).equals(data[2])) {
                                write_data.append(data[0] + data[1]);
                                break;
                            }
                            try {
                                line_ = fr.readLine();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }


                }
                int data_pre =24 - write_data.length();
                for(int i=1;i<=data_pre;i++){
                    write_data.insert(0, "0");
                }
                write.setText(write_data);



                break;
        }
    }

    //主に別スレッドを考慮したイベントハンドラを記載します
    public static final int MSG_QUIT = 9999;
    public static final int MSG_SHOW_TOAST = 20;
    public static final int MSG_BACK_COLOR = 21;
    public static final int MSG_INVENTORY_TAG = 22;
    public static final int MSG_FIRM = 23;

    private InventoryTagHandler mHandler = new InventoryTagHandler(InventoryTagDemo.this);

    /**
     * InventoryTagDemo用ハンドラ
     */
    static class InventoryTagHandler extends Handler {
        private final WeakReference<InventoryTagDemo> myActivity;

        InventoryTagHandler(InventoryTagDemo activity) {
            myActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            InventoryTagDemo activity = myActivity.get();
            switch (msg.what) {
                case MSG_QUIT:
                    activity.closeApp();
                    break;
                case MSG_SHOW_TOAST:
                    Toast.makeText(activity, (String) msg.obj,msg.arg1).show();
                    break;
                case MSG_BACK_COLOR:
                    activity.mTabHost.setBackgroundColor(msg.arg2);//背景色変更
                    break;
                case MSG_INVENTORY_TAG:
                case MSG_FIRM:
                    HashMap<String, String> item = new HashMap<>();
                    item.put("epc", (String)msg.obj);
                    activity.mAarryLog.add(item);
                    activity.mAdapterLog.notifyDataSetChanged();
                    activity.mListLog.setSelection( activity.mAarryLog.size() - 1 );//最後の行に移動
                    break;
            }
        }
    }


    private void closeApp() {
        finish();
    }


    private void postCloseApp() {
        mHandler.sendEmptyMessageDelayed(MSG_QUIT, 1000);
    }


    private void showToastByOtherThread(String msg, int time) {
        mHandler.removeMessages(MSG_SHOW_TOAST);
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_TOAST, time, 0, msg));
    }


    /* (非 Javadoc)
     * @see android.app.Activity#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        if (mDotrUtil != null) {
            if (mDotrUtil.isConnect()) {
                mDotrUtil.disconnect();
            }
        }

        super.finalize();
    }


    /* (非 Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        new AlertDialog.Builder(this)
                .setTitle("終了確認")
                .setMessage("プログラムを終了しますか？")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        InventoryTagDemo.this.postCloseApp();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).create().show();
    }
    public void onClickInv(){
        Paint paint = new Paint();
        paint.setColor(Color.YELLOW);


    }


    /** ---------------------------------------- ----------------------------------------
     * デバイスタブ用
     * ---------------------------------------- ---------------------------------------- */
    void setting() {

        //一括読取り時の取得データ設定
//        mDotrUtil.setInventoryReportMode(mChkDateTime.isChecked(), mChkRadioPower.isChecked());
        mDotrUtil.setInventoryReportMode(
                        false,
                        true,
                        false,
                        false,
                        false);
        mDotrUtil.setTagAccessFlag(EnTagAccessFlag.FlagAandB);//フラグの設定20220209
        mDotrUtil.setRadioChannel(EnChannel.ALL);//チャネルの設定（Ch5, Ch11, Ch17, Ch23, Ch24, Ch25のみ使用可能）20220209
        mDotrUtil.setSession(EnSession.Session0);//セッションの設定20220209


    }


    static final int ListBtDevice_Activity = 1;
    static final int REQUEST_ENABLE_BT = 2;

    /*
     * アクティビティの応答
     * (非 Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /** Bluetoothデバイスリスト画面からの応答の場合 */
        if (requestCode == ListBtDevice_Activity) {
            if (resultCode == RESULT_OK) {
                //選択したBluetoothデバイスを保存する。
                String addr = data.getExtras().getString(ListBtDeviceActivity.DEVICE_ADDRESS);
                String name = data.getExtras().getString(ListBtDeviceActivity.DEVICE_NAME);

                setAutoConnectDevice(name, addr);

                mTxtDeviceName.setText(name);
                mTxtDeviceAddr.setText(addr);

                showToastByOtherThread("デバイスを設定しました。", Toast.LENGTH_SHORT);
            } else if (resultCode == RESULT_CANCELED) {
                //
            }
        } else if(requestCode==REQUEST_ENABLE_BT){
            if (resultCode == RESULT_OK) {
                //Bluetooth有効
            } else if(resultCode==RESULT_CANCELED){
                //Bluetooth無効
            }
        }
    }


    /**
     * Bluetoothデバイスの名称およびMACアドレスを保存する
     *
     * @param strName    デバイス名称
     * @param strAddress MACアドレス
     */
    private void setAutoConnectDevice(String strName, String strAddress) {
        if (strName == null || strAddress == null) {
            return;
        }

        SharedPreferences pref = getSharedPreferences(TAG, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("auto_link_device_name", strName);
        editor.putString("auto_link_device_address", strAddress);
        editor.commit();
    }


    /**
     * BluetoothデバイスのMACアドレスを取得する。
     *
     * @return MACアドレス
     */
    private String getAutoConnectDeviceAddress() {
        SharedPreferences pref = getSharedPreferences(TAG, 0);
        return pref.getString("auto_link_device_address", "");
    }


    /**
     * Bluetoothデバイスの名称を取得する。
     *
     * @return デバイス名称
     */
    private String getAutoConnectDeviceName() {
        SharedPreferences pref = getSharedPreferences(TAG, 0);
        return pref.getString("auto_link_device_name", "");
    }


    /** ---------------------------------------- ----------------------------------------
     * ログタブ用
     * ---------------------------------------- ---------------------------------------- */
    private void initLog() {
        mListLog = (ListView) findViewById(R.id.list_log);
        if (mAarryLog == null) {
            mAarryLog = new ArrayList<>();
            mAdapterLog = new SimpleAdapter(this, mAarryLog,

                    R.layout.list_row,
                    new String[]{"epc", "count"},
                    new int[]{R.id.textView1});
            mListLog.setAdapter(mAdapterLog);
        }
    }


    private void resetLog() {
        mAarryLog.clear();
        mAdapterLog.notifyDataSetChanged();
    }


    /*
     * 接続完了イベント
     * (非 Javadoc)
     * @see jp.co.tss21.uhfrfid.dotr_android.OnDotrEventListener#onConnected()
     */
    @Override
    public void onConnected() {

        Log.d(TAG, "onConnected");

        resetLog();
        mHandler.sendMessage(mHandler.obtainMessage(MSG_BACK_COLOR, Toast.LENGTH_SHORT, getResources().getColor(R.color.background_connect), null));
        showToastByOtherThread("接続しました。", Toast.LENGTH_SHORT);




        mDotrUtil.setRadioPower(0);
        mDotrUtil.setTxCycle(40);


        String ver = "ファームウェア　";
        try {
            for (int ret = 0; ret <= 5; ret++){
                if (mDotrUtil.getFirmwareVersion()!= null) {
                    mDotrUtil.setQValue(0);
                    ver = ver + mDotrUtil.getFirmwareVersion();
                    break;
                }

                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //ファームウェアバージョンをログに表示
        mHandler.sendMessage(mHandler.obtainMessage(MSG_FIRM, 0, 0, ver));

        //アドレスRFIDタグの情報呼び出し20220209




        /*削除予定
        Spinner sp_cal_epc_ = (Spinner) findViewById(R.id.sp_cal_epc);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                epcadd
        );

         */


        //マップ情報デフォルト20220502
        //イノベ
        /*
        dotm_x = 237;
        dotm_y = 237;
        origin_x = 290;
        origin_y = 2955;

        add_1_x=5.0;
        add_1_y=0.0;
        add_2_x=5.0;
        add_2_y=4.0;
        add_3_x=1.5;
        add_3_y=7.5;


        invMap = mapData(R.drawable.laboratory);

         */


    }


    /*
     * 接続解除イベント
     * (非 Javadoc)
     * @see jp.co.tss21.uhfrfid.dotr_android.OnDotrEventListener#onDisconnected()
     */
    @Override
    public void onDisconnected() {
        Log.d(TAG, "onDisconnected");
        mDotrUtil = new TssRfidUtill();
        mDotrUtil.setOnDotrEventListener(this);
        mHandler.sendMessage(mHandler.obtainMessage(MSG_BACK_COLOR, Toast.LENGTH_SHORT, getResources().getColor(R.color.background_disconnect), null));
        showToastByOtherThread("切断しました。", Toast.LENGTH_SHORT);

        epcadd.clear();
    }


    /*
     * リンク切れイベント
     * (非 Javadoc)
     * @see jp.co.tss21.uhfrfid.dotr_android.OnDotrEventListener#onLinkLost()
     */
    @Override
    public void onLinkLost() {
        Log.d(TAG, "onLinkLost");

        mHandler.sendMessage(mHandler.obtainMessage(MSG_BACK_COLOR, Toast.LENGTH_SHORT, getResources().getColor(R.color.background_disconnect), null));
        showToastByOtherThread("リンクが切れました。", Toast.LENGTH_SHORT);
    }


    /*
     * トリガ状態変更イベント
     * (非 Javadoc)
     * @see jp.co.tss21.uhfrfid.dotr_android.OnDotrEventListener#onTriggerChaned(boolean)
     */
    @Override
    public void onTriggerChaned(boolean trigger) {
        Log.d(TAG, "onTriggerChaned(" + trigger + ")");

        if (trigger) {//トリガー引く




            //フラグごとの動作20220209
            if(flg==1){//cal
                //送信電力、読み取りサイクルの設定20220209
                final SeekBar sb_db_add = (SeekBar)findViewById(R.id.db_add);
                final SeekBar sb_ms_add = (SeekBar)findViewById(R.id.ms_add);
                mDotrUtil.setRadioPower((int)(30-sb_db_add.getProgress()));
                mDotrUtil.setTxCycle((int)sb_ms_add.getProgress()+40);

                if(cal_flg==1){


                }
                else if(cal_flg==2){



                }

                //マスク設定20220209
                //Spinnerにテキストが表示されないため、仮でTextViewを用いる

                String mask_epc = (String)CALEPC.getSelectedItem();
                mDotrUtil.setTagAccessMask(EnMemoryBank.EPC, 16, 112, Utility.asByteArray(mask_epc));
                //マスクあり読み取り20220209
                mDotrUtil.inventoryTag(false, EnMaskFlag.SelectMask, 0);
            }
            else if(flg==2){//保管
                if(store_flg==1){//物品読み取り
                    //送信電力、読み取りサイクルの設定20220209
                    final SeekBar sb_db_goods = (SeekBar)findViewById(R.id.db_goods);
                    final SeekBar sb_ms_goods = (SeekBar)findViewById(R.id.ms_goods);
                    mDotrUtil.setRadioPower(30-(int)sb_db_goods.getProgress());
                    mDotrUtil.setTxCycle((int)sb_ms_goods.getProgress()+40);
                }
                else if(store_flg==2){//アドレスRFIDタグ読み取り
                    //送信電力、読み取りサイクルの設定20220209
                    final SeekBar sb_db_add = (SeekBar)findViewById(R.id.db_add);
                    final SeekBar sb_ms_add = (SeekBar)findViewById(R.id.ms_add);
                    mDotrUtil.setRadioPower(30-(int)sb_db_add.getProgress());
                    mDotrUtil.setTxCycle((int)sb_ms_add.getProgress()+40);
                }

                //マスクなし読み取り20220209
                mDotrUtil.inventoryTag(false, EnMaskFlag.None, 0);
            }
            else if(flg==3){//検索
                // 送信電力、読み取りサイクルの設定20220209
                final SeekBar sb_db_add = (SeekBar)findViewById(R.id.db_add);
                final SeekBar sb_ms_add = (SeekBar)findViewById(R.id.ms_add);

                mDotrUtil.setTxCycle((int)sb_ms_add.getProgress()+40);
                mDotrUtil.setTxCycle(45);
                if(search_flg==1){
                    mDotrUtil.setRadioPower(30-(int)sb_db_add.getProgress());
                }
                else if(search_flg==2){
                    //mDotrUtil.setRadioPower(30-(int)sb_db_add.getProgress());
                    mDotrUtil.setRadioPower(15);
                }

                //マスク設定20220920
                TextView search_epc = (TextView)findViewById(R.id.txt_search_epc);
                String mask_epc = (String)search_epc.getText();
                mDotrUtil.setTagAccessMask(EnMemoryBank.EPC, 16, 112, Utility.asByteArray(mask_epc));
                Log.d("マスク確認", mask_epc);
                //マスクなし読み取り20220209
                mDotrUtil.inventoryTag(false, EnMaskFlag.SelectMask, 0);
            }
            else if(flg==4){//物品登録
                if(goods_flg==1){
                    //送信電力、読み取りサイクルの設定20220209
                    final SeekBar sb_db_goods = (SeekBar)findViewById(R.id.db_goods);
                    final SeekBar sb_ms_goods = (SeekBar)findViewById(R.id.ms_goods);
                    mDotrUtil.setRadioPower((int)(30-sb_db_goods.getProgress()));
                    mDotrUtil.setTxCycle((int)sb_ms_goods.getProgress()+40);
                    //マスクなし読み取り20220209
                    mDotrUtil.inventoryTag(false, EnMaskFlag.None, 0);
                }
                else if(goods_flg==2){
                    //送信電力、読み取りサイクルの設定20220209
                    final SeekBar sb_db_goods = (SeekBar)findViewById(R.id.db_goods);
                    final SeekBar sb_ms_goods = (SeekBar)findViewById(R.id.ms_goods);
                    mDotrUtil.setRadioPower((int)(30-sb_db_goods.getProgress()));
                    mDotrUtil.setTxCycle((int)sb_ms_goods.getProgress()+40);

                    //マスクあり書き込み20220620
                    TextView write_mask_epc_ = (TextView) findViewById(R.id.txt_write_target);
                    write_mask_epc = write_mask_epc_.getText().toString();
                    mDotrUtil.setTagAccessMask(EnMemoryBank.EPC, 16, 112, Utility.asByteArray(write_mask_epc));

                    //TagAccessParameterクラスの設定20220623
                    TagAccessParameter param = new TagAccessParameter();
                    param.setMemoryBank(EnMemoryBank.EPC);
                    param.setWordOffset(2);
                    param.setWordCount(6);

                    TextView write_epc = (TextView) findViewById(R.id.txt_write_epc);
                    txt_write = write_epc.getText().toString();
                    //mDotrUtil.clearAccessEPCList();

                    Log.d("書き込み", String.valueOf(txt_write.length()) + "/" + write_mask_epc + "/" + txt_write);
                    mDotrUtil.writeTag(param, txt_write, true, EnMaskFlag.SelectMask, 0);

                    //1000[msec]放置（メソッド同士の競合を避けるため）
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //マスクなし読み取り20220209
                    mDotrUtil.inventoryTag(false, EnMaskFlag.None, 0);
                }
            }
            else{
                //マスクなし読み取り20220209
                mDotrUtil.inventoryTag(false, EnMaskFlag.None, 0);
            }


        } else {//トリガー離す
            //トリガを離したら読取り処理をストップ
            mDotrUtil.stop();

            //フラグごとの動作20220209
            if(flg==1){//cal
                if(cal_flg==1){//a読取
                    for (Object item_n_ : addlist_m)
                    {
                        ArrayList item_n = (ArrayList)item_n_;

                        Log.d("Data", (String)item_n.get(0));
                        cal_rssi1=(double)item_n.get(1);
                        break;
                    }
                    //RSSI表示の色の変更20220209
                    TextView tv_a = (TextView) findViewById(R.id.a_rssi_cal);
                    tv_a.setBackgroundColor(Color.argb(0, 0, 0, 0));
                    TextView tv_b = (TextView) findViewById(R.id.b_rssi_cal);
                    tv_b.setBackgroundColor(Color.argb(40, 255, 255, 0));

                    tv_a.setText(String.valueOf(cal_rssi1));

                    cal_flg=2;

                }
                else if(cal_flg==2){//b読取
                    for (Object item_n_ : addlist_m)
                    {
                        ArrayList item_n = (ArrayList)item_n_;

                        Log.d("Data", (String)item_n.get(0));
                        cal_rssi2=(double)item_n.get(1);
                        break;

                    }
                    //RSSI表示の色の変更20220209
                    TextView tv_a = (TextView) findViewById(R.id.a_rssi_cal);
                    tv_a.setBackgroundColor(Color.argb(40, 255, 255, 0));
                    TextView tv_b = (TextView) findViewById(R.id.b_rssi_cal);
                    tv_b.setBackgroundColor(Color.argb(0, 0, 0, 0));

                    TextView tv_a_dis = (TextView) findViewById(R.id.a_dis_cal);
                    TextView tv_b_dis = (TextView) findViewById(R.id.b_dis_cal);
                    cal_dis1 = Double.parseDouble(tv_a_dis.getText().toString());
                    cal_dis2 = Double.parseDouble(tv_b_dis.getText().toString());

                    sub_const= (cal_rssi1-cal_rssi2)/(Math.log10(cal_dis2)-Math.log10(cal_dis1));


                    TextView tv_n = (TextView) findViewById(R.id.n_atn_cal);

                    tv_b.setText(String.valueOf(cal_rssi2));
                    tv_n.setText(String.valueOf(sub_const));


                    cal_flg=1;
                }
            }
            else if(flg==2){//保管
                if(store_flg==1){//物品読み取り
                    String epc_store = epcoc;

                    //物品情報と読み取ったEPCの一致を確認し、表示20220209
                    File file = new File("/data/data/" + this.getPackageName() + "/files/epc_goods.csv");
                    FileReader buff = null;
                    try {
                        buff = new FileReader(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    BufferedReader fr = new BufferedReader(buff);

                    String line_ = null;
                    try {
                        line_ = fr.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    while (line_ != null)
                    {
                        String[] store = line_.split(",");
                        Log.d("EPC", store[1]);

                        if(epc_store.equals(store[1])){
                            store_goods_epc = epc_store;
                            String sakuban = "";
                            String tyuban = "";
                            boolean allo_start =false;//文字列が開始していればtrue
                            boolean char_allo =false;//trueの時は10進数字以外を入力
                            boolean judge_sakutyu = false;//trueの時は注番入力
                            char c;
                            char c_bef = 0;

                            //エンコード
                            for(int i=0;i<epc_store.length();i++){
                                c=epc_store.charAt(i);
                                if(allo_start){
                                    if(judge_sakutyu){//注番
                                        if(char_allo){
                                            tyuban+=epcDecode(c_bef, c);
                                            char_allo=false;
                                        }
                                        else{
                                            if(Character.isDigit(c)){
                                                tyuban+=c;
                                            }
                                            else{
                                                c_bef=c;
                                                char_allo=true;
                                            }

                                        }
                                    }
                                    else{//作番
                                        if(char_allo){
                                            if(epcDecode(c_bef, c).equals("/")){
                                                judge_sakutyu=true;
                                            }
                                            else{
                                                sakuban+=epcDecode(c_bef, c);
                                            }
                                            char_allo = false;
                                        }
                                        else{
                                            if(Character.isDigit(c)){
                                                sakuban+=c;
                                            }
                                            else{
                                                c_bef=c;
                                                char_allo=true;
                                            }

                                        }

                                    }

                                }
                                else{
                                    if(c=='f'){
                                        allo_start = true;
                                        Log.d("デコード開始", "abc");
                                    }
                                }
                            }



                            TextView st_sakuban = (TextView) findViewById(R.id.txt_sakuban_store);
                            TextView st_tyuban = (TextView) findViewById(R.id.txt_tyuban_store);
                            st_sakuban.setText(sakuban);
                            st_tyuban.setText(tyuban);
                            epc_inv = String.valueOf(store[0]);
                            Log.d("store_name", String.valueOf(store[0]));

                            break;
                        }
                        try {
                            line_ = fr.readLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    try {
                        fr.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }



                    store_flg=2;
                    TextView textView = (TextView) findViewById(R.id.txt_inv);
                    textView.setText("");

                    //色変20220502
                    TextView st_sakuban = (TextView) findViewById(R.id.txt_sakuban_store);
                    TextView st_tyuban = (TextView) findViewById(R.id.txt_tyuban_store);
                    st_sakuban.setBackgroundColor(Color.argb(0, 255, 255, 0));
                    st_tyuban.setBackgroundColor(Color.argb(0, 255, 255, 0));

                    TextView stplace = (TextView) findViewById(R.id.txt_inv);
                    stplace.setBackgroundColor(Color.argb(40, 255, 255, 0));

                    //描画リセット20220502
                    //マップ設定20220929
                    Canvas canvas;
                    canvas = new Canvas(invMap);
                    canvas.drawColor(0, PorterDuff.Mode.CLEAR);

                    ImageView inv_sector = (ImageView) this.findViewById(R.id.img_inv);
                    // ImageViewに作成したBitmapをセット
                    invMap = setMap(inv_map_flg);
                    inv_sector.setImageBitmap(invMap);

                    inv_sector.setImageBitmap(invMap);


                }
                else if(store_flg==2){//アドレスRFIDタグ読み取り

                    //アドレスRFIDタグごとの最大値取得、および、距離の計算20220126
                    //最大値取得メソッド　20210726
                    int mst_inc = 0;
                    int total_count = addlist_m.size();

                    String epcbef = "";
                    String epcpre = "";
                    double rssi = 0;

                    //多点CAL用パラメータ20221024
                    EditText multi_a_origin = (EditText) findViewById(R.id.multi_a);
                    EditText multi_b_origin = (EditText) findViewById(R.id.multi_b);
                    float curve_a = Float.valueOf(multi_a_origin.getText().toString());
                    float curve_b = Float.valueOf(multi_b_origin.getText().toString());

                    //距離推定時のパラメーター
                    int cnt = 0;
                    double dis = 0;//水平距離
                    double dis_dg = 0;//斜め距離

                    double base_dg = 0;//基準斜め距離



                    double cal_RSSI = -52;//基準RSSI値(外に出す可能性あり)


                    for (Object item_n_ : addlist_m)
                    {
                        ArrayList item_n = (ArrayList)item_n_;
                        ArrayList mst = new ArrayList();
                        epcpre = (String)item_n.get(0);

                        Log.d("Data", (String)item_n.get(0));

                        if (mst_inc == 0)//addlist_mでEPC番号ごとの最初のデータの時//111
                        {
                            rssi = (Double)item_n.get(1);
                            epcbef = (String)item_n.get(0);
                            cnt++;
                            mst_inc++;

                            //CAL時のRSSIを格納20220209
                            if(cal_flg==1){
                                cal_rssi1=rssi;
                            }
                            else if(cal_flg==2){
                                cal_rssi2=rssi;
                            }
                        }
                        else if ((mst_inc == total_count - 1) || (!epcpre.equals(epcbef) && mst_inc != 0))//222
                        {
                            mst.add((String)epcbef);//EPC
                            mst.add((Double)rssi);//RSSI
                            mst.add((int)cnt);//読み取り回数
                            base_dg = Math.sqrt(Math.pow(cal_dis1, 2) + Math.pow(add_height - read_height, 2));//斜め基準距離

                            if(ranging_method_flg==0){//2点CAL
                                dis_dg = base_dg * Math.pow(10, ((cal_rssi1 - rssi) / sub_const));//アドレスRFIDタグからの斜め距離, sub_constは減衰定数N
                            }
                            else if(ranging_method_flg==1){//多点CAL
                                dis_dg =  Math.exp(-((rssi+curve_a)/curve_b));
                            }

                            dis = Math.sqrt((Math.pow(dis_dg, 2)) - (Math.pow(add_height - read_height, 2)));//アドレスRFIDタグからの水平距離

                            //アドレスRFIDタグの高さは全て等しいとみなすため、if文を省略
                            Log.d("RSSI" , "DIS =" + dis_dg);

                            mst.add((Double)dis);//距離
                            mst.add((Double)Math.pow(10, (Double)mst.get(1) / 10));//真値

                            int index_m = 0;


                            for (Object item_ : addlist)
                            {
                                ArrayList item = (ArrayList) item_;
                                if ((Double)mst.get(1) > (Double)item.get(1))
                                {
                                    break;
                                }
                                else
                                { index_m++; }
                            }

                            addlist.add(index_m, mst);
                            cnt=0;


                            if(!epcpre.equals(epcbef) && mst_inc != 0)//333
                            {
                                rssi = (Double)item_n.get(1);
                                epcbef = (String)item_n.get(0);
                                cnt++;
                                mst_inc++;
                            }
                        }
                        else if (epcpre.equals(epcbef) &&  mst_inc != 0)//444
                        {
                            cnt++;
                            epcbef = (String)item_n.get(0);
                            mst_inc++;

                        }

                    }

                    //読み取ったアドレスRFIDタグの最大値を表示
                    StringBuilder dis_txt = new StringBuilder();
                    String str = "";

                    StringBuilder dis_log = new StringBuilder();




                    //画像描画20220216
                    ImageView inv_sector = (ImageView) this.findViewById(R.id.img_inv);
                    // ImageViewに作成したBitmapをセット
                    inv_sector.setImageBitmap(invMap);
                    Canvas canvas;
                    canvas = new Canvas(invMap);



                    //描画用パラメータ20220418
                    String[] add_epc = {"", "", ""};
                    Double[] add_dis = new Double[3];
                    Double[] add_rssi = new Double[3];
                    Double[] add_rssi_tr = new Double[3];

                    Paint paint = new Paint();

                    //表示用テキスト設定20220418
                    int i=0;
                    for(Object item_ : addlist)//item_: [EPC, RSSI, 読み取り回数, 距離 RSSI真値]
                    {
                        ArrayList item = (ArrayList) item_;
                        StringBuilder data = new StringBuilder(
                                "EPC：" + String.valueOf(item.get(0)) +
                                        ", RSSI=" + String.valueOf(item.get(1)) +
                                        ", " + String.valueOf(item.get(2)) +
                                        "回, " + String.valueOf(item.get(3)) + "[m]\n");
                        dis_txt.append(data);

                        StringBuilder write_ = new StringBuilder(
                                          String.valueOf(item.get(0)) +
                                        "," + String.valueOf(item.get(1)) +
                                        "," + String.valueOf(item.get(2)) +
                                        "," + String.valueOf(item.get(3)) + "#");

                        dis_log.append(write_);



                        paint.setStrokeWidth(20);
                        if(i<=2){
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setAntiAlias(true);
                            add_epc[i] = String.valueOf(item.get(0));
                            add_rssi[i] = Double.parseDouble(String.valueOf(item.get(1)));
                            add_dis[i] = Double.parseDouble(String.valueOf(item.get(3)));
                            add_rssi_tr[i] = Double.parseDouble(String.valueOf(item.get(4)));


                        }
                        i++;

                    }


                    //読み取ったアドレスRFIDタグの座標（dot）と方向（y軸→x軸回転した角度[degree]）
                    int add_1st_x = origin_x;
                    int add_1st_y = origin_y;
                    int add_1st_d = 0;
                    int add_2nd_x = origin_x;
                    int add_2nd_y = origin_y;
                    int add_2nd_d = 0;
                    int add_3rd_x = origin_x;
                    int add_3rd_y = origin_y;
                    int add_3rd_d = 0;

                    //重み平均を用いる際に使用する(セクターの弧の中心の)座標（m）
                    //est_***: [x座標, y座標]
                    List<Double> est_1st_m = new ArrayList<Double>();
                    List<Double> est_2nd_m = new ArrayList<Double>();
                    List<Double> est_3rd_m = new ArrayList<Double>();

                    //(dot)
                    List<Integer> est_1st = new ArrayList<Integer>();
                    List<Integer> est_2nd = new ArrayList<Integer>();
                    List<Integer> est_3rd = new ArrayList<Integer>();

                    double tr_sq_sum = 1;//真値平方根重みの分母の初期値

                    //範囲円の中心の座標（m）
                    double det_x_m = 0;
                    double det_y_m = 0;
                    //範囲円の中心の座標（dot）
                    int det_x = origin_x;
                    int det_y = origin_y;
                    //範囲円の半径(dot)
                    int range_x = (int)(range_x_m * dotm_x);
                    int range_y = (int)(range_y_m * dotm_y);






                    //アドレスRFIDタグセクター描画
                    if(surface_flg==0){
                        //3面　20221027
                        //3rd
                        if(!add_epc[2].equals("")){
                            paint.setColor(Color.argb(255, 150, 150, 255));//青
                            paint.setStyle(Paint.Style.STROKE);
                            Inv_Sector_3(add_epc[2], add_dis[2], canvas, paint, add_3rd_d, add_3rd_x, add_3rd_y, est_3rd_m);
                        }

                        //2nd
                        if(!add_epc[1].equals("")){
                            paint.setColor(Color.argb(255, 150, 255, 150));//緑
                            paint.setStyle(Paint.Style.STROKE);
                            Inv_Sector_3(add_epc[1], add_dis[1], canvas, paint, add_2nd_d, add_2nd_x, add_2nd_y, est_2nd_m);
                        }

                        //1st
                        if(!add_epc[0].equals("")){
                            paint.setColor(Color.argb(255, 255, 150, 150));//赤
                            paint.setStyle(Paint.Style.STROKE);
                            Inv_Sector_3(add_epc[0], add_dis[0], canvas, paint, add_1st_d, add_1st_x, add_1st_y, est_1st_m);

                        }
                    }
                    else if(surface_flg==1){
                        //4面　20221027
                        if(!add_epc[2].equals("")){
                            paint.setColor(Color.argb(255, 150, 150, 255));//青
                            paint.setStyle(Paint.Style.STROKE);
                            Inv_Sector_4(add_epc[2], add_dis[2], canvas, paint, add_3rd_d, add_3rd_x, add_3rd_y, est_3rd_m);
                        }

                        //2nd
                        if(!add_epc[1].equals("")){
                            paint.setColor(Color.argb(255, 150, 255, 150));//緑
                            paint.setStyle(Paint.Style.STROKE);
                            Inv_Sector_4(add_epc[1], add_dis[1], canvas, paint, add_2nd_d, add_2nd_x, add_2nd_y, est_2nd_m);
                        }

                        //1st
                        if(!add_epc[0].equals("")){
                            paint.setColor(Color.argb(255, 255, 150, 150));//赤
                            paint.setStyle(Paint.Style.STROKE);
                            Inv_Sector_4(add_epc[0], add_dis[0], canvas, paint, add_1st_d, add_1st_x, add_1st_y, est_1st_m);

                        }
                    }







                    //真値平方根重みによる範囲円描画20220509
                    paint.setColor(Color.argb(128, 255, 255, 0));//黄
                    paint.setStyle(Paint.Style.FILL);
                    if(!add_epc[2].equals("")){
                        //アドレスRFIDタグが3つ以上読み取れた場合
                        double tr_sq_1st = Math.sqrt(add_rssi_tr[0]);
                        double tr_sq_2nd = Math.sqrt(add_rssi_tr[1]);
                        double tr_sq_3rd = Math.sqrt(add_rssi_tr[2]);
                        tr_sq_sum = tr_sq_1st + tr_sq_2nd + tr_sq_3rd;

                        det_x_m = ((est_1st_m.get(0) * tr_sq_1st) + (est_2nd_m.get(0) * tr_sq_2nd) + (est_3rd_m.get(0) * tr_sq_3rd))/tr_sq_sum;
                        det_y_m = ((est_1st_m.get(1) * tr_sq_1st) + (est_2nd_m.get(1) * tr_sq_2nd) + (est_3rd_m.get(1) * tr_sq_3rd))/tr_sq_sum;
                        det_x = origin_x + (int)(det_x_m*dotm_x);
                        det_y = origin_y - (int)(det_y_m*dotm_y);


                        canvas.drawArc(det_x-range_x, det_y-range_y, det_x+range_x, det_y+range_y, 0, 360, false, paint);




                    }
                    else if(add_epc[2].equals("") && !add_epc[1].equals("")){
                        //アドレスRFIDタグが２つ読み取れた場合
                        double tr_sq_1st = Math.sqrt(add_rssi_tr[0]);
                        double tr_sq_2nd = Math.sqrt(add_rssi_tr[1]);
                        tr_sq_sum = tr_sq_1st + tr_sq_2nd;

                        det_x_m = ((est_1st_m.get(0) * tr_sq_1st) + (est_2nd_m.get(0) * tr_sq_2nd))/tr_sq_sum;
                        det_y_m = ((est_1st_m.get(1) * tr_sq_1st) + (est_2nd_m.get(1) * tr_sq_2nd))/tr_sq_sum;
                        det_x = origin_x + (int)(det_x_m*dotm_x);
                        det_y = origin_y - (int)(det_y_m*dotm_y);

                        canvas.drawArc(det_x-range_x, det_y-range_y, det_x+range_x, det_y+range_y, 0, 360, false, paint);

                    }
                    else if(add_epc[1].equals("") && !add_epc[0].equals("")){
                        //アドレスRFIDタグが1つ読み取れた場合
                        det_x = origin_x + (int)(est_1st_m.get(0)*dotm_x);
                        det_y = origin_y - (int)(est_1st_m.get(1)*dotm_y);
                        canvas.drawArc(det_x-range_x, det_y-range_y, det_x+range_x, det_y+range_y, 0, 360, false, paint);
                    }
                    Log.d("推定座標", "(" + det_x_m + ", " + det_y_m + ")" + "(" + det_x + ", " + det_y + ")");


                    //20220927 保管位置の座標をgoods_epc.csvに記録
                    //epc_goodsの内容を一度全て変数に置き、変更箇所のみ変更を加えた後再度書き込み
                    StringBuilder all_insert = new StringBuilder();

                    File file = new File("/data/data/" + this.getPackageName() + "/files/epc_goods.csv");
                    FileReader buff = null;
                    try {
                        buff = new FileReader(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    BufferedReader fr = new BufferedReader(buff);

                    String line_insert = null;
                    try {
                        line_insert = fr.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    while(line_insert!=null){

                        String[] store_insert = line_insert.split(",");
                        Timestamp stored_time = new Timestamp(System.currentTimeMillis());
                        if(store_insert[1].equals(store_goods_epc)){
                            Log.d("変更対象", store_insert[0]);
                            all_insert.append(store_insert[0] + "," + //作番/注番
                                    store_insert[1] + "," +//EPC
                                    stored_time + "," +//タイムスタンプ
                                    String.valueOf(det_x) + "," +//x座標
                                    String.valueOf(det_y) + "," +//y座標
                                    String.valueOf(inv_map_flg) + "\n");//マップ番号
                        }
                        else{
                            Log.d("それ以外", store_insert[0]);
                            all_insert.append(store_insert[0] + "," + //作番/注番
                                    store_insert[1] + "," +//EPC
                                    store_insert[2] + "," +//タイムスタンプ
                                    store_insert[3] + "," +//x座標
                                    store_insert[4] + "," +//y座標
                                    store_insert[5] + "," + "\n");//マップ番号
                        }


                        try {
                            line_insert = fr.readLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d("上書き内容", store_goods_epc.concat(", aaa"));

                    try {
                        File store_file = new File("/data/data/" + this.getPackageName() + "/files/epc_goods.csv");
                        FileWriter goods_write = new FileWriter(store_file,false);
                        goods_write.write(all_insert.toString());//変数に入れたすべての物品データを再度epc_goods.csvに書き込み
                        //epc_goodsのフォーマット：(作番/注番,epc,タイムスタンプ,x座標,y座標,マップ番号)
                        goods_write.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //変更箇所


                    store_goods_epc = "";



                    str = dis_txt.toString();
                    TextView textView = (TextView) findViewById(R.id.txt_inv);
                    textView.setText(str);

                    //色変20220502
                    TextView st_sakuban = (TextView) findViewById(R.id.txt_sakuban_store);
                    TextView st_tyuban = (TextView) findViewById(R.id.txt_tyuban_store);
                    st_sakuban.setBackgroundColor(Color.argb(40, 255, 255, 0));
                    st_tyuban.setBackgroundColor(Color.argb(40, 255, 255, 0));

                    TextView stplace = (TextView) findViewById(R.id.txt_inv);
                    stplace.setBackgroundColor(Color.argb(0, 255, 255, 0));

                    //画像描画20220216
                    inv_sector.setImageBitmap(invMap);
                    canvas = new Canvas(invMap);



                    store_flg=1;
                }


            }
            else if(flg==3){//検索
                if(search_flg==1){
                    search_flg=2;
                }
                else if(search_flg==2){
                    search_flg=1;
                }


            }
            else if(flg==4){//物品登録
                if(goods_flg==1){
                    TextView target = (TextView) findViewById(R.id.txt_write_target);
                    TextView sakuban = (TextView) findViewById(R.id.txt_sakuban_regi);
                    TextView tyuban = (TextView) findViewById(R.id.txt_tyuban_regi);
                    TextView write = (TextView) findViewById(R.id.txt_write_epc);
                    TextView written = (TextView) findViewById(R.id.txt_written_goods);
                    TextView order = (TextView) findViewById(R.id.txt_goods_regi_order);

                    //マスクかけ用EPCの表示
                    target.setText(epcoc);
                    //作番注番入力欄の背景色変更
                    target.setBackgroundColor(Color.argb(0, 255, 255, 0));
                    sakuban.setBackgroundColor(Color.argb(40, 255, 255, 0));
                    tyuban.setBackgroundColor(Color.argb(40, 255, 255, 0));
                    write.setBackgroundColor(Color.argb(0, 255, 255, 0));
                    written.setBackgroundColor(Color.argb(0, 255, 255, 0));
                    //指示文の表示2
                    order.setText("作番と注番を入力し\n「エンコード/書き込み準備」を押してください");

                }
                else if(goods_flg==2){
                    TextView target = (TextView) findViewById(R.id.txt_write_target);
                    TextView sakuban = (TextView) findViewById(R.id.txt_sakuban_regi);
                    TextView tyuban = (TextView) findViewById(R.id.txt_tyuban_regi);
                    TextView write = (TextView) findViewById(R.id.txt_write_epc);
                    TextView written = (TextView) findViewById(R.id.txt_written_goods);
                    TextView order = (TextView) findViewById(R.id.txt_goods_regi_order);

                    //テキストの背景色変更
                    target.setBackgroundColor(Color.argb(0, 255, 255, 0));
                    sakuban.setBackgroundColor(Color.argb(0, 255, 255, 0));
                    tyuban.setBackgroundColor(Color.argb(0, 255, 255, 0));
                    write.setBackgroundColor(Color.argb(0, 255, 255, 0));
                    written.setBackgroundColor(Color.argb(40, 255, 255, 0));
                    //指示文の表示4
                    written.setText(epcoc);

                    EditText et_sakuban = (EditText) findViewById(R.id.txt_sakuban_regi);
                    final String txt_sakuban = et_sakuban.getText().toString();
                    EditText et_tyuban = (EditText) findViewById(R.id.txt_tyuban_regi);
                    final String txt_tyuban = et_tyuban.getText().toString();
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    Log.d("物品登録", txt_sakuban + "/" + txt_tyuban + "," + epcoc + "," + timestamp +  ",,,");

                    try {
                        File file = new File("/data/data/" + this.getPackageName() + "/files/epc_goods.csv");
                        FileWriter goods_write = new FileWriter(file,true);
                        goods_write.append(txt_sakuban + "/" + txt_tyuban + "," +
                                           epcoc + "," +
                                           timestamp + "," +
                                           "0,0,0\n");
                        //epc_goodsのフォーマット：(作番/注番,epc,タイムスタンプ,x座標,y座標,マップ番号)
                        goods_write.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    order.setText("登録が完了しました\n引き続き登録を行う場合は\n「読取物品の登録」を押してください");
                    goods_flg=1;

                }
            }

            addlist_m.clear();
            addlist.clear();

        }
    }


    /*
     * EPC一括読取りイベント
     * (非 Javadoc)
     * @see jp.co.tss21.uhfrfid.dotr_android.OnDotrEventListener#onInventoryEPC(java.lang.String)
     */
    @Override
    public void onInventoryEPC(String epc) {
        Log.d(TAG, "onInventoryEPC(" + epc + ")");

        String[] list = epc.split(",");
        if (list.length >= 2) {
            for (int i = 0; i < list.length; i++) {
                if (list[i].startsWith("TIME=")) {
                    Date d = new Date(Long.parseLong(list[1].replace("TIME=", "")));
                    epc = epc.replace(list[1], String.format("%tY年%tm月%td日 %tH時%tM分%tS秒(%tL)", d, d, d, d, d, d, d));
                    break;
                }
            }
        }

        mHandler.sendMessage(mHandler.obtainMessage(MSG_FIRM, 0, 0, epc));

        //addlistにepcを追記20220121
        //addlist.add(epc);

        //以下、Win版から引用１　20211129

        //RSSI取得
        int timeStart_2 = epc.indexOf(",RSSI=") + ",RSSI=".length();
        RSSI = epc.substring(timeStart_2, timeStart_2+5);
        setRSSI = Double.parseDouble(RSSI);
        //Phase取得
        /*
        int timeStart_3 = epc.indexOf(",PH=") + ",PH=".length();
        Phase = epc.substring(timeStart_3);
        setPhase = Integer.parseInt(Phase);

         */

        if (timeStart_2 == 28 + ",RSSI=".length())
        {
            epcoc = epc.substring(0, 28); ;//20180711水戸OC用アプリ追記
        }
        Log.d("EPC", epcoc);







        //最大値取得
        if (isadd(epcoc))
        {
            int index0 = 0;
            String epc0 = "";
            int i = 0;

            //EPC一致するまでインデックス番号を増やす20220126()
            for(Object item : addlist_m)
            {
                ArrayList item_ = (ArrayList)item ;//ObjectからArrayListへのキャスト（重くなるかも）

                if (epcoc.equals((String)item_.get(0)))
                {
                    i = 1;
                    if (setRSSI > (Double)item_.get(1))
                    {
                        break;
                    }
                    else
                    { index0++; }
                }
                else
                {
                    if (i == 1)
                    {
                        break;
                    }
                    else
                    { index0++; }
                }
            }
            //同一EPC番号の中で大きさ順にソートする。

            ArrayList epcl = new ArrayList();
            epcl.add(epcoc);
            epcl.add(setRSSI);
            //epcl.add(setPhase);
            addlist_m.add(index0, epcl);



        }

        //グラフ描画
        if(flg==3){
            setupPieChart();
        }





        mHandler.sendMessage(mHandler.obtainMessage(MSG_INVENTORY_TAG, 0, 0, epc));

    }


    @Override
    public void onReadTagData(String data, String epc) {
        Log.d(TAG, "onReadTagData(" + data + ")(" + epc + ")");
    }


    @Override
    public void onWriteTagData(String epc) {
        Log.d(TAG, "onWriteTagData(" + epc + ")");
    }


    @Override
    public void onUploadTagData(String data) {
        Log.d(TAG, "onUploadTagData(" + data + ")");
    }


    @Override
    public void onTagMemoryLocked(String arg0) {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void onScanCode(String code){
        Log.d(TAG, "onScanCode(" + code + ")");
    }

    @Override
    public void onScanTriggerChanged(boolean trigger){
        Log.d(TAG, "onScanTriggerChanged(" + trigger + ")");
    }


    //登録情報の判断部分20211129
    private Boolean isadd(String epcoc_)
    {

        for (Object item_ :epcadd)
        {
            String item = (String)item_;
            if (epcoc.equals(item))
            {
                return true;
            }
        }
        return false;

    }

    //BitMapの生成20220217
    private Bitmap mapData(int id){
        BitmapFactory.Options options = new  BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id, options);
        return bitmap;
    }

    //3面セクター描画
    private void Inv_Sector_3(String add_epc_, double add_dis_, Canvas canvas_, Paint paint_, int add_d, int add_x, int add_y, List<Double> est_){
        //Inv_Sector_3(アドレスタグのEPC, 距離, Canvas, Paint, y軸→x軸回転の角度, アドレスタグのx座標（dot）, アドレスタグのy座標（dot）)
        //drawArc(左上x, 左上y, 右下x, 右下y, 開始角度, 移動角度, Paintクラスのインスタンス)
        double est_x = 0;
        double est_y = 0;

        if(add_epc_.equals("3000000000000000000000000031")){
            add_d = 225;//y軸→x軸回転の角度
            add_x += (int)(add_1_x*dotm_x);
            add_y -= (int)(add_1_y*dotm_y);
            est_x = add_1_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
            est_y = add_1_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
        }
        else if(add_epc_.equals("3000000000000000000000000032")){
            add_d = 270;//y軸→x軸回転の角度
            add_x += (int)(add_1_x*dotm_x);
            add_y -= (int)(add_1_y*dotm_y);
            est_x = add_1_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
            est_y = add_1_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
        }
        else if(add_epc_.equals("3000000000000000000000000033")){
            add_d = 315;//y軸→x軸回転の角度
            add_x += (int)(add_1_x*dotm_x);
            add_y -= (int)(add_1_y*dotm_y);
            est_x = add_1_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
            est_y = add_1_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
        }
        else if(add_epc_.equals("3000000000000000000000000034")){
            add_d = 45;//y軸→x軸回転の角度
            add_x += (int)(add_2_x*dotm_x);
            add_y -= (int)(add_2_y*dotm_y);
            est_x = add_2_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
            est_y = add_2_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
        }
        else if(add_epc_.equals("3000000000000000000000000035")){
            add_d = 90;//y軸→x軸回転の角度
            add_x += (int)(add_2_x*dotm_x);
            add_y -= (int)(add_2_y*dotm_y);
            est_x = add_2_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
            est_y = add_2_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
        }
        else if(add_epc_.equals("3000000000000000000000000036")){
            add_d = 135;//y軸→x軸回転の角度
            add_x += (int)(add_2_x*dotm_x);
            add_y -= (int)(add_2_y*dotm_y);
            est_x = add_2_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
            est_y = add_2_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
        }
        else if(add_epc_.equals("3000000000000000000000000037")){
            add_d = 45;//y軸→x軸回転の角度
            add_x += (int)(add_3_x*dotm_x);
            add_y -= (int)(add_3_y*dotm_y);
            est_x = add_3_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
            est_y = add_3_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
        }
        else if(add_epc_.equals("3000000000000000000000000038")){
            add_d = 90;//y軸→x軸回転の角度
            add_x += (int)(add_3_x*dotm_x);
            add_y -= (int)(add_3_y*dotm_y);
            est_x = add_3_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
            est_y = add_3_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
        }
        else if(add_epc_.equals("3000000000000000000000000039")){
            add_d = 135;//y軸→x軸回転の角度
            add_x += (int)(add_3_x*dotm_x);
            add_y -= (int)(add_3_y*dotm_y);
            est_x = add_3_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
            est_y = add_3_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
        }

        int add_r_x_ = (int)add_dis_*dotm_x;
        int add_r_y_ = (int)add_dis_*dotm_y;
        canvas_.drawArc(add_x - add_r_x_, add_y - add_r_y_, add_x + add_r_x_, add_y + add_r_y_,add_d - 45,90,true, paint_);

        //重みづけ用の座標設定（m）
        est_.add(est_x);
        est_.add(est_y);
        Log.d("est座標", add_epc_ + ": " + "(" + (add_x-origin_x)/dotm_x + ", " + (origin_y-add_y)/dotm_y + ")" + add_r_x_ +"(" + est_x + ", " + est_y + ")");

    }

    //4面セクター描画
    private void Inv_Sector_4(String add_epc_, double add_dis_, Canvas canvas_, Paint paint_, int add_d, int add_x, int add_y, List<Double> est_){
        //Inv_Sector_4(アドレスタグのEPC, 距離, Canvas, Paint, y軸→x軸回転の角度, アドレスタグのx座標（dot）, アドレスタグのy座標（dot）)
        //drawArc(左上x, 左上y, 右下x, 右下y, 開始角度, 移動角度, Paintクラスのインスタンス)
        double est_x = 0;
        double est_y = 0;

        if(add_epc_.equals("3000000000000000000000000031")){
            add_d = 360;//y軸→x軸回転の角度
            add_x += (int)(add_1_x*dotm_x);
            add_y -= (int)(add_1_y*dotm_y);
            est_x = add_1_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
            est_y = add_1_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
        }
        else if(add_epc_.equals("3000000000000000000000000032")){
            add_d = 90;//y軸→x軸回転の角度
            add_x += (int)(add_1_x*dotm_x);
            add_y -= (int)(add_1_y*dotm_y);
            est_x = add_1_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
            est_y = add_1_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
        }
        else if(add_epc_.equals("3000000000000000000000000033")){
            add_d = 180;//y軸→x軸回転の角度
            add_x += (int)(add_1_x*dotm_x);
            add_y -= (int)(add_1_y*dotm_y);
            est_x = add_1_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
            est_y = add_1_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
        }
        else if(add_epc_.equals("3000000000000000000000000096")){
            add_d = 270;//y軸→x軸回転の角度
            add_x += (int)(add_1_x*dotm_x);
            add_y -= (int)(add_1_y*dotm_y);
            est_x = add_1_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
            est_y = add_1_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
        }
        //34,35,36,97
        else if(add_epc_.equals("3000000000000000000000000034")){
            add_d = 360;//y軸→x軸回転の角度
            add_x += (int)(add_2_x*dotm_x);
            add_y -= (int)(add_2_y*dotm_y);
            est_x = add_2_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
            est_y = add_2_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
        }
        else if(add_epc_.equals("3000000000000000000000000035")){
            add_d = 90;//y軸→x軸回転の角度
            add_x += (int)(add_2_x*dotm_x);
            add_y -= (int)(add_2_y*dotm_y);
            est_x = add_2_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
            est_y = add_2_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
        }
        else if(add_epc_.equals("3000000000000000000000000036")){
            add_d = 180;//y軸→x軸回転の角度
            add_x += (int)(add_2_x*dotm_x);
            add_y -= (int)(add_2_y*dotm_y);
            est_x = add_2_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
            est_y = add_2_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
        }
        else if(add_epc_.equals("3000000000000000000000000097")){
            add_d = 270;//y軸→x軸回転の角度
            add_x += (int)(add_2_x*dotm_x);
            add_y -= (int)(add_2_y*dotm_y);
            est_x = add_2_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
            est_y = add_2_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
        }
        //37,38,39,98
        else if(add_epc_.equals("3000000000000000000000000037")){
            add_d = 360;//y軸→x軸回転の角度
            add_x += (int)(add_3_x*dotm_x);
            add_y -= (int)(add_3_y*dotm_y);
            est_x = add_3_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
            est_y = add_3_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
        }
        else if(add_epc_.equals("3000000000000000000000000038")){
            add_d = 90;//y軸→x軸回転の角度
            add_x += (int)(add_3_x*dotm_x);
            add_y -= (int)(add_3_y*dotm_y);
            est_x = add_3_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
            est_y = add_3_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
        }
        else if(add_epc_.equals("3000000000000000000000000039")){
            add_d = 180;//y軸→x軸回転の角度
            add_x += (int)(add_3_x*dotm_x);
            add_y -= (int)(add_3_y*dotm_y);
            est_x = add_3_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
            est_y = add_3_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
        }
        else if(add_epc_.equals("3000000000000000000000000039")){
            add_d = 270;//y軸→x軸回転の角度
            add_x += (int)(add_3_x*dotm_x);
            add_y -= (int)(add_3_y*dotm_y);
            est_x = add_3_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
            est_y = add_3_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
        }

        int add_r_x_ = (int)(add_dis_*dotm_x);
        int add_r_y_ = (int)(add_dis_*dotm_y);
        canvas_.drawArc(add_x - add_r_x_, add_y - add_r_y_, add_x + add_r_x_, add_y + add_r_y_,add_d - 45,90,true, paint_);

        //重みづけ用の座標設定（m）
        est_.add(est_x);
        est_.add(est_y);
        Log.d("est座標", add_epc_ + ": " + "(" + (add_x-origin_x)/dotm_x + ", " + (origin_y-add_y)/dotm_y + ")" + add_r_x_ +"(" + est_x + ", " + est_y + ")");

    }



    //検索時グラフ作成20220530
    private void setupPieChart() {
        //PieEntriesのリストを作成する:
        List<PieEntry> pieEntries = new ArrayList<>();
        float z = (float)(setRSSI+80);
        pieEntries.add(new PieEntry(z, ""));
        pieEntries.add(new PieEntry((float)(-setRSSI), ""));


        PieDataSet dataSet = new PieDataSet(pieEntries, "Closeness");
        //20220915 RSSIの値による色の変更
        if(z < 25){
            dataSet.setColors(Color.MAGENTA, Color.argb(0, 0, 0, 0));
            Log.d("PieChart", "M: " + String.valueOf(z));
        }
        else if(z >= 25 && z < 40){
            dataSet.setColors(Color.YELLOW, Color.argb(0, 0, 0, 0));
            Log.d("PieChart", "Y: " + String.valueOf(z));
        }
        else if(z >= 40){
            dataSet.setColors(Color.CYAN, Color.argb(0, 0, 0, 0));
            Log.d("PieChart", "C: " + String.valueOf(z));
        }

        PieData data = new PieData(dataSet);

        //PieChartを取得する:
        PieChart piechart = (PieChart) findViewById(R.id.pie_chart);

        piechart.setData(data);
        piechart.invalidate();
    }

    private String epcDecode(char c_1, char c_2){
        File file = new File("/data/data/" + this.getPackageName() + "/files/char_allocation.csv");
        FileReader buff = null;
        try {
            buff = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line_ = null;
        BufferedReader fr = new BufferedReader(buff);
        try {
            line_ = fr.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (line_ != null) {
            String[] data = line_.split(",");
            if (c_1==data[0].charAt(0) && c_2==data[1].charAt(0)) {
                return data[2];
            }
            try {
                line_ = fr.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private Bitmap setMap(int map_flg){
        if(map_flg==0){
            return mapData(R.drawable.laboratory);
        }else if (map_flg==1){
            return mapData(R.drawable.e5_8f_inov);
        }
        else if (map_flg==2){
            return mapData(R.drawable.ibaden_factory);
        }
        else{
            return mapData(R.drawable.laboratory);
        }
    }

    //3面、4面の時のアドレスRFIDタグのEPCを.csvより取得 20221108
    private void setAddTag(){
        if(surface_flg==0){
            File file = new File("/data/data/" + this.getPackageName() + "/files/epc_add_3.csv");
            FileReader buff = null;
            try {
                buff = new FileReader(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            BufferedReader fr = new BufferedReader(buff);

            String line_ = null;
            try {
                line_ = fr.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (line_ != null)
            {
                String[] str = line_.split(",");
                epcadd.add(str[1]);

                try {
                    line_ = fr.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(surface_flg==1){
            epcadd = new ArrayList();

            File file = new File("/data/data/" + this.getPackageName() + "/files/epc_add_4.csv");
            FileReader buff = null;
            try {
                buff = new FileReader(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            BufferedReader fr = new BufferedReader(buff);

            String line_ = null;
            try {
                line_ = fr.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (line_ != null)
            {
                String[] str = line_.split(",");
                epcadd.add(str[1]);

                try {
                    line_ = fr.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void SearchListClear(){
        //検索時Spinnerに入れるデータの初期化20221110
        search_goods_name = new ArrayList<>();
        search_goods_epc = new ArrayList<>();
        search_goods_lasttime = new ArrayList<>();
        search_goods_coorx = new ArrayList<>();
        search_goods_coory = new ArrayList<>();
        search_goods_map = new ArrayList<>();
    }

    private void SettingsClear(){
        //一括でテキスト背景色や画像描画の設定をリセットするメソッド2021110
        TextView st_sakuban = (TextView) findViewById(R.id.txt_sakuban_store);
        TextView st_tyuban = (TextView) findViewById(R.id.txt_tyuban_store);
        TextView target = (TextView) findViewById(R.id.txt_write_target);
        TextView sakuban = (TextView) findViewById(R.id.txt_sakuban_regi);
        TextView tyuban = (TextView) findViewById(R.id.txt_tyuban_regi);
        TextView write = (TextView) findViewById(R.id.txt_write_epc);
        TextView written = (TextView) findViewById(R.id.txt_written_goods);
        TextView order = (TextView) findViewById(R.id.txt_goods_regi_order);
        TextView txtInv = (TextView) findViewById(R.id.txt_inv);
        //テキストの背景色変更
        st_sakuban.setBackgroundColor(Color.argb(0, 255, 255, 0));
        st_tyuban.setBackgroundColor(Color.argb(0, 255, 255, 0));
        target.setBackgroundColor(Color.argb(0, 255, 255, 0));
        sakuban.setBackgroundColor(Color.argb(0, 255, 255, 0));
        tyuban.setBackgroundColor(Color.argb(0, 255, 255, 0));
        write.setBackgroundColor(Color.argb(0, 255, 255, 0));
        written.setBackgroundColor(Color.argb(0, 255, 255, 0));
        //テキストの中身初期化
        st_sakuban.setText("");
        st_tyuban.setText("");
        txtInv.setText("");

        Canvas canvas;
        canvas = new Canvas(invMap);
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);

    }






}