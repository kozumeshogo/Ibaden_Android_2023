package jp.co.tss21.sample.android.inventorytag;

import static java.lang.Integer.parseInt;

import android.annotation.TargetApi;
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
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Build;
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

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

//import net.sf.javaml.classification.Classifier;
//import net.sf.javaml.classification.tree.RandomForest;
//import net.sf.javaml.core.Dataset;
//import net.sf.javaml.tools.data.FileHandler;

import java.io.BufferedReader;
//import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import jp.co.tss21.uhfrfid.dotr_android.EnChannel;
import jp.co.tss21.uhfrfid.dotr_android.EnMaskFlag;
import jp.co.tss21.uhfrfid.dotr_android.EnMemoryBank;
import jp.co.tss21.uhfrfid.dotr_android.EnSession;
import jp.co.tss21.uhfrfid.dotr_android.EnTagAccessFlag;
import jp.co.tss21.uhfrfid.dotr_android.OnDotrEventListener;
import jp.co.tss21.uhfrfid.dotr_android.TagAccessParameter;
import jp.co.tss21.uhfrfid.tssrfid.TssRfidUtill;

/**
 * jp.co.tss21.uhfrfid.dotr_androidパッケージを参照します
 */
//import androidx.appcompat.app.AppCompatActivity;


/**
 * メイン
 *
 * @author Tohoku Systems Support.Co.,Ltd.
 */
public class InventoryTagDemo<mBtnADD> extends TabActivity implements View.OnClickListener,OnDotrEventListener {
    private final static String TAG = InventoryTagDemo.class.getSimpleName();

    private TabHost mTabHost;

    //#0001 変数宣言
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
    double add_height = 2.3;//アドレスRFIDタグの高さ z軸add変更

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
    private double add_4_x = 18.0; //20230515 アンテナ4追加　孤爪
    private double add_4_y = 0.0;
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
    private Button mBtnADD; //20230416 ADD変数追加　孤爪
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
    //#0001fin



    /**
     * Called when the activity is first created.
     */
    //#0002 メニューバーの表示
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
    //#0002fin


    //#0003 イベント関連の変数宣言
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

