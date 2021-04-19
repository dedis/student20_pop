package com.github.dedis.student20_pop.detail.fragments.event.creation;

import android.app.Activity;
import android.content.Intent;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.github.dedis.student20_pop.R;
import com.github.dedis.student20_pop.detail.fragments.pickers.DatePickerFragment;
import com.github.dedis.student20_pop.detail.fragments.pickers.TimePickerFragment;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Locale;

/**
 * Multiples Event Creation Fragment have in common that they implement start/end date and start/end
 * time.
 *
 * <p>This class handles these fields.
 *
 * <p>TODO: this class needs to be refactored
 */
public abstract class AbstractEventCreationFragment extends Fragment {

  public static final int START_DATE_REQUEST_CODE = 11; // Used to identify the request
  public static final int END_DATE_REQUEST_CODE = 12;
  public static final int START_TIME_REQUEST_CODE = 13;
  public static final int END_TIME_REQUEST_CODE = 14;
  public static final String NO_LOCATION = "";
  public final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH);
  public final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.FRENCH);
  public static long startTimeInSeconds;
  public static long endTimeInSeconds;
  private Calendar startDate;
  private Calendar endDate;
  private Calendar startTime;
  private Calendar endTime;
  private Calendar today = Calendar.getInstance();
  private Calendar completeStartTime = Calendar.getInstance();
  private Calendar completeEndTime = Calendar.getInstance();
  private EditText startDateEditText;
  private EditText endDateEditText;
  private EditText startTimeEditText;
  private EditText endTimeEditText;

  public void setDateAndTimeView(View view, Fragment fragment, FragmentManager fragmentManager) {
    startDateEditText = view.findViewById(R.id.start_date_edit_text);
    startDateEditText.setInputType(InputType.TYPE_NULL);

    endDateEditText = view.findViewById(R.id.end_date_edit_text);
    endDateEditText.setInputType(InputType.TYPE_NULL);

    startTimeEditText = view.findViewById(R.id.start_time_edit_text);
    startTimeEditText.setInputType(InputType.TYPE_NULL);

    endTimeEditText = view.findViewById(R.id.end_time_edit_text);
    endTimeEditText.setInputType(InputType.TYPE_NULL);

    today.set(Calendar.HOUR, 0);
    today.set(Calendar.SECOND, 0);
    today.set(Calendar.MINUTE, 0);
    today.set(Calendar.MILLISECOND, 0);

    startDateEditText.setOnClickListener(
        v -> {
          // create the datePickerFragment
          AppCompatDialogFragment datePickerFragment = new DatePickerFragment();
          // set the targetFragment to receive the results, specifying the request code
          datePickerFragment.setTargetFragment(fragment, START_DATE_REQUEST_CODE);
          // show the datePicker
          datePickerFragment.show(fragmentManager, DatePickerFragment.TAG);
        });

    endDateEditText.setOnClickListener(
        v -> {
          AppCompatDialogFragment datePickerFragment = new DatePickerFragment();
          datePickerFragment.setTargetFragment(fragment, END_DATE_REQUEST_CODE);
          datePickerFragment.show(fragmentManager, DatePickerFragment.TAG);
        });

    startTimeEditText.setOnClickListener(
        v -> {
          AppCompatDialogFragment timePickerFragment = new TimePickerFragment();
          timePickerFragment.setTargetFragment(fragment, START_TIME_REQUEST_CODE);
          timePickerFragment.show(fragmentManager, TimePickerFragment.TAG);
        });

    endTimeEditText.setOnClickListener(
        v -> {
          AppCompatDialogFragment timePickerFragment = new TimePickerFragment();
          timePickerFragment.setTargetFragment(fragment, END_TIME_REQUEST_CODE);
          timePickerFragment.show(fragmentManager, TimePickerFragment.TAG);
        });
  }

  public void addDateAndTimeListener(TextWatcher listener) {
    startTimeEditText.addTextChangedListener(listener);
    startDateEditText.addTextChangedListener(listener);
    //For election setup only -> refactor ?
    endTimeEditText.addTextChangedListener(listener);
    endDateEditText.addTextChangedListener(listener);
  }

  public String getStartDate() {
    return startDateEditText.getText().toString().trim();
  }

  public String getStartTime() {
    return startTimeEditText.getText().toString().trim();
  }

  public String getEndDate() { return endDateEditText.getText().toString().trim(); }

  public String getEndTime() { return endTimeEditText.getText().toString().trim(); }

  public void checkDates(int requestCode, int resultCode, Intent data) {
    if (resultCode == Activity.RESULT_OK) {
      Calendar selection =
          (Calendar) data.getSerializableExtra(getString(R.string.picker_selection));
      switch (requestCode) {
        case START_DATE_REQUEST_CODE:
          startDate = selection;
          /*
           In Java, two dates can be compared using the compareTo() method of Comparable interface.
           This method returns '0' if both the dates are equal,
           it returns a value "greater than 0" if date1 is after date2 and
           it returns a value "less than 0" if date1 is before date2.
          */
          if (startDate.compareTo(today) < 0) {
            Toast.makeText(
                    getActivity(), getString(R.string.past_date_not_allowed), Toast.LENGTH_LONG)
                .show();
            startDateEditText.setText("");
            startDate = null;
          } else {
            if ((endDate != null) && (startDate.compareTo(endDate) > 0)) {
              Toast.makeText(
                      getActivity(),
                      getString(R.string.start_date_after_end_date_not_allowed),
                      Toast.LENGTH_LONG)
                  .show();
              startDateEditText.setText("");
              startDate = null;
            } else {
              startDate = selection;
              startDateEditText.setText(DATE_FORMAT.format(startDate.getTime()));
              if ((endDate != null) && (startDate.compareTo(endDate) == 0)) {
                endTime = null;
                endTimeEditText.setText("");
              }
            }
          }
          break;

        case END_DATE_REQUEST_CODE:
          endDate = selection;
          if (endDate.compareTo(today) < 0) {
            Toast.makeText(
                    getActivity(), getString(R.string.past_date_not_allowed), Toast.LENGTH_LONG)
                .show();
            endDateEditText.setText("");
            endDate = null;
          } else {
            if ((startDate != null) && (endDate.compareTo(startDate) < 0)) {
              Toast.makeText(
                      getActivity(),
                      getString(R.string.end_date_after_start_date_not_allowed),
                      Toast.LENGTH_SHORT)
                  .show();
              endDateEditText.setText("");
              endDate = null;
            } else {
              if ((startDate != null) && (startDate.compareTo(endDate) == 0)) {
                endTime = null;
                endTimeEditText.setText("");
              }
              endDate = selection;
              endDateEditText.setText(DATE_FORMAT.format(endDate.getTime()));
            }
          }
          break;

        case START_TIME_REQUEST_CODE:
          if (startDate == null || endDate == null) {
            startTime = selection;
            startTimeEditText.setText(TIME_FORMAT.format(startTime.getTime()));
          } else {
            startTime = selection;
            if ((startDate.compareTo(endDate) == 0)
                && (endTime != null)
                && (startTime.compareTo(endTime) > 0)) {
              Toast.makeText(
                      getActivity(),
                      getString(R.string.start_time_after_end_time_not_allowed),
                      Toast.LENGTH_LONG)
                  .show();
              startTime = null;
              startTimeEditText.setText("");
            } else {
              startTimeEditText.setText(TIME_FORMAT.format(selection.getTime()));
            }
          }

          break;

        case END_TIME_REQUEST_CODE:
          if ((startDate == null) || (endDate == null)) {
            endTime = selection;
            endTimeEditText.setText(TIME_FORMAT.format(selection.getTime()));
          } else {
            endTime = selection;
            if ((startDate.compareTo(endDate) == 0)
                && (startTime != null)
                && (endTime.compareTo(startTime) < 0)) {
              Toast.makeText(
                      getActivity(),
                      getString(R.string.end_time_before_start_time_not_allowed),
                      Toast.LENGTH_LONG)
                  .show();
              endTime = null;
              endTimeEditText.setText("");
            } else {
              endTimeEditText.setText(TIME_FORMAT.format(selection.getTime()));
            }
          }
          break;
      }
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    checkDates(requestCode, resultCode, data);
  }

  public void computeTimesInSeconds() {
    if (startDate == null) {
      startDate = Calendar.getInstance();
    }
    if (startTime == null) {
      startTime = Calendar.getInstance();
    }
    completeStartTime.set(
        startDate.get(Calendar.YEAR),
        startDate.get(Calendar.MONTH),
        startDate.get(Calendar.DAY_OF_MONTH),
        startTime.get(Calendar.HOUR_OF_DAY),
        startTime.get(Calendar.MINUTE));
    Instant start = Instant.ofEpochMilli(completeStartTime.getTimeInMillis());
    startTimeInSeconds = start.getEpochSecond();
    if (endDate != null) {
      if (endTime == null) {
        completeEndTime.set(
            endDate.get(Calendar.YEAR),
            endDate.get(Calendar.MONTH),
            endDate.get(Calendar.DAY_OF_MONTH));
      } else {
        completeEndTime.set(
            endDate.get(Calendar.YEAR),
            endDate.get(Calendar.MONTH),
            endDate.get(Calendar.DAY_OF_MONTH),
            endTime.get(Calendar.HOUR_OF_DAY),
            endTime.get(Calendar.MINUTE));
      }
      Instant end = Instant.ofEpochMilli(completeEndTime.getTimeInMillis());
      endTimeInSeconds = end.getEpochSecond();
    }
  }

  public void editTextInputChecker(EditText editText, String errorMessage){
    if(editText != null && errorMessage != null) {
      if (editText.getText().toString().trim().isEmpty()) {
        editText.setError(errorMessage);
      }
    }
    else{
      throw new IllegalArgumentException();
    }
  }
}
