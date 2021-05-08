package com.github.dedis.student20_pop.detail.fragments;

import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

import com.github.dedis.student20_pop.R;
import com.github.dedis.student20_pop.detail.LaoDetailActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ManageElectionFragmentTest {

    private final int YEAR = 2022;
      private final int MONTH_OF_YEAR = 10;
      private final int DAY_OF_MONTH = 10;
      private final String DATE = "" + DAY_OF_MONTH + "/" + MONTH_OF_YEAR + "/" + YEAR;
      private final int HOURS = 12;
      private final int MINUTES = 15;
      private final String TIME = "" + HOURS + ":" + MINUTES;

      @Rule
      public ActivityScenarioRule<LaoDetailActivity> activityScenarioRule =
          new ActivityScenarioRule<>(LaoDetailActivity.class);

      private View decorView;

      @Before
      public void setUp() {
        activityScenarioRule
            .getScenario()
            .onActivity(
                new ActivityScenario.ActivityAction<LaoDetailActivity>() {

                  /**
                   * This method is invoked on the main thread with the reference to the Activity.
                   *
                   * @param laoDetailActivity an Activity instrumented by the {@link ActivityScenario}. It
     never
                   *     be null.
                   */
                  @Override
                  public void perform(LaoDetailActivity laoDetailActivity) {
                    decorView = laoDetailActivity.getWindow().getDecorView();
                  }
                });

        onView(
                allOf(
                    withId(R.id.election_edit_button), // here we find the election edit button on any election and click
                                                          // on it
                    withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
            .perform(click());

      }

      @Test
      public void canLaunchManageElectionFragment() {
        onView(withId(R.id.fragment_manage_election)).check(matches((isDisplayed())));
      }


      @Test
      public void cancelButtonWorks() {
        onView(withId(R.id.cancel_election)).perform(click());
        onView(withId(R.id.lao_detail_linear_layout)).check(matches(isDisplayed()));
      }


}
