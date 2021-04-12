package com.github.dedis.student20_pop.detail.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.github.dedis.student20_pop.Injection;
import com.github.dedis.student20_pop.R;
import com.github.dedis.student20_pop.ui.qrcode.QRCodeScanningFragment;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.util.List;

/**
 * This fragment wraps the QRCodeScanningFragment in order to show the user how many attendees he
 * has added so far and here the QRCode Fragment won't disappear after scanning a QR code, but will
 * stay until user tells he's done scanning attendees.
 *
 * <p>The attribute eventId represents the Roll-Call Event's id the user wants to add attendees to.
 */
public final class AddAttendeeFragment extends Fragment {

  public static final String TAG = AddAttendeeFragment.class.getSimpleName();
  private final String eventId;
    private CameraSource camera;
    private BarcodeDetector detector;

  public AddAttendeeFragment(String eventId, CameraSource camera, BarcodeDetector detector) {
    super();
    this.eventId = eventId;
      this.camera = camera;
      this.detector = detector;
  }

    public static AddAttendeeFragment newInstance(String eventId, CameraSource camera, BarcodeDetector detector) {
        return new AddAttendeeFragment(eventId, camera, detector);
    }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_add_attendee, container, false);


    //    Fragment newFragment = new QRCodeScanningFragment(ADD_ROLL_CALL_ATTENDEE, eventId);
    //    Objects.requireNonNull(getActivity())
    //        .getSupportFragmentManager()
    //        .beginTransaction()
    //        .replace(R.id.add_attendee_qr_code_fragment, newFragment, QRCodeScanningFragment.TAG)
    //        .addToBackStack(null)
    //        .commit();

    //    PoPApplication app = (PoPApplication) getActivity().getApplication();
    //    Optional<Event> matchingEvent =
    //        app.getCurrentLao()
    //            .map(Lao::getEvents)
    //            .flatMap(
    //                events ->
    //                    events.parallelStream()
    //                        .filter(event -> event.getId().equals(eventId))
    //                        .findFirst());

    //    if (matchingEvent.isPresent()) {
    //      rollCallEvent = (RollCallEvent) matchingEvent.get();
    //      rollCallEvent
    //          .getAttendees()
    //          .addOnListChangedCallback(
    //              new ObservableArrayList.OnListChangedCallback<ObservableArrayList<String>>() {
    //                @Override
    //                public void onChanged(ObservableArrayList<String> sender) {
    //                  ((TextView) view.findViewById(R.id.add_attendee_number_text))
    //                      .setText(getString(R.string.add_attendees_number, sender.size()));
    //                }
    //
    //                @Override
    //                public void onItemRangeChanged(
    //                    ObservableArrayList<String> sender, int positionStart, int itemCount) {
    //                  ((TextView) view.findViewById(R.id.add_attendee_number_text))
    //                      .setText(getString(R.string.add_attendees_number, sender.size()));
    //                }
    //
    //                @Override
    //                public void onItemRangeInserted(
    //                    ObservableArrayList<String> sender, int positionStart, int itemCount) {
    //                  ((TextView) view.findViewById(R.id.add_attendee_number_text))
    //                      .setText(getString(R.string.add_attendees_number, sender.size()));
    //                }
    //
    //                @Override
    //                public void onItemRangeMoved(
    //                    ObservableArrayList<String> sender,
    //                    int fromPosition,
    //                    int toPosition,
    //                    int itemCount) {
    //                  ((TextView) view.findViewById(R.id.add_attendee_number_text))
    //                      .setText(getString(R.string.add_attendees_number, sender.size()));
    //                }
    //
    //                @Override
    //                public void onItemRangeRemoved(
    //                    ObservableArrayList<String> sender, int positionStart, int itemCount) {
    //                  ((TextView) view.findViewById(R.id.add_attendee_number_text))
    //                      .setText(getString(R.string.add_attendees_number, sender.size()));
    //                }
    //              });
    //    }


      QRCodeScanningFragment scanningFragment = QRCodeScanningFragment.newInstance(camera, detector);

      List<String> attendees;


    Button confirm = view.findViewById(R.id.add_attendee_confirm);
    confirm.setOnClickListener(
        click -> {
          AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
          builder
              .setMessage(getString(R.string.scan_all_attendees_question))
              .setCancelable(false)
              .setPositiveButton(
                  getString(R.string.confirm),
                  (dialog, id) -> {
                    getActivity()
                        .getSupportFragmentManager()
                        .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                  })
              .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {});

          AlertDialog alert = builder.create();
          alert.show();
        });

    return view;
  }
}
