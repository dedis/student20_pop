package com.github.dedis.student20_pop.detail.fragments;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    private Calendar modifyStartTime = Calendar.getInstance();
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
        editName = mManageElectionFragBinding.editName;
        editQuestion = mManageElectionFragBinding.editQuestion;
        editBallotOptions = mManageElectionFragBinding.editBallotOptions;
        currentTime = mManageElectionFragBinding.displayedCurrentTime;
        startTime = mManageElectionFragBinding.displayedStartTime;
        endTime = mManageElectionFragBinding.displayedEndTime;
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

        //todo change when multiple questions
        question.setText("Election Question : " + laoDetailViewModel.getCurrentElection().getElectionQuestions().get(0).getQuestion());
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
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        };
        // Date Select Listener.
        TimePickerDialog.OnTimeSetListener timeSetListener = (view, hourOfDay, minute) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        };

        editStartTimeButton.setOnClickListener(
                v -> {


                    // create the timePickerDialog
                    TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                            timeSetListener, now.HOUR_OF_DAY, now.MINUTE, true);
                    timePickerDialog.setButton(TimePickerDialog.BUTTON_POSITIVE,"Modify Time",timePickerDialog);
                    // show the timePicker
                    timePickerDialog.show();
                });

        //On click, terminate button  current Election
        terminate.setOnClickListener(
                v -> {
                    laoDetailViewModel.terminateCurrentElection();
                    laoDetailViewModel.openLaoDetail();
                });



    }


}


