package com.cy.permission.helper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class PermissionHelper {

    private final static String SUFFIX = "$$PermissionProxy";

    public static void requestPermission(final Activity activity, final int requestCode, final String... permission) {
        doRequestPermission(activity, permission, requestCode);
    }

    public static void requestPermission(final Fragment fragment, final int requestCode, final String... permission) {
        doRequestPermission(fragment.getActivity(), permission, requestCode);
    }

    private static void doRequestPermission(final Activity activity, final String[] permission, final int requestCode) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            doExecuteGrant(activity, requestCode, permission);
            return;
        }
        final boolean rational = shouldShowPermissionRational(activity, requestCode, permission);
        if (rational) {
            return;
        }
        _doRequestPermission(activity, permission, requestCode);
    }

    private static void _doRequestPermission(final Activity activity, final String[] permission, final int requestCode) {

        ActivityCompat.requestPermissions(activity, permission, requestCode);
    }

    private static boolean shouldShowPermissionRational(final Activity activity, final int requestCode, final String[] permission) {
        final PermissionProxy proxy = findProxy(activity);
        final List<String> deniedPermissions = findDeniedPermissions(activity, permission);
        if (!deniedPermissions.isEmpty()) {
            final String[] denied = new String[deniedPermissions.size()];
            deniedPermissions.toArray(denied);
            return proxy.rational(requestCode, activity, denied, new PermissionRationCallback() {
                @Override
                public void onRationalExecute() {
                    doRequestPermission(activity, permission, requestCode);
                }
            });
        }
        return false;
    }

    private static List<String> findDeniedPermissions(final Activity activity, final String[] permissions) {
        final List<String> denieds = new ArrayList<>();
        for (final String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                denieds.add(permission);
            }
        }
        return denieds;
    }

    private static void doExecuteGrant(final Activity activity, final int requestCode, final String[] permission) {
        final PermissionProxy proxy = findProxy(activity);
        proxy.grant(requestCode, activity, permission);
    }

    private static void doExecuteDenied(final Activity activity, final int requestCode, final String[] permission) {
        final PermissionProxy proxy = findProxy(activity);
        proxy.denied(requestCode, activity, permission);
    }

    private static PermissionProxy findProxy(final Activity activity) {
        final Class<? extends Activity> aClass = activity.getClass();
        try {
            final Class<?> forName = Class.forName(aClass.getName() + SUFFIX);
            try {
                return (PermissionProxy) forName.newInstance();
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            } catch (final InstantiationException e) {
                e.printStackTrace();
            }
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("找不到生成的类");
    }

    public static void onRequestPermissionsResult(final Activity activity, final int requestCode, final String[] permissions, final int[] grantResults) {
        final List<String> grant = new ArrayList<>();
        final List<String> denied = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                grant.add(permissions[i]);
            } else {
                denied.add(permissions[i]);
            }
        }

        if (!grant.isEmpty()) {
            final String[] grants = new String[grant.size()];
            grant.toArray(grants);
            doExecuteGrant(activity, requestCode, grants);
        }
        if (!denied.isEmpty()) {
            final String[] denieds = new String[denied.size()];
            denied.toArray(denieds);
            doExecuteDenied(activity, requestCode, denieds);
        }
    }
}
