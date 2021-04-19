package com.github.dedis.student20_pop.detail.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dedis.student20_pop.R;
import com.github.dedis.student20_pop.databinding.FragmentCastVoteBinding;
import com.github.dedis.student20_pop.detail.LaoDetailActivity;
import com.github.dedis.student20_pop.detail.LaoDetailViewModel;
import com.github.dedis.student20_pop.model.Election;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CastVoteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CastVoteFragment extends Fragment implements AdapterView.OnItemClickListener {



    private int numberOfChoices;
    List<String> ballotOptions;
    Set<String> selectedOptions;
    private long selectedOption;
    private TextView laoNameText;
    private TextView voteInText;
    private TextView electionNameText;
    private ListView lvBallots;
    private Button voteButton;
    private FragmentCastVoteBinding mElectionDisplayFragBinding;

    private Election election;
    private LaoDetailViewModel mLaoDetailViewModel;



    private View.OnClickListener buttonListener = v -> {
        List<List<Long>> list = Arrays.asList(Arrays.asList(selectedOption));
        System.out.println("Size is " + list.size());
        System.out.println(list.get(0).get(0));
        mLaoDetailViewModel.sendVote(election, list );

    };



    public CastVoteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    // TODO: Rename and change types and number of parameters
    public static CastVoteFragment newInstance() {
        return new CastVoteFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        selectedOptions = new HashSet<>();
         //Inflate the layout for this fragment
        mElectionDisplayFragBinding =
                FragmentCastVoteBinding.inflate(inflater,container, false);
        mLaoDetailViewModel = LaoDetailActivity.obtainViewModel(getActivity());
        laoNameText = mElectionDisplayFragBinding.castVoteLaoName;
        mElectionDisplayFragBinding.castVoteVoteInText.setText("Vote in");
        electionNameText = mElectionDisplayFragBinding.castVoteElectionName;

        //setUp the cast Vote button
        voteButton = mElectionDisplayFragBinding.castVoteButton;
        voteButton.setEnabled(false);



        //Getting election
        election = mLaoDetailViewModel.getCurrentElection();

        //Getting the Lao Name
        laoNameText.setText(mLaoDetailViewModel.getCurrentLaoName().getValue());

        //Getting ballot options
        ballotOptions = election.getBallotOptions();

        //todo get real election name
        electionNameText.setText(election.getName());

        lvBallots = mElectionDisplayFragBinding.castVoteListView;

        //Single options allowed by default
        numberOfChoices = 1;
        lvBallots.setChoiceMode(numberOfChoices);
        ArrayAdapter<String> ballotAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, ballotOptions);

       // ArrayAdapter<String> ballotAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, ballotOptions);
        lvBallots.setAdapter(ballotAdapter);

        lvBallots.setOnItemClickListener(this);
        voteButton.setOnClickListener(buttonListener);
        return mElectionDisplayFragBinding.getRoot();
    }

    private void setBallotOptions(){

    }

//    private void setDummyBallotOptions(){
//        ballotOptions = new String[]{"Alan Turing", "John von Neumann", "Claude Shannon", "Linus Torvalds", "Ken Thompson", "Tim Berners-Lee", "Charles Babbage", "Barbara Liskov", "Ronald Rivest", "Adi Shamir", "Len Adleman"};
//
//      //  ballotOptions = new String[]{"Alan Turing", "John von Neumann", "Claude Shannon", "Something else", "FOO BAR", "Some other stuff", "stuff", "Anything", "Some other stuff"};
//      //  ballotOptions = new String[]{"Alan Turing", "John von Neumann", "Claude Shannon", "Something else"};
//    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        view.setBackgroundColor(Color.TRANSPARENT);
        if (selectedOption == -1){
            selectedOption = position;
            lvBallots.setItemChecked(position, true);
            voteButton.setEnabled(true);
        }//No previously selected option

        else if(selectedOption == position){
            lvBallots.setItemChecked((int) selectedOption, false);
            selectedOption = -1;
            voteButton.setEnabled(false);
            view.setBackgroundColor(Color.WHITE);
        }//Unselecting choice

        else{
            lvBallots.setItemChecked((int) selectedOption, false);
            selectedOption = position;
            lvBallots.setItemChecked(position, true);
            voteButton.setEnabled(true);
        } //Changing the selected choice


        //That would be for multi choice later
//        if(selectedOptions.contains(vote)){
//            selectedOptions.remove(vote);
//            if (selectedOptions.isEmpty()){
//                voteButton.setEnabled(false);
//            }
//        }
//        else{
//            selectedOptions.add(vote);
//            voteButton.setEnabled(true);
//        }
    }



}