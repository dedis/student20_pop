package com.github.dedis.student20_pop.detail;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import com.github.dedis.student20_pop.Injection;
import com.github.dedis.student20_pop.R;
import com.github.dedis.student20_pop.ViewModelFactory;
import com.github.dedis.student20_pop.detail.fragments.RollCallEventCreationFragment;
import com.github.dedis.student20_pop.home.HomeActivity;
import com.github.dedis.student20_pop.home.HomeViewModel;
import com.github.dedis.student20_pop.home.fragments.IdentityFragment;
import com.github.dedis.student20_pop.model.event.EventType;
import com.github.dedis.student20_pop.ui.LaoDetailFragment;
import com.github.dedis.student20_pop.ui.event.creation.MeetingEventCreationFragment;
import com.github.dedis.student20_pop.ui.event.creation.PollEventCreationFragment;
import com.github.dedis.student20_pop.ui.qrcode.CameraPermissionFragment;
import com.github.dedis.student20_pop.ui.qrcode.QRCodeScanningFragment;
import com.github.dedis.student20_pop.utility.ActivityUtils;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import java.util.Objects;
public class LaoDetailActivity extends AppCompatActivity {
  private static final String TAG = LaoDetailActivity.class.getSimpleName();
  private LaoDetailViewModel mViewModel;
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_lao_detail);
    mViewModel = obtainViewModel(this);
    mViewModel.subscribeToLao(
            (String) Objects.requireNonNull(getIntent().getExtras()).get("LAO_ID"));
    setupLaoFragment();
    setupHomeButton();
    setupIdentityButton();
    // Subscribe to "open lao detail event"
    mViewModel
            .getOpenLaoDetailEvent()
            .observe(
                    this,
                    booleanEvent -> {
                      Boolean event = booleanEvent.getContentIfNotHandled();
                      if (event != null) {
                        setupLaoFragment();
                      }
                    });
    // Subscribe to "open home" event
    mViewModel
            .getOpenHomeEvent()
            .observe(
                    this,
                    booleanEvent -> {
                      Boolean event = booleanEvent.getContentIfNotHandled();
                      if (event != null) {
                        setupHomeActivity();
                      }
                    });
    // Subscribe to "open identity" event
    mViewModel
            .getOpenIdentityEvent()
            .observe(
                    this,
                    booleanEvent -> {
                      String pk = booleanEvent.getContentIfNotHandled();
                      if (pk != null) {
                        setupIdentityFragment(pk);
                      }
                    });
    // Subscribe to "new lao event" event
    mViewModel
            .getNewLaoEventEvent()
            .observe(
                    this,
                    eventEvent -> {
                      EventType eventType = eventEvent.getContentIfNotHandled();
                      if (eventType != null) {
                        handleNewEvent(eventType);
                      }
                    });
    mViewModel
            .getOpenConnectRollCallEvent()
            .observe(
                    this,
                    stringEvent -> {
                      String action = stringEvent.getContentIfNotHandled();
                      if (action != null) {
                        switch (action) {
                          case HomeViewModel.SCAN:
                            Log.d("ROLL CALL", "scan2");
                            setupScanFragmentRollCall();
                            break;
                          case HomeViewModel.REQUEST_CAMERA_PERMISSION:
                            Log.d("ROLL CALL", "permission2");
                            setupCameraPermissionFragmentRollCall();
                            break;
                        }
                      }
                    });
  }
  public void handleNewEvent(EventType eventType) {
    Log.d(TAG, "event type: " + eventType.toString());
    switch (eventType) {
      case MEETING:
        setupCreateMeetingFragment();
        break;
      case ROLL_CALL:
        setupCreateRollCallFragment();
        break;
      case POLL:
        setupCreatePollFragment();
        break;
      default:
        Log.d(TAG, "unknown event type: " + eventType.toString());
    }
  }
  public static LaoDetailViewModel obtainViewModel(FragmentActivity activity) {
    ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());
    LaoDetailViewModel viewModel =
            new ViewModelProvider(activity, factory).get(LaoDetailViewModel.class);
    return viewModel;
  }
  public void setupHomeButton() {
    Button homeButton = (Button) findViewById(R.id.tab_home);
    homeButton.setOnClickListener(v -> mViewModel.openHome());
  }
  public void setupIdentityButton() {
    Button identityButton = (Button) findViewById(R.id.tab_identity);
    identityButton.setOnClickListener(v -> mViewModel.openIdentity());
  }
  private void setupLaoFragment() {
    LaoDetailFragment laoDetailFragment =
            (LaoDetailFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_lao_detail);
    if (laoDetailFragment == null) {
      laoDetailFragment = LaoDetailFragment.newInstance();
      ActivityUtils.replaceFragmentInActivity(
              getSupportFragmentManager(), laoDetailFragment, R.id.fragment_container_lao_detail);
    }
  }
  private void setupHomeActivity() {
    Intent intent = new Intent(this, HomeActivity.class);
    setResult(HomeActivity.LAO_DETAIL_REQUEST_CODE, intent);
    finish();
  }
  private void setupIdentityFragment(String pk) {
    IdentityFragment identityFragment =
            (IdentityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_identity);
    if (identityFragment == null) {
      identityFragment = IdentityFragment.newInstance(pk);
      ActivityUtils.replaceFragmentInActivity(
              getSupportFragmentManager(), identityFragment, R.id.fragment_container_lao_detail);
    }
  }
  private void setupCreateMeetingFragment() {
    MeetingEventCreationFragment meetingCreationFragment =
            (MeetingEventCreationFragment)
                    getSupportFragmentManager().findFragmentById(R.id.fragment_meeting_event_creation);
    if (meetingCreationFragment == null) {
      meetingCreationFragment = MeetingEventCreationFragment.newInstance();
      ActivityUtils.replaceFragmentInActivity(
              getSupportFragmentManager(), meetingCreationFragment, R.id.fragment_container_lao_detail);
    }
  }
  private void setupCreateRollCallFragment() {
    RollCallEventCreationFragment rollCallCreationFragment =
            (RollCallEventCreationFragment)
                    getSupportFragmentManager().findFragmentById(R.id.fragment_create_roll_call_event);
    if (rollCallCreationFragment == null) {
      rollCallCreationFragment = RollCallEventCreationFragment.newInstance();
      ActivityUtils.replaceFragmentInActivity(
              getSupportFragmentManager(),
              rollCallCreationFragment,
              R.id.fragment_container_lao_detail);
    }
  }
  private void setupCreatePollFragment() {
    PollEventCreationFragment pollCreationFragment =
            (PollEventCreationFragment)
                    getSupportFragmentManager().findFragmentById(R.id.fragment_organizer_poll);
    if (pollCreationFragment == null) {
      pollCreationFragment = PollEventCreationFragment.newInstance();
      ActivityUtils.replaceFragmentInActivity(
              getSupportFragmentManager(), pollCreationFragment, R.id.fragment_container_lao_detail);
    }
  }
  private void setupScanFragmentRollCall() {
    /*QRCodeScanningFragment scanningFragment =
            (QRCodeScanningFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_qrcode);*/
    QRCodeScanningFragment scanningFragment =
            (QRCodeScanningFragment) getSupportFragmentManager().findFragmentById(R.id.add_attendee_layout);

    if (scanningFragment == null) {
      Context context = getApplicationContext();
      BarcodeDetector qrCodeDetector = Injection.provideQRCodeDetector(context);
      int width = getResources().getInteger(R.integer.camera_preview_width);
      int height = getResources().getInteger(R.integer.camera_preview_height);
      scanningFragment =
              QRCodeScanningFragment.newInstance(
                      Injection.provideCameraSource(context, qrCodeDetector, width, height),
                      qrCodeDetector);
      ActivityUtils.replaceFragmentInActivity(
              getSupportFragmentManager(), scanningFragment, R.id.fragment_container_lao_detail);
    }
  }
  private void setupCameraPermissionFragmentRollCall() {
    CameraPermissionFragment cameraPermissionFragment =
            (CameraPermissionFragment)
                    getSupportFragmentManager().findFragmentById(R.id.fragment_camera_perm);
    if (cameraPermissionFragment == null) {
      cameraPermissionFragment = CameraPermissionFragment.newInstance();
      ActivityUtils.replaceFragmentInActivity(
              getSupportFragmentManager(), cameraPermissionFragment, R.id.fragment_container_lao_detail);
    }
  }
}