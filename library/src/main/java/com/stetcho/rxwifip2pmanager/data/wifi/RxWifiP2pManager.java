package com.stetcho.rxwifip2pmanager.data.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;

import com.stetcho.rxwifip2pmanager.domain.broadcast.BroadcastObservableManager;

import rx.Completable;
import rx.CompletableSubscriber;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Func0;
import rx.functions.Func1;

import static android.net.wifi.p2p.WifiP2pManager.BUSY;
import static android.net.wifi.p2p.WifiP2pManager.ERROR;
import static android.net.wifi.p2p.WifiP2pManager.P2P_UNSUPPORTED;

/**
 * Created by Stefan Mitev on 01/07/2015.
 *
 * Wrapper class for {@link WifiP2pManager}, using RxJava
 * ({@link "https://github.com/ReactiveX/RxJava"}).
 */
public class RxWifiP2pManager {
    private final WifiP2pManager mWifiP2pManager;
    private final WifiP2pManager.Channel mChannel;
    private final BroadcastObservableManager.Factory mIntentObservableFactory;

    /**
     * Returns the class provides the API for managing Wi-Fi peer-to-peer connectivity.
     *
     * @return {@link WifiP2pManager}
     */
    public WifiP2pManager getWifiP2pManager() {
        return mWifiP2pManager;
    }

    /**
     * A channel that represents the connection between the application and the Wifi p2p framework
     *
     * @return {@link WifiP2pManager.Channel}
     */
    public WifiP2pManager.Channel getChannel() {
        return mChannel;
    }

    /**
     * The main and only (for now) constructor. External dependencies are injected from here.
     *
     * @param context                 an instance of the application's context
     * @param wifiP2pManager          an instance of {@link WifiP2pManager}
     * @param intentObservableFactory The factory allows you to have more control over the way
     *                                broadcasts emissions are handled and choose to use either the
     *                                default way, provided by the library, or plug your own
     *                                implementation.
     */
    public RxWifiP2pManager(Context context,
                            WifiP2pManager wifiP2pManager,
                            BroadcastObservableManager.Factory intentObservableFactory) {
        mWifiP2pManager = wifiP2pManager;
        mChannel = mWifiP2pManager.initialize(context, Looper.getMainLooper(), null);
        mIntentObservableFactory = intentObservableFactory;
    }

    /**
     * Requests all current peers
     *
     * @return an Observable that emits each {@link WifiP2pDevice} from the source
     * {@link WifiP2pDeviceList}
     */
    public Observable<WifiP2pDevice> requestPeers() {
        return requestPeersList()
                .toObservable()
                .flatMap(new Func1<WifiP2pDeviceList, Observable<WifiP2pDevice>>() {
                    @Override
                    public Observable<WifiP2pDevice> call(final WifiP2pDeviceList deviceList) {
                        return Observable.from(deviceList.getDeviceList());
                    }
                });
    }

