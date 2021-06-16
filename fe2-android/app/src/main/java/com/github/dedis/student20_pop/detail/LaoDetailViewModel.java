package com.github.dedis.student20_pop.detail;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.github.dedis.student20_pop.Event;
import com.github.dedis.student20_pop.R;
import com.github.dedis.student20_pop.home.HomeViewModel;
import com.github.dedis.student20_pop.model.Election;
import com.github.dedis.student20_pop.model.Lao;
import com.github.dedis.student20_pop.model.RollCall;
import com.github.dedis.student20_pop.model.Wallet;
import com.github.dedis.student20_pop.model.data.LAORepository;
import com.github.dedis.student20_pop.model.event.EventState;
import com.github.dedis.student20_pop.model.event.EventType;
import com.github.dedis.student20_pop.model.network.answer.Error;
import com.github.dedis.student20_pop.model.network.answer.Result;
import com.github.dedis.student20_pop.model.network.method.message.MessageGeneral;
import com.github.dedis.student20_pop.model.network.method.message.data.election.CastVote;
import com.github.dedis.student20_pop.model.network.method.message.data.election.ElectionSetup;
import com.github.dedis.student20_pop.model.network.method.message.data.election.ElectionVote;
import com.github.dedis.student20_pop.model.network.method.message.data.lao.StateLao;
import com.github.dedis.student20_pop.model.network.method.message.data.lao.UpdateLao;
import com.github.dedis.student20_pop.model.network.method.message.data.rollcall.CloseRollCall;
import com.github.dedis.student20_pop.model.network.method.message.data.rollcall.CreateRollCall;
import com.github.dedis.student20_pop.model.network.method.message.data.rollcall.OpenRollCall;
import com.github.dedis.student20_pop.qrcode.CameraPermissionViewModel;
import com.github.dedis.student20_pop.qrcode.QRCodeScanningViewModel;
import com.github.dedis.student20_pop.utility.security.Keys;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.PublicKeySign;
import com.google.crypto.tink.integration.android.AndroidKeysetManager;
import com.google.gson.Gson;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;



