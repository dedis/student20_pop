package com.github.dedis.student20_pop.detail.fragments.event.creation;

public class RollCallEventCreationFragmentTest {
  //  private final int YEAR = 2022;
  //  private final int MONTH_OF_YEAR = 10;
  //  private final int DAY_OF_MONTH = 10;
  //  private final String DATE = "" + DAY_OF_MONTH + "/" + MONTH_OF_YEAR + "/" + YEAR;
  //  private final int HOURS = 12;
  //  private final int MINUTES = 15;
  //  private final String TIME = "" + HOURS + ":" + MINUTES;
  //
  //  @Rule
  //  public ActivityScenarioRule<OrganizerActivity> activityScenarioRule =
  //      new ActivityScenarioRule<>(OrganizerActivity.class);
  //
  //  private View decorView;
  //
  //  @Before
  //  public void setUp() {
  //    activityScenarioRule
  //        .getScenario()
  //        .onActivity(
  //            new ActivityScenario.ActivityAction<OrganizerActivity>() {
  //
  //              /**
  //               * This method is invoked on the main thread with the reference to the Activity.
  //               *
  //               * @param activity an Activity instrumented by the {@link ActivityScenario}. It
  // never
  //               *     be null.
  //               */
  //              @Override
  //              public void perform(OrganizerActivity activity) {
  //                decorView = activity.getWindow().getDecorView();
  //              }
  //            });
  //
  //    onView(
  //            allOf(
  //                withId(R.id.add_future_event_button),
  //                withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
  //        .perform(click());
  //
  // onView(withText(getApplicationContext().getString(R.string.roll_call_event))).perform(click());
  //  }
  //
  //  @Test
  //  public void canLaunchEventMeetingFragment() {
  //    onView(withId(R.id.fragment_create_roll_call_event)).check(matches(isDisplayed()));
  //  }
  //
  //  @Test
  //  public void confirmButtonIsDisabled() {
  //    onView(withId(R.id.roll_call_confirm)).check(matches(not(isEnabled())));
  //  }
  //
  //  @Test
  //  public void cancelButtonWorks() {
  //    onView(withId(R.id.roll_call_cancel)).perform(click());
  //    onView(withId(R.id.fragment_organizer)).check(matches(isDisplayed()));
  //  }
  //
  //  @Test
  //  @Ignore("TODO: solve issue with GithubActions Emulator")
  //  public void confirmButtonIsEnabledWhenRequiredFieldsFilled() {
  //    onView(withId(R.id.roll_call_title_text)).perform(typeText("Random meeting title"));
  //
  //    onView(withId(R.id.start_date_edit_text)).perform(click());
  //    onView(withClassName(Matchers.equalTo(DatePicker.class.getName())))
  //        .perform(PickerActions.setDate(YEAR, MONTH_OF_YEAR, DAY_OF_MONTH));
  //    onView(withId(android.R.id.button1)).perform(click());
  //    onView(withId(R.id.start_date_edit_text)).check(matches(withText(DATE)));
  //
  //    onView(withId(R.id.start_time_edit_text)).perform(click());
  //    onView(withClassName(Matchers.equalTo(TimePicker.class.getName())))
  //        .perform(PickerActions.setTime(HOURS, MINUTES));
  //    onView(withId(android.R.id.button1)).perform(click());
  //    onView(withId(R.id.start_time_edit_text)).check(matches(withText(TIME)));
  //
  //    onView(withId(R.id.roll_call_confirm)).check(matches(isEnabled()));
  //  }
  //
  //  @Test
  //  @Ignore("TODO: solve issue with GithubActions Emulator")
  //  public void confirmAddsEventToEventList() {
  //    final String RANDOM_EVENT_TITLE = "Random roll call title";
  //    onView(withId(R.id.roll_call_title_text)).perform(typeText(RANDOM_EVENT_TITLE));
  //
  //    onView(withId(R.id.start_date_edit_text)).perform(click());
  //    onView(withClassName(Matchers.equalTo(DatePicker.class.getName())))
  //        .perform(PickerActions.setDate(YEAR, MONTH_OF_YEAR, DAY_OF_MONTH));
  //    onView(withId(android.R.id.button1)).perform(click());
  //    onView(withId(R.id.start_date_edit_text)).check(matches(withText(DATE)));
  //
  //    onView(withId(R.id.start_time_edit_text)).perform(click());
  //    onView(withClassName(Matchers.equalTo(TimePicker.class.getName())))
  //        .perform(PickerActions.setTime(HOURS, MINUTES));
  //    onView(withId(android.R.id.button1)).perform(click());
  //    onView(withId(R.id.start_time_edit_text)).check(matches(withText(TIME)));
  //
  //    onView(withId(R.id.roll_call_confirm)).check(matches(isEnabled()));
  //    onView(withId(R.id.roll_call_confirm)).perform(click());
  //
  //    activityScenarioRule
  //        .getScenario()
  //        .onActivity(
  //            activity -> {
  //              PoPApplication app = (PoPApplication) activity.getApplication();
  //              List<Event> events =
  //                  app.getCurrentLao().map(Lao::getEvents).orElse(new ArrayList<>());
  //              List<String> eventsName =
  //                  events.stream().map(Event::getName).collect(Collectors.toList());
  //              Assert.assertThat(RANDOM_EVENT_TITLE, isIn(eventsName));
  //            });
  //  }
}
