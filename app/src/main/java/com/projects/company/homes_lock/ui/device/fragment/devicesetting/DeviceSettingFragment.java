package com.projects.company.homes_lock.ui.device.fragment.devicesetting;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.projects.company.homes_lock.R;
import com.projects.company.homes_lock.base.BaseFragment;
import com.projects.company.homes_lock.database.tables.Device;
import com.projects.company.homes_lock.models.datamodels.response.ResponseBodyFailureModel;
import com.projects.company.homes_lock.models.viewmodels.DeviceViewModel;
import com.projects.company.homes_lock.utils.helper.DataHelper;
import com.projects.company.homes_lock.utils.helper.ViewHelper;

import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.projects.company.homes_lock.base.BaseApplication.isUserLoggedIn;
import static com.projects.company.homes_lock.utils.helper.BleHelper.DOOR_INSTALLATION_SETTING_LEFT_HANDED;
import static com.projects.company.homes_lock.utils.helper.BleHelper.DOOR_INSTALLATION_SETTING_RIGHT_HANDED;
import static com.projects.company.homes_lock.utils.helper.DataHelper.CHANGE_ONLINE_PASSWORD;
import static com.projects.company.homes_lock.utils.helper.DataHelper.CHANGE_PAIRING_PASSWORD;
import static com.projects.company.homes_lock.utils.helper.ProgressDialogHelper.closeProgressDialog;
import static com.projects.company.homes_lock.utils.helper.ProgressDialogHelper.openProgressDialog;
import static com.projects.company.homes_lock.utils.helper.ViewHelper.setTypeface;
import static java.util.Objects.requireNonNull;

/**
 * A simple {@link BaseFragment} subclass.
 */
