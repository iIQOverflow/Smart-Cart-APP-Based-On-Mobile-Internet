package com.example.charts;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.intelligentcarapp.R;
import com.example.report.BloodFatActivity;
import com.example.report.BloodPressureActivity;
import com.example.report.HeartActivity;
import com.example.report.TemperatureActivity;
import com.example.report.WeightActivity;

import java.util.List;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class LineChartsAdapter extends RecyclerView.Adapter<LineChartsAdapter.ViewHolder> {

    private Context mContext;

    private List<LineChart> mLineChartsList;

    private String userAccount;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView lineChartName;
        TextView lineChartTop;
        LineChartView lineChartView;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            lineChartName = view.findViewById(R.id.line_chart_name);
            lineChartTop = view.findViewById(R.id.line_chart_top);
            lineChartView = view.findViewById(R.id.line_chart_view);
        }
    }

    public LineChartsAdapter(List<LineChart> lineChartsList) {
        mLineChartsList = lineChartsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.linechart_item,
                parent,false);
        final ViewHolder holder = new ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                try {
                    LineChart lineChart = mLineChartsList.get(position);
                    PageTransition(mContext,position);
                } catch (Exception e){
                    e.printStackTrace();
                } finally {

                }
            }
        });
        holder.lineChartView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                try {
                    LineChart lineChart = mLineChartsList.get(position);
                    PageTransition(mContext,position);
                    //Toast.makeText(v.getContext(),lineChart.getLineChartName(),Toast.LENGTH_SHORT).show();
                } catch (Exception e){
                    e.printStackTrace();
                } finally {

                }

            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LineChart lineChart = mLineChartsList.get(position);
        holder.lineChartName.setText(lineChart.getLineChartName());
        holder.lineChartTop.setTextColor(Color.parseColor(lineChart.getLineChartColor()));
        holder.lineChartTop.setText(lineChart.getMaxNumber());
        holder.lineChartView.setLineChartData(lineChart.getLineChartData());
        //需要先设置LineChartData再设置后两项！
        Log.d("LineChartsAdapter","bottom: " + lineChart.getViewport().bottom + "top: " + lineChart.getViewport().top);
        holder.lineChartView.setMaximumViewport(lineChart.getViewport());
        Log.d("LineChartsAdapter","left: " + lineChart.getViewport().left + "right: " + lineChart.getViewport().right);
        holder.lineChartView.setCurrentViewport(lineChart.getViewport());
    }

    @Override
    public int getItemCount() {
        return mLineChartsList.size();
    }

    public void setUserAccount(String account){
        this.userAccount = account;
    }

    //页面跳转
    public void PageTransition(Context context,int position){
        Intent intent;
        switch (position){
            case 0:
                intent = new Intent(context,TemperatureActivity.class);
                break;
            case 1:
                intent = new Intent(context,WeightActivity.class);
                break;
            case 2:
                intent = new Intent(context,HeartActivity.class);
                break;
            case 3:
                intent = new Intent(context,BloodPressureActivity.class);
                break;
            case 4:
                intent = new Intent(context,BloodFatActivity.class);
                break;
            default:
                intent = new Intent(context,TemperatureActivity.class);
                break;
        }
        intent.putExtra("account",userAccount);
        context.startActivity(intent);
    }
}
