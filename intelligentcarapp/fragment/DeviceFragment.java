package com.example.intelligentcarapp.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.speech.EventListener;
import com.example.intelligentcarapp.LoadingActivity;
import com.example.intelligentcarapp.R;
import com.example.intelligentcarapp.voice.MyRecognition;
import com.example.intelligentcarapp.voice.MySpeech;
import com.example.intelligentcarapp.voice.MyWakeUp;
import com.example.intelligentcarapp.voice.MyWakeUpRecog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lecho.lib.hellocharts.formatter.LineChartValueFormatter;
import lecho.lib.hellocharts.formatter.SimpleLineChartValueFormatter;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class DeviceFragment extends Fragment {

    private static Context mContext;
    private static Activity mActivity;

    private LineChartView pulseLineChartView;
    private TextView pulseAnalysis;
    private Button uploadPulseValues;

    //broadcast definition
    private IntentFilter intentFilter;
    private LocalReceiver localReceiver;
    LocalBroadcastManager localBroadcastManager;

    private List<PointValue> pulseValues= new ArrayList<PointValue>();//图表的数据点
    private LineChartData data = new LineChartData();
    private Line line = new Line();
    private List<Line> lines = new ArrayList<Line>();
    private Axis axisX = new Axis(); //X轴
    private Viewport viewport = new Viewport();

    private Axis axisY = new Axis();  //Y轴

    public static DeviceFragment newInstance(Context context,Activity activity) {
        DeviceFragment fragment = new DeviceFragment();
        Bundle args = new Bundle();
//        args.putString("agrs1", param1);
        mContext = context;
        mActivity = activity;
        fragment.setArguments(args);
        return fragment;
    }

    public DeviceFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device, container, false);
        Bundle bundle = getArguments();
        String agrs1 = bundle.getString("agrs1");

        pulseLineChartView = view.findViewById(R.id.pulse_line_chart_view);
        pulseAnalysis = view.findViewById(R.id.pulse_analysis);
        uploadPulseValues = view.findViewById(R.id.upload_pulse_values);

        localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        intentFilter = new IntentFilter();
        intentFilter.addAction("realtime.fragment.LOCAL_BROADCAST");
        localReceiver = new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver,intentFilter);

        for(int i = 0;i < 256;i++){
            pulseValues.add(new PointValue(i,100));
        }
        Log.d("pulse_values_init", Arrays.toString(pulseValues.toArray()));
        initLineChart();

        uploadPulseValues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(mContext, LoadingActivity.class);//跳转到加载界面
                startActivity(intent);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    new Handler().postDelayed(new Runnable(){
                                        public void run(){
                                            if (pulseValues.get(0).getY() == pulseValues.get(1).getY() && pulseValues.get(0).getY() == pulseValues.get(1).getY() && pulseValues.get(0).getY() == 68.0) {
                                                pulseAnalysis.setText("未检测出脉象，请将手指紧贴脉象传感器");
                                            } else {
                                                pulseAnalysis.setText("平脉 ，脉学名词又称常脉。指脉来有胃气、有神、有根的正常脉象。也指辨别脉象。\n" +
                                                        "\n" +
                                                        "平脉 ，脉学名词。\n" +
                                                        "①又称常脉。指脉来有胃气、有神、有根的正常脉象。\n" +
                                                        "②即辨别脉象。\n");
                                            }
                                        }
                                    }, 3000);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }).start();
            }
        });


        return view;
    }

    public class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final float[] values = intent.getFloatArrayExtra("pulse_values_256");
            Log.d("pulseValues", Arrays.toString(values));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if(values != null) {
                                    pulseValuesUpdate(values);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }).start();
        }
    }

    /**
     * initialize pulse line chart
     */
    public void initLineChart() {

        line.setValues(pulseValues);
        //线的宽度
        line.setStrokeWidth(2);
        //折线的颜色
        line.setColor(Color.RED);
        line.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
        line.setCubic(true);//曲线是否平滑，即是曲线还是折线
        line.setFilled(false);//是否填充曲线的面积
        LineChartValueFormatter chartValueFormatter = new SimpleLineChartValueFormatter(1);
        line.setFormatter(chartValueFormatter);//显示小数点
        line.setHasLabels(false);//曲线的数据坐标是否加上备注
        line.setHasLabelsOnlyForSelected(false);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        line.setHasLines(true);//是否用线显示。如果为false 则没有曲线只有点显示
        line.setHasPoints(false);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
        //line.setShape(ValueShape.DIAMOND);
        lines.add(line);
        data.setLines(lines);

        //坐标轴
        axisX.setHasTiltedLabels(false);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
        axisX.setTextColor(Color.GRAY);  //设置字体颜色
        //axisX.setName("time");  //表格名称
        //axisX.setTextSize(4);//设置字体大小
        axisX.setMaxLabelChars(pulseValues.size()); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisXValues.length
        data.setAxisXBottom(axisX); //x 轴在底部
        //data.setAxisXTop(axisX);  //x 轴在顶部
        axisX.setHasLines(false); //x 轴分割线

        // Y轴是根据数据的大小自动设置Y轴上限(在下面我会给出固定Y轴数据个数的解决方案)
        //        //axisY.setName("y");//y轴标注
        //axisY.setTextSize(10);//设置字体大小
        //data.setAxisYLeft(axisY);  //Y轴设置在左边
        axisY.setHasLines(false);
        axisY.setHasTiltedLabels(false);
        axisY.setAutoGenerated(false);
        data.setAxisYLeft(axisY);  //Y轴设置在左边
        //data.setAxisYRight(axisY);  //y轴设置在右边


        //设置行为属性，支持缩放、滑动以及平移
        pulseLineChartView.setInteractive(false);
        pulseLineChartView.setZoomType(ZoomType.HORIZONTAL);
        pulseLineChartView.setMaxZoom((float) 2);//最大方法比例
        pulseLineChartView.setContainerScrollEnabled(false, ContainerScrollType.HORIZONTAL);
        pulseLineChartView.setLineChartData(data);
        pulseLineChartView.setVisibility(View.VISIBLE);
        pulseLineChartView.setValueSelectionEnabled(false);
        /*注：下面的7，10只是代表一个数字去类比而已
         * 当时是为了解决X轴固定数据个数。见（http://forum.xda-developers.com/tools/programming/library-hellocharts-charting-library-t2904456/page2）;
         */

        setViewport();
    }

    private void setViewport() {
        viewport.top = 260;
        viewport.bottom = 0;
        viewport.left = 0;
        viewport.right = pulseValues.size();
        pulseLineChartView.setMaximumViewport(viewport);
        viewport.right = pulseValues.size();
        pulseLineChartView.setCurrentViewport(viewport);
    }

    private void pulseValuesUpdate(float[] values) {

        pulseValues.clear();
        for(int i = 0;i < values.length;i++){
            pulseValues.add(new PointValue(i,values[i]));
        }
        line.setValues(pulseValues);
        lines.clear();
        lines.add(line);
        data.setLines(lines);

        pulseLineChartView.setLineChartData(data);

        setViewport();
    }

}
