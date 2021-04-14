package com.github.dedis.student20_pop.detail.fragments;

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
    String [] ballotOptions;
    Set<String> selectedOptions;
    String uniqueSelectedOption;
    private TextView laoNameText;
    private TextView voteInText;
    private TextView electionNameText;
    private ListView lvBallots;
    private Button voteButton;
    private FragmentCastVoteBinding mElectionDisplayFragBinding;

    private LaoDetailViewModel mLaoDetailViewModel;



    private View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (selectedOptions.isEmpty()) {
                    Toast.makeText(getActivity(), "No voting option selected", Toast.LENGTH_SHORT);
            } else {
                castVote();
                }
            }
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

        //Getting the Lao Name
        laoNameText.setText(mLaoDetailViewModel.getCurrentLaoName().getValue());

        //Getting ballot options
//        setBallotOptions();
        setDummyBallotOptions();

        electionNameText.setText("General Election");

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

    private void setDummyBallotOptions(){
        ballotOptions = new String[]{"Alan Turing", "John von Neumann", "Claude Shannon", "Something else", "FOO BAR", "Some other stuff", "stuff", "Anything", "Some other stuff"};
      //  ballotOptions = new String[]{"Alan Turing", "John von Neumann", "Claude Shannon", "Something else"};
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String vote = parent.getItemAtPosition(position).toString();
        if (uniqueSelectedOption.equals(vote)){
            voteButton.setEnabled(false);
        }
        else{
            voteButton.setEnabled(true);
        }
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

    private void castVote(){

    }


}