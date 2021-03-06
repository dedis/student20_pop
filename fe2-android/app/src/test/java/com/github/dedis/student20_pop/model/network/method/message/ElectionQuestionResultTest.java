package com.github.dedis.student20_pop.model.network.method.message;

import com.github.dedis.student20_pop.model.network.method.message.data.ElectionResultQuestion;
import com.github.dedis.student20_pop.model.network.method.message.data.QuestionResult;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

public class ElectionQuestionResultTest {
    private String questionId = "questionId";
    private List<QuestionResult> results = Arrays.asList(new QuestionResult("Candidate1", 30));
    private ElectionResultQuestion electionQuestionResult = new ElectionResultQuestion(questionId, results);

    @Test
    public void electionQuestionResultGetterReturnsCorrectQuestionId() {
        assertThat(electionQuestionResult.getId(), is(questionId));
    }

    @Test
    public void electionQuestionResultGetterReturnsCorrectResults() {
        assertThat(electionQuestionResult.getResult(), is(results));
    }

    @Test
    public void fieldsCantBeNull() {
        assertThrows(IllegalArgumentException.class, () -> new ElectionResultQuestion(null, results));
        assertThrows(IllegalArgumentException.class, () -> new ElectionResultQuestion(questionId, null));
    }

    @Test
    public void resultsCantBeEmpty() {
        List<QuestionResult> emptyList = new ArrayList<>();
        assertThrows(IllegalArgumentException.class, () -> new ElectionResultQuestion(questionId, emptyList));
    }
}
