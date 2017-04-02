package com.nightn.minofo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private DrawerLayout mDrawerLayout;
    FloatingActionButton begin;
    FloatingActionButton refresh;
    FloatingActionButton fab;

    MapView mapView = null; //地图显示控件
    AMap aMap = null; //地图控制器

    AMapLocationClient mLocationClient = null;
    AMapLocationClientOption mLocationOption = null;
    Marker locationMarker = null;

    UiSettings mUiSettings = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //运行时权限
        List<String> permissionList = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }


        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); //使toolbar具有和ActionBar一样的功能

        // 通过布局id找到DrawerLayout实例
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        // 获取当前actionBar,这里的actionBar是我们之前由toolbar实现的
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            // 让导航栏入口按钮显示出来
            actionBar.setDisplayHomeAsUpEnabled(true);
            // 为导航栏入口按钮设置一个图标
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }


        // 设置导航栏菜单点击事件
        NavigationView navView = (NavigationView)findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(MenuItem item){
                mDrawerLayout.closeDrawers();
                return true;
            }
        });

        // 悬浮按钮注册监听器
        begin = (FloatingActionButton)findViewById(R.id.begin);
        refresh = (FloatingActionButton)findViewById(R.id.refresh);
        fab = (FloatingActionButton)findViewById(R.id.fab);
        begin.setOnClickListener(this);
        refresh.setOnClickListener(this);
        fab.setOnClickListener(this);

        //高德地图
        mapView = (MapView)findViewById(R.id.map_view);
        //在activity执行onCreate时执行mMapView.onCreate(saveInsatenceState)，创建地图
        mapView.onCreate(savedInstanceState);

        //初始化地图控制器对象aMap
        if(aMap == null){
            aMap = mapView.getMap(); //将mapView交给地图控制器管理
        }

        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE); //设置定位模式
        //设置定位点的图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker));
        //定义精度圆样式
        myLocationStyle.strokeColor(0);
        myLocationStyle.radiusFillColor(0);

        //将定位风格设置传给地图控制器
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setMyLocationEnabled(true);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(17)); //设置缩放级别为17
        aMap.showIndoorMap(true); //显示室内地图


        mLocationClient = new AMapLocationClient(getApplicationContext());
        mLocationOption = new AMapLocationClientOption();
        mLocationOption.setOnceLocation(true); //设置为单次定位模式
        mLocationOption.setNeedAddress(true); //返回地址描述
        mLocationOption.setHttpTimeOut(10000); //设置请求超时时间
        mLocationClient.setLocationOption(mLocationOption);

        //设置定位回调监听器
        mLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if(aMapLocation != null){
                    LatLng latLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                    if(locationMarker == null){
                        locationMarker = aMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.center_marker2)));
                    }else{
                        locationMarker.setPosition(latLng);
                    }
                    //将标记移动到定位点，使用animateCamera就有动画效果
                    aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                }else{
                    Toast.makeText(MainActivity.this, "定位失败", Toast.LENGTH_SHORT).show();
                }
            }
        });


        //控件交互
        mUiSettings = aMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(false); //缩放按钮的显示与隐藏
        mUiSettings.setCompassEnabled(false); //指南针的显示与隐藏
        mUiSettings.setScaleControlsEnabled(false); //比例尺的显示与隐藏
        mUiSettings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_LEFT); //设置LOGO位置
        mUiSettings.setRotateGesturesEnabled(false); //禁止旋转


    }


    // 重写「菜单创建时的回调方法」
    public boolean onCreateOptionsMenu(Menu menu){
        // 通过布局文件tool_action.xml创建Menu对象
        getMenuInflater().inflate(R.menu.toolbar_action, menu);
        return true; // 返回true表示允许创建的Menu对象显示
    }

    // 重写「菜单响应事件」
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){  // 获取被选中项的id
            case R.id.notification:
                // 跳出toast提示
                Toast.makeText(this, "活动中心正在建设中", Toast.LENGTH_LONG).show();
                break;
            case android.R.id.home: // 导航栏入口按钮的响应事件
                // 调出导航栏菜单
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
                break;
        }
        return true;
    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.begin:{
                //TODO 开始按钮点击事件
                //启动 UsebikeActivity
                Intent intent = new Intent(MainActivity.this, UsebikeActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.refresh:{
                //TODO 刷新定位按钮点击事件
                mLocationClient.startLocation();
                break;
            }
            case R.id.fab:{
                //举报按钮点击事件
                Toast.makeText(this, "举报功能正在完善", Toast.LENGTH_SHORT).show();
                break;
            }
            default:
                break;
        }
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        mapView.onDestroy();
        mLocationClient.onDestroy();//销毁定位客户端，同时销毁本地定位服务。
    }

    @Override
    protected void onResume(){
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

}
