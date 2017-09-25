package com.mentalsnapp.com.mentalsnapp.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.gson.Gson;
import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.activities.PlayVideoActivity;
import com.mentalsnapp.com.mentalsnapp.adapters.StatsRecyclerAdapter;
import com.mentalsnapp.com.mentalsnapp.models.LineChartCoordinates;
import com.mentalsnapp.com.mentalsnapp.models.Videos;
import com.mentalsnapp.com.mentalsnapp.network.ApiClient;
import com.mentalsnapp.com.mentalsnapp.network.response.StatsResponseModel;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;
import com.mentalsnapp.com.mentalsnapp.utils.JsonToStringConverter;
import com.mentalsnapp.com.mentalsnapp.utils.LogHelper;
import com.mentalsnapp.com.mentalsnapp.views.CustomMarkerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment to display Stats
 * Created by Srinivas Kalyani.
 */
public class StatsFragment extends Fragment {

    private LineChart lineChart;
    private BarChart barChart;
    private int month, year;
    private HashMap<Integer, String> mWeek;
    private TextView monthTextView;
    private TextView monthAnalysisTextView;
    private Typeface typeFace;
    private RelativeLayout noContentView;
    private RelativeLayout lineChartLayout;
    private RelativeLayout mProgressLayout;
    private RecyclerView mRecyclerView;
    private StatsResponseModel mStatsModel;
    private StatsRecyclerAdapter mAdapter;
    private TextView mFeelingMessage;
    private String mFeelingName;
    private int[] mMoodArray;
    private static int mPosition = 0;
    private ImageView mNext;
    private ImageView mPrev;

    public StatsFragment() {
        // Required empty public constructor
    }

    public static void setPosition(int mPosition) {
        StatsFragment.mPosition = mPosition;
    }

    public static int getPosition() {
        return mPosition;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View statsScreen = inflater.inflate(R.layout.fragment_stats, container, false);
        initViews(statsScreen);
//        month = 12;
//        year = 2016;
        setDate(System.currentTimeMillis());
        typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/robotolight.ttf");
        String formattedMonth = String.format("%02d", month);
        monthAnalysisTextView.setText(formattedMonth + "/"
                + Integer.toString(year) + "'s Analysis");
        monthAnalysisTextView.setTypeface(typeFace);
        attachListeners();
        return statsScreen;
    }

