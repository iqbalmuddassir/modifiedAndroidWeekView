package com.alamkanak.weekview.sample;

import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Raquib-ul-Alam Kanak on 7/21/2014.
 * Website: http://april-shower.com
 */
public class MainActivity extends FragmentActivity implements WeekView.MonthChangeListener,
        WeekView.EventClickListener, WeekView.EventLongPressListener, WeekView.EmptyViewClickListener {

    // This map is used to store the events
    HashMap<Integer, List<WeekViewEvent>> eventMap = new HashMap<>();

    // This is the counter for event count - can be removed after testing
    private static int count = 1;

    // Day view & Week view object
    private WeekView mWeekView;

    // Month view fragment object
    private CustomMonthCalendar customMonthCalendar;

    private FragmentManager manager = null;

    // Month view listener
    final CaldroidListener listener = new CaldroidListener() {

        @Override
        public void onSelectDate(Date date, View view) {
            // this is setting background of the selected date
            customMonthCalendar.setBackgroundResourceForDate(R.drawable.ic_launcher, date);
            customMonthCalendar.setTextColorForDate(R.color.caldroid_white, date);
            customMonthCalendar.refreshView();
            FragmentTransaction t = getSupportFragmentManager().beginTransaction();
            t.remove(customMonthCalendar);
            t.commit();

            // this is opening the day view of the selected date
            Calendar requiredDate = Calendar.getInstance();
            requiredDate.setTime(date);
            mWeekView.setVisibility(View.VISIBLE);
            mWeekView.setNumberOfVisibleDays(1);
            mWeekView.goToDate(requiredDate);
        }

        @Override
        public void onChangeMonth(int month, int year) {
            /*String text = "month: " + month + " year: " + year;
            Toast.makeText(getApplicationContext(), text,
                    Toast.LENGTH_SHORT).show();*/
        }

        @Override
        public void onLongClickDate(Date date, View view) {
            Toast.makeText(getApplicationContext(),
                    "Long click " + formatter.format(date),
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCaldroidViewCreated() {
            if (customMonthCalendar.getLeftArrowButton() != null) {
                Toast.makeText(getApplicationContext(),
                        "Caldroid view is created", Toast.LENGTH_SHORT)
                        .show();
            }
        }

    };
    private SimpleDateFormat formatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        formatter = new SimpleDateFormat("dd MMM yyyy");

        // Get a reference for the week view in the layout.
        mWeekView = (WeekView) findViewById(R.id.weekView);

        mWeekView.goToToday();

        // Show a toast message about the touched event.
        mWeekView.setOnEventClickListener(this);

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(this);

        // Set long press listener for events.
        mWeekView.setEventLongPressListener(this);
        mWeekView.setmEndMinute("19:30:00");
        mWeekView.setmStartMinute("09:30:00");
        mWeekView.setEmptyViewClickListener(this);

        // Caldroid fragment for month view calendar
        customMonthCalendar = new CustomMonthCalendar();
        /*Date date = new Date();
        // If Activity is created after rotation
        if (savedInstanceState != null) {
            customMonthCalendar.restoreStatesFromKey(savedInstanceState,
                    "CALDROID_SAVED_STATE");
        }
        // If activity is created from fresh
        else {
            Bundle args = new Bundle();
            Calendar cal = Calendar.getInstance();
            date = cal.getTime();
            args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
            args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
            args.putBoolean(CaldroidFragment.ENABLE_SWIPE, true);
            args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, true);


            // Uncomment this to customize startDayOfWeek
            // args.putInt(CaldroidFragment.START_DAY_OF_WEEK,
            // CaldroidFragment.TUESDAY); // Tuesday


            // Uncomment this line to use Caldroid in compact mode
            // args.putBoolean(CaldroidFragment.SQUARE_TEXT_VIEW_CELL, false);

            customMonthCalendar.setArguments(args);

        }*/

        /*customMonthCalendar.setBackgroundResourceForDate(R.drawable.ic_launcher, date);
        customMonthCalendar.setTextColorForDate(R.color.caldroid_white, date);*/
        // Setup Caldroid
        customMonthCalendar.setCaldroidListener(listener);
        getEventFromDatabase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_today:
                mWeekView.goToToday();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClick(View view) {
        //manager = null;
        mWeekView.goToToday();
        switch (view.getId()) {
            case R.id.action_day_view:
                getSupportFragmentManager().beginTransaction().remove(customMonthCalendar).commitAllowingStateLoss();
                mWeekView.setVisibility(View.VISIBLE);

                // Lets change some dimensions to best fit the view.
                mWeekView.setTextSize((int) TypedValue.applyDimension
                        (TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                mWeekView.setEventTextSize((int) TypedValue.applyDimension
                        (TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics()));

                // Set the number of visible days to one
                mWeekView.setNumberOfVisibleDays(1);

                break;

            case R.id.action_week_view:
                getSupportFragmentManager().beginTransaction().remove(customMonthCalendar).commitAllowingStateLoss();
                mWeekView.setVisibility(View.VISIBLE);
                mWeekView.setEventTextSize((int) TypedValue.applyDimension
                        (TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));

                // Set the number of visible days to seven
                mWeekView.setNumberOfVisibleDays(7);

                break;

            case R.id.action_month_view:
                mWeekView.setVisibility(View.GONE);
                // open the month view calendar
                if(manager == null) {
                    Bundle args = new Bundle();
                    /*Calendar cal = Calendar.getInstance();
                    args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
                    args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));*/
                    args.putBoolean(CaldroidFragment.ENABLE_SWIPE, true);
                    args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, true);


                    //Uncomment this to customize startDayOfWeek
                /*args.putInt(CaldroidFragment.START_DAY_OF_WEEK,
                CaldroidFragment.TUESDAY);*/

                    customMonthCalendar.setArguments(args);
                }
                manager = getSupportFragmentManager();
                manager.beginTransaction()
                        .replace(R.id.calendar_layout, customMonthCalendar).commitAllowingStateLoss();
                customMonthCalendar.refreshView();

                break;
        }
    }

    @Override
    public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {

        // Populate the week view with some events.
        List<WeekViewEvent> events = new ArrayList<>();
        events = extractEvent(newMonth);
        return events;
    }

    private String getEventTitle(Calendar time) {
        return String.format("Event of %02d:%02d %s/%d", time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), time.get(Calendar.MONTH) + 1, time.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        Toast.makeText(MainActivity.this, "Clicked " + event.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
        Toast.makeText(MainActivity.this, "Long pressed event: " + event.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEmptyViewClicked(Calendar time) {
        int startMinute = mWeekView.getmStartMinute();
        Date startTime = new Date();
        Date endTime = new Date();

        Date date = time.getTime();
        int hour = date.getHours();
        int minute = date.getMinutes();

        int minutes = hour * 60 + minute;
        minutes += startMinute;
        minute = minutes % 60;
        int buffer = minute % 15;

        long timeInMillis = date.getTime();
        timeInMillis /= 1000;
        timeInMillis = (timeInMillis / 60) - buffer + startMinute;
        date.setTime(timeInMillis * 60 * 1000);

        String output = "Start Time : " + date.getHours() + " : " + date.getMinutes();
        startTime.setTime(timeInMillis * 60 * 1000);
        timeInMillis = timeInMillis + 15;
        date.setTime(timeInMillis * 60 * 1000);

        output += "\nEnd Time : " + date.getHours() + " : " + date.getMinutes();
        endTime.setTime(timeInMillis * 60 * 1000);


        if (addEvent(startTime, endTime, "This is the New Event Test added :" + count)) {
            Toast.makeText(MainActivity.this, output, Toast.LENGTH_SHORT).show();
            count++;
        } else {
            Toast.makeText(MainActivity.this, "Failed to add event", Toast.LENGTH_SHORT).show();
        }
    }


    private List<WeekViewEvent> extractEvent(int month) {
        List<WeekViewEvent> events = eventMap.get(month);
        if (events == null) {
            events = new ArrayList<>();
        }
        return events;
    }

    private void getEventFromDatabase() {
        List<WeekViewEvent> eventJan = new ArrayList<>();
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 10);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, 0);
        startTime.set(Calendar.YEAR, 2015);
        Calendar endTime = Calendar.getInstance();
        endTime.set(Calendar.HOUR_OF_DAY, 10);
        endTime.set(Calendar.MINUTE, 30);
        endTime.set(Calendar.MONTH, 0);
        endTime.set(Calendar.YEAR, 2015);
        WeekViewEvent event = new WeekViewEvent(1, getEventTitle(startTime), startTime, endTime);
        event.setColor(getResources().getColor(R.color.event_color_01));
        eventJan.add(event);
        eventMap.put(1, eventJan);

        List<WeekViewEvent> eventFeb = new ArrayList<>();
        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 10);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, 1);
        startTime.set(Calendar.YEAR, 2015);
        endTime = Calendar.getInstance();
        endTime.set(Calendar.HOUR_OF_DAY, 10);
        endTime.set(Calendar.MINUTE, 45);
        endTime.set(Calendar.MONTH, 1);
        endTime.set(Calendar.YEAR, 2015);
        event = new WeekViewEvent(1, getEventTitle(startTime), startTime, endTime);
        event.setColor(getResources().getColor(R.color.event_color_02));
        eventFeb.add(event);
        eventMap.put(2, eventFeb);

        List<WeekViewEvent> eventMar = new ArrayList<>();
        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 13);
        startTime.set(Calendar.MINUTE, 30);
        startTime.set(Calendar.MONTH, 2);
        startTime.set(Calendar.YEAR, 2015);
        endTime = Calendar.getInstance();
        endTime.set(Calendar.HOUR_OF_DAY, 14);
        endTime.set(Calendar.MINUTE, 30);
        endTime.set(Calendar.MONTH, 2);
        endTime.set(Calendar.YEAR, 2015);
        event = new WeekViewEvent(10, getEventTitle(startTime), startTime, endTime);
        event.setColor(getResources().getColor(R.color.event_color_03));
        eventMar.add(event);
        eventMap.put(3, eventMar);

        List<WeekViewEvent> eventApr = new ArrayList<>();
        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 14);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, 3);
        startTime.set(Calendar.YEAR, 2015);
        endTime = (Calendar) startTime.clone();
        endTime.set(Calendar.HOUR_OF_DAY, 15);
        endTime.set(Calendar.MINUTE, 0);
        endTime.set(Calendar.MONTH, 3);
        endTime.set(Calendar.YEAR, 2015);
        event = new WeekViewEvent(10, getEventTitle(startTime), startTime, endTime);
        event.setColor(getResources().getColor(R.color.event_color_04));
        eventApr.add(event);
        eventMap.put(4, eventApr);

        List<WeekViewEvent> eventMay = new ArrayList<>();
        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 14);
        startTime.set(Calendar.MINUTE, 45);
        startTime.set(Calendar.MONTH, 5);
        startTime.set(Calendar.YEAR, 2015);
        endTime = (Calendar) startTime.clone();
        endTime.set(Calendar.HOUR_OF_DAY, 15);
        endTime.set(Calendar.MINUTE, 0);
        endTime.set(Calendar.MONTH, 5);
        endTime.set(Calendar.YEAR, 2015);
        event = new WeekViewEvent(10, getEventTitle(startTime), startTime, endTime);
        event.setColor(getResources().getColor(R.color.event_color_01));
        eventApr.add(event);
        eventMap.put(5, eventMay);
    }

    private boolean addEvent(Date startTime, Date endTime, String eventTitle) {
        Calendar currentDate = Calendar.getInstance();
        Date today = currentDate.getTime();
        Calendar startEventTime = Calendar.getInstance();
        startEventTime.setTime(startTime);
        int month = startTime.getMonth();
        Calendar endEventTime = Calendar.getInstance();
        endEventTime.setTime(endTime);
        List<WeekViewEvent> events = extractEvent(month + 1);
        WeekViewEvent event = new WeekViewEvent(count, eventTitle, startEventTime, endEventTime);
        if (startTime.getTime() < today.getTime()) {
            event.setColor(getResources().getColor(R.color.event_color_past));
        } else {
            event.setColor(getResources().getColor(R.color.event_color_upcoming));
        }
        events.add(event);
        eventMap.put(month + 1, events);
        mWeekView.notifyDatasetChanged();
        return true;
    }
}
