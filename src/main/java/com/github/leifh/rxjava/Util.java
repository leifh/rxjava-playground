package com.github.leifh.rxjava;

import rx.Observable;

import java.util.stream.Stream;

public class Util {

    public static <T> Iterable<T> iterable(Stream<T> stream) {
        return stream::iterator;
    }

    public static <T> Observable<T> observable(Stream<T> stream) {
        return Observable.from(iterable(stream));
    }
}
