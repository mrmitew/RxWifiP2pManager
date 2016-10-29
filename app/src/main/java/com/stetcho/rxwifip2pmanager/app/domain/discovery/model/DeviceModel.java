package com.stetcho.rxwifip2pmanager.app.domain.discovery.model;

/**
 * Created by Stefan Mitev on 01/07/2015.
 */
public class DeviceModel {
    private String mName;
    private String mAddress;

    public DeviceModel(final String name, final String address) {
        mName = name;
        mAddress = address;
    }

    public String getName() {
        return mName;
    }


    public String getAddress() {
        return mAddress;
    }

    public void setName(String value) {
        mName = value;
    }


    public void setAddress(String value) {
        mAddress = value;
    }

    @Override
    public String toString() {
        return "DeviceModel{" +
                "mName='" + mName + '\'' +
                ", mAddress='" + mAddress + '\'' +
                '}';
    }
}
