package com.github.leifh.rxjava;

import rx.Observable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class ObservableIO {

  public static Observable<String> read(BufferedReader reader) {

    return Observable.create(subscriber -> {
      String line = null;
      try {
        while((line = reader.readLine()) != null) {
          subscriber.onNext(line);
        }
        subscriber.onCompleted();
      } catch (IOException e) {
        subscriber.onError(e);
      }
    });
  }

  public static Observable<String> read(Reader reader) {

    return Observable.create(subscriber -> {
      String line = null;
      BufferedReader r = new BufferedReader(reader);
      try {
        while((line = r.readLine()) != null) {
          subscriber.onNext(line);
        }
        subscriber.onCompleted();
      } catch (IOException e) {
        subscriber.onError(e);
      }
    });
  }

}
