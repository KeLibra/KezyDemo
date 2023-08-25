package com.kezy.a23test;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * @Author Kezy
 * @Time 2023/6/12
 * @Description
 */
public class AppNetworkUsageAdapter extends ArrayAdapter<AppNetworkUsage> {
    private int mResourceId;

    public AppNetworkUsageAdapter(Context context, int resourceId, List<AppNetworkUsage> appNetworkUsages) {
        super(context, resourceId, appNetworkUsages);
        mResourceId = resourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Log.e("-------msg", "  -------- --- --getView");
        AppNetworkUsage appNetworkUsage = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(mResourceId, parent, false);
        }
        TextView packageNameTextView = convertView.findViewById(R.id.package_name_text_view);
        TextView rxBytesTextView = convertView.findViewById(R.id.rx_bytes_text_view);
        TextView txBytesTextView = convertView.findViewById(R.id.tx_bytes_text_view);
        packageNameTextView.setText(appNetworkUsage.getPackageName());
        rxBytesTextView.setText(String.valueOf(appNetworkUsage.getRxBytes()));
        txBytesTextView.setText(String.valueOf(appNetworkUsage.getTxBytes()));
        return convertView;
    }
}

