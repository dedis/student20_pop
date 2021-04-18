package com.github.dedis.student20_pop.detail;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import com.github.dedis.student20_pop.R;
import com.github.dedis.student20_pop.ViewModelFactory;
import com.github.dedis.student20_pop.detail.fragments.ElectionSetupFragment;
import com.github.dedis.student20_pop.detail.fragments.ManageElectionFragment;
import com.github.dedis.student20_pop.detail.fragments.event.creation.RollCallEventCreationFragment;
import com.github.dedis.student20_pop.home.HomeActivity;
import com.github.dedis.student20_pop.detail.fragments.IdentityFragment;
import com.github.dedis.student20_pop.model.event.EventType;
import com.github.dedis.student20_pop.detail.fragments.LaoDetailFragment;
import com.github.dedis.student20_pop.detail.fragments.event.creation.MeetingEventCreationFragment;
import com.github.dedis.student20_pop.detail.fragments.event.creation.PollEventCreationFragment;
import com.github.dedis.student20_pop.utility.ActivityUtils;
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
              Boolean event = booleanEvent.getContentIfNotHandled();
              if (event != null) {
                setupIdentityFragment();
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

//    // Subscribe to "open Cast Votes event" event
    mViewModel
            .getOpenCastVotes()
            .observe(
                    this,
                    booleanEvent -> {
                      Boolean event = booleanEvent.getContentIfNotHandled();
                      if(event!= null) {
                        setupCastVotesFragment();
                      }
                    });

    // Subscribe to "Election Display event" event
    mViewModel
            .getOpenElectionResultsEvent()
            .observe(
                    this,
                    booleanEvent -> {
                      Boolean event = booleanEvent.getContentIfNotHandled();
                      if(event!= null) {
                        setupElectionResultsFragment();
                      }
                    });

    // Subscribe to " Manage Election event" event
    mViewModel
            .getOpenManageElectionEvent()
            .observe(
                    this,
                    booleanEvent -> {
                      Boolean event = booleanEvent.getContentIfNotHandled();
                      if(event!= null) {
                        setupManageElectionFragment();
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
      case ELECTION:
        setupCreateElectionSetupFragment();
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

  private void setupIdentityFragment() {
    IdentityFragment identityFragment =
        (IdentityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_identity);
    if (identityFragment == null) {
      identityFragment = IdentityFragment.newInstance();
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

  private void setupCreateElectionSetupFragment() {
    ElectionSetupFragment electionSetupFragment =
            (ElectionSetupFragment)
              getSupportFragmentManager().findFragmentById(R.id.fragment_setup_election_event);
    if (electionSetupFragment == null) {
      electionSetupFragment = ElectionSetupFragment.newInstance();
      ActivityUtils.replaceFragmentInActivity(
              getSupportFragmentManager(), electionSetupFragment, R.id.fragment_container_lao_detail);
    }
  }
  private void setupManageElectionFragment() {

    ManageElectionFragment manageElectionFragment =
            (ManageElectionFragment)
                    getSupportFragmentManager().findFragmentById(R.id.fragment_manage_election);
    if (manageElectionFragment == null) {
     manageElectionFragment = ManageElectionFragment.newInstance();
      ActivityUtils.replaceFragmentInActivity(
              getSupportFragmentManager(), manageElectionFragment, R.id.fragment_container_lao_detail);
    }

  }
  //TODO : Implement those two methods
  private void setupCastVotesFragment() { }
  private void setupElectionResultsFragment() {
  }


}
