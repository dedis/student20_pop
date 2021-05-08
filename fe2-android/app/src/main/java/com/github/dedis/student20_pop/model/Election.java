package com.github.dedis.student20_pop.model;

import com.github.dedis.student20_pop.model.event.Event;
import com.github.dedis.student20_pop.model.event.EventType;
import com.github.dedis.student20_pop.model.network.method.message.ElectionQuestion;

import java.util.ArrayList;
import java.util.List;

public class Election extends Event {

    private String id;
    private String channel;
    private String name;
    private long creation;
    private long start;
    private long end;
    private boolean writeIn;
    private String question;
    List<ElectionQuestion> electionQuestions;
    private List<String> ballotOptions;

    public Election() {
        this.ballotOptions = new ArrayList<>();
        type = EventType.ELECTION;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null) throw new IllegalArgumentException("Election's id shouldn't be null");
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) throw new IllegalArgumentException("Election's name shouldn't be null");
        this.name = name;
    }

    public long getCreation() {
        return creation;
    }

    public String getChannel(){ return channel; }


    private void checkTime(long time) {
        if (time < 0) throw new IllegalArgumentException("A time can't be negative");
    }

    public void setCreation(long creation) {
        checkTime(creation);
        this.creation = creation;
    }

    public void setStart(long start) {
        checkTime(start);
        this.start = start;
    }

    public void setEnd(long end) {
        checkTime(end);
        this.end = end;
    }

    public void setChannel(String channel) {this.channel = channel;}

    public List<String> getBallotOptions() {
        return ballotOptions;
    }

    public void setBallotOptions(List<String> ballotOptions) {
        if (ballotOptions == null)
            throw new IllegalArgumentException("ballot options can't be null");
        if (ballotOptions.size() < 2)
            throw new IllegalArgumentException("ballot must have at least two options");
        this.ballotOptions = ballotOptions;
    }

    public String getQuestion() {
        return question;
    }

    public List<ElectionQuestion> getElectionQuestions(){return electionQuestions;}

    public void setQuestion(String question) {
        if (question == null) throw new IllegalArgumentException("question can't be null");
        this.question = question;
    }

    public boolean getWriteIn() {
        return writeIn;
    }

    public void setWriteIn(boolean writeIn) {
        this.writeIn = writeIn;
    }

    @Override
    public long getStartTimestamp() {
        return start;
    }

    @Override
    public long getEndTimestamp() {
        return end;
    }
}
