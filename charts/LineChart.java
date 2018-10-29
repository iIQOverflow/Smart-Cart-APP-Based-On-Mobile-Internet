package com.example.charts;

import android.graphics.Color;
import android.util.Log;
import android.view.View;

import com.example.data.NormalData;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.formatter.LineChartValueFormatter;
import lecho.lib.hellocharts.formatter.SimpleLineChartValueFormatter;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class LineChart {

    private LineChartView lineChartView;

    private List<AxisValue> axisXValues = new ArrayList<AxisValue>();//X轴的标注

    private List<AxisValue> axisYValues = new ArrayList<AxisValue>();//Y轴的标注

    private List<PointValue> pointValues = new ArrayList<PointValue>();//图表的数据点

    private List<Line> lines = new ArrayList<Line>();

    private Line line = new Line();

    private LineChartData data = new LineChartData();

    private Axis axisX = new Axis(); //X轴

    private Axis axisY = new Axis();  //Y轴

    private static String lineColor = "#5603ca";

    private String lineChartName;

    private Viewport viewport = new Viewport();

    private int lineChartId;

    private float userMaxNumber;

    private float userMinNumber;

    private float axisMaxNumber;

    private float axisMinNumber;

    private List<PointValue> normalValues1High = new ArrayList<PointValue>();//图表的数据点
    private List<PointValue> normalValues1Low = new ArrayList<PointValue>();//图表的数据点
    private List<PointValue> normalValues2High = new ArrayList<PointValue>();//图表的数据点
    private List<PointValue> normalValues2Low = new ArrayList<PointValue>();//图表的数据点

    //颜色选择
    private static String GREEN = "#12da8a";
    private static String PURPLE = "#5f5ff1";
    private static String RED = "#ff0400";
    private static String BLUE = "#b8b0ff";
    private static String PINK = "#ff6cc2";
    private static String NORMALCOLOR = "#999999";

    //指标
    private final static int TEMPERATUREID = 1;
    private final static int WEIGHTID = 3;
    private final static int HEARTID = 2;
    private final static int PRESSUREID = 4;
    private final static int FATID = 5;

    private String lineChartColor;

    public void setLineChartId() {
        String name = getLineChartName();
        if (name.equals("体温")) {
            lineChartId = TEMPERATUREID;
        } else if (name.equals("体重")) {
            lineChartId = WEIGHTID;
        } else if (name.equals("心率")) {
            lineChartId = HEARTID;
        } else if (name.equals("血压")) {
            lineChartId = PRESSUREID;
        } else if (name.equals("血脂")) {
            lineChartId = FATID;
        } else {
            lineChartId = TEMPERATUREID;
        }
    }

    public void setLineChartColor() {
        if (lineChartId == TEMPERATUREID) {
            lineChartColor = GREEN;
        } else if (lineChartId == WEIGHTID) {
            lineChartColor = PURPLE;
        } else if (lineChartId == HEARTID) {
            lineChartColor = RED;
        } else if (lineChartId == PRESSUREID) {
            lineChartColor = BLUE;
        } else if (lineChartId == FATID) {
            lineChartColor = PINK;
        } else {
            lineChartColor = GREEN;
        }
    }

    public void setAxisXLabels(String[] label) {
        for (int i = 0;i < label.length;i++){
            axisXValues.add(new AxisValue(i).setLabel(label[i]));
        }
    }

    public void setAxisPoints(float[] y) {
        userMaxNumber = -1;
        userMinNumber = 100000;
        for(int i = 0;i < y.length;i++){
            pointValues.add(new PointValue(i,y[i]));
            if (y[i] > userMaxNumber) {
                userMaxNumber = y[i];
                axisMaxNumber = y[i];
            }
            if (y[i] < userMinNumber) {
                userMinNumber = y[i];
                axisMinNumber = y[i];
            }
        }
    }

    public LineChart(String lineChartName){
        this.lineChartName = lineChartName;
    }

    public void setLineChartView(LineChartView lineChartView) {
        this.lineChartView = lineChartView;
    }

    public LineChartView getLineChartView(){
        return lineChartView;
    }

    public String getLineChartName(){
        return lineChartName;
    }

    public void initLineChart() {

        setLineChartId();

        setLineChartColor();

        addNormalLines();

        line.setValues(pointValues);
        //线的宽度
        line.setStrokeWidth(2);
        line.setPointRadius(4);
        //折线的颜色
        line.setColor(Color.parseColor(getLineChartColor()));
        line.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
        line.setCubic(false);//曲线是否平滑，即是曲线还是折线
        line.setFilled(false);//是否填充曲线的面积
        LineChartValueFormatter chartValueFormatter = new SimpleLineChartValueFormatter(1);
        line.setFormatter(chartValueFormatter);//显示小数点
        line.setHasLabels(true);//曲线的数据坐标是否加上备注
        line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        line.setHasLines(true);//是否用线显示。如果为false 则没有曲线只有点显示
        line.setHasPoints(false);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
        line.setShape(ValueShape.DIAMOND);
        lines.add(line);
        data.setLines(lines);

        //坐标轴
        axisX.setHasTiltedLabels(true);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
        axisX.setTextColor(Color.GRAY);  //设置字体颜色
        axisX.setName("time");  //表格名称
        axisX.setTextSize(8);//设置字体大小
        axisX.setMaxLabelChars(pointValues.size()); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisXValues.length
        axisX.setValues(axisXValues);  //填充X轴的坐标名称
        data.setAxisXBottom(axisX); //x 轴在底部
        //data.setAxisXTop(axisX);  //x 轴在顶部
        axisX.setHasLines(false); //x 轴分割线

        // Y轴是根据数据的大小自动设置Y轴上限(在下面我会给出固定Y轴数据个数的解决方案)
        //axisY.setName("y");//y轴标注
        axisY.setTextSize(10);//设置字体大小
        //data.setAxisYLeft(axisY);  //Y轴设置在左边
        axisY.setHasLines(false);
        axisY.setAutoGenerated(true);
        data.setAxisYLeft(axisY);  //Y轴设置在左边
        //data.setAxisYRight(axisY);  //y轴设置在右边


        //设置行为属性，支持缩放、滑动以及平移
        lineChartView.setInteractive(true);
        lineChartView.setZoomType(ZoomType.HORIZONTAL);
        lineChartView.setMaxZoom((float) 2);//最大方法比例
        lineChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChartView.setLineChartData(data);
        lineChartView.setVisibility(View.VISIBLE);
        lineChartView.setValueSelectionEnabled(false);
        /*注：下面的7，10只是代表一个数字去类比而已
         * 当时是为了解决X轴固定数据个数。见（http://forum.xda-developers.com/tools/programming/library-hellocharts-charting-library-t2904456/page2）;
         */

        setTopAndBottom();
    }

    private void setTopAndBottom() {
        float max;
        float min;
        switch (lineChartId) {
            case TEMPERATUREID:
                max = axisMaxNumber + 0.5f;
                min = axisMinNumber - 1f;
                break;
            case WEIGHTID:
                max = axisMaxNumber + 10f;
                min = axisMinNumber - 10f;
                break;
            case HEARTID:
                max = axisMaxNumber + 10f;
                min = axisMinNumber - 10f;
                break;
            case PRESSUREID:
                max = axisMaxNumber + 10f;
                min = axisMinNumber - 30f;
                break;
            case FATID:
                max = axisMaxNumber + 0.5f;
                min = axisMinNumber - 0.5f;
                break;
            default:
                max = axisMaxNumber + 1f;
                min = axisMinNumber - 1f;
                break;
        }

        if(max < min) {
            float temp;
            temp = max;
            max = min;
            min = temp;
        }
        if (min < 0) {
            min = 0;
        }

        viewport.set(lineChartView.getMaximumViewport());
        viewport.top = max;
        viewport.bottom = min;
        viewport.left = 0;
        viewport.right = pointValues.size() - 1;
        lineChartView.setMaximumViewport(viewport);
        viewport.right = 15;
        lineChartView.setCurrentViewport(viewport);
    }

    //注意bottom和top，顺序颠倒之后无法显示y轴刻度
    public void setViewport(float max,float min) {
        if(max < min) {
            float temp;
            temp = max;
            max = min;
            min = temp;
        }
        viewport.set(lineChartView.getMaximumViewport());
        viewport.top = max;
        viewport.bottom = min;
        viewport.left = 0;
        viewport.right = pointValues.size() - 1;
        lineChartView.setMaximumViewport(viewport);
        lineChartView.setCurrentViewport(viewport);
    }

    public Viewport getViewport(){
        return viewport;
    }

    public LineChartData getLineChartData() {
        return data;
    }

    public List<Line> getLines(){
        line.update(7);

        lineChartView.setLineChartData(data);

        return lines;
    }

    public boolean updateData(float[] y){
        pointValues.clear();
        setAxisPoints(y);
        line.setValues(pointValues);
        data.setLines(lines);
        lineChartView.setLineChartData(data);
        return true;
    }

    public void setAxisY(){
        for(float i = 36f; i <= 40f; i += 2){
            AxisValue value = new AxisValue(i);
            String label = "";
            value.setLabel(label);
            axisYValues.add(value);
        }
        axisY.setValues(axisYValues);
        //data.setAxisYLeft(axisY);  //Y轴设置在左边
    }

    //画出人正常情况下的曲线，临界值
    public void addNormalLines() {
        Line line1High = new Line();
        Line line1Low = new Line();

        Line line2High = new Line();
        Line line2Low = new Line();

        switch (lineChartId) {
            case TEMPERATUREID:
                setNormalData(NormalData.NormalTemperatureHigh,
                        NormalData.NormalTemperatureLow);
                line1High.setValues(normalValues1High);
                line1Low.setValues(normalValues1Low);
                break;
            case WEIGHTID:

                break;
            case HEARTID:
                setNormalData(NormalData.NormalHeartHigh,
                        NormalData.NormalHeartLow);
                line1High.setValues(normalValues1High);
                line1Low.setValues(normalValues1Low);
                break;
            case PRESSUREID:
                setNormalData(NormalData.NormalHighPressureHigh,
                        NormalData.NormalHighPressureLow);
                setNormalData2(NormalData.NormalLowPressureHigh,
                        NormalData.NormalLowPressureLow);
                line1High.setValues(normalValues1High);
                line1Low.setValues(normalValues1Low);
                line2High.setValues(normalValues2High);
                line2Low.setValues(normalValues2Low);
                break;
            case FATID:
                setNormalData(NormalData.NormalFatHigh,
                        NormalData.NormalFatLow);
                line1High.setValues(normalValues1High);
                line1Low.setValues(normalValues1Low);
                break;
            default:
                break;
        }
        //折线的颜色
        //是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
        line1High.setColor(Color.parseColor(NORMALCOLOR)).setHasPoints(false).setStrokeWidth(1);
        line1Low.setColor(Color.parseColor(NORMALCOLOR)).setHasPoints(false).setStrokeWidth(1);
        line2High.setColor(Color.parseColor(NORMALCOLOR)).setHasPoints(false).setStrokeWidth(1);
        line2Low.setColor(Color.parseColor(NORMALCOLOR)).setHasPoints(false).setStrokeWidth(1);
        lines.add(line1High);
        lines.add(line1Low);
        lines.add(line2High);
        lines.add(line2Low);
    }

    //增加一条折线，这里主要是低血压时需要再画一条线
    public void addLines(float[] y) {
        Line lineAdd = new Line();
        List<PointValue> pointValues2 = new ArrayList<PointValue>();//图表的数据点

        for(int i = 0;i < y.length;i++){
            pointValues2.add(new PointValue(i,y[i]));
            if (y[i] > userMaxNumber)
                userMaxNumber = y[i];
            if (y[i] < userMinNumber)
                userMinNumber = y[i];
        }

        lineAdd.setValues(pointValues2);
        lineAdd.setStrokeWidth(2); //线的宽度
        lineAdd.setPointRadius(4); //点的大小
        //折线的颜色
        lineAdd.setColor(Color.parseColor(getLineChartColor()));
        lineAdd.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
        lineAdd.setCubic(false);//曲线是否平滑，即是曲线还是折线
        lineAdd.setFilled(false);//是否填充曲线的面积
        LineChartValueFormatter chartValueFormatter = new SimpleLineChartValueFormatter(1);
        lineAdd.setFormatter(chartValueFormatter);//显示小数点
        lineAdd.setHasLabels(false);//曲线的数据坐标是否加上备注
        //line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        lineAdd.setHasLines(true);//是否用线显示。如果为false 则没有曲线只有点显示
        lineAdd.setHasPoints(false);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
        lineAdd.setShape(ValueShape.DIAMOND);
        lines.add(lineAdd);
        data.setLines(lines);
        lineChartView.setLineChartData(data);
        setTopAndBottom();
    }

    public String getMaxNumber() {
        return String.valueOf(userMaxNumber);
    }

    public String getMinNumber() {
        return String.valueOf(userMinNumber);
    }

    public String getLineChartColor() {
        return lineChartColor;
    }

    /**
     * 设置正常值
     * @param normalDataHigh
     * @param normalDataLow
     */
    private void setNormalData(float normalDataHigh,float normalDataLow) {
        for(int i = 0;i < pointValues.size();i++){
            normalValues1High.add(new PointValue(i,normalDataHigh));
            normalValues1Low.add(new PointValue(i,normalDataLow));
        }
        if (normalDataHigh > axisMaxNumber)
            axisMaxNumber = normalDataHigh;
        if (normalDataLow < axisMinNumber)
            axisMinNumber = normalDataLow;
    }

    private void setNormalData2(float normalDataHigh,float normalDataLow) {
        for(int i = 0;i < pointValues.size();i++){
            normalValues2High.add(new PointValue(i,normalDataHigh));
            normalValues2Low.add(new PointValue(i,normalDataLow));
        }
        if (normalDataHigh > axisMaxNumber)
            axisMaxNumber = normalDataHigh;
        if (normalDataLow < axisMinNumber)
            axisMinNumber = normalDataLow;
    }
}