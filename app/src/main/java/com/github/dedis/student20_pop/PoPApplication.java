package com.github.dedis.student20_pop;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.github.dedis.student20_pop.model.Lao;
import com.github.dedis.student20_pop.model.Person;
import com.github.dedis.student20_pop.utility.security.PrivateInfoStorage;
import com.google.gson.Gson;
import android.os.Handler;
import android.os.Looper;

import com.github.dedis.student20_pop.model.Event;
import com.github.dedis.student20_pop.model.Keys;
import com.github.dedis.student20_pop.utility.network.HighLevelClientProxy;
import com.github.dedis.student20_pop.utility.network.PoPClientEndpoint;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.github.dedis.student20_pop.PoPApplication.AddWitnessResult.ADD_WITNESS_ALREADY_EXISTS;
import static com.github.dedis.student20_pop.PoPApplication.AddWitnessResult.ADD_WITNESS_SUCCESSFUL;
import static com.github.dedis.student20_pop.model.Event.EventType.DISCUSSION;
import static com.github.dedis.student20_pop.model.Event.EventType.MEETING;
import static com.github.dedis.student20_pop.model.Event.EventType.POLL;

/**
 * Class modelling the application : a unique person associated with LAOs
 */
public class PoPApplication extends Application {
    public static final String TAG = PoPApplication.class.getSimpleName();
    public static final String SP_PERSON_ID_KEY = "SHARED_PREFERENCES_PERSON_ID";
    private static final String LOCAL_BACKEND_URI = "ws://10.0.2.2:2000";
    public static final String USERNAME = "USERNAME"; //TODO: let user choose/change its name

    private static Context appContext;
    private Person person;
    private Map<Lao, List<Event>> laoEventsMap;
    private Map<Lao, List<String>> laoWitnessMap;

    //represents the Lao which we are connected to, can be null
    private Lao currentLao;

    //TODO: person/laos used for testing when we don't have a backend connected
    private Person dummyPerson;
    private Lao dummyLao;
    private Map<Lao, List<Event>> dummyLaoEventsMap;
    private CompletableFuture<HighLevelClientProxy> localProxy;

    /**
     * @return PoP Application Context
     */
    public static Context getAppContext() {
        return appContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        PoPClientEndpoint.startPurgeRoutine(new Handler(Looper.getMainLooper()));

        appContext = getApplicationContext();

        Gson gson = new Gson();
        SharedPreferences sp = this.getSharedPreferences(TAG, Context.MODE_PRIVATE);

        // Verify if the information is not present
        if(person == null && laoEventsMap == null && laoWitnessMap == null) {
            // Verify if the user already exists
            if (sp.contains(SP_PERSON_ID_KEY)) {
                // Recover user's information
                String id = sp.getString(SP_PERSON_ID_KEY, "");
                String authentication = PrivateInfoStorage.readData(this, id);
                if (authentication == null) {
                    person = new Person(USERNAME);
                    Log.d(TAG, "Private key of user cannot be accessed, new key pair is created");
                } else {
                    person = new Person(USERNAME, id, authentication, new ArrayList<>());
                }
            } else {
                // Create new user and list of LAOs
                person = new Person(USERNAME);
                laoEventsMap = new HashMap<>();
                laoWitnessMap = new HashMap<>();
                // Store private key of user
                if (PrivateInfoStorage.storeData(this, person.getId(), person.getAuthentication()))
                    Log.d(TAG, "Stored private key of organizer");
            }
        }

        dummyPerson = new Person("name");
        dummyLao = new Lao("LAO I just joined", new Date(), dummyPerson.getId());
        dummyLaoEventsMap = dummyLaoEventMap();
        laoWitnessMap.put(dummyLao, new ArrayList<>());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        SharedPreferences sp = this.getSharedPreferences(TAG, Context.MODE_PRIVATE);

        // Use commit for information to be stored immediately
        sp.edit().putString(SP_PERSON_ID_KEY, person.getId()).commit();
    }

    /**
     * @return Person corresponding to the user
     */
    public Person getPerson() {
        return dummyPerson;
        //TODO when connected to backend
        //return person;
    }

    /**
     * @return the current lao
     */
    public Lao getCurrentLao() {
        return dummyLao;
        //TODO when connected to backend
        //return currentLao;
    }

    /**
     * @return list of LAOs corresponding to the user
     */
    public List<Lao> getLaos() {
        return new ArrayList<>(dummyLaoEventsMap.keySet());
        //TODO when connected to backend
        //return new ArrayList<>(laoEventsMap.keySet());
    }

    /**
     * @return map of LAOs as keys and lists of events corresponding to the lao as values
     */
    public Map<Lao, List<Event>> getLaoEventsMap() {
        return dummyLaoEventsMap;
        //TODO when connected to backend
        //return laoEventsMap;
    }

    /**
     * @return the list of Events associated with the given LAO, null if lao is not in the map
     */
    public List<Event> getEvents(Lao lao) {
        return dummyLaoEventsMap.get(lao);
        //TODO when connected to backend
        //return laoEventsMap.get(lao);
    }

    /**
     * Get witnesses of current LAO
     *
     * @return lao's corresponding list of witnesses
     */
    public List<String> getWitnesses() {
        return laoWitnessMap.get(dummyLao);
        //TODO when connected to backend
        //return laoWitnessMap.get(currentLao);
    }

    /**
     * @param lao from where we want to get the witnesses
     * @return lao's corresponding list of witnesses
     */
    public List<String> getWitnesses(Lao lao) {
        return laoWitnessMap.get(lao);
    }

