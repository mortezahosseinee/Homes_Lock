package com.projects.company.homes_lock.ui.proservices.fragment.ProServicesList;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.projects.company.homes_lock.R;
import com.projects.company.homes_lock.ui.proservices.activity.ProServicesActivityContract;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProServicesListFragment extends Fragment implements ProServicesListFragmentContract.mMvpView {

    //region Declare Constants
    //endregion Declare Constants

    //region Declare Views
    //endregion Declare Views

    //region Declare Variables
    //endregion Declare Variables

    //region Declare Objects
    ProServicesListFragmentPresenter mProServicesListFragmentPresenter;
    //endregion Declare Objects

    public ProServicesListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //region Initialize Variables
        //endregion Initialize Variables

        //region Initialize Objects
        mProServicesListFragmentPresenter = new ProServicesListFragmentPresenter(this);
        //endregion Initialize Objects
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pro_services_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //region Initialize Views
        //endregion Initialize Views

        //region Setup Views
        //endregion Setup Views
    }

    //region Declare Methods
    //endregion Declare Methods
}
