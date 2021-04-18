package com.github.dedis.student20_pop.home;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import com.github.dedis.student20_pop.R;
import org.junit.Ignore;
import org.junit.Test;

public class LaunchFragmentTest {

  //  @Before
  //  public void launchActivity() {
  //    ActivityScenario.launch(MainActivity.class);
  //  }

  @Test
  @Ignore("TODO : Modify test because it requires connection to backend")
  public void launchNewLaoSetsInfoTest() {
    onView(withId(R.id.tab_launch)).perform(click());
    onView(withId(R.id.entry_box_launch)).perform(typeText("LAO"), closeSoftKeyboard());
    onView(withId(R.id.button_launch)).perform(click());

    //    ActivityScenario.launch(OrganizerActivity.class)
    //        .onActivity(
    //            a -> {
    //              PoPApplication app = (PoPApplication) a.getApplication();
    //              assertThat(app.getLaos().get(0).getName(), is("LAO"));
    //              assertThat(app.getPerson().getName(), is(PoPApplication.USERNAME));
    //              assertThat(app.getPerson().getLaos().get(0), is(app.getLaos().get(0).getId()));
    //            });
  }
}
