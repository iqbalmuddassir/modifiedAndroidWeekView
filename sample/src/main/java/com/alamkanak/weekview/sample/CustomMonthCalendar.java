package com.alamkanak.weekview.sample;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alamkanak.weekview.sample.R;
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