    private void attachListeners() {
        mPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevWeek();
            }
        });
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextWeek();
            }
        });
    }

    private void prevWeek() {
        if (mStatsModel.weekData.get(mPosition - 1) != null && mStatsModel.weekData.get(mPosition - 1).size() > 0) {
            setPosition(--mPosition);
            mFeelingName = getMoodName(mMoodArray[mPosition]);
            mAdapter.notifyDataSetChanged();
            mFeelingMessage.setText("I was " + mFeelingName + " in week " + mWeek.get(mPosition));
            mNext.setVisibility(View.VISIBLE);
            if (mPosition == 0) {
                mPrev.setVisibility(View.GONE);
                mNext.setVisibility(View.VISIBLE);
            } else {
                mPrev.setVisibility(View.VISIBLE);
            }
        }
    }

    private void nextWeek() {
        if (mStatsModel.weekData.get(mPosition + 1) != null && mStatsModel.weekData.get(mPosition + 1).size() > 0) {
            setPosition(++mPosition);
            mFeelingName = getMoodName(mMoodArray[mPosition]);
            mAdapter.notifyDataSetChanged();
            mFeelingMessage.setText("I was " + mFeelingName + " in week " + mWeek.get(mPosition));
            mPrev.setVisibility(View.VISIBLE);
            if (mPosition != mStatsModel.weekData.size() - 1) {
                mNext.setVisibility(View.VISIBLE);
            } else {
                mNext.setVisibility(View.GONE);
            }
        }
    }

    Callback mResponseCallback = new Callback() {
        @Override
        public void onResponse(Call call, Response response) {
            LogHelper.logInfo(getContext(), "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                    call.request().method(), call.request().body()));
            if (response.isSuccessful()) {
                lineChart.setOnChartGestureListener(null);
                lineChart.setOnChartValueSelectedListener(null);
                LogHelper.logInfo(getContext(), "mResponseCallback", JsonToStringConverter.convertToJson(response.body()));
                final int numDays = getNumberOfDays(month - 1, year);
                StatsResponseModel statsResponse = (StatsResponseModel) response.body();
                mStatsModel = new StatsResponseModel();
                mStatsModel = statsResponse;
                Object barchartData = statsResponse.barChart;
                Gson gson = new Gson();
                String barData = gson.toJson(barchartData);

                final HashMap<LineChartCoordinates, Videos> entryMap = new HashMap<>();
                ArrayList<Videos> posts = statsResponse.posts;
                ArrayList<Integer> circleColors = new ArrayList<>();
                List<Entry> lineChartEntries = new ArrayList<Entry>();
                if (posts != null && posts.size() > 0) {
                    hideNoContentView();
                    Date date = getNextMonthFirstSat();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    final int numberOfWeeks = calendar.getActualMaximum(Calendar.WEEK_OF_MONTH);
                    int differenceInDays = (int) ((numberOfWeeks * 7) - numDays);
                    monthTextView.setText(getMonth(posts.get(0).createdAt) + " " + Integer.toString(year));
                    String monthString = calendar.get(Calendar.MONTH) + 1 > 10 ? Integer.toString(month) : ("0" + Integer.toString(calendar.get(Calendar.MONTH) + 1));
                    monthAnalysisTextView.setText(monthString + "/"
                            + Integer.toString(year) + "'s Analysis");
                    monthTextView.setTypeface(typeFace);

                    for (int index = 0; index < posts.size(); index++) {
                        int moodValue = Integer.parseInt(posts.get(index).moodValue);
                        int dayOfMonth = getDayOfMonth(posts.get(index).createdAt);
                        lineChartEntries.add(new Entry((float) (dayOfMonth + ((differenceInDays * 1.0) / 5)),
                                moodValue));
                        if (moodValue == 1) {
                            circleColors.add(ContextCompat.getColor(getActivity(), R.color.feeling_best));
                        } else if (moodValue == 2) {
                            circleColors.add(ContextCompat.getColor(getActivity(), R.color.feeling_very_good));
                        } else if (moodValue == 3) {
                            circleColors.add(ContextCompat.getColor(getActivity(), R.color.feeling_good));
                        } else if (moodValue == 4) {
                            circleColors.add(ContextCompat.getColor(getActivity(), R.color.feeling_ok));
                        } else if (moodValue == 5) {
                            circleColors.add(ContextCompat.getColor(getActivity(), R.color.feeling_bad));
                        } else if (moodValue == 6) {
                            circleColors.add(ContextCompat.getColor(getActivity(), R.color.feeling_very_bad));
                        } else if (moodValue == 7) {
                            circleColors.add(ContextCompat.getColor(getActivity(), R.color.feeling_worst));
                        }
                        LineChartCoordinates lineChartCoordinates = new LineChartCoordinates();
                        lineChartCoordinates.x = dayOfMonth + (float) (((differenceInDays * 1.0) / 5));
                        lineChartCoordinates.y = moodValue;
                        entryMap.put(lineChartCoordinates, posts.get(index));
                    }

                    final LineDataSet monthDataset = new LineDataSet(lineChartEntries, "Month");
                    monthDataset.setCircleRadius(6f);
                    monthDataset.setColor(Color.parseColor("#FABA15"));
                    monthDataset.setLineWidth(2.0f);
                    monthDataset.setDrawValues(false);
                    monthDataset.setCircleColors(circleColors);
                    monthDataset.setDrawCircleHole(false);
                    List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                    dataSets.add(monthDataset);
                    LineData data = new LineData(dataSets);
                    lineChart.setData(data);
//                    lineChart.getData().setHighlightEnabled(false);
                    lineChart.setDoubleTapToZoomEnabled(false);


                    final float indexArray[] = new float[numberOfWeeks];
                    final XAxis xAxis = lineChart.getXAxis();
                    xAxis.setAxisMinimum(0);
                    xAxis.setAxisMaximum(numberOfWeeks * 7);

                    lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                    indexArray[0] = xAxis.getAxisMaximum() / numberOfWeeks;
                    for (int i = 1; i < indexArray.length; i++) {
                        indexArray[i] = indexArray[i - 1] + 7;
                    }
                    final Calendar cal = calendar;
                    xAxis.setValueFormatter(new IAxisValueFormatter() {
                        @Override
                        public String getFormattedValue(float value, AxisBase axis) {

                            String monthString = month > 10 ? Integer.toString(month) : ("0" + Integer.toString(month));
//                                return String.format("%02d", cal.get(Calendar.DAY_OF_MONTH));
                            if (value == 0) {
                                return "";
                            } else if (value == Math.round((0 + indexArray[0]) / 2)) {
                                String date = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH));
//                                return "";
                                return monthString + "/01 - " + monthString + "/" + date;
                            } else if (value == Math.round((indexArray[0] + indexArray[1]) / 2)) {
                                String date = String.format("%02d", (cal.get(Calendar.DAY_OF_MONTH) + 1));
                                String date2 = String.format("%02d", (cal.get(Calendar.DAY_OF_MONTH) + 7));
//                                return "";
                                return monthString + "/" + date + " - " + monthString + "/" + date2;
                            } else if (value == Math.round((indexArray[1] + indexArray[2]) / 2)) {
//                                return "";
                                return monthString + "/" + (cal.get(Calendar.DAY_OF_MONTH) + 8) + " - " + monthString + "/" + (cal.get(Calendar.DAY_OF_MONTH) + 14);
                            } else if (value == Math.round((indexArray[2] + indexArray[3]) / 2)) {
//                                return "";
                                return monthString + "/" + (cal.get(Calendar.DAY_OF_MONTH) + 15) + " - " + monthString + "/" + (cal.get(Calendar.DAY_OF_MONTH) + 21);
                            } else {

                                if (indexArray.length > 4) {
                                    if (value == Math.round((indexArray[3] + indexArray[4]) / 2)) {
                                        return monthString + "/" + (cal.get(Calendar.DAY_OF_MONTH) + 22) + " - " + monthString + "/" + Integer.toString(numDays);
                                    }
                                } else if (indexArray.length > 5) {
                                    if (value == Math.round((indexArray[4] + indexArray[5]) / 2)) {
                                        return monthString + "/" + (cal.get(Calendar.DAY_OF_MONTH) + 28) + " - "
                                                + monthString + "/" + Integer.toString(numDays);
                                    }
                                }
                            }
                            return "";
                        }
                    });

                    final String[] moods = getResources().getStringArray(R.array.moodArray);
                    for (int index = 1; index <= 7; index++) {
                        LimitLine limitLine = new LimitLine(index, "");
                        limitLine.setLineColor(Color.parseColor("#A3B8BD"));
                        limitLine.setLineWidth(0.05f);
                        lineChart.getAxisLeft().addLimitLine(limitLine);
                    }

                    xAxis.removeAllLimitLines();
                    float index = indexArray[0];   /*get(Calendar.WEEK_OF_MONTH);*/
                    while (index <= indexArray[indexArray.length - 2]) {
                        LimitLine limitLine = new LimitLine(index, "");
                        limitLine.setLineColor(Color.parseColor("#A3B8BD"));
                        limitLine.setLineWidth(0.05f);
                        xAxis.addLimitLine(limitLine);
                        index += indexArray[0];
                    }

                    lineChart.setScaleEnabled(false);
                    Description description = new Description();
                    description.setText("");
                    lineChart.setDescription(description);
                    lineChart.getLegend().setEnabled(false);
                    xAxis.setLabelCount(51);
                    xAxis.setDrawGridLines(false);

                    YAxis yAxis = lineChart.getAxisLeft();
                    yAxis.setValueFormatter(new IAxisValueFormatter() {
                        @Override
                        public String getFormattedValue(float value, AxisBase axis) {
                            if (value == 1) {
                                return moods[0];
                            } else if (value == 2) {
                                return moods[1];
                            } else if (value == 3) {
                                return moods[2];
                            } else if (value == 4) {
                                return moods[3];
                            } else if (value == 5) {
                                return moods[4];
                            } else if (value == 6) {
                                return moods[5];
                            } else if (value == 7) {
                                return moods[6];
                            } else {
                                return Constants.EMPTY_STRING;
                            }
                        }
                    });

//            lineChart.getAxisLeft().setDrawGridLines(false);
                    yAxis.setAxisMinimum(0);
                    yAxis.setAxisMaximum(7);
                    yAxis.setTextColor(Color.parseColor("#758384"));
                    xAxis.setTextColor(Color.parseColor("#758384"));
                    yAxis.setTextSize(8f);
                    xAxis.setTextSize(8f);
                    yAxis.setAxisLineColor(Color.parseColor("#A3B8BD"));
                    xAxis.setAxisLineColor(Color.parseColor("#A3B8BD"));
                    yAxis.setInverted(true);

                    lineChart.getAxisRight().setAxisLineColor(Color.parseColor("#A3B8BD"));
//            yAxis.setLabelCount(8);
                    lineChart.getAxisRight().setDrawLabels(false);
                    lineChart.getAxisRight().setDrawGridLines(false);
                    lineChart.setExtraLeftOffset(12f);
                    lineChart.setExtraRightOffset(-5f);
                    lineChart.setExtraBottomOffset(30f);
//                    lineChart.getData().setHighlightEnabled(false);
//                    lineChart.setMaxHighlightDistance(3);
                    Entry firstEntry = lineChartEntries.get(0);
                    int weekPos = (int) firstEntry.getX();
                    int week = 0;
                    if (weekPos < 7) {
                        week = 0;
                    } else if (weekPos >= 7 && weekPos < 14) {
                        week = 1;
                    } else if (weekPos >= 14 && weekPos < 21) {
                        week = 2;
                    } else if (weekPos >= 21 && weekPos < 28) {
                        week = 3;
                    } else if (weekPos >= 28) {
                        week = 4;
                    }

                    ArrayList<Integer> positionArray = new ArrayList<>();
                    final int count = xAxis.getLimitLines().size();
                    for (int i = 0; i < count; i++) {
                        for (int j = 0; j < monthDataset.getEntryCount(); j++) {
                            if (i == 0) {
                                if (monthDataset.getEntryForIndex(j).getX() <= xAxis.getLimitLines().get(0).getLimit()) {
                                    if (positionArray.isEmpty()) {
                                        positionArray.add(0);
                                    }
                                }
                            } else if (i == count - 1) {
                                if (monthDataset.getEntryForIndex(j).getX() > xAxis.getLimitLines().get(i).getLimit()) {
                                    positionArray.add(i + 1);
                                }
                                if (monthDataset.getEntryForIndex(j).getX() > xAxis.getLimitLines().get(i - 1).getLimit() &&
                                        monthDataset.getEntryForIndex(j).getX() <= xAxis.getLimitLines().get(i).getLimit()) {
                                    positionArray.add(i);
                                }
                            } else {
                                if (monthDataset.getEntryForIndex(j).getX() > xAxis.getLimitLines().get(i - 1).getLimit() &&
                                        monthDataset.getEntryForIndex(j).getX() <= xAxis.getLimitLines().get(i).getLimit()) {
                                    positionArray.add(i);
                                }
                            }
                        }
                    }
                    Set<Integer> monthSet = new HashSet<>(positionArray);
                    List<Integer> tempList = new ArrayList<>(monthSet);
                    Collections.sort(tempList);
                    final HashMap<Integer, Integer> monthDivision = new HashMap<>();
                    for (int i = 0; i < tempList.size(); i++) {
                        monthDivision.put(tempList.get(i), i);
                    }
                    lineChart.setOnChartGestureListener(new OnChartGestureListener() {
                        @Override
                        public void onChartGestureStart(MotionEvent motionEvent, ChartTouchListener.ChartGesture chartGesture) {

                        }

                        @Override
                        public void onChartGestureEnd(MotionEvent motionEvent, ChartTouchListener.ChartGesture chartGesture) {

                        }

                        @Override
                        public void onChartLongPressed(MotionEvent motionEvent) {

                        }

                        @Override
                        public void onChartDoubleTapped(MotionEvent motionEvent) {

                        }

                        @Override
                        public void onChartSingleTapped(MotionEvent motionEvent) {
                            float tappedX = motionEvent.getX();
                            float tappedY = motionEvent.getY();
                            MPPointD point = lineChart.
                                    getTransformer(YAxis.AxisDependency.LEFT).
                                    getValuesByTouchPoint(tappedX, tappedY);
                            List<LimitLine> limitLines = xAxis.getLimitLines();

                            if (point.x <= limitLines.get(0).getLimit()) {
                                if (monthDivision.containsKey(0)) {
                                    setDetails(monthDivision.get(0));
                                }
                            } else if (point.x > limitLines.get(0).getLimit() && point.x <= limitLines.get(1).getLimit()) {
                                if (monthDivision.containsKey(1)) {
                                    setDetails(monthDivision.get(1));
                                }
                            } else if (point.x > limitLines.get(1).getLimit() && point.x <= limitLines.get(2).getLimit()) {
                                if (monthDivision.containsKey(2)) {
                                    setDetails(monthDivision.get(2));
                                }
                            } else if (point.x > limitLines.get(2).getLimit() && point.x <= limitLines.get(3).getLimit()) {
                                if (monthDivision.containsKey(3)) {
                                    setDetails(monthDivision.get(3));
                                }
                            } else if (point.x > limitLines.get(3).getLimit()) {
                                if (monthDivision.containsKey(4)) {
                                    setDetails(monthDivision.get(4));
                                }
                            }

                            /*String text = null;
                            for (Map.Entry<LineChartCoordinates, Videos> entry : entryMap.entrySet()) {
                                if ((entry.getKey().x <= point.x+2&&entry.getKey().x >= point.x-2)&&
                                        (entry.getKey().y <= point.y+1&&entry.getKey().y >= point.y-1)) {
                                    Highlight highlight = new Highlight(entry.getKey().x, 0.0f, 0);
                                    if (entry.getValue().feelings.length > 0) {
                                        if (entry.getValue().tags != null && !entry.getValue().tags.isEmpty()) {
                                            text = "Feeling " + entry.getValue().feelings[0].name + ", " + entry.getValue().tags;
                                            Toast.makeText(getContext(),text,Toast.LENGTH_LONG).show();
                                        } else {
                                            text = "Feeling " + entry.getValue().feelings[0].name;
                                            Toast.makeText(getContext(),text,Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    break;
                                }
                            }*/
                        }

                        @Override
                        public void onChartFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {

                        }

                        @Override
                        public void onChartScale(MotionEvent motionEvent, float v, float v1) {

                        }

                        @Override
                        public void onChartTranslate(MotionEvent motionEvent, float v, float v1) {

                        }
                    });

                    lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                        @Override
                        public void onValueSelected(Entry e, Highlight h) {
                            final long selectionTime = System.currentTimeMillis();
                            final Highlight copyHighlight = h;
                            final Entry copyEntry = e;
                            final boolean[] isTapped = {false};
                            /*String text = null;
                            for (Map.Entry<LineChartCoordinates, Videos> entry : entryMap.entrySet()) {
                                if (entry.getKey().x == copyEntry.getX() && entry.getKey().y == copyEntry.getY()) {
                                    if (entry.getValue().feelings.length > 0) {
                                        if (entry.getValue().tags != null && !entry.getValue().tags.isEmpty()) {
                                            text = "Feeling " + entry.getValue().feelings[0].name + ", " + entry.getValue().tags;
//                                            Toast.makeText(getContext(),text,Toast.LENGTH_LONG).show();
                                        } else {
                                            text = "Feeling " + entry.getValue().feelings[0].name;
//                                            Toast.makeText(getContext(),text,Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    CustomMarkerView mv = new CustomMarkerView(getActivity(), R.layout.linechart_popup, text);
                                    lineChart.setMarker(mv);
//                                    Highlight highlight = new Highlight(entry.getKey().x, 0.0f, 0);
//                                    lineChart.highlightValue(entry.getKey().x, 0.0f);
                                    break;
                                }
                            }*/

                            lineChart.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View view, MotionEvent motionEvent) {

                                    if ((motionEvent.getX() <= copyHighlight.getDrawX() + 50
                                            && motionEvent.getX() >= copyHighlight.getDrawX() - 50)
                                            && (motionEvent.getY() <= copyHighlight.getDrawY() + 50
                                            && motionEvent.getY() >= motionEvent.getY() - 50)) {
                                        String text = null;
                                        for (Map.Entry<LineChartCoordinates, Videos> entry : entryMap.entrySet()) {
                                            if (entry.getKey().x == copyEntry.getX() && entry.getKey().y == copyEntry.getY()) {
                                                if (entry.getValue().feelings.length > 0) {
                                                    if (entry.getValue().tags != null && !entry.getValue().tags.isEmpty()) {
                                                        text = "Feeling " + entry.getValue().feelings[0].name + ", " + entry.getValue().tags;
//                                            Toast.makeText(getContext(),text,Toast.LENGTH_LONG).show();
                                                    } else {
                                                        text = "Feeling " + entry.getValue().feelings[0].name;
//                                            Toast.makeText(getContext(),text,Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                                CustomMarkerView mv = new CustomMarkerView(getActivity(), R.layout.linechart_popup, text);
                                                lineChart.setMarker(mv);
                                                lineChart.invalidate();
//                                    Highlight highlight = new Highlight(entry.getKey().x, 0.0f, 0);
//                                    lineChart.highlightValue(entry.getKey().x, 0.0f);
                                                break;
                                            }
                                        }
                                    }

                                    if (!isTapped[0]) {
                                        final long doubleTapTime = System.currentTimeMillis();
                                        if (doubleTapTime - selectionTime < 300) {
                                            if ((motionEvent.getX() <= copyHighlight.getDrawX() + 50
                                                    && motionEvent.getX() >= copyHighlight.getDrawX() - 50)
                                                    && (motionEvent.getY() <= copyHighlight.getDrawY() + 50
                                                    && motionEvent.getY() >= motionEvent.getY() - 50)) {
                                                for (Map.Entry<LineChartCoordinates, Videos> e : entryMap.entrySet()) {
                                                    if (e.getKey().x == copyEntry.getX() && e.getKey().y == copyEntry.getY()) {
                                                        Intent intent = new Intent(getActivity(), PlayVideoActivity.class);
                                                        intent.putExtra(Constants.PLAY_VIDEO_URL, e.getValue().videoURL);
                                                        lineChart.setMarker(null);
                                                        getActivity().startActivity(intent);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    isTapped[0] = true;
                                    return false;
                                }
                            });
                        }

                        @Override
                        public void onNothingSelected() {

                        }
                    });

                    lineChart.invalidate();

                    JSONObject barChartJData = new JSONObject();
                    try

                    {
                        final String[] moodsArray = getResources().getStringArray(R.array.moodArray);
                        barChartJData = new JSONObject(barData);
                        ArrayList<BarEntry> barEntries = new ArrayList<>();
                        String[] moodValues = {"1", "2", "3", "4", "5", "6", "7"};
                        ArrayList<Integer> barColors = new ArrayList<>();
                        for (int position = 0; position < moodValues.length; position++) {
                            if (barChartJData.has(moodValues[position])) {
                                barEntries.add(new BarEntry(position,
                                        Float.parseFloat((barChartJData.get(moodValues[position])).
                                                toString()), moodsArray[position]));
                            } else {
                                barEntries.add(new BarEntry(position, 0));
                            }
                            if (position == 0) {
                                barColors.add(ContextCompat.getColor(getActivity(), R.color.feeling_best));
                            } else if (position == 1) {
                                barColors.add(ContextCompat.getColor(getActivity(), R.color.feeling_very_good));
                            } else if (position == 2) {
                                barColors.add(ContextCompat.getColor(getActivity(), R.color.feeling_good));
                            } else if (position == 3) {
                                barColors.add(ContextCompat.getColor(getActivity(), R.color.feeling_ok));
                            } else if (position == 4) {
                                barColors.add(ContextCompat.getColor(getActivity(), R.color.feeling_bad));
                            } else if (position == 5) {
                                barColors.add(ContextCompat.getColor(getActivity(), R.color.feeling_very_bad));
                            } else if (position == 6) {
                                barColors.add(ContextCompat.getColor(getActivity(), R.color.feeling_worst));
                            }
                        }


                        BarDataSet barDataSet = new BarDataSet(barEntries, "");
                        barDataSet.setColors(barColors);
                        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                        int width = displayMetrics.widthPixels;
                        if(width<=480) {
                            barDataSet.setValueTextSize(7);
                        }
                        barDataSet.setValueFormatter(new IValueFormatter() {
                            @Override
                            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                                return moodsArray[(int) entry.getX()] + "\n" +
                                        Integer.toString((int) entry.getY()) + "%";
                            }
                        });
                        BarData barData1 = new BarData(barDataSet);
                        barChart.setData(barData1);
                        barChart.getLegend().setEnabled(false);
                        barChart.getAxisRight().setDrawGridLines(false);
                        XAxis barXAxis = barChart.getXAxis();
                        YAxis baryAxis = barChart.getAxisLeft();
                        barChart.getAxisRight().setDrawAxisLine(false);
                        barChart.getAxisRight().setDrawLabels(false);
                        barXAxis.setDrawAxisLine(true);
                        baryAxis.setAxisMaximum(100);
                        baryAxis.setAxisMinimum(0);
                        baryAxis.setLabelCount(5, true);
                        barXAxis.setDrawGridLines(false);
                        baryAxis.setDrawGridLines(false);
                        barXAxis.setDrawLabels(false);
                        barXAxis.setAxisLineWidth(1.0f);
                        baryAxis.setAxisLineWidth(2.0f);
                        barXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                        barXAxis.setAxisLineColor(Color.BLACK);
                        baryAxis.setAxisLineColor(Color.BLACK);
                        Description bardescription = new Description();
                        description.setText("");
                        barChart.setDescription(null);
                        barChart.setTouchEnabled(false);
                        barChart.setNoDataText(getResources().getString(R.string.bar_chart_loading_message));
                        barChart.animateY(2000);
                        barChart.invalidate();
                        setupRecyclerView(statsResponse);

                        setMessageFeeling();
                        if (mStatsModel.weekData.size() == 1) {
                            mNext.setVisibility(View.GONE);
                            mPrev.setVisibility(View.GONE);
                        } else {
                            mNext.setVisibility(View.VISIBLE);
                            mPrev.setVisibility(View.GONE);
                        }
                        mProgressLayout.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogHelper.logError(getActivity(), "STATSFRAGMENT", e.toString());
                    }
                } else {
                    showNoContentView();
                    mProgressLayout.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onFailure(Call call, Throwable t) {
            LogHelper.logInfo(getContext(), "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                    call.request().method(), call.request().body()));
            mProgressLayout.setVisibility(View.GONE);
            try {
                if (t.getMessage().equals(Constants.NO_INTERNET_MESSAGE)) {
                    Toast.makeText(getContext(), String.valueOf(getResources().getString(R.string.no_internet)), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), String.valueOf(t.getMessage()), Toast.LENGTH_LONG).show();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            LogHelper.logError(getContext(), "error", t.toString());
        }
    };

    private void hideNoContentView() {
        barChart.setVisibility(View.VISIBLE);
        lineChartLayout.setVisibility(View.VISIBLE);
        noContentView.setVisibility(View.GONE);
        mFeelingMessage.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
        monthTextView.setVisibility(View.VISIBLE);
    }

    private void showNoContentView() {
        barChart.setVisibility(View.GONE);
        lineChartLayout.setVisibility(View.GONE);
        noContentView.setVisibility(View.VISIBLE);
        mNext.setVisibility(View.GONE);
        mPrev.setVisibility(View.GONE);
        mFeelingMessage.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        monthTextView.setVisibility(View.GONE);
    }


    private void setMessageFeeling() {
        int value;
        int frequency;
        ArrayList<ArrayList<Integer>> weekData = null;
        mMoodArray = new int[mStatsModel.weekData.size()];
        for (int j = 0; j < mStatsModel.weekData.size(); j++) {
            weekData = new ArrayList<>();
            weekData.addAll(mStatsModel.weekData.get(j));
            value = 0;
            frequency = 0;
            for (int i = 0; i < weekData.size(); i++) {
                if (weekData.get(i).get(1) > frequency) {
                    frequency = weekData.get(i).get(1);
                    value = weekData.get(i).get(0);
                } else if (weekData.get(i).get(1) == frequency) {
                    if (weekData.get(i).get(0) < value) {
                        value = weekData.get(i).get(0);
                        frequency = weekData.get(i).get(1);
                    }
                }
            }
            mMoodArray[j] = value;
        }
        mFeelingName = getMoodName(mMoodArray[0]);
        Calendar calendar = Calendar.getInstance();
        Set<Integer> mFeelWeek = new HashSet<>();
        for (int j = 0; j < mStatsModel.posts.size(); j++) {
            calendar.setTimeInMillis(Long.parseLong(mStatsModel.posts.get(j).createdAt) * 1000);

            mFeelWeek.add(calendar.get(Calendar.WEEK_OF_MONTH));
        }
        mWeek = new HashMap<>();
        int counter = 0;
        Date date = getNextMonthFirstSat();
        calendar.setTime(date);
        String mMonth = String.valueOf(month < 10 ? ("0" + month) : month);
        if (mFeelWeek.contains(1)) {
            String tempDate = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
            mWeek.put(counter, "(" + mMonth + "/01 - " + mMonth + "/" + tempDate + ")");
            ++counter;
        }
        if (mFeelWeek.contains(2)) {
            String tempDate = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH) + 1);
            String tempDate2 = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH) + 7);
            mWeek.put(counter, "(" + mMonth + "/" + tempDate + " - " + mMonth + "/" + tempDate2 + ")");
            ++counter;
        }
        if (mFeelWeek.contains(3)) {
            String tempDate = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH) + 8);
            String tempDate2 = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH) + 14);
            mWeek.put(counter, "(" + mMonth + "/" + tempDate + " - " + mMonth + "/" + tempDate2 + ")");
            ++counter;
        }
        if (mFeelWeek.contains(4)) {
            String tempDate = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH) + 15);
            String tempDate2 = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH) + 21);
            mWeek.put(counter, "(" + mMonth + "/" + tempDate + " - " + mMonth + "/" + tempDate2 + ")");
            ++counter;
        }
        if (mFeelWeek.contains(5)) {
            String tempDate = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH) + 22);
            String tempDate2 = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH) + 28);
            mWeek.put(counter, "(" + mMonth + "/" + tempDate + " - " + mMonth + "/" + tempDate2 + ")");
        }
        mFeelingMessage.setText("I was " + mFeelingName + " in week " + mWeek.get(mPosition));
    }

    private void setDetails(int position) {
        setPosition(position);
        mFeelingName = getMoodName(mMoodArray[position]);
        mAdapter.notifyDataSetChanged();
        mFeelingMessage.setText("I was " + mFeelingName + " in week " + mWeek.get(mPosition));
        if (mStatsModel.weekData.size() > 1) {
            if (mPosition == 0) {
                mPrev.setVisibility(View.GONE);
                mNext.setVisibility(View.VISIBLE);
            } else if (mPosition == mStatsModel.weekData.size() - 1) {
                mNext.setVisibility(View.GONE);
                mPrev.setVisibility(View.VISIBLE);
            } else {
                mNext.setVisibility(View.VISIBLE);
                mPrev.setVisibility(View.VISIBLE);
            }
        }
    }

    private String getMoodName(int i) {
        String moodName = null;
        if (i == 1) {
            moodName = getResources().getString(R.string.best_mood);
        }
        if (i == 2) {
            moodName = getResources().getString(R.string.very_good_mood);
        }
        if (i == 3) {
            moodName = getResources().getString(R.string.good_mood);
        }
        if (i == 4) {
            moodName = getResources().getString(R.string.ok_mood);
        }
        if (i == 5) {
            moodName = getResources().getString(R.string.bad_mood);
        }
        if (i == 6) {
            moodName = getResources().getString(R.string.very_bad_mood);
        }
        if (i == 7) {
            moodName = getResources().getString(R.string.worst_mood);
        }
        return moodName;
    }

    public void getStats(int month, int year) {
        setPosition(0);
        mProgressLayout.setVisibility(View.VISIBLE);
        this.month = month;
        this.year = year;
        Call<StatsResponseModel> statsResponse = ApiClient.getApiInterface().getStats(month, year);
        statsResponse.enqueue(mResponseCallback);
    }

    private void initViews(View statsScreen) {
        lineChart = (LineChart) statsScreen.findViewById(R.id.lineChart);
        barChart = (BarChart) statsScreen.findViewById(R.id.barChart);
        monthTextView = (TextView) statsScreen.findViewById(R.id.monthText);
        monthAnalysisTextView = (TextView) statsScreen.findViewById(R.id.monthAnalysisText);
        noContentView = (RelativeLayout) statsScreen.findViewById(R.id.noContentView);
        lineChartLayout = (RelativeLayout) statsScreen.findViewById(R.id.lineChartLayout);
        mRecyclerView = (RecyclerView) statsScreen.findViewById(R.id.recycler);
        mNext = (ImageView) statsScreen.findViewById(R.id.next_btn);
        mPrev = (ImageView) statsScreen.findViewById(R.id.previous_btn);
        mFeelingMessage = (TextView) statsScreen.findViewById(R.id.feeling_message);
        mProgressLayout = (RelativeLayout) statsScreen.findViewById(R.id.progress_bar_layout);
    }

    private Date getNextMonthFirstSat() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis((Long.parseLong(mStatsModel.posts.get(0).createdAt) * 1000));
        c.set(Calendar.DAY_OF_MONTH, 1);

        // search until saturday
        while (c.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
            c.add(Calendar.DAY_OF_MONTH, 1);
        }
        return c.getTime();
    }


    private int getNumberOfDays(int month, int year) {
        int maxDayNumber;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        maxDayNumber = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        // Month value starts from 0 to 11 for Jan to Dec
        return maxDayNumber;
    }

    private int getDayOfMonth(String timeStamp) {
        long timeInMillis = Long.parseLong(timeStamp) * 1000L;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return day;
    }

    private String getMonth(String timeStamp) {
        long timeInMillis = Long.parseLong(timeStamp) * 1000L;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        String month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
        return month;
    }

    private void setDate(long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);
        this.month = calendar.get(Calendar.MONTH) + 1;
        this.year = calendar.get(Calendar.YEAR);
    }


    private void setupRecyclerView(StatsResponseModel statsResponse) {
        mAdapter = new StatsRecyclerAdapter(getContext(), statsResponse);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setFocusable(false);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            if (lineChart != null) {
                lineChart.setMarker(null);
            }
        } else {
            getStats(month, year);
        }
    }
}
