package com.projects.company.homes_lock.utils.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.projects.company.homes_lock.models.datamodels.ble.ScannedDeviceModel;
import com.projects.company.homes_lock.models.datamodels.response.FailureModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.projects.company.homes_lock.utils.helper.DataHelper.ERROR_CODE_BLE_DEVICE_NOT_SUPPORT_BLE;
import static com.projects.company.homes_lock.utils.helper.DataHelper.ERROR_CODE_BLE_NOT_ENABLED;
import static com.projects.company.homes_lock.utils.helper.DataHelper.containsValueInListOfObjects;

/**
 * This class retrieves the current visible BLE Devices.
 */
public class BleScanner {

    //region Declare Constants
    //endregion Declare Constants

    //region Declare Variables
    //endregion Declare Variables

    //region Declare Objects
    private Context context;
    private BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver mBroadcastReceiver;
    private IBleScanListener mBleScanListener;
    private List<ScannedDeviceModel> mScannedDeviceModelList;
    //endregion Declare Objects

    public BleScanner(Context context, final IBleScanListener mBleScanListener) {

        //region Initialize Variables
        //endregion Initialize Variables

        //region Initialize Objects
        this.context = context.getApplicationContext();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mBleScanListener = mBleScanListener;
        mScannedDeviceModelList = new ArrayList<>();

        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

//                    final int rssiPercent = (int) (100.0f * (127.0f + device.get()) / (127.0f + 20.0f));
//                    holder.rssi.setImageLevel(rssiPercent);

//                    if (!containsValueInListOfObjects(mScannedDeviceModelList, device.getAddress()))
//                        mScannedDeviceModelList.add(new ScannedDeviceModel(device, device.getName(), device.getAddress()));

                    mBleScanListener.onFindBleCompleted(mScannedDeviceModelList);
                }
            }
        };
        //endregion Initialize Objects

        this.mBleScanListener.setReceiver(mBroadcastReceiver);
        findBleNetworks();
    }

    //region Declare Methods
    private void findBleNetworks() {
        if (mBluetoothAdapter == null)
            mBleScanListener.onFindBleFault(new FailureModel("Device does not support BLE", ERROR_CODE_BLE_DEVICE_NOT_SUPPORT_BLE));
        else if (!mBluetoothAdapter.isEnabled())
            mBleScanListener.onFindBleFault(new FailureModel("BLE is not enable", ERROR_CODE_BLE_NOT_ENABLED));
        else
            scanBleNetworks();
    }

    private void scanBleNetworks() {
        mBluetoothAdapter.startDiscovery();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(mBroadcastReceiver, filter);
    }

    public int getPairedDevicesCount() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        return pairedDevices.size();
    }
    //endregion Declare Methods
}