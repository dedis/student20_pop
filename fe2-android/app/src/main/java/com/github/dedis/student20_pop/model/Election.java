package com.github.dedis.student20_pop.model;

import com.github.dedis.student20_pop.model.event.Event;
import com.github.dedis.student20_pop.model.event.EventType;
import com.github.dedis.student20_pop.model.network.method.message.data.election.ElectionQuestion;

import java.util.ArrayList;
import java.util.List;

public class Election extends Event {

    private String id;
    private String name;
    private long creation;
    private long start;
    private long end;

    private List<ElectionQuestion> electionQuestions;
    public Election() {
      this.electionQuestions = new ArrayList<>();
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


    public List<ElectionQuestion> getElectionQuestions() {
        return electionQuestions;
    }

    public void setElectionQuestions(List<ElectionQuestion> electionQuestions) {
        if(electionQuestions == null) throw new IllegalArgumentException();
        this.electionQuestions = electionQuestions;
    }

    @Override
    public long getStartTimestamp() {
        return start;
    }

    @Override
    public EventType getType() {
        return EventType.ELECTION;
    }

    @Override
    public long getEndTimestamp() {
        return end;
    }
}
