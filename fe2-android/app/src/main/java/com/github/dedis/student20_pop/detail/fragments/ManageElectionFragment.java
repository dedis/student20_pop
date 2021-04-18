package com.github.dedis.student20_pop.detail.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.dedis.student20_pop.databinding.FragmentManageElectionBinding;
import com.github.dedis.student20_pop.detail.LaoDetailActivity;
import com.github.dedis.student20_pop.detail.LaoDetailViewModel;

import java.text.SimpleDateFormat;
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
    private Button editName;
    private Button editQuestion;
    private Button editBallotOptions;
    private TextView currentTime;
    private TextView startTime;
    private TextView endTime;
    private TextView question;
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

        //setDateAndTimeView(mManageElectionFragBinding.getRoot(), this, getFragmentManager());

        terminate = mManageElectionFragBinding.terminateElection;
        editName = mManageElectionFragBinding.editName;
        editQuestion = mManageElectionFragBinding.editQuestion;
        editBallotOptions = mManageElectionFragBinding.editBallotOptions;
        currentTime = mManageElectionFragBinding.displayedCurrentTime;
        startTime = mManageElectionFragBinding.displayedStartTime;
        endTime = mManageElectionFragBinding.displayedEndTime;
        question = mManageElectionFragBinding.electionQuestion;
        Date dCurrent = new java.util.Date(System.currentTimeMillis()); // Get's the date based on the unix time stamp
        Date dStart = new java.util.Date(laoDetailViewModel.getCurrentElection().getStartTimestamp() * 1000);// *1000 because it needs to be in milisecond
        Date dEnd = new java.util.Date(laoDetailViewModel.getCurrentElection().getEndTimestamp() * 1000);
        currentTime.setText(DATE_FORMAT.format(dCurrent)); // Set's the start time in the form dd/MM/yyyy HH:mm z
        startTime.setText(DATE_FORMAT.format(dStart));
        endTime.setText(DATE_FORMAT.format(dEnd));
        laoName.setText(laoDetailViewModel.getCurrentLaoName().getValue());
        electionName.setText(laoDetailViewModel.getCurrentElection().getName());

        mManageElectionFragBinding.setLifecycleOwner(getActivity());
        return mManageElectionFragBinding.getRoot();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //On click, terminate button  current Election
        terminate.setOnClickListener(
                v -> {
                    //computeTimesInSeconds();
                    laoDetailViewModel.terminateCurrentElection();
                    laoDetailViewModel.openLaoDetail();
                });
    }
}
