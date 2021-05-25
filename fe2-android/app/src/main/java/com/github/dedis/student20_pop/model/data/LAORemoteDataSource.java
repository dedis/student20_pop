package com.github.dedis.student20_pop.model.data;


import com.github.dedis.student20_pop.model.network.GenericMessage;
import com.github.dedis.student20_pop.model.network.method.Message;
import io.reactivex.Observable;
import java.util.concurrent.atomic.AtomicInteger;

public class LAORemoteDataSource implements LAODataSource.Remote {

  private static LAORemoteDataSource INSTANCE;

  private LAOService laoService;

  private AtomicInteger requestId;

  public static LAORemoteDataSource getInstance(LAOService laoService) {
    if (INSTANCE == null) {
      INSTANCE = new LAORemoteDataSource(laoService);
    }
    return INSTANCE;
  }

  private LAORemoteDataSource(LAOService service) {
    this.laoService = service;
    requestId = new AtomicInteger();
  }

  public Observable<GenericMessage> observeMessage() {
    return laoService.observeMessage();
  }

  public void sendMessage(Message msg) {
    laoService.sendMessage(msg);
  }

  public int incrementAndGetRequestId() {
    return requestId.incrementAndGet();
  }

  public int getRequestId() {
    return requestId.get();
  }
}
