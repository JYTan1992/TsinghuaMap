package temple.tsinghuamap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;


public class SiteInfo extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_info);
        TextView txt_sitename = (TextView) findViewById(R.id.textView3);
        txt_sitename.setText(getIntent().getStringExtra("sitename"));
        WebView wv = (WebView) findViewById(R.id.webView1);
        WebSettings webSettings = wv.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUserAgentString("Android");
        wv.setWebViewClient(new Callback());
        String url = getIntent().getStringExtra("sitelink");
        wv.loadUrl(url);
    }

    private class Callback extends WebViewClient{
        public boolean shouldOverrideUrlLoading(WebView view,String url){
            return (false);
        }
    }

    public void onClickBack(View view){
        /*
        Intent i = new Intent(this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);*/
        finish();
    }

}
