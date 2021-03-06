package com.projects.company.homes_lock.repositories.remote;

import com.projects.company.homes_lock.models.datamodels.response.FailureModel;

public interface NetworkListener {
    interface SingleNetworkListener<T> {
        void onResponse(T response);

        void onSingleNetworkListenerFailure(T response);
    }

    interface ListNetworkListener<T> {
        void onResponse(T response);

        void onListNetworkListenerFailure(FailureModel response);
    }
}
