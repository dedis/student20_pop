package com.github.dedis.student20_pop.model.data;

import androidx.annotation.NonNull;

import com.github.dedis.student20_pop.model.data.dao.LAODao;
import com.github.dedis.student20_pop.model.network.GenericMessage;
import com.github.dedis.student20_pop.model.network.answer.Result;
import com.github.dedis.student20_pop.model.network.method.Broadcast;

import io.reactivex.Flowable;

public class LAORepository {
    private volatile static LAORepository INSTANCE = null;

    private final LAODataSource.Remote mRemoteDataSource;
    private final LAODataSource.Local mLocalDataSource;

    private LAORepository(@NonNull LAODataSource.Remote remoteDataSource, @NonNull LAODataSource.Local localDataSource) {
        mRemoteDataSource = remoteDataSource;
        mLocalDataSource = localDataSource;
    }

    public static LAORepository getInstance(LAODataSource.Remote laoRemoteDataSource, LAODataSource.Local localDataSource) {
        if (INSTANCE == null) {
            synchronized (LAORepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LAORepository(laoRemoteDataSource, localDataSource);
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    public Flowable<Result> observeResults() {
        return mRemoteDataSource.observeMessage()
                .filter(genericMessage -> genericMessage instanceof Result)
                .map(genericMessage -> (Result) genericMessage);
    }

    public Flowable<Broadcast> observeBroadcasts() {
        return mRemoteDataSource.observeMessage()
                .filter(genericMessage -> genericMessage instanceof Broadcast)
                .map(genericMessage -> (Broadcast) genericMessage);
    }

}
