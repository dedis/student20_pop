package com.github.dedis.student20_pop.detail.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.github.dedis.student20_pop.databinding.FragmentCreateRollCallEventBinding;
import com.github.dedis.student20_pop.detail.LaoDetailActivity;
import com.github.dedis.student20_pop.detail.LaoDetailViewModel;
import java.time.Instant;

/** Fragment that shows up when user wants to create a Roll-Call Event */
public final class RollCallEventCreationFragment extends AbstractEventCreationFragment {

  public static final String TAG = RollCallEventCreationFragment.class.getSimpleName();


  private LaoDetailViewModel mLaoDetailViewModel;
  private FragmentCreateRollCallEventBinding mFragmentCreateRollCallEventBinding;

  private final TextWatcher confirmTextWatcher =
      new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
          String meetingTitle = mFragmentCreateRollCallEventBinding.rollCallTitleText.getText().toString().trim();
          boolean areFieldsFilled =
              !meetingTitle.isEmpty() && !getStartDate().isEmpty() && !getStartTime().isEmpty();
            mFragmentCreateRollCallEventBinding.rollCallOpen.setEnabled(areFieldsFilled);
            mFragmentCreateRollCallEventBinding.rollCallConfirm.setEnabled(areFieldsFilled);
        }

        @Override
        public void afterTextChanged(Editable s) {}
      };

  public static RollCallEventCreationFragment newInstance() {
    return new RollCallEventCreationFragment();
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

      mLaoDetailViewModel
              .getCreatedRollCallEvent()
              .observe(
                      this,
                      booleanEvent -> {
                          Boolean action = booleanEvent.getContentIfNotHandled();
                          if (action != null) {
                              mLaoDetailViewModel.openLaoDetail();
                          }
                      });

      mFragmentCreateRollCallEventBinding.rollCallConfirm.setOnClickListener(
              v -> {
                  computeTimesInSeconds();

                  String title = mFragmentCreateRollCallEventBinding.rollCallTitleText.getText().toString();
                  String description = mFragmentCreateRollCallEventBinding.rollCallEventDescriptionText.getText().toString();
                  mLaoDetailViewModel.createNewRollCall(title, description, startTimeInSeconds, endTimeInSeconds, false);
              });

      mFragmentCreateRollCallEventBinding.rollCallOpen.setOnClickListener(
              v -> {
                  computeTimesInSeconds();

                  String title = mFragmentCreateRollCallEventBinding.rollCallTitleText.getText().toString();
                  String description = mFragmentCreateRollCallEventBinding.rollCallEventDescriptionText.getText().toString();
                  mLaoDetailViewModel.openConnectRollCall(mLaoDetailViewModel.createNewRollCall(title, description, startTimeInSeconds, endTimeInSeconds, true));
              });


      mFragmentCreateRollCallEventBinding.rollCallCancel.setOnClickListener(
              v -> {
                  mLaoDetailViewModel.openLaoDetail();
              });
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    mFragmentCreateRollCallEventBinding =
        FragmentCreateRollCallEventBinding.inflate(inflater, container, false);

    mLaoDetailViewModel = LaoDetailActivity.obtainViewModel(getActivity());

    // TODO: refactor this
    setDateAndTimeView(mFragmentCreateRollCallEventBinding.getRoot(), this, getFragmentManager());
    addDateAndTimeListener(confirmTextWatcher);

    /*rollCallTitleEditText = binding.rollCallTitleText;
    rollCallDescriptionEditText = binding.rollCallEventDescriptionText;

    openButton = binding.rollCallOpen;

    confirmButton = binding.rollCallConfirm;
       */
    // TODO: this has to be replaced by a 'scheduled' button
    //    confirmButton.setOnClickListener(
    //        v -> {
    //          computeTimesInSeconds();
    //
    //          String title = rollCallTitleEditText.getText().toString();
    //          String description = rollCallDescriptionEditText.getText().toString();
    //          long now = Instant.now().getEpochSecond();
    //          long start = startTimeInSeconds > now ? 0 : startTimeInSeconds;
    //          long scheduled = startTimeInSeconds >= now ? startTimeInSeconds : 0;
    //          mLaoDetailViewModel
    //              .createNewRollCall(title, description, start, scheduled, endTimeInSeconds);
    //        });

    mFragmentCreateRollCallEventBinding.setLifecycleOwner(getActivity());


    return mFragmentCreateRollCallEventBinding.getRoot();
  }
}
