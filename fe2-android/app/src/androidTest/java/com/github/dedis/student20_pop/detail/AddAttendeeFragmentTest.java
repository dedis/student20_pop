package com.github.dedis.student20_pop.detail;

public class AddAttendeeFragmentTest {

  //  @Rule
  //  public final GrantPermissionRule rule = GrantPermissionRule.grant(Manifest.permission.CAMERA);
  //
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
  //  }
  //
  //  @Test
  //  @Ignore("TODO: solve issue with GithubActions Emulator")
  //  public void canAddAttendee() {
  //    onView(
  //        allOf(
  //            withId(R.id.add_future_event_button),
  //            withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
  //        .perform(click());
  //
  // onView(withText(getApplicationContext().getString(R.string.roll_call_event))).perform(click());
  //
  //    onView(withId(R.id.roll_call_title_text)).perform(typeText("Random title"));
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
  //    onView(withId(R.id.roll_call_open)).perform(click());
  //
  //    activityScenarioRule
  //        .getScenario()
  //        .onActivity(
  //            a -> {
  //              Fragment fragment =
  //                  a.getSupportFragmentManager().findFragmentByTag(QRCodeScanningFragment.TAG);
  //              Assert.assertNotNull(fragment);
  //              Assert.assertTrue(fragment instanceof QRCodeScanningFragment);
  //
  ////              PoPApplication app = (PoPApplication) a.getApplication();
  ////              final String LAO_ID = app.getCurrentLaoUnsafe().getId();
  ////
  ////              RollCallEvent rollCallEvent =
  ////                  new RollCallEvent(
  ////                      "Random Name",
  ////                      Instant.now().getEpochSecond(),
  ////                      Instant.now().getEpochSecond(),
  ////                      LAO_ID,
  ////                      new ObservableArrayList<>(),
  ////                      "",
  ////                      "No description");
  ////
  ////              app.addEvent(rollCallEvent);
  ////
  ////              final String ATTENDEE_ID = "t9Ed+TEwDM0+u0ZLdS4ZB/Vrrnga0Lu2iMkAQtyFRrQ=";
  ////              final String TEST_IDS = ATTENDEE_ID + LAO_ID;
  ////
  ////              ((QRCodeScanningFragment) fragment)
  ////                  .onQRCodeDetected(TEST_IDS, ADD_ROLL_CALL_ATTENDEE, rollCallEvent.getId());
  ////
  ////              List<String> attendees =
  ////                  app.getCurrentLao()
  ////                      .map(Lao::getEvents)
  ////                      .flatMap(
  ////                          events ->
  ////                              events.parallelStream()
  ////                                  .filter(event ->
  // event.getId().equals(rollCallEvent.getId()))
  ////                                  .findFirst())
  ////                      .map(Event::getAttendees)
  ////                      .orElseThrow(Error::new);
  //
  ////              Assert.assertThat(ATTENDEE_ID, isIn(attendees));
  //            });
  //
  //    onView(withText(getApplicationContext().getString(R.string.add_attendee_successful)))
  //        .inRoot(withDecorView(not(decorView)))
  //        .check(matches(isDisplayed()));
  //  }
  //
  //  @Test
  //  @Ignore("TODO: solve issue with Toast not showing")
  //  public void addTwiceSameAttendeeShowsToast() {
  //    onView(
  //        allOf(
  //            withId(R.id.add_future_event_button),
  //            withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
  //        .perform(click());
  //
  // onView(withText(getApplicationContext().getString(R.string.roll_call_event))).perform(click());
  //
  //    onView(withId(R.id.roll_call_title_text)).perform(typeText("Random title"));
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
  //    onView(withId(R.id.roll_call_open)).perform(click());
  //
  //    activityScenarioRule
  //        .getScenario()
  //        .onActivity(
  //            a -> {
  //              Fragment fragment =
  //                  a.getSupportFragmentManager().findFragmentByTag(QRCodeScanningFragment.TAG);
  //              Assert.assertNotNull(fragment);
  //              Assert.assertTrue(fragment instanceof QRCodeScanningFragment);
  //
  ////              PoPApplication app = (PoPApplication) a.getApplication();
  ////              final String LAO_ID = app.getCurrentLaoUnsafe().getId();
  //
  ////              RollCallEvent rollCallEvent =
  ////                  new RollCallEvent(
  ////                      "Random Name",
  ////                      Instant.now().getEpochSecond(),
  ////                      Instant.now().getEpochSecond(),
  ////                      LAO_ID,
  ////                      new ObservableArrayList<>(),
  ////                      "",
  ////                      "No description");
  ////
  ////              app.addEvent(rollCallEvent);
  ////
  ////              final String ATTENDEE_ID = "t9Ed+TEwDM0+u0ZLdS4ZB/Vrrnga0Lu2iMkAQtyFRrQ=";
  ////              final String TEST_IDS = ATTENDEE_ID + LAO_ID;
  ////
  ////              rollCallEvent.addAttendee(ATTENDEE_ID);
  ////
  ////              List<String> attendees =
  ////                  app.getCurrentLao()
  ////                      .map(Lao::getEvents)
  ////                      .flatMap(
  ////                          events ->
  ////                              events.parallelStream()
  ////                                  .filter(event ->
  // event.getId().equals(rollCallEvent.getId()))
  ////                                  .findFirst())
  ////                      .map(Event::getAttendees)
  ////                      .orElseThrow(Error::new);
  ////
  ////              Assert.assertThat(ATTENDEE_ID, isIn(attendees));
  ////
  ////              ((QRCodeScanningFragment) fragment)
  ////                  .onQRCodeDetected(TEST_IDS, ADD_ROLL_CALL_ATTENDEE, rollCallEvent.getId());
  //            });
  //
  //    onView(withText(getApplicationContext().getString(R.string.add_attendee_already_exists)))
  //        .inRoot(withDecorView(not(decorView)))
  //        .check(matches(isDisplayed()));
  //  }
}
