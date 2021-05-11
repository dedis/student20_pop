package com.github.dedis.student20_pop.detail.fragments.event.creation;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import com.github.dedis.student20_pop.R;
import com.github.dedis.student20_pop.databinding.FragmentSetupElectionEventBinding;
import com.github.dedis.student20_pop.databinding.LayoutBallotOptionBinding;
import com.github.dedis.student20_pop.detail.LaoDetailActivity;
import com.github.dedis.student20_pop.detail.LaoDetailViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ElectionSetupFragment extends AbstractEventCreationFragment implements AdapterView.OnItemSelectedListener {

    public static final String TAG = ElectionSetupFragment.class.getSimpleName();

    private FragmentSetupElectionEventBinding mSetupElectionFragBinding;

    //mandatory fields for submitting
    private EditText electionNameText;
    private EditText electionQuestionText;
    private Button submitButton;

    private LaoDetailViewModel mLaoDetailViewModel;

    //Enum of all voting methods, associated to a string desc for protocol and spinner display
    public enum VotingMethods { PLURALITY("Plurality");
        private String desc;
        VotingMethods(String desc) { this.desc=desc; }
        public String getDesc() { return desc; }
    }

    private String votingMethod;

    private List<String> ballotOptions;

    //the number of valid ballot options set by the organizer
    private int numberBallotOptions = 0;

    //Text watcher that checks if mandatory fields are filled for submitting each time the user changes a field (with at least two valid ballot options)
    private final TextWatcher submitTextWatcher =
            new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {/* no check to make before text is changed */}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {/* no check to make during the text is being changes */}

                @Override
                public void afterTextChanged(Editable s) {
                    Log.d(TAG, "ballot options is" + ballotOptions.toString());
                    boolean areFieldsFilled =
                            !electionNameText.getText().toString().trim().isEmpty() && !getStartDate().isEmpty() && !getStartTime().isEmpty() && !getEndDate().isEmpty() && !getEndTime().isEmpty() &&
                                    !electionQuestionText.getText().toString().trim().isEmpty() && numberBallotOptions >= 2;
                    submitButton.setEnabled(areFieldsFilled);}
            };

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

        mLaoDetailViewModel = LaoDetailActivity.obtainViewModel(getActivity());

        //Set the view for the date and time
        setDateAndTimeView(mSetupElectionFragBinding.getRoot(), this, getFragmentManager());
        //Make the textWatcher listen to changes in the start and end date/time
        addEndDateAndTimeListener(submitTextWatcher);
        addStartDateAndTimeListener(submitTextWatcher);

        ballotOptions = new ArrayList<>();

        submitButton = mSetupElectionFragBinding.electionSubmitButton;
        electionNameText = mSetupElectionFragBinding.electionSetupName;
        electionQuestionText = mSetupElectionFragBinding.electionQuestion;

        //Add text watchers on the fields that need to be filled
        electionQuestionText.addTextChangedListener(submitTextWatcher);
        electionNameText.addTextChangedListener(submitTextWatcher);

        // Set up the basic fields for ballot options, with at least two options
        initNewBallotOptionsField();

        // Set the text widget in layout to current LAO name
        TextView laoNameTextView = mSetupElectionFragBinding.electionSetupLaoName;
        laoNameTextView.setText(mLaoDetailViewModel.getCurrentLaoName().getValue());

        mSetupElectionFragBinding.setLifecycleOwner(getActivity());

        return mSetupElectionFragBinding.getRoot();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupAddBallotOptionsButton();
        setupElectionCancelButton();
        setupElectionSpinner();
        setupElectionSubmitButton();
    }


    /**
     * Adds a view of ballot option to the layout when user clicks the button
     */
    private void addBallotOption() {
        //Adds the view for a new ballot option, from the corresponding layout
        View ballotOptionView = LayoutBallotOptionBinding.inflate(getLayoutInflater()).newBallotOptionLl;
        EditText ballotOptionText = ballotOptionView.findViewById(R.id.new_ballot_option_text);
        mSetupElectionFragBinding.electionSetupBallotOptionsLl.addView(ballotOptionView);

        //Gets the index associated to the ballot option's view
        int ballotIndex = mSetupElectionFragBinding.electionSetupBallotOptionsLl.indexOfChild(ballotOptionView);
        ballotOptionText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {/* no check to make before text is changed */}
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) { /*no check to make during the text is being changed */}
            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                //Prevents the user from creating two different ballot options with the same name
                if (!text.isEmpty() && ballotOptions.contains(text)) ballotOptionText.setError("Two different ballot options can't have the same name");
                //Counts the number of non-empty ballot options, to know when the user can create the election (at least 2 non-empty)
                if (ballotOptions.isEmpty() || (ballotOptions.get(ballotIndex).isEmpty() && !text.isEmpty())) numberBallotOptions++;
                else if (!ballotOptions.get(ballotIndex).isEmpty() && text.isEmpty())  numberBallotOptions--;
                //Keeps the list of string updated when the user changes the text
                ballotOptions.set(ballotIndex, editable.toString());
            }
        });
        ballotOptionText.addTextChangedListener(submitTextWatcher);
        ballotOptions.add(ballotOptionText.getText().toString());
    }


    /**
     * Initializes the layout with two ballot options (minimum number of ballot options)
     */
    private void initNewBallotOptionsField() {
        addBallotOption();
        addBallotOption();
    }


    /**
     * Setups the button that adds a new ballot option on click
     */
    private void setupAddBallotOptionsButton() {
        FloatingActionButton addBallotOptionButton = mSetupElectionFragBinding.addBallotOption;
        addBallotOptionButton.setOnClickListener(v -> addBallotOption());
    }

    /**
     * Setups the submit button that creates the new election
     */
    private void setupElectionSubmitButton() {
        submitButton.setOnClickListener(
                v -> {
                    //We "deactivate" the button on click, to prevent the user from creating multiple elections at once
                    submitButton.setEnabled(false);
                    //When submitting, we compute the timestamps for the selected start and end time
                    computeTimesInSeconds();
                    //Filter the list of ballot options to keep only non-empty fields
                    List<String> filteredBallotOptions = new ArrayList<>();
                    for (String ballotOption: ballotOptions) {
                        if (!ballotOption.equals("")) filteredBallotOptions.add(ballotOption);
                    }
                    mLaoDetailViewModel.createNewElection(electionNameText.getText().toString(), startTimeInSeconds, endTimeInSeconds, votingMethod, mSetupElectionFragBinding.writeIn.isChecked(),
                            filteredBallotOptions, electionQuestionText.getText().toString());
                });
    }

    /**
     * Setups the cancel button, that brings back to LAO detail page
     */
    private void setupElectionCancelButton() {
        Button cancelButton = mSetupElectionFragBinding.electionCancelButton;
        cancelButton.setOnClickListener(v -> mLaoDetailViewModel.openLaoDetail());
    }

    /**
     * Setups the spinner displaying the voting methods, and that selects the corresponding item on click
     */
    private void setupElectionSpinner() {
        Spinner spinner = mSetupElectionFragBinding.electionSetupSpinner;
        String[] items = Arrays.stream(VotingMethods.values()).map(VotingMethods::getDesc).toArray(String[]::new);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    // Spinner methods
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        votingMethod = adapterView.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        votingMethod = "Plurality";
    }

}