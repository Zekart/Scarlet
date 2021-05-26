package com.example.testscarler.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.testscarler.R;
import com.example.testscarler.model.SocketStatusPojo;

import java.util.List;

public class LogerAdapter extends RecyclerView.Adapter<LogerAdapter.ViewHolder>{
    private List<SocketStatusPojo> mDataStatus;
    private LayoutInflater mInflater;

    public LogerAdapter(Context context, List<SocketStatusPojo> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mDataStatus = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.log_recycler_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SocketStatusPojo status = mDataStatus.get(position);
        holder.mTextViewDate.setText(status.getmTime());
        holder.mTextViewSocketDownload.setText(status.getmDownload());
        holder.mTextViewSocketUpload.setText(status.getmUpload());
    }

    @Override
    public int getItemCount() {
        return mDataStatus.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextViewDate;
        TextView mTextViewSocketDownload;
        TextView mTextViewSocketUpload;

        ViewHolder(View itemView) {
            super(itemView);
            mTextViewDate = itemView.findViewById(R.id.txt_recycler_date);
            mTextViewSocketDownload = itemView.findViewById(R.id.txt_socket_download);
            mTextViewSocketUpload = itemView.findViewById(R.id.txt_socket_upload);
        }
    }
}
