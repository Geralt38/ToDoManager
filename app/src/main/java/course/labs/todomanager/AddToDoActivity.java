package course.labs.todomanager;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import course.labs.todomanager.ToDoItem.Priority;
import course.labs.todomanager.ToDoItem.Status;

public class AddToDoActivity extends Activity {

	// 7 дней в милисекундах 7 * 24 * 60 * 60 * 1000
	private static final int SEVEN_DAYS = 604800000;

	private static final String TAG = "Lab-UserInterface";

	private static boolean edit;
	private static int position = 0;

	private static String timeString;
	private static String dateString;
	private static TextView dateView;
	private static TextView timeView;

	private Date mDate;
	private RadioGroup mPriorityRadioGroup;
	private RadioGroup mStatusRadioGroup;
	private EditText mTitleText;
	private RadioButton mDefaultStatusButton;
	private RadioButton mDefaultPriorityButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_todo);

		mTitleText = (EditText) findViewById(R.id.title);
		mDefaultStatusButton = (RadioButton) findViewById(R.id.statusNotDone);
		mDefaultPriorityButton = (RadioButton) findViewById(R.id.medPriority);
		mPriorityRadioGroup = (RadioGroup) findViewById(R.id.priorityGroup);
		mStatusRadioGroup = (RadioGroup) findViewById(R.id.statusGroup);
		dateView = (TextView) findViewById(R.id.date);
		timeView = (TextView) findViewById(R.id.time);

		edit = false;
        Intent intent = getIntent();
        if(intent.getBooleanExtra("edit", false)){
			edit = true;
			position = intent.getIntExtra("position",0);
            mTitleText.setText(intent.getStringExtra("title"));
			int prId = mDefaultPriorityButton.getId();
			switch (Priority.valueOf(intent.getStringExtra("priority"))) {
				case MED: prId = R.id.medPriority; break;
				case LOW: prId = R.id.lowPriority; break;
				case HIGH: prId = R.id.highPriority; break;
			}
            mPriorityRadioGroup.check(prId);
			int stId = mDefaultStatusButton.getId();
			switch (Status.valueOf(intent.getStringExtra("status"))) {
				case DONE: stId = R.id.statusDone; break;
				case NOTDONE: stId = R.id.statusNotDone; break;
			}
			mStatusRadioGroup.check(stId);
			//setDateString(intent.getIntExtra("year",0),intent.getIntExtra("month",0),intent.getIntExtra("day",0));
			//setTimeString(intent.getIntExtra("hours",0),intent.getIntExtra("minutes",0),0);
			dateString = intent.getStringExtra("date");
			timeString = intent.getStringExtra("time");
            dateView.setText(dateString);
			timeView.setText(timeString);
        } else {
            setDefaultDateTime();
        }

		// OnClickListener для кнопки Date, вызывает showDatePickerDialog() для
		// показа диалога выбора даты

		final Button datePickerButton = (Button) findViewById(R.id.date_picker_button);
		datePickerButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDatePickerDialog();
			}
		});

		// OnClickListener для кнопки Time, вызывает showTimePickerDialog() для
		// показа диалога выбора Времени

		final Button timePickerButton = (Button) findViewById(R.id.time_picker_button);
		timePickerButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showTimePickerDialog();
			}
		});

		// OnClickListener для кнопки Cancel,

		final Button cancelButton = (Button) findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				setResult(RESULT_CANCELED);
				finish();
			}
		});

		final Button resetButton = (Button) findViewById(R.id.resetButton);
		resetButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

                mTitleText.setText("");
                mStatusRadioGroup.check(mDefaultStatusButton.getId());
				mPriorityRadioGroup.check(mDefaultPriorityButton.getId());
				// очистить дату и время
				setDefaultDateTime();

			}
		});

		// Установить OnClickListener для кнопки Submit

		final Button submitButton = (Button) findViewById(R.id.submitButton);
		submitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {


				// получить данные ToDoItem
				Priority priority = getPriority();
				Status status = getStatus();
				String titleString = getToDoTitle();


				// Сконструировать строку даты
				dateString = dateView.getText().toString();
				timeString = timeView.getText().toString();
				String fullDate = dateString + " " + timeString;

				// Упаковка данных ToDoItem в Intent
				Intent data = new Intent();
				ToDoItem.packageIntent(data, titleString, priority, status,
						fullDate);

				if (edit) {
					data.putExtra("position",position);
				}

				setResult(RESULT_OK, data);
				finish();

			}
		    });
	}

	// Do not modify below this point.

	private void setDefaultDateTime() {

		// Default is current time + 7 days
		mDate = new Date();
		mDate = new Date(mDate.getTime() + SEVEN_DAYS);

		Calendar c = Calendar.getInstance();
		c.setTime(mDate);

		setDateString(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
				c.get(Calendar.DAY_OF_MONTH));

		dateView.setText(dateString);

		setTimeString(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE),
				c.get(Calendar.MILLISECOND));

		timeView.setText(timeString);
	}

	private static void setDateString(int year, int monthOfYear, int dayOfMonth) {

		// Increment monthOfYear for Calendar/Date -> Time Format setting
		monthOfYear++;
		String mon = "" + monthOfYear;
		String day = "" + dayOfMonth;

		if (monthOfYear < 10)
			mon = "0" + monthOfYear;
		if (dayOfMonth < 10)
			day = "0" + dayOfMonth;

		dateString = year + "-" + mon + "-" + day;
	}

	private static void setTimeString(int hourOfDay, int minute, int mili) {
		String hour = "" + hourOfDay;
		String min = "" + minute;

		if (hourOfDay < 10)
			hour = "0" + hourOfDay;
		if (minute < 10)
			min = "0" + minute;

		timeString = hour + ":" + min + ":00";
	}

	private Priority getPriority() {

		switch (mPriorityRadioGroup.getCheckedRadioButtonId()) {
		case R.id.lowPriority: {
			return Priority.LOW;
		}
		case R.id.highPriority: {
			return Priority.HIGH;
		}
		default: {
			return Priority.MED;
		}
		}
	}

	private Status getStatus() {

		switch (mStatusRadioGroup.getCheckedRadioButtonId()) {
		case R.id.statusDone: {
			return Status.DONE;
		}
		default: {
			return Status.NOTDONE;
		}
		}
	}

	private String getToDoTitle() {
		return mTitleText.getText().toString();
	}
	
	
	// DialogFragment используется для выбора даты дедлайна

	public static class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			// Исполльзуйте текущую дату в качестве значения по умолчанию

			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Создать новый экземпляр DatePickerDialog и вернуть его
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			setDateString(year, monthOfYear, dayOfMonth);

			dateView.setText(dateString);
		}

	}

	// DialogFragment используется для выбора Времени дедлайна

	public static class TimePickerFragment extends DialogFragment implements
			TimePickerDialog.OnTimeSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			// Используетеся текущее время в качестве значения по умолчанию
			final Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);

			// Создаем новый экземпляр TimePickerDialog и возврат
			return new TimePickerDialog(getActivity(), this, hour, minute, true);
		}

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			setTimeString(hourOfDay, minute, 0);

			timeView.setText(timeString);
		}
	}

	private void showDatePickerDialog() {
		DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(getFragmentManager(), "datePicker");
	}

	private void showTimePickerDialog() {
		DialogFragment newFragment = new TimePickerFragment();
		newFragment.show(getFragmentManager(), "timePicker");
	}
}
