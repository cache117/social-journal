package edu.byu.cs456.journall.social_journal.activities.calendar;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.CalendarView;

import edu.byu.cs456.journall.social_journal.R;
import edu.byu.cs456.journall.social_journal.activities.main.MainActivity;

/**
 * The Activity for the calendar.
 */
public class JournalCalendar extends AppCompatActivity {
    private CalendarView calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
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
