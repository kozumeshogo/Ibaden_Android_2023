package jp.co.tss21.sample.android.inventorytag;


import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class setting extends Activity implements View.OnClickListener {

    private Button mBtnSettingReturn;


    @Override


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        mBtnSettingReturn = (Button) findViewById(R.id.setting_return);
        mBtnSettingReturn.setOnClickListener(this);
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.setting_return: //デバイス検索ボタン
                finish();
        }
    }
}
