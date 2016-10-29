package com.stetcho.rxwifip2pmanager.data.wifi.broadcast.factory;

import android.content.Context;

import com.stetcho.rxwifip2pmanager.data.wifi.broadcast.WifiP2PBroadcastObservableManager;
import com.stetcho.rxwifip2pmanager.domain.broadcast.BroadcastObservableManager;


/**
 * Created by Stefan Mitev on 01/07/2015.
 *
 * This concrete implementation of {@link BroadcastObservableManager.Factory} takes care of
 * instantiation of new {@link WifiP2PBroadcastObservableManager}s.
 */
public class WifiP2pBroadcastObservableManagerFactory
        implements BroadcastObservableManager.Factory {
    private final Context mContext;

    public WifiP2pBroadcastObservableManagerFactory(final Context context) {
        mContext = context;
    }

    @Override
    public WifiP2PBroadcastObservableManager create() {
        return new WifiP2PBroadcastObservableManager(mContext);
    }
}
