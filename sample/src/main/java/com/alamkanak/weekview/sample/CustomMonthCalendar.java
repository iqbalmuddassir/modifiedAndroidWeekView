package com.alamkanak.weekview.sample;


import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.roomorama.caldroid.CaldroidFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class CustomMonthCalendar extends CaldroidFragment {
    View view;

    public CustomMonthCalendar() {
        // Required empty public constructor
    }

    protected int getGridViewRes() {
        return R.layout.custom_month_calendar;
    }

    /*protected GridView getGridView() {
        GridView gridView = (GridView)view.findViewById(R.id.calendar_gridview);
        return gridView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.custom_month_calendar,container);
        return view;
    }*/
}
