package com.alamkanak.weekview;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.OverScroller;
import android.widget.Scroller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Raquib-ul-Alam Kanak on 7/21/2014.
 * Website: http://alamkanak.me
 * Edited by - Muddassir Iqbal, Lakshmi Bansal
 */
public class WeekView extends View {

    @Deprecated
    public static final int LENGTH_SHORT = 1;
    @Deprecated
    public static final int LENGTH_LONG = 2;
    @Deprecated
    private int mDayNameLength = LENGTH_LONG;

    // For Shared Preferences
    private static final String CALENDAR_PREFERENCES = "Calendar date";
    private static final String DATE_KEY_WEEK = "Week View";
    private final Context mContext;
    // Typeface for text - Added by Muddassir
    Typeface textTypeface;
    String textTypefaceName;
    private SharedPreferences calendarPreference;
    private Calendar mToday;
    private Calendar mStartDate;
    private int mStartMinute;
    private int mEndMinute;
    private int quarterCount;
    private Paint mTimeTextPaint;
    private float mTimeTextWidth;
    private float mTimeTextHeight;
    private Paint mHeaderTextPaint;
    private Paint mHeaderRowTextPaint; // Added by Muddassir
    private float mHeaderTextHeight;
    private GestureDetectorCompat mGestureDetector;
    private OverScroller mScroller;
    private Scroller mStickyScroller;
    private PointF mCurrentOrigin = new PointF(0f, 0f);
    private Direction mCurrentScrollDirection = Direction.NONE;
    private Paint mHeaderBackgroundPaint;
    private float mWidthPerDay;
    private float mWidthPerDayOriginal; // Added - Muddassir
    private Paint mDayBackgroundPaint;
    private Paint mHourSeparatorPaint;
    private float mHeaderMarginBottom;
    private Paint mTodayBackgroundPaint;
    private Paint mTodayHeaderTextPaint;
    private Paint mEventBackgroundPaint;
    private float mHeaderColumnWidth;
    private List<EventRect> mEventRects;
    private RectF monthRect;
    private TextPaint mEventTextPaint;
    private Paint mHeaderColumnBackgroundPaint;
    private int mFetchedMonths[] = new int[3];
    private boolean mRefreshEvents = false;
    private float mDistanceY = 0;
    private float mDistanceX = 0;
    private Direction mCurrentFlingDirection = Direction.NONE;
    // Attributes and their default values.
    private int mHourHeight = 50;
    private int mColumnGap = 10;
    private int mFirstDayOfWeek = Calendar.MONDAY;
    private int mTextSize = 14;
    private int mHeaderColumnPadding = 10;
    private int mHeaderColumnTextColor = Color.BLACK;
    private int mNumberOfVisibleDays = 1;
    private int mHeaderRowPadding = 15;
    private int mHeaderRowTextColor = Color.rgb(134, 131, 132); // Added by Muddassir
    private int mHeaderRowBackgroundColor = Color.rgb(224, 20, 79); // Edited by Muddassir
    private int mDayBackgroundColor = Color.rgb(235, 236, 236); // Color.rgb(245, 245, 245); // Change by Muddassir
    private int mHourSeparatorColor = Color.WHITE;/*Color.rgb(221, 222, 222);*/ // Edited by Muddassir
    private int mTodayBackgroundColor = Color.rgb(235, 236, 236);  //Color.rgb(239, 247, 254); // Changed by Muddassir
    private int mHourSeparatorHeight = 2;
    private int mTodayHeaderTextColor = Color.rgb(224, 20, 79); // Edited - Muddassir
    private int mEventTextSize = 12;
    private int mEventTextColor = Color.BLACK;
    private int mEventPadding = 8;
    private int mHeaderColumnBackgroundColor = Color.WHITE;
    private int mDefaultEventColor = Color.rgb(174, 208, 238); // Changed by Muddassir
    private boolean mIsFirstDraw = true;
    private int mOverlappingEventGap = 0;
    private int mEventMarginVertical = 0;
    private float mXScrollingSpeed = 0.5f; // changed
    private Calendar mFirstVisibleDay;
    private Calendar mLastVisibleDay;
    // To make dates clickable - Added by Lakshmi
    private List<RectF> rectList = new ArrayList<>();
    private Map<RectF, Calendar> rectanglesWithDate = new HashMap<>();
    private RectF headerRect;
    private boolean fromMonthView = false;
    // Listeners.
    private EventClickListener mEventClickListener;
    private EventLongPressListener mEventLongPressListener;
    private MonthChangeListener mMonthChangeListener;
    private EmptyViewClickListener mEmptyViewClickListener;
    private EmptyViewLongPressListener mEmptyViewLongPressListener;
    private final GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            mScroller.forceFinished(true);
            mStickyScroller.forceFinished(true);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (mCurrentScrollDirection == Direction.NONE) {
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    mCurrentScrollDirection = Direction.HORIZONTAL;
                    mCurrentFlingDirection = Direction.HORIZONTAL;
                } else {
                    mCurrentFlingDirection = Direction.VERTICAL;
                    mCurrentScrollDirection = Direction.VERTICAL;
                }
            }
            mDistanceX = distanceX;
            mDistanceY = distanceY;
            invalidate();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            mScroller.forceFinished(true);
            mStickyScroller.forceFinished(true);

            quarterCount = ((mEndMinute - mStartMinute) / 15);
            if (mCurrentFlingDirection == Direction.HORIZONTAL) {
                mScroller.fling((int) mCurrentOrigin.x, 0,
                        (int) (velocityX * mXScrollingSpeed), 0,
                        Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
                rectanglesWithDate.clear();
                rectList.clear();
            } else if (mCurrentFlingDirection == Direction.VERTICAL) {
                //mScroller.fling(0, (int) mCurrentOrigin.y, 0, (int) velocityY, 0, 0, (int) -(mHourHeight * 24 + mHeaderTextHeight + mHeaderRowPadding * 2 - getHeight()), 0);
                mScroller.fling(0, (int) mCurrentOrigin.y, 0,
                        (int) velocityY, 0, 0,
                        (int) -(mHourHeight * quarterCount + mHeaderTextHeight +
                                mHeaderRowPadding * 68 / 15 - getHeight()), 0); // Edited by Muddassir
            }

            ViewCompat.postInvalidateOnAnimation(WeekView.this);
            return true;
        }


        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // If the tap was on an event then trigger the callback.
            if (mEventRects != null && mEventClickListener != null) {
                List<EventRect> reversedEventRects = mEventRects;
                Collections.reverse(reversedEventRects);
                for (EventRect event : reversedEventRects) {
                    if (event.rectF != null && e.getX() > event.rectF.left && e.getX() < event.rectF.right && e.getY() > event.rectF.top && e.getY() < event.rectF.bottom) {
                        mEventClickListener.onEventClick(event.originalEvent, event.rectF);
                        playSoundEffect(SoundEffectConstants.CLICK);
                        return super.onSingleTapConfirmed(e);
                    }
                }
            }

            // If the tap was on in an empty space, then trigger the callback.
            if (mEmptyViewClickListener != null && e.getX() > mHeaderColumnWidth && e.getY() > (mHeaderTextHeight + mHeaderRowPadding * 68 / 15 + mHeaderMarginBottom)) { // Changed
                Calendar selectedTime = getTimeFromPoint(e.getX(), e.getY());
                if (selectedTime != null) {
                    playSoundEffect(SoundEffectConstants.CLICK);
                    mEmptyViewClickListener.onEmptyViewClicked(selectedTime);
                }
            }

            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);

            if (mEventLongPressListener != null && mEventRects != null) {
                List<EventRect> reversedEventRects = mEventRects;
                Collections.reverse(reversedEventRects);
                for (EventRect event : reversedEventRects) {
                    if (event.rectF != null && e.getX() > event.rectF.left && e.getX() < event.rectF.right && e.getY() > event.rectF.top && e.getY() < event.rectF.bottom) {
                        mEventLongPressListener.onEventLongPress(event.originalEvent, event.rectF);
                        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        return;
                    }
                }
            }

            // If the tap was on in an empty space, then trigger the callback.
            if (mEmptyViewLongPressListener != null && e.getX() > mHeaderColumnWidth && e.getY() > (mHeaderTextHeight + mHeaderRowPadding * 68 / 15 + mHeaderMarginBottom)) { // Changed
                Calendar selectedTime = getTimeFromPoint(e.getX(), e.getY());
                if (selectedTime != null) {
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    mEmptyViewLongPressListener.onEmptyViewLongPress(selectedTime);
                }
            }
        }
    };
    private ChangeBackgroundListener mBackgroundListener;
    private DateTimeInterpreter mDateTimeInterpreter;

    public WeekView(Context context) {
        this(context, null);
    }

    public WeekView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Hold references.
        mContext = context;

        // Get the attribute values (if any).
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.WeekView, 0, 0);
        try {
            mFirstDayOfWeek = a.getInteger(R.styleable.WeekView_firstDayOfWeek, mFirstDayOfWeek);
            mHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_hourHeight, mHourHeight);
            mTextSize = a.getDimensionPixelSize(R.styleable.WeekView_textSize, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSize, context.getResources().getDisplayMetrics()));
            mHeaderColumnPadding = a.getDimensionPixelSize(R.styleable.WeekView_headerColumnPadding, mHeaderColumnPadding);
            mColumnGap = a.getDimensionPixelSize(R.styleable.WeekView_columnGap, mColumnGap);
            mHeaderColumnTextColor = a.getColor(R.styleable.WeekView_headerColumnTextColor, mHeaderColumnTextColor);
            mNumberOfVisibleDays = a.getInteger(R.styleable.WeekView_noOfVisibleDays, mNumberOfVisibleDays);
            mHeaderRowPadding = a.getDimensionPixelSize(R.styleable.WeekView_headerRowPadding, mHeaderRowPadding);
            mHeaderRowBackgroundColor = a.getColor(R.styleable.WeekView_headerRowBackgroundColor, mHeaderRowBackgroundColor);
            mDayBackgroundColor = a.getColor(R.styleable.WeekView_dayBackgroundColor, mDayBackgroundColor);
            mHourSeparatorColor = a.getColor(R.styleable.WeekView_hourSeparatorColor, mHourSeparatorColor);
            mTodayBackgroundColor = a.getColor(R.styleable.WeekView_todayBackgroundColor, mTodayBackgroundColor);
            mHourSeparatorHeight = a.getDimensionPixelSize(R.styleable.WeekView_hourSeparatorHeight, mHourSeparatorHeight);
            mTodayHeaderTextColor = a.getColor(R.styleable.WeekView_todayHeaderTextColor, mTodayHeaderTextColor);
            mEventTextSize = a.getDimensionPixelSize(R.styleable.WeekView_eventTextSize, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mEventTextSize, context.getResources().getDisplayMetrics()));
            mEventTextColor = a.getColor(R.styleable.WeekView_eventTextColor, mEventTextColor);
            mEventPadding = a.getDimensionPixelSize(R.styleable.WeekView_hourSeparatorHeight, mEventPadding);
            mHeaderColumnBackgroundColor = a.getColor(R.styleable.WeekView_headerColumnBackground, mHeaderColumnBackgroundColor);
            mDayNameLength = a.getInteger(R.styleable.WeekView_dayNameLength, mDayNameLength);
            mOverlappingEventGap = a.getDimensionPixelSize(R.styleable.WeekView_overlappingEventGap, mOverlappingEventGap);
            mEventMarginVertical = a.getDimensionPixelSize(R.styleable.WeekView_eventMarginVertical, mEventMarginVertical);
            mXScrollingSpeed = a.getFloat(R.styleable.WeekView_xScrollingSpeed, mXScrollingSpeed);
            textTypefaceName = a.getString(R.styleable.WeekView_typeface); // added by Muddassir
        } finally {
            a.recycle();
        }

        init();
    }

    public void setFromMonthView(boolean fromMonthView) {
        this.fromMonthView = fromMonthView;
    }

    public GestureDetectorCompat getmGestureDetector() {
        return mGestureDetector;
    }

    public void setmGestureDetector(GestureDetectorCompat mGestureDetector) {
        this.mGestureDetector = mGestureDetector;
    }

    public void setmScroller(OverScroller mScroller) {
        this.mScroller = mScroller;
    }

    public void setmStickyScroller(Scroller mStickyScroller) {
        this.mStickyScroller = mStickyScroller;
    }

    public ChangeBackgroundListener getmBackgroundListener() {
        return mBackgroundListener;
    }

    public void setmBackgroundListener(ChangeBackgroundListener mBackgroundListener) {
        this.mBackgroundListener = mBackgroundListener;
    }

    public void setTextTypeface(Typeface textTypeface) {
        this.textTypeface = textTypeface;
    }

    // Get the end time of the calendar view - Added by Muddassir
    public int getmEndMinute() {
        return mEndMinute;
    }

    // Set the end time of the calendar view - Added by Muddassir
    public void setmEndMinute(String endTime) {
        DateFormat sdf = new SimpleDateFormat("hh:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(endTime);
            mEndMinute = (date.getHours() * 4 + date.getMinutes() / 15) * 15;
            System.out.println("Time: " + sdf.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    // Get the start time of the calendar view - Added by Muddassir
    public int getmStartMinute() {
        return mStartMinute;
    }

    // Set the start time of the calendar view - Added by Muddassir
    public void setmStartMinute(String startTime) {
        DateFormat sdf = new SimpleDateFormat("hh:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(startTime);
            mStartMinute = (date.getHours() * 4 + date.getMinutes() / 15) * 15;
            System.out.println("Time: " + sdf.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        // Get the date today.
        mToday = Calendar.getInstance();
        mToday.set(Calendar.HOUR_OF_DAY, 0);
        mToday.set(Calendar.MINUTE, 0);
        mToday.set(Calendar.SECOND, 0);

        // Scrolling initialization.
        mGestureDetector = new GestureDetectorCompat(mContext, mGestureListener);
        mScroller = new OverScroller(mContext);
        mStickyScroller = new Scroller(mContext);

        // Measure settings for time column.
        mTimeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTimeTextPaint.setTextAlign(Paint.Align.RIGHT);
        mTimeTextPaint.setTextSize(mTextSize);
        mTimeTextPaint.setColor(mHeaderColumnTextColor);
        Rect rect = new Rect();
        mTimeTextPaint.getTextBounds("00 PM", 0, "00 PM".length(), rect);
        mTimeTextWidth = mTimeTextPaint.measureText("00 PM");
        mTimeTextHeight = rect.height();
        mHeaderMarginBottom = mTimeTextHeight / 2;

        // Measure settings for header row.
        mHeaderTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHeaderTextPaint.setColor(mHeaderColumnTextColor);
        mHeaderTextPaint.setTextAlign(Paint.Align.CENTER);
        mHeaderTextPaint.setTextSize(mTextSize);
        mHeaderTextPaint.getTextBounds("00 PM", 0, "00 PM".length(), rect);
        mHeaderTextHeight = rect.height();
        mHeaderTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        // Measure settings for header row text.- Added By Muddassir
        mHeaderRowTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHeaderRowTextPaint.setColor(mHeaderRowTextColor);
        mHeaderRowTextPaint.setTextAlign(Paint.Align.CENTER);
        mHeaderRowTextPaint.setTextSize(mTextSize);
        mHeaderRowTextPaint.getTextBounds("00 PM", 0, "00 PM".length(), rect);
        mHeaderRowTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        // Prepare header background paint.
        mHeaderBackgroundPaint = new Paint();
        mHeaderBackgroundPaint.setColor(/*Color.WHITE*/mHeaderRowBackgroundColor);

        // Prepare day background color paint.
        mDayBackgroundPaint = new Paint();
        mDayBackgroundPaint.setColor(mDayBackgroundColor);

        // Prepare hour separator color paint.
        mHourSeparatorPaint = new Paint();
        mHourSeparatorPaint.setStyle(Paint.Style.STROKE);
        mHourSeparatorPaint.setStrokeWidth(mHourSeparatorHeight);
        mHourSeparatorPaint.setColor(mHourSeparatorColor);

        // Prepare today background color paint.
        mTodayBackgroundPaint = new Paint();
        mTodayBackgroundPaint.setColor(mTodayBackgroundColor);

        // Prepare today header text color paint.
        mTodayHeaderTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTodayHeaderTextPaint.setTextAlign(Paint.Align.CENTER);
        mTodayHeaderTextPaint.setTextSize(mTextSize);
        mTodayHeaderTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTodayHeaderTextPaint.setColor(mTodayHeaderTextColor);

        // Prepare event background color.
        mEventBackgroundPaint = new Paint();
        mEventBackgroundPaint.setColor(mDefaultEventColor);

        // Prepare header column background color.
        mHeaderColumnBackgroundPaint = new Paint();
        mHeaderColumnBackgroundPaint.setColor(mHeaderColumnBackgroundColor);

        // Prepare event text size and color.
        mEventTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        mEventTextPaint.setStyle(Paint.Style.FILL);
        mEventTextPaint.setColor(mEventTextColor);
        mEventTextPaint.setTextSize(mEventTextSize);
        mStartDate = (Calendar) mToday.clone();

        // Set default event color.
        mDefaultEventColor = Color.parseColor("#9fc6e7");

        // Set Typeface of the texts - Added by Muddassir
        if (textTypefaceName != null) {
            textTypeface = Typeface.createFromAsset(getContext().getAssets(), textTypefaceName);
            setCustomTextTypeface(textTypeface);
        }

        // Initialise Shared Preferences

        calendarPreference = mContext.getSharedPreferences
                (CALENDAR_PREFERENCES, Context.MODE_PRIVATE);
    }

    // Set the typeface of all the texts - Added by Muddassir
    private void setCustomTextTypeface(Typeface textTypeface) {
        mTimeTextPaint.setTypeface(textTypeface);
        mEventTextPaint.setTypeface(textTypeface);
        mEventTextPaint.setTypeface(textTypeface);
        mTodayHeaderTextPaint.setTypeface(textTypeface);
        mHeaderTextPaint.setTypeface(textTypeface);
        mHeaderTextPaint.setFakeBoldText(true);
        mHeaderRowTextPaint.setTypeface(textTypeface);
        mHeaderRowTextPaint.setFakeBoldText(true);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the header row.
        String[] dayDate = drawHeaderRowAndEvents(canvas);

        // Draw the time column and all the axes/separators.
        drawTimeColumnAndAxes(canvas);

        // Hide everything in the first cell (top left corner).
        //canvas.drawRect(0, 0, mTimeTextWidth + mHeaderColumnPadding * 2, mHeaderTextHeight + mHeaderRowPadding * 2, mHeaderBackgroundPaint);
        monthRect = new RectF(0, 0, mTimeTextWidth + mHeaderColumnPadding * 8, mHeaderTextHeight + mHeaderRowPadding * 68 / 15); // Edited by Lakshmi
        canvas.drawRect(monthRect, mHeaderBackgroundPaint); // Edited by Lakshmi

        // Edited by Lakshmi
        if (dayDate != null) {
            mHeaderTextPaint.setColor(Color.WHITE);
            canvas.drawText(dayDate[0], mHeaderColumnPadding * 8, mHeaderRowPadding * 3.5f, mHeaderRowTextPaint);
            canvas.drawText(dayDate[1], mHeaderColumnPadding * 8, mHeaderRowPadding * 2.5f, mHeaderRowTextPaint);
        }
        // Hide anything that is in the bottom margin of the header row.
        //canvas.drawRect(mHeaderColumnWidth, mHeaderTextHeight * 2 + mHeaderRowPadding * 68 / 15, getWidth(), mHeaderRowPadding * 68 / 15 + mHeaderTextHeight + mHeaderMarginBottom + mTimeTextHeight / 2 - mHourSeparatorHeight / 2, mHeaderColumnBackgroundPaint);// Changed
    }

    private void drawTimeColumnAndAxes(Canvas canvas) {
        // Do not let the view go above/below the limit due to scrolling. Set the max and min limit of the scroll.
        quarterCount = ((mEndMinute - mStartMinute) / 15); // Added by Muddassir for quarter calculation
        if (mCurrentScrollDirection == Direction.VERTICAL) {
            if (mCurrentOrigin.y - mDistanceY > 0) mCurrentOrigin.y = 0;
            /*else if (mCurrentOrigin.y - mDistanceY < -(mHourHeight * 24 + mHeaderTextHeight + mHeaderRowPadding * 2 - getHeight()))
                mCurrentOrigin.y = -(mHourHeight * 24 + mHeaderTextHeight + mHeaderRowPadding * 2 - getHeight());*/
            else if (mCurrentOrigin.y - mDistanceY < -(mHourHeight * quarterCount + mHeaderTextHeight + mHeaderRowPadding * 68 / 15 - getHeight())) // Edited by Muddassir
                mCurrentOrigin.y = -(mHourHeight * quarterCount + mHeaderTextHeight + mHeaderRowPadding * 68 / 15 - getHeight());

            else mCurrentOrigin.y -= mDistanceY;
        }

        // Draw the background color for the header column.
        canvas.drawRect(0, mHeaderTextHeight + mHeaderRowPadding * 3, mHeaderColumnWidth, getHeight(), mHeaderColumnBackgroundPaint); // Edited by Muddassir

        // Calculate initial hours and quarter
        int initHour = mStartMinute / 60;
        int quarter = (mStartMinute % 60) / 15;
        /*for (int i = 0; i < 24; i++) {*/ // Changed
        for (int i = 0; i < quarterCount; i++) { // Edited by Muddassir
            float top = mHeaderTextHeight + mHeaderRowPadding * 68 / 15 + mCurrentOrigin.y + mHourHeight * i + mHeaderMarginBottom; // to change top of column header, Edited by Muddassir
            if (quarter == 4) {
                quarter = 0;
                initHour++;
            }
            // Draw the text if its y position is not outside of the visible area. The pivot point of the text is the point at the bottom-right corner.
            // String time = getDateTimeInterpreter().interpretTime(i);
            String time = getDateTimeInterpreter().interpretQuarter(initHour, quarter); // Added by Muddassir
            quarter++;
            if (time == null)
                throw new IllegalStateException("A DateTimeInterpreter must not return null time");
            if (top < getHeight()) {
                if (time.contains("AM") || time.contains("PM")) {
                    mTimeTextPaint.setFakeBoldText(true);
                } else {
                    mTimeTextPaint.setFakeBoldText(false);
                }
                canvas.drawText(time, mTimeTextWidth + mHeaderColumnPadding * 3, top + mTimeTextHeight, mTimeTextPaint); // Edited by Muddassir
            }
        }
    }

    private String[] drawHeaderRowAndEvents(Canvas canvas) {
        // Calculate the available width for each day.
        mHeaderColumnWidth = mTimeTextWidth + mHeaderColumnPadding * 8; // Edited by Muddassir
        mWidthPerDay = getWidth() - mHeaderColumnWidth - mColumnGap * (mNumberOfVisibleDays - 1);

        // To make the tiles square shaped - Added by Muddassir
        mHourHeight = (int) mWidthPerDay / 7;

        // To record the column width of the one day view - Added by Muddassir
        if (mIsFirstDraw) {
            mWidthPerDayOriginal = mWidthPerDay;
            mIsFirstDraw = false;
        }
        mWidthPerDay = mWidthPerDay / mNumberOfVisibleDays;

        // If the week view is being drawn for the first time, then consider the first day of week.
        if (mIsFirstDraw && mNumberOfVisibleDays > 7) { // Changed by Muddassir
            if (mToday.get(Calendar.DAY_OF_WEEK) != mFirstDayOfWeek) {
                int difference = 7 + (mToday.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek);
                mCurrentOrigin.x += (mWidthPerDay + mColumnGap) * difference;
            }
            //mIsFirstDraw = false; // Changed
        }

        // Consider scroll offset.
        if (mCurrentScrollDirection == Direction.HORIZONTAL) {
            mCurrentOrigin.x -= mDistanceX;
        }
        int leftDaysWithGaps = (int) -(Math.ceil(mCurrentOrigin.x / (mWidthPerDay + mColumnGap)));
        float startFromPixel = mCurrentOrigin.x + (mWidthPerDay + mColumnGap) * leftDaysWithGaps +
                mHeaderColumnWidth;
        float startPixel = startFromPixel;

        // Prepare to iterate for each day.
        Calendar day = (Calendar) mToday.clone();
        day.add(Calendar.HOUR, 6);

        // Prepare to iterate for each hour to draw the hour lines.
        int lineCount = (int) ((getHeight() - mHeaderTextHeight - mHeaderRowPadding * 68 / 15 - // Changed by Muddassir
                mHeaderMarginBottom) / mHourHeight) + 1;
        lineCount = (lineCount) * (mNumberOfVisibleDays + 1);
        float[] hourLines = new float[lineCount * 4];

        // Clear the cache for event rectangles.
        if (mEventRects != null) {
            for (EventRect eventRect : mEventRects) {
                eventRect.rectF = null;
            }
        }

        // Iterate through each day.
        mFirstVisibleDay = (Calendar) mToday.clone();
        mFirstVisibleDay.add(Calendar.DATE, leftDaysWithGaps);
        for (int dayNumber = leftDaysWithGaps + 1;
             dayNumber <= leftDaysWithGaps + mNumberOfVisibleDays + 1;
             dayNumber++) {

            // Check if the day is today.
            day = (Calendar) mToday.clone();
            mLastVisibleDay = (Calendar) day.clone();
            day.add(Calendar.DATE, dayNumber - 1);
            mLastVisibleDay.add(Calendar.DATE, dayNumber - 2);
            boolean sameDay = isSameDay(day, mToday);

            // Get more events if necessary. We want to store the events 3 months beforehand. Get
            // events only when it is the first iteration of the loop.
            if (mEventRects == null || mRefreshEvents || (dayNumber == leftDaysWithGaps + 1 && mFetchedMonths[1] != day.get(Calendar.MONTH) + 1 && day.get(Calendar.DAY_OF_MONTH) == 15)) {
                getMoreEvents(day);
                mRefreshEvents = false;
            }

            // Draw background color for each day.
            float start = (startPixel < mHeaderColumnWidth ? mHeaderColumnWidth : startPixel);
            if (mWidthPerDay + startPixel - start > 0)
                canvas.drawRect(start, mHeaderTextHeight + mHeaderRowPadding * 60 / 15 + mTimeTextHeight / 2 + mHeaderMarginBottom, startPixel + mWidthPerDay, getHeight(), sameDay ? mTodayBackgroundPaint : mDayBackgroundPaint);

            // Prepare the separator lines for hours.
            int i = 0;
            quarterCount = ((mEndMinute - mStartMinute) / 15);
            for (int hourNumber = 0; hourNumber < quarterCount; hourNumber++) {
                float top = mHeaderTextHeight + mHeaderRowPadding * 60 / 15 + mCurrentOrigin.y + mHourHeight * hourNumber + mTimeTextHeight / 2 + mHeaderMarginBottom; // To change start line of hour separator
                if (top > mHeaderTextHeight + mHeaderRowPadding * 60 / 15 + mTimeTextHeight / 2 + mHeaderMarginBottom - mHourSeparatorHeight && top < getHeight() && startPixel + mWidthPerDay - start > 0) {
                    hourLines[i * 4] = start;
                    hourLines[i * 4 + 1] = top;
                    hourLines[i * 4 + 2] = startPixel + mWidthPerDay;
                    hourLines[i * 4 + 3] = top;
                    i++;
                }
            }

            // Draw the lines for hours.
            canvas.drawLines(hourLines, mHourSeparatorPaint);

            // Draw the events.
            drawEvents(day, startPixel, canvas);

            // In the next iteration, start from the next day.
            startPixel += mWidthPerDay + mColumnGap;
        }

        // Draw the header background.
        headerRect = new RectF(0, 0, getWidth(), mHeaderTextHeight + mHeaderRowPadding * 68 / 15);
        canvas.drawRect(headerRect, mHeaderBackgroundPaint); // Edited by Muddassir to be changed back to 68 /15

        // Draw the header row texts.
        startPixel = startFromPixel;
        String[] dayDate = null;
        for (int dayNumber = leftDaysWithGaps + 1; dayNumber <= leftDaysWithGaps + mNumberOfVisibleDays + 1; dayNumber++) {
            // Check if the day is today.
            day = (Calendar) mToday.clone();
            day.add(Calendar.DATE, dayNumber - 1);
            boolean sameDay = isSameDay(day, mToday);

            // Draw the day labels.
            String dayLabel = getDateTimeInterpreter().interpretDate(day);
            if (dayLabel == null)
                throw new IllegalStateException("A DateTimeInterpreter must not return null date");
            /*canvas.drawText(dayLabel, startPixel + mWidthPerDay / 2,
                    mHeaderTextHeight + mHeaderRowPadding, sameDay ? mTodayHeaderTextPaint : mHeaderTextPaint);*/

            /** Edited to show different texts in one day view and week view
             *  Added by Muddassir
             */
            if (this.mNumberOfVisibleDays == 1) {
                /*canvas.drawText(dayLabel, startPixel + mWidthPerDay / 2,
                        mHeaderTextHeight * 2f + mHeaderRowPadding, mHeaderTextPaint);*/
                String[] dateAndDay = dayLabel.split(",");
                Paint newPaint = new Paint();
                newPaint.setTypeface(mHeaderRowTextPaint.getTypeface());
                newPaint.setColor(mHeaderRowTextColor); // Edited by Muddassir
                newPaint.setTextSize(mTextSize * 1.5f);
                canvas.drawText(dateAndDay[1].toUpperCase().substring(0, 3) + " "
                                + dateAndDay[2], startPixel + mWidthPerDay / 2.6f,
                        mHeaderTextHeight * 2.5f + mHeaderRowPadding, newPaint);
                newPaint.setTextSize(mTextSize * 1f);
                canvas.drawText(dateAndDay[3].toUpperCase(), startPixel + mWidthPerDay / 2.6f,
                        mHeaderTextHeight * 4f + mHeaderRowPadding, newPaint);
                newPaint.setTextSize(mTextSize * 3);
                newPaint.setFakeBoldText(true);
                canvas.drawText(dateAndDay[0], startPixel + mWidthPerDay / 5,
                        mHeaderTextHeight * 3.5f + mHeaderRowPadding, newPaint);
            } else {
                // Draw rectangle for click - Added by Lakshmi
                Paint mPaint = new Paint();
                mPaint.setColor(Color.WHITE);
                mPaint.setAlpha(0); // Full transparent
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setAntiAlias(true);
                RectF bounds = new RectF(startPixel,
                        mHeaderTextHeight/* * 4*/,
                        startPixel + mWidthPerDay,
                        mHeaderTextHeight * 6 + mHeaderRowPadding);

                // Draw circle to distinguish today - Added by Muddassir
                Paint circlePaint = new Paint();
                circlePaint.setAntiAlias(true);
                circlePaint.setColor(Color.WHITE);
                if (sameDay) {
                    circlePaint.setAlpha(255);
                } else {
                    circlePaint.setAlpha(0);
                }

                // store date with the boundary rectangle - Added by Lakshmi
                rectList.add(bounds);
                rectanglesWithDate.put(bounds, day);
                canvas.drawRect(bounds, mPaint);

                canvas.drawCircle(startPixel + mWidthPerDay / 2,
                        mHeaderTextHeight * 4.25f + mHeaderRowPadding, mHeaderRowPadding * 0.8f, circlePaint);   // Changed by Muddassir
                dayDate = dayLabel.split(",");

                // Added by Muddassir
                Paint newPaint = new Paint();
                newPaint.setColor(mHeaderRowTextColor);
                newPaint.setTypeface(mHeaderTextPaint.getTypeface());
                newPaint.setFakeBoldText(mHeaderTextPaint.isFakeBoldText());
                newPaint.setTextAlign(mHeaderTextPaint.getTextAlign());
                newPaint.setTextSize(mHeaderTextPaint.getTextSize());
                /*canvas.drawText(dayDate[2], startPixel + mWidthPerDay / 2,
                        mHeaderTextHeight + mHeaderRowPadding / 2, sameDay ? mTodayHeaderTextPaint : mHeaderTextPaint);
                if(sameDay) {
                    mTodayHeaderTextPaint.setColor(Color.argb(255, 224, 20, 79));
                } else {
                    mTodayHeaderTextPaint.setColor(Color.WHITE);
                }
                canvas.drawText(dayDate[3], startPixel + mWidthPerDay / 2,
                        mHeaderTextHeight * 3 + mHeaderRowPadding / 2, mTodayHeaderTextPaint);*/

                canvas.drawText(dayDate[2], startPixel + mWidthPerDay / 2,
                        mHeaderTextHeight * 2 + mHeaderRowPadding, newPaint);
                if (sameDay) {
                    newPaint.setColor(mTodayHeaderTextColor);
                } else {
                    newPaint.setColor(mHeaderRowTextColor);
                }
                canvas.drawText(dayDate[3], startPixel + mWidthPerDay / 2,
                        mHeaderTextHeight * 4.5f + mHeaderRowPadding, newPaint);
            }
            startPixel += mWidthPerDay + mColumnGap;
        }
        return dayDate;
    }

    /**
     * Get the time and date where the user clicked on.
     *
     * @param x The x position of the touch event.
     * @param y The y position of the touch event.
     * @return The time and date at the clicked position.
     */
    public Calendar getTimeFromPoint(float x, float y) {
        int leftDaysWithGaps = (int) -(Math.ceil(mCurrentOrigin.x / (mWidthPerDay + mColumnGap)));
        float startPixel = mCurrentOrigin.x + (mWidthPerDay + mColumnGap) * leftDaysWithGaps +
                mHeaderColumnWidth;
        for (int dayNumber = leftDaysWithGaps + 1;
             dayNumber <= leftDaysWithGaps + mNumberOfVisibleDays + 1;
             dayNumber++) {
            float start = (startPixel < mHeaderColumnWidth ? mHeaderColumnWidth : startPixel);
            if (mWidthPerDay + startPixel - start > 0
                    && x > start && x < startPixel + mWidthPerDay) {
                Calendar day = (Calendar) mToday.clone();
                day.add(Calendar.DATE, dayNumber - 1);
                float pixelsFromZero = y - mCurrentOrigin.y - mHeaderTextHeight - mHeaderRowPadding * 60 / 15 - mTimeTextHeight / 2 - mHeaderMarginBottom;// Changed

                // changed for quarter - Edited by Muddassir
                int mHourHeight = this.mHourHeight * 4;
                int hour = (int) (pixelsFromZero / (mHourHeight));
                int minute = (int) (60 * (pixelsFromZero - hour * mHourHeight) / mHourHeight);
                day.add(Calendar.HOUR, hour);
                day.set(Calendar.MINUTE, minute);
                return day;
            }
            startPixel += mWidthPerDay + mColumnGap;
        }
        return null;
    }

    /**
     * Draw all the events of a particular day.
     *
     * @param date           The day.
     * @param startFromPixel The left position of the day area. The events will never go any left from this value.
     * @param canvas         The canvas to draw upon.
     */
    private void drawEvents(Calendar date, float startFromPixel, Canvas canvas) {
        if (mEventRects != null && mEventRects.size() > 0) {
            for (int i = 0; i < mEventRects.size(); i++) {
                if (isSameDay(mEventRects.get(i).event.getStartTime(), date)) {

                    // Calculate top.
                    quarterCount = ((mEndMinute - mStartMinute) / 15);  // Adjust event height
                    //float top = mHourHeight * 24 * mEventRects.get(i).top / 1440 + mCurrentOrigin.y + mHeaderTextHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom + mTimeTextHeight / 2 + mEventMarginVertical;
                    float top = ((mEventRects.get(i).top - mStartMinute) * mHourHeight) / 15 + mCurrentOrigin.y + mHeaderTextHeight + mHeaderRowPadding * 60 / 15 + mHeaderMarginBottom + mTimeTextHeight / 2; // Edited by Lakshmi
                    float originalTop = top;
                    if (top < mHeaderTextHeight + mHeaderRowPadding * 60 / 15 + mHeaderMarginBottom + mTimeTextHeight / 2)
                        top = mHeaderTextHeight + mHeaderRowPadding * 60 / 15 + mHeaderMarginBottom + mTimeTextHeight / 2;

                    // Calculate bottom.
                    float bottom = mEventRects.get(i).bottom;
                    //bottom = mHourHeight * 24 * bottom / 1440 + mCurrentOrigin.y + mHeaderTextHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom + mTimeTextHeight / 2 - mEventMarginVertical;
                    bottom = ((mEventRects.get(i).bottom - mStartMinute) * mHourHeight) / 15 + mCurrentOrigin.y + mHeaderTextHeight + mHeaderRowPadding * 60 / 15 + mHeaderMarginBottom + mTimeTextHeight / 2; // Edited by Lakshmi

                    // Calculate left and right.
                    float left = startFromPixel + mEventRects.get(i).left * mWidthPerDay;

                    // Removed this to avoid overlapping events
                    /*if (left < startFromPixel)
                        left += mOverlappingEventGap;*/

                    float originalLeft = left;
                    float right = left + mEventRects.get(i).width * mWidthPerDay;

                    // Removed this to avoid overlapping events
                    /*if (right < startFromPixel + mWidthPerDay)
                        right -= mOverlappingEventGap;*/

                    if (left < mHeaderColumnWidth) left = mHeaderColumnWidth;

                    // Draw the event and the event name on top of it.
                    RectF eventRectF = new RectF(left, top, right, bottom);
                    if (bottom > mHeaderTextHeight + mHeaderRowPadding * 60 / 15 + mHeaderMarginBottom + mTimeTextHeight / 2 && left < right && // Changed
                            eventRectF.right > mHeaderColumnWidth &&
                            eventRectF.left < getWidth() &&
                            eventRectF.bottom > mHeaderTextHeight + mHeaderRowPadding * 60 / 15 + mTimeTextHeight / 2 + mHeaderMarginBottom && //Changed
                            eventRectF.top < getHeight() &&
                            left < right
                            ) {
                        mEventRects.get(i).rectF = eventRectF;
                        mEventBackgroundPaint.setColor(mEventRects.get(i).event.getColor() == 0 ? mDefaultEventColor : mEventRects.get(i).event.getColor());
                        canvas.drawRect(mEventRects.get(i).rectF, mEventBackgroundPaint);
                        drawText(mEventRects.get(i).event.getName(), mEventRects.get(i).rectF, canvas, originalTop, originalLeft);
                    } else
                        mEventRects.get(i).rectF = null;
                }
            }
        }
    }

    /**
     * Draw the name of the event on top of the event rectangle.
     *
     * @param text         The text to draw.
     * @param rect         The rectangle on which the text is to be drawn.
     * @param canvas       The canvas to draw upon.
     * @param originalTop  The original top position of the rectangle. The rectangle may have some of its portion outside of the visible area.
     * @param originalLeft The original left position of the rectangle. The rectangle may have some of its portion outside of the visible area.
     */
    private void drawText(String text, RectF rect, Canvas canvas, float originalTop, float originalLeft) {
        if (rect.right - rect.left - mEventPadding * 2 < 0) return;

        // Get text dimensions
        StaticLayout textLayout = new StaticLayout(text, mEventTextPaint, (int) (rect.right - originalLeft - mEventPadding * 2), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

        // Crop height
        int availableHeight = (int) (rect.bottom - originalTop - mEventPadding * 2);
        int lineHeight = textLayout.getHeight() / textLayout.getLineCount();
        if (lineHeight < availableHeight && textLayout.getHeight() > rect.height() - mEventPadding * 2) {
            int lineCount = textLayout.getLineCount();
            int availableLineCount = (int) Math.floor(lineCount * availableHeight / textLayout.getHeight());
            float widthAvailable = (rect.right - originalLeft - mEventPadding * 2) * availableLineCount;
            textLayout = new StaticLayout(TextUtils.ellipsize(text, mEventTextPaint, widthAvailable, TextUtils.TruncateAt.END), mEventTextPaint, (int) (rect.right - originalLeft - mEventPadding * 2), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        } else if (lineHeight >= availableHeight) {
            int width = (int) (rect.right - originalLeft - mEventPadding * 2);
            textLayout = new StaticLayout(TextUtils.ellipsize(text, mEventTextPaint, width, TextUtils.TruncateAt.END), mEventTextPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 1.0f, false);
        }

        // Draw text
        canvas.save();
        canvas.translate(originalLeft + mEventPadding, originalTop + mEventPadding);
        textLayout.draw(canvas);
        canvas.restore();
    }

    /**
     * Gets more events of one/more month(s) if necessary. This method is called when the user is
     * scrolling the week view. The week view stores the events of three months: the visible month,
     * the previous month, the next month.
     *
     * @param day The day where the user is currently is.
     */
    private void getMoreEvents(Calendar day) {

        // Delete all events if its not current month +- 1.
        deleteFarMonths(day);

        // Get more events if the month is changed.
        if (mEventRects == null)
            mEventRects = new ArrayList<EventRect>();
        if (mMonthChangeListener == null && !isInEditMode())
            throw new IllegalStateException("You must provide a MonthChangeListener");

        // If a refresh was requested then reset some variables.
        if (mRefreshEvents) {
            mEventRects.clear();
            mFetchedMonths = new int[3];
        }

        // Get events of previous month.
        int previousMonth = (day.get(Calendar.MONTH) == 0 ? 12 : day.get(Calendar.MONTH));
        int nextMonth = (day.get(Calendar.MONTH) + 2 == 13 ? 1 : day.get(Calendar.MONTH) + 2);
        int[] lastFetchedMonth = mFetchedMonths.clone();
        if (mFetchedMonths[0] < 1 || mFetchedMonths[0] != previousMonth || mRefreshEvents) {
            if (!containsValue(lastFetchedMonth, previousMonth) && !isInEditMode()) {
                List<WeekViewEvent> events = mMonthChangeListener.onMonthChange((previousMonth == 12) ? day.get(Calendar.YEAR) - 1 : day.get(Calendar.YEAR), previousMonth);
                sortEvents(events);
                for (WeekViewEvent event : events) {
                    cacheEvent(event);
                }
            }
            mFetchedMonths[0] = previousMonth;
        }

        // Get events of this month.
        if (mFetchedMonths[1] < 1 || mFetchedMonths[1] != day.get(Calendar.MONTH) + 1 || mRefreshEvents) {
            if (!containsValue(lastFetchedMonth, day.get(Calendar.MONTH) + 1) && !isInEditMode()) {
                List<WeekViewEvent> events = mMonthChangeListener.onMonthChange(day.get(Calendar.YEAR), day.get(Calendar.MONTH) + 1);
                sortEvents(events);
                for (WeekViewEvent event : events) {
                    cacheEvent(event);
                }
            }
            mFetchedMonths[1] = day.get(Calendar.MONTH) + 1;
        }

        // Get events of next month.
        if (mFetchedMonths[2] < 1 || mFetchedMonths[2] != nextMonth || mRefreshEvents) {
            if (!containsValue(lastFetchedMonth, nextMonth) && !isInEditMode()) {
                List<WeekViewEvent> events = mMonthChangeListener.onMonthChange(nextMonth == 1 ? day.get(Calendar.YEAR) + 1 : day.get(Calendar.YEAR), nextMonth);
                sortEvents(events);
                for (WeekViewEvent event : events) {
                    cacheEvent(event);
                }
            }
            mFetchedMonths[2] = nextMonth;
        }

        // Prepare to calculate positions of each events.
        ArrayList<EventRect> tempEvents = new ArrayList<EventRect>(mEventRects);
        mEventRects = new ArrayList<EventRect>();
        Calendar dayCounter = (Calendar) day.clone();
        dayCounter.add(Calendar.MONTH, -1);
        dayCounter.set(Calendar.DAY_OF_MONTH, 1);
        Calendar maxDay = (Calendar) day.clone();
        maxDay.add(Calendar.MONTH, 1);
        maxDay.set(Calendar.DAY_OF_MONTH, maxDay.getActualMaximum(Calendar.DAY_OF_MONTH));

        // Iterate through each day to calculate the position of the events.
        while (dayCounter.getTimeInMillis() <= maxDay.getTimeInMillis()) {
            ArrayList<EventRect> eventRects = new ArrayList<EventRect>();
            for (EventRect eventRect : tempEvents) {
                if (isSameDay(eventRect.event.getStartTime(), dayCounter))
                    eventRects.add(eventRect);
            }

            // Added this loop to avoid overlapping events
            for (EventRect column : eventRects) {

                EventRect eventRect = column;
                eventRect.width = 1f;
                eventRect.left = 0;
                eventRect.top = eventRect.event.getStartTime().get(Calendar.HOUR_OF_DAY) * 60 + eventRect.event.getStartTime().get(Calendar.MINUTE);
                eventRect.bottom = eventRect.event.getEndTime().get(Calendar.HOUR_OF_DAY) * 60 + eventRect.event.getEndTime().get(Calendar.MINUTE);
                mEventRects.add(eventRect);

            }
            // computePositionOfEvents(eventRects); // Removed this to avoid overlapping events
            dayCounter.add(Calendar.DATE, 1);
        }
    }

    /**
     * Cache the event for smooth scrolling functionality.
     *
     * @param event The event to cache.
     */
    private void cacheEvent(WeekViewEvent event) {
        if (!isSameDay(event.getStartTime(), event.getEndTime())) {
            Calendar endTime = (Calendar) event.getStartTime().clone();
            endTime.set(Calendar.HOUR_OF_DAY, 23);
            endTime.set(Calendar.MINUTE, 59);
            Calendar startTime = (Calendar) event.getEndTime().clone();
            startTime.set(Calendar.HOUR_OF_DAY, 0);
            startTime.set(Calendar.MINUTE, 0);
            WeekViewEvent event1 = new WeekViewEvent(event.getId(), event.getName(), event.getStartTime(), endTime);
            event1.setColor(event.getColor());
            WeekViewEvent event2 = new WeekViewEvent(event.getId(), event.getName(), startTime, event.getEndTime());
            event2.setColor(event.getColor());
            mEventRects.add(new EventRect(event1, event, null));
            mEventRects.add(new EventRect(event2, event, null));
        } else
            mEventRects.add(new EventRect(event, event, null));
    }

    /**
     * Sorts the events in ascending order.
     *
     * @param events The events to be sorted.
     */
    private void sortEvents(List<WeekViewEvent> events) {
        Collections.sort(events, new Comparator<WeekViewEvent>() {
            @Override
            public int compare(WeekViewEvent event1, WeekViewEvent event2) {
                long start1 = event1.getStartTime().getTimeInMillis();
                long start2 = event2.getStartTime().getTimeInMillis();
                int comparator = start1 > start2 ? 1 : (start1 < start2 ? -1 : 0);
                if (comparator == 0) {
                    long end1 = event1.getEndTime().getTimeInMillis();
                    long end2 = event2.getEndTime().getTimeInMillis();
                    comparator = end1 > end2 ? 1 : (end1 < end2 ? -1 : 0);
                }
                return comparator;
            }
        });
    }

    /**
     * Calculates the left and right positions of each events. This comes handy specially if events
     * are overlapping.
     *
     * @param eventRects The events along with their wrapper class.
     */
    private void computePositionOfEvents(List<EventRect> eventRects) {
        // Make "collision groups" for all events that collide with others.
        List<List<EventRect>> collisionGroups = new ArrayList<List<EventRect>>();
        for (EventRect eventRect : eventRects) {
            boolean isPlaced = false;
            outerLoop:
            for (List<EventRect> collisionGroup : collisionGroups) {
                for (EventRect groupEvent : collisionGroup) {
                    if (isEventsCollide(groupEvent.event, eventRect.event)) {
                        collisionGroup.add(eventRect);
                        isPlaced = true;
                        break outerLoop;
                    }
                }
            }
            if (!isPlaced) {
                List<EventRect> newGroup = new ArrayList<EventRect>();
                newGroup.add(eventRect);
                collisionGroups.add(newGroup);
            }
        }

        for (List<EventRect> collisionGroup : collisionGroups) {
            expandEventsToMaxWidth(collisionGroup);
        }
    }

    /**
     * Expands all the events to maximum possible width. The events will try to occupy maximum
     * space available horizontally.
     *
     * @param collisionGroup The group of events which overlap with each other.
     */
    private void expandEventsToMaxWidth(List<EventRect> collisionGroup) {
        // Expand the events to maximum possible width.
        List<List<EventRect>> columns = new ArrayList<List<EventRect>>();
        columns.add(new ArrayList<EventRect>());
        for (EventRect eventRect : collisionGroup) {
            boolean isPlaced = false;
            for (List<EventRect> column : columns) {
                if (column.size() == 0) {
                    column.add(eventRect);
                    isPlaced = true;
                } else if (!isEventsCollide(eventRect.event, column.get(column.size() - 1).event)) {
                    column.add(eventRect);
                    isPlaced = true;
                    break;
                }
            }
            if (!isPlaced) {
                List<EventRect> newColumn = new ArrayList<EventRect>();
                newColumn.add(eventRect);
                columns.add(newColumn);
            }
        }


        // Calculate left and right position for all the events.
        int maxRowCount = columns.get(0).size();
        for (int i = 0; i < maxRowCount; i++) {
            // Set the left and right values of the event.
            float j = 0;
            for (List<EventRect> column : columns) {
                if (column.size() >= i + 1) {
                    EventRect eventRect = column.get(i);
                    eventRect.width = 1f / columns.size();
                    eventRect.left = j / columns.size();
                    eventRect.top = eventRect.event.getStartTime().get(Calendar.HOUR_OF_DAY) * 60 + eventRect.event.getStartTime().get(Calendar.MINUTE);
                    eventRect.bottom = eventRect.event.getEndTime().get(Calendar.HOUR_OF_DAY) * 60 + eventRect.event.getEndTime().get(Calendar.MINUTE);
                    mEventRects.add(eventRect);
                }
                j++;
            }
        }
    }

    /**
     * Checks if two events overlap.
     *
     * @param event1 The first event.
     * @param event2 The second event.
     * @return true if the events overlap.
     */
    private boolean isEventsCollide(WeekViewEvent event1, WeekViewEvent event2) {
        long start1 = event1.getStartTime().getTimeInMillis();
        long end1 = event1.getEndTime().getTimeInMillis();
        long start2 = event2.getStartTime().getTimeInMillis();
        long end2 = event2.getEndTime().getTimeInMillis();
        return !((start1 >= end2) || (end1 <= start2));
    }

    /**
     * Checks if time1 occurs after (or at the same time) time2.
     *
     * @param time1 The time to check.
     * @param time2 The time to check against.
     * @return true if time1 and time2 are equal or if time1 is after time2. Otherwise false.
     */
    private boolean isTimeAfterOrEquals(Calendar time1, Calendar time2) {
        return !(time1 == null || time2 == null) && time1.getTimeInMillis() >= time2.getTimeInMillis();
    }

    /**
     * Deletes the events of the months that are too far away from the current month.
     *
     * @param currentDay The current day.
     */
    private void deleteFarMonths(Calendar currentDay) {

        if (mEventRects == null) return;

        Calendar nextMonth = (Calendar) currentDay.clone();
        nextMonth.add(Calendar.MONTH, 1);
        nextMonth.set(Calendar.DAY_OF_MONTH, nextMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
        nextMonth.set(Calendar.HOUR_OF_DAY, 12);
        nextMonth.set(Calendar.MINUTE, 59);
        nextMonth.set(Calendar.SECOND, 59);

        Calendar prevMonth = (Calendar) currentDay.clone();
        prevMonth.add(Calendar.MONTH, -1);
        prevMonth.set(Calendar.DAY_OF_MONTH, 1);
        prevMonth.set(Calendar.HOUR_OF_DAY, 0);
        prevMonth.set(Calendar.MINUTE, 0);
        prevMonth.set(Calendar.SECOND, 0);

        List<EventRect> newEvents = new ArrayList<EventRect>();
        for (EventRect eventRect : mEventRects) {
            boolean isFarMonth = eventRect.event.getStartTime().getTimeInMillis() > nextMonth.getTimeInMillis() || eventRect.event.getEndTime().getTimeInMillis() < prevMonth.getTimeInMillis();
            if (!isFarMonth) newEvents.add(eventRect);
        }
        mEventRects.clear();
        mEventRects.addAll(newEvents);
    }

    public void setOnEventClickListener(EventClickListener listener) {
        this.mEventClickListener = listener;
    }

    public EventClickListener getEventClickListener() {
        return mEventClickListener;
    }


    /////////////////////////////////////////////////////////////////
    //
    //      Functions related to setting and getting the properties.
    //
    /////////////////////////////////////////////////////////////////

    public MonthChangeListener getMonthChangeListener() {
        return mMonthChangeListener;
    }

    public void setMonthChangeListener(MonthChangeListener monthChangeListener) {
        this.mMonthChangeListener = monthChangeListener;
    }

    public EventLongPressListener getEventLongPressListener() {
        return mEventLongPressListener;
    }

    public void setEventLongPressListener(EventLongPressListener eventLongPressListener) {
        this.mEventLongPressListener = eventLongPressListener;
    }

    public EmptyViewClickListener getEmptyViewClickListener() {
        return mEmptyViewClickListener;
    }

    public void setEmptyViewClickListener(EmptyViewClickListener emptyViewClickListener) {
        this.mEmptyViewClickListener = emptyViewClickListener;
    }

    public EmptyViewLongPressListener getEmptyViewLongPressListener() {
        return mEmptyViewLongPressListener;
    }

    public void setEmptyViewLongPressListener(EmptyViewLongPressListener emptyViewLongPressListener) {
        this.mEmptyViewLongPressListener = emptyViewLongPressListener;
    }

    /**
     * Get the interpreter which provides the text to show in the header column and the header row.
     *
     * @return The date, time interpreter.
     */
    public DateTimeInterpreter getDateTimeInterpreter() {
        if (mDateTimeInterpreter == null) {
            mDateTimeInterpreter = new DateTimeInterpreter() {
                @Override
                public String interpretDate(Calendar date) {
                    SimpleDateFormat sdf;
                    sdf = mDayNameLength == LENGTH_SHORT ? new SimpleDateFormat("EEEEE") : new SimpleDateFormat("EEEEE")/*new SimpleDateFormat("EEE")*/;// changed
                    try {
                        /** Extract the Formatted date for day view as well as week view
                         * Edited by Muddassir
                         */
                        if (mNumberOfVisibleDays == 1) { // New code
                            sdf = new SimpleDateFormat("dd,MMMM,yyyy,EEEE");
                            String dayName = sdf.format(date.getTime());
                            return dayName;
                        }

                        /*String dayName = sdf.format(date.getTime()).toUpperCase().substring(0,1);*/
                        String dayName = sdf.format(date.getTime()).toUpperCase().substring(0, 1);
                        /*return String.format("%s\n%d/%02d", dayName, date.get(Calendar.MONTH) + 1, date.get(Calendar.DAY_OF_MONTH));*/
                        String dayAndDate = dayName + String.format(",%d", date.get(Calendar.DAY_OF_MONTH));
                        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
                        String month_name = month_date.format(date.getTime()).toUpperCase();
                        SimpleDateFormat year = new SimpleDateFormat("yyyy");
                        String year_value = year.format(date.getTime()).toUpperCase();
                        dayAndDate = month_name + "," + dayAndDate;
                        dayAndDate = year_value + "," + dayAndDate;
                        return dayAndDate;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "";
                    }
                }

                @Override
                public String interpretTime(int hour) {
                    String amPm;
                    if (hour >= 0 && hour < 12) amPm = "AM";
                    else amPm = "PM";
                    if (hour == 0) hour = 12;
                    if (hour > 12) hour -= 12;
                    return String.format("%02d %s", hour, amPm);
                }

                @Override
                /**
                 * This method returns the time with the proper quarter format
                 * Added by Muddassir
                 */
                public String interpretQuarter(int hour, int quarter) {
                    String amPm;
                    String min = "";
                    if (quarter == 1) {
                        min = "15";
                    } else if (quarter == 2) {
                        min = "30";
                    } else if (quarter == 3) {
                        min = "45";
                    }
                    if (hour >= 0 && hour < 12) {
                        amPm = "AM";
                    } else {
                        amPm = "PM";
                    }
                    hour %= 12;
                    if (hour == 0) {
                        hour = 12;
                    }
                    if (quarter == 0 || quarter == 4) {
                        return String.format("%d %s", hour, amPm);
                    }
                    return String.format("%d:%s", hour, min);
                }
            };
        }
        return mDateTimeInterpreter;
    }

    /**
     * Set the interpreter which provides the text to show in the header column and the header row.
     *
     * @param dateTimeInterpreter The date, time interpreter.
     */
    public void setDateTimeInterpreter(DateTimeInterpreter dateTimeInterpreter) {
        this.mDateTimeInterpreter = dateTimeInterpreter;
    }

    /**
     * Get the number of visible days in a week.
     *
     * @return The number of visible days in a week.
     */
    public int getNumberOfVisibleDays() {
        return mNumberOfVisibleDays;
    }

    /**
     * Set the number of visible days in a week.
     *
     * @param numberOfVisibleDays The number of visible days in a week.
     */
    public void setNumberOfVisibleDays(int numberOfVisibleDays) {
        this.mNumberOfVisibleDays = numberOfVisibleDays;
        mCurrentOrigin.x = 0;
        mCurrentOrigin.y = 0;
        if (numberOfVisibleDays == 1) {

        }
        invalidate();
    }

    public int getHourHeight() {
        return mHourHeight;
    }

    public void setHourHeight(int hourHeight) {
        mHourHeight = hourHeight;
        invalidate();
    }

    public int getColumnGap() {
        return mColumnGap;
    }

    public void setColumnGap(int columnGap) {
        mColumnGap = columnGap;
        invalidate();
    }

    public int getFirstDayOfWeek() {
        return mFirstDayOfWeek;
    }

    /**
     * Set the first day of the week. First day of the week is used only when the week view is first
     * drawn. It does not of any effect after user starts scrolling horizontally.
     * <p>
     * <b>Note:</b> This method will only work if the week view is set to display more than 6 days at
     * once.
     * </p>
     *
     * @param firstDayOfWeek The supported values are {@link java.util.Calendar#SUNDAY},
     *                       {@link java.util.Calendar#MONDAY}, {@link java.util.Calendar#TUESDAY},
     *                       {@link java.util.Calendar#WEDNESDAY}, {@link java.util.Calendar#THURSDAY},
     *                       {@link java.util.Calendar#FRIDAY}.
     */
    public void setFirstDayOfWeek(int firstDayOfWeek) {
        mFirstDayOfWeek = firstDayOfWeek;
        invalidate();
    }

    public int getTextSize() {
        return mTextSize;
    }

    public void setTextSize(int textSize) {
        mTextSize = textSize;
        mTodayHeaderTextPaint.setTextSize(mTextSize);
        mHeaderTextPaint.setTextSize(mTextSize);
        mTimeTextPaint.setTextSize(mTextSize);
        invalidate();
    }

    public int getHeaderColumnPadding() {
        return mHeaderColumnPadding;
    }

    public void setHeaderColumnPadding(int headerColumnPadding) {
        mHeaderColumnPadding = headerColumnPadding;
        invalidate();
    }

    public int getHeaderColumnTextColor() {
        return mHeaderColumnTextColor;
    }

    public void setHeaderColumnTextColor(int headerColumnTextColor) {
        mHeaderColumnTextColor = headerColumnTextColor;
        invalidate();
    }

    public int getHeaderRowPadding() {
        return mHeaderRowPadding;
    }

    public void setHeaderRowPadding(int headerRowPadding) {
        mHeaderRowPadding = headerRowPadding;
        invalidate();
    }

    public int getHeaderRowBackgroundColor() {
        return mHeaderRowBackgroundColor;
    }

    public void setHeaderRowBackgroundColor(int headerRowBackgroundColor) {
        mHeaderRowBackgroundColor = headerRowBackgroundColor;
        invalidate();
    }

    public int getDayBackgroundColor() {
        return mDayBackgroundColor;
    }

    public void setDayBackgroundColor(int dayBackgroundColor) {
        mDayBackgroundColor = dayBackgroundColor;
        invalidate();
    }

    public int getHourSeparatorColor() {
        return mHourSeparatorColor;
    }

    public void setHourSeparatorColor(int hourSeparatorColor) {
        mHourSeparatorColor = hourSeparatorColor;
        invalidate();
    }

    public int getTodayBackgroundColor() {
        return mTodayBackgroundColor;
    }

    public void setTodayBackgroundColor(int todayBackgroundColor) {
        mTodayBackgroundColor = todayBackgroundColor;
        invalidate();
    }

    public int getHourSeparatorHeight() {
        return mHourSeparatorHeight;
    }

    public void setHourSeparatorHeight(int hourSeparatorHeight) {
        mHourSeparatorHeight = hourSeparatorHeight;
        invalidate();
    }

    public int getTodayHeaderTextColor() {
        return mTodayHeaderTextColor;
    }

    public void setTodayHeaderTextColor(int todayHeaderTextColor) {
        mTodayHeaderTextColor = todayHeaderTextColor;
        invalidate();
    }

    public int getEventTextSize() {
        return mEventTextSize;
    }

    public void setEventTextSize(int eventTextSize) {
        mEventTextSize = eventTextSize;
        mEventTextPaint.setTextSize(mEventTextSize);
        invalidate();
    }

    public int getEventTextColor() {
        return mEventTextColor;
    }

    public void setEventTextColor(int eventTextColor) {
        mEventTextColor = eventTextColor;
        invalidate();
    }

    public int getEventPadding() {
        return mEventPadding;
    }

    public void setEventPadding(int eventPadding) {
        mEventPadding = eventPadding;
        invalidate();
    }

    public int getHeaderColumnBackgroundColor() {
        return mHeaderColumnBackgroundColor;
    }

    public void setHeaderColumnBackgroundColor(int headerColumnBackgroundColor) {
        mHeaderColumnBackgroundColor = headerColumnBackgroundColor;
        invalidate();
    }

    public int getDefaultEventColor() {
        return mDefaultEventColor;
    }

    public void setDefaultEventColor(int defaultEventColor) {
        mDefaultEventColor = defaultEventColor;
        invalidate();
    }

    /**
     * <b>Note:</b> Use {@link #setDateTimeInterpreter(DateTimeInterpreter)} and
     * {@link #getDateTimeInterpreter()} instead.
     *
     * @return Either long or short day name is being used.
     */
    @Deprecated
    public int getDayNameLength() {
        return mDayNameLength;
    }

    /**
     * Set the length of the day name displayed in the header row. Example of short day names is
     * 'M' for 'Monday' and example of long day names is 'Mon' for 'Monday'.
     * <p>
     * <b>Note:</b> Use {@link #setDateTimeInterpreter(DateTimeInterpreter)} instead.
     * </p>
     *
     * @param length Supported values are {@link com.alamkanak.weekview.WeekView#LENGTH_SHORT} and
     *               {@link com.alamkanak.weekview.WeekView#LENGTH_LONG}.
     */
    @Deprecated
    public void setDayNameLength(int length) {
        if (length != LENGTH_LONG && length != LENGTH_SHORT) {
            throw new IllegalArgumentException("length parameter must be either LENGTH_LONG or LENGTH_SHORT");
        }
        this.mDayNameLength = length;
    }

    public int getOverlappingEventGap() {
        return mOverlappingEventGap;
    }

    /**
     * Set the gap between overlapping events.
     *
     * @param overlappingEventGap The gap between overlapping events.
     */
    public void setOverlappingEventGap(int overlappingEventGap) {
        this.mOverlappingEventGap = overlappingEventGap;
        invalidate();
    }

    public int getEventMarginVertical() {
        return mEventMarginVertical;
    }

    /**
     * Set the top and bottom margin of the event. The event will release this margin from the top
     * and bottom edge. This margin is useful for differentiation consecutive events.
     *
     * @param eventMarginVertical The top and bottom margin.
     */
    public void setEventMarginVertical(int eventMarginVertical) {
        this.mEventMarginVertical = eventMarginVertical;
        invalidate();
    }

    /**
     * Returns the first visible day in the week view.
     *
     * @return The first visible day in the week view.
     */
    public Calendar getFirstVisibleDay() {
        return mFirstVisibleDay;
    }

    /**
     * Returns the last visible day in the week view.
     *
     * @return The last visible day in the week view.
     */
    public Calendar getLastVisibleDay() {
        return mLastVisibleDay;
    }

    /**
     * Get the scrolling speed factor in horizontal direction.
     *
     * @return The speed factor in horizontal direction.
     */
    public float getXScrollingSpeed() {
        return mXScrollingSpeed;
    }

    /**
     * Sets the speed for horizontal scrolling.
     *
     * @param xScrollingSpeed The new horizontal scrolling speed.
     */
    public void setXScrollingSpeed(float xScrollingSpeed) {
        this.mXScrollingSpeed = xScrollingSpeed;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // this is to move to day view on date touch of week view - Added by Lakshmi
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            for (RectF rectF : rectList) {
                if (rectF.contains(event.getX(), event.getY())) {
                    setNumberOfVisibleDays(1);
                    Calendar date = rectanglesWithDate.get(rectF);
                    // Store date in the shared preference
                    SharedPreferences.Editor editor = calendarPreference.edit();
                    editor.putLong(DATE_KEY_WEEK, date.getTimeInMillis());
                    editor.commit();
                    goToDate(date);

                    rectanglesWithDate.clear();
                    rectList.clear();
                    notifyDatasetChanged();
                    mBackgroundListener.changeBackground();
                    break;
                }
            }
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {

            if (mCurrentScrollDirection == Direction.HORIZONTAL) {
                float leftDays = Math.round(mCurrentOrigin.x / (mWidthPerDay + mColumnGap));
                int nearestOrigin = (int) (mCurrentOrigin.x - leftDays * (mWidthPerDay + mColumnGap));
                mStickyScroller.startScroll((int) mCurrentOrigin.x, 0, -nearestOrigin, 0);
                ViewCompat.postInvalidateOnAnimation(WeekView.this);

                rectanglesWithDate.clear();
                rectList.clear();
            }
            mCurrentScrollDirection = Direction.NONE;
        }
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        rectanglesWithDate.clear();
        rectList.clear();
        if (mNumberOfVisibleDays == 1 || mNumberOfVisibleDays == 7) {
            if (mScroller.computeScrollOffset()) {
                if (Math.abs(mScroller.getFinalX() - mScroller.getCurrX()) < mWidthPerDay + mColumnGap && Math.abs(mScroller.getFinalX() - mScroller.getStartX()) != 0) {
                    mScroller.forceFinished(true);
                    float leftDays = Math.round(mCurrentOrigin.x / (mWidthPerDay + mColumnGap));

                    // To scroll number of days according to visible days - Edited by Muddassir
                    if (mScroller.getFinalX() < mScroller.getCurrX()) {
                        if (mNumberOfVisibleDays == 1) {
                            leftDays--;
                        } else {
                            leftDays -= (mNumberOfVisibleDays - 1); // Added by Muddassir
                        }
                    } else {
                        if (mNumberOfVisibleDays == 1) {
                            leftDays++;
                        } else {
                            leftDays += (mNumberOfVisibleDays - 1); // Added by Muddassir
                        }
                    }
                    int nearestOrigin = (int) (mCurrentOrigin.x - leftDays * (mWidthPerDay + mColumnGap));
                    mStickyScroller.startScroll((int) mCurrentOrigin.x, 0, -nearestOrigin, 0);

                    ViewCompat.postInvalidateOnAnimation(WeekView.this);
                } else {
                    if (mCurrentFlingDirection == Direction.VERTICAL)
                        mCurrentOrigin.y = mScroller.getCurrY();
                    else mCurrentOrigin.x = mScroller.getCurrX();
                    ViewCompat.postInvalidateOnAnimation(this);
                }
            }
            if (mStickyScroller.computeScrollOffset()) {
                mCurrentOrigin.x = mStickyScroller.getCurrX();
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }
    }
    /////////////////////////////////////////////////////////////////
    //
    //      Functions related to scrolling.
    //
    /////////////////////////////////////////////////////////////////

    /**
     * Show today on the week view.
     */
    public void goToToday() {
        Calendar today = Calendar.getInstance();
        goToDate(today);
    }

    /**
     * Show a specific day on the week view.
     *
     * @param date The date to show.
     */
    public void goToDate(Calendar date) {
        mScroller.forceFinished(true);
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        mRefreshEvents = true;

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        int dateDifference = (int) ((date.getTimeInMillis() - today.getTimeInMillis()) / (1000 * 60 * 60 * 24));
//        mCurrentOrigin.x = -dateDifference * (mWidthPerDay + mColumnGap);
        if (mNumberOfVisibleDays == 7 && !fromMonthView) {
            mCurrentOrigin.x = -dateDifference * (mWidthPerDay + mColumnGap) / 7;
        } else if (mNumberOfVisibleDays == 1 && fromMonthView) {
            mCurrentOrigin.x = -dateDifference * (mWidthPerDayOriginal + mColumnGap);
            fromMonthView = false;
        } else {
            mCurrentOrigin.x = -dateDifference * (mWidthPerDayOriginal + mColumnGap);
        }
        invalidate();
    }


    /////////////////////////////////////////////////////////////////
    //
    //      Public methods.
    //
    /////////////////////////////////////////////////////////////////

    /**
     * Refreshes the view and loads the events again.
     */
    public void notifyDatasetChanged() {
        mRefreshEvents = true;
        invalidate();
    }

    /**
     * Vertically scroll to a specific hour in the week view.
     *
     * @param hour The hour to scroll to in 24-hour format. Supported values are 0-24.
     */
    public void goToHour(double hour) {
        if (hour < 0)
            throw new IllegalArgumentException("Cannot scroll to an hour of negative value.");
        else if (hour > 24)
            throw new IllegalArgumentException("Cannot scroll to an hour of value greater than 24.");
        else if (hour * mHourHeight > mHourHeight * 24 - getHeight() + mHeaderTextHeight + mHeaderRowPadding * 68 / 15 + mHeaderMarginBottom) // Changed
            throw new IllegalArgumentException("Cannot scroll to an hour which will result the calendar to go off the screen.");

        int verticalOffset = (int) (mHourHeight * hour);
        mCurrentOrigin.y = -verticalOffset;
        invalidate();
    }

    /**
     * Checks if an integer array contains a particular value.
     *
     * @param list  The haystack.
     * @param value The needle.
     * @return True if the array contains the value. Otherwise returns false.
     */
    private boolean containsValue(int[] list, int value) {
        for (int i = 0; i < list.length; i++) {
            if (list[i] == value)
                return true;
        }
        return false;
    }

    /**
     * Checks if two times are on the same day.
     *
     * @param dayOne The first day.
     * @param dayTwo The second day.
     * @return Whether the times are on the same day.
     */
    private boolean isSameDay(Calendar dayOne, Calendar dayTwo) {
        return dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR) && dayOne.get(Calendar.DAY_OF_YEAR) == dayTwo.get(Calendar.DAY_OF_YEAR);
    }


    /////////////////////////////////////////////////////////////////
    //
    //      Interfaces.
    //
    /////////////////////////////////////////////////////////////////

    private enum Direction {
        NONE, HORIZONTAL, VERTICAL
    }

    public interface EventClickListener {
        public void onEventClick(WeekViewEvent event, RectF eventRect);
    }

    public interface MonthChangeListener {
        public List<WeekViewEvent> onMonthChange(int newYear, int newMonth);
    }

    public interface EventLongPressListener {
        public void onEventLongPress(WeekViewEvent event, RectF eventRect);
    }

    public interface EmptyViewClickListener {
        public void onEmptyViewClicked(Calendar time);
    }

    // This will allow us to toggle the button in MainActivity
    public interface ChangeBackgroundListener {
        public void changeBackground();
    }
    /////////////////////////////////////////////////////////////////
    //
    //      Helper methods.
    //
    /////////////////////////////////////////////////////////////////

    public interface EmptyViewLongPressListener {
        public void onEmptyViewLongPress(Calendar time);
    }

    /**
     * A class to hold reference to the events and their visual representation. An EventRect is
     * actually the rectangle that is drawn on the calendar for a given event. There may be more
     * than one rectangle for a single event (an event that expands more than one day). In that
     * case two instances of the EventRect will be used for a single event. The given event will be
     * stored in "originalEvent". But the event that corresponds to rectangle the rectangle
     * instance will be stored in "event".
     */
    private class EventRect {
        public WeekViewEvent event;
        public WeekViewEvent originalEvent;
        public RectF rectF;
        public float left;
        public float width;
        public float top;
        public float bottom;

        /**
         * Create a new instance of event rect. An EventRect is actually the rectangle that is drawn
         * on the calendar for a given event. There may be more than one rectangle for a single
         * event (an event that expands more than one day). In that case two instances of the
         * EventRect will be used for a single event. The given event will be stored in
         * "originalEvent". But the event that corresponds to rectangle the rectangle instance will
         * be stored in "event".
         *
         * @param event         Represents the event which this instance of rectangle represents.
         * @param originalEvent The original event that was passed by the user.
         * @param rectF         The rectangle.
         */
        public EventRect(WeekViewEvent event, WeekViewEvent originalEvent, RectF rectF) {
            this.event = event;
            this.rectF = rectF;
            this.originalEvent = originalEvent;
        }
    }

}
