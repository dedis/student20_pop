package com.github.dedis.student20_pop.detail.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.github.dedis.student20_pop.R;
import java.util.LinkedList;
import java.util.List;

public class ChoicesListViewAdapter extends BaseAdapter {
  private final Context context;
  private final List<String> choices;
  private final TextWatcher choicesTextWatcher;

  public ChoicesListViewAdapter(Context context, TextWatcher textWatcher) {
    this.context = context;
    this.choices = new LinkedList<>();
    this.choices.add("");
    this.choicesTextWatcher = textWatcher;
  }

  @Override
  public int getCount() {
    return choices.size();
  }

  @Override
  public Object getItem(int position) {
    return choices.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
      LayoutInflater inflater =
          (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = inflater.inflate(R.layout.layout_poll_choices, null);
    }
    ((TextView) convertView.findViewById(R.id.choice_number)).setText(String.valueOf(position + 1));
    EditText choice = convertView.findViewById(R.id.choice_edit_text);
    choice.setText(choices.get(position));
    choice.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            choices.set(position, s.toString());
          }

          @Override
          public void afterTextChanged(Editable s) {
            choices.set(position, s.toString());
          }
        });
    choice.addTextChangedListener(choicesTextWatcher);

    Button button = convertView.findViewById(R.id.delete_choice_button);
    if (position == 0) {
      button.setVisibility(View.INVISIBLE);
    }
    View finalConvertView = convertView;
    button.setOnClickListener(
        clicked -> {
          choice.getText().clear();
          if (position < getCount() - 1) {
            choice.setText((String) getItem(position + 1));
          }
          removeChoice(position);
          justifyHeight(finalConvertView, parent);
        });
    return convertView;
  }

  public void addChoice() {
    this.choices.add("");
    this.notifyDataSetChanged();
  }

  public void removeChoice(int position) {
    if (position > 0) {
      this.choices.remove(position);
      this.notifyDataSetChanged();
    }
  }

  public void justifyHeight(View item, ViewGroup parent) {
    int totalHeight = 0;
    int count = getCount();
    for (int i = 0; i < count; i++) {
      item.measure(0, 0);
      totalHeight += item.getMeasuredHeight();
    }
    ListView lv = (ListView) parent;
    ViewGroup.LayoutParams par = parent.getLayoutParams();
    par.height = totalHeight + (lv.getDividerHeight() * (count - 1));
    lv.setLayoutParams(par);
    lv.requestLayout();
  }
}
