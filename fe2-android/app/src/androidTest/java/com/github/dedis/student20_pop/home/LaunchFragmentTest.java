package com.github.dedis.student20_pop.home;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.github.dedis.student20_pop.R;
import com.github.dedis.student20_pop.model.Lao;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
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
public class LaunchFragmentTest {

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

    @Rule
    public ActivityTestRule<HomeActivity> mActivityTestRule = new ActivityTestRule<>(HomeActivity.class);

    @Test
    public void launchFragment() {
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
                        isDisplayed()));
        //We check that the name of the lao displayed is " new lao test"
        textView.check(matches(withText("new lao test")));

        DataInteraction appCompatButton16 = onData(
                allOf(is(instanceOf(Lao.class)), laoHasName("new lao test"))).inAdapterView(withId(R.id.lao_list));
        appCompatButton16.perform(click());

        // We check that we opened the launch fragment
        onView(withId(R.id.fragment_lao_detail)).check(matches(isDisplayed()));
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