    /**
     * Initiates a peer discovery by scanning for available Wi-Fi peers for the purpose of
     * establishing a connection.
     *
     * @return a {@link Completable} observable that indicates whether the discovery was successful
     * or not
     */
    public Completable discoverPeers() {
        return Completable.create(new Completable.OnSubscribe() {
            @Override
            public void call(final CompletableSubscriber subscriber) {
                final WifiP2pManager.ActionListener discoverPeersListener =
                        new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                subscriber.onCompleted();
                            }

                            @Override
                            public void onFailure(int reasonCode) {
                                subscriber.onError(
                                        new RuntimeException("Error: " +
                                                getErrorString(reasonCode)));
                            }
                        };

                mWifiP2pManager.discoverPeers(mChannel, discoverPeersListener);
            }
        });
    }

    /**
     * Initiates peer discovery by scanning for available Wi-Fi peers for the purpose of
     * establishing a connection.
     * This method is created to be used for composing with other {@link Single} or
     * {@link Observable} observables.
     *
     * @return a {@link Single} observable that emits a null value for successful discovery, or
     * throws an error
     */
    public Single<Void> singleDiscoverPeers() {
        return Single.create(new Single.OnSubscribe<Void>() {
            @Override
            public void call(final SingleSubscriber<? super Void> subscriber) {
                final WifiP2pManager.ActionListener discoverPeersListener =
                        new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                if (!subscriber.isUnsubscribed()) {
                                    subscriber.onSuccess(null);
                                }
                            }

                            @Override
                            public void onFailure(int reasonCode) {
                                subscriber.onError(
                                        new RuntimeException("Error: " +
                                                getErrorString(reasonCode)));
                            }
                        };

                mWifiP2pManager.discoverPeers(mChannel, discoverPeersListener);
            }
        });
    }

    /**
     * Initiates a connection request to a peer. After a successful group formation, you might use
     * {@link RxWifiP2pManager#requestConnectionInfo} to fetch the connection details.
     *
     * @param config <p>
     *               The configuration for setting up a new Wi-Fi p2p connection.
     *               Use {@link RxWifiP2pManager#createConfig(String, int)} to create one.
     *               </p>
     * @return a {@link Completable} observable that indicates completion upon successful connection
     */
    public Completable connect(final WifiP2pConfig config) {
        return Completable.create(new Completable.OnSubscribe() {
            @Override
            public void call(final CompletableSubscriber subscriber) {
                mWifiP2pManager.connect(mChannel, config,
                        new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                subscriber.onCompleted();
                            }

                            @Override
                            public void onFailure(int reason) {
                                subscriber.onError(
                                        new RuntimeException("Error: " + getErrorString(reason)));
                            }
                        });
            }
        });
    }

    /**
     * Fetches information about the current connection.
     * The connection info {@link WifiP2pInfo} contains the address of the group owner
     * groupOwnerAddress and a flag isGroupOwner to indicate if the current device is a p2p group
     * owner. A p2p client can thus communicate with the p2p group owner through a socket
     * connection.
     *
     * @return a {@link Single} observable that emits {@link WifiP2pInfo}
     */
    public Single<WifiP2pInfo> requestConnectionInfo() {
        return Single.defer(new Func0<Single<WifiP2pInfo>>() {
            @Override
            public Single<WifiP2pInfo> call() {
                return Single.create(new Single.OnSubscribe<WifiP2pInfo>() {
                    @Override
                    public void call(final SingleSubscriber<? super WifiP2pInfo> subscriber) {
                        mWifiP2pManager.requestConnectionInfo(mChannel,
                                new WifiP2pManager.ConnectionInfoListener() {
                                    @Override
                                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                                        if (!subscriber.isUnsubscribed()) {
                                            subscriber.onSuccess(info);
                                        }
                                    }
                                });
                    }
                });
            }
        });
    }

    /**
     * Requests a list with all current nearby p2p devices
     *
     * @return a {@link Observable} that emits {@link WifiP2pDeviceList}
     */
    public Single<WifiP2pDeviceList> requestPeersList() {
        return Single.defer(new Func0<Single<WifiP2pDeviceList>>() {
            @Override
            public Single<WifiP2pDeviceList> call() {
                return Single.create(new Single.OnSubscribe<WifiP2pDeviceList>() {
                    @Override
                    public void call(final SingleSubscriber<? super WifiP2pDeviceList> subscriber) {
                        WifiP2pManager.PeerListListener listener =
                                new WifiP2pManager.PeerListListener() {
                                    @Override
                                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                                        if (!subscriber.isUnsubscribed()) {
                                            subscriber.onSuccess(peers);
                                        }
                                    }
                                };
                        mWifiP2pManager.requestPeers(mChannel, listener);
                    }
                });
            }
        });
    }

    /**
     * Initiates a peer discovery and looks for nearby devices.
     *
     * @return a {@link Single} observable that emits a list with all discovered nearby devices
     */
    public Single<WifiP2pDeviceList> discoverAndRequestPeersList() {
        return Single.defer(new Func0<Single<WifiP2pDeviceList>>() {
            @Override
            public Single<WifiP2pDeviceList> call() {
                return singleDiscoverPeers()
                        .compose(listenForNewPeersTransformer())
                        .flatMap(new Func1<Intent, Single<WifiP2pDeviceList>>() {
                            @Override
                            public Single<WifiP2pDeviceList> call(final Intent intent) {
                                return requestPeersList();
                            }
                        });
            }
        });
    }

    /**
     * Initiates a peer discovery and looks for nearby devices.
     *
     * @return a {@link Observable} observable that emits {@link WifiP2pDevice} items from its
     * source {@link WifiP2pDeviceList#getDeviceList()}
     */
    public Observable<WifiP2pDevice> discoverAndRequestPeers() {
        return discoverAndRequestPeersList()
                .toObservable()
                .flatMap(new Func1<WifiP2pDeviceList, Observable<WifiP2pDevice>>() {
                    @Override
                    public Observable<WifiP2pDevice> call(final WifiP2pDeviceList p2pDeviceList) {
                        return Observable.from(p2pDeviceList.getDeviceList());
                    }
                });
    }

    /**
     * Transformer function used internally to transform {@link Single} observable to
     * {@link Single<Intent>} as it listens for {@link WifiP2pManager#WIFI_P2P_PEERS_CHANGED_ACTION}
     * broadcast event.
     *
     * @return a {@link Single} observable that emits the intent which indicated that p2p peers
     * changed
     */
    private Single.Transformer<Void, Intent> listenForNewPeersTransformer() {
        return new Single.Transformer<Void, Intent>() {
            @Override
            public Single<Intent> call(final Single<Void> voidSingle) {
                return voidSingle.flatMap(new Func1<Void, Single<Intent>>() {
                    @Override
                    public Single<Intent> call(final Void aVoid) {
                        return mIntentObservableFactory
                                .create()
                                .getBroadcastObservable()
                                .filter(new Func1<Intent, Boolean>() {
                                    @Override
                                    public Boolean call(Intent intent) {
                                        if (intent == null) {
                                            return false;
                                        }
                                        // Filter the intents by action as we are interested only in
                                        // WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION
                                        return intent.getAction().equals(
                                                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
                                    }
                                })
                                .take(1) // Sometimes we get two broadcasts, so let's emit only one.
                                .toSingle();
                    }
                });
            }
        };
    }

    /**
     * Removes the group for the current channel {@link WifiP2pManager.Channel}.
     *
     * @return a {@link Completable} that indicates whether removing the group for the current
     * channel
     * {@link WifiP2pManager.Channel} was successful or not
     */
    public Completable disconnect() {
        return Completable.create(new Completable.OnSubscribe() {
            @Override
            public void call(final CompletableSubscriber subscriber) {
                mWifiP2pManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                    @Override
                    public void onGroupInfoAvailable(WifiP2pGroup group) {
                        if (group != null && group.isGroupOwner()) {
                            mWifiP2pManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    subscriber.onCompleted();
                                }

                                @Override
                                public void onFailure(int reason) {
                                    subscriber.onError(new RuntimeException("Error: " +
                                            getErrorString(reason)));
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    /**
     * Creates a {@link WifiP2pConfig} configuration for setting up a Wi-Fi p2p connection
     *
     * @param deviceAddress Device MAC Address
     * @param wpsInfoSetup  <p>
     *                      {@link WifiP2pConfig#wps}'s setup field.
     *                      Example options:
     *                      {@link WpsInfo#KEYPAD}
     *                      {@link WpsInfo#PBC}
     *                      {@link WpsInfo#DISPLAY}
     *                      {@link WpsInfo#LABEL}
     *                      </p>
     * @return a {@link WifiP2pConfig}, containing Wi-Fi p2p configuration for setting up a
     * connection
     */
    public WifiP2pConfig createConfig(String deviceAddress, int wpsInfoSetup) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = deviceAddress;
        config.wps.setup = wpsInfoSetup;
        config.groupOwnerIntent = 15;
        return config;
    }

    /**
     * Converts the error code, returned when peer discovery fails
     *
     * @param errorCode See {@link WifiP2pManager.ActionListener#onFailure(int)}
     * @return the reason of a failure by given error code as a string
     */
    private String getErrorString(int errorCode) {
        switch (errorCode) {
            case BUSY:
                return "Busy";

            case ERROR:
                return "Error";

            case P2P_UNSUPPORTED:
                return "P2P Unsupported";

            default:
                return "Unknown (code: " + errorCode + ")";
        }
    }
}
