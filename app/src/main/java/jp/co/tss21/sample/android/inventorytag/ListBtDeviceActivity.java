package jp.co.tss21.sample.android.inventorytag;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * デバイス選択
 *
 * @author Tohoku Systems Support.Co.,Ltd.
 */
public class ListBtDeviceActivity extends Activity {
    private final static String TAG = ListBtDeviceActivity.class.getSimpleName();
    private static final String HQ_UHF_READER = "HQ_UHF_READER";
    private static final String DOTR910_PREFIX_TSS = "TSS91JJ-";
    private static final String DOTR920_PREFIX_TSS = "TSS92JJ-";
    private static final String DOTR2100_PREFIX = "DOTR2100-";
    private static final String DOTR2200_PREFIX = "DOTR2200-";
    private static final String DOTR2100_PREFIX_TSS = "TSS2100";
    private static final String DOTR2200_PREFIX_TSS = "TSS2200";
    public static final String DOTR3100_PREFIX = "DOTR3100";
    public static final String DOTR3200_PREFIX = "DOTR3200";
    private static final String DOTR3100_PREFIX_TSS = "TSS3100";
    private static final String DOTR3200_PREFIX_TSS = "TSS3200";
    private static final String SR7_PREFIX = "SR7_";
    private static final String DOTR900I_PREFIX_TSS = "BLE SPP";    //出荷時
    private static final String DOTR910I_PREFIX_TSS = "TSS91JI-";    //出荷時
    private static final String DOTR920I_PREFIX_TSS = "TSS92JI-";    //出荷時


    private ListView mListDevice;                                //デバイスリスト
    private ArrayList<HashMap<String, String>> mArrBtDevice;
    private BaseAdapter mAdapterDevice;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private ProgressDialog progress;

    public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String DEVICE_NAME = "DEVICE_NAME";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_bt_device);

        mListDevice = (ListView) findViewById(R.id.list_btdevice);
        initBluetoothDeviceList(R.id.list_btdevice);

        IntentFilter filter = new IntentFilter();
        //端末の検出を受信する
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        if (!mBluetoothAdapter.isEnabled()) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }

        //Android M以降でBluetooth検知・接続するため
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1){
            final String[] lPermissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
            int permissionCheckLocation = ContextCompat.checkSelfPermission(ListBtDeviceActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permissionCheckLocation == PackageManager.PERMISSION_GRANTED) {
                resetBtDeviceList();
                mBluetoothAdapter.startDiscovery();
            } else if(permissionCheckLocation==PackageManager.PERMISSION_DENIED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    // パーミッション確認ダイアログ
                    new AlertDialog.Builder(this)
                            .setTitle(getResources().getString(R.string.alertDialog_title_permission))
                            .setMessage(getResources().getString(R.string.alertDialog_message_permission_location))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(ListBtDeviceActivity.this, lPermissions, REQ_CODE_ACCESS_COARSE_LOCATION);
                                }
                            })
                            .create()
                            .show();
                } else{
                    ActivityCompat.requestPermissions(ListBtDeviceActivity.this, lPermissions, REQ_CODE_ACCESS_COARSE_LOCATION);
                }
            }
        } else {
            resetBtDeviceList();
            mBluetoothAdapter.startDiscovery();
        }


        //プログレスバーを表示する
        progress = ProgressDialog.show(this, "", "デバイス検索中...", false, true, new OnCancelListener() {
            public void onCancel(DialogInterface arg0) {
                mBluetoothAdapter.cancelDiscovery();
            }
        });
    }


    private void resetBtDeviceList() {
        mArrBtDevice.clear();
        mAdapterDevice.notifyDataSetChanged();
    }


    private boolean initBluetoothDeviceList(int id) {
        mListDevice = (ListView) findViewById(id);
        if (mListDevice != null) {
            mArrBtDevice = new ArrayList<>();
            mAdapterDevice = new SimpleAdapter(this, mArrBtDevice,
                    android.R.layout.simple_list_item_2,
                    new String[]{"name", "address"},
                    new int[]{android.R.id.text1, android.R.id.text2});
            mListDevice.setAdapter(mAdapterDevice);
            mListDevice.setOnItemClickListener(mDeviceClickListener);
        }

        return mListDevice != null;
    }


    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering())
                mBluetoothAdapter.cancelDiscovery();

            HashMap<String, String> map = mArrBtDevice.get(arg2);
            if (map == null)
                return;

            if (map.get("address") == null)
                return;

			/* ----------------------------------------
             * 呼び出し元に結果を返す。
			 * ---------------------------------------- */
            Intent i = new Intent();
            i.putExtra(DEVICE_ADDRESS, map.get("address"));
            i.putExtra(DEVICE_NAME, map.get("name"));
            setResult(RESULT_OK, i);
            finish();
        }
    };


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        mBluetoothAdapter.cancelDiscovery();
        setResult(RESULT_CANCELED);
    }


    private boolean mUnregister = false;

    /* (非 Javadoc)
     * @see android.app.Activity#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            super.finalize();
        } finally {
            unregisterReceiver(mReceiver);
        }
    }

    private static final int REQ_CODE_ACCESS_COARSE_LOCATION = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQ_CODE_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    resetBtDeviceList();
                    mBluetoothAdapter.startDiscovery();
                } else {
                    Log.d(TAG, "このアプリの利用には低精度の位置情報へのアクセス許可が必要です");
                }
                break;
            default:
                break;
        }
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.d(TAG, "ACTION_FOUND[" + device.getName() + "][" + device.getAddress() + "]");

                String deviceName = device.getName();
                if (deviceName == null || deviceName.equals(""))
                    return;

                if (!HQ_UHF_READER.equalsIgnoreCase(deviceName) && !deviceName.startsWith(DOTR910_PREFIX_TSS) && !deviceName.startsWith(DOTR920_PREFIX_TSS)
                        && !deviceName.startsWith(DOTR2100_PREFIX_TSS) && !deviceName.startsWith(DOTR2200_PREFIX_TSS)
                        && !deviceName.startsWith(DOTR2100_PREFIX) && !deviceName.startsWith(DOTR2200_PREFIX)
                        && !deviceName.startsWith(DOTR3100_PREFIX_TSS) && !deviceName.startsWith(DOTR3200_PREFIX_TSS)
                        && !deviceName.startsWith(DOTR3100_PREFIX) && !deviceName.startsWith(DOTR3200_PREFIX)
                        && !deviceName.startsWith(SR7_PREFIX)
                        && !deviceName.startsWith(DOTR900I_PREFIX_TSS) && !deviceName.startsWith(DOTR910I_PREFIX_TSS) && !deviceName.startsWith(DOTR920I_PREFIX_TSS)
                        )
                    return;

                HashMap<String, String> item = new HashMap<>();
                item.put("name", device.getName());
                item.put("address", device.getAddress());

                if (mArrBtDevice.contains(item))
                    return;

                mArrBtDevice.add(item);
                mAdapterDevice.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "ACTION_DISCOVERY_FINISHED");

                if (mArrBtDevice.size() == 0) {
                    HashMap<String, String> item = new HashMap<>();
                    item.put("name", "見つかりませんでした。");
                    item.put("address", "");
                    mArrBtDevice.add(item);
                    mAdapterDevice.notifyDataSetChanged();
                }

                //UIスレッドで表示しているダイアログを消す
                progress.dismiss();
            }
        }
    };
}
