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

import com.github.dedis.student20_pop.R;
import com.github.dedis.student20_pop.databinding.FragmentSetupElectionEventBinding;
import com.github.dedis.student20_pop.detail.LaoDetailActivity;
import com.github.dedis.student20_pop.detail.LaoDetailViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ElectionSetupFragment extends AbstractEventCreationFragment implements AdapterView.OnItemSelectedListener {

    public static final String TAG = ElectionSetupFragment.class.getSimpleName();

    private LaoDetailViewModel mLaoDetailViewModel;
    private FragmentSetupElectionEventBinding mSetupElectionFragBinding;

    private EditText electionNameText;
    private EditText electionQuestionText;
    private EditText ballotOption1;
    private EditText ballotOption2;
    private Button submitButton;
    private FloatingActionButton addBallotOptionButton;

    private enum votingMethods {Plurality}
    private votingMethods votingMethod;

    private List<String> ballotOptions;

    private final TextWatcher confirmTextWatcher =
            new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    boolean areFieldsFilled =
                            !electionNameText.getText().toString().trim().isEmpty() && !getStartDate().isEmpty() && !getStartTime().isEmpty() && !getEndDate().isEmpty() && !getEndTime().isEmpty() &&
                            !ballotOption1.getText().toString().trim().isEmpty() && !ballotOption2.getText().toString().trim().isEmpty() && !electionQuestionText.getText().toString().trim().isEmpty();
                    submitButton.setEnabled(areFieldsFilled);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            };

    public static ElectionSetupFragment newInstance() {
        return new ElectionSetupFragment();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        mSetupElectionFragBinding =
                FragmentSetupElectionEventBinding.inflate(inflater, container, false);

        setDateAndTimeView(mSetupElectionFragBinding.getRoot(), this, getFragmentManager());
        addDateAndTimeListener(confirmTextWatcher);

        View view = mSetupElectionFragBinding.getRoot();
        mLaoDetailViewModel = LaoDetailActivity.obtainViewModel(getActivity());

        Button cancelButton = mSetupElectionFragBinding.electionCancelButton;
        boolean write_in = mSetupElectionFragBinding.writeIn.isChecked();

        //Right place to instantiate ?
        ballotOptions = new ArrayList<>();

        submitButton = mSetupElectionFragBinding.electionSubmitButton;
        electionNameText = mSetupElectionFragBinding.electionSetupName;
        electionQuestionText = mSetupElectionFragBinding.electionQuestion;

        // At least two candidates must be selected
        ballotOption1 = mSetupElectionFragBinding.ballotOption1;
        ballotOption2 = mSetupElectionFragBinding.ballotOption2;


        //Adding ballot options
        addBallotOptionButton = mSetupElectionFragBinding.addBallotOption;
        addBallotOptionButton.setOnClickListener(
                v -> {
                    Context c = getActivity();
                    EditText ballotOption = new EditText(c);
                    ballotOption.setText("ballot option");
                    mSetupElectionFragBinding.ll.addView(ballotOption);
                }
        );



        // Set the text widget in layout to current LAO name
        TextView laoName = (TextView) view.findViewById(R.id.LAOname);
        laoName.setText(mLaoDetailViewModel.getCurrentLaoName().getValue());

        //Set dropdown spinner as a list of string (from enum of votingmethods)
        Spinner spinner = mSetupElectionFragBinding.electionSetupSpinner;
        String[] items = Arrays.stream(votingMethods.values()).map(votingMethods::toString).toArray(String[]::new);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, items);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) getActivity());


        //On click, submit button creates a new election event and launches the election UI for organizer
        submitButton.setOnClickListener(
                v -> {
                    computeTimesInSeconds();
                    String title = electionNameText.getText().toString();
                    List<String> questions = Arrays.asList(electionQuestionText.getText().toString());
                    mLaoDetailViewModel.createNewElection(title, startTimeInSeconds, endTimeInSeconds, votingMethod.toString(), write_in, ballotOptions, questions);
                    mLaoDetailViewModel.openLaoDetail();
                });

        //On click, cancel button takes back to LAO detail page
        cancelButton.setOnClickListener(
                v -> {
                    mLaoDetailViewModel.openLaoDetail();
                });


        mSetupElectionFragBinding.setLifecycleOwner(getActivity());

        return view;

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
}