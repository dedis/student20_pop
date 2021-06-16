package com.github.dedis.student20_pop.home.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.github.dedis.student20_pop.databinding.FragmentSeedWalletBinding;
import com.github.dedis.student20_pop.home.HomeActivity;
import com.github.dedis.student20_pop.home.HomeViewModel;
import com.github.dedis.student20_pop.model.Wallet;
import java.util.StringJoiner;

/** Fragment used to display the new seed UI */
public class SeedWalletFragment extends Fragment {
  public static final String TAG = SeedWalletFragment.class.getSimpleName();
  private FragmentSeedWalletBinding mSeedWalletFragBinding;
  private HomeViewModel mHomeViewModel;
  private Wallet wallet;
  public static SeedWalletFragment newInstance() {
    return new SeedWalletFragment();
  }
  private AlertDialog seedAlert;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    wallet = Wallet.getInstance();

    mSeedWalletFragBinding = FragmentSeedWalletBinding.inflate(inflater, container, false);

    FragmentActivity activity = getActivity();
    if (activity instanceof HomeActivity) {
      mHomeViewModel = HomeActivity.obtainViewModel(activity);
    } else {
      throw new IllegalArgumentException("Cannot obtain view model for " + TAG);
    }

    mSeedWalletFragBinding.setViewModel(mHomeViewModel);
    mSeedWalletFragBinding.setLifecycleOwner(activity);

    return mSeedWalletFragBinding.getRoot();
  }
  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setupDisplaySeed();
    setupConfirmSeedButton();
  }

  private void setupDisplaySeed() {
    String[] exportSeed = new String[0];
    String err = "Error import key, try again";
    try {
      exportSeed = wallet.exportSeed();
    } catch (Exception e) {
      Toast.makeText(getContext().getApplicationContext(),
          err,
          Toast.LENGTH_LONG).show();
      Log.d(TAG, e.getMessage());
    }
    if(exportSeed != null && exportSeed.length > 0) {
      StringJoiner joiner = new StringJoiner(" ");
      for (String i : exportSeed)
        joiner.add(i);
      mSeedWalletFragBinding.seedWallet.setText(joiner.toString());
    } else{
      Toast.makeText(getContext().getApplicationContext(),
          err,
          Toast.LENGTH_LONG).show();
    }
  }

  private void setupConfirmSeedButton() {
    mSeedWalletFragBinding.buttonConfirmSeed.setOnClickListener(v -> {
      if(seedAlert!=null && seedAlert.isShowing()) {
        seedAlert.dismiss();
      }
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setTitle("You are sure you have saved the words somewhere?");
      builder.setPositiveButton("Yes", (dialog, which)-> {
        if(!mHomeViewModel.importSeed(mSeedWalletFragBinding.seedWallet.getText().toString())) {
          Toast.makeText(getContext().getApplicationContext(),
              "Error import key, try again",
              Toast.LENGTH_LONG).show();
          }
        }
      );
      builder.setNegativeButton("Cancel",(dialog, which)-> dialog.cancel());
      seedAlert = builder.create();
      seedAlert.show();
    });
  }

}