package com.stetcho.rxwifip2pmanager.app.adapter.mapper;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;

import com.stetcho.rxwifip2pmanager.app.domain.discovery.model.DeviceModel;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Func1;

/**
 * Created by Stefan Mitev on 01/07/2015.
 *
 * Implementation of {@link rx.Observable#map(Func1)} that allows mapping a {@link WifiP2pDeviceList}
 * to a list with {@link DeviceModel}.
 */
public class DeviceModelMapper implements Func1<WifiP2pDeviceList, List<DeviceModel>> {
    public DeviceModelMapper() {
    }

    @Override
    public List<DeviceModel> call(final WifiP2pDeviceList wifiP2pDeviceList) {
        List<DeviceModel> deviceModelList =
                new ArrayList<>(wifiP2pDeviceList.getDeviceList().size());
        for (final WifiP2pDevice wifiP2pDevice : wifiP2pDeviceList.getDeviceList()) {
            deviceModelList.add(DeviceModelMapper.Single.map(wifiP2pDevice));
        }
        return deviceModelList;
    }

    /**
     * Implementation of {@link rx.Observable#map(Func1)} that allows mapping a
     * {@link WifiP2pDevice} to a {@link DeviceModel}.
     */
    public static class Single implements Func1<WifiP2pDevice, DeviceModel> {
        public Single() {
        }

        @Override
        public DeviceModel call(final WifiP2pDevice wifiP2pDevice) {
            return map(wifiP2pDevice);
        }

        /**
         * Method used to map a single {@link WifiP2pDevice} to a {@link DeviceModel}
         * @param wifiP2pDevice
         * @return a mapped instance of {@link DeviceModel} from {@link WifiP2pDevice}
         */
        static DeviceModel map(WifiP2pDevice wifiP2pDevice) {
            return new DeviceModel(wifiP2pDevice.deviceName, wifiP2pDevice.deviceAddress);
        }
    }
}
