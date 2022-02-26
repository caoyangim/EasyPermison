[![](https://jitpack.io/v/caoyangim/EasyPermison.svg)](https://jitpack.io/#caoyangim/EasyPermison)
# EasyPermison
一个简易版的权限获取框架

# 引入依赖
``` groovy
implementation 'com.github.caoyangim.EasyPermison:permision:v1.0.0'
annotationProcessor 'com.github.caoyangim.EasyPermison:lib-compiler:v1.0.0'
```

# 使用方法
``` java
@Override
public void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    PermissionHelper.requestPermission(this, requestCode,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION);
}

@PermissionGrant(requestCode)
public void grant(final String[] grantPermissions) {
    for (final String grantPermission : grantPermissions) {
        Toast.makeText(this, "获取成功：" + grantPermission, Toast.LENGTH_SHORT).show();
        initData();
    }
}

@PermissionDenied(requestCode)
public void denied(final String[] deniedPermissions) {
    for (final String deniedPermission : deniedPermissions) {
        Toast.makeText(this, "获取被拒绝：" + deniedPermission, Toast.LENGTH_SHORT).show();
    }
}

@PermissionRational(requestCode)
public void rational(final String[] permissions) {
    for (final String deniedPermission : permissions) {
        Toast.makeText(this, "我想获取该权限：" + deniedPermission, Toast.LENGTH_SHORT).show();
    }
}

@Override
public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    PermissionHelper.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
}
```
