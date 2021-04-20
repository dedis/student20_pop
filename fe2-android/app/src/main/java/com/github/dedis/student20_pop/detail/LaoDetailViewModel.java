package com.github.dedis.student20_pop.detail;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.github.dedis.student20_pop.Event;
import com.github.dedis.student20_pop.model.Election;
import com.github.dedis.student20_pop.model.Lao;
import com.github.dedis.student20_pop.model.data.LAORepository;
import com.github.dedis.student20_pop.model.event.EventCategory;
import com.github.dedis.student20_pop.model.event.EventType;
import com.github.dedis.student20_pop.model.network.answer.Result;
import com.github.dedis.student20_pop.model.network.method.message.MessageGeneral;
import com.github.dedis.student20_pop.model.network.method.message.data.election.CastVote;
import com.github.dedis.student20_pop.model.network.method.message.data.election.ElectionSetup;
import com.github.dedis.student20_pop.model.network.method.message.data.rollcall.CreateRollCall;
import com.github.dedis.student20_pop.model.network.method.message.data.rollcall.CreateRollCall.StartType;
import com.github.dedis.student20_pop.utility.security.Keys;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LaoDetailViewModel extends AndroidViewModel {

  public static final String TAG = LaoDetailViewModel.class.getSimpleName();

  /*
   * LiveData objects for capturing events like button clicks
   */
  private final MutableLiveData<Event<Boolean>> mOpenHomeEvent = new MutableLiveData<>();
  private final MutableLiveData<Event<Boolean>> mOpenIdentityEvent = new MutableLiveData<>();
  private final MutableLiveData<Event<Boolean>> mShowPropertiesEvent = new MutableLiveData<>();
  private final MutableLiveData<Event<Boolean>> mEditPropertiesEvent = new MutableLiveData<>();
  private final MutableLiveData<Event<Boolean>> mOpenLaoDetailEvent = new MutableLiveData<>();
  private final MutableLiveData<Event<EventType>> mChooseNewLaoEventTypeEvent = new MutableLiveData<>();
  private final MutableLiveData<Event<EventType>> mNewLaoEventCreationEvent = new MutableLiveData<>();
  private final MutableLiveData<Event<Boolean>> mOpenNewRollCallEvent = new MutableLiveData<>();
  private final MutableLiveData<Event<Boolean>> mOpenElectionResultsEvent = new MutableLiveData<>();
  private final MutableLiveData<Event<Boolean>> mOpenManageElectionEvent = new MutableLiveData<>();
  private final MutableLiveData<Event<Boolean>> mElectionCreated = new MutableLiveData<>();
  private final MutableLiveData<Event<Boolean>> mOpenCastVotes = new MutableLiveData<>();

  /*
   * LiveData objects that represent the state in a fragment
   */
  private final MutableLiveData<Lao> mCurrentLao = new MutableLiveData<>();
  private final MutableLiveData<Election> mCurrentElection = new MutableLiveData<>(); // Represents the current election being managed/opened in a fragment
  private final MutableLiveData<Boolean> mIsOrganizer = new MutableLiveData<>();
  private final MutableLiveData<Boolean> showProperties = new MutableLiveData<>(false);
  private final MutableLiveData<String> mLaoName = new MutableLiveData<>("");
  private final LiveData<List<String>> mWitnesses =
      Transformations.map(
          mCurrentLao,
          lao -> lao == null ? new ArrayList<>() : new ArrayList<>(lao.getWitnesses()));

  private final LiveData<String> mCurrentLaoName =
      Transformations.map(mCurrentLao, lao -> lao == null ? "" : lao.getName());

  //  Multiple events from Lao may be concatenated using Stream.concat()
  private final LiveData<List<com.github.dedis.student20_pop.model.event.Event>> mLaoEvents = Transformations
          .map(mCurrentLao,
                  lao -> lao == null ? new ArrayList<com.github.dedis.student20_pop.model.event.Event>() :
                          Stream.concat(lao.getRollCalls().values().stream(),lao.getElections().values().stream()).collect(Collectors.toList()));

  /*
   * Dependencies for this class
   */
  private final LAORepository mLAORepository;
  private final AndroidKeysetManager mKeysetManager;
  private CompositeDisposable disposables;
  private Gson mGson;

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

  @Override
  protected void onCleared() {
    super.onCleared();
    disposables.dispose();
  }


  public void sendVote(Election election, List<List<Long>> votes) {
    Log.d(TAG, "sending a new vote in election : " + election.getName());

    Lao lao = getCurrentLao();
    if (lao == null) {
      Log.d(TAG, "failed to retrieve current lao");
      return;
    }

    String channel = election.getChannel();
    CastVote  castVote;
    String laoId = channel.substring(6); // removing /root/ prefix
    castVote = new CastVote(election.getWriteIn(), votes, election.getQuestion(), election.getId(), laoId);


    try {
      KeysetHandle publicKeysetHandle = mKeysetManager.getKeysetHandle().getPublicKeysetHandle();
      String publicKey = Keys.getEncodedKey(publicKeysetHandle);
      byte[] sender = Base64.getDecoder().decode(publicKey);

      PublicKeySign signer = mKeysetManager.getKeysetHandle().getPrimitive(PublicKeySign.class);
      MessageGeneral msg = new MessageGeneral(sender, castVote, signer, mGson);

      Log.d(TAG, "sending publish message");
      Disposable disposable =
              mLAORepository
                      .sendPublish(channel, msg)
                      .subscribeOn(Schedulers.io())
                      .observeOn(AndroidSchedulers.mainThread())
                      .timeout(5, TimeUnit.SECONDS)
                      .subscribe(
                              answer -> {
                                if (answer instanceof Result) {
                                    Log.d(TAG, "sent a vote successfully");
                                    // Toast ? + send back to election screen or details screen ?
                                    openLaoDetail();
                                } else {
                                  Log.d(TAG, "failed to send the vote");
                                }
                              },
                              throwable -> {
                                Log.d(TAG, "timed out waiting for result on cast_vote", throwable);
                              });

      disposables.add(disposable);
    } catch (GeneralSecurityException | IOException e) {
      Log.d(TAG, "failed to retrieve public key", e);
      return;
    }
  }



  /**
   * Creates new roll call event.
   * <p>
   * Publish a GeneralMessage containing CreateRollCall data.
   *
   * @param title       the title of the roll call
   * @param description the description of the roll call, can be empty
   * @param start       the start time of the roll call, zero if start type is SCHEDULED
   * @param scheduled   the scheduled time of the roll call, zero if start type is NOW
   * @return the id of the newly created roll call event, null if fails to create the event
   */
  public String createNewRollCall(String title, String description, long start, long scheduled) {
    Log.d(TAG, "creating a new roll call with title " + title);

    Lao lao = getCurrentLao();
    if (lao == null) {
      Log.d(TAG, "failed to retrieve current lao");
      return null;
    }

    String channel = lao.getChannel();
    CreateRollCall createRollCall;
    String laoId = channel.substring(6); // removing /root/ prefix
    if (start != 0) {
      createRollCall = new CreateRollCall(title, start, StartType.NOW, "", description, laoId);
    } else {
      createRollCall =
          new CreateRollCall(title, scheduled, StartType.SCHEDULED, "", description, laoId);
    }

    try {
      KeysetHandle publicKeysetHandle = mKeysetManager.getKeysetHandle().getPublicKeysetHandle();
      String publicKey = Keys.getEncodedKey(publicKeysetHandle);
      byte[] sender = Base64.getDecoder().decode(publicKey);

      PublicKeySign signer = mKeysetManager.getKeysetHandle().getPrimitive(PublicKeySign.class);
      MessageGeneral msg = new MessageGeneral(sender, createRollCall, signer, mGson);

      Log.d(TAG, "sending publish message");
      Disposable disposable =
          mLAORepository
              .sendPublish(channel, msg)
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .timeout(5, TimeUnit.SECONDS)
              .subscribe(
                  answer -> {
                    if (answer instanceof Result) {
                      Log.d(TAG, "created a roll call successfully");
                      openLaoDetail();
                    } else {
                      Log.d(TAG, "failed to create a roll call");
                    }
                  },
                  throwable -> {
                    Log.d(TAG, "timed out waiting for result on roll_call/create", throwable);
                  });

      disposables.add(disposable);
    } catch (GeneralSecurityException | IOException e) {
      Log.d(TAG, "failed to retrieve public key", e);
      return null;
    }
    return createRollCall.getId();
  }

  public void createNewElection(String name, long start, long end, String voting_method, boolean write_in, List<String> ballot_options, String question) {
    Log.d(TAG,"creating a new election with name " + name);

    Lao lao = getCurrentLao();
    if (lao == null) {
      Log.d(TAG, "failed to retrieve current lao");
      return;
    }

    String channel = lao.getChannel();
    ElectionSetup electionSetup;
    String laoId = channel.substring(6);

    electionSetup = new ElectionSetup(name, start, end, voting_method, write_in, ballot_options, question, laoId);

    try {
      // Retrieve identity of who is creating the election
      KeysetHandle publicKeysetHandle = mKeysetManager.getKeysetHandle().getPublicKeysetHandle();
      String publicKey = Keys.getEncodedKey(publicKeysetHandle);
      byte[] sender = Base64.getDecoder().decode(publicKey);

      PublicKeySign signer = mKeysetManager.getKeysetHandle().getPrimitive(PublicKeySign.class);
      MessageGeneral msg = new MessageGeneral(sender, electionSetup, signer, mGson);

      Log.d(TAG, "sending publish message");
      Disposable disposable =
              mLAORepository
                      .sendPublish(channel, msg)
                      .subscribeOn(Schedulers.io())
                      .observeOn(AndroidSchedulers.mainThread())
                      .timeout(5, TimeUnit.SECONDS)
                      .subscribe(
                              answer -> {
                                if (answer instanceof Result) {
                                  Log.d(TAG, "created an election successfully");
                                  mElectionCreated.postValue(new Event<>(true));
                                  mCurrentLao.postValue(lao);
                                }
                                else {
                                  Log.d(TAG, "failed to setup an election");

                                }
                              },
                              throwable -> {
                                Log.d(TAG, "timed out waiting for result on election/create", throwable);
                              });

      disposables.add(disposable);

    } catch (GeneralSecurityException | IOException e) {
      Log.d(TAG, "failed to retrieve public key", e);
    }

  }

  /**
   * Opens a roll call event.
   * <p>
   * Publish a GeneralMessage containing OpenRollCall data.
   *
   * @param id the roll call id to open
   */
  public void openRollCall(String id) {
    Log.d(TAG, "opening a roll call with id " + id);

    //TODO: implement open roll call
  }

  /**
   * Remove specific witness from the LAO's list of witnesses.
   * <p>
   * Publish a GeneralMessage containing UpdateLao data.
   *
   * @param witness the id of the witness to remove
   */
  public void removeWitness(String witness) {
    Log.d(TAG, "trying to remove witness: " + witness);
    // TODO: implement this by sending an UpdateLao
  }

  /*
   * Getters for MutableLiveData instances declared above
   */
  public LiveData<Event<Boolean>> getOpenLaoDetailEvent() {
    return mOpenLaoDetailEvent;
  }

  public LiveData<Event<Boolean>> getOpenElectionResultsEvent() { return mOpenElectionResultsEvent; }

  public LiveData<Event<Boolean>> getElectionCreated() {return mElectionCreated;}

  public LiveData<Event<Boolean>> getOpenManageElectionEvent() { return mOpenManageElectionEvent;}

  public LiveData<Event<Boolean>> getOpenCastVotes() {return mOpenCastVotes;}

  public LiveData<List<com.github.dedis.student20_pop.model.event.Event>> getLaoEvents() { return mLaoEvents; }

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

  public LiveData<Event<EventType>> getNewLaoEventEvent() {
    return mChooseNewLaoEventTypeEvent;
  }

  public LiveData<Event<EventType>> getNewLaoEventCreationEvent() {
    return mNewLaoEventCreationEvent;
  }

  public LiveData<Event<Boolean>> getOpenNewRollCallEvent() {
    return mOpenNewRollCallEvent;
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

  public LiveData<Boolean> getShowProperties() {
    return showProperties;
  }

  public LiveData<List<String>> getWitnesses() {
    return mWitnesses;
  }

  public Election getCurrentElection() { return mCurrentElection.getValue(); }

  /*
   * Methods that modify the state or post an Event to update the UI.
   */

  public void openHome() {
    mOpenHomeEvent.setValue(new Event<>(true));
  }

  public void openLaoDetail() {
    mOpenLaoDetailEvent.postValue(new Event<>(true));
  }
  public void openCastVotes() {
    mOpenCastVotes.postValue(new Event<>(true));
  }

  public void openIdentity() {
    mOpenIdentityEvent.setValue(new Event<>(true));
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

  public void setCurrentElection(Election e) {
    mCurrentElection.setValue(e);
  }
  public void terminateCurrentElection() {
    if(mCurrentLao.getValue().removeElection(mCurrentElection.getValue().getId())) {
      Log.d(TAG, "Election deleted : " + mCurrentElection.getValue().getId());
      Lao lao = getCurrentLao();
      mCurrentLao.postValue(lao);
      openLaoDetail();
    } else {
      Log.d(TAG, "Impossible to delete election : " + mCurrentElection.getValue().getId());
    }
  }


  /**
   * Choosing an event type to create on the multiple-choice screen
   *
   * @param eventType the event type to create
   */
  public void chooseEventType(EventType eventType) {
    mChooseNewLaoEventTypeEvent.postValue(new Event<>(eventType));
  }

  /**
   * Creating a new event of specified type
   *
   * @param eventType the event type of the new event
   */
  public void newLaoEventCreation(EventType eventType) {
    mNewLaoEventCreationEvent.postValue(new Event<>(eventType));
  }

  public void openNewRollCall(Boolean open) {
    mOpenNewRollCallEvent.postValue(new Event<>(open));
  }

  public void openElectionResults(Boolean open) {
    mOpenElectionResultsEvent.postValue(new Event<>(open));
  }
  public void openManageElection(Boolean open) {mOpenManageElectionEvent.postValue(new Event<>(open));}

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

  public void setCurrentLaoName(String laoName) {
    if (laoName != null && !laoName.isEmpty() && !laoName.equals(getCurrentLaoName())) {
      Log.d(TAG, "New name for current LAO: " + laoName);
      mLaoName.setValue(laoName);
    }
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


}
