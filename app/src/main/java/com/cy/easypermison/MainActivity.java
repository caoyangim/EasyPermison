package com.cy.easypermison;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.cy.lib_annotation.annotation.PermissionDenied;
import com.cy.lib_annotation.annotation.PermissionGrant;
import com.cy.lib_annotation.annotation.PermissionRational;
import com.cy.permission.helper.PermissionHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int requestCode = 1;
    private TextView tvMain;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_main);
        tvMain = findViewById(R.id.tv_main);

        PermissionHelper.requestPermission(this, requestCode,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    @PermissionGrant(requestCode)
    public void grant(final String[] grantPermissions) {
        for (final String grantPermission : grantPermissions) {
            Toast.makeText(this, "获取成功" + grantPermission, Toast.LENGTH_SHORT).show();
            initData();
        }
    }

    @PermissionDenied(requestCode)
    public void denied(final String[] deniedPermissions) {
        for (final String deniedPermission : deniedPermissions) {
            Toast.makeText(this, "获取被拒绝" + deniedPermission, Toast.LENGTH_SHORT).show();
        }
    }

    @PermissionRational(requestCode)
    public void rational(final String[] permissions) {
        for (final String deniedPermission : permissions) {
            Toast.makeText(this, "rational" + deniedPermission, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    Geocoder geocoder;
    private List<Address> addressList;

    private void initData() {
        // 获取经纬度坐标
        // 1 获取位置管理者对象
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // 2 通过lm获得经纬调度坐标
        // (参数： provider（定位方式 提供者 通过 LocationManager静态调用），
        // minTime（获取经纬度间隔的最小时间 时时刻刻获得传参数0），
        // minDistance（移动的最小间距 时时刻刻传0），LocationListener（监听）)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                // 获取经纬度主要方法
                final double latitude = location.getLatitude();
                final double longitude = location.getLongitude();
                tvMain.setText("latitude" + latitude + "  " + "longitude" + longitude);
                final StringBuilder sb = new StringBuilder();
                geocoder = new Geocoder(MainActivity.this);
                addressList = new ArrayList<Address>();

                try {
                    // 返回集合对象泛型address
                    addressList = geocoder.getFromLocation(latitude, longitude, 1);


                    if (addressList.size() > 0) {
                        final Address address = addressList.get(0);
                        for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                            sb.append(address.getAddressLine(i)).append("\n");
                        }
                        sb.append(address.getFeatureName());//周边地址
                    }
                    tvMain.setText("当前位置" + sb.toString());
                } catch (final IOException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onStatusChanged(final String s, final int i, final Bundle bundle) {
                //状态发生改变监听
            }

            @Override
            public void onProviderEnabled(final String s) {
                // ProviderEnabled
            }

            @Override
            public void onProviderDisabled(final String s) {
                // ProviderDisabled
            }
        });
    }

}
