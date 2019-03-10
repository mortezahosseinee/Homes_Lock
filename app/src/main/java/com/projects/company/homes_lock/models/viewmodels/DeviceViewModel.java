package com.projects.company.homes_lock.models.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.projects.company.homes_lock.R;
import com.projects.company.homes_lock.base.BaseApplication;
import com.projects.company.homes_lock.database.tables.Device;
import com.projects.company.homes_lock.database.tables.User;
import com.projects.company.homes_lock.database.tables.UserLock;
import com.projects.company.homes_lock.models.datamodels.ble.ConnectedClientsModel;
import com.projects.company.homes_lock.models.datamodels.ble.ScannedDeviceModel;
import com.projects.company.homes_lock.models.datamodels.ble.WifiNetworksModel;
import com.projects.company.homes_lock.models.datamodels.mqtt.MessageModel;
import com.projects.company.homes_lock.models.datamodels.request.AddRelationHelperModel;
import com.projects.company.homes_lock.models.datamodels.request.UserLockModel;
import com.projects.company.homes_lock.models.datamodels.response.FailureModel;
import com.projects.company.homes_lock.models.datamodels.response.ResponseBodyFailureModel;
import com.projects.company.homes_lock.models.datamodels.response.ResponseBodyModel;
import com.projects.company.homes_lock.repositories.local.ILocalRepository;
import com.projects.company.homes_lock.repositories.local.LocalRepository;
import com.projects.company.homes_lock.repositories.remote.NetworkListener;
import com.projects.company.homes_lock.repositories.remote.NetworkRepository;
import com.projects.company.homes_lock.ui.device.fragment.adddevice.AddDeviceFragment;
import com.projects.company.homes_lock.ui.device.fragment.adddevice.IAddDeviceFragment;
import com.projects.company.homes_lock.ui.device.fragment.gatewaypage.IGatewayPageFragment;
import com.projects.company.homes_lock.ui.device.fragment.lockpage.ILockPageFragment;
import com.projects.company.homes_lock.ui.device.fragment.lockpage.LockPageFragment;
import com.projects.company.homes_lock.ui.device.fragment.managemembers.IManageMembersFragment;
import com.projects.company.homes_lock.ui.device.fragment.setting.ISettingFragment;
import com.projects.company.homes_lock.utils.ble.BleDeviceManager;
import com.projects.company.homes_lock.utils.ble.IBleDeviceManagerCallbacks;
import com.projects.company.homes_lock.utils.ble.IBleScanListener;
import com.projects.company.homes_lock.utils.ble.SingleLiveEvent;
import com.projects.company.homes_lock.utils.helper.BleHelper;
import com.projects.company.homes_lock.utils.mqtt.IMQTTListener;
import com.projects.company.homes_lock.utils.mqtt.MQTTHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;
import okhttp3.ResponseBody;

import static com.projects.company.homes_lock.utils.helper.BleHelper.CHARACTERISTIC_UUID_RX;
import static com.projects.company.homes_lock.utils.helper.BleHelper.CHARACTERISTIC_UUID_TX;
import static com.projects.company.homes_lock.utils.helper.BleHelper.createCommand;
import static com.projects.company.homes_lock.utils.helper.DataHelper.isInstanceOfList;
import static com.projects.company.homes_lock.utils.helper.DataHelper.subArrayByte;

