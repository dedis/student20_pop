package com.github.dedis.student20_pop.detail.fragments.event.creation;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.github.dedis.student20_pop.R;
import com.github.dedis.student20_pop.home.HomeActivity;
import com.github.dedis.student20_pop.model.Lao;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

// TODO : Mock the server with LAOService interface to return a PublishSubject object that you control whenever observeMessage is called
@LargeTest
@RunWith(AndroidJUnit4.class)
public class ElectionSetupTest {
    public static final String TAG = ElectionSetupTest.class.getSimpleName();
    private static Matcher<Lao> laoHasName(
            final String name) {
        return new TypeSafeMatcher<Lao>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("select LAO with name "+name);
            }
            @Override
            public boolean matchesSafely(Lao lao) {
                return lao.getName().equals(name);
            }
        };
    }

    public static Matcher<View> matchesDate( final int day, final int month,final int year) {
        return new BoundedMatcher<View, DatePicker>(DatePicker.class) {

            @Override
            public void describeTo(Description description) {
                description.appendText("matches date:");
            }

            @Override
            protected boolean matchesSafely(DatePicker item) {
                return (year == item.getYear() && month == item.getMonth() && day == item.getDayOfMonth());
            }
        };
    }

    @Rule
    public ActivityTestRule<HomeActivity> mActivityTestRule = new ActivityTestRule<>(HomeActivity.class);

   //Start Date
    // we initialize the start date to now
    private static Calendar calendar = Calendar.getInstance();
    private static final int startYear = calendar.get(Calendar.YEAR);
    private static final int startMonth = calendar.get(Calendar.MONTH);
    private static final int startDay = calendar.get(Calendar.DAY_OF_MONTH);

    //End Date
    private static final int endYear = 2030;
    private static final int endMonth = 7;
    private static final int endDay = 16;

    //Start Time
    private static final int startHour = calendar.getTime().getHours();
    private static final int startMinute = calendar.getTime().getMinutes();

    //End Time
    private static final int endHour = 15;
    private static final int endMinute = 20;

    @Test
    public void electionSetupTest() {
            ViewInteraction appCompatButton = onView(
                    allOf(withId(R.id.tab_wallet), withText("Wallet"),
                            childAtPosition(
                                    allOf(withId(R.id.tab_wallet_only),
                                            childAtPosition(
                                                    withId(R.id.fragment_container_home),
                                                    2)),
                                    2),
                            isDisplayed()));
            appCompatButton.perform(click());

            ViewInteraction appCompatButton2 = onView(
                    allOf(withId(R.id.button_new_wallet), withText("New wallet"),
                            childAtPosition(
                                    childAtPosition(
                                            withClassName(is("android.widget.LinearLayout")),
                                            3),
                                    0),
                            isDisplayed()));
            appCompatButton2.perform(click());

            ViewInteraction appCompatButton3 = onView(
                    allOf(withId(R.id.button_confirm_seed), withText("Confirm"),
                            childAtPosition(
                                    childAtPosition(
                                            withClassName(is("android.widget.LinearLayout")),
                                            2),
                                    0),
                            isDisplayed()));
            appCompatButton3.perform(click());

            ViewInteraction appCompatButton4 = onView(
                    allOf(withId(android.R.id.button1), withText("Yes"),
                            childAtPosition(
                                    childAtPosition(
                                            withId(R.id.buttonPanel),
                                            0),
                                    3)));
            appCompatButton4.perform(scrollTo(), click());

            ViewInteraction appCompatButton5 = onView(
                    allOf(withId(R.id.tab_launch), withText("Launch"),
                            childAtPosition(
                                    allOf(withId(R.id.tab_connect_launch),
                                            childAtPosition(
                                                    withId(R.id.fragment_container_home),
                                                    1)),
                                    5),
                            isDisplayed()));
            appCompatButton5.perform(click());

            ViewInteraction appCompatEditText = onView(
                    allOf(withId(R.id.entry_box_launch),
                            childAtPosition(
                                    childAtPosition(
                                            withId(R.id.fragment_launch),
                                            0),
                                    2),
                            isDisplayed()));
            appCompatEditText.perform(replaceText("new lao test"), closeSoftKeyboard());

            ViewInteraction editText = onView(
                    allOf(withId(R.id.entry_box_launch), withText("new lao test"),
                            withParent(withParent(withId(R.id.fragment_launch))),
                            isDisplayed()));

            //We check that the entry_box_launch contains  " new lao test"
            editText.check(matches(withText("new lao test")));

            ViewInteraction appCompatEditText2 = onView(
                    allOf(withId(R.id.entry_box_launch), withText("new lao test"),
                            childAtPosition(
                                    childAtPosition(
                                            withId(R.id.fragment_launch),
                                            0),
                                    2),
                            isDisplayed()));
            appCompatEditText2.perform(pressImeActionButton());

            ViewInteraction appCompatButton6 = onView(
                    allOf(withId(R.id.button_launch), withText("Launch"),
                            childAtPosition(
                                    childAtPosition(
                                            withClassName(is("android.widget.LinearLayout")),
                                            3),
                                    1),
                            isDisplayed()));
            appCompatButton6.perform(click());

            ViewInteraction textView = onView(
                    allOf(withId(R.id.lao_name), withText("new lao test"),
                            withParent(allOf(withId(R.id.layout_lao_home),
                                    withParent(withId(R.id.lao_list)))),
                            isDisplayed()));
            //We check that the name of the lao displayed is " new lao test"
            textView.check(matches(withText("new lao test")));

            DataInteraction appCompatButton16 = onData(
                    allOf(is(instanceOf(Lao.class)), laoHasName("new lao test"))).inAdapterView(withId(R.id.lao_list));
            appCompatButton16.perform(click());

            // We check that we opened the launch fragment
            onView(withId(R.id.fragment_lao_detail)).check(matches(isDisplayed()));

       /* // click on show/hide properties

        ViewInteraction appCompatButton8 = onView(
                allOf(withId(R.id.tab_properties), withText("Show/Hide Properties"),
                        childAtPosition(
                                allOf(withId(R.id.tab_back),
                                        childAtPosition(
                                                withId(R.id.fragment_lao_detail),
                                                0)),
                                2),
                        isDisplayed()));
        appCompatButton8.perform(click());



        // click on add witness button

        ViewInteraction appCompatImageButton2 = onView(
                allOf(withId(R.id.add_witness_button),
                        childAtPosition(
                                allOf(withId(R.id.edit_properties_linear_layout),
                                        childAtPosition(
                                                withId(R.id.properties_linear_layout),
                                                4)),
                                1),
                        isDisplayed()));
        appCompatImageButton2.perform(click());*/


            // we click on add future events
            ViewInteraction appCompatImageButton3 = onView(
                    allOf(withId(R.id.add_future_event_button),
                            childAtPosition(
                                    allOf(withId(R.id.linearLayout),
                                            withParent(withId(R.id.exp_list_view))),
                                    1),
                            isDisplayed()));
            appCompatImageButton3.perform(click());

            // we click to open an election
            DataInteraction appCompatCheckedTextView = onData(anything())
                    .inAdapterView(allOf(withClassName(is("com.android.internal.app.AlertController$RecycleListView")),
                            childAtPosition(
                                    withClassName(is("android.widget.FrameLayout")),
                                    0)))
                    .atPosition(1);
            appCompatCheckedTextView.perform(click());

            // We check that we opened the election setup fragment
            onView(withId(R.id.fragment_setup_election_event)).check(matches(isDisplayed()));

            ViewInteraction appCompatEditText3 = onView(
                    allOf(withId(R.id.election_setup_name),
                            childAtPosition(
                                    allOf(withId(R.id.election_setup_fields_ll),
                                            childAtPosition(
                                                    withId(R.id.election_setup_scrollview),
                                                    0)),
                                    2)));
            appCompatEditText3.perform(scrollTo(), replaceText("new election test"), closeSoftKeyboard());

            // we set the start date
            ViewInteraction appCompatEditText4 = onView(
                    allOf(withId(R.id.start_date_edit_text),
                            childAtPosition(
                                    allOf(withId(R.id.election_setup_date),
                                            childAtPosition(
                                                    withId(R.id.election_setup_fields_ll),
                                                    3)),
                                    0)));
            appCompatEditText4.perform(scrollTo(), click());
            onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(startYear, startMonth + 1, startDay));

            ViewInteraction appCompatButton11 = onView(
                    allOf(withId(android.R.id.button1), withText("OK"),
                            childAtPosition(
                                    childAtPosition(
                                            withClassName(is("android.widget.ScrollView")),
                                            0),
                                    3)));
            appCompatButton11.perform(scrollTo(), click());


            ViewInteraction appCompatEditText5 = onView(
                    allOf(withId(R.id.end_date_edit_text),
                            childAtPosition(
                                    allOf(withId(R.id.election_setup_date),
                                            childAtPosition(
                                                    withId(R.id.election_setup_fields_ll),
                                                    3)),
                                    1)));
            appCompatEditText5.perform(scrollTo(), click());
            onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(endYear, endMonth, endDay));

            ViewInteraction appCompatButton12 = onView(
                    allOf(withId(android.R.id.button1), withText("OK"),
                            childAtPosition(
                                    childAtPosition(
                                            withClassName(is("android.widget.ScrollView")),
                                            0),
                                    3)));
            appCompatButton12.perform(scrollTo(), click());


            // we set the start time
            ViewInteraction appCompatEditText6 = onView(
                    allOf(withId(R.id.start_time_edit_text),
                            childAtPosition(
                                    allOf(withId(R.id.election_setup_time),
                                            childAtPosition(
                                                    withId(R.id.election_setup_fields_ll),
                                                    4)),
                                    0)));
            appCompatEditText6.perform(scrollTo(), click());
            onView(withClassName(Matchers.equalTo(TimePicker.class.getName()))).perform(PickerActions.setTime(startHour, startMinute));

            ViewInteraction appCompatButton13 = onView(
                    allOf(withId(android.R.id.button1), withText("OK"),
                            childAtPosition(
                                    childAtPosition(
                                            withClassName(is("android.widget.ScrollView")),
                                            0),
                                    3)));
            appCompatButton13.perform(scrollTo(), click());

            // set the end time
            ViewInteraction appCompatEditText7 = onView(
                    allOf(withId(R.id.end_time_edit_text),
                            childAtPosition(
                                    allOf(withId(R.id.election_setup_time),
                                            childAtPosition(
                                                    withId(R.id.election_setup_fields_ll),
                                                    4)),
                                    1)));
            appCompatEditText7.perform(scrollTo(), click());
            onView(withClassName(Matchers.equalTo(TimePicker.class.getName()))).perform(PickerActions.setTime(endHour, endMinute));

            ViewInteraction appCompatButton14 = onView(
                    allOf(withId(android.R.id.button1), withText("OK"),
                            childAtPosition(
                                    childAtPosition(
                                            withClassName(is("android.widget.ScrollView")),
                                            0),
                                    3)));
            appCompatButton14.perform(scrollTo(), click());

            ViewInteraction appCompatEditText8 = onView(
                    allOf(withId(R.id.election_question),
                            childAtPosition(
                                    allOf(withId(R.id.election_setup_question_ll),
                                            childAtPosition(
                                                    withId(R.id.election_setup_fields_ll),
                                                    5)),
                                    0)));
            appCompatEditText8.perform(scrollTo(), replaceText("election question test"), closeSoftKeyboard());

            ViewInteraction appCompatEditText9 = onView(
                    allOf(withId(R.id.new_ballot_option_text),
                            childAtPosition(
                                    allOf(withId(R.id.new_ballot_option_ll),
                                            childAtPosition(
                                                    withId(R.id.election_setup_ballot_options_ll),
                                                    0)),
                                    0)));
            appCompatEditText9.perform(scrollTo(), replaceText("new ballot test 1"), closeSoftKeyboard());

            ViewInteraction appCompatEditText10 = onView(
                    allOf(withId(R.id.new_ballot_option_text),
                            childAtPosition(
                                    allOf(withId(R.id.new_ballot_option_ll),
                                            childAtPosition(
                                                    withId(R.id.election_setup_ballot_options_ll),
                                                    1)),
                                    0)));
            appCompatEditText10.perform(scrollTo(), replaceText("new ballot test 2"), closeSoftKeyboard());


            // We do the corresponding checks for the attributes we have fill in here
            ViewInteraction editText2 = onView(
                    allOf(withId(R.id.election_setup_name),
                            withParent(allOf(withId(R.id.election_setup_fields_ll),
                                    withParent(withId(R.id.election_setup_scrollview)))),
                            isDisplayed()));

            // we check the election name is right
            editText2.check(matches(withText("new election test")));

            // We create the start date/time string
            Date dateStart = new Date(startYear - 1900, startMonth, startDay, startHour, startMinute);
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            DateFormat timeFormat = new SimpleDateFormat("HH:mm");
            String strDateStart = dateFormat.format(dateStart);
            String strTimeStart = timeFormat.format(dateStart);

            System.out.println(strDateStart);
            System.out.println(strTimeStart);
            ViewInteraction editText3 = onView(
                    allOf(withId(R.id.start_date_edit_text), withText(strDateStart),
                            withParent(allOf(withId(R.id.election_setup_date),
                                    withParent(withId(R.id.election_setup_fields_ll)))),
                            isDisplayed()));
            // we check the start date is right
            editText3.check(matches(withText(strDateStart)));

            // We create the end date/time string
            Date dateEnd = new Date(endYear - 1900, endMonth - 1, endDay, endHour, endMinute);
            String strDateEnd = dateFormat.format(dateEnd);
            String strTimeEnd = timeFormat.format(dateEnd);
            // we check the end date is right
            ViewInteraction editText4 = onView(
                    allOf(withId(R.id.end_date_edit_text), withText(strDateEnd),
                            withParent(allOf(withId(R.id.election_setup_date),
                                    withParent(withId(R.id.election_setup_fields_ll)))),
                            isDisplayed()));
            editText4.check(matches(withText(strDateEnd)));


            // we check the start time is right
            ViewInteraction editText8 = onView(
                    allOf(withId(R.id.start_time_edit_text), withText(strTimeStart),
                            withParent(allOf(withId(R.id.election_setup_time),
                                    withParent(withId(R.id.election_setup_fields_ll)))),
                            isDisplayed()));
            editText8.check(matches(withText(strTimeStart)));

            // we check the end time is right
            ViewInteraction editText9 = onView(
                    allOf(withId(R.id.end_time_edit_text), withText(strTimeEnd),
                            withParent(allOf(withId(R.id.election_setup_time),
                                    withParent(withId(R.id.election_setup_fields_ll)))),
                            isDisplayed()));
            editText9.check(matches(withText(strTimeEnd)));

            //we check the election question is right
            ViewInteraction editText5 = onView(
                    allOf(withId(R.id.election_question),
                            withParent(allOf(withId(R.id.election_setup_question_ll),
                                    withParent(withId(R.id.election_setup_fields_ll)))),
                            isDisplayed()));
            editText5.check(matches(withText("election question test")));

            ViewInteraction editText6 = onView(
                    allOf(withId(R.id.new_ballot_option_text), withText("new ballot test 1"),
                            withParent(allOf(withId(R.id.new_ballot_option_ll),
                                    withParent(withId(R.id.election_setup_ballot_options_ll)))),
                            isDisplayed()));

            //we check the ballot option 1
            editText6.check(matches(withText("new ballot test 1")));

            ViewInteraction editText7 = onView(
                    allOf(withId(R.id.new_ballot_option_text), withText("new ballot test 2"),
                            withParent(allOf(withId(R.id.new_ballot_option_ll),
                                    withParent(withId(R.id.election_setup_ballot_options_ll)))),
                            isDisplayed()));
            //we check the ballot option 2
            editText7.check(matches(withText("new ballot test 2")));

            ViewInteraction button2 = onView(
                    allOf(withId(R.id.election_submit_button), withText("Submit"),
                            withParent(allOf(withId(R.id.election_setup_submit_cancel_ll),
                                    withParent(withId(R.id.fragment_setup_election_event)))),
                            isDisplayed()));
            button2.check(matches(isDisplayed()));
            button2.check(matches(isClickable()));

            ViewInteraction button3 = onView(
                    allOf(withId(R.id.election_cancel_button), withText("Cancel"),
                            withParent(allOf(withId(R.id.election_setup_submit_cancel_ll),
                                    withParent(withId(R.id.fragment_setup_election_event)))),
                            isDisplayed()));
            button3.check(matches(isDisplayed()));
            button3.check(matches(isClickable()));

            ViewInteraction button4 = onView(
                    allOf(withId(R.id.tab_home), withText("HOME"),
                            withParent(allOf(withId(R.id.tab_home_only),
                                    withParent(withId(R.id.fragment_container_lao_detail)))),
                            isDisplayed()));
            button4.check(matches(isDisplayed()));
            button4.check(matches(isClickable()));

            ViewInteraction button5 = onView(
                    allOf(withId(R.id.tab_identity), withText("IDENTITY"),
                            withParent(allOf(withId(R.id.tab_home_identity),
                                    withParent(withId(R.id.fragment_container_lao_detail)))),
                            isDisplayed()));
            button5.check(matches(isDisplayed()));
            button5.check(matches(isClickable()));

            ViewInteraction appCompatButton15 = onView(
                    allOf(withId(R.id.election_submit_button), withText("Submit"),
                            childAtPosition(
                                    allOf(withId(R.id.election_setup_submit_cancel_ll),
                                            childAtPosition(
                                                    withId(R.id.fragment_setup_election_event),
                                                    2)),
                                    1),
                            isDisplayed()));
            appCompatButton15.perform(click());

            // we check the election created has the right display text
            ViewInteraction textView2 = onView(
                    allOf(withId(R.id.election_title), withText("Election : new election test"),
                            withParent(allOf(withId(R.id.election_layout),
                                    withParent(withId(R.id.include_layout_election)))),
                            isDisplayed()));
            textView2.check(matches(withText("Election : new election test")));

            SimpleDateFormat dateFormat1 =
                    new SimpleDateFormat("dd/MM/yyyy HH:mm ", Locale.ENGLISH);

            ViewInteraction textView3 = onView(
                    allOf(withId(R.id.election_start_date), withText("Start date : " + dateFormat1.format(dateStart)),
                            withParent(allOf(withId(R.id.election_layout),
                                    withParent(withId(R.id.include_layout_election)))),
                            isDisplayed()));
            textView3.check(matches(withText("Start date : " + dateFormat1.format(dateStart))));

            ViewInteraction textView4 = onView(
                    allOf(withId(R.id.election_end_date), withText("End Date : " + dateFormat1.format(dateEnd)),
                            withParent(allOf(withId(R.id.election_layout),
                                    withParent(withId(R.id.include_layout_election)))),
                            isDisplayed()));
            textView4.check(matches(withText("End Date : " + dateFormat1.format(dateEnd))));

            ViewInteraction button6 = onView(
                    allOf(withId(R.id.election_action_button), withText("CAST VOTE "),
                            withParent(allOf(withId(R.id.include_layout_election),
                                    withParent(withId(R.id.event_layout)))),
                            isDisplayed()));
            button6.check(matches(isDisplayed()));
            button6.check(matches(isClickable()));
        }


    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }

        };
    }
}
