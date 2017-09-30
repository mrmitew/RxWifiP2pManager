package com.stetcho.rxwifip2pmanager.app.framework.discovery.view;

import android.content.Context;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pixplicity.sharp.Sharp;
import com.stetcho.rxwifip2pmanager.app.R;
import com.stetcho.rxwifip2pmanager.app.domain.discovery.model.DeviceModel;
import com.stetcho.rxwifip2pmanager.app.framework.discovery.data.DeviceListAdapter;
import com.stetcho.rxwifip2pmanager.data.wifi.RxWifiP2pManager;
import com.stetcho.rxwifip2pmanager.data.wifi.broadcast.factory.WifiP2pBroadcastObservableManagerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Stefan Mitev on 01/07/2015.
 * <p>
 * TODO: There is some sort of separation of concerns, but there is still a mix which will be nice if it is fixed.
 * Possible solution is to implement MVP.
 */
public class DiscoveryActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = DiscoveryActivity.class.getSimpleName();

    /*
     * Views
     */
    @BindView(R.id.lv_devices)
    protected ListView mLvDevices;

    @BindView(R.id.swipeRefreshLayout)
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.iv_discovery_state_not_found)
    protected ImageView mIvDiscoveryStateNotFound;

    @BindView(R.id.vg_discovery_state_not_found)
    protected ViewGroup mVgDiscoveryStateNotFound;

    @BindView(R.id.vg_discovery_state_initial)
    protected ViewGroup mVgDiscoveryStateInitial;

    @BindView(R.id.vg_device_connected)
    protected ViewGroup mVgDeviceConnected;

    @BindView(R.id.tv_text)
    protected TextView mTvText;

    @BindView(R.id.tv_title)
    protected TextView mTvTitle;

    @BindView(R.id.tv_device_found_count)
    protected TextView mTvDeviceFoundCount;

    /*
     * Adapters
     */
    protected DeviceListAdapter mDeviceListAdapter;

    /*
     * Disposable
     */
    private Disposable mRequestConnectionInfoSubscription;
    private Disposable mDiscoverPeersSubscription;

    /*
     * Other instance variables
     */
    private RxWifiP2pManager mRxWifiP2pManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.discovery_activity);
        ButterKnife.bind(this);

        mRxWifiP2pManager =
                new RxWifiP2pManager(
                        getApplicationContext(),
                        (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE),
                        new WifiP2pBroadcastObservableManagerFactory(getApplicationContext()));

        mDeviceListAdapter = new DeviceListAdapter(getApplicationContext(), new ArrayList<>());
        mLvDevices.setAdapter(mDeviceListAdapter);

        // Check if we are connected to another device
        getConnectedToDeviceObservable()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deviceModel -> {
                    if (deviceModel != null) {
                        // We got the information for the device we are connected to
                        setConnectedToDeviceScreen(deviceModel);
                    } else {
                        Snackbar.make(mSwipeRefreshLayout, R.string.unknown_error, Snackbar.LENGTH_SHORT)
                                .show();
                    }
                }, Throwable::printStackTrace);

        mSwipeRefreshLayout.setOnRefreshListener(this);

        // Configure the refreshing colors
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
    }

    private void setWelcomeScreen() {
        mDeviceListAdapter.clearData();
        mTvTitle.setText(R.string.welcome);
        mTvText.setText("");
        mVgDiscoveryStateNotFound.setVisibility(View.GONE);
        mVgDeviceConnected.setVisibility(View.GONE);
        mLvDevices.setVisibility(View.VISIBLE);
        mVgDiscoveryStateInitial.setVisibility(View.VISIBLE);
        mTvDeviceFoundCount.setVisibility(View.GONE);
    }

    private void setNoDevicesFoundScreen() {
        mLvDevices.setVisibility(View.GONE);
        mTvDeviceFoundCount.setVisibility(View.GONE);
        mVgDiscoveryStateInitial.setVisibility(View.GONE);
        mVgDeviceConnected.setVisibility(View.GONE);
        Sharp.loadResource(getResources(), R.raw.ic_error_outline_black_24px)
                .into(mIvDiscoveryStateNotFound);
        mVgDiscoveryStateNotFound.setVisibility(View.VISIBLE);
    }

    private void setFoundNewDevicesScreen(final List<DeviceModel> deviceList) {
        mTvDeviceFoundCount.setText(String.format(getString(R.string.device_found_1d),
                deviceList.size()));
        mVgDiscoveryStateInitial.setVisibility(View.GONE);
        mVgDiscoveryStateNotFound.setVisibility(View.GONE);
        mVgDeviceConnected.setVisibility(View.GONE);
        mLvDevices.setVisibility(View.VISIBLE);
        mTvDeviceFoundCount.setVisibility(View.VISIBLE);

        // Reset the list with previously found devices (if there were any)
        mDeviceListAdapter.clearData();

        ((DeviceListAdapter) mLvDevices.getAdapter()).setData(deviceList);
    }

    private void setConnectedToDeviceScreen(final DeviceModel deviceModel) {
        mTvTitle.setText(deviceModel.getName());
        mLvDevices.setVisibility(View.GONE);
        mVgDiscoveryStateInitial.setVisibility(View.GONE);
        mVgDiscoveryStateNotFound.setVisibility(View.GONE);
        mVgDeviceConnected.setVisibility(View.VISIBLE);
        mTvDeviceFoundCount.setVisibility(View.GONE);
    }

    @Override
    public void onRefresh() {
        mDiscoverPeersSubscription = mRxWifiP2pManager.discoverAndRequestPeersList()
                .timeout(5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .map(deviceList -> deviceList.getDeviceList().stream()
                        .map(device -> new DeviceModel(device.deviceName, device.deviceAddress))
                        .collect(Collectors.toList()))
                .subscribe(deviceModels -> {
                    // We got a list with nearby p2p devices

                    // Stop refreshing
                    stopDiscoveringUi();

                    // Change the screen state
                    setFoundNewDevicesScreen(deviceModels);
                }, e -> {
                    if (e instanceof TimeoutException) {
                        // No devices were found, or the discovery took too long time

                        // Stop refreshing
                        stopDiscoveringUi();

                        setNoDevicesFoundScreen();
                    } else {
                        // Ooops, something went wrong here
                        e.printStackTrace();
                    }
                });
    }

    @OnItemClick(R.id.lv_devices)
    protected void onDeviceClick(int position) {
        final DeviceModel deviceModel = mDeviceListAdapter.getItem(position);
        mRxWifiP2pManager
                .connect(mRxWifiP2pManager.createConfig(deviceModel.getAddress(), WpsInfo.PBC))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    // Change screen state
                    setConnectedToDeviceScreen(deviceModel);
                });
    }

    @OnClick(R.id.btn_disconnect)
    protected void onBtnDisconnectClick() {
        mRxWifiP2pManager.disconnect()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    // Change screen state. Restore to the default - welcome screen.
                    setWelcomeScreen();
                    Snackbar.make(mSwipeRefreshLayout, R.string.disconnected, Snackbar.LENGTH_SHORT).show();
                });
    }

    @OnClick(R.id.btn_request_connection_info)
    protected void onBtnRequestConnectionInfoClick() {
        mRequestConnectionInfoSubscription = mRxWifiP2pManager.requestConnectionInfo()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(wifiP2pInfo -> mTvText.setText(wifiP2pInfo.toString()));
    }

    private Single<DeviceModel> getConnectedToDeviceObservable() {
        return mRxWifiP2pManager.requestConnectionInfo()
                .flatMap(wifiP2pInfo -> Single.create(emitter -> {
                    if (wifiP2pInfo == null || wifiP2pInfo.groupOwnerAddress == null) {
                        emitter.onError(new RuntimeException("Not connected"));
                    } else {
                        WifiP2pDevice device = mRxWifiP2pManager.requestPeers().blockingFirst();
                        emitter.onSuccess(new DeviceModel(device.deviceName, device.deviceAddress));
                    }
                }));
    }

    @Override
    protected void onStop() {
        super.onStop();
        safeDispose();
    }

    private void stopDiscoveringUi() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void safeDispose() {
        safeDispose(mDiscoverPeersSubscription);
        safeDispose(mRequestConnectionInfoSubscription);
    }

    private void safeDispose(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