public class LaoDetailViewModel extends AndroidViewModel implements CameraPermissionViewModel,
        QRCodeScanningViewModel {
    public static final String TAG = LaoDetailViewModel.class.getSimpleName();
    private static final String LAO_FAILURE_MESSAGE = "failed to retrieve current lao";
    private static final String PK_FAILURE_MESSAGE = "failed to retrieve public key";
    private static final String PUBLISH_MESSAGE = "sending publish message";

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
    private final MutableLiveData<Event<String>> mOpenRollCallEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> mOpenRollCallTokenEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> mOpenAttendeesListEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> mOpenLaoWalletEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> mOpenElectionResultsEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> mOpenManageElectionEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> mElectionCreatedEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> mOpenCastVotesEvent = new MutableLiveData<>();

    private final MutableLiveData<Event<Integer>> mNbAttendeesEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<Integer>> mAskCloseRollCallEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<Integer>> mCloseRollCallEvent = new MutableLiveData<>();

    private final MutableLiveData<Event<Boolean>> mCreatedRollCallEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> mPkRollCallEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> mWalletMessageEvent = new MutableLiveData<>();

    private final MutableLiveData<Event<String>> mAttendeeScanConfirmEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> mScanWarningEvent = new MutableLiveData<>();
    /*
     * LiveData objects that represent the state in a fragment
     */
    private final MutableLiveData<Lao> mCurrentLao = new MutableLiveData<>();
    private final MutableLiveData<Election> mCurrentElection = new MutableLiveData<>(); // Represents the current election being managed/opened in a fragment
    private final MutableLiveData<Boolean> mIsOrganizer = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showProperties = new MutableLiveData<>(false);
    private final MutableLiveData<String> mLaoName = new MutableLiveData<>("");
    private final MutableLiveData<List<List<Integer>>> mCurrentElectionVotes = new MutableLiveData<>();
    private final LiveData<List<String>> mWitnesses =
            Transformations.map(
                    mCurrentLao,
                    lao -> lao == null ? new ArrayList<>() : new ArrayList<>(lao.getWitnesses()));
    private final LiveData<String> mCurrentLaoName =
            Transformations.map(mCurrentLao, lao -> lao == null ? "" : lao.getName());
    //  Multiple events from Lao may be concatenated using Stream.concat()
    private final LiveData<List<com.github.dedis.student20_pop.model.event.Event>> mLaoEvents = Transformations
            .map(mCurrentLao,
                    lao -> lao == null ? new ArrayList<>() :
                            Stream.concat(lao.getRollCalls().values().stream(), lao.getElections().values().stream()).collect(Collectors.toList()));

  private final LiveData<List<com.github.dedis.student20_pop.model.RollCall>> mLaoAttendedRollCalls = Transformations
          .map(mCurrentLao,
                  lao -> lao == null ? new ArrayList<com.github.dedis.student20_pop.model.RollCall>() :
                          lao.getRollCalls().values().stream().filter(rollcall->rollcall.getState()== EventState.CLOSED).filter(rollcall->attendedOrOrganized(lao, rollcall)).collect(Collectors.toList()));

  /**
   * Predicate used for filtering rollcalls to make sure that the user either attended the rollcall or was the organizer
   * @param lao
   * @param rollcall
   * @return boolean saying whether user attended or organized the given roll call
   */
  private boolean attendedOrOrganized(Lao lao, RollCall rollcall){
    //find out if user has attended the rollcall
    String firstLaoId = lao.getChannel().substring(6);
    String pk = "";
    try {
      pk = Base64.getUrlEncoder().encodeToString(Wallet.getInstance().findKeyPair(firstLaoId, rollcall.getPersistentId()).second);
    } catch (GeneralSecurityException e) {
      Log.d(TAG, "failed to retrieve public key from wallet", e);
      return false;
    }
    return rollcall.getAttendees().contains(pk) || isOrganizer().getValue();
  }

    /*
     * Dependencies for this class
     */
    private final LAORepository mLAORepository;
    private final AndroidKeysetManager mKeysetManager;
    private final CompositeDisposable disposables;
    private final Gson mGson;
    private String mCurrentRollCallId = ""; //used to know which roll call to close
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

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.dispose();
    }

    /**
     * Sends a ElectionCastVotes message .
     *
     * <p>Publish a GeneralMessage containing ElectionCastVotes data.
     *
     * @param votes the corresponding votes for that election
     */
    public void sendVote(List<ElectionVote> votes) {
        Election election = mCurrentElection.getValue();
        if (election == null) {
            Log.d(TAG, "failed to retrieve current election");
            return;
        }
        Log.d(TAG, "sending a new vote in election : " + election + " with election start time" + election.getStartTimestamp());
        Lao lao = getCurrentLaoValue();
        if (lao == null) {
            Log.d(TAG, LAO_FAILURE_MESSAGE);
            return;
        }
        String laoChannel = lao.getChannel();
        String laoId = laoChannel.substring(6);
        CastVote castVote = new CastVote(votes, election.getId(), laoId);

        //TODO change this to election.getChannel() when method is implemented in Victor's PR
        String electionChannel = laoChannel + "/" + election.getId();

        try {
            // Retrieve identity of who is sending the votes
            KeysetHandle publicKeysetHandle = mKeysetManager.getKeysetHandle().getPublicKeysetHandle();
            String publicKey = Keys.getEncodedKey(publicKeysetHandle);
            byte[] sender = Base64.getUrlDecoder().decode(publicKey);

            PublicKeySign signer = mKeysetManager.getKeysetHandle().getPrimitive(PublicKeySign.class);
            MessageGeneral msg = new MessageGeneral(sender, castVote, signer, mGson);

            Log.d(TAG, PUBLISH_MESSAGE);
            Disposable disposable =
                    mLAORepository
                            .sendPublish(electionChannel, msg)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .timeout(5, TimeUnit.SECONDS)
                            .subscribe(
                                    answer -> {
                                        if (answer instanceof Result) {
                                            Log.d(TAG, "sent a vote successfully");
                                            openLaoDetail();
                                        } else {
                                            Log.d(TAG, "failed to send the vote");
                                        }
                                    },
                                    throwable -> Log.d(TAG, "timed out waiting for result on cast_vote", throwable));

            disposables.add(disposable);
        } catch (GeneralSecurityException | IOException e) {
            Log.d(TAG, PK_FAILURE_MESSAGE, e);
        }
    }

    /**
     * Creates new Election event.
     *
     * <p>Publish a GeneralMessage containing ElectionSetup data.
     *
     * @param name          the name of the election
     * @param start         the start time of the election
     * @param end           the end time of the election
     * @param votingMethod  the type of voting method (e.g Plurality)
     * @param ballotOptions the list of ballot options
     * @param question      the question associated to the election
     * @return the id of the newly created election event, null if fails to create the event
     */
    public String createNewElection(String name, long start, long end, String votingMethod, boolean writeIn, List<String> ballotOptions, String question) {
        Log.d(TAG, "creating a new election with name " + name);

        Lao lao = getCurrentLaoValue();
        if (lao == null) {
            Log.d(TAG, LAO_FAILURE_MESSAGE);
            return null;
        }

        String channel = lao.getChannel();
        ElectionSetup electionSetup;
        String laoId = channel.substring(6);

        electionSetup = new ElectionSetup(name, start, end, votingMethod, writeIn, ballotOptions, question, laoId);

        try {
            // Retrieve identity of who is creating the election
            KeysetHandle publicKeysetHandle = mKeysetManager.getKeysetHandle().getPublicKeysetHandle();
            String publicKey = Keys.getEncodedKey(publicKeysetHandle);
            byte[] sender = Base64.getUrlDecoder().decode(publicKey);
            PublicKeySign signer = mKeysetManager.getKeysetHandle().getPrimitive(PublicKeySign.class);
            MessageGeneral msg = new MessageGeneral(sender, electionSetup, signer, mGson);

            Log.d(TAG, PUBLISH_MESSAGE);
            Disposable disposable =
                    mLAORepository
                            .sendPublish(channel, msg)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .timeout(5, TimeUnit.SECONDS)
                            .subscribe(
                                    answer -> {
                                        if (answer instanceof Result) {
                                            Log.d(TAG, "setup an election");
                                            mElectionCreatedEvent.postValue(new Event<>(true));
                                        } else if (answer instanceof Error) {
                                            Log.d(TAG, "failed to setup an election because of the following error : " + ((Error) answer).getError().getDescription());
                                        } else {
                                            Log.d(TAG, "failed to setup an election");
                                        }
                                    },
                                    throwable ->
                                            Log.d(TAG, "timed out waiting for result on election/create", throwable)
                            );

            disposables.add(disposable);

        } catch (GeneralSecurityException | IOException e) {
            Log.d(TAG, PK_FAILURE_MESSAGE, e);
            return null;
        }
        return electionSetup.getId();
    }

    /**
     * Creates new roll call event.
     *
     * <p>Publish a GeneralMessage containing CreateRollCall data.
     *
     * @param title         the title of the roll call
     * @param description   the description of the roll call, can be empty
     * @param proposedStart the proposed start time of the roll call
     * @param proposedEnd   the proposed end time of the roll call
     * @param open          true if we want to directly open the roll call
     * @return the id of the newly created roll call event, null if fails to create the event
     */
    public void createNewRollCall(String title, String description, long proposedStart, long proposedEnd, boolean open) {
        Log.d(TAG, "creating a new roll call with title " + title);
        Lao lao = getCurrentLaoValue();
        if (lao == null) {
            Log.d(TAG, LAO_FAILURE_MESSAGE);
            return;
        }
        String channel = lao.getChannel();
        CreateRollCall createRollCall;
        String laoId = channel.substring(6); // removing /root/ prefix
        createRollCall = new CreateRollCall(title, proposedStart, proposedEnd, "Lausanne", description, laoId);

        try {

            KeysetHandle publicKeysetHandle = mKeysetManager.getKeysetHandle().getPublicKeysetHandle();
            String publicKey = Keys.getEncodedKey(publicKeysetHandle);
            byte[] sender = Base64.getUrlDecoder().decode(publicKey);
            PublicKeySign signer = mKeysetManager.getKeysetHandle().getPrimitive(PublicKeySign.class);
            Log.d(TAG, PUBLISH_MESSAGE);
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
                                            Log.d(TAG, "created a roll call with id: " + createRollCall.getId());
                                            if (open) {
                                                openRollCall(createRollCall.getId());
                                            } else {
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
    }

    /**
     * Opens a roll call event.
     *
     * <p>Publish a GeneralMessage containing OpenRollCall data.
     *
     * @param id the roll call id to open
     */
    public void openRollCall(String id) {
        Log.d(TAG, "call openRollCall");

        Lao lao = getCurrentLaoValue();
        if (lao == null) {
            Log.d(TAG, LAO_FAILURE_MESSAGE);
            return;
        }
        long openedAt = Instant.now().getEpochSecond();
        String channel = lao.getChannel();
        String laoId = channel.substring(6); // removing /root/ prefix
        Optional<RollCall> optRollCall = lao.getRollCall(id);
        if (!optRollCall.isPresent()) {
            Log.d(TAG, "failed to retrieve roll call with id " + id);
            return;
        }
        RollCall rollCall = optRollCall.get();
        OpenRollCall openRollCall = new OpenRollCall(laoId, id, openedAt, rollCall.getState());
        attendees = new HashSet<>(rollCall.getAttendees());

        try {
            KeysetHandle publicKeysetHandle = mKeysetManager.getKeysetHandle().getPublicKeysetHandle();
            String publicKey = Keys.getEncodedKey(publicKeysetHandle);
            byte[] sender = Base64.getUrlDecoder().decode(publicKey);
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
                                            openAttendeeScanning();
                                        } else {
                                            Log.d(TAG, "failed to open the roll call");
                                        }
                                    },
                                    throwable -> Log.d(TAG, "timed out waiting for result on roll_call/open", throwable)
                            );
            disposables.add(disposable);
        } catch (GeneralSecurityException | IOException e) {
            Log.d(TAG, PK_FAILURE_MESSAGE, e);
        }
    }

    /**
     * Closes the roll call event currently open
     *
     * <p>Publish a GeneralMessage containing CloseRollCall data.
     */
    public void closeRollCall(int nextFragment) {
        Log.d(TAG, "call closeRollCall");
        Lao lao = getCurrentLaoValue();
        if (lao == null) {
            Log.d(TAG, LAO_FAILURE_MESSAGE);
            return;
        }
        long end = Instant.now().getEpochSecond();
        String channel = lao.getChannel();
        String laoId = channel.substring(6); // removing /root/ prefix
        CloseRollCall closeRollCall = new CloseRollCall(laoId, mCurrentRollCallId, end, new ArrayList<>(attendees));
        try {
            KeysetHandle publicKeysetHandle = mKeysetManager.getKeysetHandle().getPublicKeysetHandle();
            String publicKey = Keys.getEncodedKey(publicKeysetHandle);
            byte[] sender = Base64.getUrlDecoder().decode(publicKey);
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
                                            mCurrentRollCallId = "";
                                            attendees.clear();
                                            mCloseRollCallEvent.setValue(new Event<>(nextFragment));
                                        } else {
                                            Log.d(TAG, "failed to close the roll call");
                                        }
                                    },
                                    throwable -> Log.d(TAG, "timed out waiting for result on roll_call/open", throwable)
                            );
            disposables.add(disposable);
        } catch (GeneralSecurityException | IOException e) {
            Log.d(TAG, PK_FAILURE_MESSAGE, e);
        }

    }

    /**
     * Remove specific witness from the LAO's list of witnesses.
     *
     * <p>Publish a GeneralMessage containing UpdateLao data.
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

    public LiveData<Event<Boolean>> getOpenElectionResultsEvent() {
        return mOpenElectionResultsEvent;
    }

    public LiveData<Event<Boolean>> getElectionCreated() {
        return mElectionCreatedEvent;
    }

    public LiveData<Event<Boolean>> getOpenManageElectionEvent() {
        return mOpenManageElectionEvent;
    }

    public LiveData<Event<Boolean>> getOpenCastVotes() {
        return mOpenCastVotesEvent;
    }

    public LiveData<List<com.github.dedis.student20_pop.model.event.Event>> getLaoEvents() {
        return mLaoEvents;
    }

    public LiveData<List<com.github.dedis.student20_pop.model.RollCall>> getLaoAttendedRollCalls() {
        return mLaoAttendedRollCalls;
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

    public LiveData<Event<EventType>> getNewLaoEventEvent() {
        return mChooseNewLaoEventTypeEvent;
    }

    public LiveData<Event<EventType>> getNewLaoEventCreationEvent() {
        return mNewLaoEventCreationEvent;
    }

    public LiveData<Event<Boolean>> getOpenNewRollCallEvent() {
        return mOpenNewRollCallEvent;
    }

    public LiveData<Lao> getCurrentLao() {
        return mCurrentLao;
    }

    public Lao getCurrentLaoValue() {
        return mCurrentLao.getValue();
    }

    public LiveData<String> getCurrentLaoName() {
        return mCurrentLaoName;
    }

    public void setCurrentLaoName(String laoName) {
        if (laoName != null && !laoName.isEmpty() && !laoName.equals(getCurrentLaoName())) {
            Log.d(TAG, "New name for current LAO: " + laoName);
            mLaoName.setValue(laoName);
        }
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

    public LiveData<Event<String>> getOpenRollCallEvent() {
        return mOpenRollCallEvent;
    }

    public LiveData<Event<String>> getOpenRollCallTokenEvent() {
        return mOpenRollCallTokenEvent;
    }

    public LiveData<Event<String>> getOpenAttendeesListEvent() {
      return mOpenAttendeesListEvent;
    }

    public LiveData<Event<Boolean>> getOpenLaoWalletEvent() {
      return mOpenLaoWalletEvent;
    }

    public LiveData<Event<Integer>> getNbAttendeesEvent() {
        return mNbAttendeesEvent;
    }

    public LiveData<Event<Integer>> getAskCloseRollCallEvent() {
        return mAskCloseRollCallEvent;
    }

    public LiveData<Event<Integer>> getCloseRollCallEvent() {
        return mCloseRollCallEvent;
    }

    public LiveData<Event<Boolean>> getCreatedRollCallEvent() {
        return mCreatedRollCallEvent;
    }

    public LiveData<Event<String>> getAttendeeScanConfirmEvent() {
        return mAttendeeScanConfirmEvent;
    }

    public LiveData<Event<String>> getScanWarningEvent() {
        return mScanWarningEvent;
    }

    public LiveData<Event<String>> getPkRollCallEvent() {
        return mPkRollCallEvent;
    }

    public LiveData<Event<Boolean>> getWalletMessageEvent() {
        return mWalletMessageEvent;
    }

    public Election getCurrentElection() {
        return mCurrentElection.getValue();
    }

    public void setCurrentElection(Election e) {
        mCurrentElection.setValue(e);
    }

    public MutableLiveData<List<List<Integer>>> getCurrentElectionVotes() {
        return mCurrentElectionVotes;
    }

    public void setCurrentElectionVotes(List<List<Integer>> currentElectionVotes) {
        if(currentElectionVotes == null)
            throw new IllegalArgumentException();
        mCurrentElectionVotes.setValue(currentElectionVotes);
    }

    public void setCurrentElectionQuestionVotes(List<Integer> votes, int position){
        if(votes == null || position < 0 || position >= mCurrentElectionVotes.getValue().size())
            throw new IllegalArgumentException();

        mCurrentElectionVotes.getValue().set(position, votes);
    }

    /*
     * Methods that modify the state or post an Event to update the UI.
     */
    public void openHome() {
        if(mCurrentRollCallId.equals("")){
            mOpenHomeEvent.setValue(new Event<>(true));
        }else{
            mAskCloseRollCallEvent.setValue(new Event<>(R.id.fragment_home));
        }
    }

    public void electionCreated() {
        mElectionCreatedEvent.postValue(new Event<>(true));
    }

    public void openLaoDetail() {
        mOpenLaoDetailEvent.postValue(new Event<>(true));
    }

    public void openCastVotes() {
        mOpenCastVotesEvent.postValue(new Event<>(true));
    }

    public void openIdentity() {
        if(mCurrentRollCallId.equals("")){
            mOpenIdentityEvent.setValue(new Event<>(true));
        }else{
            mAskCloseRollCallEvent.setValue(new Event<>(R.id.fragment_identity));
        }
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

    public void terminateCurrentElection() {
        if (mCurrentLao.getValue().removeElection(mCurrentElection.getValue().getId())) {
            Log.d(TAG, "Election deleted : " + mCurrentElection.getValue().getId());
            Lao lao = getCurrentLaoValue();
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

    public void openManageElection(Boolean open) {
        mOpenManageElectionEvent.postValue(new Event<>(open));
    }

    public void confirmEdit() {
        closeEditProperties();
        if (!mLaoName.getValue().isEmpty()) {
            updateLaoName();
        }
    }

    public void updateLaoName() {
        Log.d(TAG, "Updating lao name to " + mLaoName.getValue());

        Lao lao = getCurrentLaoValue();
        String channel = lao.getChannel();
        try {
            KeysetHandle publicKeysetHandle = mKeysetManager.getKeysetHandle().getPublicKeysetHandle();
            String publicKey = Keys.getEncodedKey(publicKeysetHandle);
            byte[] sender = Base64.getUrlDecoder().decode(publicKey);
            PublicKeySign signer = mKeysetManager.getKeysetHandle().getPrimitive(PublicKeySign.class);

            long now = Instant.now().getEpochSecond();
            UpdateLao updateLao = new UpdateLao(publicKey, lao.getCreation(), mLaoName.getValue(), now, lao.getWitnesses());
            MessageGeneral msg = new MessageGeneral(sender, updateLao, signer, mGson);
            Disposable disposable =
                    mLAORepository
                            .sendPublish(channel, msg)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .timeout(5, TimeUnit.SECONDS)
                            .subscribe(
                                    answer -> {
                                        if (answer instanceof Result) {
                                            Log.d(TAG, "updated lao name");
                                            StateLao stateLao = new StateLao(updateLao.getId(), updateLao.getName(), lao.getCreation(), updateLao.getLastModified(), publicKey, msg.getMessageId(), lao.getWitnesses(), new ArrayList<>());
                                            MessageGeneral stateMsg = new MessageGeneral(sender, stateLao, signer, mGson);
                                            mLAORepository
                                                    .sendPublish(channel, stateMsg)
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .timeout(5, TimeUnit.SECONDS)
                                                    .subscribe(
                                                            answer2 -> {
                                                                if (answer2 instanceof Result) {
                                                                    Log.d(TAG, "updated lao name2");
                                                                } else {
                                                                    Log.d(TAG, "failed to update lao name");
                                                                }
                                                            },
                                                            throwable -> Log.d(TAG, "timed out waiting for result on update lao name", throwable)
                                                    );
                                        } else {
                                            Log.d(TAG, "failed to update lao name");
                                        }
                                    },
                                    throwable -> Log.d(TAG, "timed out waiting for result on update lao name", throwable)
                            );
            disposables.add(disposable);
        } catch (GeneralSecurityException | IOException e) {
            Log.d(TAG, PK_FAILURE_MESSAGE, e);
        }
    }

    public void cancelEdit() {
        mLaoName.setValue("");
        closeEditProperties();
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
                                        mIsOrganizer.setValue(isOrganizer);
                                        return;
                                    } catch (GeneralSecurityException e) {
                                        Log.d(TAG, "failed to get public keyset handle", e);
                                    } catch (IOException e) {
                                        Log.d(TAG, "failed to get public key", e);
                                    }
                                    mIsOrganizer.setValue(false);
                                }));
    }

    public void openQrCodeScanningRollCall() {
        mOpenRollCallEvent.setValue(new Event<>(HomeViewModel.SCAN));
        mNbAttendeesEvent.postValue(new Event<>(attendees.size())); //this to display the initial number of attendees
    }

    public void openCameraPermissionRollCall() {
        mOpenRollCallEvent.setValue(new Event<>(HomeViewModel.REQUEST_CAMERA_PERMISSION));
    }

    public void enterRollCall(String id) {
        if(!Wallet.getInstance().isSetUp()){
            mWalletMessageEvent.setValue(new Event<>(true));
            return;
        }
        String firstLaoId = getCurrentLaoValue().getChannel().substring(6); // use the laoId set at creation + need to remove /root/ prefix
        String errorMessage = "failed to retrieve public key from wallet";
        try {
            String pk = Base64.getUrlEncoder().encodeToString(Wallet.getInstance().findKeyPair(firstLaoId, id).second);
            mPkRollCallEvent.postValue(new Event<>(pk));
        } catch (Exception e) {
            Log.d(TAG, errorMessage, e);
        }
    }

    public void openAttendeeScanning() {
        if (ContextCompat.checkSelfPermission(
                getApplication().getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openQrCodeScanningRollCall();
        } else {
            openCameraPermissionRollCall();
        }
    }

    public void openLaoWallet(){
      mOpenLaoWalletEvent.postValue(new Event<>(true));
    }

    public void openRollCallToken(String rollCallId){
        mOpenRollCallTokenEvent.postValue(new Event<>(rollCallId));
    }

    public void openAttendeesList(String rollCallId){
      mOpenAttendeesListEvent.postValue(new Event<>(rollCallId));
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
        try {
            Base64.getUrlDecoder().decode(barcode.rawValue);
        } catch (IllegalArgumentException e) {
            mScanWarningEvent.postValue(new Event<>("Invalid QR code. Please try again."));
            return;
        }
        if (attendees.contains(barcode.rawValue)) {
            mScanWarningEvent.postValue(new Event<>("This QR code has already been scanned. Please try again."));
            return;
        }
        attendees.add(barcode.rawValue);
        mAttendeeScanConfirmEvent.postValue(new Event<>("Attendee has been added."));
        mNbAttendeesEvent.postValue(new Event<>(attendees.size()));
    }

}
