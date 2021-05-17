package com.github.dedis.student20_pop.detail.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.dedis.student20_pop.databinding.FragmentRollCallBinding;
import com.github.dedis.student20_pop.detail.LaoDetailActivity;
import com.github.dedis.student20_pop.detail.LaoDetailViewModel;

import net.glxn.qrgen.android.QRCode;

public class RollCallDetailFragment extends Fragment {
    public static final String TAG = RollCallDetailFragment.class.getSimpleName();

    private FragmentRollCallBinding mRollCallFragBinding;
    private LaoDetailViewModel mLaoDetailViewModel;
    private String pk;

    public RollCallDetailFragment(String pk){
        super();
        this.pk = pk;
    }

    public static RollCallDetailFragment newInstance(String pk) {
        return new RollCallDetailFragment(pk);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        mRollCallFragBinding = FragmentRollCallBinding.inflate(inflater, container, false);

        Bitmap myBitmap = QRCode.from(pk).bitmap();
        mRollCallFragBinding.pkQrCode.setImageBitmap(myBitmap);

        mLaoDetailViewModel = LaoDetailActivity.obtainViewModel(getActivity());

        mRollCallFragBinding.setLifecycleOwner(getActivity());

        return mRollCallFragBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRollCallFragBinding.backButton.setOnClickListener(clicked -> mLaoDetailViewModel.openLaoDetail());
    }
}