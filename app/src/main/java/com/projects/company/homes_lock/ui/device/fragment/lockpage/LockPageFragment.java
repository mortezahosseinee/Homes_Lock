package com.projects.company.homes_lock.ui.device.fragment.lockpage;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ederdoski.simpleble.models.BluetoothLE;
import com.ederdoski.simpleble.utils.BluetoothLEHelper;
import com.google.gson.Gson;
import com.projects.company.homes_lock.R;
import com.projects.company.homes_lock.database.tables.Device;
import com.projects.company.homes_lock.models.datamodels.ble.ScannedDeviceModel;
import com.projects.company.homes_lock.models.viewmodels.DeviceViewModel;
import com.projects.company.homes_lock.models.viewmodels.DeviceViewModelFactory;
import com.projects.company.homes_lock.utils.ble.IBleScanListener;
import com.projects.company.homes_lock.utils.helper.BleHelper;
import com.projects.company.homes_lock.utils.helper.DataHelper;
import com.projects.company.homes_lock.utils.helper.ViewHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class LockPageFragment extends Fragment
        implements ILockPageFragment, IBleScanListener, View.OnClickListener {

    //region Declare Constants
    private static final String ARG_PARAM = "param";
    //endregion Declare Constants

    //region Declare Views
    ImageView imgMainLockPage;
    ImageView imgBleLockPage;
    //endregion Declare Views

    //region Declare Variables
    //endregion Declare Variables

    //region Declare Objects
    private DeviceViewModel mDeviceViewModel;
    private Device mDevice;
    private BluetoothLEHelper mBluetoothLEHelper;
    //endregion Declare Objects

    public LockPageFragment() {
        // Required empty public constructor
    }

    public static LockPageFragment newInstance(Device device) {
        LockPageFragment fragment = new LockPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM, new Gson().toJson(device));
        fragment.setArguments(args);
        return fragment;
    }

    //region Main CallBacks
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //region Initialize Variables
        //endregion Initialize Variables

        //region Initialize Objects
        this.mDeviceViewModel = ViewModelProviders.of(
                this,
                new DeviceViewModelFactory(getActivity().getApplication(), this))
                .get(DeviceViewModel.class);

        mDevice = getArguments() != null ?
                (Device) DataHelper.convertJsonToObject(getArguments().getString(ARG_PARAM), Device.class.getName()) : null;

        mBluetoothLEHelper = new BluetoothLEHelper(getActivity());
        //endregion Initialize Objects
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lock_page, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //region Initialize Views
        imgMainLockPage = view.findViewById(R.id.img_main_lock_page);
        imgBleLockPage = view.findViewById(R.id.img_ble_lock_page);
        //endregion Initialize Views

        //region Setup Views
        imgMainLockPage.setOnClickListener(this);
        imgBleLockPage.setOnClickListener(this);
        //endregion Setup Views

        //region init
        setDataInView();
        //endregion init
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_main_lock_page:
                break;
            case R.id.img_ble_lock_page:
                connectToDevice();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
    //endregion Main CallBacks

    //region BLE CallBacks
    @Override
    public void onDataReceived(Object response) {
    }

    @Override
    public void onDataSent(Object value) {
    }

    @Override
    public void onFindBleCompleted(List response) {
    }

    @Override
    public void onFindBleFault(Object response) {
    }

    @Override
    public void onClickBleDevice(ScannedDeviceModel mScannedDeviceModel) {
    }
    //endregion BLE CallBacks

    //region Declare Methods
    private void setDataInView() {
        if (imgMainLockPage != null)
            ViewHelper.setLockStatusImage(imgMainLockPage, mDevice.getLockStatus());
    }
    //endregion Declare Methods

    //region Declare BLE Methods
    private void connectToDevice() {
        if (BleHelper.isLocationRequired(getContext())) {
            if (BleHelper.isLocationPermissionsGranted(getContext())) {
                if (!BleHelper.isLocationEnabled(getContext()))
                    BleHelper.enableLocation(getActivity());
                else {
                    if (BleHelper.isBleEnabled()) {
                        if (mBluetoothLEHelper.isReadyForScan())
                            scanDevices();
                    } else BleHelper.enableBluetooth(getActivity());
                }
            } else {
                BleHelper.grantLocationPermission(getActivity());

                final boolean deniedForever = BleHelper.isLocationPermissionDeniedForever(getActivity());
                if (!deniedForever)
                    BleHelper.grantLocationPermission(getActivity());

                if (deniedForever)
                    BleHelper.handlePermissionSettings(getActivity());
            }
        } else {
            if (BleHelper.isBleEnabled()) {
                scanDevices();
            } else BleHelper.enableBluetooth(getActivity());
        }
    }

    public void scanDevices() {
        if (!mBluetoothLEHelper.isScanning()) {
            mBluetoothLEHelper.setScanPeriod(1000);
            Handler mHandler = new Handler();
            mBluetoothLEHelper.scanLeDevice(true);

            mHandler.postDelayed(() -> {
                connectToSpecificBleDevice(getListOfScannedDevices());
            }, mBluetoothLEHelper.getScanPeriod());
        }
    }

    private boolean connectToSpecificBleDevice(List<ScannedDeviceModel> listOfScannedDevices) {
        for (ScannedDeviceModel device : listOfScannedDevices)
            if (device.getMacAddress().equals(mDevice.getBleDeviceMacAddress())) {
                this.mDeviceViewModel.connect(device);
                this.mDeviceViewModel.isConnected().observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(@Nullable Boolean isConnected) {
                        ViewHelper.setBleConnectionStatusImage(imgBleLockPage, isConnected);

                        mDeviceViewModel.changeNotifyForCharacteristic(BleHelper.CHARACTERISTIC_UUID_TX, true);
                        mDeviceViewModel.readCharacteristic(BleHelper.CHARACTERISTIC_UUID_TX);
                    }
                });
                return true;
            }

        return false;
    }

    public List<ScannedDeviceModel> getListOfScannedDevices() {
        List<ScannedDeviceModel> mScannedDeviceModelList = new ArrayList<>();

        if (mBluetoothLEHelper.getListDevices().size() > 0)
            for (BluetoothLE device : mBluetoothLEHelper.getListDevices())
                mScannedDeviceModelList.add(new ScannedDeviceModel(device));

        return mScannedDeviceModelList;
    }
    //endregion Declare BLE Methods
}
