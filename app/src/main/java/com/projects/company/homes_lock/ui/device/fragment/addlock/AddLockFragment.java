package com.projects.company.homes_lock.ui.device.fragment.addlock;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.projects.company.homes_lock.R;
import com.projects.company.homes_lock.base.BaseFragment;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 */
public class AddLockFragment extends BaseFragment implements IAddLockFragment {

    //region Declare Constants
    //endregion Declare Constants

    //region Declare Views
    //endregion Declare Views

    //region Declare Variables
    //endregion Declare Variables

    //region Declare Objects
    //endregion Declare Objects

    public AddLockFragment() {
    }

    public static AddLockFragment newInstance() {
        return new AddLockFragment();
    }

    //region Main CallBacks
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //region Initialize Variables
        //endregion Initialize Variables

        //region Initialize Objects
        //endregion Initialize Objects
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_lock, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //region Initialize Views
        //endregion Initialize Views

        //region Setup Views
        //endregion Setup Views
    }
    //endregion Main CallBacks

    //region Declare Methods
    //endregion Declare Methods
}
