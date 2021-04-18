package com.github.dedis.student20_pop.qrcode;

/** Class handling connect fragment tests */
public class QRCodeScanningFragmentTest {

  //  private static final String TEST_URL = "Test url";
  //
  //  @Rule
  //  public final GrantPermissionRule rule = GrantPermissionRule.grant(Manifest.permission.CAMERA);
  //
  //  @Test
  //  @Ignore("No matching view exception")
  //  public void testSimpleBarcodeReaction() {
  //    ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
  //
  //    // Set good fragment
  //    onView(ViewMatchers.withId(R.id.tab_connect)).perform(click());
  //    onView(withId(R.id.fragment_qrcode)).check(matches(isDisplayed()));
  //
  //    // Simulate a detected url
  //    scenario.onActivity(
  //        a -> {
  //          Fragment fragment =
  //              a.getSupportFragmentManager().findFragmentByTag(QRCodeScanningFragment.TAG);
  //          Assert.assertNotNull(fragment);
  //          Assert.assertTrue(fragment instanceof QRCodeScanningFragment);
  //          ((QRCodeScanningFragment) fragment).onQRCodeDetected(TEST_URL, CONNECT_LAO, null);
  //        });
  //
  //    // Check everything
  //    onView(withId(R.id.fragment_connecting)).check(matches(isDisplayed()));
  //    onView(withId(R.id.connecting_url)).check(matches(withText(TEST_URL)));
  //  }
}
