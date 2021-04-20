package com.github.dedis.student20_pop.detail.fragments.pickers;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TimePicker;
import androidx.appcompat.app.AppCompatDialogFragment;
import com.github.dedis.student20_pop.R;
import java.util.Calendar;

public final class TimePickerFragment extends AppCompatDialogFragment
    implements TimePickerDialog.OnTimeSetListener {

  public static final String TAG = TimePickerFragment.class.getSimpleName();
  private final Calendar calendar = Calendar.getInstance();

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
    int minute = calendar.get(Calendar.MINUTE);

    return new TimePickerDialog(getActivity(), this, hourOfDay, minute, true);
  }


  /**
   * Called when the user is done setting a new time and the dialog has closed.
   *
   * @param view the view associated with this listener
   * @param hourOfDay the hour that was set
   * @param minute the minute that was set
   */
  @Override
  public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
    calendar.setTimeInMillis(0L);
    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
    calendar.set(Calendar.MINUTE, minute);

    getTargetFragment()
        .onActivityResult(
            getTargetRequestCode(),
            Activity.RESULT_OK,
            new Intent().putExtra(getString(R.string.picker_selection), calendar));
  }
}
