package com.projects.company.homes_lock.utils.helper;

import com.google.gson.Gson;
import com.projects.company.homes_lock.models.datamodels.ble.ScannedDeviceModel;
import com.projects.company.homes_lock.models.datamodels.response.DeviceModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DataHelper {

    public static final int ERROR_CODE_BLE_DEVICE_NOT_SUPPORT_BLE = 1000000;
    public static final int ERROR_CODE_BLE_NOT_ENABLED = 1000001;

    public static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;
    public static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 2;

    public static boolean isInstanceOfList(Object object, String className) {
        try {
            if (object instanceof List)
                for (Object obj : (List) object) {
                    if (Class.forName(className).isInstance(obj))
                        continue;
                    else
                        return false;
                }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean containsValueInListOfObjects(List<?> listOfObjects, String substring) {
        // This method search a list of object's Model (every model has getSpecialValue method)
        // Return TRUE if find special substring else return false
        // See com.projects.company.homes_lock.models.datamodels.ble.ScannedDeviceModel class
        if (listOfObjects.size() == 0)
            return false;
        else if (listOfObjects.get(0) instanceof ScannedDeviceModel)
            for (int i = 0; i < listOfObjects.size(); i++)
                if (((ScannedDeviceModel) listOfObjects.get(i)).getSpecialValue().equals(substring))
                    return true;

        return false;
    }

    public static ArrayList<String> convertListOfObjectsToArrayList(List<?> listOfObjects) {
        ArrayList<String> mArray = new ArrayList<>();
        if (listOfObjects.size() == 0)
            return mArray;
        else if (listOfObjects.get(0) instanceof ScannedDeviceModel)
            for (int i = 0; i < listOfObjects.size(); i++)
                mArray.add(((ScannedDeviceModel) listOfObjects.get(i)).getName());

        return mArray;
    }

    public static Object convertJsonToObject(String data, String className){
        try {
            return new Gson().fromJson(data, (Type) Class.forName(className));
        } catch (Exception e) {
            e.printStackTrace();
            return new DeviceModel();
        }
    }
}
