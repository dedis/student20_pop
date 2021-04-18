package com.github.dedis.student20_pop.detail.adapters;

import static com.github.dedis.student20_pop.model.event.EventCategory.FUTURE;
import static com.github.dedis.student20_pop.model.event.EventCategory.PAST;
import static com.github.dedis.student20_pop.model.event.EventCategory.PRESENT;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.github.dedis.student20_pop.databinding.LayoutElectionDisplayBinding;
import com.github.dedis.student20_pop.databinding.LayoutEventBinding;
import com.github.dedis.student20_pop.databinding.LayoutEventCategoryBinding;
import com.github.dedis.student20_pop.detail.LaoDetailViewModel;
import com.github.dedis.student20_pop.detail.listeners.AddEventListener;
import com.github.dedis.student20_pop.detail.listeners.OnEventCreatedListener;
import com.github.dedis.student20_pop.detail.listeners.OnEventTypeSelectedListener;
import com.github.dedis.student20_pop.model.Election;
import com.github.dedis.student20_pop.model.RollCall;
import com.github.dedis.student20_pop.model.event.Event;
import com.github.dedis.student20_pop.model.event.EventCategory;
import com.github.dedis.student20_pop.model.event.EventType;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class EventExpandableListViewAdapter extends BaseExpandableListAdapter {

  protected HashMap<EventCategory, List<Event>> eventsMap;
  private final EventCategory[] categories = EventCategory.values();
  protected static final SimpleDateFormat DATE_FORMAT =
      new SimpleDateFormat("dd/MM/yyyy HH:mm z", Locale.ENGLISH);
  private final LifecycleOwner lifecycleOwner;
  private final LaoDetailViewModel viewModel;

  /**
   * Constructor for the expandable list view adapter to display the events in the attendee UI
   *
   * @param events the list of events of the lao
   */
  public EventExpandableListViewAdapter(
      List<Event> events, LaoDetailViewModel viewModel, LifecycleOwner lifecycleOwner) {
    this.eventsMap = new HashMap<>();
    this.eventsMap.put(PAST, new ArrayList<>());
    this.eventsMap.put(PRESENT, new ArrayList<>());
    this.eventsMap.put(FUTURE, new ArrayList<>());
    this.lifecycleOwner = lifecycleOwner;
    this.viewModel = viewModel;

    putEventsInMap(events);
  }

  public void replaceList(List<Event> events) {
    setList(events);
  }

  private void setList(List<Event> events) {
    putEventsInMap(events);
    notifyDataSetChanged();
  }

  /** @return the amount of categories */
  @Override
  public int getGroupCount() {
    return this.categories.length;
  }

  /**
   * @param groupPosition
   * @return the amount of events in a given group
   */
  @Override
  public int getChildrenCount(int groupPosition) {
    if (groupPosition > getGroupCount()) {
      return 0;
    }
    return this.eventsMap.getOrDefault(categories[groupPosition], new ArrayList<>()).size();
  }

  /**
   * @param groupPosition
   * @return the category of a given position
   */
  @Override
  public Object getGroup(int groupPosition) {
    if (groupPosition >= getGroupCount()) {
      return null;
    }
    return this.categories[groupPosition];
  }

  /**
   * @param groupPosition
   * @param childPosition
   * @return the event for a given position in a given category
   */
  @Override
  public Object getChild(int groupPosition, int childPosition) {

    if (groupPosition >= getGroupCount()) {
      return null;
    }
    if (childPosition >= getChildrenCount(groupPosition)) {
      return null;
    }

    return this.eventsMap.get(categories[groupPosition]).get(childPosition);

  }

  /**
   * Gets the ID for the group at the given position. This group ID must be unique across groups.
   * The combined ID (see {@link #getCombinedGroupId(long)}) must be unique across ALL items (groups
   * and all children).
   *
   * @param groupPosition the position of the group for which the ID is wanted
   * @return the ID associated with the group
   */
  @Override
  public long getGroupId(int groupPosition) {
    return groupPosition;
  }

  /**
   * Gets the ID for the given child within the given group. This ID must be unique across all
   * children within the group. The combined ID (see {@link #getCombinedChildId(long, long)}) must
   * be unique across ALL items (groups and all children).
   *
   * @param groupPosition the position of the group that contains the child
   * @param childPosition the position of the child within the group for which the ID is wanted
   * @return the ID associated with the child
   */
  @Override
  public long getChildId(int groupPosition, int childPosition) {
    return childPosition;
  }

  /**
   * Indicates whether the child and group IDs are stable across changes to the underlying data.
   *
   * @return whether or not the same ID always refers to the same object d
   */
  @Override
  public boolean hasStableIds() {
    return false;
  }

  /**
   * Gets a View that displays the given group. This View is only for the group--the Views for the
   * group's children will be fetched using {@link #getChildView(int, int, boolean, View,
   * ViewGroup)}.
   *
   * @param groupPosition the position of the group for which the View is returned
   * @param isExpanded whether the group is expanded or collapsed
   * @param convertView the old view to reuse, if possible. You should check that this view is
   *     non-null and of an appropriate type before using. If it is not possible to convert this
   *     view to display the correct data, this method can create a new view. It is not guaranteed
   *     that the convertView will have been previously created by {@link #getGroupView(int,
   *     boolean, View, ViewGroup)}.
   * @param parent the parent that this view will eventually be attached to
   * @return the View corresponding to the group at the specified position
   */
  @Override
  public View getGroupView(
      int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
    EventCategory eventCategory = (EventCategory) getGroup(groupPosition);

    LayoutEventCategoryBinding binding;
    if (convertView == null) {
      LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      binding = LayoutEventCategoryBinding.inflate(inflater, parent, false);
    } else {
      binding = DataBindingUtil.getBinding(convertView);
    }

    binding.setCategoryName(eventCategory.name());

    Context context = parent.getContext();

    AddEventListener addEventOnClickListener =
        new AddEventListener() {
          @Override
          public void addEvent() {
            AlertDialog.Builder builder = new Builder(context);
            builder.setTitle("Select Event Type");

            ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(context, android.R.layout.select_dialog_singlechoice);

            arrayAdapter.add("Roll-Call Event");
            arrayAdapter.add("Election Event");

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.setAdapter(
                arrayAdapter,
                ((dialog, which) -> {
                  dialog.dismiss();
                  viewModel.chooseEventType(EventType.values()[which]);
                }));
            builder.show();
          }
        };

    binding.setIsFutureCategory(eventCategory.equals(FUTURE));
    binding.setViewmodel(viewModel);
    binding.setLifecycleOwner(lifecycleOwner);
    binding.setAddEventListener(addEventOnClickListener);
    binding.executePendingBindings();
    
    binding.addFutureEventButton.setFocusable(false);

    return binding.getRoot();
  }

  /**
   * Gets a View that displays the data for the given child within the given group.
   *
   * @param groupPosition the position of the group that contains the child
   * @param childPosition the position of the child (for which the View is returned) within the
   *     group
   * @param isLastChild Whether the child is the last child within the group
   * @param convertView the old view to reuse, if possible. You should check that this view is
   *     non-null and of an appropriate type before using. If it is not possible to convert this
   *     view to display the correct data, this method can create a new view. It is not guaranteed
   *     that the convertView will have been previously created by {@link #getChildView(int, int,
   *     boolean, View, ViewGroup)}.
   * @param parent the parent that this view will eventually be attached to
   * @return the View corresponding to the child at the specified position
   */
  @Override
  public View getChildView(
      int groupPosition,
      int childPosition,
      boolean isLastChild,
      View convertView,
      ViewGroup parent) {

    // TODO : For the moment, events are displayed the same if user is attendee or organizer,
    // in the future it could be nice to have a pencil icon to allow organizer to modify an event

    ViewDataBinding binding;
    Event event = ((Event) getChild(groupPosition, childPosition));
    if (convertView == null) {
      LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      binding = LayoutEventBinding.inflate(inflater, parent, false);
    } else {
      binding = DataBindingUtil.getBinding(convertView);
    }
      // we use a switch case to handle all the different type of actions we want when we click on a certain event

EventCategory category = (EventCategory) getGroup(groupPosition);
/* if the election is the present when we click on it it will launch cast vote, if it's in the past it will launch
            if the election is in the past it will display all the election results */

      switch (event.type) {
        case ELECTION:
         LayoutElectionDisplayBinding electionBinding ;
          if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            electionBinding = LayoutElectionDisplayBinding.inflate(inflater, parent, false);
          } else {
            electionBinding = DataBindingUtil.getBinding(convertView);
          }
          Election election =(Election) event;
          electionBinding.setElection(election);
          Date dStart = new java.util.Date(Long.valueOf(election.getStartTimestamp())*1000);// *1000 because it needs to be in milisecond
          String dateStart = new SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.FRENCH).format(dStart);
          electionBinding.electionStartDate.setText("Start date : " +dateStart);
          Date dEnd = new java.util.Date(Long.valueOf(election.getEndTimestamp())*1000);
          String dateEnd = new SimpleDateFormat("dd/MM, yyyy HH:mm",Locale.FRENCH).format(dEnd);
          electionBinding.electionEndDate.setText("End Date : " + dateEnd);
          if(category == PRESENT) {
           electionBinding.electionActionButton.setText("Cast Vote");
            electionBinding.electionActionButton.setOnClickListener(
                    clicked -> viewModel.openCastVotes());
          }
          else if (category == PAST) {
            electionBinding.electionActionButton.setText("Election Results");
              electionBinding.electionActionButton.setOnClickListener(
                      clicked -> viewModel.openElectionResults(true));

          }
          else if (category == FUTURE) {
            electionBinding.electionActionButton.setVisibility(View.GONE);
          }

          electionBinding.electionEditButton.setOnClickListener( clicked -> {
              viewModel.setCurrentElection(election);
              viewModel.openManageElection(true);
          });
          electionBinding.setEventCategory(category);
          electionBinding.setViewModel(viewModel);
          electionBinding.setLifecycleOwner(lifecycleOwner);
          electionBinding.executePendingBindings();
          return electionBinding.getRoot();
            }


return binding.getRoot();
  }

  /**
   * A helper method that places the events in the correct key-value pair according to their times
   *
   * @param events
   */
  private void putEventsInMap(List<Event> events) {
    Collections.sort(events);
    long now = Instant.now().getEpochSecond();
    for (Event event : events) {
      if (event.getEndTimestamp() < now) {
        eventsMap.get(PAST).add(event);
      } else if (event.getStartTimestamp() > now) {
        eventsMap.get(FUTURE).add(event);
      } else {
        eventsMap.get(PRESENT).add(event);
      }
    }
  }

  /**
   * Whether the child at the specified position is selectable.
   *
   * @param groupPosition the position of the group that contains the child
   * @param childPosition the position of the child within the group
   * @return whether the child is selectable.
   */
  @Override
  public boolean isChildSelectable(int groupPosition, int childPosition) {
    return true;
  }
}
