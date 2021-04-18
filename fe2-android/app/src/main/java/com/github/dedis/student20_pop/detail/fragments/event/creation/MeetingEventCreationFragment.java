package com.github.dedis.student20_pop.detail.fragments.event.creation;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import com.github.dedis.student20_pop.R;
import com.github.dedis.student20_pop.detail.listeners.OnEventCreatedListener;

/**
 * Fragment that shows up when user wants to create a Meeting Event
 *
 * @deprecated This needs to be refactored
 */
public final class MeetingEventCreationFragment extends AbstractEventCreationFragment {

  public static final String TAG = MeetingEventCreationFragment.class.getSimpleName();

  private EditText meetingTitleEditText;
  private EditText meetingLocationEditText;
  private EditText meetingDescriptionEditText;

  private Button confirmButton;
  private final TextWatcher confirmTextWatcher =
      new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
          String meetingTitle = meetingTitleEditText.getText().toString().trim();

          confirmButton.setEnabled(
              !meetingTitle.isEmpty() && !getStartDate().isEmpty() && !getStartTime().isEmpty());
        }

        @Override
        public void afterTextChanged(Editable s) {}
      };

  private Button cancelButton;
  private OnEventCreatedListener eventCreatedListener;

  public static MeetingEventCreationFragment newInstance() {
    return new MeetingEventCreationFragment();
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnEventCreatedListener) {
      eventCreatedListener = (OnEventCreatedListener) context;
    } else {
      throw new ClassCastException(context.toString() + " must implement OnEventCreatedListener");
    }
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    final FragmentManager fragmentManager = (getActivity()).getSupportFragmentManager();
    View view = inflater.inflate(R.layout.fragment_create_meeting_event, container, false);
    //    PoPApplication app = (PoPApplication) getActivity().getApplication();

    setDateAndTimeView(view, MeetingEventCreationFragment.this, fragmentManager);
    addDateAndTimeListener(confirmTextWatcher);

    meetingTitleEditText = view.findViewById(R.id.meeting_title_text);
    meetingLocationEditText = view.findViewById(R.id.meeting_event_location_text);
    meetingDescriptionEditText = view.findViewById(R.id.meeting_event_description_text);

    meetingTitleEditText.addTextChangedListener(confirmTextWatcher);

    confirmButton = view.findViewById(R.id.meeting_event_creation_confirm);
    confirmButton.setOnClickListener(
        v -> {
          computeTimesInSeconds();

          //          Event meetingEvent =
          //              new MeetingEvent(
          //                  meetingTitleEditText.getText().toString(),
          //                  startTimeInSeconds,
          //                  endTimeInSeconds,
          //                  app.getCurrentLaoUnsafe().getId(),
          //                  meetingLocationEditText.getText().toString(),
          //                  meetingDescriptionEditText.getText().toString());
          //
          //          eventCreatedListener.OnEventCreatedListener(meetingEvent);

          fragmentManager.popBackStackImmediate();
        });

    cancelButton = view.findViewById(R.id.meeting_event_creation_cancel);
    cancelButton.setOnClickListener(
        v -> {
          fragmentManager.popBackStackImmediate();
        });

    return view;
  }
}
