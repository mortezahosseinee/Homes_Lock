package com.projects.company.homes_lock.utils.helper;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.ederdoski.simpleble.models.BluetoothLE;
import com.projects.company.homes_lock.models.datamodels.ble.ScannedDeviceModel;
import com.projects.company.homes_lock.ui.device.fragment.addlock.AddLockFragment;
import com.projects.company.homes_lock.utils.ble.CustomBluetoothLEHelper;
import com.projects.company.homes_lock.utils.ble.IBleScanListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.projects.company.homes_lock.utils.helper.DataHelper.REQUEST_CODE_ACCESS_COARSE_LOCATION;

public class BleHelper {

    //region Declare Constants
    public static final UUID SERVICE_UUID_SERIAL = UUID.fromString("927c9cb0-cd09-11e8-b568-0800200c9a66");
    public static final UUID CHARACTERISTIC_UUID_TX = UUID.fromString("927c9cb2-cd09-11e8-b568-0800200c9a66");
    public static final UUID CHARACTERISTIC_UUID_RX = UUID.fromString("927c9cb3-cd09-11e8-b568-0800200c9a66");

    public static final String LOCK_STATUS_LOCK = "lock";
    public static final String LOCK_STATUS_UNLOCK = "unlock";
    public static final String LOCK_STATUS_IDLE = "idle";

    private static final String PREFS_LOCATION_NOT_REQUIRED = "location_not_required";
    private static final String PREFS_PERMISSION_REQUESTED = "permission_requested";

    public static final int FINDING_BLE_DEVICES_SCAN_MODE = 1000;
    public static final int FINDING_BLE_DEVICES_TIMEOUT_MODE = 2000;
    //endregion Declare Constants

    //region Declare Objects
    //endregion Declare Objects

    //region Declare Methods

    /**
     * Checks whether Bluetooth is enabled.
     *
     * @return true if Bluetooth is enabled, false otherwise.
     */
    public static boolean isBleEnabled() {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter != null && adapter.isEnabled();
    }

    /**
     * Checks for required permissions.
     *
     * @return true if permissions are already granted, false otherwise.
     */
    public static boolean isLocationPermissionsGranted(final Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Returns true if location permission has been requested at least twice and
     * user denied it, and checked 'Don't ask again'.
     *
     * @param activity the activity
     * @return true if permission has been denied and the popup will not come up any more, false otherwise
     */
    public static boolean isLocationPermissionDeniedForever(final Activity activity) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);

