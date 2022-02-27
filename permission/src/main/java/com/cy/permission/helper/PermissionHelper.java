package com.cy.permission.helper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

public class PermissionHelper {

    private final static String SUFFIX = "$$PermissionProxy";
    private static final String HIDE_FRAGMENT_TAG = "permission&$$HideFragment";

    private volatile static PermissionHelper helper;

    private PermissionHelper() {
    }

    public static void requestPermission(final FragmentActivity activity, final int requestCode, final String... permission) {
        doRequestPermission(activity, permission, requestCode);
    }

    /*hide*/
    public static void requestPermission(final Fragment fragment, final int requestCode, final String... permission) {
        doRequestPermission(fragment.getActivity(), permission, requestCode);
    }

    private static void doRequestPermission(final FragmentActivity activity, final String[] permission, final int requestCode) {
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

    private static void _doRequestPermission(final FragmentActivity activity, final String[] permission, final int requestCode) {
        final FragmentManager fm = activity.getSupportFragmentManager();
        get().fragmentGet(fm).requestPermissions(permission, requestCode);
    }

    private static boolean shouldShowPermissionRational(final FragmentActivity activity, final int requestCode, final String[] permission) {
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

    private static void doExecuteGrant(final FragmentActivity activity, final int requestCode, final String[] permission) {
        final PermissionProxy proxy = findProxy(activity);
        proxy.grant(requestCode, activity, permission);
    }

    private static void doExecuteDenied(final FragmentActivity activity, final int requestCode, final String[] permission) {
        final PermissionProxy proxy = findProxy(activity);
        proxy.denied(requestCode, activity, permission);
    }

    private static PermissionProxy findProxy(final FragmentActivity activity) {
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

    private static void onRequestPermissionsResult(final FragmentActivity activity, final int requestCode, final String[] permissions, final int[] grantResults) {
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

    private static PermissionHelper get() {
        if (helper == null) {
            synchronized (PermissionHelper.class) {
                if (helper == null) {
                    helper = new PermissionHelper();
                }
            }
        }
        return helper;
    }

    private HideFragment fragmentGet(FragmentManager fm) {
        HideFragment current = (HideFragment) fm.findFragmentByTag(HIDE_FRAGMENT_TAG);
        if (current == null) {
            current = new HideFragment();
            fm.beginTransaction().add(current, HIDE_FRAGMENT_TAG).commitNow();
        }
        return current;
    }

    public static class HideFragment extends Fragment {

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            PermissionHelper.onRequestPermissionsResult(getActivity(), requestCode, permissions, grantResults);
        }
    }
}