    /**
     * Get the map of LAOs and its witnesses
     */
    public Map<Lao, List<String>> getLaoWitnessMap() {
        return laoWitnessMap;
    }

    /**
     * Get the proxy of the local device's backend
     * <p>
     * Create it if needed
     *
     * @return a completable future that will hold the proxy once the connection the backend is established
     */
    public CompletableFuture<HighLevelClientProxy> getLocalProxy() {
        refreshLocalProxy();

        return localProxy;
    }

    /**
     * @param lao to add to the app
     */
    public void addLao(Lao lao) {
        if (!laoEventsMap.containsKey(lao)) {
            this.laoEventsMap.put(lao, new ArrayList<>());
        }
    }

    /**
     * @param event to be added to the current lao
     */
    public void addEvent(Event event) {
        addEvent(getCurrentLao(), event);
    }

    /**
     * @param lao of the new event
     * @param event to be added
     */
    public void addEvent(Lao lao, Event event) {
        getEvents(lao).add(event);
    }

    /**
     * @param witness add witness to current lao
     * @return ADD_WITNESS_SUCCESSFUL if witness has been added
     * ADD_WITNESS_ALREADY_EXISTS if witness already exists
     */
    public AddWitnessResult addWitness(String witness) {
        return addWitness(dummyLao, witness);
        //TODO when connected to backend
        //addWitness(currentLao, witness);
    }

    /**
     * @param lao of the new witness
     * @param witness id to add on the list of witnesses for the LAO
     * @return ADD_WITNESS_SUCCESSFUL if witness has been added
     * ADD_WITNESS_ALREADY_EXISTS if witness already exists
     */
    public AddWitnessResult addWitness(Lao lao, String witness) {
        //TODO when connected to backend
        // send info to backend
        // If witness has been added return true, otherwise false

        List<String> laoWitnesses = laoWitnessMap.get(lao);
        if (laoWitnesses == null) {
            laoWitnesses = new ArrayList<>();
            laoWitnessMap.put(lao, laoWitnesses);
        }

        if (laoWitnesses.contains(witness)) {
            return ADD_WITNESS_ALREADY_EXISTS;
        }

        laoWitnesses.add(witness);

        return ADD_WITNESS_SUCCESSFUL;
    }

    /**
     * @param witnesses add witness to current lao
     * @return corresponding result for each witness in the list
     */
    public List<AddWitnessResult> addWitnesses(List<String> witnesses) {
        return addWitnesses(dummyLao, witnesses);
        //TODO when connected to backend
        //addWitnesses(currentLao, witness);
    }

    /**
     * @param witnesses add witness to current lao
     * @return corresponding result for each witness in the list
     */
    public List<AddWitnessResult> addWitnesses(Lao lao, List<String> witnesses) {
        List<AddWitnessResult> results = new ArrayList<>();
        for (String witness : witnesses) {
            results.add(addWitness(lao, witness));
        }
        return results;
    }

    /**
     * @param person to be set for this Application, can only be done one
     */
    public void setPerson(Person person) {
        if (person != null) {
            this.person = person;
        }
    }

    /**
     * sets the current lao
     *
     * @param lao
     */
    public void setCurrentLao(Lao lao) {
        this.currentLao = lao;
    }

    /**
     * Type of results when adding a witness
     */
    public enum AddWitnessResult {
        ADD_WITNESS_SUCCESSFUL,
        ADD_WITNESS_ALREADY_EXISTS
    }

    /**
     * Refresh the local proxy future.
     * <p>
     * If there was no connections yet, start one.
     * If there was an attempt but it failed, retry.
     * If the connection was lost, retry
     */
    private void refreshLocalProxy() {
        if (localProxy == null)
            // If there was no attempt yet, try
            localProxy = PoPClientEndpoint.connectAsync(URI.create(LOCAL_BACKEND_URI), person);
        else if (localProxy.isDone()) {
            try {
                // If it succeeded, but it is now closed, retry
                HighLevelClientProxy currentSession = localProxy.getNow(null);
                if (currentSession == null || !currentSession.isOpen())
                    localProxy = PoPClientEndpoint.connectAsync(URI.create(LOCAL_BACKEND_URI), person);
            } catch (Exception e) {
                //There was an error during competition, retry
                localProxy = PoPClientEndpoint.connectAsync(URI.create(LOCAL_BACKEND_URI), person);
            }
        }
    }

    /**
     * This method creates a map for testing, when no backend is connected
     *
     * @return the dummy map
     */
    private Map<Lao, List<Event>> dummyLaoEventMap() {
        Map<Lao, List<Event>> map = new HashMap<>();
        List<Event> events = new ArrayList<>();
        Event event1 = new Event("Future Event 1", new Date(2617547969000L), new Keys().getPublicKey(), "EPFL", POLL);
        Event event2 = new Event("Present Event 1", new Date(), new Keys().getPublicKey(), "Somewhere", DISCUSSION);
        Event event3 = new Event("Past Event 1", new Date(1481643086000L), new Keys().getPublicKey(), "Here", MEETING);
        events.add(event1);
        events.add(event2);
        events.add(event3);

        String notMyPublicKey = new Keys().getPublicKey();

        map.put(dummyLao, events);
        map.put(new Lao("LAO 1", new Date(), notMyPublicKey), events);
        map.put(new Lao("LAO 2", new Date(), notMyPublicKey), events);
        map.put(new Lao("My LAO 3", new Date(), dummyPerson.getId()), events);
        map.put(new Lao("LAO 4", new Date(), notMyPublicKey), events);
        return map;
    }
}
