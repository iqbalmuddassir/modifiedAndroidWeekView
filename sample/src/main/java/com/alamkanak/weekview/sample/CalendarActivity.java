package com.alamkanak.weekview.sample;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.alamkanak.weekview.activities.SimpleActivity;
import com.alamkanak.weekview.utils.TypeFaceSpan;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.alamkanak.weekview.utils.AppConstants.APR;
import static com.alamkanak.weekview.utils.AppConstants.CALENDAR_PREFERENCES;
import static com.alamkanak.weekview.utils.AppConstants.DATE_KEY_MONTH;
import static com.alamkanak.weekview.utils.AppConstants.DATE_KEY_WEEK;
import static com.alamkanak.weekview.utils.AppConstants.DAY_EVENT_SIZE;
import static com.alamkanak.weekview.utils.AppConstants.DAY_TEXT_SIZE;
import static com.alamkanak.weekview.utils.AppConstants.DAY_VIEW;
import static com.alamkanak.weekview.utils.AppConstants.DD_MMM_YYYY;
import static com.alamkanak.weekview.utils.AppConstants.END;
import static com.alamkanak.weekview.utils.AppConstants.EVENT_ADD_FAILURE_MESSAGE;
import static com.alamkanak.weekview.utils.AppConstants.FEB;
import static com.alamkanak.weekview.utils.AppConstants.JAN;
import static com.alamkanak.weekview.utils.AppConstants.MAR;
import static com.alamkanak.weekview.utils.AppConstants.MAY;
import static com.alamkanak.weekview.utils.AppConstants.MONTH_VIEW;
import static com.alamkanak.weekview.utils.AppConstants.RALEWAY_LIGHT;
import static com.alamkanak.weekview.utils.AppConstants.RALEWAY_REGULAR;
import static com.alamkanak.weekview.utils.AppConstants.RALEWAY_SEMI_BOLD;
import static com.alamkanak.weekview.utils.AppConstants.SELECT_SLOT_TITLE;
import static com.alamkanak.weekview.utils.AppConstants.START;
import static com.alamkanak.weekview.utils.AppConstants.WEEK_EVENT_SIZE;
import static com.alamkanak.weekview.utils.AppConstants.WEEK_VIEW;


/**
 * Created by Raquib-ul-Alam Kanak on 7/21/2014.
 * Website: http://april-shower.com
 */
