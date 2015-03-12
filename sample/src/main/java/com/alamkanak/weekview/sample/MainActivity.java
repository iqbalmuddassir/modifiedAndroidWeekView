package com.alamkanak.weekview.sample;

import android.content.Intent;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GestureDetectorCompat;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.alamkanak.weekview.activities.SimpleActivity;
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
        WeekView.EventClickListener, WeekView.EventLongPressListener,
        WeekView.EmptyViewClickListener, WeekView.EmptyViewLongPressListener,
        WeekView.ChangeBackgroundListener {

    // Constants for month identifier - Added by Muddassir
    private static final int JAN = 1, FEB = 2, MAR = 3, APR = 4, MAY = 5, JUN = 6, JUL = 7, AUG = 8,
            SEP = 9, OCT = 10, NOV = 11, DEC = 12;
    private static final int DAY_VIEW = 1;
    private static final int WEEK_VIEW = 2;
    private static final int MONTH_VIEW = 3;

    // For event Swipe - Added by Muddassir
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    // This is the counter for event count - can be removed after testing
    private static int count = 1;

    // To keep record of viewType being shown - Added by Muddassir
    private static int viewType;
    // This map is used to store the events
    HashMap<Integer, List<WeekViewEvent>> eventMap = new HashMap<>();
    // Typeface for text - Added by Muddassir
    Typeface ralewayLight, ralewayRegular;
    TextView monthText, headerTitle, stylistOptionTitle;
    private View.OnTouchListener gestureListener;
    private GestureDetectorCompat leftGestureDetector, rightGestureDetector, previousGesture;

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
                monthText.setTypeface(ralewayRegular); // Added by Muddassir
            }
        }

    };

    // Variables to be used in the program - Added by Muddassir
    private FragmentManager manager = null;
    private SimpleDateFormat formatter;
    private Date[] startEndTime;
    private WeekViewEvent event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponents();
    }

    // Initialize all the required components
    private void initComponents() {
        // Set the view type to Day view
        viewType = DAY_VIEW;

        // Initialise the typefaces
        ralewayLight = Typeface.createFromAsset(getAssets(),
                "fonts/RalewayLight.ttf"); // Added by Muddassir
        ralewayRegular = Typeface.createFromAsset(getAssets(),
                "fonts/RalewayRegular.ttf"); // Added by Muddassir

        // To record the button pressed - Added by Muddassir
        headerTitle = (TextView) findViewById(R.id.header_title);
        stylistOptionTitle = (TextView) findViewById(R.id.stylist_option_title);
        buttonDayView = (Button) findViewById(R.id.action_day_view);
        buttonWeekView = (Button) findViewById(R.id.action_week_view);
        buttonMonthView = (Button) findViewById(R.id.action_month_view);
        buttonDayView.setTypeface(ralewayRegular);
        buttonWeekView.setTypeface(ralewayRegular);
        buttonMonthView.setTypeface(ralewayRegular);
        headerTitle.setTypeface(ralewayRegular);
        stylistOptionTitle.setTypeface(ralewayRegular);

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

        // to toggle button
        mWeekView.setmBackgroundListener(this);

        // Setup start and end time of the calendar view - Added by Muddassir
        mWeekView.setmEndMinute("19:30:00");
        mWeekView.setmStartMinute("09:30:00");
        mWeekView.setEmptyViewClickListener(this); // Added by Muddassir
        mWeekView.setEmptyViewLongPressListener(this); // Added by Muddassir

        // Caldroid fragment for month view calendar
        customMonthCalendar = new CustomMonthCalendar();

        customMonthCalendar.setCaldroidListener(listener);

        // Fetch all events
        getEventFromDatabase();

        // Gesture detection for left swipe
        leftGestureDetector = new GestureDetectorCompat(this, new MyLeftGestureDetector());

        // Gesture detection for right swipe
        rightGestureDetector = new GestureDetectorCompat(this, new MyRightGestureDetector());
    }

    public void onClick(View view) {

        mWeekView.goToToday();
        switch (view.getId()) {
            // When Day view is clicked
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
                viewType = DAY_VIEW;

                // Change the button colors - Added by Muddassir
                changeButtonBackground(buttonDayView);
                break;

            // When Week view is clicked
            case R.id.action_week_view:
                getSupportFragmentManager().beginTransaction().remove(customMonthCalendar).commitAllowingStateLoss();
                mWeekView.setVisibility(View.VISIBLE);
                mWeekView.setEventTextSize((int) TypedValue.applyDimension
                        (TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));

                // Set the number of visible days to seven
                mWeekView.setNumberOfVisibleDays(7);
                viewType = WEEK_VIEW;
                changeButtonBackground(buttonWeekView);
                break;

            // When Month view is clicked
            case R.id.action_month_view:
                viewType = MONTH_VIEW;
                mWeekView.setVisibility(View.GONE);

                // open the month view calendar
                if (manager == null) {
                    Bundle args = new Bundle();
                    Calendar cal = Calendar.getInstance();
                    args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
                    args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
                    args.putBoolean(CaldroidFragment.ENABLE_SWIPE, true);
                    args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, false);

                    //Uncomment this to customize startDayOfWeek
                /*args.putInt(CaldroidFragment.START_DAY_OF_WEEK,
                CaldroidFragment.TUESDAY);*/

                    customMonthCalendar.setArguments(args);
                }
                manager = getSupportFragmentManager();
                manager.beginTransaction()
                        .replace(R.id.calendar_layout, customMonthCalendar).commitAllowingStateLoss();
                customMonthCalendar.refreshView();
                viewType = WEEK_VIEW;
                changeButtonBackground(buttonMonthView);
                break;

            case R.id.back_button:

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
        Toast.makeText(MainActivity.this, "Pressed event: " + event.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
        this.event = event;
        if (viewType == DAY_VIEW) {
            /** This stores the previous gesture detector component attached to WeekView
             *  Added by Muddassir
             */
            gestureListener = new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    return leftGestureDetector.onTouchEvent(event);
                }
            };
            previousGesture = mWeekView.getmGestureDetector();
            mWeekView.setmGestureDetector(leftGestureDetector);
        }
    }

    @Override
    public void onEmptyViewClicked(Calendar time) {
        startEndTime = convertTime(time);
        // This tries to add event - Added by Muddassir
        if (addEvent(startEndTime[0], startEndTime[1], "This is the New Event Test added :" + count)) {
            Toast.makeText(MainActivity.this, "Successful", Toast.LENGTH_SHORT).show();
            count++;
        } else {
            Toast.makeText(MainActivity.this, "Failed to add event", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onEmptyViewLongPress(Calendar time) {
        startEndTime = convertTime(time);
        if (viewType == DAY_VIEW) {
            /** This stores the previous gesture detector component attached to WeekView
             *  Added by Muddassir
             */
            gestureListener = new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    return rightGestureDetector.onTouchEvent(event);
                }
            };
            previousGesture = mWeekView.getmGestureDetector();
            mWeekView.setmGestureDetector(rightGestureDetector);
        }
    }

    // This will remove the event from calendar - Added by Muddassir
    private boolean cancelEvent(WeekViewEvent event) {
        final int month = event.getStartTime().getTime().getMonth();
        List<WeekViewEvent> availableSlots = extractEvent(month + 1);
        for (WeekViewEvent addedSot : availableSlots) {
            if (event.getStartTime().getTime().getTime() == addedSot.getStartTime().getTime().getTime()) {
                availableSlots.remove(addedSot);
                eventMap.put(month + 1, availableSlots);
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
        startNend[0] = startTime;
        startNend[1] = endTime;
        return startNend;
    }

    // It will implement the left swipe on the events to cancel it - Added by Muddassir
    class MyLeftGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE/* && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY*/) {
                    cancelEvent(event);
                }
                mWeekView.setmGestureDetector(previousGesture);
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

    // It will implement the right swipe on the empty view to add blank - Added by Muddassir
    class MyRightGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE/* && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY*/) {
                    if (!addEvent(startEndTime[0], startEndTime[1], "")) {
                        Toast.makeText(MainActivity.this, "Failed to add event", Toast.LENGTH_SHORT).show();
                    }
                }
                mWeekView.setmGestureDetector(previousGesture);
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }
}
