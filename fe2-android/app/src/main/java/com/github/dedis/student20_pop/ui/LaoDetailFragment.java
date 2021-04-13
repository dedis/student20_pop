package com.github.dedis.student20_pop.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.dedis.student20_pop.R;
import com.github.dedis.student20_pop.databinding.FragmentLaoDetailBinding;
import com.github.dedis.student20_pop.detail.LaoDetailActivity;
import com.github.dedis.student20_pop.detail.LaoDetailViewModel;
import com.github.dedis.student20_pop.detail.adapters.EventExpandableListViewAdapter;
import com.github.dedis.student20_pop.detail.adapters.WitnessListViewAdapter;
import com.github.dedis.student20_pop.model.RollCall;
import com.github.dedis.student20_pop.model.event.Event;

import java.util.ArrayList;

/** Fragment used to display the LAO Detail UI */
public class LaoDetailFragment extends Fragment {

  public static final String TAG = LaoDetailFragment.class.getSimpleName();

  private FragmentLaoDetailBinding mLaoDetailFragBinding;
  private LaoDetailViewModel mLaoDetailViewModel;
  private WitnessListViewAdapter mWitnessListViewAdapter;
  private EventExpandableListViewAdapter mEventListViewEventAdapter;

  public LaoDetailFragment() {}

  public static LaoDetailFragment newInstance() {
    return new LaoDetailFragment();
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    mLaoDetailFragBinding = FragmentLaoDetailBinding.inflate(inflater, container, false);

    mLaoDetailViewModel = LaoDetailActivity.obtainViewModel(getActivity());

    mLaoDetailFragBinding.setViewModel(mLaoDetailViewModel);
    mLaoDetailFragBinding.setLifecycleOwner(getActivity());
    
    return mLaoDetailFragBinding.getRoot();
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    setupPropertiesButton();
    setupEditPropertiesButton();
    setupConfirmEditButton();
    setupCancelEditButton();

    setupEventListAdapter();
    setupEventListUpdates();
    setupWitnessListAdapter();
    setupWitnessListUpdates();

    // TODO: Add witness handler

    //    setupSwipeRefresh();

    // Subscribe to "show/hide properties" event
    mLaoDetailViewModel
        .getShowPropertiesEvent()
        .observe(
            this,
            booleanEvent -> {
              Boolean action = booleanEvent.getContentIfNotHandled();
              if (action != null) {
                showHideProperties(action);
              }
            });

    // Subscribe to "edit properties" event
    mLaoDetailViewModel
        .getEditPropertiesEvent()
        .observe(
            this,
            booleanEvent -> {
              Boolean action = booleanEvent.getContentIfNotHandled();
              if (action != null) {
                editProperties(action);
              }
            });
  }

  private void setupPropertiesButton() {
    Button propertiesButton = (Button) getActivity().findViewById(R.id.tab_properties);

    propertiesButton.setOnClickListener(clicked -> mLaoDetailViewModel.toggleShowHideProperties());
  }

  private void setupEditPropertiesButton() {
    mLaoDetailFragBinding.editButton.setOnClickListener(
        clicked -> {
          mLaoDetailViewModel.openEditProperties();
        });
  }

  private void setupConfirmEditButton() {
    mLaoDetailFragBinding.propertiesEditConfirm.setOnClickListener(
        clicked -> mLaoDetailViewModel.confirmEdit());
  }

  private void setupCancelEditButton() {
    mLaoDetailFragBinding.propertiesEditCancel.setOnClickListener(
        clicked -> {
          mLaoDetailViewModel.cancelEdit();
        });
  }

  private void setupWitnessListAdapter() {
    ListView listView = mLaoDetailFragBinding.witnessList;

    mWitnessListViewAdapter =
        new WitnessListViewAdapter(new ArrayList<>(), mLaoDetailViewModel, getActivity());

    listView.setAdapter(mWitnessListViewAdapter);
  }

  private void setupWitnessListUpdates() {
    mLaoDetailViewModel
        .getWitnesses()
        .observe(
            getActivity(),
            witnesses -> {
              Log.d(TAG, "witnesses updated");
              mWitnessListViewAdapter.replaceList(witnesses);
            });
  }

  private void setupEventListAdapter() {
    ExpandableListView expandableListView = mLaoDetailFragBinding.expListView;

    mEventListViewEventAdapter =
        new EventExpandableListViewAdapter(new ArrayList<>(), mLaoDetailViewModel, getActivity());
      Log.d(TAG, "created adapter");
    expandableListView.setAdapter(mEventListViewEventAdapter);
    expandableListView.expandGroup(0);
    expandableListView.expandGroup(1);
  }

  private void setupEventListUpdates() {

    mLaoDetailViewModel
            .getLaoEvents()
            .observe(
                getActivity(),
                events -> {
                  Log.d(TAG, "Got an event list update");
                  for(Event event : events){
                      Log.d(TAG, ((RollCall)event).getDescription());
                  }
                  mEventListViewEventAdapter.replaceList(events);
                }
            );
  }


  private void setupSwipeRefresh() {
    //    mLaoDetailFragBinding.swipeRefresh.setOnRefreshListener(
    //        () -> {
    //          mWitnessListViewAdapter.notifyDataSetChanged();
    //          mEventListViewEventAdapter.notifyDataSetChanged();
    //          if (getFragmentManager() != null) {
    //            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
    //          }
    //          mLaoDetailFragBinding.swipeRefresh.setRefreshing(false);
    //        });
  }

  private void showHideProperties(Boolean show) {
    mLaoDetailFragBinding.propertiesLinearLayout.setVisibility(show ? View.VISIBLE : View.GONE);
  }

  private void editProperties(Boolean edit) {
    mLaoDetailFragBinding.editPropertiesLinearLayout.setVisibility(edit ? View.VISIBLE : View.GONE);

    // Hide current LAO name and edit button while editing
    mLaoDetailFragBinding.editButton.setVisibility(edit ? View.GONE : View.VISIBLE);
    mLaoDetailFragBinding.organizationName.setVisibility(edit ? View.GONE : View.VISIBLE);
  }
}