public class CalendarActivity extends ActionBarActivity implements WeekView.MonthChangeListener,
        WeekView.EventClickListener, WeekView.EmptyViewClickListener,
        WeekView.ChangeBackgroundListener {

    // This is the counter for event count - can be removed after testing
    private static int count = 1;
    // To keep record of viewType being shown - Added by Muddassir
    private static int viewType;
    // This map is used to store the events
    HashMap<Integer, List<WeekViewEvent>> eventMap = new HashMap<>();
    // Typeface for text - Added by Muddassir
    Typeface ralewayLight, ralewayRegular, ralewaySemiBold;
    TextView monthText, stylistOptionTitle;
    // Day view & Week view object
    private WeekView mWeekView;
    // Month view fragment object
    private CustomMonthCalendar customMonthCalendar;
    // For button background toggle
    private Button buttonDayView, buttonWeekView, buttonMonthView;
    // Month view listener
    final CaldroidListener listener = new CaldroidListener() {

        @Override
        public void onSelectDate(Date date, View view) {
            // this is setting background of the selected date
            customMonthCalendar.setBackgroundResourceForDate(R.color.event_color_upcoming, date);
            customMonthCalendar.setTextColorForDate(R.color.caldroid_white, date);
            customMonthCalendar.refreshView();
            FragmentTransaction t = getSupportFragmentManager().beginTransaction();
            t.remove(customMonthCalendar);
            t.commit();

            SharedPreferences.Editor editor = calendarPreference.edit();
            editor.putLong(DATE_KEY_MONTH, date.getTime());
            editor.commit();

            // this is opening the day view of the selected date
            Calendar requiredDate = Calendar.getInstance();
            requiredDate.setTime(date);
            mWeekView.setVisibility(View.VISIBLE);
            mWeekView.setNumberOfVisibleDays(1);
            mWeekView.goToDate(requiredDate);
            changeButtonBackground(buttonDayView);

            // Set the view type to Day view
            viewType = DAY_VIEW;
        }

        @Override
        public void onChangeMonth(int month, int year) {
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
                monthText = customMonthCalendar.getMonthTitleTextView();
                monthText.setTypeface(ralewaySemiBold); // Added by Muddassir
            }
        }

    };
    // Variables to be used in the program - Added by Muddassir
    private FragmentManager manager = null;
    private SimpleDateFormat formatter;
    private Date[] startEndTime;
    private WeekViewEvent event;
    private SharedPreferences calendarPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_activity);

        // to customize the action bar
        customizeActionBar();

        // Initialize all the required components
        initComponents();
    }

    // to customize the action bar
    private void customizeActionBar() {
        SpannableString s = new SpannableString(SELECT_SLOT_TITLE);
        s.setSpan(new TypeFaceSpan(this, RALEWAY_REGULAR), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(s);
    }

    // Initialize all the required components
    private void initComponents() {
        // Set the view type to Day view
        viewType = DAY_VIEW;

        // Initialise the typefaces
        ralewayLight = Typeface.createFromAsset(getAssets(),
                RALEWAY_LIGHT); // Added by Muddassir
        ralewayRegular = Typeface.createFromAsset(getAssets(),
                RALEWAY_REGULAR); // Added by Muddassir
        ralewaySemiBold = Typeface.createFromAsset(getAssets(),
                RALEWAY_SEMI_BOLD); // Added by Muddassir

        // To record the button pressed - Added by Muddassir
        stylistOptionTitle = (TextView) findViewById(R.id.stylist_option_title);
        buttonDayView = (Button) findViewById(R.id.action_day_view);
        buttonWeekView = (Button) findViewById(R.id.action_week_view);
        buttonMonthView = (Button) findViewById(R.id.action_month_view);
        buttonDayView.setTypeface(ralewayRegular);
        buttonWeekView.setTypeface(ralewayRegular);
        buttonMonthView.setTypeface(ralewayRegular);
        stylistOptionTitle.setTypeface(ralewayRegular);

        formatter = new SimpleDateFormat(DD_MMM_YYYY);

        // Get a reference for the week view in the layout.
        mWeekView = (WeekView) findViewById(R.id.weekView);

        mWeekView.goToToday();

        // Show a toast message about the touched event.
        mWeekView.setOnEventClickListener(this);

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(this);

        // to toggle button
        mWeekView.setmBackgroundListener(this);

        // Setup start and end time of the calendar view - Added by Muddassir
        mWeekView.setmEndMinute("19:30:00");
        mWeekView.setmStartMinute("09:30:00");
        mWeekView.setEmptyViewClickListener(this); // Added by Muddassir

        // Caldroid fragment for month view calendar
        customMonthCalendar = new CustomMonthCalendar();

        customMonthCalendar.setCaldroidListener(listener);

        // Fetch all events
        getEventFromDatabase();

        // Initialise the shared preference
        calendarPreference = getSharedPreferences
                (CALENDAR_PREFERENCES, Context.MODE_PRIVATE);
    }

    public void onClick(View view) {

        //mWeekView.goToToday();
        switch (view.getId()) {
            // When Day view is clicked
            case R.id.action_day_view:
                if (viewType != DAY_VIEW) {
                    getSupportFragmentManager().beginTransaction().remove(customMonthCalendar).commitAllowingStateLoss();
                    mWeekView.setVisibility(View.VISIBLE);

                    // Lets change some dimensions to best fit the view.
                    mWeekView.setTextSize((int) TypedValue.applyDimension
                            (TypedValue.COMPLEX_UNIT_SP, DAY_TEXT_SIZE, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension
                            (TypedValue.COMPLEX_UNIT_SP, DAY_EVENT_SIZE, getResources().getDisplayMetrics()));

                    // Set the number of visible days to one
                    mWeekView.setNumberOfVisibleDays(DAY_VIEW);
                    viewType = DAY_VIEW;

                    // Change the button colors - Added by Muddassir
                    changeButtonBackground(buttonDayView);
                    mWeekView.setFromMonthView(false);
                }
                break;

            // When Week view is clicked
            case R.id.action_week_view:
                if (viewType != WEEK_VIEW) {
                    getSupportFragmentManager().beginTransaction().remove(customMonthCalendar).commitAllowingStateLoss();
                    mWeekView.setVisibility(View.VISIBLE);
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension
                            (TypedValue.COMPLEX_UNIT_SP, WEEK_EVENT_SIZE, getResources().getDisplayMetrics()));

                    // Set the number of visible days to seven
                    mWeekView.setNumberOfVisibleDays(WEEK_VIEW);
                    viewType = WEEK_VIEW;
                    changeButtonBackground(buttonWeekView);

                    if (calendarPreference.contains(DATE_KEY_WEEK)) {
                        long dateInMillis = calendarPreference.getLong(DATE_KEY_WEEK, 0);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(dateInMillis);
                        mWeekView.goToDate(calendar);
                        mWeekView.notifyDatasetChanged();
                    }
                    mWeekView.setFromMonthView(false);
                }
                break;

            // When Month view is clicked
            case R.id.action_month_view:
                if (viewType != MONTH_VIEW) {
                    if (viewType == WEEK_VIEW) {
                        mWeekView.setFromMonthView(true);
                    }
                    viewType = MONTH_VIEW;
                    mWeekView.setVisibility(View.GONE);
                    manager = null;
                    // open the month view calendar
                    if (manager == null) {
                        Bundle args = new Bundle();
                        Calendar cal = Calendar.getInstance();
                        // Extract Date from the Shared Preferences if already stored
                        if (calendarPreference.contains(DATE_KEY_MONTH)) {
                            long dateInMillis = calendarPreference.getLong(DATE_KEY_MONTH, 0);
                            cal.setTimeInMillis(dateInMillis);
                            args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
                            args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
                        } else {
                            args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
                            args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
                        }
                        args.putBoolean(CaldroidFragment.ENABLE_SWIPE, true);
                        args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, false);

                        customMonthCalendar.setArguments(args);
                    }
                    manager = getSupportFragmentManager();
                    manager.beginTransaction()
                            .replace(R.id.calendar_layout, customMonthCalendar).commitAllowingStateLoss();

                    customMonthCalendar.refreshView();
                    changeButtonBackground(buttonMonthView);
                }
                break;

            case R.id.show_list_button:
                Intent intent = new Intent(this, SimpleActivity.class);
                startActivity(intent);
                break;
        }
    }

    // Change the background color of the selected button - Added by Muddassir
    public void changeButtonBackground(Button button) {
        buttonDayView.setBackgroundColor(getResources().getColor(R.color.button_not_selected));
        buttonWeekView.setBackgroundColor(getResources().getColor(R.color.button_not_selected));
        buttonMonthView.setBackgroundColor(getResources().getColor(R.color.button_not_selected));
        button.setBackgroundColor(getResources().getColor(R.color.button_selected));
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
        Toast.makeText(CalendarActivity.this, "Pressed event: " + event.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    // It will implement the swipe on the events to cancel it - Added by Muddassir
    public void onEventSwipe(WeekViewEvent event, RectF eventRect) {
        if (cancelEvent(event)) {
            Toast.makeText(CalendarActivity.this, "deleted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEmptyViewClicked(Calendar time) {
        startEndTime = convertTime(time);
        {
            // This tries to add event - Added by Muddassir
            if (addEvent(startEndTime[0], startEndTime[1], "This is the New Event Test added :" + count)) {
                Toast.makeText(CalendarActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                count++;
            } else {
                Toast.makeText(CalendarActivity.this, EVENT_ADD_FAILURE_MESSAGE, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    // It will implement the swipe on the empty view to add blank - Added by Muddassir
    public void onEmptyViewSwiped(Calendar time) {
        startEndTime = convertTime(time);
        if (addEvent(startEndTime[0], startEndTime[1], "")) {
            Toast.makeText(CalendarActivity.this, "Successful", Toast.LENGTH_SHORT).show();
        }
    }

    // Extracts the event list of the specified month - Added by Muddassir
    private List<WeekViewEvent> extractEvent(int month) {
        List<WeekViewEvent> events = eventMap.get(month);
        if (events == null) {
            events = new ArrayList<>();
        }
        return events;
    }

    // Fetch all the events from the database and store it into the Map - Added by Muddassir
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
        Date date = startTime.getTime();
        customMonthCalendar.setBackgroundResourceForDate(R.color.event_color_upcoming, date);
        customMonthCalendar.setTextColorForDate(R.color.caldroid_white, date);
        eventMap.put(JAN, eventJan);

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
        date = startTime.getTime();
        customMonthCalendar.setBackgroundResourceForDate(R.color.event_color_upcoming, date);
        customMonthCalendar.setTextColorForDate(R.color.caldroid_white, date);
        eventMap.put(FEB, eventFeb);

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
        date = startTime.getTime();
        customMonthCalendar.setBackgroundResourceForDate(R.color.event_color_upcoming, date);
        //customMonthCalendar.setBackgroundResourceForDate(R.drawable.event_cell_background, date);
        customMonthCalendar.setTextColorForDate(R.color.caldroid_white, date);
        eventMap.put(MAR, eventMar);

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
        date = startTime.getTime();
        customMonthCalendar.setBackgroundResourceForDate(R.color.event_color_upcoming, date);
        customMonthCalendar.setTextColorForDate(R.color.caldroid_white, date);
        eventMap.put(APR, eventApr);

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
        date = startTime.getTime();
        customMonthCalendar.setBackgroundResourceForDate(R.color.event_color_upcoming, date);
        customMonthCalendar.setTextColorForDate(R.color.caldroid_white, date);
        eventMap.put(MAY, eventMay);
    }

    // Add event to the calendar - Added by Muddassir
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

    @Override
    public void changeBackground() {
        viewType = DAY_VIEW;
        changeButtonBackground(buttonDayView);
    }

    // This will remove the event from calendar - Added by Muddassir
    private boolean cancelEvent(WeekViewEvent event) {
        final int month = event.getStartTime().getTime().getMonth();
        List<WeekViewEvent> eventList = extractEvent(month + 1);
        for (WeekViewEvent viewEvent : eventList) {
            if (event.getStartTime().getTime().getTime() == viewEvent.getStartTime().getTime().getTime()) {
                eventList.remove(viewEvent);
                eventMap.put(month + 1, eventList);
                mWeekView.notifyDatasetChanged();
                count--;
                return true;
            }
        }
        return false;
    }

    /**
     * Convert the given Calendar touch time and compute its slot
     * Added by Muddassir
     *
     * @param time
     * @return Date[] of start and end time of the slot
     */
    private Date[] convertTime(Calendar time) {
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

        startTime.setTime(timeInMillis * 60 * 1000);
        timeInMillis = timeInMillis + 15;
        date.setTime(timeInMillis * 60 * 1000);

        endTime.setTime(timeInMillis * 60 * 1000);
        Date[] startNend = new Date[2];
        startNend[START] = startTime;
        startNend[END] = endTime;
        return startNend;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        calendarPreference.edit().clear().commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_today) {
            mWeekView.goToToday();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
