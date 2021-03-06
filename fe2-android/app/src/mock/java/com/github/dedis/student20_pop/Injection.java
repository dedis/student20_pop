package com.github.dedis.student20_pop;


import android.app.Application;
import android.content.Context;
import com.github.dedis.student20_pop.model.data.LAODatabase;
import com.github.dedis.student20_pop.model.data.LAOLocalDataSource;
import com.github.dedis.student20_pop.model.data.LAORemoteDataSource;
import com.github.dedis.student20_pop.model.data.LAORepository;
import com.github.dedis.student20_pop.model.data.LAOService;
import com.github.dedis.student20_pop.model.network.GenericMessage;
import com.github.dedis.student20_pop.model.network.answer.Answer;
import com.github.dedis.student20_pop.model.network.answer.Result;
import com.github.dedis.student20_pop.model.network.method.Message;
import com.github.dedis.student20_pop.model.network.method.message.MessageGeneral;
import com.github.dedis.student20_pop.model.network.method.message.data.Data;
import com.github.dedis.student20_pop.utility.json.JsonAnswerSerializer;
import com.github.dedis.student20_pop.utility.json.JsonDataSerializer;
import com.github.dedis.student20_pop.utility.json.JsonGenericMessageDeserializer;
import com.github.dedis.student20_pop.utility.json.JsonMessageGeneralSerializer;
import com.github.dedis.student20_pop.utility.json.JsonMessageSerializer;
import com.github.dedis.student20_pop.utility.json.JsonResultSerializer;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.crypto.tink.integration.android.AndroidKeysetManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tinder.scarlet.Scarlet;
import com.tinder.scarlet.WebSocket;
import io.reactivex.Observable;
import io.reactivex.Observer;
import java.io.IOException;
import java.security.GeneralSecurityException;
import okhttp3.OkHttpClient;


public class Injection {

  private Injection() {}


  @SuppressWarnings("unused")
  public static AndroidKeysetManager provideAndroidKeysetManager(Context applicationContext)
      throws GeneralSecurityException, IOException {
    return null;
  }

  public static Gson provideGson() {
    return new GsonBuilder()
        .registerTypeAdapter(GenericMessage.class, new JsonGenericMessageDeserializer())
        .registerTypeAdapter(Message.class, new JsonMessageSerializer())
        .registerTypeAdapter(Data.class, new JsonDataSerializer())
        .registerTypeAdapter(Result.class, new JsonResultSerializer())
        .registerTypeAdapter(Answer.class, new JsonAnswerSerializer())
        .registerTypeAdapter(MessageGeneral.class, new JsonMessageGeneralSerializer())
        .create();
  }

  @SuppressWarnings("unused")
  public static BarcodeDetector provideQRCodeDetector(Context context) {
    return null;
  }

  @SuppressWarnings("unused")
  public static CameraSource provideCameraSource(
      Context context, Detector<Barcode> qrDetector, int width, int height) {
    return null;
  }

  public static OkHttpClient provideOkHttpClient() {
    return null;
  }

  @SuppressWarnings("unused")
  public static Scarlet provideScarlet(
      Application application, OkHttpClient okHttpClient, Gson gson) {
    return null;
  }

  @SuppressWarnings("unused")
  public static LAOService provideLAOService(Scarlet scarlet) {
    return null;
  }

  @SuppressWarnings("unused")
  public static LAORepository provideLAORepository(
      Application application, LAOService service, AndroidKeysetManager keysetManager, Gson gson){
    LAODatabase db = LAODatabase.getDatabase(application);
    return LAORepository.getInstance(
        LAORemoteDataSource.getInstance(getMockService()),
        LAOLocalDataSource.getInstance(db),
        keysetManager,
        gson);
  }

  private static LAOService getMockService(){
   return new LAOService() {
     @Override
     public void sendMessage(Message msg) {
       //"mock" method
     }
     @Override
     public Observable<GenericMessage> observeMessage() {
       return new Observable<GenericMessage>() {
         @Override
         protected void subscribeActual(Observer<? super GenericMessage> observer) {
           //"mock" method
         }
       };
     }
     @Override
     public Observable<WebSocket.Event> observeWebsocket() {
       return new Observable<WebSocket.Event>() {
         @Override
         protected void subscribeActual(Observer<? super WebSocket.Event> observer) {
           //"mock" method
         }
       };
     }
   };
  }

}

