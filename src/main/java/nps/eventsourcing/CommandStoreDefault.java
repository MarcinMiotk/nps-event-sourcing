package nps.eventsourcing;

import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import org.springframework.stereotype.Service;

import java.util.LinkedList;

@Service
class CommandStoreDefault implements EventSourcing.CommandStore, EventSourcing.CommandBus {

    final LinkedList<CommandRecord> store = new LinkedList<>();     // TODO: externalize it
    final PublishSubject<CommandRecord> subject = PublishSubject.create();

    @Override
    public void append(EventSourcing.Command command) {
        CommandRecord record = new CommandRecord(command);
        store.offer(record);
        subject.onNext(record);
    }

    @Override
    public <T extends EventSourcing.Command> void subscribe(EventSourcing.CommandHandler handler, Class<T> type) {
        subject.subscribeOn(Schedulers.computation())
                .filter(r->type.isInstance(r.command))
                .subscribe(record->handler.handle(record.command));
    }

    static class CommandRecord {
        private final EventSourcing.Command command;

        public CommandRecord(EventSourcing.Command command) {
            this.command = command;
        }

        @Override
        public String toString() {
            return command.toString();
        }
    }


}
