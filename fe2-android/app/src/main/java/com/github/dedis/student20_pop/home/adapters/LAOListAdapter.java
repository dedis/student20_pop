package com.github.dedis.student20_pop.home.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;
import com.github.dedis.student20_pop.databinding.LayoutLaoHomeBinding;
import com.github.dedis.student20_pop.home.HomeViewModel;
import com.github.dedis.student20_pop.home.listeners.LAOItemUserActionsListener;
import com.github.dedis.student20_pop.model.Lao;
import java.util.List;

public class LAOListAdapter extends BaseAdapter {

  private final HomeViewModel homeViewModel;

  private List<Lao> laos;

  private LifecycleOwner lifecycleOwner;


  public LAOListAdapter(List<Lao> laos, HomeViewModel homeViewModel, LifecycleOwner activity) {
    this.homeViewModel = homeViewModel;
    setList(laos);
    lifecycleOwner = activity;
  }

  public void replaceList(List<Lao> laos) {
    setList(laos);
  }

  private void setList(List<Lao> laos) {
    this.laos = laos;
    notifyDataSetChanged();
  }

  @Override
  public int getCount() {
    return laos != null ? laos.size() : 0;
  }

  @Override
  public Object getItem(int position) {
    return laos.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View view, ViewGroup viewGroup) {
    LayoutLaoHomeBinding binding;
    if (view == null) {
      // inflate
      LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

      binding = LayoutLaoHomeBinding.inflate(inflater, viewGroup, false);
    } else {
      binding = DataBindingUtil.getBinding(view);
    }

    LAOItemUserActionsListener userActionsListener =
            new LAOItemUserActionsListener() {
              @Override
              public void onLAOClicked(Lao lao) {
                homeViewModel.openLAO("/root/" + lao.getId());
              }
            };

    binding.setLao(laos.get(position));
    binding.setLifecycleOwner(lifecycleOwner);

    binding.setListener(userActionsListener);

    binding.executePendingBindings();

    return binding.getRoot();
  }
}
