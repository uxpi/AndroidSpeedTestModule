package com.example.nicholasarduini.androidspeedtest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SpeedResultsRecyclerViewAdapter extends RecyclerView.Adapter<SpeedResultsRecyclerViewAdapter.ViewHolder> {

    private List<SpeedResults> items;
    private int itemLayout;
    private Activity activity;

    SpeedResultsRecyclerViewAdapter(List<SpeedResults> items, int itemLayout, Activity activity){
        this.items = items;
        this.itemLayout = itemLayout;
        this.activity = activity;
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
        final SpeedResults item = items.get(position);

        holder.dateTime.setText(getFormattedDateString(item.getTime()));
        holder.downloadSpeed.setText(String.valueOf(String.format("%.2f", item.getDownloadSpeed())));
        holder.uploadSpeed.setText(String.valueOf(String.format("%.2f", item.getUploadSpeed())));
        holder.totalDownload.setText(String.valueOf(String.format("%dMB", item.getTotalDownloadMB())));
        holder.totalUpload.setText(String.valueOf(String.format("%dMB", item.getTotalUploadMB())));
        holder.pingTime.setText(String.valueOf(String.format("Ping  %.2fms", item.getPingTime())));

        holder.cellDataButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                CellDataDialog cdd = new CellDataDialog(activity, item.getCellData());
                cdd.show();
            }
        });

        if(item.isSslOn()){
            holder.sslOn.setAlpha(1);
        } else {
            holder.sslOn.setAlpha(0);
        }

        if(item.isUsingCellular()){
            holder.cellDataButton.setAlpha(1);
        } else {
            holder.cellDataButton.setAlpha(0);
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

        public Button cellDataButton;

        ViewHolder(View itemView) {
            super(itemView);
            dateTime = itemView.findViewById(R.id.dateTime);
            downloadSpeed = itemView.findViewById(R.id.downloadSpeed);
            uploadSpeed = itemView.findViewById(R.id.uploadSpeed);
            totalDownload = itemView.findViewById(R.id.totalDownload);
            totalUpload = itemView.findViewById(R.id.totalUpload);
            pingTime = itemView.findViewById(R.id.pingTime);
            sslOn = itemView.findViewById(R.id.sslOn);
            cellDataButton = itemView.findViewById(R.id.cellDataButton);
        }
    }
}