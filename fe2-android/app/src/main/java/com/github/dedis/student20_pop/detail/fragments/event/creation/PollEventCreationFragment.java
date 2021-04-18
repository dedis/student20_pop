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
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import com.github.dedis.student20_pop.R;
import com.github.dedis.student20_pop.detail.listeners.OnEventCreatedListener;
import com.github.dedis.student20_pop.detail.adapters.ChoicesListViewAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that shows up when user wants to create a Poll Event
 *
 * @deprecated this needs to be refactored
 */
public final class PollEventCreationFragment extends AbstractEventCreationFragment {

  public static final String TAG = PollEventCreationFragment.class.getSimpleName();

  private EditText questionEditText;

  private boolean pollTypeIsOneOfN;

  private Button scheduleButton;

  private ListView choicesListView;
  private final TextWatcher buttonsTextWatcher =
      new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
          scheduleButton.setEnabled(isScheduleButtonEnabled());
        }

        @Override
        public void afterTextChanged(Editable s) {
          scheduleButton.setEnabled(isScheduleButtonEnabled());
        }
      };
  private ChoicesListViewAdapter listViewAdapter;
  private OnEventCreatedListener eventCreatedListener;

  /**
   * Method to adapt the size of the view with the number of children
   *
   * @param listView
   */
  public static void justifyListViewHeightBasedOnChildren(ListView listView) {
    ListAdapter adapter = listView.getAdapter();
    if (adapter == null) {
      return;
    }
    int totalHeight = 0;
    for (int i = 0; i < adapter.getCount(); i++) {
      View listItem = adapter.getView(i, null, listView);
      listItem.measure(0, 0);
      totalHeight += listItem.getMeasuredHeight();
    }
    ViewGroup.LayoutParams par = listView.getLayoutParams();
    par.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
    listView.setLayoutParams(par);
    listView.requestLayout();
  }

  public static PollEventCreationFragment newInstance() {
    return new PollEventCreationFragment();
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    eventCreatedListener = (OnEventCreatedListener) context;
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    final FragmentManager fragmentManager = (getActivity()).getSupportFragmentManager();
    View view = inflater.inflate(R.layout.fragment_create_poll_event, container, false);

    setDateAndTimeView(view, PollEventCreationFragment.this, fragmentManager);
    addDateAndTimeListener(buttonsTextWatcher);

    // Question
    questionEditText = view.findViewById(R.id.question_edit_text);
    questionEditText.addTextChangedListener(buttonsTextWatcher);

    // Radio Buttons: poll type
    RadioButton pollType1;
    pollType1 = view.findViewById(R.id.radio_poll_type_1);
    pollType1.setChecked(true);
    RadioGroup pollType = view.findViewById(R.id.radio_group_poll_type);
    pollType.setOnCheckedChangeListener(
        new RadioGroup.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(RadioGroup group, int checkedId) {
            pollTypeIsOneOfN = (checkedId == R.id.radio_poll_type_1);
          }
        });

    // Choices list
    choicesListView = view.findViewById(R.id.choices_list);
    listViewAdapter = new ChoicesListViewAdapter(this.getContext(), buttonsTextWatcher);
    choicesListView.setAdapter(listViewAdapter);
    justifyListViewHeightBasedOnChildren(choicesListView);

    ImageButton addChoiceButton;
    addChoiceButton = view.findViewById(R.id.button_add);
    addChoiceButton.setFocusable(false);
    addChoiceButton.setOnClickListener(
        clicked -> {
          listViewAdapter.addChoice();
          justifyListViewHeightBasedOnChildren(choicesListView);
        });

    // Schedule
    scheduleButton = view.findViewById(R.id.schedule_button);
    scheduleButton.setOnClickListener(
        clicked -> {
          //          PoPApplication app = (PoPApplication) (getActivity().getApplication());
          String question = questionEditText.getText().toString();
          List<String> choicesList = getChoices(choicesListView);
          computeTimesInSeconds();
          //          Event pollEvent =
          //              new PollEvent(
          //                  question,
          //                  startTimeInSeconds,
          //                  endTimeInSeconds,
          //                  app.getCurrentLaoUnsafe().getId(),
          //                  NO_LOCATION,
          //                  choicesList,
          //                  pollTypeIsOneOfN);
          //          eventCreatedListener.OnEventCreatedListener(pollEvent);

          fragmentManager.popBackStackImmediate();
        });

    Button cancelButton = view.findViewById(R.id.cancel_button);
    cancelButton.setOnClickListener(
        clicked -> {
          fragmentManager.popBackStackImmediate();
        });
    return view;
  }

  private boolean isScheduleButtonEnabled() {
    String question = questionEditText.getText().toString().trim();
    int numberOfChoices = getChoices(choicesListView).size();

    return !question.isEmpty() && numberOfChoices >= 2;
  }

  public List<String> getChoices(ListView listView) {
    List<String> choices = new ArrayList<>();
    ChoicesListViewAdapter adapter = (ChoicesListViewAdapter) listView.getAdapter();
    if (adapter == null) {
      return choices;
    }
    int numberOfChoices = adapter.getCount();
    for (int i = 0; i < numberOfChoices; i++) {
      String choice = (String) adapter.getItem(i);
      if (!choice.isEmpty()) {
        choices.add(choice);
      }
    }
    return choices;
  }
}
