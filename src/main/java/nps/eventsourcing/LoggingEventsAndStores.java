package nps.eventsourcing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.StringJoiner;

@Component
class LoggingEventsAndStores {

    final EventSourcing.CommandBus commandBus;
    final EventSourcing.EventBus eventBus;


    public LoggingEventsAndStores(EventSourcing.CommandBus commandBus, EventSourcing.EventBus eventBus) {
        this.commandBus = commandBus;
        this.eventBus = eventBus;
    }

    @PostConstruct
    void registering() {
        ObjectMapper mapper = new ObjectMapper();
        commandBus.subscribe(command -> {
            try {
                StringJoiner joiner = new StringJoiner(" ; ");
                joiner.add("COMMAND");
                joiner.add(command.getClass().getSimpleName());
                joiner.add(mapper.writeValueAsString(command));

                System.out.println(joiner.toString());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }, EventSourcing.Command.class);

        eventBus.subscribe(event -> {
            try {
                StringJoiner joiner = new StringJoiner(" ; ");
                joiner.add("EVENT");
                joiner.add(event.getClass().getSimpleName());
                joiner.add(mapper.writeValueAsString(event));

                System.out.println(joiner.toString());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }, EventSourcing.Event.class, EventSourcing.Replay.ONLY_FIRST_OCCURENCE_ALLOWED);
    }
}