public class DeviceViewModel extends AndroidViewModel
        implements
        NetworkListener.SingleNetworkListener,
        NetworkListener.ListNetworkListener,
        IMQTTListener,
        IBleDeviceManagerCallbacks,
        ILocalRepository {

    //region Declare Constants
    //endregion Declare Constants

    //region Declare Variables
    private String requestType = "";
    private boolean bleBufferStatus = false;

    private Integer oldPairingPassword = 0;
    private Integer newPairingPassword = 0;
    //endregion Declare Variables

    //region Declare Arrays & Lists
    private List<byte[]> mBleCommandsPool = new ArrayList<>();
    //endregion Declare Arrays & Lists

    //region Declare Objects
    private LocalRepository mLocalRepository;
    private NetworkRepository mNetworkRepository;
    private IBleScanListener mIBleScanListener;
    private ILockPageFragment mILockPageFragment;
    private IGatewayPageFragment mIGatewayPageFragment;
    private IAddDeviceFragment mIAddDeviceFragment;
    private IManageMembersFragment mIManageMembersFragment;
    private ISettingFragment mISettingFragment;

    private final BleDeviceManager mBleDeviceManager;

    //    private final MutableLiveData<Boolean> mBleBufferIsClean = new MutableLiveData<>();
    private final MutableLiveData<String> mConnectionState = new MutableLiveData<>(); // Connecting, Connected, Disconnecting, Disconnected
    private final MutableLiveData<Boolean> mIsConnected = new MutableLiveData<>();
    private final SingleLiveEvent<Boolean> mIsSupported = new SingleLiveEvent<>();
    //endregion Declare Objects

    //region Constructor
    public DeviceViewModel(Application application) {
        super(application);

        //region Initialize Variables
        //endregion Initialize Variables

        //region Initialize Objects
        this.mLocalRepository = new LocalRepository(application);
        this.mNetworkRepository = new NetworkRepository();
        this.mBleDeviceManager = new BleDeviceManager(getApplication());
        this.mBleDeviceManager.setGattCallbacks(this);
        //endregion Initialize Objects
    }
    //endregion Constructor

    //region Device Table
    public LiveData<List<Device>> getAllLocalDevices() {
        return mLocalRepository.getAllDevices();
    }

    public Device getUserLockInfo(String objectId) {
        return mLocalRepository.getUserLockInfo(objectId);
    }

    public List<Device> getAllUserLocks() {
        return mLocalRepository.getAllUserLocks();
    }

    public LiveData<Device> getDeviceInfo(String mActiveDeviceObjectId) {
        return mLocalRepository.getADevice(mActiveDeviceObjectId);
    }

    public void insertLocalDevice(Device device) {
        mLocalRepository.insertDevice(this, device);
    }

    private void insertLocalDevices(List<Device> devices) {
        for (Device device : devices)
            mLocalRepository.insertDevice(this, device);
    }

    public void deleteLocalDevice(Device device) {
        mLocalRepository.deleteDevice(device);
    }
    //endregion Device Table

    //region BLE CallBacks
    @Override
    public void onDeviceConnecting(final BluetoothDevice device) {
        mConnectionState.postValue(getApplication().getString(R.string.ble_state_connecting));
    }

    @Override
    public void onDeviceConnected(final BluetoothDevice device) {
        mBleCommandsPool.clear();
        bleBufferStatus = true;
//        mBleBufferIsClean.postValue(true);
        mIsConnected.postValue(true);
        mConnectionState.postValue(getApplication().getString(R.string.ble_state_discovering_services));

//        //TODO remove these lines after test ble without password
        if (mIBleScanListener != null)
            mIBleScanListener.onBonded(device);
    }

    @Override
    public void onDeviceDisconnecting(final BluetoothDevice device) {
        mBleCommandsPool.clear();
        mIsConnected.postValue(false);
        mIsSupported.postValue(false);
    }

    @Override
    public void onDeviceDisconnected(final BluetoothDevice device) {
        mBleCommandsPool.clear();
        mIsConnected.postValue(false);
        mIsSupported.postValue(false);
    }

    @Override
    public void onLinklossOccur(final BluetoothDevice device) {
        mBleCommandsPool.clear();
        mIsConnected.postValue(false);
        mIsSupported.postValue(false);
    }

    @Override
    public void onServicesDiscovered(final BluetoothDevice device, final boolean optionalServicesFound) {
        mConnectionState.postValue(getApplication().getString(R.string.ble_state_initializing));
    }

    @Override
    public void onDeviceReady(final BluetoothDevice device) {
        mIsSupported.postValue(true);
    }

    @Override
    public boolean shouldEnableBatteryLevelNotifications(final BluetoothDevice device) {
        return false;
    }

    @Override
    public void onBatteryValueReceived(final BluetoothDevice device, final int value) {
    }

    @Override
    public void onBondingRequired(final BluetoothDevice device) {
        mIsSupported.postValue(false);
    }

    @Override
    public void onBonded(final BluetoothDevice device) {
        //TODO uncomment these lines after test ble without password
        if (mIBleScanListener != null)
            mIBleScanListener.onBonded(device);
    }

    @Override
    public void onError(final BluetoothDevice device, final String message, final int errorCode) {
    }

    @Override
    public void onDeviceNotSupported(final BluetoothDevice device) {
        mIsSupported.postValue(false);
    }

    @Override
    public void onDataReceived(Object response) {
        UUID responseUUID;
        if (response instanceof BluetoothGattCharacteristic) {
            responseUUID = ((BluetoothGattCharacteristic) response).getUuid();
            if (responseUUID.equals(CHARACTERISTIC_UUID_TX))
                handleReceivedResponse(((BluetoothGattCharacteristic) response).getValue());
        } else
            handleReceivedResponse((byte[]) response);
    }

    @Override
    public void onDataSent(Object response) {
        Log.d("onDataSent", Arrays.toString(((BluetoothGattCharacteristic) response).getValue()));
    }
    //endregion BLE CallBacks

    //region Network Callbacks
    @Override
    public void onResponse(Object response) {
        if (isInstanceOfList(response, Device.class.getName()))
            insertLocalDevices((List<Device>) response);
        else if (response instanceof ResponseBody) {
            switch (getRequestType()) {
                case "validateLockInOnlineDatabase":
                    if (mIAddDeviceFragment != null)
                        mIAddDeviceFragment.onFindLockInOnlineDataBaseSuccessful(
                                ((ResponseBody) response).source().toString()
                                        .replace("[text=", "")
                                        .replace("]", "")
                                        .replace("\"", ""));
                    break;
                case "addLockToUserLock":
                    if (mIAddDeviceFragment != null)
                        mIAddDeviceFragment.onAddLockToUserLockSuccessful(((ResponseBody) response).source().toString().equals("[text=1]"));
                    else if (mIManageMembersFragment != null)
                        mIManageMembersFragment.onAddLockToUserLockSuccessful(((ResponseBody) response).source().toString().equals("[text=1]"));
                    break;
                case "addUserLockToUser":
                    if (mIAddDeviceFragment != null)
                        mIAddDeviceFragment.onAddUserLockToUserSuccessful(((ResponseBody) response).source().toString().equals("[text=1]"));
                    else if (mIManageMembersFragment != null)
                        mIManageMembersFragment.onAddUserLockToUserSuccessful(((ResponseBody) response).source().toString().equals("[text=1]"));
                    break;
                case "removeDeviceForAllMembers":
                    if (mISettingFragment != null)
                        mISettingFragment.onRemoveAllLockMembersSuccessful(
                                ((ResponseBody) response).source().toString()
                                        .replace("[text=", "")
                                        .replace("]", "")
                                        .replace("\"", ""));
                    break;
                case "removeDeviceForOneMember":
                    if (mISettingFragment != null)
                        mISettingFragment.onRemoveDeviceForOneMemberSuccessful(
                                ((ResponseBody) response).source().toString()
                                        .replace("[text=", "")
                                        .replace("]", "")
                                        .replace("\"", ""));
                    break;
                case "enablePushNotification":
                    if (mIAddDeviceFragment != null)
                        mIAddDeviceFragment.onDeviceRegistrationPushNotificationSuccessful(((ResponseBodyModel) response).getRegistrationId());
                    break;
                default:
                    break;
            }
        } else if (response instanceof UserLock) {
            if (mIAddDeviceFragment != null)
                mIAddDeviceFragment.onInsertUserLockSuccessful((UserLock) response);
            else if (mIManageMembersFragment != null)
                mIManageMembersFragment.onInsertUserLockSuccessful((UserLock) response);
        } else if (response instanceof User) {
            if (mIAddDeviceFragment != null)
                mIAddDeviceFragment.onGetUserSuccessful((User) response);
        } else if (response instanceof Device) {
            if (mILockPageFragment != null)
                mILockPageFragment.onGetUpdatedDevice((Device) response);
        }
    }

    @Override
    public void onSingleNetworkListenerFailure(Object response) {
        if (response instanceof ResponseBodyFailureModel) {
            switch (getRequestType()) {
                case "validateLockInOnlineDatabase":
                    if (mIAddDeviceFragment != null)
                        mIAddDeviceFragment.onFindLockInOnlineDataBaseFailed((ResponseBodyFailureModel) response);
                    break;
                case "addLockToUserLock":
                    if (mIAddDeviceFragment != null)
                        mIAddDeviceFragment.onAddLockToUserLockFailed((ResponseBodyFailureModel) response);
                    else if (mIManageMembersFragment != null)
                        mIManageMembersFragment.onAddLockToUserLockFailed((ResponseBodyFailureModel) response);
                    break;
                case "addUserLockToUser":
                    if (mIAddDeviceFragment != null)
                        mIAddDeviceFragment.onAddUserLockToUserFailed((ResponseBodyFailureModel) response);
                    else if (mIManageMembersFragment != null)
                        mIManageMembersFragment.onAddUserLockToUserFailed((ResponseBodyFailureModel) response);
                    break;
                case "removeDeviceForAllMembers":
                    if (mISettingFragment != null)
                        mISettingFragment.onRemoveAllLockMembersFailed((ResponseBodyFailureModel) response);
                    break;
                case "removeDeviceForOneMember":
                    if (mISettingFragment != null)
                        mISettingFragment.onRemoveDeviceForOneMemberFailed((ResponseBodyFailureModel) response);
                    break;
                case "enablePushNotification":
                    if (mIAddDeviceFragment != null)
                        mIAddDeviceFragment.onDeviceRegistrationPushNotificationFailed((ResponseBodyFailureModel) response);
                    break;
                default:
                    break;
            }
        } else if (response instanceof FailureModel) {
            if (mIAddDeviceFragment != null) {
                switch (getRequestType()) {
                    case "############"://TODO i do not know this label is true
                        mIAddDeviceFragment.onGetUserFailed((FailureModel) response);
                        break;
                    case "insertOnlineUserLock":
                        mIAddDeviceFragment.onInsertUserLockFailed((FailureModel) response);
                        break;
                }
            } else if (mIManageMembersFragment != null)
                mIManageMembersFragment.onInsertUserLockFailed((FailureModel) response);
        }
    }

    @Override
    public void onListNetworkListenerFailure(FailureModel response) {
    }
    //endregion Network Callbacks

    //region Local Repository Callbacks
    @Override
    public void onDataInsert(Object object) {
        Log.d(getClass().getName(), "Data inserted");

        if (object instanceof Device) {
            if (mIAddDeviceFragment != null)
                mIAddDeviceFragment.onDataInsert(object);
        }
    }

    @Override
    public void onClearAllData() {
    }
    //endregion Local Repository Callbacks

    //region MQTT CallBacks
    @Override
    public void onConnectionToBrokerLost(Object response) {
    }

    @Override
    public void onMessageArrived(Object response) {
        onDataReceived(((MessageModel) response).getMqttMessagePayload());
    }

    @Override
    public void onDeliveryMessageComplete(Object response) {

    }

    @Override
    public void onConnectionSuccessful(Object response) {
        MQTTHandler.subscribe(this);
    }

    @Override
    public void onConnectionFailure(Object response) {
    }

    @Override
    public void onSubscribeSuccessful(Object response) {
    }

    @Override
    public void onSubscribeFailure(Object response) {
    }

    @Override
    public void onPublishSuccessful(Object response) {
    }

    @Override
    public void onPublishFailure(Object response) {
    }
    //endregion MQTT CallBacks

    //region BLE Methods
    public void connect(IBleScanListener mIBleScanListener, final ScannedDeviceModel device) {
        this.mIBleScanListener = mIBleScanListener;
        if (mIBleScanListener instanceof ILockPageFragment)
            this.mILockPageFragment = (ILockPageFragment) mIBleScanListener;
        else if (mIBleScanListener instanceof IAddDeviceFragment)
            this.mIAddDeviceFragment = (IAddDeviceFragment) mIBleScanListener;

        final LogSession logSession = Logger.newSession(getApplication(), null, device.getMacAddress(), device.getName());
        mBleDeviceManager.setLogger(logSession);
        mBleDeviceManager.connect(device.getDevice());
    }

    public void disconnect() {
        mBleDeviceManager.disconnect();
    }

    private void handleReceivedResponse(byte[] responseValue) {
//        for (byte aResponseValue : responseValue)
//            Log.e("Response: ", String.format("%d", aResponseValue & 0xff));
        Log.e("handleReceivedResponse", new String(responseValue));

        if (responseValue[1] == 0x40) {
            Log.e(getClass().getName(), "Buffer is free");
            bleBufferStatus = true;
//            mBleBufferIsClean.postValue(true);
            sendNextCommandFromBlePool();
        }

        String keyValue = new String(subArrayByte(responseValue, 2, responseValue.length - 1));

        try {
            JSONObject keyCommandJson = new JSONObject(keyValue);
            String keyCommand = keyCommandJson.keys().next();

            switch (keyCommand) {
                case "batt":
                    if (mILockPageFragment != null)
                        mLocalRepository.updateDeviceBatteryPercentage(((LockPageFragment) mILockPageFragment).getDevice().getObjectId(),
                                keyCommandJson.getInt(keyCommand));
                    break;
                case "islock":
                    if (mILockPageFragment != null)
                        mLocalRepository.updateDeviceIsLocked(((LockPageFragment) mILockPageFragment).getDevice().getObjectId(),
                                keyCommandJson.getBoolean(keyCommand));
                    break;
                case "isopen":
                    if (mILockPageFragment != null)
                        mLocalRepository.updateDeviceIsDoorClosed(((LockPageFragment) mILockPageFragment).getDevice().getObjectId(),
                                keyCommandJson.getBoolean(keyCommand));
                    break;
                case "knock":
                    Log.e(getClass().getName(), String.format("Door knocked : %s", keyCommandJson.getString(keyCommand)));
                    break;
                case "lock":
                    if (mILockPageFragment != null && keyCommandJson.get(keyCommand).equals("ok"))
                        mILockPageFragment.onSendLockCommandSuccessful("lock");
                    break;
                case "unlock":
                    if (mILockPageFragment != null && keyCommandJson.get(keyCommand).equals("ok"))
                        mILockPageFragment.onSendLockCommandSuccessful("unlock");
                    break;
                case "type":
                    Log.e(getClass().getName(), String.format("type setting IS: %s", keyCommandJson.getString(keyCommand)));
                    if (mILockPageFragment != null)
                        mLocalRepository.updateDeviceType(((LockPageFragment) mILockPageFragment).getDevice().getObjectId(),
                                keyCommandJson.getString(keyCommand));
                    break;
                case "fw_ver"://TODO change to fw_version MR. Ghaderan
                    Log.e(getClass().getName(), String.format("fw_ver setting IS: %s", keyCommandJson.getString(keyCommand)));
                    if (mILockPageFragment != null)
                        mLocalRepository.updateFirmwareVersion(((LockPageFragment) mILockPageFragment).getDevice().getObjectId(),
                                keyCommandJson.getString(keyCommand));
                    break;
                case "hw_ver":
                    Log.e(getClass().getName(), String.format("hw_ver setting IS: %s", keyCommandJson.getString(keyCommand)));
                    if (mILockPageFragment != null)
                        mLocalRepository.updateHardwareVersion(((LockPageFragment) mILockPageFragment).getDevice().getObjectId(),
                                keyCommandJson.getString(keyCommand));
                    break;
                case "prd":
                    Log.e(getClass().getName(), String.format("prd setting IS: %s", keyCommandJson.getString(keyCommand)));
                    if (mILockPageFragment != null)
                        mLocalRepository.updateProductionDate(((LockPageFragment) mILockPageFragment).getDevice().getObjectId(),
                                keyCommandJson.getString(keyCommand));
                    break;
                case "sn":
                    Log.e(getClass().getName(), String.format("sn setting IS: %s", keyCommandJson.getString(keyCommand)));
                    if (mILockPageFragment != null)
                        mLocalRepository.updateSerialNumber(((LockPageFragment) mILockPageFragment).getDevice().getObjectId(),
                                keyCommandJson.getString(keyCommand));
                    break;
                case "did":
                    Log.e(getClass().getName(), String.format("did setting %s", keyCommandJson.getString(keyCommand)));
                    if (mILockPageFragment != null)
                        mLocalRepository.updateDynamicId(((LockPageFragment) mILockPageFragment).getDevice().getObjectId(),
                                keyCommandJson.getString(keyCommand));
                    break;
                case "opass":
                    Log.e(getClass().getName(), String.format("old pass %s", keyCommandJson.getString(keyCommand)));
                    if (mISettingFragment != null && keyCommandJson.get(keyCommand).equals("ok")) {
                        changePairingPasswordViaBleFinalStep();
                        mISettingFragment.onCheckOldPairingPasswordSuccessful();
                    }
                    break;
                case "npass":
                    Log.e(getClass().getName(), String.format("new pass %s", keyCommandJson.getString(keyCommand)));
                    if (mISettingFragment != null && keyCommandJson.get(keyCommand).equals("ok"))
                        mISettingFragment.onChangePairingPasswordSuccessful();
                    break;
                case "right":
                    if (mISettingFragment != null) {
                        if (keyCommandJson.get(keyCommand).equals("ok"))
                            mISettingFragment.onSetDoorInstallationSuccessful();
                        else
                            mLocalRepository.updateDoorInstallation(((LockPageFragment) mILockPageFragment).getDevice().getObjectId(),
                                    keyCommandJson.getBoolean(keyCommand));
                    }
                    break;
                case "step":
                    if (mISettingFragment != null) {
                        if (keyCommandJson.get(keyCommand).equals("ok"))
                            mISettingFragment.onSetLockStagesSuccessful();
                        else
                            mLocalRepository.updateLockStages(((LockPageFragment) mILockPageFragment).getDevice().getObjectId(),
                                    keyCommandJson.getInt(keyCommand));
                    }
                    break;
                case "reset":
                    if (mISettingFragment != null) {
                        if (keyCommandJson.get(keyCommand).equals("ok"))
                            mISettingFragment.onResetBleDeviceSuccessful();
                    }
                    break;
                case "err":
                    switch (keyCommandJson.getString(keyCommand)) {
                        case "inter":
                            Log.e(getClass().getName(), String.format("Device faces with internal Error, try last command again!: %s",
                                    keyCommandJson.getString(keyCommand)));
                            break;
                        case "per":
                            Log.e(getClass().getName(), String.format("Don't have permission for last command!: %s",
                                    keyCommandJson.getString(keyCommand)));
                            break;
                        case "key":
                            Log.e(getClass().getName(), String.format("Key of Last command does not exist!: %s",
                                    keyCommandJson.getString(keyCommand)));
                            break;
                        case "opass":
                            if (mISettingFragment != null)
                                mISettingFragment.onCheckOldPairingPasswordFailed(keyCommandJson.getString(keyCommand));
                            break;
                        case "npass":
                            if (mISettingFragment != null)
                                mISettingFragment.onChangePairingPasswordFailed(keyCommandJson.getString(keyCommand));
                            break;
                    }
                    break;
                case "clt":
                    if (mILockPageFragment != null) {
                        if (keyCommandJson.get(keyCommand).equals("end"))
                            Log.i(getClass().getName(), "Get connected clients FINISHED.");
                        else
                            mILockPageFragment.onGetReceiveNewConnectedClientToDevice(keyCommandJson.getString(keyCommand));
                    }
                    break;
                case "dis":
                    if (mILockPageFragment != null) {
                        if (keyCommandJson.get(keyCommand).equals("ok")) {
                            Log.i(getClass().getName(), "Device disconnects successfully.");
                            mILockPageFragment.onDeviceDisconnectedSuccessfully();
                        }
                    }
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendLockCommand(LockPageFragment fragment, boolean lockCommand) {
        mILockPageFragment = fragment;
        if (BaseApplication.isUserLoggedIn())
            MQTTHandler.toggle(this, "", createCommand(new byte[]{0x02}, new byte[]{(byte) (lockCommand ? 0x01 : 0x02)}));
        else
            mBleDeviceManager.writeCharacteristic(CHARACTERISTIC_UUID_RX, BleHelper.createReadMessage(lockCommand ? "lock" : "unlock"));
    }

    public void getDeviceDataFromBleDevice() {
        addNewCommandToBlePool(BleHelper.createReadMessage("batt"));
        addNewCommandToBlePool(BleHelper.createReadMessage("isopen"));
        addNewCommandToBlePool(BleHelper.createReadMessage("islock"));
    }

    public void getDeviceCommonSettingInfoFromBleDevice() {
        addNewCommandToBlePool(BleHelper.createReadMessage("type"));
        addNewCommandToBlePool(BleHelper.createReadMessage("fw_ver"));
        addNewCommandToBlePool(BleHelper.createReadMessage("hw_ver"));
        addNewCommandToBlePool(BleHelper.createReadMessage("prd"));
        addNewCommandToBlePool(BleHelper.createReadMessage("sn"));
        addNewCommandToBlePool(BleHelper.createReadMessage("did"));
    }

    public void getLockSpecifiedSettingInfoFromBleDevice() {
        addNewCommandToBlePool(BleHelper.createReadMessage("right"));
        addNewCommandToBlePool(BleHelper.createReadMessage("step"));
    }

    private void addNewCommandToBlePool(byte[] command) {
        if (mBleCommandsPool == null)
            mBleCommandsPool = new ArrayList<>();

        mBleCommandsPool.add(command);
        sendNextCommandFromBlePool();
    }

    private void sendNextCommandFromBlePool() {
        if (mBleCommandsPool.size() > 0 && bleBufferStatus) {
            Log.e(getClass().getName(), "Buffer is full");
            bleBufferStatus = false;
            mBleDeviceManager.writeCharacteristic(CHARACTERISTIC_UUID_RX, mBleCommandsPool.get(0));
            mBleCommandsPool.remove(0);
        }
    }

    public void getAvailableWifiNetworksCountAroundDevice(IGatewayPageFragment mIGatewayPageFragment) {
        this.mIGatewayPageFragment = mIGatewayPageFragment;
        Log.d("Scenario Wifi", "1: Send request to get wifi network list");
        mBleDeviceManager.writeCharacteristic(CHARACTERISTIC_UUID_RX, createCommand(new byte[]{0x06}, new byte[]{}));
    }

    public void getConnectedClientsToDevice(ILockPageFragment mILockPageFragment) {
        this.mILockPageFragment = mILockPageFragment;
        Log.d("Scenario Wifi", "1: Send request to get wifi network list");
        addNewCommandToBlePool(BleHelper.createReadMessage("clt"));
    }

    public void getAvailableWifiNetworksAroundDevice(IGatewayPageFragment mIGatewayPageFragment, int networkIndex) {
        this.mIGatewayPageFragment = mIGatewayPageFragment;
        Log.d("Scenario Wifi", String.format("7: Send Request to get %dth wifi network.", networkIndex));
        mBleDeviceManager.writeCharacteristic(CHARACTERISTIC_UUID_RX, createCommand(new byte[]{0x07}, new byte[]{(byte) networkIndex}));
    }

    public void setDeviceWifiNetwork(IGatewayPageFragment mIGatewayPageFragment, WifiNetworksModel wifiNetwork) {
        this.mIGatewayPageFragment = mIGatewayPageFragment;
        mBleDeviceManager.writeCharacteristic(CHARACTERISTIC_UUID_RX, createCommand(new byte[]{0x08}, wifiNetwork.getSSID().getBytes()));
        mBleDeviceManager.writeCharacteristic(CHARACTERISTIC_UUID_RX, createCommand(new byte[]{0x09}, wifiNetwork.getPassword().getBytes()));
        mBleDeviceManager.writeCharacteristic(CHARACTERISTIC_UUID_RX, createCommand(new byte[]{0x0A}, new byte[]{(byte) wifiNetwork.getAuthenticateType()}));
        mBleDeviceManager.writeCharacteristic(CHARACTERISTIC_UUID_RX, createCommand(new byte[]{0x0B}, new byte[]{0x01}));
    }

    public void disconnectGatewayWifiNetwork(IGatewayPageFragment mIGatewayPageFragment) {
        this.mIGatewayPageFragment = mIGatewayPageFragment;//TODO gateway
        mBleDeviceManager.writeCharacteristic(CHARACTERISTIC_UUID_RX, createCommand(new byte[]{0x0B}, new byte[]{0x00}));
    }

    public void setDeviceSetting(Fragment parentFragment, boolean doorInstallation, int lockStages) {
        mISettingFragment = (ISettingFragment) parentFragment;

        JSONObject commandJson;
        try {
            commandJson = new JSONObject();
            commandJson.put("right", doorInstallation);
            mBleDeviceManager.writeCharacteristic(CHARACTERISTIC_UUID_RX, BleHelper.createWriteMessage(commandJson.toString()));

            commandJson = new JSONObject();
            commandJson.put("step", lockStages);
            mBleDeviceManager.writeCharacteristic(CHARACTERISTIC_UUID_RX, BleHelper.createWriteMessage(commandJson.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void changeOnlinePasswordViaBle(Fragment parentFragment, String oldPassword, String newPassword) {
        mISettingFragment = (ISettingFragment) parentFragment;
    }

    public void changePairingPasswordViaBle(Fragment parentFragment, Integer oldPassword, Integer newPassword) {
        mISettingFragment = (ISettingFragment) parentFragment;

        oldPairingPassword = oldPassword;
        newPairingPassword = newPassword;

        changePairingPasswordViaBleInitialStep();
    }

    private void changePairingPasswordViaBleInitialStep() {
        JSONObject commandJson;
        try {
            commandJson = new JSONObject();
            commandJson.put("opass", oldPairingPassword);
            mBleDeviceManager.writeCharacteristic(CHARACTERISTIC_UUID_RX, BleHelper.createWriteMessage(commandJson.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void changePairingPasswordViaBleFinalStep() {
        JSONObject commandJson;
        try {
            commandJson = new JSONObject();
            commandJson.put("npass", newPairingPassword);
            mBleDeviceManager.writeCharacteristic(CHARACTERISTIC_UUID_RX, BleHelper.createWriteMessage(commandJson.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void resetBleDevice(Fragment parentFragment) {
        mISettingFragment = (ISettingFragment) parentFragment;

        JSONObject commandJson;
        try {
            commandJson = new JSONObject();
            commandJson.put("reset", JSONObject.NULL);

            addNewCommandToBlePool(BleHelper.createWriteMessage(commandJson.toString()));
            sendNextCommandFromBlePool();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public LiveData<Boolean> isConnected() {
        return mIsConnected;
    }

    public LiveData<Boolean> isSupported() {
        return mIsSupported;
    }
    //endregion BLE Methods

    //region Online Methods
    public void validateLockInOnlineDatabase(AddDeviceFragment fragment, String serialNumber) {
        setRequestType("validateLockInOnlineDatabase");
        mIAddDeviceFragment = fragment;
        mNetworkRepository.getDeviceObjectIdWithSerialNumber(this, serialNumber);
    }

    public void validateLockInOnlineDatabase(Fragment fragment, String serialNumber) {
        setRequestType("validateLockInOnlineDatabase");
        mIAddDeviceFragment = (IAddDeviceFragment) fragment;
        mNetworkRepository.getDeviceObjectIdWithSerialNumber(this, serialNumber);
    }

    public void insertOnlineUserLock(UserLockModel userLock) {
        setRequestType("insertOnlineUserLock");
        mNetworkRepository.insertUserLock(this, userLock);
    }

    public void insertOnlineUserLock(IManageMembersFragment mIManageMembersFragment, UserLockModel userLock) {
        this.mIManageMembersFragment = mIManageMembersFragment;
        mNetworkRepository.insertUserLock(this, userLock);
    }

    public void addLockToUserLock(String userLockObjectId, String lockObjectId) {
        setRequestType("addLockToUserLock");
        mNetworkRepository.addLockToUserLock(this, userLockObjectId, new AddRelationHelperModel(lockObjectId));
    }

    public void addUserLockToUser(String userObjectId, String userLockObjectId) {
        setRequestType("addUserLockToUser");
        mNetworkRepository.addUserLockToUser(this, userObjectId, new AddRelationHelperModel(userLockObjectId));
    }

    public void setListenerForDevice(Fragment fragment, Device mDevice) {
        mILockPageFragment = (ILockPageFragment) fragment;
        mNetworkRepository.setListenerForDevice(this, mDevice);
    }

    public void enablePushNotification(String lockObjectId) {
        setRequestType("enablePushNotification");
        mNetworkRepository.enablePushNotification(this, Collections.singletonList(lockObjectId));
    }

    public void removeDevice(Fragment parentFragment, boolean removeAllMembers, Device mDevice) {
        mISettingFragment = (ISettingFragment) parentFragment;
        if (mDevice.isLockSavedInServerByCheckUserLocks()) {
            if (removeAllMembers) {
                setRequestType("removeDeviceForAllMembers");
                mNetworkRepository.removeDeviceForAllMembers(this, mDevice.getObjectId());
            } else {
                setRequestType("removeDeviceForOneMember");
                mNetworkRepository.removeDeviceForOneMember(this, mDevice.getUserLockObjectId());
            }
        } else
            mLocalRepository.deleteDevice(mDevice);
    }
    //endregion Online Methods

    //region Declare Methods
    public void initMQTT(Context context, String deviceObjectId) {
        if (BaseApplication.isUserLoggedIn())
            MQTTHandler.setup(this, context, deviceObjectId);
    }

    public void disconnectMQTT() {
        MQTTHandler.disconnect(this);
    }

    public void updateDevice(Device device) {
        mLocalRepository.updateDevice(device);
    }

    private void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    private String getRequestType() {
        return requestType;
    }

    public void disconnectClientFromDevice(ILockPageFragment mILockPageFragment, ConnectedClientsModel mConnectedClientsModel) {
        this.mILockPageFragment = mILockPageFragment;

        JSONObject commandJson;
        try {
            commandJson = new JSONObject();
            commandJson.put("dis", mConnectedClientsModel.getMacAddress());

            addNewCommandToBlePool(BleHelper.createWriteMessage(commandJson.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //endregion Declare Methods

    //region SharePreferences
    //endregion SharePreferences
}