package com.stetcho.rxwifip2pmanager.app.framework.discovery.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.stetcho.rxwifip2pmanager.app.R;
import com.stetcho.rxwifip2pmanager.app.domain.discovery.model.DeviceModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Stefan Mitev on 01/07/2015.
 */
public class DeviceListAdapter extends BaseAdapter {
    private final Context mContext;
    private List<DeviceModel> mData;

    class ViewHolder {
        @BindView(R.id.tv_name)
        TextView tvName;

        @BindView(R.id.tv_address)
        TextView tvAddress;

        ViewHolder(View v) {
            ButterKnife.bind(this, v);
        }
    }

    public DeviceListAdapter(Context context, List<DeviceModel> deviceModelList) {
        mData = deviceModelList;
        mContext = context;
    }

    public void setData(List<DeviceModel> deviceModelList) {
        mData = deviceModelList;
        notifyDataSetChanged();
    }

    public void clearData() {
        if(mData != null) {
            mData.clear();
        }
    }

    public void addData(DeviceModel deviceModel) {
        if(mData == null) {
            mData = new ArrayList<>();
        }
        mData.add(deviceModel);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public DeviceModel getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DeviceModel device = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.li_device, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.tvName.setText(device.getName());
        viewHolder.tvAddress.setText(device.getAddress());

        return convertView;
    }
}