public class DeviceSettingFragment extends BaseFragment
        implements
        IDeviceSettingFragment,
        View.OnClickListener {

    //region Declare Constants
    private static final String ARG_PARAM = "param";
    //endregion Declare Constants

    //region Declare Views
    //Common Setting
    private TextView txvDeviceTypeSettingFragment;
    private TextView txvDeviceTypeDescriptionSettingFragment;
    private TextView txvFirmwareVersionSettingFragment;
    private TextView txvFirmwareVersionDescriptionSettingFragment;
    private TextView txvHardwareVersionSettingFragment;
    private TextView txvHardwareVersionDescriptionSettingFragment;
    private TextView txvProductionDateSettingFragment;
    private TextView txvProductionDateDescriptionSettingFragment;
    private TextView txvChangePairingPasswordSettingFragment;
    private TextView txvChangePairingPasswordDescriptionSettingFragment;
    private TextView txvDynamicIdSettingFragment;
    private TextView txvDynamicIdDescriptionSettingFragment;
    private TextView txvSerialNumberSettingFragment;
    private TextView txvSerialNumberDescriptionSettingFragment;
    private TextView txvResetDeviceSettingFragment;
    private TextView txvResetDeviceDescriptionSettingFragment;
    private TextView txvRemoveDeviceSettingFragment;
    private TextView txvRemoveDeviceDescriptionSettingFragment;
    private TextView txvChangeOnlinePasswordSettingFragment;
    private TextView txvChangeOnlinePasswordDescriptionSettingFragment;

    //Lock Specific Setting
    private TextView txvDoorInstallationSettingFragment;
    private TextView txvDoorInstallationDescriptionSettingFragment;
    private TextView txvCalibrationLockSettingFragment;
    private TextView txvCalibrationLockDescriptionSettingFragment;
    //endregion Declare Views

    //region Declare Variables
    private boolean doorInstallationDone = false;
    private boolean isConnectedToBleDevice = false;

    private String mDeviceType = "";
    //endregion Declare Variables

    //region Declare Objects
    private Fragment mFragment;
    private DeviceViewModel mDeviceViewModel;
    private Device mDevice;

    private Dialog mDoorInstallationDialog;
    private Dialog mInitializeCalibrationLockDialog;
    private Dialog mCalibrationLockDialog;
    private Dialog mLockPositionsDialog;
    private Dialog mChangeOnlinePasswordDialog;
    private Dialog mChangePairingPasswordDialog;
    private Dialog mRemoveLockDialog;
    private Dialog mResetLockDialog;
    //endregion Declare Objects

    //region Constructor
    public DeviceSettingFragment() {
    }

    public static DeviceSettingFragment newInstance(Device device, String deviceType, DeviceViewModel deviceViewModel) {
        DeviceSettingFragment fragment = new DeviceSettingFragment();

        fragment.mDeviceViewModel = deviceViewModel;
        fragment.mDevice = device;
        fragment.mDeviceType = deviceType;

        return fragment;
    }
    //endregion Constructor

    //region Main Callbacks
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //region Initialize Variables
        //endregion Initialize Variables

        //region Initialize Objects
        mFragment = this;
        this.mDeviceViewModel.getDeviceInfo(DeviceSettingFragment.this.mDevice.getObjectId()).observe(this, device -> {
            DeviceSettingFragment.this.mDevice = device;
            initViews();
        });
        this.mDeviceViewModel.isConnected().observe(this, isConnected -> {
            if (isConnected != null)
                isConnectedToBleDevice = isConnected;
        });
        //endregion Initialize Objects
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(
                this.mDeviceType.equals("LOCK") ?
                        R.layout.fragment_lock_setting :
                        (this.mDeviceType.equals("GTWY") ? R.layout.fragment_gateway_setting : R.layout.fragment_lock_setting), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //region Initialize Views
        //Common Setting
        txvDeviceTypeSettingFragment = view.findViewById(R.id.txv_device_type_setting_fragment);
        txvDeviceTypeDescriptionSettingFragment = view.findViewById(R.id.txv_device_type_description_setting_fragment);
        txvFirmwareVersionSettingFragment = view.findViewById(R.id.txv_firmware_version_setting_fragment);
        txvFirmwareVersionDescriptionSettingFragment = view.findViewById(R.id.txv_firmware_version_description_setting_fragment);
        txvHardwareVersionSettingFragment = view.findViewById(R.id.txv_hardware_version_setting_fragment);
        txvHardwareVersionDescriptionSettingFragment = view.findViewById(R.id.txv_hardware_version_description_setting_fragment);
        txvProductionDateSettingFragment = view.findViewById(R.id.txv_production_date_setting_fragment);
        txvProductionDateDescriptionSettingFragment = view.findViewById(R.id.txv_production_date_description_setting_fragment);
        txvChangePairingPasswordSettingFragment = view.findViewById(R.id.txv_change_pairing_password_setting_fragment);
        txvChangePairingPasswordDescriptionSettingFragment = view.findViewById(R.id.txv_change_pairing_password_description_setting_fragment);
        txvDynamicIdSettingFragment = view.findViewById(R.id.txv_dynamic_id_setting_fragment);
        txvDynamicIdDescriptionSettingFragment = view.findViewById(R.id.txv_dynamic_id_description_setting_fragment);
        txvSerialNumberSettingFragment = view.findViewById(R.id.txv_serial_number_setting_fragment);
        txvSerialNumberDescriptionSettingFragment = view.findViewById(R.id.txv_serial_number_description_setting_fragment);
        txvResetDeviceSettingFragment = view.findViewById(R.id.txv_reset_device_setting_fragment);
        txvResetDeviceDescriptionSettingFragment = view.findViewById(R.id.txv_reset_device_description_setting_fragment);
        txvRemoveDeviceSettingFragment = view.findViewById(R.id.txv_remove_device_setting_fragment);
        txvRemoveDeviceDescriptionSettingFragment = view.findViewById(R.id.txv_remove_device_description_setting_fragment);

        //Lock Specific Setting
        if (this.mDeviceType.equals("LOCK")) {
            txvDoorInstallationSettingFragment = view.findViewById(R.id.txv_door_installation_lock_setting_fragment);
            txvDoorInstallationDescriptionSettingFragment = view.findViewById(R.id.txv_door_installation_description_lock_setting_fragment);
            txvCalibrationLockSettingFragment = view.findViewById(R.id.txv_calibration_lock_setting_fragment);
            txvCalibrationLockDescriptionSettingFragment = view.findViewById(R.id.txv_calibration_description_lock_setting_fragment);
        }

        //Gateway Specific Setting
        if (this.mDeviceType.equals("GTWY")) {
            txvChangeOnlinePasswordSettingFragment = view.findViewById(R.id.txv_change_online_password_setting_fragment);
            txvChangeOnlinePasswordDescriptionSettingFragment = view.findViewById(R.id.txv_change_online_password_description_setting_fragment);
        }
        //endregion Initialize Views

        //region Setup Views
        //Common Setting
        txvDeviceTypeSettingFragment.setOnClickListener(this);
        txvDeviceTypeDescriptionSettingFragment.setOnClickListener(this);
        txvFirmwareVersionSettingFragment.setOnClickListener(this);
        txvFirmwareVersionDescriptionSettingFragment.setOnClickListener(this);
        txvHardwareVersionSettingFragment.setOnClickListener(this);
        txvHardwareVersionDescriptionSettingFragment.setOnClickListener(this);
        txvProductionDateSettingFragment.setOnClickListener(this);
        txvProductionDateDescriptionSettingFragment.setOnClickListener(this);
        txvChangePairingPasswordSettingFragment.setOnClickListener(this);
        txvChangePairingPasswordDescriptionSettingFragment.setOnClickListener(this);
        txvDynamicIdSettingFragment.setOnClickListener(this);
        txvDynamicIdDescriptionSettingFragment.setOnClickListener(this);
        txvSerialNumberSettingFragment.setOnClickListener(this);
        txvSerialNumberDescriptionSettingFragment.setOnClickListener(this);
        txvResetDeviceSettingFragment.setOnClickListener(this);
        txvResetDeviceDescriptionSettingFragment.setOnClickListener(this);
        txvRemoveDeviceSettingFragment.setOnClickListener(this);
        txvRemoveDeviceDescriptionSettingFragment.setOnClickListener(this);

        //Lock Specific Setting
        if (this.mDeviceType.equals("LOCK")) {
            txvDoorInstallationSettingFragment.setOnClickListener(this);
            txvDoorInstallationDescriptionSettingFragment.setOnClickListener(this);
            txvCalibrationLockSettingFragment.setOnClickListener(this);
            txvCalibrationLockDescriptionSettingFragment.setOnClickListener(this);
        }

        //Gateway Specific Setting
        if (this.mDeviceType.equals("GTWY")) {
            txvChangeOnlinePasswordSettingFragment.setOnClickListener(this);
            txvChangeOnlinePasswordDescriptionSettingFragment.setOnClickListener(this);
        }
        //endregion Setup Views
    }

    @Override
    public void onPause() {
        super.onPause();
        closeProgressDialog();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txv_device_type_setting_fragment:
            case R.id.txv_device_type_description_setting_fragment:
                //TODO copy info to clipboard
                break;
            case R.id.txv_firmware_version_setting_fragment:
            case R.id.txv_firmware_version_description_setting_fragment:
                //TODO copy info to clipboard
                break;
            case R.id.txv_hardware_version_setting_fragment:
            case R.id.txv_hardware_version_description_setting_fragment:
                //TODO copy info to clipboard
                break;
            case R.id.txv_door_installation_lock_setting_fragment:
            case R.id.txv_door_installation_description_lock_setting_fragment:
                handleDoorInstallation();
                break;
            case R.id.txv_calibration_lock_setting_fragment:
            case R.id.txv_calibration_description_lock_setting_fragment:
                v.setEnabled(false);
                handleInitializeCalibrationLock();
                break;
            case R.id.txv_change_pairing_password_setting_fragment:
            case R.id.txv_change_pairing_password_description_setting_fragment:
                handleChangePassword(CHANGE_PAIRING_PASSWORD);
                break;
            case R.id.txv_dynamic_id_setting_fragment:
            case R.id.txv_dynamic_id_description_setting_fragment:
                //TODO copy info to clipboard
                break;
            case R.id.txv_serial_number_setting_fragment:
            case R.id.txv_serial_number_description_setting_fragment:
                //TODO copy info to clipboard
                break;
            case R.id.txv_reset_device_setting_fragment:
            case R.id.txv_reset_device_description_setting_fragment:
                if (isConnectedToBleDevice)
                    handleResetLock();
                else
                    showToast("This is not available in BLE mqttDisconnect mode!");
                break;
            case R.id.txv_remove_device_setting_fragment:
            case R.id.txv_remove_device_description_setting_fragment:
                if (isUserLoggedIn())
                    handleRemoveDevice();
                else
                    showToast("This is not available in Local mode!");
                break;
            case R.id.btn_done_dialog_calibration_lock:
                dismissAllConfigDialogs();
                break;
            case R.id.txv_change_online_password_setting_fragment:
            case R.id.txv_change_online_password_description_setting_fragment:
                handleChangePassword(CHANGE_ONLINE_PASSWORD);
                break;
        }
    }
    //endregion Main Callbacks

    //region IDeviceSettingFragment Callbacks
    @Override
    public void onChangeOnlinePassword(boolean value) {
        closeProgressDialog();
        if (mChangeOnlinePasswordDialog != null) {
            mChangeOnlinePasswordDialog.dismiss();
            mChangeOnlinePasswordDialog = null;
        }
    }

    @Override
    public void onRemoveAllDeviceMembersSuccessful(String count) {
        closeProgressDialog();
        if (mRemoveLockDialog != null) {
            mRemoveLockDialog.dismiss();
            mRemoveLockDialog = null;
        }

        requireNonNull(getActivity()).getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onRemoveAllLockMembersFailed(ResponseBodyFailureModel response) {
    }

    @Override
    public void onSetDoorInstallationSuccessful() {
        doorInstallationDone = true;
        handleSetSettingResponse();
        showToast("Door installation done.");
    }

    @Override
    public void onSetDoorInstallationFailed() {
        mDeviceViewModel.getLockSpecifiedSettingInfoFromBleDevice(this);
        closeProgressDialog();
        Timber.tag("Set Door Installation").e("Failed");
        showToast("Door installation failed.");
    }

    @Override
    public void onCheckOldPairingPasswordSuccessful() {
        closeProgressDialog();
        Timber.e("Old pairing password matches.");
    }

    @Override
    public void onChangePairingPasswordSuccessful() {
        closeProgressDialog();
        Timber.e("Change Pairing password successful.");
        showToast("Change password done.");

        if (mChangePairingPasswordDialog != null) {
            mChangePairingPasswordDialog.dismiss();
            mChangePairingPasswordDialog = null;
        }
    }

    @Override
    public void onChangePairingPasswordFailed(String errorValue) {
        closeProgressDialog();
        showToast(String.format("New password hasn't minimum requirements as pairing password!: %s", errorValue));
        Timber.e("New password hasn't minimum requirements as pairing password!: %s", errorValue);
    }

    @Override
    public void onResetBleDeviceSuccessful() {
        if (mResetLockDialog != null) {
            mResetLockDialog.dismiss();
            mResetLockDialog = null;
        }

        this.mDeviceViewModel.disconnect();
        requireNonNull(getActivity()).getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onCheckOldPairingPasswordFailed(String errorValue) {
        Timber.e("Old password does not match pass in Device!: %s", errorValue);
        showToast("Error in Old password.");
        closeProgressDialog();
    }

    @Override
    public void onRemoveDeviceForOneMemberSuccessful(String count) {
        closeProgressDialog();
        if (mRemoveLockDialog != null) {
            mRemoveLockDialog.dismiss();
            mRemoveLockDialog = null;
        }

        requireNonNull(getActivity()).getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onRemoveDeviceForOneMemberFailed(ResponseBodyFailureModel response) {
        showToast("Can't remove member for device");
    }

    @Override
    public void onInitializeCalibrationLockSuccessful() {
        closeProgressDialog();

        if (mInitializeCalibrationLockDialog != null) {
            mInitializeCalibrationLockDialog.dismiss();
            mInitializeCalibrationLockDialog = null;
        }

        requireNonNull(DeviceSettingFragment.this.getActivity()).runOnUiThread(() -> handleCalibrationLockSteps());
    }

    @Override
    public void onSendCalibrationLockSuccessful() {
    }

    @Override
    public void onSetIdlePositionSuccessful() {
        closeProgressDialog();

        if (mCalibrationLockDialog != null) {
            if (mLockPositionsDialog != null) {
                mLockPositionsDialog.dismiss();
                mLockPositionsDialog = null;
            }
        }

        requireNonNull(DeviceSettingFragment.this.getActivity()).runOnUiThread(() ->
                handleLockPositionStatusImage("Idle", true));
    }

    @Override
    public void onSetLatchPositionSuccessful() {
        closeProgressDialog();

        if (mCalibrationLockDialog != null) {
            if (mLockPositionsDialog != null) {
                mLockPositionsDialog.dismiss();
                mLockPositionsDialog = null;
            }
        }

        requireNonNull(DeviceSettingFragment.this.getActivity()).runOnUiThread(() ->
                handleLockPositionStatusImage("Latch", true));
    }

    @Override
    public void onSetLockPositionSuccessful() {
        closeProgressDialog();

        if (mCalibrationLockDialog != null) {
            if (mLockPositionsDialog != null) {
                mLockPositionsDialog.dismiss();
                mLockPositionsDialog = null;
            }
        }

        requireNonNull(DeviceSettingFragment.this.getActivity()).runOnUiThread(() -> {
            handleLockPositionStatusImage("Lock", true);
            DeviceSettingFragment.this.mDeviceViewModel.sendConfigCommand(mFragment);
        });
    }

    @Override
    public void onSetConfigSuccessful() {
        showToast("Set Config done.");
        handleConfigResponse();
    }

    @Override
    public void onInitializeCalibrationLockFailed() {
        showToast("Check Lock then Click START again.");
    }

    @Override
    public void onCheckOldOnlinePasswordSuccessful() {
        closeProgressDialog();
        Timber.e("Old rest password matches.");
    }

    @Override
    public void onChangeOnlinePasswordWait() {
        Timber.e("Change Online password wait.");
        showToast("Change online password wait.");
    }

    @Override
    public void onChangeOnlinePasswordSuccessful() {
        closeProgressDialog();
        Timber.e("Change Online password successful.");
        showToast("Change online password done.");

        if (mChangeOnlinePasswordDialog != null) {
            mChangeOnlinePasswordDialog.dismiss();
            mChangeOnlinePasswordDialog = null;
        }
    }

    @Override
    public void onCheckOldOnlinePasswordFailed(String errorValue) {
        Timber.e("Old password does not match pass in Server!: %s", errorValue);
        showToast("Error in Old password online.");
        closeProgressDialog();
    }

    @Override
    public void onChangeOnlinePasswordFailed(String errorValue) {
        closeProgressDialog();
        showToast(String.format("New password hasn't minimum requirements as online password!: %s", errorValue));
        Timber.e("New password hasn't minimum requirements as online password!: %s", errorValue);
    }
    //endregion IDeviceSettingFragment Callbacks

    //region Declare Methods
    private void initViews() {
        txvDeviceTypeDescriptionSettingFragment.setText(mDevice.getDeviceType());
        txvFirmwareVersionDescriptionSettingFragment.setText(mDevice.getFWVersion());
        txvHardwareVersionDescriptionSettingFragment.setText(mDevice.getHWVersion());
        txvProductionDateDescriptionSettingFragment.setText(mDevice.getProductionDate());
        txvDynamicIdDescriptionSettingFragment.setText(mDevice.getDynamicId());
        txvSerialNumberDescriptionSettingFragment.setText(mDevice.getSerialNumber());

        if (!mDevice.getMemberAdminStatus() || isUserLoggedIn()) {
            if (this.mDeviceType.equals("GTWY")) {
                txvChangeOnlinePasswordSettingFragment.setVisibility(View.GONE);
                txvChangeOnlinePasswordDescriptionSettingFragment.setVisibility(View.GONE);
            }

            if (this.mDeviceType.equals("LOCK")) {
                txvDoorInstallationSettingFragment.setVisibility(GONE);
                txvDoorInstallationDescriptionSettingFragment.setVisibility(GONE);
                txvCalibrationLockSettingFragment.setVisibility(GONE);
                txvCalibrationLockDescriptionSettingFragment.setVisibility(GONE);
            }

            txvChangePairingPasswordSettingFragment.setVisibility(GONE);
            txvChangePairingPasswordDescriptionSettingFragment.setVisibility(GONE);
        }

        if (!mDevice.getMemberAdminStatus()) {
            txvSerialNumberSettingFragment.setVisibility(GONE);
            txvSerialNumberDescriptionSettingFragment.setVisibility(GONE);
        }

        if (isUserLoggedIn()) {
            txvResetDeviceSettingFragment.setVisibility(GONE);
            txvResetDeviceDescriptionSettingFragment.setVisibility(GONE);
        } else {
            txvRemoveDeviceSettingFragment.setVisibility(GONE);
            txvRemoveDeviceDescriptionSettingFragment.setVisibility(GONE);
        }
    }

    private void handleDoorInstallation() {
        if (mDevice.getUserAdminStatus() != DataHelper.MEMBER_STATUS_NOT_ADMIN) {
            mDoorInstallationDialog = new Dialog(requireNonNull(getContext()));
            mDoorInstallationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mDoorInstallationDialog.setContentView(R.layout.dialog_device_setting);
            mDoorInstallationDialog.setCancelable(false);

            setTypeface((TextView) mDoorInstallationDialog.findViewById(R.id.txv_title_door_installation_dialog_device_setting), "roboto_medium");

            RadioGroup rdgDoorInstallationDialogDoorInstallation = mDoorInstallationDialog.findViewById(R.id.rdg_door_installation_dialog_device_setting);

            ((RadioButton) rdgDoorInstallationDialogDoorInstallation.getChildAt(mDevice.getDoorInstallation() ? 0 : 1)).setChecked(true);

            Button btnCancelDialogDoorInstallation = mDoorInstallationDialog.findViewById(R.id.btn_cancel_dialog_device_setting);
            Button btnApplyDialogDoorInstallation = mDoorInstallationDialog.findViewById(R.id.btn_apply_dialog_device_setting);

            btnCancelDialogDoorInstallation.setOnClickListener(v -> {
                mDoorInstallationDialog.dismiss();
                mDoorInstallationDialog = null;
            });

            btnApplyDialogDoorInstallation.setOnClickListener(v -> {
                if (isConnectedToBleDevice) {
                    openProgressDialog(mFragment.getContext(), null, "Door installation setup ...");
                    mDeviceViewModel.setDoorInstallation(
                            mFragment,
                            findSelectedDoorInstallationOption(rdgDoorInstallationDialogDoorInstallation));
                } else
                    showToast("This is not available in BLE mqttDisconnect mode!");
            });
        }

        mDoorInstallationDialog.show();
        requireNonNull(mDoorInstallationDialog.getWindow())
                .setAttributes(ViewHelper.getDialogLayoutParams(mDoorInstallationDialog));
    }

    private void handleInitializeCalibrationLock() {
        if (mDevice.getUserAdminStatus() != DataHelper.MEMBER_STATUS_NOT_ADMIN) {
            mInitializeCalibrationLockDialog = new Dialog(requireNonNull(getContext()));
            mInitializeCalibrationLockDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mInitializeCalibrationLockDialog.setContentView(R.layout.dialog_initialize_calibration_lock_confirmation);
            mInitializeCalibrationLockDialog.setCancelable(false);

            setTypeface((TextView) mInitializeCalibrationLockDialog.findViewById(R.id.txv_title_dialog_initialize_calibration_lock_confirmation), "roboto_medium");

            Button btnCancelDialogInitializeCalibrationLock = mInitializeCalibrationLockDialog.findViewById(R.id.btn_cancel_dialog_initialize_calibration_lock_confirmation);
            Button btnStartDialogInitializeCalibrationLock = mInitializeCalibrationLockDialog.findViewById(R.id.btn_start_dialog_initialize_calibration_lock_confirmation);

            btnCancelDialogInitializeCalibrationLock.setOnClickListener(v -> {
                mInitializeCalibrationLockDialog.dismiss();
                mInitializeCalibrationLockDialog = null;
            });

            btnStartDialogInitializeCalibrationLock.setOnClickListener(v -> {
                if (isConnectedToBleDevice) {
                    openProgressDialog(mFragment.getContext(), null, "Initialize calibration lock ...");
                    DeviceSettingFragment.this.mDeviceViewModel.initializeCalibrationLock(this);
                } else
                    showToast("This is not available in BLE mqttDisconnect mode!");
            });
        }

        mInitializeCalibrationLockDialog.show();
        requireNonNull(mInitializeCalibrationLockDialog.getWindow())
                .setAttributes(ViewHelper.getDialogLayoutParams(mInitializeCalibrationLockDialog));
    }

    private void handleCalibrationLockSteps() {
        if (mDevice.getUserAdminStatus() != DataHelper.MEMBER_STATUS_NOT_ADMIN) {
            mCalibrationLockDialog = new Dialog(requireNonNull(mFragment.getContext()));
            mCalibrationLockDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mCalibrationLockDialog.setContentView(R.layout.dialog_calibration_lock);
            mCalibrationLockDialog.setCancelable(false);

            setTypeface((TextView) mCalibrationLockDialog.findViewById(R.id.txv_title_dialog_calibration_lock), "roboto_medium");

            ConstraintLayout ctlSetIdlePositionDialogCalibrationLock = mCalibrationLockDialog.findViewById(R.id.ctl_set_idle_position_dialog_calibration_lock);
            ConstraintLayout ctlSetLatchPositionDialogCalibrationLock = mCalibrationLockDialog.findViewById(R.id.ctl_set_latch_position_dialog_calibration_lock);
            ConstraintLayout ctlSetLockPositionDialogCalibrationLock = mCalibrationLockDialog.findViewById(R.id.ctl_set_lock_position_dialog_calibration_lock);

            Button btnDoneDialogInitializeCalibrationLock = mCalibrationLockDialog.findViewById(R.id.btn_done_dialog_calibration_lock);
            Button btnCancelDialogInitializeCalibrationLock = mCalibrationLockDialog.findViewById(R.id.btn_cancel_dialog_calibration_lock);

            btnDoneDialogInitializeCalibrationLock.setVisibility(GONE);
            btnDoneDialogInitializeCalibrationLock.setOnClickListener(this);

            ctlSetIdlePositionDialogCalibrationLock.setOnClickListener(v -> {
                handleLockPositions("Idle");
            });
            ctlSetLatchPositionDialogCalibrationLock.setOnClickListener(v -> {
                handleLockPositions("Latch");
            });
            ctlSetLockPositionDialogCalibrationLock.setOnClickListener(v -> {
                handleLockPositions("Lock");
            });

            btnCancelDialogInitializeCalibrationLock.setOnClickListener(v -> {
                dismissAllConfigDialogs();
            });
        }

        if (mCalibrationLockDialog != null) {
            mCalibrationLockDialog.show();
            requireNonNull(mCalibrationLockDialog.getWindow())
                    .setAttributes(ViewHelper.getDialogLayoutParams(mCalibrationLockDialog));
        }
    }

    private void handleLockPositions(String position) {
        if (mDevice.getUserAdminStatus() != DataHelper.MEMBER_STATUS_NOT_ADMIN) {
            mLockPositionsDialog = new Dialog(requireNonNull(mFragment.getContext()));
            mLockPositionsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mLockPositionsDialog.setContentView(R.layout.dialog_confirmation_lock_position);
            mLockPositionsDialog.setCancelable(false);

            setTypeface((TextView) mLockPositionsDialog.findViewById(R.id.txv_title_dialog_calibration_lock), "roboto_medium");

            TextView txvMessageDialogConfirmationLockPosition = mLockPositionsDialog.findViewById(R.id.txv_message_dialog_confirmation_lock_position);

            Button btnCancelDialogConfirmationLockPosition = mLockPositionsDialog.findViewById(R.id.btn_cancel_dialog_confirmation_lock_position);
            Button btnApplyDialogConfirmationLockPosition = mLockPositionsDialog.findViewById(R.id.btn_apply_dialog_confirmation_lock_position);

            switch (position) {
                case "Idle":
                    txvMessageDialogConfirmationLockPosition.setText(getString(R.string.dialog_text_view_message_confirmation_lock_position_idle));
                    break;
                case "Latch":
                    txvMessageDialogConfirmationLockPosition.setText(getString(R.string.dialog_text_view_message_confirmation_lock_position_latch));
                    break;
                case "Lock":
                    txvMessageDialogConfirmationLockPosition.setText(getString(R.string.dialog_text_view_message_confirmation_lock_position_lock));
                    break;
            }

            btnCancelDialogConfirmationLockPosition.setOnClickListener(v -> {
                mLockPositionsDialog.dismiss();
                mLockPositionsDialog = null;
            });

            btnApplyDialogConfirmationLockPosition.setOnClickListener(v -> {
                switch (position) {
                    case "Idle":
                        openProgressDialog(mFragment.getContext(), null, "Set Idle position ...");
                        DeviceSettingFragment.this.mDeviceViewModel.applyCalibrationIdlePosition(this);
                        break;
                    case "Latch":
                        openProgressDialog(mFragment.getContext(), null, "Set Latch position ...");
                        DeviceSettingFragment.this.mDeviceViewModel.applyCalibrationLatchPosition(this);
                        break;
                    case "Lock":
                        openProgressDialog(mFragment.getContext(), null, "Set Lock position ...");
                        DeviceSettingFragment.this.mDeviceViewModel.applyCalibrationLockPosition(this);
                        break;
                }
            });
        }

        mLockPositionsDialog.show();
        requireNonNull(mLockPositionsDialog.getWindow())
                .setAttributes(ViewHelper.getDialogLayoutParams(mLockPositionsDialog));
    }

    private void handleLockPositionStatusImage(String position, boolean status) {
        if (mCalibrationLockDialog != null) {
            switch (position) {
                case "Idle":
                    ViewHelper.setLockPositionStatusImage(
                            mCalibrationLockDialog.findViewById(R.id.img_idle_position_status_dialog_calibration_lock), status);
                    break;
                case "Latch":
                    ViewHelper.setLockPositionStatusImage(
                            mCalibrationLockDialog.findViewById(R.id.img_latch_position_status_dialog_calibration_lock), status);
                    break;
                case "Lock":
                    ViewHelper.setLockPositionStatusImage(
                            mCalibrationLockDialog.findViewById(R.id.img_lock_position_status_dialog_calibration_lock), status);
                    break;
            }
        }
    }

    private void handleResetLock() {
        mResetLockDialog = new Dialog(requireNonNull(getContext()));
        mResetLockDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mResetLockDialog.setContentView(R.layout.dialog_reset_lock);
        mResetLockDialog.setCancelable(false);

        setTypeface((TextView) mResetLockDialog.findViewById(R.id.txv_title_dialog_reset_lock), "roboto_medium");

        Button btnCancelDialogResetLock =
                mResetLockDialog.findViewById(R.id.btn_cancel_dialog_reset_lock);
        Button btnResetDialogResetLock =
                mResetLockDialog.findViewById(R.id.btn_reset_dialog_reset_lock);

        btnCancelDialogResetLock.setOnClickListener(v -> {
            mResetLockDialog.dismiss();
            mResetLockDialog = null;
        });

        btnResetDialogResetLock.setOnClickListener(v -> {
            openProgressDialog(getContext(), null, "Reset Device ...");
            mDeviceViewModel.resetBleDevice(mFragment);
        });

        mResetLockDialog.show();
        requireNonNull(mResetLockDialog.getWindow())
                .setAttributes(ViewHelper.getDialogLayoutParams(mResetLockDialog));
    }

    private void handleRemoveDevice() {
        mRemoveLockDialog = new Dialog(requireNonNull(getContext()));
        mRemoveLockDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mRemoveLockDialog.setContentView(R.layout.dialog_remove_device);
        mRemoveLockDialog.setCancelable(false);

        setTypeface((TextView) mRemoveLockDialog.findViewById(R.id.txv_title_dialog_remove_device), "roboto_medium");

        //TODO when remove any device, must deleteDevice any members of that admin, but now any admin member deleteDevice all members but in future
        //TODO any admin just can deleteDevice his/her members

//            CheckBox chbRemoveAllMembersDialogRemoveLock =
//                    mRemoveLockDialog.findViewById(R.id.chb_remove_all_members_dialog_remove_lock);

//            if (mDevice.getAdminMembersCount() == 1) {//TODO I must get this from server
//                chbRemoveAllMembersDialogRemoveLock.setChecked(true);
//                chbRemoveAllMembersDialogRemoveLock.setEnabled(false);
//            }

//            if (mDevice.getUserAdminStatus() == DataHelper.MEMBER_STATUS_NOT_ADMIN) {
//                chbRemoveAllMembersDialogRemoveLock.setChecked(false);
//                chbRemoveAllMembersDialogRemoveLock.setVisibility(View.GONE);
//            }

        Button btnCancelDialogRemoveLock =
                mRemoveLockDialog.findViewById(R.id.btn_cancel_dialog_remove_device);
        Button btnRemoveDialogRemoveLock =
                mRemoveLockDialog.findViewById(R.id.btn_remove_dialog_remove_device);

        btnCancelDialogRemoveLock.setOnClickListener(v -> {
            mRemoveLockDialog.dismiss();
            mRemoveLockDialog = null;
        });

        btnRemoveDialogRemoveLock.setOnClickListener(v -> {
            openProgressDialog(getContext(), null, "Remove device ...");
            DeviceSettingFragment.this.mDeviceViewModel.removeDevice(
                    mFragment,
                    true,
                    mDevice);
        });

        mRemoveLockDialog.show();
        requireNonNull(mRemoveLockDialog.getWindow())
                .setAttributes(ViewHelper.getDialogLayoutParams(mRemoveLockDialog));
    }

    private void handleChangePassword(int changeStatus) {
        switch (changeStatus) {
            case CHANGE_ONLINE_PASSWORD:
                handleDialogChangeOnlinePassword();
                break;
            case CHANGE_PAIRING_PASSWORD:
                handleDialogChangePairingPassword();
                break;
        }
    }

    private void handleDialogChangeOnlinePassword() {
        if (mDevice.getMemberAdminStatus()) {
            mChangeOnlinePasswordDialog = new Dialog(requireNonNull(getContext()));
            mChangeOnlinePasswordDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mChangeOnlinePasswordDialog.setContentView(R.layout.dialog_change_online_password);
            mChangeOnlinePasswordDialog.setCancelable(false);

            setTypeface((TextView) mChangeOnlinePasswordDialog.findViewById(R.id.txv_title_dialog_change_online_password), "roboto_medium");

            TextInputEditText tietOldPasswordDialogChangeOnlinePassword =
                    mChangeOnlinePasswordDialog.findViewById(R.id.tiet_old_password_dialog_change_online_password);
            TextInputEditText tietNewPasswordDialogChangeOnlinePassword =
                    mChangeOnlinePasswordDialog.findViewById(R.id.tiet_new_password_dialog_change_online_password);

            Button btnCancelDialogChangeOnlinePassword =
                    mChangeOnlinePasswordDialog.findViewById(R.id.btn_cancel_dialog_change_online_password);
            Button btnApplyDialogChangeOnlinePassword =
                    mChangeOnlinePasswordDialog.findViewById(R.id.btn_apply_dialog_change_online_password);

            btnCancelDialogChangeOnlinePassword.setOnClickListener(v -> {
                mChangeOnlinePasswordDialog.dismiss();
                mChangeOnlinePasswordDialog = null;
            });

            btnApplyDialogChangeOnlinePassword.setOnClickListener(v -> {
                openProgressDialog(mFragment.getContext(), null, "Change online password ...");
                mDeviceViewModel.changeOnlinePasswordViaBle(mFragment,
                        requireNonNull(tietOldPasswordDialogChangeOnlinePassword.getText()).toString(),
                        requireNonNull(tietNewPasswordDialogChangeOnlinePassword.getText()).toString());
            });
        }

        mChangeOnlinePasswordDialog.show();
        requireNonNull(mChangeOnlinePasswordDialog.getWindow())
                .setAttributes(ViewHelper.getDialogLayoutParams(mChangeOnlinePasswordDialog));
    }

    private void handleDialogChangePairingPassword() {
        if (mDevice.getMemberAdminStatus()) {
            mChangePairingPasswordDialog = new Dialog(requireNonNull(getContext()));
            mChangePairingPasswordDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mChangePairingPasswordDialog.setContentView(R.layout.dialog_change_pairing_password);
            mChangePairingPasswordDialog.setCancelable(false);

            setTypeface((TextView) mChangePairingPasswordDialog.findViewById(R.id.txv_title_dialog_change_pairing_password), "roboto_medium");

            TextInputEditText tietOldPasswordDialogChangePairingPassword =
                    mChangePairingPasswordDialog.findViewById(R.id.tiet_old_password_dialog_change_pairing_password);
            TextInputEditText tietNewPasswordDialogChangePairingPassword =
                    mChangePairingPasswordDialog.findViewById(R.id.tiet_new_password_dialog_change_pairing_password);

            Button btnCancelDialogChangePairingPassword =
                    mChangePairingPasswordDialog.findViewById(R.id.btn_cancel_dialog_change_pairing_password);
            Button btnApplyDialogChangePairingPassword =
                    mChangePairingPasswordDialog.findViewById(R.id.btn_apply_dialog_change_pairing_password);

            btnCancelDialogChangePairingPassword.setOnClickListener(v -> {
                mChangePairingPasswordDialog.dismiss();
                mChangePairingPasswordDialog = null;
            });

            btnApplyDialogChangePairingPassword.setOnClickListener(v -> {
                openProgressDialog(mFragment.getContext(), null, "Change pairing password ...");
                mDeviceViewModel.changePairingPasswordViaBle(mFragment,
                        Integer.valueOf(requireNonNull(tietOldPasswordDialogChangePairingPassword.getText()).toString()),
                        Integer.valueOf(requireNonNull(tietNewPasswordDialogChangePairingPassword.getText()).toString()));
            });
        }

        mChangePairingPasswordDialog.show();
        requireNonNull(mChangePairingPasswordDialog.getWindow())
                .setAttributes(ViewHelper.getDialogLayoutParams(mChangePairingPasswordDialog));
    }

    private boolean findSelectedDoorInstallationOption(RadioGroup radioGroup) {
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.rdb_right_handed_dialog_device_setting:
                return DOOR_INSTALLATION_SETTING_RIGHT_HANDED;
            case R.id.rdb_left_handed_dialog_device_setting:
                return DOOR_INSTALLATION_SETTING_LEFT_HANDED;
            default:
                return true;
        }
    }

    private void handleSetSettingResponse() {
        if (doorInstallationDone) {
            closeProgressDialog();
            if (mDoorInstallationDialog != null) {
                mDoorInstallationDialog.dismiss();
                mDoorInstallationDialog = null;
            }
            mDeviceViewModel.getLockSpecifiedSettingInfoFromBleDevice(this);
            Timber.i("Done");
        }
    }

    private void handleConfigResponse() {
        requireNonNull(DeviceSettingFragment.this.getActivity()).runOnUiThread(() -> {
            if (mCalibrationLockDialog != null)
                mCalibrationLockDialog.findViewById(R.id.btn_done_dialog_calibration_lock).setVisibility(VISIBLE);
        });
    }

    private void dismissAllConfigDialogs() {
        if (mInitializeCalibrationLockDialog != null) {
            mInitializeCalibrationLockDialog.dismiss();
            mInitializeCalibrationLockDialog = null;
        }

        if (mCalibrationLockDialog != null) {
            mCalibrationLockDialog.dismiss();
            mCalibrationLockDialog = null;
        }

        if (mLockPositionsDialog != null) {
            mLockPositionsDialog.dismiss();
            mLockPositionsDialog = null;
        }
    }

    public Device getDevice() {
        return mDevice;
    }
    //endregion Declare Methods
}