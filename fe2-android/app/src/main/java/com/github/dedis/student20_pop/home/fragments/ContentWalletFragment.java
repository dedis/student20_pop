package com.github.dedis.student20_pop.home.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.github.dedis.student20_pop.R;
import com.github.dedis.student20_pop.databinding.FragmentContentWalletBinding;
import com.github.dedis.student20_pop.home.HomeActivity;
import com.github.dedis.student20_pop.home.HomeViewModel;
import com.github.dedis.student20_pop.home.adapters.LAOListAdapter;
import com.github.dedis.student20_pop.model.Wallet;

import java.util.ArrayList;

/** Fragment used to display the content wallet UI */
public class ContentWalletFragment extends Fragment {
  public static final String TAG = ContentWalletFragment.class.getSimpleName();
  public static ContentWalletFragment newInstance() {
    return new ContentWalletFragment();
  }

  private FragmentContentWalletBinding mContentWalletBinding;
  private HomeViewModel mHomeViewModel;
  private LAOListAdapter mListAdapter;
  private AlertDialog logoutAlert;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    mContentWalletBinding = FragmentContentWalletBinding.inflate(inflater, container, false);

    mHomeViewModel = HomeActivity.obtainViewModel(getActivity());

    mContentWalletBinding.setViewmodel(mHomeViewModel);
    mContentWalletBinding.setLifecycleOwner(getActivity());

    return mContentWalletBinding.getRoot();
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    setupListAdapter();
    setupListUpdates();

    if(Wallet.getInstance().isSetUp()){
      mContentWalletBinding.logoutButton.setVisibility(View.VISIBLE);
      mContentWalletBinding.logoutButton.setOnClickListener(clicked -> {
        if(logoutAlert!=null && logoutAlert.isShowing()) {
          logoutAlert.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.logout_title);
        builder.setMessage(R.string.logout_message);
        builder.setPositiveButton(R.string.confirm, (dialog, which) ->
          mHomeViewModel.logoutWallet()
        );
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        logoutAlert = builder.create();
        logoutAlert.show();
      });
    }
  }

  private void setupListUpdates() {
    mHomeViewModel
            .getLAOs()
            .observe(
                getActivity(),
                laos -> {
                  Log.d(TAG, "Got a list update");

                  mListAdapter.replaceList(laos);

                  if (!laos.isEmpty()) {
                    mContentWalletBinding.welcomeScreen.setVisibility(View.GONE);
                    mContentWalletBinding.listScreen.setVisibility(View.VISIBLE);
                  }
                });
  }

  private void setupListAdapter() {
    ListView listView = mContentWalletBinding.laoList;

    mListAdapter = new LAOListAdapter(new ArrayList<>(0), mHomeViewModel, getActivity(), false);

    listView.setAdapter(mListAdapter);
  }
}
