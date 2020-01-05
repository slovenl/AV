//！！！！！！！！！ 加群23304930下载代码和交流


package xplay.xplay;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Administrator on 2018-03-11.
 */

public class OpenUrl extends Activity {
    private Button btfile;
    private Button btrtmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.openurl );
        btfile = findViewById( R.id.playvideo );
        btfile.setOnClickListener(
                new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       EditText t = findViewById( R.id.fileurl );
                       //用户输入的URL，打开视频
                       Open(t.getText().toString());

                       //关闭当前窗口
                       finish();
                   }
               }
        );


        btrtmp = findViewById( R.id.playrtmp );
        btrtmp.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText t = findViewById( R.id.rtmpurl );
                        //用户输入的URL，打开视频
                        Open(t.getText().toString());

                        //关闭当前窗口
                        finish();
                    }
                }
        );


    }
    public native void Open(String url);

}
