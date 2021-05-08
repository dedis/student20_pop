package com.github.dedis.student20_pop.detail.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.dedis.student20_pop.R;
import com.github.dedis.student20_pop.databinding.FragmentManageElectionBinding;
import com.github.dedis.student20_pop.detail.LaoDetailActivity;
import com.github.dedis.student20_pop.detail.LaoDetailViewModel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ManageElectionFragment extends Fragment {

    public static final String TAG = ManageElectionFragment.class.getSimpleName();

    private static final int EDIT_NAME_CODE = 0; // Used to identify the request
    private static final int EDIT_QUESTION_CODE = 1;
    private static final int EDIT_BALLOT_CODE = 2;
    private static final int START_TIME_CODE = 3;
    private static final int END_TIME_CODE = 4;
    private static final int START_DATE_CODE = 5;
    private static final int END_DATE_CODE = 6;
    private static final int CANCEL_CODE = 7;
    private final Calendar calendar = Calendar.getInstance();
    private int requestCode;
    private String newName;
    private String newQuestion;
    protected static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd/MM/yyyy HH:mm z", Locale.ENGLISH);
    private FragmentManageElectionBinding mManageElectionFragBinding;
    private TextView laoName;
    private TextView electionName;
    private Button terminate;
    private TextView currentTime;
    private TextView startTime;
    private TextView endTime;
    private TextView question;
    private Button editName;
    private Button editQuestion;
    private Button editBallotOptions;
    private Button editStartTimeButton;
    private Button editEndTimeButton;
    private Button editStartDateButton;
    private Button editEndDateButton;
    private LaoDetailViewModel laoDetailViewModel;


    public static ManageElectionFragment newInstance() {
        return new ManageElectionFragment();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        mManageElectionFragBinding =
                FragmentManageElectionBinding.inflate(inflater, container, false);

        laoDetailViewModel = LaoDetailActivity.obtainViewModel(getActivity());
        terminate = mManageElectionFragBinding.terminateElection;
        editStartTimeButton = mManageElectionFragBinding.editStartTime;
        editEndTimeButton = mManageElectionFragBinding.editEndTime;
        editName = mManageElectionFragBinding.editName;
        editQuestion = mManageElectionFragBinding.editQuestion;
        editBallotOptions = mManageElectionFragBinding.editBallotOptions;
        currentTime = mManageElectionFragBinding.displayedCurrentTime;
        startTime = mManageElectionFragBinding.displayedStartTime;
        endTime = mManageElectionFragBinding.displayedEndTime;
        editStartDateButton = mManageElectionFragBinding.editStartDate;
        editEndDateButton = mManageElectionFragBinding.editEndDate;
        question = mManageElectionFragBinding.electionQuestion;
        laoName = mManageElectionFragBinding.manageElectionLaoName;
        electionName = mManageElectionFragBinding.manageElectionTitle;
        Date dCurrent = new java.util.Date(System.currentTimeMillis()); // Get's the date based on the unix time stamp
        Date dStart = new java.util.Date(laoDetailViewModel.getCurrentElection().getStartTimestamp() * 1000);// *1000 because it needs to be in milisecond
        Date dEnd = new java.util.Date(laoDetailViewModel.getCurrentElection().getEndTimestamp() * 1000);
        currentTime.setText(DATE_FORMAT.format(dCurrent)); // Set's the start time in the form dd/MM/yyyy HH:mm z
        startTime.setText(DATE_FORMAT.format(dStart));
        endTime.setText(DATE_FORMAT.format(dEnd));
        laoName.setText(laoDetailViewModel.getCurrentLaoName().getValue());
        electionName.setText(laoDetailViewModel.getCurrentElection().getName());
        question.setText("Election Question : " + laoDetailViewModel.getCurrentElection().getQuestion());
        mManageElectionFragBinding.setLifecycleOwner(getActivity());
        return mManageElectionFragBinding.getRoot();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Button back = (Button) getActivity().findViewById(R.id.tab_back);
        back.setOnClickListener(v->laoDetailViewModel.openLaoDetail());

        Calendar now = Calendar.getInstance();

        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    switch (requestCode) {
                        case CANCEL_CODE:
                            laoDetailViewModel.terminateCurrentElection();
                            laoDetailViewModel.openLaoDetail();


                        case START_TIME_CODE:


                    }
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        };


        // Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener);

        // create the timePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, hourOfDay, minute)  -> builder.show(), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePickerDialog.setButton(TimePickerDialog.BUTTON_POSITIVE,"Modify Time",timePickerDialog);

        // create the DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, day)  -> builder.show(), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE,"Modify Date",timePickerDialog);

        // create the Alert Dialog to edit name
        AlertDialog.Builder editNameBuilder = new AlertDialog.Builder(getContext());
        editNameBuilder.setTitle("Edit Election Name");
        editNameBuilder.setMessage("Please enter the new name you want for the election ");
// Set up the input
        final EditText inputName = new EditText(getContext());

        inputName.setHint("New Name");


// Set up the buttons
        editNameBuilder.setPositiveButton("SUBMIT", (dialog, which) -> {
            newName = inputName.getText().toString();
            requestCode = EDIT_NAME_CODE;
            builder.show();
        });
        editNameBuilder.setNegativeButton("CANCEL", (dialog, which) -> {
            dialog.cancel();
        });

        editNameBuilder.create();

        // create the Alert Dialog to edit question
        AlertDialog.Builder editQuestionBuilder = new AlertDialog.Builder(getContext());
        editQuestionBuilder.setTitle("Edit Election Question");
        editQuestionBuilder.setMessage("Please enter the new question you want for the election ");
// Set up the input
        final EditText inputQuestion = new EditText(getContext());

        inputQuestion.setHint("New Question");



// Set up the buttons
        editQuestionBuilder.setPositiveButton("SUBMIT", (dialog, which) -> {
            newQuestion = inputQuestion.getText().toString();
            requestCode = EDIT_QUESTION_CODE;
            builder.show();
        });
        editQuestionBuilder.setNegativeButton("CANCEL", (dialog, which) -> {
            dialog.cancel();
        });

        editQuestionBuilder.create();

        // On click, edit new name button

        editName.setOnClickListener(v-> {
            inputName.setText(null); // we make sure the text is blank when we reclick the button
            if(inputName.getParent() != null) {
                ((ViewGroup)inputName.getParent()).removeView(inputName);
            }
            editNameBuilder.setView(inputName);

            editNameBuilder.show();
        });

        // On click, edit new question button

        editQuestion.setOnClickListener(v-> {
            inputQuestion.setText(null); // we make sure the text is blank when we click the button
            if(inputQuestion.getParent() != null) {
                ((ViewGroup)inputQuestion.getParent()).removeView(inputQuestion);
            }
            editQuestionBuilder.setView(inputQuestion);
            editQuestionBuilder.show();
        });

        // On click, edit start time button
        editStartTimeButton.setOnClickListener(
                v -> {
                    // we set the request code
                    requestCode = START_TIME_CODE;
                    // show the timePicker
                    timePickerDialog.show();
                });

        // On click, edit end time button
        editEndTimeButton.setOnClickListener(
                v -> {
                    // we set the request code
                    requestCode = END_TIME_CODE;
                    // show the timePicker
                    timePickerDialog.show();
                });

        // On click, edit start date button
        editStartDateButton.setOnClickListener(
                v -> {
                    // we set the request code
                    requestCode = START_DATE_CODE;
                    // show the timePicker
                    datePickerDialog.show();
                });

        // On click, edit end time button
        editEndDateButton.setOnClickListener(
                v -> {
                    // we set the request code
                    requestCode = END_DATE_CODE;
                    // show the timePicker
                    datePickerDialog.show();
                });

        //On click, cancel button  current Election
        terminate.setOnClickListener(
                v -> {
                    requestCode = CANCEL_CODE;
                    builder.show();

                });



    }


}
