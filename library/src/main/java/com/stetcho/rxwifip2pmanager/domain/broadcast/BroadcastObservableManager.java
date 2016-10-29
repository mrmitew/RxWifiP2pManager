package com.stetcho.rxwifip2pmanager.domain.broadcast;

import android.content.Intent;

import rx.Observable;

/**
 * Created by Stefan Mitev on 01/07/2015.
 *
 * Interface for working with {@link Observable} that emits {@link Intent}s when a broadcast
 * receiver receives and event.
 */
public interface BroadcastObservableManager {
    /**
     * Factory for creating {@link BroadcastObservableManager} classes
     */
    interface Factory {
        BroadcastObservableManager create();
    }
    Observable<Intent> getBroadcastObservable();
}
