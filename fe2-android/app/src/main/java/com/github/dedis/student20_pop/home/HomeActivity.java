package com.github.dedis.student20_pop.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import com.github.dedis.student20_pop.Injection;
import com.github.dedis.student20_pop.R;
import com.github.dedis.student20_pop.ViewModelFactory;
import com.github.dedis.student20_pop.detail.LaoDetailActivity;
import com.github.dedis.student20_pop.home.fragments.ConnectingFragment;
import com.github.dedis.student20_pop.home.fragments.ContentWalletFragment;
import com.github.dedis.student20_pop.home.fragments.HomeFragment;
import com.github.dedis.student20_pop.home.fragments.LaunchFragment;
import com.github.dedis.student20_pop.home.fragments.SeedWalletFragment;
import com.github.dedis.student20_pop.home.fragments.WalletFragment;
import com.github.dedis.student20_pop.qrcode.CameraPermissionFragment;
import com.github.dedis.student20_pop.qrcode.QRCodeScanningFragment;
import com.github.dedis.student20_pop.utility.ActivityUtils;
import com.google.android.gms.vision.barcode.BarcodeDetector;

/** HomeActivity represents the entry point for the application. */
public class HomeActivity extends AppCompatActivity {

  private final String TAG = HomeActivity.class.getSimpleName();
  public static final int LAO_DETAIL_REQUEST_CODE = 0;

