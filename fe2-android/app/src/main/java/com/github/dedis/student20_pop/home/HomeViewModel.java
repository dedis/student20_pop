package com.github.dedis.student20_pop.home;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.util.Base64;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.MutableLiveData;
import com.github.dedis.student20_pop.Event;
import com.github.dedis.student20_pop.R;
import com.github.dedis.student20_pop.model.Lao;
import com.github.dedis.student20_pop.model.data.LAORepository;
import com.github.dedis.student20_pop.model.network.answer.Error;
import com.github.dedis.student20_pop.model.network.answer.Result;
import com.github.dedis.student20_pop.model.network.method.message.MessageGeneral;
import com.github.dedis.student20_pop.model.network.method.message.data.lao.CreateLao;
import com.github.dedis.student20_pop.ui.qrcode.CameraPermissionViewModel;
import com.github.dedis.student20_pop.ui.qrcode.QRCodeScanningViewModel;
import com.github.dedis.student20_pop.utility.security.Keys;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.PublicKeySign;
import com.google.crypto.tink.integration.android.AndroidKeysetManager;
import com.google.gson.Gson;
import io.reactivex.BackpressureStrategy;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HomeViewModel extends AndroidViewModel
    implements CameraPermissionViewModel, QRCodeScanningViewModel {

  public static final String TAG = HomeViewModel.class.getSimpleName();
  public static final String SCAN = "SCAN";
  public static final String REQUEST_CAMERA_PERMISSION = "REQUEST_CAMERA_PERMISSION";

  /*
   * LiveData objects for capturing events like button clicks
   */
  private final MutableLiveData<Event<String>> mOpenLaoEvent = new MutableLiveData<>();
  private final MutableLiveData<Event<Boolean>> mOpenHomeEvent = new MutableLiveData<>();
  private final MutableLiveData<Event<Boolean>> mOpenConnectingEvent = new MutableLiveData<>();
  private final MutableLiveData<Event<String>> mOpenConnectEvent = new MutableLiveData<>();
  private final MutableLiveData<Event<Boolean>> mOpenLaunchEvent = new MutableLiveData<>();
  private final MutableLiveData<Event<Boolean>> mLaunchNewLaoEvent = new MutableLiveData<>();
  private final MutableLiveData<Event<Boolean>> mCancelNewLaoEvent = new MutableLiveData<>();
  private final MutableLiveData<Event<Boolean>> mCancelConnectEvent = new MutableLiveData<>();

  /*
   * LiveData objects that represent the state in a fragment
   */
  private final MutableLiveData<String> mConnectingLao = new MutableLiveData<>();
  private final MutableLiveData<String> mLaoName = new MutableLiveData<>();
  private LiveData<List<Lao>> mLAOs;

  /*
   * Dependencies for this class
   */
  private final Gson mGson;
  private final LAORepository mLAORepository;
  private final AndroidKeysetManager mKeysetManager;

  private Disposable disposable;

  public HomeViewModel(
      @NonNull Application application,
      Gson gson,
      LAORepository laoRepository,
      AndroidKeysetManager keysetManager) {
    super(application);

    mLAORepository = laoRepository;
    mGson = gson;
    mKeysetManager = keysetManager;

    mLAOs =
        LiveDataReactiveStreams.fromPublisher(
            mLAORepository.getAllLaos().toFlowable(BackpressureStrategy.BUFFER));
  }

  @Override
  public void onPermissionGranted() {
    openQrCodeScanning();
  }

  @Override
  public int getScanDescription() {
    return R.string.qrcode_scanning_connect_lao;
  }

  @Override
  public void onQRCodeDetected(Barcode barcode) {
    Log.d(TAG, "Detected barcode with value: " + barcode.rawValue);
    // TODO: retrieve lao id/name from barcode.rawValue
    // TODO: send subscribe and switch to the home screen on an answer
    String channel = barcode.rawValue;
    mLAORepository
          .sendSubscribe(channel)
          .observeOn(AndroidSchedulers.mainThread())
          .timeout(5, TimeUnit.SECONDS)
          .subscribe(
                  answer -> {
                    if (answer instanceof Result) {
                      Log.d(TAG, "got success result for subscribe to lao");
                      openHome();
                    } else {
                      Log.d(
                              TAG,
                              "got failure result for subscribe to lao: "
                                      + ((Error) answer).getError().getDescription());
                    }
                  },
                  throwable -> {
                    Log.d(TAG, "timed out waiting for a response for subscribe to lao", throwable);
                  });

    setConnectingLao(channel);
    openConnecting();
  }

  /** onCleared is used to cancel all subscriptions to observables. */
  @Override
  protected void onCleared() {
    if (disposable != null) {
      disposable.dispose();
    }
    super.onCleared();
  }

  /**
   * launchLao is invoked when the user tries to create a new LAO. The method creates a `CreateLAO`
   * message and publishes it to the root channel. It observers the response in the background and
   * switches to the home screen on success.
   */
  public void launchLao() {
    String laoName = mLaoName.getValue();

    try {
      KeysetHandle myKey = mKeysetManager.getKeysetHandle().getPublicKeysetHandle();
      String organizer = Keys.getEncodedKey(myKey);
      byte[] organizerBuf = Base64.decode(organizer, Base64.NO_WRAP);

      Log.d(TAG, "creating lao with name " + laoName);
      CreateLao createLao = new CreateLao(laoName, organizer);
      PublicKeySign signer = mKeysetManager.getKeysetHandle().getPrimitive(PublicKeySign.class);

      MessageGeneral msg = new MessageGeneral(organizerBuf, createLao, signer, mGson);

      mLAORepository
          .sendPublish("/root", msg)
          .observeOn(AndroidSchedulers.mainThread())
          .timeout(5, TimeUnit.SECONDS)
          .subscribe(
              answer -> {
                if (answer instanceof Result) {
                  Log.d(TAG, "got success result for create lao");
                  openHome();
                } else {
                  Log.d(
                      TAG,
                      "got failure result for create lao: "
                          + ((Error) answer).getError().getDescription());
                }
              },
              throwable -> {
                Log.d(TAG, "timed out waiting for a response for create lao", throwable);
              });

    } catch (GeneralSecurityException e) {
      Log.d(TAG, "failed to get public key", e);
    } catch (IOException e) {
      Log.d(TAG, "failed to encode public key", e);
    }
  }

  /*
   * Getters for MutableLiveData instances declared above
   */
  public LiveData<List<Lao>> getLAOs() {
    return mLAOs;
  }

  public LiveData<Event<String>> getOpenLaoEvent() {
    return mOpenLaoEvent;
  }

  public LiveData<Event<Boolean>> getOpenHomeEvent() {
    return mOpenHomeEvent;
  }

  public LiveData<Event<Boolean>> getOpenConnectingEvent() {
    return mOpenConnectingEvent;
  }

  public LiveData<Event<String>> getOpenConnectEvent() {
    return mOpenConnectEvent;
  }

  public LiveData<Event<Boolean>> getOpenLaunchEvent() {
    return mOpenLaunchEvent;
  }

  public LiveData<Event<Boolean>> getLaunchNewLaoEvent() {
    return mLaunchNewLaoEvent;
  }

  public LiveData<Event<Boolean>> getCancelNewLaoEvent() {
    return mCancelNewLaoEvent;
  }

  public LiveData<Event<Boolean>> getCancelConnectEvent() {
    return mCancelConnectEvent;
  }

  public LiveData<String> getConnectingLao() {
    return mConnectingLao;
  }

  public LiveData<String> getLaoName() {
    return mLaoName;
  }

  /*
   * Methods that modify the state or post an Event to update the UI.
   */

  public void openLAO(String laoId) {
    mOpenLaoEvent.setValue(new Event<>(laoId));
  }

  public void openHome() {
    mOpenHomeEvent.postValue(new Event<>(true));
  }

  public void openConnecting() {
    mOpenConnectingEvent.postValue(new Event<>(true));
  }

  public void openConnect() {
    if (ActivityCompat.checkSelfPermission(
            getApplication().getApplicationContext(), Manifest.permission.CAMERA)
        == PackageManager.PERMISSION_GRANTED) {
      openQrCodeScanning();
    } else {
      openCameraPermission();
    }
  }

  public void openQrCodeScanning() {
    mOpenConnectEvent.setValue(new Event<>(SCAN));
  }

  public void openCameraPermission() {
    mOpenConnectEvent.setValue(new Event<>(REQUEST_CAMERA_PERMISSION));
  }

  public void openLaunch() {
    mOpenLaunchEvent.setValue(new Event<>(true));
  }

  public void launchNewLao() {
    mLaunchNewLaoEvent.setValue(new Event<>(true));
  }

  public void cancelNewLao() {
    mCancelNewLaoEvent.setValue(new Event<>(true));
  }

  public void cancelConnect() {
    mCancelConnectEvent.setValue(new Event<>(true));
  }

  public void setConnectingLao(String lao) {
    this.mConnectingLao.postValue(lao);
  }

  public void setLaoName(String name) {
    this.mLaoName.setValue(name);
  }
}
