package com.github.dedis.student20_pop.detail.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.dedis.student20_pop.databinding.FragmentSetupElectionEventBinding;
import com.github.dedis.student20_pop.detail.LaoDetailActivity;
import com.github.dedis.student20_pop.detail.LaoDetailViewModel;
import com.github.dedis.student20_pop.detail.fragments.event.creation.AbstractEventCreationFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ElectionSetupFragment extends AbstractEventCreationFragment implements AdapterView.OnItemSelectedListener {

    public static final String TAG = ElectionSetupFragment.class.getSimpleName();

    private FragmentSetupElectionEventBinding mSetupElectionFragBinding;

    private EditText electionNameText;
    private EditText electionQuestionText;
    private EditText ballotOption1;
    private EditText ballotOption2;
    private Button submitButton;

    private enum votingMethods {Plurality}
    private votingMethods votingMethod;

    private List<String> ballotOptions;

    private final TextWatcher confirmTextWatcher =
            new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {/* no check to make before text is changed */}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    boolean areFieldsFilled =
                            !electionNameText.getText().toString().trim().isEmpty() && !getStartDate().isEmpty() && !getStartTime().isEmpty() && !getEndDate().isEmpty() && !getEndTime().isEmpty() &&
                            !ballotOption1.getText().toString().trim().isEmpty() && !ballotOption2.getText().toString().trim().isEmpty() && !electionQuestionText.getText().toString().trim().isEmpty();
                    submitButton.setEnabled(areFieldsFilled);
                }

                @Override
                public void afterTextChanged(Editable s) {/* no check to make after text is changed */}
            };

    private class BallotOptionsTextWatcher implements TextWatcher {

        private int ballotTag;

        public BallotOptionsTextWatcher(int ballotTag) {
            this.ballotTag = ballotTag;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {/* no check to make before text is changed */}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {/* no check to make when text is changed */}

        @Override
        public void afterTextChanged(Editable editable) {
            ballotOptions.set(ballotTag, editable.toString());
        }
    }

    public static ElectionSetupFragment newInstance() {
        return new ElectionSetupFragment();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        mSetupElectionFragBinding =
                FragmentSetupElectionEventBinding.inflate(inflater, container, false);

        LaoDetailViewModel laoDetailViewModel = LaoDetailActivity.obtainViewModel(getActivity());

        setDateAndTimeView(mSetupElectionFragBinding.getRoot(), this, getFragmentManager());
        addDateAndTimeListener(confirmTextWatcher);

        Button cancelButton = mSetupElectionFragBinding.electionCancelButton;

        ballotOptions = new ArrayList<>();

        submitButton = mSetupElectionFragBinding.electionSubmitButton;
        electionNameText = mSetupElectionFragBinding.electionSetupTitle;
        electionQuestionText = mSetupElectionFragBinding.electionQuestion;

        //Add text watchers on the fields that need to be filled
        electionQuestionText.addTextChangedListener(confirmTextWatcher);
        electionNameText.addTextChangedListener(confirmTextWatcher);

        // Set up the basic fields for ballot options, with at least two options
        initNewBallotOptionsField();

        //When the button is clicked, add a new ballot option
       FloatingActionButton addBallotOptionButton = mSetupElectionFragBinding.addBallotOption;
        addBallotOptionButton.setOnClickListener(
                v -> addBallotOption());


        // Set the text widget in layout to current LAO name
        TextView laoNameTextView = mSetupElectionFragBinding.electionSetupLaoName;
        laoNameTextView.setText(laoDetailViewModel.getCurrentLaoName().getValue());

        //Set dropdown spinner as a list of string (from enum of votingmethods)
       Spinner spinner = mSetupElectionFragBinding.electionSetupSpinner;
        String[] items = Arrays.stream(votingMethods.values()).map(votingMethods::toString).toArray(String[]::new);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);


        //On click, submit button creates a new election event and launches the election UI for organizer
       submitButton.setOnClickListener(
                v -> {
                    computeTimesInSeconds();
                    String title = electionNameText.getText().toString();
                    String question = electionQuestionText.getText().toString();
                    List<String> filteredBallotOptions = new ArrayList<>();
                    for (String ballotOption: ballotOptions) {
                        if (!ballotOption.equals("")) filteredBallotOptions.add(ballotOption);
                    }

                    laoDetailViewModel.createNewElection(title, startTimeInSeconds, endTimeInSeconds, votingMethod.toString(), mSetupElectionFragBinding.writeIn.isChecked(), filteredBallotOptions, question);
                });

        //On click, cancel button takes back to LAO detail page
        cancelButton.setOnClickListener(
                v ->
                    laoDetailViewModel.openLaoDetail());

        // Subscribe on updates for Election Creation

        laoDetailViewModel
                .getElectionCreated()
                .observe(
                        this,
                        booleanEvent -> {
                            Boolean action = booleanEvent.getContentIfNotHandled();
                            if (action != null) {
                                laoDetailViewModel.openLaoDetail();
                            }
                        });

        mSetupElectionFragBinding.setLifecycleOwner(getActivity());
        return mSetupElectionFragBinding.getRoot();



        //

    }

    // Spinner methods
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String item = adapterView.getItemAtPosition(i).toString();
        votingMethod = votingMethods.valueOf(item);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        votingMethod = votingMethods.valueOf("Plurality");
    }

    private EditText addBallotOption() {
        Context c = getActivity();
        EditText ballotOption = new EditText(c);
        ballotOption.setHint("ballot option");
        mSetupElectionFragBinding.electionSetupFieldsLl.addView(ballotOption);
        ViewGroup.LayoutParams params = ballotOption.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        ballotOption.setLayoutParams(params);
        int ballotTag = ballotOptions.size();
        ballotOption.setTag(ballotTag);
        ballotOption.addTextChangedListener(new BallotOptionsTextWatcher(ballotTag));
        ballotOptions.add(ballotOption.getText().toString());
        return ballotOption;
    }

    private void initNewBallotOptionsField() {
        ballotOption1 = addBallotOption();
        ballotOption2 = addBallotOption();
        ballotOption1.addTextChangedListener(confirmTextWatcher);
        ballotOption2.addTextChangedListener(confirmTextWatcher);
    }

}
