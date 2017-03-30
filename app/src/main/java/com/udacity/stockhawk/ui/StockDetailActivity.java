package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVReader;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.attr.data;
import static android.R.attr.entries;
import static android.R.attr.value;

public class StockDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SYMBOL = "symbol";
    @BindView(R.id.text)
    TextView text;

    @BindView(R.id.chart)
    LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);
        ButterKnife.bind(this);

        String symbol = getIntent().getStringExtra(EXTRA_SYMBOL);
        text.setText(symbol);

        showHistory(symbol);
    }

    private void showHistory(String symbol) {
        String history = getHistoryString(symbol);

        List<Entry> entries = new ArrayList<>();

        List<String[]> lines = getLines(history);

        final List<Long> xAxisValues = new ArrayList<>();
        int xAxisPosition = 0;

        for (int i = lines.size() - 1; i >= 0; i--) {
            String[] line = lines.get(i);

            // setup xAxis
            xAxisValues.add(Long.valueOf(line[0]));
            xAxisPosition++;

            // add entry data
            Entry entry = new Entry(xAxisPosition, // timestamp
                    Float.valueOf(line[1]) // price
            );
            entries.add(entry);
        }

        drawMyChart(symbol, entries, xAxisValues);
    }


    private String getHistoryString(String symbol) {
        Cursor cursor = getContentResolver().query(Contract.Quote.makeUriForStock(symbol), // table name
                null,   // Projection-table columns (null for all columns)
                null,   // Selection- whereClause (should include ? for things that are dynamic)
                null,   // SelectionArgs- whereArgs (specify content that fills each ? in whereClause)
                null    // sortOrder
        );
        String history = "";
        if (cursor.moveToFirst()) {
            history = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
            cursor.close();
        }
        return history;
    }

    private void drawMyChart(String symbol, List<Entry> entries, final List<Long> xAxisValues) {

        Description description = new Description();
        description.setText(symbol+" "+getString(R.string.desciption_text));
        description.setTextColor(Color.RED);
        description.setTextSize(11f);
        chart.setDescription(description);

        LineDataSet dataSet = new LineDataSet(entries, symbol);
        dataSet.setColor(Color.BLUE);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.setBackgroundColor(Color.GRAY);
        chart.setDrawBorders(true);

        XAxis xAxis = chart.getXAxis();
        xAxis.setLabelRotationAngle(75f);
        xAxis.setTextSize(11f);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Date date = new Date(xAxisValues.get((int) value));
                return (new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(date));
            }
        });

        // refresh
        chart.invalidate();
    }

    private List<String[]> getLines(String history) {
        List<String[]> lines = null;
        CSVReader csvReader = new CSVReader(new StringReader(history));
        try {
            lines = csvReader.readAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }


}
