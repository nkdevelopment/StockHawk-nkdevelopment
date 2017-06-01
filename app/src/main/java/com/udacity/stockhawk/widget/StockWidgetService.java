package com.udacity.stockhawk.widget;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.StockDetailActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Created by NKdevelopment on 29/3/2017.  test github
 */

public class StockWidgetService extends RemoteViewsService {
    private Context mContext;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockWidgetViewFactory(this.getApplicationContext(), intent);
    }

    private class StockWidgetViewFactory implements RemoteViewsFactory {

        private final Context mApplicationContect;
        private final DecimalFormat dollarFormat;
        private final DecimalFormat dollarFormatWithPlus;
        private final DecimalFormat percentageFormat;
        private List<ContentValues> mCvList = new ArrayList<>();

        public StockWidgetViewFactory(Context applicationContext, Intent intent) {
            mApplicationContect = applicationContext;

            dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setPositivePrefix("+$");
            percentageFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.getDefault());
            percentageFormat.setMaximumFractionDigits(2);
            percentageFormat.setMinimumFractionDigits(2);
            percentageFormat.setPositivePrefix("+");
        }

        @Override
        public void onCreate() {
            getData();
        }

        private void getData() {
            mCvList.clear();

            ContentResolver contentResolver = mApplicationContect.getContentResolver();

            Cursor cursor = contentResolver.query(
                    Contract.Quote.URI,
                    null,   // projection
                    null,   // selection
                    null,   // selectionArgs
                    null    // setOrder
            );

            while (cursor.moveToNext()) {

                String symbol = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));
                float price = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_PRICE));
                float absChange = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_ABSOLUTE_CHANGE));
                float percentChange = cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_PERCENTAGE_CHANGE));


                ContentValues cv = new ContentValues();

                cv.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                cv.put(Contract.Quote.COLUMN_PRICE, price);
                cv.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, absChange);
                cv.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);

                mCvList.add(cv);

            }
            cursor.close();
        }


        // this method is called from AppWidgetProvider/onReceive()
        // when is called from AppWidgetManager .notifyAppWidgetViewDataChanged()..
        @Override
        public void onDataSetChanged() {
            Thread thread = new Thread() {
                public void run() {
                    getData();
                }
            };
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
            }

        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return mCvList.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            ContentValues cv = mCvList.get(position);

            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.list_item_quote);

            String symbol = cv.getAsString(Contract.Quote.COLUMN_SYMBOL);
            remoteViews.setTextViewText(R.id.symbol, symbol);
            remoteViews.setTextViewText(R.id.price, dollarFormat.format(cv.getAsFloat(Contract.Quote.COLUMN_PRICE)));

            float absChange = cv.getAsFloat(Contract.Quote.COLUMN_ABSOLUTE_CHANGE);
            float perChange = cv.getAsFloat(Contract.Quote.COLUMN_PERCENTAGE_CHANGE);

            if (absChange > 0) {
                remoteViews.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
            } else {
                remoteViews.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
            }

            remoteViews.setTextViewText(R.id.change, percentageFormat.format(perChange / 100));

            Intent fillIntent = new Intent(getApplicationContext(), StockDetailActivity.class);
            fillIntent.putExtra(StockDetailActivity.EXTRA_SYMBOL, symbol);
            remoteViews.setOnClickFillInIntent(R.id.list_itemm, fillIntent);

            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return new RemoteViews(mApplicationContect.getPackageName(), R.layout.list_item_quote);
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

}
