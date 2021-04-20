package com.github.dedis.student20_pop.detail.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.dedis.student20_pop.R;
import net.glxn.qrgen.android.QRCode;

/**
 * Represents the identity of a user within an organization (which allows users to “wear different
 * hats” in different organizations) TODO : For the moment, the goal of this UI is just to show a QR
 * code, but in the future, it will be needed to store identity information somewhere to make it
 * dependent of the current user and LAO
 */
public class IdentityFragment extends Fragment {

  public static final String TAG = IdentityFragment.class.getSimpleName();

  private EditText identityNameEditText;
  private EditText identityTitleEditText;
  private EditText identityOrganizationEditText;
  private EditText identityEmailEditText;
  private EditText identityPhoneEditText;
  private ImageView qrCode;

  public static IdentityFragment newInstance() {
    return new IdentityFragment();
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_identity, container, false);

    // TODO :   The QR code does not appear at all unless the Name field is non-empty
    //  and not all whitespace.
    CheckBox anonymousCheckBox = view.findViewById(R.id.checkbox_anonymous);
    qrCode = view.findViewById(R.id.qr_code);
    identityEmailEditText = view.findViewById(R.id.identity_email);
    identityNameEditText = view.findViewById(R.id.identity_name);
    identityOrganizationEditText = view.findViewById(R.id.identity_organization);
    identityPhoneEditText = view.findViewById(R.id.identity_phone);
    identityTitleEditText = view.findViewById(R.id.identity_title);

    hideIdentityInformation();

    anonymousCheckBox.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          if (isChecked) {
            hideIdentityInformation();
          } else {
            qrCode.setVisibility(View.VISIBLE);
            identityEmailEditText.setVisibility(View.VISIBLE);
            identityNameEditText.setVisibility(View.VISIBLE);
            identityOrganizationEditText.setVisibility(View.VISIBLE);
            identityPhoneEditText.setVisibility(View.VISIBLE);
            identityTitleEditText.setVisibility(View.VISIBLE);
          }
        });

    // NOTE : I was not sure which unique id to use to generate the QR code
    // User identity is composed of :
    // User's public key
    // Organization's ID
    // TODO: verify from view model if user is organizer, if not show uid (get from view model), if
    // yes then show organizer_id and lao_id (get from view model)

    /*
    final PoPApplication app = ((PoPApplication) this.getActivity().getApplication());
    String key = app.getPerson().getAuthentication();
    String laoId = app.getCurrentLaoUnsafe().getId();
    String uniqueIdentity = key + laoId;*/

    identityNameEditText.setText("USERNAME");

    Bitmap myBitmap = QRCode.from("UNIQUE IDENTITY").bitmap();
    qrCode.setImageBitmap(myBitmap);

    return view;
  }

  /** Hide fields when user wants to be anonymous */
  private void hideIdentityInformation() {
    qrCode.setVisibility(View.GONE);
    identityEmailEditText.setVisibility(View.GONE);
    identityNameEditText.setVisibility(View.GONE);
    identityOrganizationEditText.setVisibility(View.GONE);
    identityPhoneEditText.setVisibility(View.GONE);
    identityTitleEditText.setVisibility(View.GONE);
  }
}
