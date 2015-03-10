package com.alamkanak.weekview.sample;


import android.app.Fragment;

import com.roomorama.caldroid.CaldroidFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class CustomMonthCalendar extends CaldroidFragment {

    public CustomMonthCalendar() {
        // Required empty public constructor
    }

    protected int getGridViewRes() {
        return R.layout.custom_month_calendar;
    }
}