  private HomeViewModel mViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);

    setupHomeFragment();

    mViewModel = obtainViewModel(this);

    setupHomeButton();
    setupLaunchButton();
    setupConnectButton();
    setupWalletButton();

    // Subscribe to "open lao" event
    mViewModel.getOpenLaoEvent()
        .observe(
            this,
            stringEvent -> {
              String laoId = stringEvent.getContentIfNotHandled();
              if (laoId != null) {
                openLaoDetails(laoId);
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
                setupHomeFragment();
              }
            });

    // Subscribe to "open connecting" event
    mViewModel
        .getOpenConnectingEvent()
        .observe(
            this,
            booleanEvent -> {
              Boolean event = booleanEvent.getContentIfNotHandled();
              if (event != null) {
                setupConnectingFragment();
              }
            });

    // Subscribe to "open connect" event
    mViewModel
        .getOpenConnectEvent()
        .observe(
            this,
            stringEvent -> {
              String action = stringEvent.getContentIfNotHandled();
              if (action != null) {
                switch (action) {
                  case HomeViewModel.SCAN:
                    setupScanFragment();
                    break;
                  case HomeViewModel.REQUEST_CAMERA_PERMISSION:
                    setupCameraPermissionFragment();
                    break;
                }
              }
            });

    // Subscribe to "open launch" event
    mViewModel
        .getOpenLaunchEvent()
        .observe(
            this,
            booleanEvent -> {
              Boolean event = booleanEvent.getContentIfNotHandled();
              if (event != null) {
                setupLaunchFragment();
              }
            });

    subscribeWalletEvents();
  }

  private void subscribeWalletEvents(){

    // Subscribe to "open Seed" event
    mViewModel
        .getOpenSeedEvent()
        .observe(
            this,
            booleanEvent -> {
              Boolean action = booleanEvent.getContentIfNotHandled();
              if (action != null) {
                setupSeedWalletFragment();
              }
            });

    // Subscribe to "open wallet" event
    mViewModel
        .getOpenWalletEvent()
        .observe(
            this,
            booleanEvent -> {
              Boolean isSetUp = booleanEvent.getContentIfNotHandled();
              if (isSetUp != null) {
                if (isSetUp){
                  setupContentWalletFragment();
                }else{
                  setupWalletFragment();
                }
              }
            });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == LAO_DETAIL_REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        startActivity(new Intent(data));
      }
    }
  }

  public static HomeViewModel obtainViewModel(FragmentActivity activity) {
    ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());
    HomeViewModel viewModel = new ViewModelProvider(activity, factory).get(HomeViewModel.class);
    return viewModel;
  }

  public void setupHomeButton() {
    Button homeButton = (Button) findViewById(R.id.tab_home);
    homeButton.setOnClickListener(v -> mViewModel.openHome());
  }

  public void setupConnectButton() {
    Button connectButton = (Button) findViewById(R.id.tab_connect);
    connectButton.setOnClickListener(v -> {
      if(walletIsSetUp()){
        mViewModel.openConnect();
      }});
  }

  public void setupLaunchButton() {
    Button launchButton = (Button) findViewById(R.id.tab_launch);
    launchButton.setOnClickListener(v ->{
      if(walletIsSetUp()){
        mViewModel.openLaunch();
      }});
  }

  private boolean walletIsSetUp() {
    boolean isSetUp = mViewModel.isWalletSetUp();
    if(!isSetUp){
      setUpWalletMessage();
    }
    return isSetUp;
  }

  public void setUpWalletMessage(){
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("You have to setup up your wallet before connecting.");
    builder.setPositiveButton("Go to wallet", (dialog, which) -> mViewModel.openWallet());
    builder.show();
  }

  public void setupWalletButton() {
    Button launchButton = (Button) findViewById(R.id.tab_wallet);
    launchButton.setOnClickListener(v ->
        mViewModel.openWallet());
  }

  private void setupHomeFragment() {
    HomeFragment homeFragment =
        (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_home);
    if (homeFragment == null) {
      homeFragment = HomeFragment.newInstance();
      ActivityUtils.replaceFragmentInActivity(
          getSupportFragmentManager(), homeFragment, R.id.fragment_container_home);
    }
  }

  private void setupScanFragment() {
    QRCodeScanningFragment scanningFragment =
        (QRCodeScanningFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_qrcode);
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
          getSupportFragmentManager(), scanningFragment, R.id.fragment_container_home);
    }
  }

  private void setupCameraPermissionFragment() {
    CameraPermissionFragment cameraPermissionFragment =
        (CameraPermissionFragment)
            getSupportFragmentManager().findFragmentById(R.id.fragment_camera_perm);
    if (cameraPermissionFragment == null) {
      cameraPermissionFragment = CameraPermissionFragment.newInstance();
      ActivityUtils.replaceFragmentInActivity(
          getSupportFragmentManager(), cameraPermissionFragment, R.id.fragment_container_home);
    }
  }

  private void setupLaunchFragment() {
    LaunchFragment launchFragment =
        (LaunchFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_launch);
    if (launchFragment == null) {
      launchFragment = LaunchFragment.newInstance();
      ActivityUtils.replaceFragmentInActivity(
          getSupportFragmentManager(), launchFragment, R.id.fragment_container_home);
    }
  }

  private void setupConnectingFragment() {
    ConnectingFragment connectingFragment =
        (ConnectingFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_connecting);
    if (connectingFragment == null) {
      connectingFragment = ConnectingFragment.newInstance();
      ActivityUtils.replaceFragmentInActivity(
          getSupportFragmentManager(), connectingFragment, R.id.fragment_container_home);
    }
  }

  private void setupWalletFragment() {
    WalletFragment walletFragment =
        (WalletFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_wallet);
    if (walletFragment == null) {
      walletFragment = WalletFragment.newInstance();
      ActivityUtils.replaceFragmentInActivity(
          getSupportFragmentManager(), walletFragment, R.id.fragment_container_home);
    }
  }

  private void setupContentWalletFragment() {
    ContentWalletFragment contentWalletFragment =
        (ContentWalletFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_content_wallet);
    if (contentWalletFragment == null) {
      contentWalletFragment = ContentWalletFragment.newInstance();
      ActivityUtils.replaceFragmentInActivity(
          getSupportFragmentManager(), contentWalletFragment, R.id.fragment_container_home);
    }
  }

  private void setupSeedWalletFragment() {
    SeedWalletFragment seedWalletFragment =
        (SeedWalletFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_seed_wallet);
    if (seedWalletFragment == null) {
      seedWalletFragment = SeedWalletFragment.newInstance();
      ActivityUtils.replaceFragmentInActivity(
          getSupportFragmentManager(), seedWalletFragment, R.id.fragment_container_home);
    }
  }

  private void openLaoDetails(String laoId) {
    Intent intent = new Intent(this, LaoDetailActivity.class);
    Log.d(TAG, "Trying to open lao detail for lao with id " + laoId);
    intent.putExtra("LAO_ID", laoId);
    startActivityForResult(intent, LAO_DETAIL_REQUEST_CODE);
  }
}
