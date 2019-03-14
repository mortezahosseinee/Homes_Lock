package com.projects.company.homes_lock.ui.device.fragment.gatewaypage;

import com.projects.company.homes_lock.database.tables.Device;
import com.projects.company.homes_lock.models.datamodels.ble.WifiNetworksModel;

/**
 * This is GatewayPageFragment Interface
 */
public interface IGatewayPageFragment {
    void onFindNewNetworkAroundDevice(WifiNetworksModel wifiNetworksModel);

//    void onGetAvailableWifiNetworksCountAroundDevice(int count);

    void onSetDeviceWifiNetworkSSIDSuccessful();

    void onSetDeviceWifiNetworkSSIDFailed();

    void onSetDeviceWifiNetworkPasswordSuccessful();

    void onSetDeviceWifiNetworkPasswordFailed();

    void onSetDeviceWifiNetworkAuthenticationTypeSuccessful();

    void onSetDeviceWifiNetworkAuthenticationTypeFailed();

    void onSetDeviceWifiNetworkSuccessful();

    void onSetDeviceWifiNetworkFailed();

    void onGetUpdatedDevice(Device response);

    void onSendRequestGetAvailableWifiSuccessful();

    void onGetReceiveNewConnectedClientToDevice(String connectedClientMacAddress);

    void onDeviceDisconnectedSuccessfully();
}
