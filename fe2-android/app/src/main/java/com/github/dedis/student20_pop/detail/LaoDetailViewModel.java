package com.github.dedis.student20_pop.detail;
import android.Manifest;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.github.dedis.student20_pop.Event;
import com.github.dedis.student20_pop.R;
import com.github.dedis.student20_pop.home.HomeViewModel;
import com.github.dedis.student20_pop.model.Lao;
import com.github.dedis.student20_pop.model.RollCall;
import com.github.dedis.student20_pop.model.data.LAORepository;
import com.github.dedis.student20_pop.model.event.EventType;
import com.github.dedis.student20_pop.model.network.answer.Result;
import com.github.dedis.student20_pop.model.network.method.message.MessageGeneral;
import com.github.dedis.student20_pop.model.network.method.message.data.rollcall.CloseRollCall;
import com.github.dedis.student20_pop.model.network.method.message.data.rollcall.CreateRollCall;
import com.github.dedis.student20_pop.model.network.method.message.data.rollcall.OpenRollCall;
import com.github.dedis.student20_pop.ui.qrcode.CameraPermissionViewModel;
import com.github.dedis.student20_pop.ui.qrcode.QRCodeScanningViewModel;
import com.github.dedis.student20_pop.utility.security.Hash;
import com.github.dedis.student20_pop.utility.security.Keys;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.PublicKeySign;
import com.google.crypto.tink.integration.android.AndroidKeysetManager;
import com.google.gson.Gson;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class LaoDetailViewModel extends AndroidViewModel implements CameraPermissionViewModel,
        QRCodeScanningViewModel {
  public static final String TAG = LaoDetailViewModel.class.getSimpleName();
  private static final String LAO_FAILURE_MESSAGE = "failed to retrieve current lao";
  private static final String PK_FAILURE_MESSAGE = "failed to retrieve public key";
  /*
   * LiveData objects for capturing events like button clicks
   */
  private final MutableLiveData<Event<Boolean>> mOpenHomeEvent = new MutableLiveData<>();
  private final MutableLiveData<Event<Boolean>> mOpenIdentityEvent = new MutableLiveData<>();
  private final MutableLiveData<Event<Boolean>> mShowPropertiesEvent = new MutableLiveData<>();
  private final MutableLiveData<Event<Boolean>> mEditPropertiesEvent = new MutableLiveData<>();
  private final MutableLiveData<Event<Boolean>> mOpenLaoDetailEvent = new MutableLiveData<>();
  private final MutableLiveData<Event<String>> mOpenConnectRollCallEvent = new MutableLiveData<>();

  private final MutableLiveData<Event<Integer>>  mNbAttendees = new MutableLiveData<>();
  private final MutableLiveData<Event<Boolean>> mCloseRollCallEvent = new MutableLiveData<>();

  private final MutableLiveData<Event<Boolean>> mCreatedRollCallEvent = new MutableLiveData<>();
  private final MutableLiveData<Event<String>>  mScanWarning = new MutableLiveData<>();
  /*
   * LiveData objects that represent the state in a fragment
   */
  private final MutableLiveData<Lao> mCurrentLao = new MutableLiveData<>();
  private final MutableLiveData<Boolean> mIsOrganizer = new MutableLiveData<>();
  private final MutableLiveData<Boolean> showProperties = new MutableLiveData<>(false);
  private final MutableLiveData<String> mLaoName = new MutableLiveData<>("");
  private final LiveData<List<String>> mWitnesses =
          Transformations.map(
                  mCurrentLao,
                  lao -> lao == null ? new ArrayList<>() : new ArrayList<>(lao.getWitnesses()));
  private final LiveData<String> mCurrentLaoName =
          Transformations.map(mCurrentLao, lao -> lao == null ? "" : lao.getName());
  private final MutableLiveData<Event<EventType>> mNewLaoEventEvent = new MutableLiveData<>();
  private String mCurrentRollCallId = "";

  private final LiveData<List<com.github.dedis.student20_pop.model.event.Event>> mLaoEvents = Transformations
          .map(mCurrentLao,
                  lao -> lao == null ? new ArrayList<com.github.dedis.student20_pop.model.event.Event>() :
                          lao.getRollCalls().values().stream().collect(Collectors.toList()));

  /*
   * Dependencies for this class
   */
  private final LAORepository mLAORepository;
  private final AndroidKeysetManager mKeysetManager;
  private CompositeDisposable disposables;
  private Gson mGson;
  private Set<String> attendees = new HashSet<>();

  public LaoDetailViewModel(
          @NonNull Application application,
          LAORepository laoRepository,
          Gson gson,
          AndroidKeysetManager keysetManager) {
    super(application);
    mLAORepository = laoRepository;
    mKeysetManager = keysetManager;
    mGson = gson;
    disposables = new CompositeDisposable();

  }

  public LiveData<Event<Boolean>> getOpenLaoDetailEvent() {
    return mOpenLaoDetailEvent;
  }
  public LiveData<Event<Boolean>> getOpenHomeEvent() {
    return mOpenHomeEvent;
  }
  public LiveData<Event<Boolean>> getOpenIdentityEvent() {
    return mOpenIdentityEvent;
  }
  public LiveData<Event<Boolean>> getShowPropertiesEvent() {
    return mShowPropertiesEvent;
  }
  public LiveData<Event<Boolean>> getEditPropertiesEvent() {
    return mEditPropertiesEvent;
  }
  public Lao getCurrentLao() {
    return mCurrentLao.getValue();
  }
  public LiveData<String> getCurrentLaoName() {
    return mCurrentLaoName;
  }

  public LiveData<Boolean> isOrganizer() {
    return mIsOrganizer;
  }
  public LiveData<List<String>> getWitnesses() {
    return mWitnesses;
  }
  public LiveData<Event<String>> getOpenConnectRollCallEvent() {
    return mOpenConnectRollCallEvent;
  }
  public LiveData<Event<Integer>> getNbAttendees() {
    return mNbAttendees;
  }
  public LiveData<Event<Boolean>> getCloseRollCallEvent() {
    return mCloseRollCallEvent;
  }
  public LiveData<Event<Boolean>> getCreatedRollCallEvent() {
    return mCreatedRollCallEvent;
  }
  public LiveData<Event<String>> getScanWarning() {
    return mScanWarning;
  }
  //from pull request:
  public LiveData<List<com.github.dedis.student20_pop.model.event.Event>> getLaoEvents() {
    return mLaoEvents;
  }

  public void openHome() {
    mOpenHomeEvent.setValue(new Event<>(true));
  }
  public void openLaoDetail() {
    mOpenLaoDetailEvent.postValue(new Event<>(true));
  }
  public void openIdentity() {
    mOpenIdentityEvent.setValue(new Event<>(true));
  }
  public LiveData<Boolean> getShowProperties() {
    return showProperties;
  }
  public void toggleShowHideProperties() {
    boolean val = showProperties.getValue();
    showProperties.postValue(!val);
  }
  public void openEditProperties() {
    mEditPropertiesEvent.setValue(new Event<>(true));
  }
  public void closeEditProperties() {
    mEditPropertiesEvent.setValue(new Event<>(false));
  }
  public void confirmEdit() {
    closeEditProperties();
    if (!mLaoName.getValue().isEmpty()) {
      updateLaoName();
    }
  }
  public void updateLaoName() {
    // TODO: Create an `UpdateLao` message and publish it. Observe the response on a background
    // thread and update the UI accordingly.
    Log.d(TAG, "Updating lao name to " + mLaoName.getValue());
  }
  public void cancelEdit() {
    mLaoName.setValue("");
    closeEditProperties();
  }
  @Override
  protected void onCleared() {
    super.onCleared();
    disposables.dispose();
  }
  public void subscribeToLao(String laoId) {
    disposables.add(
            mLAORepository
                    .getLaoObservable(laoId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            lao -> {
                              Log.d(TAG, "got an update for lao: " + lao.getName());
                              mCurrentLao.postValue(lao);
                              //is this correct??
                              //mLaoEvents.postValue(new ArrayList<com.github.dedis.student20_pop.model.event.Event>(lao.getRollCalls().values()));
                              try {
                                KeysetHandle publicKeysetHandle =
                                        mKeysetManager.getKeysetHandle().getPublicKeysetHandle();
                                boolean isOrganizer =
                                        lao.getOrganizer().equals(Keys.getEncodedKey(publicKeysetHandle));
                                Log.d(TAG, "isOrganizer: " + isOrganizer);
                                mIsOrganizer.postValue(isOrganizer);
                                return;
                              } catch (GeneralSecurityException e) {
                                Log.d(TAG, "failed to get public keyset handle", e);
                              } catch (IOException e) {
                                Log.d(TAG, "failed to get public key", e);
                              }
                              mIsOrganizer.postValue(false);
                            }));
  }
  public void removeWitness(String witness) {
    // TODO: implement this by sending an UpdateLao
    Log.d(TAG, "trying to remove witness: " + witness);
  }
  public void addEvent(EventType eventType) {
    mNewLaoEventEvent.postValue(new Event<>(eventType));
  }
  public void setCurrentLaoName(String laoName) {
    if (laoName != null && !laoName.isEmpty() && !laoName.equals(getCurrentLaoName())) {
      Log.d(TAG, "New name for current LAO: " + laoName);
      mLaoName.setValue(laoName);
    }
  }
  public LiveData<Event<EventType>> getNewLaoEventEvent() {
    return mNewLaoEventEvent;
  }
  public String createNewRollCall(String title, String description, long proposedStart, long proposedEnd, boolean open) {
    Log.d(TAG, "creating a new roll call with title " + title);
    AtomicBoolean created = new AtomicBoolean(false);
    Lao lao = getCurrentLao();
    if (lao == null) {
      Log.d(TAG, LAO_FAILURE_MESSAGE);
      return null;
    }
    String channel = lao.getChannel();
    CreateRollCall createRollCall;
    String laoId = channel.substring(6); // removing /root/ prefix
    createRollCall = new CreateRollCall(title, proposedStart, proposedEnd, "Lausanne", description, laoId);

    try {

      KeysetHandle publicKeysetHandle = mKeysetManager.getKeysetHandle().getPublicKeysetHandle();
      String publicKey = Keys.getEncodedKey(publicKeysetHandle);
      byte[] sender = Base64.getDecoder().decode(publicKey);
      PublicKeySign signer = mKeysetManager.getKeysetHandle().getPrimitive(PublicKeySign.class);

      MessageGeneral msg = new MessageGeneral(sender, createRollCall, signer, mGson);
      Disposable disposable =
              mLAORepository
                      .sendPublish(channel, msg)
                      .subscribeOn(Schedulers.io())
                      .observeOn(AndroidSchedulers.mainThread())
                      .timeout(5, TimeUnit.SECONDS)
                      .subscribe(
                              answer -> {
                                if (answer instanceof Result) {
                                  Log.d(TAG, "created a roll call with id: "+createRollCall.getId());
                                  if(open){
                                    openConnectRollCall(createRollCall.getId());
                                  }else{
                                    mCreatedRollCallEvent.postValue(new Event<>(true));
                                  }
                                } else {
                                  Log.d(TAG, "failed to create a roll call");
                                }
                              },
                              throwable -> {
                                Log.d(TAG, "timed out waiting for result on roll_call/create", throwable);
                              });
      disposables.add(disposable);
    } catch (GeneralSecurityException | IOException e) {
      Log.d(TAG, PK_FAILURE_MESSAGE, e);
    }
    return createRollCall.getId();
  }
  public void openConnectRollCall(String id) {
    Log.d(TAG, "call openConnectRollCall");

    Lao lao = getCurrentLao();
    if (lao == null) {
      Log.d(TAG, LAO_FAILURE_MESSAGE);
      return;
    }
    long openedAt = Instant.now().getEpochSecond();
    String channel = lao.getChannel();
    String laoId = channel.substring(6); // removing /root/ prefix
    String updateId = Hash.hash("R", laoId, id, Long.toString(openedAt));
    Optional<RollCall> optRollCall = lao.getRollCall(id);
    if(!optRollCall.isPresent()){
      Log.d(TAG, "failed to retrieve roll call with id "+id);
      return;
    }
    RollCall rollCall = optRollCall.get();
    OpenRollCall openRollCall = new OpenRollCall(updateId, id, openedAt, rollCall.getState());
    attendees = rollCall.getAttendees();
    Log.d(TAG, "nb attendees: "+attendees.size());
    try {
      KeysetHandle publicKeysetHandle = mKeysetManager.getKeysetHandle().getPublicKeysetHandle();
      String publicKey = Keys.getEncodedKey(publicKeysetHandle);
      byte[] sender = Base64.getDecoder().decode(publicKey);
      PublicKeySign signer = mKeysetManager.getKeysetHandle().getPrimitive(PublicKeySign.class);
      MessageGeneral msg = new MessageGeneral(sender, openRollCall, signer, mGson);
      Disposable disposable =
              mLAORepository
                      .sendPublish(channel, msg)
                      .subscribeOn(Schedulers.io())
                      .observeOn(AndroidSchedulers.mainThread())
                      .timeout(5, TimeUnit.SECONDS)
                      .subscribe(
                              answer -> {
                                if (answer instanceof Result) {
                                  Log.d(TAG, "opened the roll call");
                                  mCurrentRollCallId = openRollCall.getUpdateId();
                                  if (ContextCompat.checkSelfPermission(
                                          getApplication().getApplicationContext(), Manifest.permission.CAMERA)
                                          == PackageManager.PERMISSION_GRANTED) {
                                    openQrCodeScanningRollCall();
                                  } else {
                                    openCameraPermissionRollCall();
                                  }
                                } else {
                                  Log.d(TAG, "failed to open the roll call");
                                }
                              },
                              throwable -> {
                                Log.d(TAG, "timed out waiting for result on roll_call/open", throwable);
                              });
      disposables.add(disposable);
    } catch (GeneralSecurityException | IOException e) {
      Log.d(TAG, PK_FAILURE_MESSAGE, e);
    }
  }

  public void closeRollCall(){
    Log.d(TAG, "call closeRollCall");
    //mCloseRollCallEvent.postValue(new Event<>(true));
    Lao lao = getCurrentLao();
    if (lao == null) {
      Log.d(TAG, LAO_FAILURE_MESSAGE);
      return;
    }
    long end = Instant.now().getEpochSecond();
    String channel = lao.getChannel();//try loa.getId() ??
    String laoId = channel.substring(6); // removing /root/ prefix
    String updateId = Hash.hash("R", laoId, mCurrentRollCallId, Long.toString(end));
    CloseRollCall closeRollCall = new CloseRollCall(updateId, mCurrentRollCallId, end, new ArrayList<>(attendees));
    try {
      KeysetHandle publicKeysetHandle = mKeysetManager.getKeysetHandle().getPublicKeysetHandle();
      String publicKey = Keys.getEncodedKey(publicKeysetHandle);
      byte[] sender = Base64.getDecoder().decode(publicKey);
      PublicKeySign signer = mKeysetManager.getKeysetHandle().getPrimitive(PublicKeySign.class);
      MessageGeneral msg = new MessageGeneral(sender, closeRollCall, signer, mGson);
      Disposable disposable =
              mLAORepository
                      .sendPublish(channel, msg)
                      .subscribeOn(Schedulers.io())
                      .observeOn(AndroidSchedulers.mainThread())
                      .timeout(5, TimeUnit.SECONDS)
                      .subscribe(
                              answer -> {
                                if (answer instanceof Result) {
                                  Log.d(TAG, "closed the roll call");
                                  mCurrentRollCallId = "";//is this really needed or we give id in param??
                                  attendees.clear();
                                  openLaoDetail();
                                } else {
                                  Log.d(TAG, "failed to close the roll call");
                                }
                              },
                              throwable -> {
                                Log.d(TAG, "timed out waiting for result on roll_call/close", throwable);
                              });
      disposables.add(disposable);
    } catch (GeneralSecurityException | IOException e) {
      Log.d(TAG, PK_FAILURE_MESSAGE, e);
    }

  }

  public void openQrCodeScanningRollCall() {
    mOpenConnectRollCallEvent.setValue(new Event<>(HomeViewModel.SCAN));
  }
  public void openCameraPermissionRollCall() {
    mOpenConnectRollCallEvent.setValue(new Event<>(HomeViewModel.REQUEST_CAMERA_PERMISSION));
  }

  @Override
  public void onPermissionGranted() {
    openQrCodeScanningRollCall();
  }
  @Override
  public int getScanDescription() {
    return R.string.qrcode_scanning_add_attendee;
  }
  @Override
  public void onQRCodeDetected(Barcode barcode) {
    Log.d(TAG, "Detected barcode with value: " + barcode.rawValue);
    try{
      Base64.getDecoder().decode(barcode.rawValue);
    }catch(IllegalArgumentException e){
      mScanWarning.postValue(new Event<>("Invalid QR code. Please try again."));
      return;
    }
    if(attendees.contains(barcode.rawValue)){
      mScanWarning.postValue(new Event<>("This QR code has already been scanned. Please try again."));
      return;
    }
    attendees.add(barcode.rawValue);
    mNbAttendees.postValue(new Event<>(attendees.size()));
  }
}