package com.example.nicholasarduini.androidspeedtest;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.luas.networkspeedtest.SpeedResults;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SpeedResultsRecyclerViewAdapter extends RecyclerView.Adapter<SpeedResultsRecyclerViewAdapter.ViewHolder> {

    private List<SpeedResults> items;
    private int itemLayout;

    SpeedResultsRecyclerViewAdapter(List<SpeedResults> items, int itemLayout){
        this.items = items;
        this.itemLayout = itemLayout;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SpeedResults item = items.get(position);

        holder.dateTime.setText(getFormattedDateString(item.getTime()));
        holder.downloadSpeed.setText(String.valueOf(String.format("%.2f", item.getDownloadSpeed())));
        holder.uploadSpeed.setText(String.valueOf(String.format("%.2f", item.getUploadSpeed())));
        holder.totalDownload.setText(String.valueOf(String.format("%dMB", item.getTotalDownloadMB())));
        holder.totalUpload.setText(String.valueOf(String.format("%dMB", item.getTotalUploadMB())));
        holder.pingTime.setText(String.valueOf(String.format("Ping  %.2fms", item.getPingTime())));
        if(item.isSslOn()){
            holder.sslOn.setVisibility(View.VISIBLE);
        } else {
            holder.sslOn.setVisibility(View.GONE);
        }
    }

    private String getFormattedDateString(String dateString){
        String stringDatePattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat stringDateFormat = new SimpleDateFormat(stringDatePattern);
        String datePattern = "MMM dd \n HH:mm a";
        SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);

        try {
            Date date = stringDateFormat.parse(dateString);
            String formatedDate = dateFormat.format(date);
            return formatedDate;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTime;
        public TextView downloadSpeed;
        public TextView uploadSpeed;
        public TextView totalDownload;
        public TextView totalUpload;
        public TextView pingTime;
        public TextView sslOn;

        ViewHolder(View itemView) {
            super(itemView);
            dateTime = itemView.findViewById(R.id.dateTime);
            downloadSpeed = itemView.findViewById(R.id.downloadSpeed);
            uploadSpeed = itemView.findViewById(R.id.uploadSpeed);
            totalDownload = itemView.findViewById(R.id.totalDownload);
            totalUpload = itemView.findViewById(R.id.totalUpload);
            pingTime = itemView.findViewById(R.id.pingTime);
            sslOn = itemView.findViewById(R.id.sslOn);
        }
    }
}