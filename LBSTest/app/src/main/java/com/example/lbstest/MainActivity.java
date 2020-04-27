package com.example.lbstest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.DistanceUtil;


import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class MainActivity extends AppCompatActivity {
    public LocationClient mLocationClient;
    private TextView positionText;
    private MapView mapView;
    private BaiduMap baiduMap;
    private boolean isFirstLocate = true;
    private static GeoCoder mCoder;
    Button  bt1, bt2;
    GeoCoder mGeoCoder;
    double jingdu1,weidu1,jingdu2,weidu2;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bmob.initialize(this, "0a972706fe2b439171f0c531575b2223");
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.bmapView);
        chaxun();
        fabu();
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        positionText = findViewById(R.id.position_text_view);
        List<String> permissionList = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else{
            requestLocation();
        }

    }




    private void fabu() {
        bt2= (Button) findViewById(R.id.fabu);

        bt2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Order order = new Order();
                order.setLatitude1(jingdu1);
                order.setLongitude1(weidu1);
                order.setLatitude2(jingdu2);
                order.setLongitude2(weidu2);
                //根据经纬度测距离
                LatLng start = new LatLng(jingdu1, weidu1);
                LatLng end = new LatLng(jingdu2, weidu2);
                double distanse = DistanceUtil.getDistance(start,end);
                order.setDistanse(distanse);
                //经纬度坐标的传入
                order.save(new SaveListener<String>() {
                    @Override
                    public void done(String objectId, BmobException e) {
                        if (e == null) {
                            Toast.makeText(getApplication(), "发布订单成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplication(), "发布失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }

    private void chaxun() {

        // 输入框
        final EditText address= (EditText) findViewById(R.id.address1);
        final EditText city= (EditText) findViewById(R.id.city1);
        // 按钮
        bt1= (Button) findViewById(R.id.cha);
        // 按钮添加监听
        bt1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {// 实现点击事件
                // 通过GeoCoder的实例方法得到GerCoder对象
                mGeoCoder = GeoCoder.newInstance();
                // 得到GenCodeOption对象
                GeoCodeOption mGeoCodeOption = new GeoCodeOption();
                // 得到输入框的内容赋值
                mGeoCodeOption.address(address.getText().toString());
                mGeoCodeOption.city(city.getText().toString());
                // 为GeoCoder设置监听事件
                mGeoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
                    @Override
                    public void onGetReverseGeoCodeResult(
                            ReverseGeoCodeResult arg0) {

                    }

                    // 地址转化经纬度
                    @Override
                    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                        if (geoCodeResult == null
                                || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                            Toast.makeText(MainActivity.this, "未查询到改地点",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                          jingdu2 = geoCodeResult.getLocation().latitude;
                            weidu2= geoCodeResult.getLocation().longitude;      //全局变量赋值
                            Toast.makeText(MainActivity.this, "你的目的地"+"经度为："+jingdu2+"纬度为："+ weidu2,
                                    Toast.LENGTH_SHORT).show();
                            //根据经纬度在地图上标注
                            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);
                            LatLng point = new LatLng( jingdu2, weidu2);
                            OverlayOptions option = new MarkerOptions().position(point).icon(descriptor);
                            baiduMap.addOverlay(option);
                        }
                    }
                });
                mGeoCoder.geocode(mGeoCodeOption);

            }
        });
    }


    private void navigateTo(BDLocation location){
        if(isFirstLocate){
            LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        baiduMap.setMyLocationData(locationData);
    }

    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    private void initLocation() {
        LocationClientOption option  = new LocationClientOption();
        option.setScanSpan(5000);
        option.setIsNeedAddress(true);
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        mLocationClient.setLocOption(option);
    }

    protected void onResume(){
        super.onResume();
        mapView.onResume();
    }

    protected void onPause(){
        super.onPause();
        mapView.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

    public void onResquestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length>0){
                    for(int result : grantResults){
                        if(result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意所有权限才能使用本程序",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else{
                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    public class MyLocationListener implements BDLocationListener{

        public void onReceiveLocation(final BDLocation location) {
            if(location.getLocType() == BDLocation.TypeGpsLocation ||  location.getLocType() == BDLocation.TypeNetWorkLocation){
                navigateTo(location);
            }
  runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuilder currentPosition = new StringBuilder();
                    currentPosition.append("纬度：").append(location.getLatitude()).append("\n");
                   currentPosition.append("经度：").append(location.getLongitude()).append("\n");
                    currentPosition.append("国家：").append(location.getCountry()).append("\n");
                    currentPosition.append("省：").append(location.getProvince()).append("\n");
                    currentPosition.append("市：").append(location.getCity()).append("\n");
                    currentPosition.append("区：").append(location.getDistrict()).append("\n");
                    currentPosition.append("街道：").append(location.getStreet()).append("\n");
                    currentPosition.append("定位方式：");
                    jingdu1 = location.getLatitude();
                     weidu1= location.getLongitude();
                    Toast.makeText(MainActivity.this, "你现在的位置："+"经度："+jingdu1+"纬度"+ weidu1,
                            Toast.LENGTH_SHORT).show();
//                    String city=location.getCity();
//                    String district=location.getDistrict();
//                    String street=location.getStreet();
//                    Order order = new Order();
//
//                    order.setLatitude1(latitude);
//                    order.setLongitude1(longitude);
//                    order.setCity(city);
//                    order.setDistrict(district);
//                    order.setStreet(street);
//                    order.save(new SaveListener<String>() {
//                        @Override
//                        public void done(String objectId, BmobException e) {
//                            if (e == null) {
//                                Toast.makeText(getApplication(), "添加数据成功" ,Toast.LENGTH_SHORT).show();
//                            } else {
//                                Toast.makeText(getApplication(), "失败" +e.getMessage(),Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
                    positionText.setText(currentPosition);
                }

            });
        }
        public void onConnectHotSpotMessage(String s, int i){

        }

    }


}