        //#0004 マップ切替用ラジオボタンに関する処理
        //マップ番号取得20221025
        mRadioGroupMapSelect = (RadioGroup)findViewById(R.id.rdgroup_map_select);
        mRadioGroupMapSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId != -1) {
                    // 選択されているラジオボタンの取得
                    RadioButton radioButton = (RadioButton) findViewById(checkedId);
                    //inv_map_flg = 2131230833 - checkedId; //20230424 値変更？　孤爪
                    inv_map_flg = 2131230849 - checkedId;
                    Log.d("マップ選択", String.valueOf(inv_map_flg));
                    //アドレスRFIDタグ座標設定
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
                        Log.d("map", "lab");
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
                        Log.d("map", "inv");
                    }
                    else if(inv_map_flg==2){//マップ「工場」20211108
                        inv_map_flg=2;

                        dotm_x = 186;
                        dotm_y = 186;
                        origin_x = 560;
                        origin_y = 2510;

                        add_1_x=6.0;
                        add_1_y=0.0;
                        add_2_x=6.0;
                        add_2_y=10.0;
                        add_3_x=18.0;
                        add_3_y=10.0;
                        add_4_x=18.0;  //20230515 アンテナ4追加　孤爪
                        add_4_y=0.0;

                        invMap = mapData(R.drawable.ibaden_factory);
                        Log.d("map", "fac");
                    }
                } else {
                    // 何も選択されていない場合の処理
                }
            }
        });
        //#0004fin

        //#0005 金属板の面数切替用ラジオボタンに関する処理
        //金属板の面数20221025
        mRadioGroupSurfaceSelect = (RadioGroup)findViewById(R.id.rdgroup_surface_select);
        mRadioGroupSurfaceSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId != -1){
                    //surface_flg = checkedId - 2131230834;
                    surface_flg = checkedId - 2131230850; //20230424 値変更？　孤爪
                    Log.d("面数選択", String.valueOf(surface_flg));

                    //多点CAL用パラメータ20221024
                    EditText multi_a_origin = (EditText) findViewById(R.id.multi_a);
                    EditText multi_b_origin = (EditText) findViewById(R.id.multi_b);
                    if(surface_flg==0){//3面
                        multi_a_origin.setText("11.55");
                        multi_b_origin.setText("42.434");
                    }
                    else if(surface_flg==1){//4面
                        multi_a_origin.setText("10.65");
                        multi_b_origin.setText("44.342");
                    }

                    setAddTag();
                }
                else{

                }
            }

        });
        setAddTag();
        //#0005fin

        //#0006 測距方法切替用ラジオボタンに関する処理
        /** CALタブ用 */
        mBtnCAL = (Button) findViewById(R.id.btn_cal);
        mBtnCAL.setOnClickListener(this);
        mRadioGroupCalSelect = (RadioGroup)findViewById(R.id.rdgroup_cal_select);
        mRadioGroupCalSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId != -1) {
                    //ranging_method_flg = 2131230830 - checkedId;
                    ranging_method_flg = 2131230846 - checkedId; //20230424 値変更？　孤爪
                    Log.d("測距手法選択", String.valueOf(ranging_method_flg));
                } else {

                }
            }
        });
        //#0006fin


        //#0007 cal用アドレスタグ選択spinnerの処理
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
        //#0007fin

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

        //20230416 ADD登録追加　孤爪
        /** ADD登録タブ用 */

        mBtnADD = (Button) findViewById(R.id.btn_add);
        mBtnADD.setOnClickListener(this);


        //#0008 検索時物品選択用spnnerの処理
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
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                Spinner spinner = (Spinner)parent;
                String item = (String)spinner.getSelectedItem();
                goods_epc.setText(search_goods_epc.get(spinner.getSelectedItemPosition()));
                goods_lasttime.setText(search_goods_lasttime.get(spinner.getSelectedItemPosition()));

                //検索マップ描画パラメータ
                int search_x = parseInt(search_goods_coorx.get(spinner.getSelectedItemPosition()));
                int search_y = parseInt(search_goods_coory.get(spinner.getSelectedItemPosition()));
                int search_dotm_x = 0;
                int search_dotm_y = 0;
                int search_map_flg = parseInt(search_goods_map.get(spinner.getSelectedItemPosition()));
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
        //#0008fin

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

        //#0009 シークバー関連の処理
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
        //#0009fin

        //マップデフォルト（落ち防止）20220502
        invMap = mapData(R.drawable.laboratory);
    }
    //#0003fin


    @Override

    //#0010 クリックイベント一覧
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
            case R.id.btn_add:    //ADD登録　20230416 孤爪
                TextView tv_1x_add = (TextView) findViewById(R.id.add_1_posx);
                TextView tv_1y_add = (TextView) findViewById(R.id.add_1_posy);
                TextView tv_1z_add = (TextView) findViewById(R.id.add_1_posz);
                TextView tv_2x_add = (TextView) findViewById(R.id.add_2_posx);
                TextView tv_2y_add = (TextView) findViewById(R.id.add_2_posy);
                TextView tv_2z_add = (TextView) findViewById(R.id.add_2_posz);
                TextView tv_3x_add = (TextView) findViewById(R.id.add_3_posx);
                TextView tv_3y_add = (TextView) findViewById(R.id.add_3_posy);
                TextView tv_3z_add = (TextView) findViewById(R.id.add_3_posz);
                TextView tv_4x_add = (TextView) findViewById(R.id.add_4_posx);
                TextView tv_4y_add = (TextView) findViewById(R.id.add_4_posy);  //20230515 アンテナ4 dotm追加 孤爪
                TextView tv_4z_add = (TextView) findViewById(R.id.add_4_posz);
                TextView tv_dotm_x = (TextView) findViewById(R.id.add_dotm_x);
                TextView tv_dotm_y = (TextView) findViewById(R.id.add_dotm_y);
                add_1_x = Double.parseDouble(tv_1x_add.getText().toString());
                add_1_y = Double.parseDouble(tv_1y_add.getText().toString());
                add_height = Double.parseDouble(tv_1z_add.getText().toString());
                add_2_x = Double.parseDouble(tv_2x_add.getText().toString());
                add_2_y = Double.parseDouble(tv_2y_add.getText().toString());
                add_3_x = Double.parseDouble(tv_3x_add.getText().toString());
                add_3_y = Double.parseDouble(tv_3y_add.getText().toString());
                add_4_x = Double.parseDouble(tv_4x_add.getText().toString());
                add_4_y = Double.parseDouble(tv_4y_add.getText().toString());
                dotm_x = parseInt(tv_dotm_x.getText().toString());
                dotm_y = parseInt(tv_dotm_y.getText().toString());
                break;
        }
    }
    //#0010fin

    //#0011 イベントハンドラ関連
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

    //#0011fin


    /** ---------------------------------------- ----------------------------------------
     * デバイスタブ用
     * ---------------------------------------- ---------------------------------------- */

    //#0012 RFID関連初期値設定
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
    //#0012fin


    static final int ListBtDevice_Activity = 1;
    static final int REQUEST_ENABLE_BT = 2;

    /*
     * アクティビティの応答
     * (非 Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    //#0013 Bluetooth関連設定
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
    //#0013fin


    /** ---------------------------------------- ----------------------------------------
     * ログタブ用
     * ---------------------------------------- ---------------------------------------- */
    //#0014 リストにEPC, RSSIの表示処理
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
    //#0014fin


    /*
     * 接続完了イベント
     * (非 Javadoc)
     * @see jp.co.tss21.uhfrfid.dotr_android.OnDotrEventListener#onConnected()
     */
    //#0015 RFID R/W接続時の処理 (R/W: リーダ/ライタ)
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

    }
    //#0015fin


    /*
     * 接続解除イベント
     * (非 Javadoc)
     * @see jp.co.tss21.uhfrfid.dotr_android.OnDotrEventListener#onDisconnected()
     */
    //#0016 RFID R/W切断時の処理
    @Override
    public void onDisconnected() {
        Log.d(TAG, "onDisconnected");
        mDotrUtil = new TssRfidUtill();
        mDotrUtil.setOnDotrEventListener(this);
        mHandler.sendMessage(mHandler.obtainMessage(MSG_BACK_COLOR, Toast.LENGTH_SHORT, getResources().getColor(R.color.background_disconnect), null));
        showToastByOtherThread("切断しました。", Toast.LENGTH_SHORT);

        epcadd.clear();
    }
    //#0016fin


    /*
     * リンク切れイベント
     * (非 Javadoc)
     * @see jp.co.tss21.uhfrfid.dotr_android.OnDotrEventListener#onLinkLost()
     */
    //#0017 リンク切れ時の処理
    @Override
    public void onLinkLost() {
        Log.d(TAG, "onLinkLost");

        mHandler.sendMessage(mHandler.obtainMessage(MSG_BACK_COLOR, Toast.LENGTH_SHORT, getResources().getColor(R.color.background_disconnect), null));
        showToastByOtherThread("リンクが切れました。", Toast.LENGTH_SHORT);
    }
    //#0017fin


    /*
     * トリガ状態変更イベント
     * (非 Javadoc)
     * @see jp.co.tss21.uhfrfid.dotr_android.OnDotrEventListener#onTriggerChaned(boolean)
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onTriggerChaned(boolean trigger) {
        Log.d(TAG, "onTriggerChaned(" + trigger + ")");

        //#0018 RFID R/Wのトリガーを引いた時の処理
        if (trigger) {
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


        }
        //#0018fin
        //#0019 RFID R/Wのトリガーを離した時の処理
        else {
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
                                dis_dg = NanIsZero(base_dg * Math.pow(10, ((cal_rssi1 - rssi) / sub_const)));//アドレスRFIDタグからの斜め距離, sub_constは減衰定数N
                            }
                            else if(ranging_method_flg==1){//多点CAL
                                dis_dg =  NanIsZero(Math.exp(-((rssi+curve_b)/curve_a)));

                            }


                            dis = NanIsZero(Math.sqrt((Math.pow(dis_dg, 2)) - (Math.pow(add_height - read_height, 2))));//アドレスRFIDタグからの水平距離

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





                    //表示用テキスト設定20220418
                    int j=0;
                    for(Object item_ : addlist){
                        j++;
                    }
                    //描画用パラメータ20220418
                    String[] add_epc = new String[j];
                    Double[] add_dis = new Double[j];
                    Double[] add_rssi = new Double[j];
                    Double[] add_rssi_tr = new Double[j];

                    Paint paint = new Paint();
                    paint.setStrokeWidth(20);

                    //重み平均を用いる際に使用する(セクターの弧の中心の)座標（m）
                    List<Double> est_x_m = new ArrayList<Double>();
                    List<Double> est_y_m = new ArrayList<Double>();

                    double det_sum_x = 0;//距離重みの分子x初期値
                    double det_sum_y = 0;//距離重みの分子yの初期値
                    double dis_weight_sum = 1;//距離重みの分母の初期値

                    double first_x = 0;//1stの扇形の弧の中心のx座標
                    double first_y = 0;//1stの扇形の弧の中心のy座標

                    //P=α*exp(-β*dis1n)
                    /*
                    TextView dis_weight_alpha_ = (TextView)findViewById(R.id.dis_weight_alpha_set);
                    TextView dis_weight_beta_ = (TextView)findViewById(R.id.dis_weight_beta_set);
                    Log.d("aaa", "aaa");
                    double dis_weight_alpha = Double.parseDouble((String)dis_weight_alpha_.getText());//距離重み式のパラメータα
                    double dis_weight_beta =Double.parseDouble((String)dis_weight_beta_.getText());//距離重み式のパラメータβ

                     */

                    double dis_weight_alpha = 1;//距離重み式のパラメータα
                    double dis_weight_beta =0.460517;//距離重み式のパラメータβ

                    //範囲円の中心の座標（m）
                    double det_x_m = 0;
                    double det_y_m = 0;
                    //範囲円の中心の座標（dot）
                    int det_x = origin_x;
                    int det_y = origin_y;
                    //範囲円の半径(dot)
                    int range_x = (int)(range_x_m * dotm_x);
                    int range_y = (int)(range_y_m * dotm_y);

                    int i=0;
                    int inv_alpha = 255;//描画時のアルファ値（透け具合）
                    int inv_color = 150;
                    String epc_1st = "";
                    String epc_2nd = "";
                    String epc_3rd = "";
                    String epc_4th = "";
                    String epc_5th = "";
                    String epc_6th = "";
                    String epc_7th = "";
                    String epc_8th = "";
                    String epc_9th = "";
                    int n = 0;
                    for(Object item_ : addlist)//item_: [EPC, RSSI, 読み取り回数, 距離 RSSI真値]
                    {
                        ArrayList item = (ArrayList) item_;

                            if (n == 0)
                            {
                                epc_1st = String.valueOf(item.get(0));
                            }
                            if (n == 1)
                            {
                                epc_2nd = String.valueOf(item.get(0));;
                            }
                            else if (n == 2)
                            {
                                epc_3rd = String.valueOf(item.get(0));;
                            }
                            else if (n == 3)
                            {
                                epc_4th = String.valueOf(item.get(0));;
                            }
                            else if (n == 4)
                            {
                                epc_5th = String.valueOf(item.get(0));;
                            }
                            else if (n == 5)
                            {
                                epc_6th = String.valueOf(item.get(0));;
                            }
                            else if (n == 6)
                            {
                                epc_7th = String.valueOf(item.get(0));;
                            }
                            else if (n == 7)
                            {
                                epc_8th = String.valueOf(item.get(0));;
                            }
                            else if (n == 8)
                            {
                                epc_9th = String.valueOf(item.get(0));;
                            }
                            n++;



                    }
                    //testAI宣言　20231024 孤爪
                    double rssiout1 = -99999;
                    double rssiout2 = -99999;
                    double rssiout3 = -99999;
                    double rssiout4 = -99999;
                    double rssiout5 = -99999;
                    double rssiout6 = -99999;
                    double rssiout7 = -99999;
                    double rssiout8 = -99999;
                    double rssiout9 = -99999;
                    double rssiout96 = -99999;
                    double rssiout97 = -99999;
                    double rssiout98 = -99999;

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

                        //testAI 20231024 孤爪


                          String addepc0;
                          String addrssi0 = String.valueOf(item.get(1));
                          addepc0 = String.valueOf(item.get(0));
                        Log.d("addepc",addepc0 );

                        if (addepc0.equals("3000000000000000000000000031")) {rssiout1 = Double.parseDouble(String.valueOf(item.get(1))); }
                        else if (addepc0.equals("3000000000000000000000000032")) {rssiout2 = Double.parseDouble(String.valueOf(item.get(1))); }
                        else if (addepc0.equals("3000000000000000000000000033")) {rssiout3 = Double.parseDouble(String.valueOf(item.get(1))); }
                        else if (addepc0.equals("3000000000000000000000000034")) {rssiout4 = Double.parseDouble(String.valueOf(item.get(1))); }
                        else if (addepc0.equals("3000000000000000000000000035")) {rssiout5 = Double.parseDouble(String.valueOf(item.get(1))); }
                        else if (addepc0.equals("3000000000000000000000000036")) {rssiout6 = Double.parseDouble(String.valueOf(item.get(1))); }
                        else if (addepc0.equals("3000000000000000000000000037")) {rssiout7 = Double.parseDouble(String.valueOf(item.get(1))); }
                        else if (addepc0.equals("3000000000000000000000000038")) {rssiout8 = Double.parseDouble(String.valueOf(item.get(1))); }
                        else if (addepc0.equals("3000000000000000000000000039")) {rssiout9 = Double.parseDouble(String.valueOf(item.get(1))); }
                        else if (addepc0.equals("3000000000000000000000000096")) {rssiout96 = Double.parseDouble(String.valueOf(item.get(1))); }
                        else if (addepc0.equals("3000000000000000000000000097")) {rssiout97 = Double.parseDouble(String.valueOf(item.get(1))); }
                        else if (addepc0.equals("3000000000000000000000000098")) {rssiout98 = Double.parseDouble(String.valueOf(item.get(1))); }

                        paint.setStyle(Paint.Style.STROKE);
                        paint.setAntiAlias(true);
                        add_epc[i] = String.valueOf(item.get(0));
                        add_rssi[i] = Double.parseDouble(String.valueOf(item.get(1)));
                        add_dis[i] = Double.parseDouble(String.valueOf(item.get(3)));
                        add_rssi_tr[i] = Double.parseDouble(String.valueOf(item.get(4)));

                        //epc1st判定20230310 孤爪

                        /*
                        String epc_1st = "";
                        String epc_2nd = "";
                        String epc_3rd = "";
                        String epc_4th = "";
                        String epc_5th = "";
                        String epc_6th = "";
                        String epc_7th = "";
                        String epc_8th = "";
                        String epc_9th = "";


                        for (int n = 0; n < add_epc.length; n++)
                        {
                            if (n == 0)
                            {
                                epc_1st = add_epc[n];
                            }
                            if (n == 1)
                            {
                                epc_2nd = add_epc[n];
                            }
                            else if (n == 2)
                            {
                                epc_3rd = add_epc[n];
                            }
                            else if (n == 3)
                            {
                                epc_4th = add_epc[n];
                            }
                            else if (n == 4)
                            {
                                epc_5th = add_epc[n];
                            }
                            else if (n == 5)
                            {
                                epc_6th = add_epc[n];
                            }
                            else if (n == 6)
                            {
                                epc_7th = add_epc[n];
                            }
                            else if (n == 7)
                            {
                                epc_8th = add_epc[n];
                            }
                            else if (n == 8)
                            {
                                epc_9th = add_epc[n];
                            }
                        }



                         */


                        //反対除去 20230310 孤爪
                        int judge = 0;
                        Log.d ("確認epc1st_1",epc_1st);
                        if(surface_flg==0){//3面
                           judge = Rem_Sector_3(epc_1st, epc_2nd, epc_3rd, epc_4th, epc_5th, epc_6th, epc_7th, epc_8th, epc_9th);
                        }

                        //testAI出力　20231024 孤爪
                        if (judge == 31){rssiout1 = -99999.9;}
                        else if (judge == 32){rssiout2 = -99999.9;}
                        else if (judge == 33){rssiout3 = -99999.9;}
                        else if (judge == 34){rssiout4 = -99999.9;}
                        else if (judge == 35){rssiout5 = -99999.9;}
                        else if (judge == 36){rssiout6 = -99999.9;}
                        else if (judge == 37){rssiout7 = -99999.9;}
                        else if (judge == 38){rssiout8 = -99999.9;}
                        else if (judge == 39){rssiout9 = -99999.9;}
                        else if (judge == 96){rssiout96 = -99999.9;}
                        else if (judge == 97){rssiout97 = -99999.9;}
                        else if (judge == 98){rssiout98 = -99999.9;}







                        Log.d ("確認",String.valueOf(judge));

                        //セクターの描画20221206
                        paint.setColor(Color.argb(inv_alpha, inv_color, 255, inv_color));//緑
                        paint.setStyle(Paint.Style.STROKE);
                        if(surface_flg==0){//3面
                            Inv_Sector_3(add_epc[i], add_dis[i], canvas, paint, est_x_m, est_y_m,judge); //judeg追加　20230310 孤爪
                        }
                        else if(surface_flg==1){//4面
                            Inv_Sector_4(add_epc[i], add_dis[i], canvas, paint, est_x_m, est_y_m);
                        }

                        if(i==0){
                            first_x = est_x_m.get(i);
                            first_y = est_y_m.get(i);
                            det_sum_x += est_x_m.get(i);
                            det_sum_y += est_y_m.get(i);
                        }
                        else{
                            //推定座標の分子、分母の加算20221206
                            double dis_weight = dis_weight_alpha * Math.exp(-(dis_weight_beta*Math.sqrt(Math.pow((first_x-est_x_m.get(i)), 2)+Math.pow((first_y-est_y_m.get(i)), 2))));//距離重み
                            det_sum_x += dis_weight * est_x_m.get(i);//x成分の分子の足し算
                            det_sum_y += dis_weight * est_y_m.get(i);//y成分の分子の足し算
                            dis_weight_sum += dis_weight;//分母の足し算（距離重みの和）

                            Log.d((i+1) + "番目", String.valueOf(dis_weight));
                        }




                        inv_alpha -= 20;
                        inv_color -= 10;
                        i++;

                    }
                    //testAI出力　20231024 孤爪
                    TextView txt_store_x_m_ = (TextView) findViewById(R.id.txt_store_x_m);
                    TextView txt_store_y_m_ = (TextView) findViewById(R.id.txt_store_y_m);
                    float store_x_m = Float.valueOf(txt_store_x_m_.getText().toString());
                    float store_y_m = Float.valueOf(txt_store_y_m_.getText().toString());

                    try {
                        //FileWriter writer = new FileWriter("/files/testAI.txt", true);  // "true" を指定すると追記モードになります
                        File store_file = new File("/data/data/" + this.getPackageName() + "/files/testAI.txt");
                        FileWriter writer = new FileWriter(store_file,true);

                        //writer.append("#epc#").append(String.valueOf(addepc0));
                        //writer.append("#rssi#").append(String.valueOf(addrssi0));
                        //writer.append("#judge#").append(String.valueOf(judge));
                        writer.append("#31#").append(String.valueOf(rssiout1));
                        writer.append("#32#").append(String.valueOf(rssiout2));
                        writer.append("#33#").append(String.valueOf(rssiout3));
                        writer.append("#34#").append(String.valueOf(rssiout4));
                        writer.append("#35#").append(String.valueOf(rssiout5));
                        writer.append("#36#").append(String.valueOf(rssiout6));
                        writer.append("#37#").append(String.valueOf(rssiout7));
                        writer.append("#38#").append(String.valueOf(rssiout8));
                        writer.append("#39#").append(String.valueOf(rssiout9));
                        // アンテナ4本で追加 20230426 孤爪
                        writer.append("#96#").append(String.valueOf(rssiout96));
                        writer.append("#97#").append(String.valueOf(rssiout97));
                        writer.append("#98#").append(String.valueOf(rssiout98));

                        writer.append("#X#").append(String.valueOf(store_x_m)).append("#Y#").append(String.valueOf(store_y_m)).append("#");

                        writer.append(System.lineSeparator());  // 改行を追加

                        writer.close();  // ファイルを閉じる
                    } catch (IOException e) {
                        e.printStackTrace();  // エラーハンドリング
                    }

                    /*
                    //分類用csv出力　20231123 孤爪
                    double[] rssirist = {rssiout1,rssiout2,rssiout3,rssiout4,rssiout5,rssiout6,rssiout7,rssiout8,rssiout9,rssiout96,rssiout97,rssiout98};
                    try {
                        // 出力ファイルの作成
                        PrintWriter p = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream("/data/data/" + this.getPackageName() + "/files/testdata.csv", false),"Shift-JIS")));


                        // 内容をセットする
                        for(int csvc = 0; csvc < 12; csvc++){
                            p.print(rssirist[csvc]);
                            p.print(",");
                        }
                        p.print(0);
                        p.println();    // 改行
                        // ファイルに書き出し閉じる
                        p.close();
                        System.out.println("ファイル出力完了");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                     */

                    //分類推定　20231123 孤爪
                    /*

                    String filePath1 = "/data/data/" + this.getPackageName() + "/files/rearranging_24_分類.csv";
                    String filePath2 = "/data/data/" + this.getPackageName() + "/files/testdata.csv";
                    Dataset data = FileHandler.loadDataset(new File(filePath1), 12, ",");
                    // ランダムフォレストの作成と学習
                    Classifier rf = new RandomForest(100);
                    rf.buildClassifier(data);

                    // 検証用データの読み込み（同じデータを使用）
                    FileHandler.loadDataset(new File(filePath2), 12, ",")
                            .stream()
                            //reduceでfor文と同じ処理を行う
                            .reduce(new HashMap<String,Integer>(), //ループの初期値を設定する
                                    (accum, ins) -> {return createOperation(accum, ins,rf);},//ループで適用する処理を記載する
                                    (s,v)->v)//paralle処理を考慮しない場合は適当なラムダ式を渡す
                            //forEachでMapのキーと値の組み合わせを列挙する
                            .forEach((m1,m2)->System.out.println(String.format("%s : %d", m1,m2)));

                     */



                    //アンテナ4追加用　20231024 孤爪
                    /*try {
                        //FileWriter writer = new FileWriter("/files/testAI.txt", true);  // "true" を指定すると追記モードになります
                        File store_file = new File("/data/data/" + this.getPackageName() + "/files/epc_add_3.csv");
                        FileWriter writer = new FileWriter(store_file,false);


                        writer.write("1-1,3000000000000000000000000031,1,1");
                        writer.write(System.lineSeparator());  // 改行を追加
                        writer.write("1-2,3000000000000000000000000032,1,2");
                        writer.write(System.lineSeparator());  // 改行を追加
                        writer.write("1-3,3000000000000000000000000033,1,3");
                        writer.write(System.lineSeparator());  // 改行を追加
                        writer.write("2-1,3000000000000000000000000034,2,1");
                        writer.write(System.lineSeparator());  // 改行を追加
                        writer.write("2-2,3000000000000000000000000035,2,2");
                        writer.write(System.lineSeparator());  // 改行を追加
                        writer.write("2-3,3000000000000000000000000036,2,3");
                        writer.write(System.lineSeparator());  // 改行を追加
                        writer.write("3-1,3000000000000000000000000037,3,1");
                        writer.write(System.lineSeparator());  // 改行を追加
                        writer.write("3-2,3000000000000000000000000038,3,2");
                        writer.write(System.lineSeparator());  // 改行を追加
                        writer.write("3-3,3000000000000000000000000039,3,3");
                        writer.write(System.lineSeparator());  // 改行を追加
                        writer.write("4-1,3000000000000000000000000096,4,1");
                        writer.write(System.lineSeparator());  // 改行を追加
                        writer.write("4-2,3000000000000000000000000097,4,2");
                        writer.write(System.lineSeparator());  // 改行を追加
                        writer.write("4-3,3000000000000000000000000098,4,3");


                        writer.close();  // ファイルを閉じる
                    } catch (IOException e) {
                        e.printStackTrace();  // エラーハンドリング
                    }

                     */


                    //推定座標の算出及び推定円の描画20221206
                    det_x_m = det_sum_x / dis_weight_sum;
                    det_y_m = det_sum_y / dis_weight_sum;
                    det_x = origin_x + (int)(det_x_m*dotm_x);
                    det_y = origin_y - (int)(det_y_m*dotm_y);

                    //距離差計算20230202 //testAI出力と重複するため上4行コメント化　20231024　孤爪
                    //TextView txt_store_x_m_ = (TextView) findViewById(R.id.txt_store_x_m);
                    //TextView txt_store_y_m_ = (TextView) findViewById(R.id.txt_store_y_m);
                    //double store_x_m = Double.valueOf(txt_store_x_m_.getText().toString());
                    //double store_y_m = Double.valueOf(txt_store_y_m_.getText().toString());
                    double error_m = Math.sqrt(Math.pow(det_x_m - store_x_m,2)+Math.pow(det_y_m-store_y_m, 2));

                    /*
                    int store_x_dot = origin_x + (int)(store_x_m*dotm_x);
                    int store_y_dot = origin_y + (int)(store_y_m*dotm_y);

                     */

                    try{
                        File file = new File("/data/data/" + this.getPackageName() + "/files/error_logs.csv");
                        FileWriter filewriter = new FileWriter(file, true);

                        filewriter.write("(" + store_x_m  + ", " + store_y_m + ")#" + error_m + "\n");

                        filewriter.close();
                    }catch(IOException e){
                        System.out.println(e);
                    }



                    //真値平方根重みによる範囲円描画20220509
                    paint.setColor(Color.argb(128, 255, 255, 0));//黄
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawArc(det_x-range_x, det_y-range_y, det_x+range_x, det_y+range_y, 0, 360, false, paint);

                    /*
                    paint.setColor(Color.argb(128, 255, 255, 255));//黄
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawArc(store_x_dot-5, store_y_dot-5, store_x_dot+5, store_y_dot+5, 0, 360, false, paint);


                     */
                    /*
                    if(dis_weight_sum!=1){

                    }

                     */

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
        //#0019fin
    }


    /*
     * EPC一括読取りイベント
     * (非 Javadoc)
     * @see jp.co.tss21.uhfrfid.dotr_android.OnDotrEventListener#onInventoryEPC(java.lang.String)
     */
    //#0020 EPCを複数回読み取った時の読み取りごとの処理
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
    //#0020fin


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
    //0021 アドレスRFIDタグを読み取ったかどうかの判別
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
    //#0021fin

    //#0022 BitMapの生成(描画用)20220217
    private Bitmap mapData(int id){
        BitmapFactory.Options options = new  BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id, options);
        return bitmap;
    }
    //#0022fin

    //3面反対除去　20230310
    private int Rem_Sector_3(String epc_1st, String epc_2nd, String epc_3rd, String epc_4th, String epc_5th, String epc_6th, String epc_7th, String epc_8th, String epc_9th){
        int RSSIcheck1 = 0;
        int RSSIcheck2 = 0;
        int RSSIcheck3 = 0;
        int RSSIcheck4 = 0;
        int RSSIcheck5 = 0;
        int RSSIcheck6 = 0;
        int RSSIcheck7 = 0;
        int RSSIcheck8 = 0;
        int RSSIcheck9 = 0;
        int Rem = 0;

        Log.d ("確認epc1st",epc_1st);

        //31
        if (epc_1st.equals("3000000000000000000000000031")) { RSSIcheck1 = 311;}
        else if (epc_2nd.equals("3000000000000000000000000031")) { RSSIcheck2 = 312;}
        else if (epc_3rd.equals("3000000000000000000000000031")) { RSSIcheck3 = 313;}
        else if (epc_4th.equals("3000000000000000000000000031")){ RSSIcheck4 = 314;}
        else if (epc_5th.equals("3000000000000000000000000031")) { RSSIcheck5 = 315;}
        else if (epc_6th.equals("3000000000000000000000000031")) { RSSIcheck6 = 316;}
        else if (epc_7th.equals("3000000000000000000000000031")) { RSSIcheck7 = 317;}
        else if (epc_8th.equals("3000000000000000000000000031")) { RSSIcheck8 = 318;}
        else if (epc_9th.equals("3000000000000000000000000031")) { RSSIcheck9 = 319;}
        Log.d ("確認1",epc_1st);
        Log.d ("確認2.1",epc_2nd);

        //32 変更前 -71.60
        if (epc_1st.equals("3000000000000000000000000032")) { RSSIcheck1 = 321;}
        else if (epc_2nd.equals("3000000000000000000000000032")) { RSSIcheck2 = 322;}
        else if (epc_3rd.equals("3000000000000000000000000032")) { RSSIcheck3 = 323;}
        else if (epc_4th.equals("3000000000000000000000000032")) { RSSIcheck4 = 324;}
        else if (epc_5th.equals("3000000000000000000000000032")) { RSSIcheck5 = 325;}
        else if (epc_6th.equals("3000000000000000000000000032")) { RSSIcheck6 = 326;}
        else if (epc_7th.equals("3000000000000000000000000032")) { RSSIcheck7 = 327;}
        else if (epc_8th.equals("3000000000000000000000000032")) { RSSIcheck8 = 328;}
        else if (epc_9th.equals("3000000000000000000000000032")) { RSSIcheck9 = 329;}
        Log.d ("確認2",epc_2nd);

        //33 変更前  -75.49
        if (epc_1st.equals("3000000000000000000000000033")) { RSSIcheck1 = 331;}
        else if (epc_2nd.equals("3000000000000000000000000033")) { RSSIcheck2 = 332;}
        else if (epc_3rd.equals("3000000000000000000000000033")) { RSSIcheck3 = 333;}
        else if (epc_4th.equals("3000000000000000000000000033")) { RSSIcheck4 = 334;}
        else if (epc_5th.equals("3000000000000000000000000033")) { RSSIcheck5 = 335;}
        else if (epc_6th.equals("3000000000000000000000000033")) { RSSIcheck6 = 336;}
        else if (epc_7th.equals("3000000000000000000000000033")) { RSSIcheck7 = 337;}
        else if (epc_8th.equals("3000000000000000000000000033")) { RSSIcheck8 = 338;}
        else if (epc_9th.equals("3000000000000000000000000033")) { RSSIcheck9 = 339;}
        Log.d ("確認3",epc_3rd);

        //34 変更前
        if (epc_1st.equals("3000000000000000000000000034")) { RSSIcheck1 = 341;}
        else if (epc_2nd.equals("3000000000000000000000000034")) { RSSIcheck2 = 342;}
        else if (epc_3rd.equals("3000000000000000000000000034")) { RSSIcheck3 = 343;}
        else if (epc_4th.equals("3000000000000000000000000034")) { RSSIcheck4 = 344;}
        else if (epc_5th.equals("3000000000000000000000000034")) { RSSIcheck5 = 345;}
        else if (epc_6th.equals("3000000000000000000000000034")) { RSSIcheck6 = 346;}
        else if (epc_7th.equals("3000000000000000000000000034")) { RSSIcheck7 = 347;}
        else if (epc_8th.equals("3000000000000000000000000034")) { RSSIcheck8 = 348;}
        else if (epc_9th.equals("3000000000000000000000000034")) { RSSIcheck9 = 349;}
        Log.d ("確認4",epc_4th);

        //35 変更前
        if (epc_1st.equals("3000000000000000000000000035")) { RSSIcheck1 = 351;}
        else if (epc_2nd.equals("3000000000000000000000000035")) { RSSIcheck2 = 352;}
        else if (epc_3rd.equals("3000000000000000000000000035")) { RSSIcheck3 = 353; }
        else if (epc_4th.equals("3000000000000000000000000035")) { RSSIcheck4 = 354;}
        else if (epc_5th.equals("3000000000000000000000000035")) { RSSIcheck5 = 355;}
        else if (epc_6th.equals("3000000000000000000000000035")) { RSSIcheck6 = 356;}
        else if (epc_7th.equals("3000000000000000000000000035")) { RSSIcheck7 = 357;}
        else if (epc_8th.equals("3000000000000000000000000035")) { RSSIcheck8 = 358;}
        else if (epc_9th.equals("3000000000000000000000000035")) { RSSIcheck9 = 359;}
        Log.d ("確認5",epc_5th);

        //36 変更前 -67.81
        if (epc_1st.equals("3000000000000000000000000036")) { RSSIcheck1 = 361;}
        else if (epc_2nd.equals("3000000000000000000000000036")) { RSSIcheck2 = 362;}
        else if (epc_3rd.equals("3000000000000000000000000036")) { RSSIcheck3 = 363;}
        else if (epc_4th.equals("3000000000000000000000000036")) { RSSIcheck4 = 364;}
        else if (epc_5th.equals("3000000000000000000000000036")) { RSSIcheck5 = 365;}
        else if (epc_6th.equals("3000000000000000000000000036")) { RSSIcheck6 = 366;}
        else if (epc_7th.equals("3000000000000000000000000036")) { RSSIcheck7 = 367;}
        else if (epc_8th.equals("3000000000000000000000000036")) { RSSIcheck8 = 368;}
        else if (epc_9th.equals("3000000000000000000000000036")) { RSSIcheck9 = 369;}
        Log.d ("確認6",epc_6th);

        //37 変更前 -71.6
        if (epc_1st.equals("3000000000000000000000000037")) { RSSIcheck1 = 371;}
        else if (epc_2nd.equals("3000000000000000000000000037")) { RSSIcheck2 = 372;}
        else if (epc_3rd.equals("3000000000000000000000000037")) { RSSIcheck3 = 373;}
        else if (epc_4th.equals("3000000000000000000000000037")) { RSSIcheck4 = 374;}
        else if (epc_5th.equals("3000000000000000000000000037")) { RSSIcheck5 = 375;}
        else if (epc_6th.equals("3000000000000000000000000037")) { RSSIcheck6 = 376;}
        else if (epc_7th.equals("3000000000000000000000000037")) { RSSIcheck7 = 377;}
        else if (epc_8th.equals("3000000000000000000000000037")) { RSSIcheck8 = 378;}
        else if (epc_9th.equals("3000000000000000000000000037")) { RSSIcheck9 = 379;}

        //38 変更前
        if (epc_1st.equals("3000000000000000000000000038")) { RSSIcheck1 = 381;}
        else if (epc_2nd.equals("3000000000000000000000000038")) { RSSIcheck2 = 382;}
        else if (epc_3rd.equals("3000000000000000000000000038")) { RSSIcheck3 = 383;}
        else if (epc_4th.equals("3000000000000000000000000038")) { RSSIcheck4 = 384;}
        else if (epc_5th.equals("3000000000000000000000000038")) { RSSIcheck5 = 385;}
        else if (epc_6th.equals("3000000000000000000000000038")) { RSSIcheck6 = 386;}
        else if (epc_7th.equals("3000000000000000000000000038")) { RSSIcheck7 = 387;}
        else if (epc_8th.equals("3000000000000000000000000038")) { RSSIcheck8 = 388;}
        else if (epc_9th.equals("3000000000000000000000000038")) { RSSIcheck9 = 389;}

        //39 変更前 75.49
        if (epc_1st.equals("3000000000000000000000000039")) { RSSIcheck1 = 391;}
        else if (epc_2nd.equals("3000000000000000000000000039")) { RSSIcheck2 = 392;}
        else if (epc_3rd.equals("3000000000000000000000000039")) { RSSIcheck3 = 393;}
        else if (epc_4th.equals("3000000000000000000000000039")) { RSSIcheck4 = 394;}
        else if (epc_5th.equals("3000000000000000000000000039")) { RSSIcheck5 = 395;}
        else if (epc_6th.equals("3000000000000000000000000039")) { RSSIcheck6 = 396;}
        else if (epc_7th.equals("3000000000000000000000000039")) { RSSIcheck7 = 397;}
        else if (epc_8th.equals("3000000000000000000000000039")) { RSSIcheck8 = 398;}
        else if (epc_9th.equals("3000000000000000000000000039")) { RSSIcheck9 = 399;}

        //20230515 アンテナ4追加 孤爪
        //96
        if (epc_1st.equals("3000000000000000000000000096")) { RSSIcheck1 = 961;}
        else if (epc_2nd.equals("3000000000000000000000000096")) { RSSIcheck2 = 962;}
        else if (epc_3rd.equals("3000000000000000000000000096")) { RSSIcheck3 = 963;}
        else if (epc_4th.equals("3000000000000000000000000096")) { RSSIcheck4 = 964;}
        else if (epc_5th.equals("3000000000000000000000000096")) { RSSIcheck5 = 965;}
        else if (epc_6th.equals("3000000000000000000000000096")) { RSSIcheck6 = 966;}
        else if (epc_7th.equals("3000000000000000000000000096")) { RSSIcheck7 = 967;}
        else if (epc_8th.equals("3000000000000000000000000096")) { RSSIcheck8 = 968;}
        else if (epc_9th.equals("3000000000000000000000000096")) { RSSIcheck9 = 969;}
        
        //97
        if (epc_1st.equals("3000000000000000000000000097")) { RSSIcheck1 = 971;}
        else if (epc_2nd.equals("3000000000000000000000000097")) { RSSIcheck2 = 972;}
        else if (epc_3rd.equals("3000000000000000000000000097")) { RSSIcheck3 = 973;}
        else if (epc_4th.equals("3000000000000000000000000097")) { RSSIcheck4 = 974;}
        else if (epc_5th.equals("3000000000000000000000000097")) { RSSIcheck5 = 975;}
        else if (epc_6th.equals("3000000000000000000000000097")) { RSSIcheck6 = 976;}
        else if (epc_7th.equals("3000000000000000000000000097")) { RSSIcheck7 = 977;}
        else if (epc_8th.equals("3000000000000000000000000097")) { RSSIcheck8 = 978;}
        else if (epc_9th.equals("3000000000000000000000000097")) { RSSIcheck9 = 979;}
        
        //98
        if (epc_1st.equals("3000000000000000000000000098")) { RSSIcheck1 = 981;}
        else if (epc_2nd.equals("3000000000000000000000000098")) { RSSIcheck2 = 982;}
        else if (epc_3rd.equals("3000000000000000000000000098")) { RSSIcheck3 = 983;}
        else if (epc_4th.equals("3000000000000000000000000098")) { RSSIcheck4 = 984;}
        else if (epc_5th.equals("3000000000000000000000000098")) { RSSIcheck5 = 985;}
        else if (epc_6th.equals("3000000000000000000000000098")) { RSSIcheck6 = 986;}
        else if (epc_7th.equals("3000000000000000000000000098")) { RSSIcheck7 = 987;}
        else if (epc_8th.equals("3000000000000000000000000098")) { RSSIcheck8 = 988;}
        else if (epc_9th.equals("3000000000000000000000000098")) { RSSIcheck9 = 989;}



        if (RSSIcheck1 == 311)
        {
            if (RSSIcheck2 == 332) { Rem = 33;}
            else if (RSSIcheck3 == 333) { Rem = 33;}
            else if (RSSIcheck4 == 334) { Rem = 33;}
            else if (RSSIcheck5 == 335) { Rem = 33;}
            else if (RSSIcheck6 == 336) { Rem = 33;}
            else if (RSSIcheck7 == 337) { Rem = 33;}
            else if (RSSIcheck8 == 338) { Rem = 33;}
            else if (RSSIcheck9 == 339) { Rem = 33;}
        }
        //32
        if (RSSIcheck1 == 321)
        {
            if (RSSIcheck2 == 312)
            {
                if (RSSIcheck3 == 333) { Rem = 33;}
                else if (RSSIcheck4 == 334) { Rem = 33;}
                else if (RSSIcheck5 == 335) { Rem = 33;}
                else if (RSSIcheck6 == 336) { Rem = 33;}
                else if (RSSIcheck7 == 337) { Rem = 33;}
                else if (RSSIcheck8 == 338) { Rem = 33;}
                else if (RSSIcheck9 == 339) { Rem = 33;}
            }
            else if (RSSIcheck3 == 313)
            {
                if (RSSIcheck4 == 334) { Rem = 33;}
                else if (RSSIcheck5 == 335) { Rem = 33;}
                else if (RSSIcheck6 == 336) { Rem = 33;}
                else if (RSSIcheck7 == 337) { Rem = 33;}
                else if (RSSIcheck8 == 338) { Rem = 33;}
                else if (RSSIcheck9 == 339) { Rem = 33;}
            }
            else if (RSSIcheck4 == 314)
            {
                if (RSSIcheck5 == 335) { Rem = 33;}
                else if (RSSIcheck6 == 336) { Rem = 33;}
                else if (RSSIcheck7 == 337) { Rem = 33;}
                else if (RSSIcheck8 == 338) { Rem = 33;}
                else if (RSSIcheck9 == 339) { Rem = 33;}
            }
            else if (RSSIcheck5 == 315)
            {
                if (RSSIcheck6 == 336) { Rem = 33;}
                else if (RSSIcheck7 == 337) { Rem = 33;}
                else if (RSSIcheck8 == 338) { Rem = 33;}
                else if (RSSIcheck9 == 339) { Rem = 33;}
            }
            else if (RSSIcheck6 == 316)
            {
                if (RSSIcheck7 == 337) { Rem = 33;}
                else if (RSSIcheck8 == 338) { Rem = 33;}
                else if (RSSIcheck9 == 339) { Rem = 33;}
            }
            else if (RSSIcheck7 == 317)
            {
                if (RSSIcheck8 == 338) { Rem = 33;}
                else if (RSSIcheck9 == 339) { Rem = 33;}
            }
            else if (RSSIcheck8 == 318)
            {
                if (RSSIcheck9 == 339) { Rem = 33;}
            }

            if (RSSIcheck2 == 332)
            {
                if (RSSIcheck3 == 313) { Rem = 31;}
                else if (RSSIcheck4 == 314) { Rem = 31;}
                else if (RSSIcheck5 == 315) { Rem = 31;}
                else if (RSSIcheck6 == 316) { Rem = 31;}
                else if (RSSIcheck7 == 317) { Rem = 31;}
                else if (RSSIcheck8 == 318) { Rem = 31;}
                else if (RSSIcheck9 == 319) { Rem = 31;}
            }
            else if (RSSIcheck3 == 333)
            {
                if (RSSIcheck4 == 314) { Rem = 31;}
                else if (RSSIcheck5 == 315) { Rem = 31;}
                else if (RSSIcheck6 == 316) { Rem = 31;}
                else if (RSSIcheck7 == 317) { Rem = 31;}
                else if (RSSIcheck8 == 318) { Rem = 31;}
                else if (RSSIcheck9 == 319) { Rem = 31;}
            }
            else if (RSSIcheck4 == 334)
            {
                if (RSSIcheck5 == 315) { Rem = 31;}
                else if (RSSIcheck6 == 316) { Rem = 31;}
                else if (RSSIcheck7 == 317) { Rem = 31;}
                else if (RSSIcheck8 == 318) { Rem = 31;}
                else if (RSSIcheck9 == 319) { Rem = 31;}
            }
            else if (RSSIcheck5 == 335)
            {
                if (RSSIcheck6 == 316) { Rem = 31;}
                else if (RSSIcheck7 == 317) { Rem = 31;}
                else if (RSSIcheck8 == 318) { Rem = 31;}
                else if (RSSIcheck9 == 319) { Rem = 31;}
            }
            else if (RSSIcheck6 == 336)
            {
                if (RSSIcheck7 == 317) { Rem = 31;}
                else if (RSSIcheck8 == 318) { Rem = 31;}
                else if (RSSIcheck9 == 319) { Rem = 31;}
            }
            else if (RSSIcheck7 == 337)
            {
                if (RSSIcheck8 == 318) { Rem = 31;}
                else if (RSSIcheck9 == 319) { Rem = 31;}
            }
            else if (RSSIcheck8 == 338)
            {
                if (RSSIcheck9 == 319) { Rem = 31;}
            }
        }
        //33
        if (RSSIcheck1 == 331)
        {
            if (RSSIcheck2 == 312) { Rem = 31;}
            else if (RSSIcheck3 == 313) { Rem = 31;}
            else if (RSSIcheck4 == 314) { Rem = 31;}
            else if (RSSIcheck5 == 315) { Rem = 31;}
            else if (RSSIcheck6 == 316) { Rem = 31;}
            else if (RSSIcheck7 == 317) { Rem = 31;}
            else if (RSSIcheck8 == 318) { Rem = 31;}
            else if (RSSIcheck9 == 319) { Rem = 31;}
        }
        //34
        if (RSSIcheck1 == 341)
        {
            if (RSSIcheck2 == 362) { Rem = 36;}
            else if (RSSIcheck3 == 363) { Rem = 36;}
            else if (RSSIcheck4 == 364) { Rem = 36;}
            else if (RSSIcheck5 == 365) { Rem = 36;}
            else if (RSSIcheck6 == 366) { Rem = 36;}
            else if (RSSIcheck7 == 367) { Rem = 36;}
            else if (RSSIcheck8 == 368) { Rem = 36;}
            else if (RSSIcheck9 == 369) { Rem = 36;}
        }
        //35
        if (RSSIcheck1 == 351)
        {
            if (RSSIcheck2 == 342)
            {
                if (RSSIcheck3 == 363) { Rem = 36;}
                else if (RSSIcheck4 == 364) { Rem = 36;}
                else if (RSSIcheck5 == 365) { Rem = 36;}
                else if (RSSIcheck6 == 366) { Rem = 36;}
                else if (RSSIcheck7 == 367) { Rem = 36;}
                else if (RSSIcheck8 == 368) { Rem = 36;}
                else if (RSSIcheck9 == 369) { Rem = 36;}
            }
            else if (RSSIcheck3 == 343)
            {
                if (RSSIcheck4 == 364) { Rem = 36;}
                else if (RSSIcheck5 == 365) { Rem = 36;}
                else if (RSSIcheck6 == 366) { Rem = 36;}
                else if (RSSIcheck7 == 367) { Rem = 36;}
                else if (RSSIcheck8 == 368) { Rem = 36;}
                else if (RSSIcheck9 == 369) { Rem = 36;}
            }
            else if (RSSIcheck4 == 344)
            {
                if (RSSIcheck5 == 365) { Rem = 36;}
                else if (RSSIcheck6 == 366) { Rem = 36;}
                else if (RSSIcheck7 == 367) { Rem = 36;}
                else if (RSSIcheck8 == 368) { Rem = 36;}
                else if (RSSIcheck9 == 369) { Rem = 36;}
            }
            else if (RSSIcheck5 == 345)
            {
                if (RSSIcheck6 == 366) { Rem = 36;}
                else if (RSSIcheck7 == 367) { Rem = 36;}
                else if (RSSIcheck8 == 368) { Rem = 36;}
                else if (RSSIcheck9 == 369) { Rem = 36;}
            }
            else if (RSSIcheck6 == 346)
            {
                if (RSSIcheck7 == 367) { Rem = 36;}
                else if (RSSIcheck8 == 368) { Rem = 36;}
                else if (RSSIcheck9 == 369) { Rem = 36;}
            }
            else if (RSSIcheck7 == 347)
            {
                if (RSSIcheck8 == 368) { Rem = 36;}
                else if (RSSIcheck9 == 369) { Rem = 36;}
            }
            else if (RSSIcheck8 == 348)
            {
                if (RSSIcheck9 == 369) { Rem = 36;}
            }


            if (RSSIcheck2 == 362)
            {
                if (RSSIcheck3 == 343) { Rem = 34;}
                else if (RSSIcheck4 == 344) { Rem = 34;}
                else if (RSSIcheck5 == 345) { Rem = 34;}
                else if (RSSIcheck6 == 346) { Rem = 34;}
                else if (RSSIcheck7 == 347) { Rem = 34;}
                else if (RSSIcheck8 == 348) { Rem = 34;}
                else if (RSSIcheck9 == 349) { Rem = 34;}
            }
            else if (RSSIcheck3 == 363)
            {
                if (RSSIcheck4 == 344) { Rem = 34;}
                else if (RSSIcheck5 == 345) { Rem = 34;}
                else if (RSSIcheck6 == 346) { Rem = 34;}
                else if (RSSIcheck7 == 347) { Rem = 34;}
                else if (RSSIcheck8 == 348) { Rem = 34;}
                else if (RSSIcheck9 == 349) { Rem = 34;}
            }

            else if (RSSIcheck4 == 364)
            {
                if (RSSIcheck5 == 345) { Rem = 34;}
                else if (RSSIcheck6 == 346) { Rem = 34;}
                else if (RSSIcheck7 == 347) { Rem = 34;}
                else if (RSSIcheck8 == 348) { Rem = 34;}
                else if (RSSIcheck9 == 349) { Rem = 34;}
            }
            else if (RSSIcheck5 == 365)
            {
                if (RSSIcheck6 == 346) { Rem = 34;}
                else if (RSSIcheck7 == 347) { Rem = 34;}
                else if (RSSIcheck8 == 348) { Rem = 34;}
                else if (RSSIcheck9 == 349) { Rem = 34;}
            }
            else if (RSSIcheck6 == 366)
            {
                if (RSSIcheck7 == 347) { Rem = 34;}
                else if (RSSIcheck8 == 348) { Rem = 34;}
                else if (RSSIcheck9 == 349) { Rem = 34;}
            }
            else if (RSSIcheck7 == 367)
            {
                if (RSSIcheck8 == 348) { Rem = 34;}
                else if (RSSIcheck9 == 349) { Rem = 34;}
            }
            else if (RSSIcheck8 == 368)
            {
                if (RSSIcheck9 == 349) { Rem = 34;}
            }
        }
        //36
        if (RSSIcheck1 == 361)
        {
            if (RSSIcheck2 == 342) {  Rem = 34;}
            else if (RSSIcheck3 == 343) { Rem = 34;}
            else if (RSSIcheck4 == 344) { Rem = 34;}
            else if (RSSIcheck5 == 345) { Rem = 34;}
            else if (RSSIcheck6 == 346) { Rem = 34;}
            else if (RSSIcheck7 == 347) { Rem = 34;}
            else if (RSSIcheck8 == 348) { Rem = 34;}
            else if (RSSIcheck9 == 349) { Rem = 34;}
        }
        //37
        if (RSSIcheck1 == 371)
        {
            if (RSSIcheck2 == 392) { Rem = 39;}
            else if (RSSIcheck3 == 393) { Rem = 39;}
            else if (RSSIcheck4 == 394) { Rem = 39;}
            else if (RSSIcheck5 == 395) { Rem = 39;}
            else if (RSSIcheck6 == 396) { Rem = 39;}
            else if (RSSIcheck7 == 397) { Rem = 39;}
            else if (RSSIcheck8 == 398) { Rem = 39;}
            else if (RSSIcheck9 == 399) { Rem = 39;}
        }
        //38
        if (RSSIcheck1 == 381)
        {
            if (RSSIcheck2 == 372)
            {
                if (RSSIcheck3 == 393) { Rem = 39;}
                else if (RSSIcheck4 == 394) { Rem = 39;}
                else if (RSSIcheck5 == 395) { Rem = 39;}
                else if (RSSIcheck6 == 396) { Rem = 39;}
                else if (RSSIcheck7 == 397) { Rem = 39;}
                else if (RSSIcheck8 == 398) { Rem = 39;}
                else if (RSSIcheck9 == 399) { Rem = 39;}
            }
            else if (RSSIcheck3 == 373)
            {
                if (RSSIcheck4 == 394) { Rem = 39;}
                else if (RSSIcheck5 == 395) { Rem = 39;}
                else if (RSSIcheck6 == 396) { Rem = 39;}
                else if (RSSIcheck7 == 397) { Rem = 39;}
                else if (RSSIcheck8 == 398) { Rem = 39;}
                else if (RSSIcheck9 == 399) { Rem = 39;}
            }
            else if (RSSIcheck4 == 374)
            {
                if (RSSIcheck5 == 395) { Rem = 39;}
                else if (RSSIcheck6 == 396) { Rem = 39;}
                else if (RSSIcheck7 == 397) { Rem = 39;}
                else if (RSSIcheck8 == 398) { Rem = 39;}
                else if (RSSIcheck9 == 399) { Rem = 39;}
            }
            else if (RSSIcheck5 == 375)
            {
                if (RSSIcheck6 == 396) { Rem = 39;}
                else if (RSSIcheck7 == 397) { Rem = 39;}
                else if (RSSIcheck8 == 398) { Rem = 39;}
                else if (RSSIcheck9 == 399) { Rem = 39;}
            }
            else if (RSSIcheck6 == 376)
            {
                if (RSSIcheck7 == 397) { Rem = 39;}
                else if (RSSIcheck8 == 398) { Rem = 39;}
                else if (RSSIcheck9 == 399) { Rem = 39;}
            }
            else if (RSSIcheck7 == 377)
            {
                if (RSSIcheck8 == 398) { Rem = 39;}
                else if (RSSIcheck9 == 399) { Rem = 39;}
            }
            else if (RSSIcheck8 == 378)
            {
                if (RSSIcheck9 == 399) { Rem = 39;}
            }

            if (RSSIcheck2 == 392)
            {
                if (RSSIcheck3 == 373) { Rem = 37;}
                else if (RSSIcheck4 == 374) { Rem = 37;}
                else if (RSSIcheck5 == 375) { Rem = 37;}
                else if (RSSIcheck6 == 376) { Rem = 37;}
                else if (RSSIcheck7 == 377) { Rem = 37;}
                else if (RSSIcheck8 == 378) { Rem = 37;}
                else if (RSSIcheck9 == 379) { Rem = 37;}
            }
            else if (RSSIcheck3 == 393)
            {
                if (RSSIcheck4 == 374) { Rem = 37;}
                else if (RSSIcheck5 == 375) { Rem = 37;}
                else if (RSSIcheck6 == 376) { Rem = 37;}
                else if (RSSIcheck7 == 377) { Rem = 37;}
                else if (RSSIcheck8 == 378) { Rem = 37;}
                else if (RSSIcheck9 == 379) { Rem = 37;}
            }
            else if (RSSIcheck4 == 394)
            {
                if (RSSIcheck5 == 375) { Rem = 37;}
                else if (RSSIcheck6 == 376) { Rem = 37;}
                else if (RSSIcheck7 == 377) { Rem = 37;}
                else if (RSSIcheck8 == 378) { Rem = 37;}
                else if (RSSIcheck9 == 379) { Rem = 37;}
            }
            else if (RSSIcheck5 == 395)
            {
                if (RSSIcheck6 == 376) { Rem = 37;}
                else if (RSSIcheck7 == 377) { Rem = 37;}
                else if (RSSIcheck8 == 378) { Rem = 37;}
                else if (RSSIcheck9 == 379) { Rem = 37;}
            }
            else if (RSSIcheck6 == 396)
            {
                if (RSSIcheck7 == 377) { Rem = 37;}
                else if (RSSIcheck8 == 378) { Rem = 37;}
                else if (RSSIcheck9 == 379) { Rem = 37;}
            }
            else if (RSSIcheck7 == 397)
            {
                if (RSSIcheck8 == 378) { Rem = 37;}
                else if (RSSIcheck9 == 379) { Rem = 37;}
            }
            else if (RSSIcheck8 == 398)
            {
                if (RSSIcheck9 == 379) { Rem = 37;}
            }
        }
        //39
        if (RSSIcheck1 == 391)
        {
            if (RSSIcheck2 == 372) { Rem = 37;}
            else if (RSSIcheck3 == 373) { Rem = 37;}
            else if (RSSIcheck4 == 374) { Rem = 37;}
            else if (RSSIcheck5 == 375) { Rem = 37;}
            else if (RSSIcheck6 == 376) { Rem = 37;}
            else if (RSSIcheck7 == 377) { Rem = 37;}
            else if (RSSIcheck8 == 378) { Rem = 37;}
            else if (RSSIcheck9 == 379) { Rem = 37;}
        }
        //20230515 アンテナ4追加 孤爪
        //96
        if (RSSIcheck1 == 961)
        {
            if (RSSIcheck2 == 982) { Rem = 98;}
            else if (RSSIcheck3 == 983) { Rem = 98;}
            else if (RSSIcheck4 == 984) { Rem = 98;}
            else if (RSSIcheck5 == 985) { Rem = 98;}
            else if (RSSIcheck6 == 986) { Rem = 98;}
            else if (RSSIcheck7 == 987) { Rem = 98;}
            else if (RSSIcheck8 == 988) { Rem = 98;}
            else if (RSSIcheck9 == 989) { Rem = 98;}
        }

        //97
        if (RSSIcheck1 == 971)
        {
            if (RSSIcheck2 == 962)
            {
                if (RSSIcheck3 == 983) { Rem = 98;}
                else if (RSSIcheck4 == 984) { Rem = 98;}
                else if (RSSIcheck5 == 985) { Rem = 98;}
                else if (RSSIcheck6 == 986) { Rem = 98;}
                else if (RSSIcheck7 == 987) { Rem = 98;}
                else if (RSSIcheck8 == 988) { Rem = 98;}
                else if (RSSIcheck9 == 989) { Rem = 98;}
            }
            else if (RSSIcheck3 == 963)
            {
                if (RSSIcheck4 == 984) { Rem = 98;}
                else if (RSSIcheck5 == 985) { Rem = 98;}
                else if (RSSIcheck6 == 986) { Rem = 98;}
                else if (RSSIcheck7 == 987) { Rem = 98;}
                else if (RSSIcheck8 == 988) { Rem = 98;}
                else if (RSSIcheck9 == 989) { Rem = 98;}
            }
            else if (RSSIcheck4 == 964)
            {
                if (RSSIcheck5 == 985) { Rem = 98;}
                else if (RSSIcheck6 == 986) { Rem = 98;}
                else if (RSSIcheck7 == 987) { Rem = 98;}
                else if (RSSIcheck8 == 988) { Rem = 98;}
                else if (RSSIcheck9 == 989) { Rem = 98;}
            }
            else if (RSSIcheck5 == 965)
            {
                if (RSSIcheck6 == 986) { Rem = 98;}
                else if (RSSIcheck7 == 987) { Rem = 98;}
                else if (RSSIcheck8 == 988) { Rem = 98;}
                else if (RSSIcheck9 == 989) { Rem = 98;}
            }
            else if (RSSIcheck6 == 966)
            {
                if (RSSIcheck7 == 987) { Rem = 98;}
                else if (RSSIcheck8 == 988) { Rem = 98;}
                else if (RSSIcheck9 == 989) { Rem = 98;}
            }
            else if (RSSIcheck7 == 967)
            {
                if (RSSIcheck8 == 988) { Rem = 98;}
                else if (RSSIcheck9 == 989) { Rem = 98;}
            }
            else if (RSSIcheck8 == 968)
            {
                if (RSSIcheck9 == 989) { Rem = 98;}
            }

            if (RSSIcheck2 == 982)
            {
                if (RSSIcheck3 == 963) { Rem = 96;}
                else if (RSSIcheck4 == 964) { Rem = 96;}
                else if (RSSIcheck5 == 965) { Rem = 96;}
                else if (RSSIcheck6 == 966) { Rem = 96;}
                else if (RSSIcheck7 == 967) { Rem = 96;}
                else if (RSSIcheck8 == 968) { Rem = 96;}
                else if (RSSIcheck9 == 969) { Rem = 96;}
            }
            else if (RSSIcheck3 == 983)
            {
                if (RSSIcheck4 == 964) { Rem = 96;}
                else if (RSSIcheck5 == 965) { Rem = 96;}
                else if (RSSIcheck6 == 966) { Rem = 96;}
                else if (RSSIcheck7 == 967) { Rem = 96;}
                else if (RSSIcheck8 == 968) { Rem = 96;}
                else if (RSSIcheck9 == 969) { Rem = 96;}
            }
            else if (RSSIcheck4 == 984)
            {
                if (RSSIcheck5 == 965) { Rem = 96;}
                else if (RSSIcheck6 == 966) { Rem = 96;}
                else if (RSSIcheck7 == 967) { Rem = 96;}
                else if (RSSIcheck8 == 968) { Rem = 96;}
                else if (RSSIcheck9 == 969) { Rem = 96;}
            }
            else if (RSSIcheck5 == 985)
            {
                if (RSSIcheck6 == 966) { Rem = 96;}
                else if (RSSIcheck7 == 967) { Rem = 96;}
                else if (RSSIcheck8 == 968) { Rem = 96;}
                else if (RSSIcheck9 == 969) { Rem = 96;}
            }
            else if (RSSIcheck6 == 986)
            {
                if (RSSIcheck7 == 967) { Rem = 96;}
                else if (RSSIcheck8 == 968) { Rem = 96;}
                else if (RSSIcheck9 == 969) { Rem = 96;}
            }
            else if (RSSIcheck7 == 987)
            {
                if (RSSIcheck8 == 968) { Rem = 96;}
                else if (RSSIcheck9 == 969) { Rem = 96;}
            }
            else if (RSSIcheck8 == 988)
            {
                if (RSSIcheck9 == 969) { Rem = 96;}
            }
        }
        //98
        if (RSSIcheck1 == 981)
        {
            if (RSSIcheck2 == 962) { Rem = 96;}
            else if (RSSIcheck3 == 963) { Rem = 96;}
            else if (RSSIcheck4 == 964) { Rem = 96;}
            else if (RSSIcheck5 == 965) { Rem = 96;}
            else if (RSSIcheck6 == 966) { Rem = 96;}
            else if (RSSIcheck7 == 967) { Rem = 96;}
            else if (RSSIcheck8 == 968) { Rem = 96;}
            else if (RSSIcheck9 == 969) { Rem = 96;}
        }
        
        Log.d ("確認Rem",String.valueOf(Rem));
        return Rem;
    }



    //#0023 3面セクター描画
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void Inv_Sector_3(String add_epc_, double add_dis_, Canvas canvas_, Paint paint_, List<Double> est_x_, List<Double> est_y_ ,int judge){
        //Inv_Sector_3(アドレスタグのEPC, 距離, Canvas, Paint, y軸→x軸回転の角度, アドレスタグのx座標（dot）, アドレスタグのy座標（dot）)
        //drawArc(左上x, 左上y, 右下x, 右下y, 開始角度, 移動角度, Paintクラスのインスタンス)
        //judge,if-else文,色変更追加　20230310 孤爪
        double est_x = 0;
        double est_y = 0;
        int add_d = 0;
        int add_x = origin_x;
        int add_y = origin_y;
        int inv_alpha = 255;
        int inv_color = 255;



        //judge = 31; //実験用　孤爪

        if(add_epc_.equals("3000000000000000000000000031")){
            add_d = 225;//y軸→x軸回転の角度
            add_x += (int)(add_1_x*dotm_x);
            add_y -= (int)(add_1_y*dotm_y);
            if (judge != 31) {
                est_x = add_1_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
                est_y = add_1_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
            }
            else {
                paint_.setColor(Color.argb(inv_alpha, inv_color, 0, inv_color));//色変更
            }
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
            if (judge != 33) {
                est_x = add_1_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
                est_y = add_1_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
            }
            else {
                paint_.setColor(Color.argb(inv_alpha, inv_color, 0, inv_color));
            }
        }
        else if(add_epc_.equals("3000000000000000000000000034")){
            add_d = 45;//y軸→x軸回転の角度
            add_x += (int)(add_2_x*dotm_x);
            add_y -= (int)(add_2_y*dotm_y);
            if (judge != 34) {
                est_x = add_2_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
                est_y = add_2_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
            }
            else {
                paint_.setColor(Color.argb(inv_alpha, inv_color, 0, inv_color));
            }
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
            if (judge != 36) {
                est_x = add_2_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
                est_y = add_2_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
            }
            else {
                paint_.setColor(Color.argb(inv_alpha, inv_color, 0, inv_color));
            }
        }
        else if(add_epc_.equals("3000000000000000000000000037")){
            add_d = 45;//y軸→x軸回転の角度
            add_x += (int)(add_3_x*dotm_x);
            add_y -= (int)(add_3_y*dotm_y);
            if (judge != 37) {
                est_x = add_3_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
                est_y = add_3_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
            }
            else {
                paint_.setColor(Color.argb(inv_alpha, inv_color, 0, inv_color));
            }
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
            if (judge != 39) {
                est_x = add_3_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
                est_y = add_3_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
            }
            else {
                paint_.setColor(Color.argb(inv_alpha, inv_color, 0, inv_color));
            }
        }
        //20230515 アンテナ4追加 孤爪
        else if(add_epc_.equals("3000000000000000000000000096")){
            add_d = 225;//y軸→x軸回転の角度
            add_x += (int)(add_4_x*dotm_x);
            add_y -= (int)(add_4_y*dotm_y);
            if (judge != 96) {
                est_x = add_4_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
                est_y = add_4_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
            }
            else {
                paint_.setColor(Color.argb(inv_alpha, inv_color, 0, inv_color));
            }
        }
        else if(add_epc_.equals("3000000000000000000000000097")){
            add_d = 270;//y軸→x軸回転の角度
            add_x += (int)(add_4_x*dotm_x);
            add_y -= (int)(add_4_y*dotm_y);
            est_x = add_4_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
            est_y = add_4_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
        }
        else if(add_epc_.equals("3000000000000000000000000098")){
            add_d = 315;//y軸→x軸回転の角度
            add_x += (int)(add_4_x*dotm_x);
            add_y -= (int)(add_4_y*dotm_y);
            if (judge != 98) {
                est_x = add_4_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
                est_y = add_4_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
            }
            else {
                paint_.setColor(Color.argb(inv_alpha, inv_color, 0, inv_color));
            }
        }

        //20230418 真値　白円描画追加　孤爪

        Paint paint_real = new Paint();;
        paint_real.setColor(Color.argb(255, 255, 255, 255));
        TextView txt_store_x_m_ = (TextView) findViewById(R.id.txt_store_x_m);
        TextView txt_store_y_m_ = (TextView) findViewById(R.id.txt_store_y_m);
        float store_x_m = Float.valueOf(txt_store_x_m_.getText().toString());
        float store_y_m = Float.valueOf(txt_store_y_m_.getText().toString());
        float store_x_pl = (float)3.0;
        float store_y_pl = (float)13.5;
        store_y_m *= -1;

        canvas_.drawCircle(((store_x_m+store_x_pl)*dotm_x)+store_x_pl, ((store_y_m+store_y_pl)*dotm_y), 70, paint_real);


        Log.d("sin", "3");
        String store_x_demo = String.valueOf(store_x_m);
        String store_y_demo = String.valueOf(store_y_m);
        Log.d("sin", store_x_demo);
        Log.d("sin", store_y_demo);

        int add_r_x_ = (int)(add_dis_*dotm_x);
        int add_r_y_ = (int)(add_dis_*dotm_y);
        canvas_.drawArc(add_x - add_r_x_, add_y - add_r_y_, add_x + add_r_x_, add_y + add_r_y_,add_d - 45,90,true, paint_);
        paint_.setColor(Color.argb(inv_alpha, 150, 255, 150));//色戻し

        //重みづけ用の座標設定（m）
        est_x_.add(est_x);
        est_y_.add(est_y);
        Log.d("est座標", add_epc_ + ": " + "(" + (add_x-origin_x)/dotm_x + ", " + (origin_y-add_y)/dotm_y + ")" + add_r_x_ +"(" + est_x + ", " + est_y + ")");

    }
    //#0023fin

    //#0024 4面セクター描画
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void Inv_Sector_4(String add_epc_, double add_dis_, Canvas canvas_, Paint paint_, List<Double> est_x_, List<Double> est_y_){
        //Inv_Sector_4(アドレスタグのEPC, 距離, Canvas, Paint, y軸→x軸回転の角度, アドレスタグのx座標（dot）, アドレスタグのy座標（dot）)
        //drawArc(左上x, 左上y, 右下x, 右下y, 開始角度, 移動角度, Paintクラスのインスタンス)
        double est_x = 0;
        double est_y = 0;
        int add_d = 0;
        int add_x = origin_x;
        int add_y = origin_y;

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
        else if(add_epc_.equals("3000000000000000000000000098")){
            add_d = 270;//y軸→x軸回転の角度
            add_x += (int)(add_3_x*dotm_x);
            add_y -= (int)(add_3_y*dotm_y);
            est_x = add_3_x + (add_dis_ * Math.cos(Math.toRadians(add_d)));
            est_y = add_3_y - (add_dis_ * Math.sin(Math.toRadians(add_d)));
        }
        //20230418 真値　白円描画追加　孤爪

        Paint paint_real = new Paint();;
        paint_real.setColor(Color.argb(255, 255, 255, 255));
        TextView txt_store_x_m_ = (TextView) findViewById(R.id.txt_store_x_m);
        TextView txt_store_y_m_ = (TextView) findViewById(R.id.txt_store_y_m);
        float store_x_m = Float.valueOf(txt_store_x_m_.getText().toString());
        float store_y_m = Float.valueOf(txt_store_y_m_.getText().toString());
        canvas_.drawCircle(store_x_m*dotm_x, store_y_m*dotm_y, 2, paint_real);
        Log.d("sin", "4");

        int add_r_x_ = (int)(add_dis_*dotm_x);
        int add_r_y_ = (int)(add_dis_*dotm_y);
        canvas_.drawArc(add_x - add_r_x_, add_y - add_r_y_, add_x + add_r_x_, add_y + add_r_y_,add_d - 45,90,true, paint_);

        //重みづけ用の座標設定（m）
        est_x_.add(est_x);
        est_y_.add(est_y);
        Log.d("est座標", add_epc_ + ": " + "(" + (add_x-origin_x)/dotm_x + ", " + (origin_y-add_y)/dotm_y + ")" + add_r_x_ +"(" + est_x + ", " + est_y + ")");

    }
    //#0024fin



    //#0025 検索時グラフ描画20220530
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
    //#0025fin

    //#0026 EPCのデコード処理（物品登録）
    private String epcDecode(char c_1, char c_2){
        File file = new File("/data/data/" + this.getPackageName() + "/files/char_allocation.csv");
        FileReader buff = null;
        try {
            buff = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //return ""; //20231130 孤爪　null回避
        }
        String line_ = null;

        BufferedReader fr = new BufferedReader(buff);

        try {
            line_ = fr.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            //return ""; //20231130 孤爪　null回避
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
                //return ""; //20231130 孤爪　null回避
            }
        }
        return "";
    }
    //#0026fin

    //#0027 描画するマップ画像のセット
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
    //#0027


    //#0028 アドレスRFIDタグのEPCを.csvより取得 20221108
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

    //#0028fin

    //#0029 各種設定
    private void SearchListClear(){
        //検索時Spinnerに入れるデータの初期化20221110
        search_goods_name = new ArrayList<>();
        search_goods_epc = new ArrayList<>();
        search_goods_lasttime = new ArrayList<>();
        search_goods_coorx = new ArrayList<>();
        search_goods_coory = new ArrayList<>();
        search_goods_map = new ArrayList<>();
    }

    //double型変数がNaNを出力した時に0を返す 20221213
    private double NanIsZero(double dis_){
        if (!Double.isNaN(dis_) || dis_<0){
            return dis_;
        }
        else{
            return 0.1;
        }
    };

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
    //#0029fin

    //#0030 テキストファイル出力　20230601 孤爪

    //20230802 test孤爪
//20230802 test守田
    //20230802 test住谷
    //#0030fin

}