        return !isLocationPermissionsGranted(activity) // Location permission must be denied
                && preferences.getBoolean(PREFS_PERMISSION_REQUESTED, false) // Permission must have been requested before
                && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION); // This method should return false
    }

    /**
     * On some devices running Android Marshmallow or newer location services must be enabled in order to scan for Bluetooth LE devices.
     * This method returns whether the Location has been enabled or not.
     *
     * @return true on Android 6.0+ if location mode is different than LOCATION_MODE_OFF. It always returns true on Android versions prior to Marshmallow.
     */
    public static boolean isLocationEnabled(final Context context) {
        if (isMarshmallowOrAbove()) {
            int locationMode = Settings.Secure.LOCATION_MODE_OFF;
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (final Settings.SettingNotFoundException e) {
                // do nothing
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        }

        return true;
    }

    /**
     * Location enabled is required on some phones running Android Marshmallow or newer (for example on Nexus and Pixel devices).
     *
     * @param context the context
     * @return false if it is known that location is not required, true otherwise
     */
    public static boolean isLocationRequired(final Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREFS_LOCATION_NOT_REQUIRED, isMarshmallowOrAbove());
    }

    /**
     * The first time an app requests a permission there is no 'Don't ask again' checkbox and
     * {@link ActivityCompat#shouldShowRequestPermissionRationale(Activity, String)} returns false.
     * This situation is similar to a permission being denied forever, so to distinguish both cases
     * a flag needs to be saved.
     *
     * @param context the context
     */
    private static void markLocationPermissionRequested(final Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(PREFS_PERMISSION_REQUESTED, true).apply();
    }

    private static boolean isMarshmallowOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    static void enableLocation(Activity context) {
        final Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivity(intent);
    }

    public static void enableBluetooth(Activity context) {
        final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        context.startActivity(enableIntent);
    }

    public static void grantLocationPermission(Activity context) {
        BleHelper.markLocationPermissionRequested(context);
        ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_ACCESS_COARSE_LOCATION);
    }

    public static void handlePermissionSettings(Activity context) {
        final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        context.startActivity(intent);
    }

    public static byte[] createCommand(byte[] functionId, byte[] data) {
        byte[] fullCommand = new byte[data.length + 2];

        fullCommand = mergeArrays(functionId, data);
        fullCommand = mergeArrays(fullCommand, new byte[]{0x00});

        return fullCommand;
    }

    private static byte[] mergeArrays(byte[] firstArray, byte[] secondArray) {
        byte[] finalArray = new byte[firstArray.length + secondArray.length];
        System.arraycopy(firstArray, 0, finalArray, 0, firstArray.length);
        System.arraycopy(secondArray, 0, finalArray, firstArray.length, secondArray.length);

        return finalArray;
    }

    private byte createChecksum(byte[] commandArray) {
//            byte checksum = 0x00;
//
//            if(commandArray.length == 0)
//                return 0x00;
//
//            for(byte i=0; i<commandArray.length; i++) {
//                checksum = checksum + (commandArray[i] );
//            }
//
//            return(0xff - checksum);
//        }

        return 0x00;
    }

    public static void findDevices(Fragment fragment) {
        if (((AddLockFragment) fragment).mBluetoothLEHelper != null && !((AddLockFragment) fragment).mBluetoothLEHelper.isScanning()) {
            ((AddLockFragment) fragment).mBluetoothLEHelper.setScanPeriod(1000);
            ((AddLockFragment) fragment).mBluetoothLEHelper.scanLeDevice(true);

            Handler mHandler = new Handler();
            mHandler.postDelayed(() -> {
                List<ScannedDeviceModel> tempList = getListOfScannedDevices(((AddLockFragment) fragment).mBluetoothLEHelper);
                if (tempList.size() == 0)
                    ((IBleScanListener<Object>) fragment).onFindBleFault();
                else
                    ((IBleScanListener<Object>) fragment).onFindBleSuccess(tempList);
            }, ((AddLockFragment) fragment).mBluetoothLEHelper.getScanPeriod());
        }
    }

    private static List<ScannedDeviceModel> getListOfScannedDevices(CustomBluetoothLEHelper mBluetoothLEHelper) {
        List<ScannedDeviceModel> mScannedDeviceModelList = new ArrayList<>();

        for (BluetoothLE device : mBluetoothLEHelper.getListDevices())
            mScannedDeviceModelList.add(new ScannedDeviceModel(device));

        return mScannedDeviceModelList;
    }

    public static boolean getScanPermission(Fragment fragment) {
        if (BleHelper.isLocationRequired(fragment.getContext())) {
            if (BleHelper.isLocationPermissionsGranted(fragment.getContext())) {
                if (!BleHelper.isLocationEnabled(fragment.getContext()))
                    DialogHelper.handleEnableLocationDialog(fragment.getActivity());
                else {
                    if (BleHelper.isBleEnabled()) {
                        return true;
                    } else BleHelper.enableBluetooth(fragment.getActivity());
                }
            } else {
                final boolean deniedForever = BleHelper.isLocationPermissionDeniedForever(fragment.getActivity());
                if (!deniedForever)
                    BleHelper.grantLocationPermission(fragment.getActivity());

                if (deniedForever)
                    BleHelper.handlePermissionSettings(fragment.getActivity());
            }
        } else {
            if (BleHelper.isBleEnabled())
                return true;
            else BleHelper.enableBluetooth(fragment.getActivity());
        }

        return false;
    }
    //endregion Declare Methods
}
