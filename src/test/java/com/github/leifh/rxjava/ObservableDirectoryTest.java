package com.github.leifh.rxjava;

import org.glassfish.jersey.client.rx.Rx;
import org.glassfish.jersey.client.rx.RxClient;
import org.glassfish.jersey.client.rx.rxjava.RxObservableInvoker;
import org.junit.BeforeClass;
import org.junit.Test;
import rx.Observable;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import static com.github.leifh.rxjava.Util.observable;
import static org.junit.Assert.assertEquals;

public class ObservableDirectoryTest {

  private static Client client = null;
  private static RxClient<RxObservableInvoker> newRxClient = null;

  @BeforeClass
  public static void warmup() {
    // initialize Jersey Client
    client = ClientBuilder.newClient();
    newRxClient = Rx.from(client, RxObservableInvoker.class);
  }
  
  public static Observable<String> splitWord(String str) {
    return Observable.from(str.replace("\"", "").replace("\\.", "").replace(",", "").replace(":", "").split(" "));
  }

  public Observable<Integer> numberOfWordsInBook(String bookUrl) {
    RxClient<RxObservableInvoker> newRxClient = Rx.newClient(RxObservableInvoker.class);

    // get a text file
    Observable<Response> response = newRxClient.target("http://jv.gilead.org.il/pg/8vcen10.txt").request().rx().get();

    // read a text file
    Observable<String> lines = response.flatMap(r -> observable(new BufferedReader(r.readEntity(Reader.class)).lines()));

    // get words from each lines in the text file
    Observable<String> words = lines.flatMap(ObservableDirectoryTest::splitWord);

    // count words in the text file
    return words.distinct().count();
  }

  @Test
  public void numberOfWordsBooksSync() {
    List<String> books = Arrays.asList(
            "http://jv.gilead.org.il/pg/17660-8.txt",
            "http://jv.gilead.org.il/pg/11927-8.txt",
            "http://jv.gilead.org.il/pg/8vcen10.txt",
            "http://jv.gilead.org.il/pg/8lune09.txt",
            "http://jv.gilead.org.il/le-chateau/20MIL.TXT"
    );

    int numberOfWords = 0;

    for(String url : books) {
      numberOfWords = numberOfWords + numberOfWordsInBook(url).toBlocking().first();
    }

    assertEquals(74535, numberOfWords);
  }

  @Test
  public void numberOfWordsBooks() {
    List<String> books = Arrays.asList(
        "http://jv.gilead.org.il/pg/17660-8.txt",
        "http://jv.gilead.org.il/pg/11927-8.txt",
        "http://jv.gilead.org.il/pg/8vcen10.txt",
        "http://jv.gilead.org.il/pg/8lune09.txt",
        "http://jv.gilead.org.il/le-chateau/20MIL.TXT"
    );

    Observable<Integer> w = Observable.from(books).flatMap(this::numberOfWordsInBook);

    int numberOfWords = w.reduce((a,b) -> a + b).toBlocking().first();

    assertEquals(74535, numberOfWords);
  }
}
