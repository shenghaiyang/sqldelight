@file:JvmName("RxQuery")

package com.squareup.sqldelight.runtime.rx

import com.squareup.sqldelight.Query
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Scheduler
import io.reactivex.annotations.CheckReturnValue
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.Optional
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Turns this [Query] into an [Observable] which emits whenever the underlying result set changes.
 *
 * ### Scheduler:
 *   [asObservable] by default operates on the [io.reactivex.schedulers.Schedulers.io] scheduler but
 *   can be optionally overridden with [scheduler]
 */
@CheckReturnValue
@JvmOverloads
@JvmName("toObservable")
fun <T : Any> Query<T>.asObservable(scheduler: Scheduler = Schedulers.io()): Observable<Query<T>> {
  return Observable.create(QueryOnSubscribe(this)).observeOn(scheduler)
}

private class QueryOnSubscribe<T : Any>(
  private val query: Query<T>
) : AtomicBoolean(), ObservableOnSubscribe<Query<T>>, Query.Listener, Disposable {

  private val emitters = mutableListOf<ObservableEmitter<Query<T>>>()

  override fun subscribe(emitter: ObservableEmitter<Query<T>>) {
    emitters.add(emitter)
    emitter.setDisposable(this)
    query.addListener(this)
    emitter.onNext(query)
  }

  override fun queryResultsChanged() {
    emitters.forEach { it.onNext(query) }
  }

  override fun isDisposed() = get()

  override fun dispose() {
    if (compareAndSet(false, true)) {
      query.removeListener(this)
    }
  }
}

@CheckReturnValue
fun <T : Any> Observable<Query<T>>.mapToOne(): Observable<T> {
  return map { it.executeAsOne() }
}

@CheckReturnValue
fun <T : Any> Observable<Query<T>>.mapToOneOrDefault(defaultValue: T): Observable<T> {
  return map { it.executeAsOneOrNull() ?: defaultValue }
}

@CheckReturnValue
fun <T : Any> Observable<Query<T>>.mapToOptional(): Observable<Optional<T>> {
  return map { Optional.ofNullable(it.executeAsOneOrNull()) }
}

@CheckReturnValue
fun <T: Any> Observable<Query<T>>.mapToList(): Observable<List<T>> {
  return map { it.executeAsList() }
}

@CheckReturnValue
fun <T : Any> Observable<Query<T>>.mapToOneNonNull(): Observable<T> {
  return flatMap {
    val result = it.executeAsOneOrNull()
    if (result == null) Observable.empty() else Observable.just(result)
  }
}
