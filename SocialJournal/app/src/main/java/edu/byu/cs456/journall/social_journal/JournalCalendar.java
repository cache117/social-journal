package edu.byu.cs456.journall.social_journal;

import android.app.Activity;
import android.content.Intent;
import android.icu.util.Calendar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.CalendarView;
import android.widget.Toast;

public class JournalCalendar extends AppCompatActivity {
    private CalendarView calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initializeCalendar();
    }

    private void initializeCalendar() {
        calendar = (CalendarView) findViewById(R.id.calendar);
        calendar.setFirstDayOfWeek(1); //SUNDAY
        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int day) {
                navigateToDate(year, month, day);
            }
        });
    }

    private void navigateToDate(int year, int month, int day) {
        Intent data = new Intent(JournalCalendar.this, MainActivity.class);
        data.putExtra("YEAR", year);
        data.putExtra("MONTH", month);
        data.putExtra("DAY", day);
        setResult(Activity.RESULT_OK, data);
        finish();
    }
}
