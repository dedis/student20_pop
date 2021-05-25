package com.github.dedis.student20_pop.home.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.github.dedis.student20_pop.databinding.FragmentContentWalletBinding;
import com.github.dedis.student20_pop.home.HomeActivity;
import com.github.dedis.student20_pop.home.HomeViewModel;

/** Fragment used to display the content wallet UI */
public class ContentWalletFragment extends Fragment {
  public static final String TAG = ContentWalletFragment.class.getSimpleName();
  public static ContentWalletFragment newInstance() {
    return new ContentWalletFragment();
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    FragmentContentWalletBinding mContentWalletFragBinding = FragmentContentWalletBinding.inflate(inflater, container, false);

    FragmentActivity activity = getActivity();
    HomeViewModel mHomeViewModel;
    if (activity instanceof HomeActivity) {
      mHomeViewModel = HomeActivity.obtainViewModel(activity);
    } else {
      throw new IllegalArgumentException("Cannot obtain view model for " + TAG);
    }

    mContentWalletFragBinding.setViewModel(mHomeViewModel);
    mContentWalletFragBinding.setLifecycleOwner(activity);

    return mContentWalletFragBinding.getRoot();
  }
}
