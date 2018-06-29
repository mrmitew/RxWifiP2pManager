package com.stetcho.rxwifip2pmanager.data.wifi.broadcast;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;

import com.cantrowitz.rxbroadcast.RxBroadcast;
import com.stetcho.rxwifip2pmanager.domain.broadcast.BroadcastObservableManager;

import org.reactivestreams.Subscriber;

import io.reactivex.Observable;

/**
 * Created by Stefan Mitev on 01/07/2015.
 * <p>
 * This concrete implementation of {@link BroadcastObservableManager} uses {@link RxBroadcast} to
 * create a broadcast receiver, wrapped with RxJava, to emit {@link Intent}s every time there is
 * an intent concerning WiFi P2P connectivity.
 * <p>
 * Note that the broadcast receiver will be unregistered as soon as {@link Subscriber}
 * unsubscribes from {@link #mBroadcastObservable}.
 */
public class WifiP2PBroadcastObservableManager implements BroadcastObservableManager {
    private final Observable<Intent> mBroadcastObservable;

    @Override
    public Observable<Intent> getBroadcastObservable() {
        return mBroadcastObservable;
    }

    public WifiP2PBroadcastObservableManager(Context context) {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mBroadcastObservable = RxBroadcast.fromBroadcast(context, intentFilter);
    }
}
