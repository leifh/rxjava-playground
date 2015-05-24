package com.github.leifh.rxjava;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.nio.file.*;

public class ObservableDirectory {

  public static Observable<Path> directoryList(final Path path, final String filter) {

    return Observable.create(subscriber -> {

      try(DirectoryStream<Path> stream = Files.newDirectoryStream(path, filter)) {
        for(Path p : stream) {
          if (subscriber.isUnsubscribed()) {
            break;
          }
          subscriber.onNext(p);
        }
        subscriber.onCompleted();
      } catch (IOException e) {
        subscriber.onError(e);
      }

    });
  }

  public static Observable<Path> watchPath(final Path path, final WatchEvent.Kind<?>... events) {
    return Observable.create(subscriber -> {
      Schedulers.io().createWorker().schedule(() -> {
        WatchService watchService = null;

        try {
          watchService = FileSystems.getDefault().newWatchService();

          final WatchService w = watchService;
          subscriber.add(new Subscription() {
            @Override
            public void unsubscribe() {
              if(w != null) {
                try {
                  w.close();
                } catch (IOException e) {
                  // ignore close exception
                }
              }
            }

            @Override
            public boolean isUnsubscribed() {
              return false;
            }
          });

          path.register(watchService, events);
          while (true) {

            if (subscriber.isUnsubscribed()) {
              break;
            }

            final WatchKey key = watchService.take();

            for (WatchEvent<?> watchEvent : key.pollEvents()) {


              final WatchEvent.Kind<?> kind = watchEvent.kind();

              if (kind == StandardWatchEventKinds.OVERFLOW) {
                continue;
              }

              final WatchEvent<Path> watchEventPath = (WatchEvent<Path>) watchEvent;
              final Path entry = watchEventPath.context();

              subscriber.onNext(entry);
            }

            key.reset();

            if (!key.isValid()) {
              break;
            }
          }
        } catch (Exception e) {
          throw new RuntimeException(e);
        } finally {
          if(watchService != null) {
            try {
              watchService.close();
            } catch (IOException e) {
              // ignore close exception
            }
          }
        }
      });
    });
  }
}
