package temple.tsinghuamap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import android.content.Intent;
import android.widget.RelativeLayout;
import android.widget.TextView;

import temple.core.*;

public class MainActivity extends Activity {
    MapView mMapView;
    RelativeLayout infoBar;
    IMapController mController;
    OSM mapinfo;
    PathOverlay line;
    MyLocationNewOverlay mLocationOverlay;
    final GeoPoint center = new GeoPoint(40.0027467,116.3202408);
    final LinkedList<Sight> sights = new LinkedList<Sight>(){{
        add(new Sight("二校门",39.9997127,116.3182903,"http://wapbaike.baidu.com/view/5620934.htm"));
        add(new Sight("清华学堂",40.0009009, 116.3189578,"http://wapbaike.baidu.com/view/442036.htm"));
        add(new Sight("大礼堂",40.0022154, 116.3183353,"http://wapbaike.baidu.com/view/4666667.htm"));
        add(new Sight("图书馆",40.0037275, 116.3180391,"http://wapbaike.baidu.com/view/121209.htm"));
        add(new Sight("水木清华",40.0019716,116.3167408,"http://wapbaike.baidu.com/subview/9942/4992826.htm"));
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        infoBar = (RelativeLayout) findViewById(R.id.infoBar);
        infoBar.setVisibility(View.GONE);
        mMapView = (MapView) findViewById(R.id.myOSMmapview);
        mController = mMapView.getController();
        //ResourceProxy init
        mMapView.setTileSource(TileSourceFactory.MAPNIK);
        mMapView.setBuiltInZoomControls(false);
        mMapView.setMultiTouchControls(true);
        //定位当前位置，清华大学

        InputStream stream1=getResources().openRawResource(R.raw.tsinghua);
        InputStream stream2=getResources().openRawResource(R.raw.tsinghua);
        mapinfo = new temple.core.OSM(stream1,stream2);

        LinkedList<Way> busline = mapinfo.getBusLine();
        for(Way b:busline) {
            line = new PathOverlay(Color.RED, this);
            LinkedList<Node> busway = b.way;
            Node n1 = busway.get(0);
            for (int i = 1; i < busway.size(); i++) {
                Node n2 = busway.get(i);
                GeoPoint gp1 = new GeoPoint(n1.lat, n1.lon);
                GeoPoint gp2 = new GeoPoint(n2.lat, n2.lon);
                line.addPoint(gp1);
                line.addPoint(gp2);
                n1 = n2;
            }
            mMapView.getOverlays().add(line);
        }
        line = null;


        Drawable dr = getResources().getDrawable(android.R.drawable.star_big_on);
        int markerWidth = dr.getIntrinsicWidth()/2;
        int markerHeight =dr.getIntrinsicHeight()/2;
        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
        Drawable marker = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, markerHeight, markerWidth, true));
        marker.setBounds(0, markerHeight, markerWidth, 0);
        for(int i = 0; i < sights.size(); i++) {
            ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
            Sight sight = sights.get(i);
            GeoPoint sitePoint = new GeoPoint(sight.lat, sight.lon);
            OverlayItem item = new OverlayItem(String.valueOf(i),sight.name,"",sitePoint);
            item.setMarker(marker);
            items.add(item);
            ResourceProxy mResourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
            ItemizedOverlayWithFocus<OverlayItem> mLocationOverlay = new ItemizedOverlayWithFocus<OverlayItem>(
                    items,
                    new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                        @Override
                        public boolean onItemSingleTapUp(final int index,
                                                         final OverlayItem item) {
                            Intent i = new Intent("temple.SiteInfo");
                            i.putExtra("sitename", item.getTitle());
                            int uid=Integer.parseInt(item.getUid());
                            i.putExtra("sitelink",sights.get(uid).link);
                            startActivity(i);
                            return true;
                        }

                        @Override
                        public boolean onItemLongPress(final int index,
                                                       final OverlayItem item) {
                            return false;
                        }
                    }, mResourceProxy);
            mLocationOverlay.setFocusItemsOnTap(true);
            mMapView.getOverlays().add(mLocationOverlay);
        }

        mController.setZoom(16);
        ViewTreeObserver vto = mMapView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mController.setCenter(center);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    mMapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                else{
                    mMapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });


        mLocationOverlay = new MyLocationNewOverlay(this, new GpsMyLocationProvider(this),
                mMapView);
        mMapView.getOverlays().add(mLocationOverlay);
        mLocationOverlay.enableMyLocation();
    }

    public void onClickSearch(View view){
        EditText edittext = (EditText) findViewById(R.id.editText);
        String startPoint = edittext.getText().toString();
        EditText edittext2 = (EditText) findViewById(R.id.editText2);
        String targetPoint = edittext2.getText().toString();

        mMapView.getOverlays().remove(line);
        infoBar.setVisibility(View.GONE);

        GeoPoint position = mLocationOverlay.getMyLocation();
        if(position==null) {
            position = center;
        }
        Node nowNode = new Node((long) 0, position.getLatitude(), position.getLongitude());
        OSMResult searchResult = mapinfo.FindRoute(startPoint,targetPoint, 1,nowNode);
        TextView info = (TextView) findViewById(R.id.infoText);
        if(searchResult.Check[0] && searchResult.Check[1] && searchResult.result.route!=null) {
            Cost c = searchResult.result.c;
            String s = String.format("%.1f",c.dis)+"公里"+" （";
            int h =(int) Math.floor(c.time);
            if(h!=0) {
                s = s + String.valueOf(h) + "小时";
            }
            int m =(int) ((c.time - h) * 60);
            s = s + m + "分钟" + "）";
            info.setText(s);
            LinkedList<Node> route=searchResult.result.route;
            line = new PathOverlay(Color.BLUE, this);
            Paint paint = line.getPaint();
            paint.setColor(Color.argb(205, 44, 177, 211));
            paint.setStrokeWidth(5);
            line.setPaint(paint);
            Node n1 = route.get(0);
            for (int i = 1; i < route.size(); i++) {
                Node n2 = route.get(i);
                GeoPoint gp1 = new GeoPoint(n1.lat, n1.lon);
                GeoPoint gp2 = new GeoPoint(n2.lat, n2.lon);
                line.addPoint(gp1);
                line.addPoint(gp2);
                n1 = n2;
            }
            mMapView.getOverlays().add(line);
        }
        else{
            if(!searchResult.Check[0]){
                if(startPoint.equals("")){
                    info.setText("请输入出发地");
                }
                else{
                    info.setText("未找到" + startPoint);
                }
            }
            else if(!searchResult.Check[1]){
                if(targetPoint.equals("")){
                    info.setText("请输入目的地");
                }
                else{
                    info.setText("未找到" + targetPoint);
                }
            }
            else{
                info.setText("未找到路线");
            }
        }
        mController.setZoom(16);
        mController.setCenter(center);

        View mview = this.getCurrentFocus();
        if (mview != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        mMapView.setFocusable(true);
        mMapView.setFocusableInTouchMode(true);
        mMapView.requestFocus();
        mMapView.requestFocusFromTouch();


        infoBar.setVisibility(View.VISIBLE);
    }

    public void onClickLocate(View view){
        EditText edittext = (EditText) findViewById(R.id.editText);
        GeoPoint position = mLocationOverlay.getMyLocation();
        if(position!=null) {
            edittext.setText("我的位置");
            mController.setZoom(18);
            mController.setCenter(position);
        }
    }

    public  void onClickRefresh(View view){
        EditText edittext = (EditText) findViewById(R.id.editText);
        EditText edittext2 = (EditText) findViewById(R.id.editText2);
        TextView edittext3 = (TextView) findViewById(R.id.infoText);
        edittext.setText("");
        edittext2.setText("");
        edittext3.setText("欢迎使用TsinghuaMap");
        mMapView.getOverlays().remove(line);
        infoBar.setVisibility(View.GONE);
        mController.setZoom(16);
        mController.setCenter(center);
    }
}

class Sight {
    public String name;
    public double lat,lon;
    public String link;
    public Sight(String name, double lat, double lon, String link){
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.link = link;
    }

}