package com.stetcho.rxwifip2pmanager.app.adapter.mapper;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;

import rx.Observable;
import rx.Single;
import rx.functions.Func1;

/**
 * Created by Stefan Mitev on 01/07/2015.
 *
 * Implementation of {@link rx.Observable#map(Func1)} that allows mapping a {@link WifiP2pDeviceList}
 * to {@link Single<WifiP2pDevice>}.
 */
public class WifiP2pSingleDeviceMapper implements Func1<WifiP2pDeviceList, Single<? extends WifiP2pDevice>> {
    @Override
    public Single<? extends WifiP2pDevice> call(final WifiP2pDeviceList wifiP2pDeviceList) {
        return Observable.from(wifiP2pDeviceList.getDeviceList())
                .limit(1)
                .toSingle();
    }
}
