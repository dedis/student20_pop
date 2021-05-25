package com.github.dedis.student20_pop.home;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import androidx.test.espresso.matcher.BoundedMatcher;
import com.github.dedis.student20_pop.R;
import com.github.dedis.student20_pop.model.Lao;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Ignore;
import org.junit.Test;

// TODO: Update tests when the data between activities will be passed

@Ignore
public class HomeFragmentTest {

  /**
   * Matches a Lao with a given lao name
   *
   * @param title
   * @return
   */
  public static Matcher<Object> titleMatches(String title) {
    return new BoundedMatcher<Object, Lao>(Lao.class) {

      @Override
      public void describeTo(Description description) {
        description.appendText("with title " + title + "'");
      }

      @Override
      protected boolean matchesSafely(Lao item) {
        return item.getName().equals(title);
      }
    };
  }

  //  @Before
  //  public void launchActivity() {
  //    ActivityScenario.launch(MainActivity.class);
  //  }

  @Test
  public void homeFragmentIsDisplayed() {
    onView(withId(R.id.fragment_home)).check(matches(isDisplayed()));
  }

  @Test
  public void listOfLaosIsDisplayed() {
    onView(withId(R.id.lao_list)).check(matches(isDisplayed()));
  }

  @Test
  public void clickOnLaoWhichOfIAmOrganizerStartsOrganizer() {
    onData(allOf(is(instanceOf(Lao.class)), titleMatches("LAO I just joined"))).perform(click());
    onView(withId(R.id.fragment_organizer)).check(matches(isDisplayed()));
  }

  @Test
  public void clickOnLaoWhichOfIAmAttendeeStartsAttendee() {
    onData(allOf(is(instanceOf(Lao.class)), titleMatches("LAO 1"))).perform(click());
    onView(withId(R.id.fragment_attendee)).check(matches(isDisplayed()));
  }
}
