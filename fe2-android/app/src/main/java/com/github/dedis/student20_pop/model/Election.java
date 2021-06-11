package com.github.dedis.student20_pop.model;


import androidx.lifecycle.MutableLiveData;

import com.github.dedis.student20_pop.model.event.Event;
import com.github.dedis.student20_pop.model.network.method.message.ElectionQuestion;
import com.github.dedis.student20_pop.model.network.method.message.ElectionVote;
import com.github.dedis.student20_pop.model.network.method.message.QuestionResult;
import com.github.dedis.student20_pop.model.event.EventType;
import com.github.dedis.student20_pop.utility.security.Hash;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Election extends Event {

    private String channel;
    private String id;
    private String name;
    private long creation;
    private long start;
    private long end;
    private List<ElectionQuestion> electionQuestions;

    private MutableLiveData<Boolean> isEnded = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> areResultsReady = new MutableLiveData<>(false);

    //Map that associates each sender pk to their votes
    private Map<String, List<ElectionVote>> voteMap;
    //Map that associates each messageId to its sender
    private Map<String, String> messageMap;


    //Results of an election
    private List<QuestionResult> results;

    public Election() {
        this.results = new ArrayList<>();
        this.electionQuestions = new ArrayList<>();
        this.voteMap = new HashMap<>();
        this.messageMap = new TreeMap<>();
    }

    public void setResultsReady(boolean areResultsReady) {this.areResultsReady.postValue(areResultsReady);}

    public MutableLiveData<Boolean> areResultsReady() {return areResultsReady;}

    public void setEnded(boolean isEnded) {
        this.isEnded.postValue(isEnded);
    }

    public MutableLiveData<Boolean> isEnded() {
        return isEnded;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreation() {
        return creation;
    }

    public String getChannel(){ return channel; }

    public List<ElectionQuestion> getElectionQuestions() {
        return electionQuestions;
    }

    public void setCreation(long creation) {
        if (creation < 0) throw new IllegalArgumentException();
        this.creation = creation;
    }

    public void setStart(long start) {
        if (start < 0) throw new IllegalArgumentException();
        this.start = start;
    }

    public void setEnd(long end) {
        if (end < 0) throw new IllegalArgumentException();
        this.end = end;
    }

    public Map<String, String> getMessageMap() {
        return messageMap;
    }

    public void putVotesBySender(String senderPk, List<ElectionVote> votes) {
        if (senderPk == null) throw new IllegalArgumentException("Sender public key cannot be null.");
        if (votes == null || votes.isEmpty()) throw new IllegalArgumentException("votes cannot be null or empty");
        //The list must be sorted by order of question ids
        List<ElectionVote> votesCopy = new ArrayList<>(votes);
        Collections.sort(votesCopy, (Comparator<ElectionVote>) (v1, v2) -> v1.getQuestionId().compareTo(v2.getQuestionId()));
        voteMap.put(senderPk, votesCopy);
    }

    public void putSenderByMessageId(String senderPk, String messageId) {
        if (senderPk == null || messageId == null) throw new IllegalArgumentException("Sender public key or message id cannot be null.");
        messageMap.put(messageId, senderPk);
    }

    public void setChannel(String channel) {this.channel = channel;}

    public void setElectionQuestions(List<ElectionQuestion> electionQuestions) {
        this.electionQuestions = electionQuestions;
    }

    @Override
    public long getStartTimestamp() {
        return start;
    }

    @Override
    public long getEndTimestamp() {
        return end;
    }

    /**
     * Computes the hash for the registered votes, when terminating an election (sorted by message id's alphabetical order)
     * @return the hash of all registered votes
     */
    public String computerRegisteredVotes() {
        List<String> listOfVoteIds = new ArrayList<>();
        //Since messageMap is a TreeMap, votes will already be sorted in the alphabetical order of messageIds
        for (String senderPk: messageMap.values()) {
            for (ElectionVote vote: voteMap.get(senderPk)) {
                listOfVoteIds.add(vote.getId());
            }
        }
        if (listOfVoteIds.isEmpty()) return Hash.hash("novotes");
        else return Hash.hash(listOfVoteIds.toArray(new String[0]));
    }


    public void setResults(List<QuestionResult> results) {
        if (results == null) throw new IllegalArgumentException("the list of winners should not be null");
        results.sort((r1, r2) -> r2.getCount().compareTo(r1.getCount()));
        this.results = results;
    }

    public List<QuestionResult> getResults() {
        return results;
    }

    @Override
    public EventType getType() {
        return EventType.ELECTION;
    }
}
