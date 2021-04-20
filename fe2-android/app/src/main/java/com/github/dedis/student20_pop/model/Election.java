package com.github.dedis.student20_pop.model;

import com.github.dedis.student20_pop.model.event.Event;
import com.github.dedis.student20_pop.model.event.EventType;
import com.github.dedis.student20_pop.model.network.method.message.data.election.ElectionQuestion;

import java.util.ArrayList;
import java.util.List;

public class Election extends Event {

    private String channel;
    private String id;
    private String name;
    private long creation;
    private long start;
    private long end;
    List<ElectionQuestion> electionQuestions;
    //votes as attribute ?


    public Election() {
        type = EventType.ELECTION;
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

    public List<ElectionQuestion> getElectionQuestions(){return electionQuestions;}

    public void setCreation(long creation) {
        this.creation = creation;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public void setEnd(long end) {
        this.end = end;
    }



    public void setChannel(String channel) {this.channel = channel;}

    public void setElectionQuestions(List<ElectionQuestion> electionQuestions){this.electionQuestions = electionQuestions;}
    @Override
    public long getStartTimestamp() {
        return start;
    }

    @Override
    public long getEndTimestamp() {
        return end;
    }
}
