package nps.eventsourcing;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class EventStoreDefault implements EventSourcing.EventStore, EventSourcing.EventBus, EventSourcing.EventStream {

    final LinkedList<EventStoreDefault.EventRecord> store = new LinkedList<>();     // TODO: externalize it
    final PublishSubject<EventStoreDefault.EventRecord> subject = PublishSubject.create();

    @Override
    public UUID append(EventSourcing.Event event) {
        EventRecord record = new EventRecord(event);
        store.offer(record);
        subject.onNext(record);
        return event.getId().getId();
    }

    @Override
    public Observable<EventSourcing.Event> load(IdAggregate id) {
        return Observable.create(observableEmitter -> {
            store.stream()
                    .filter(eventRecord -> eventRecord.event.getId().equals(id))
                    .flatMap(eventRecord -> Stream.of(eventRecord.event))
            .forEach(event -> observableEmitter.onNext(event));
            observableEmitter.onComplete();
        });
    }

    @Override
    public <T extends EventSourcing.Event> Disposable subscribe(EventSourcing.EventHandler handler, Class<T> type, EventSourcing.Replay replay) {
        return subject/*.subscribeOn(Schedulers.computation())*/
                .filter(r->type.isInstance(r.event))
                .subscribe(record->handler.handle(record.event));
    }

    static class EventRecord {
        private final EventSourcing.Event event;

        public EventRecord(EventSourcing.Event event) {
            this.event = event;
        }

        @Override
        public String toString() {
            return event.toString();
        }
    }

}
