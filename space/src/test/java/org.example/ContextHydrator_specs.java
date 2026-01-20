package shoplive.liveagent;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import autoparams.AutoParams;
import org.junit.jupiter.api.Test;
import shoplive.liveagent.command.ApplyPinnedProducts;
import shoplive.liveagent.command.ApplyTranscribedUtterance;
import shoplive.liveagent.command.ApplyUnpinnedProducts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContextHydrator_specs {

    public static class InMemoryContextStore implements ContextStore {

        private final Map<String, LiveContext> contexts = new HashMap<>();

        @Override
        public void addContext(LiveContext context) {
            contexts.put(context.broadcast().key(), context);
        }

        @Override
        public boolean reviseContextIfExists(
            String broadcastKey,
            UnaryOperator<LiveContext> reviser
        ) {
            LiveContext existing = contexts.get(broadcastKey);
            if (existing == null) {
                return false;
            }
            contexts.put(broadcastKey, reviser.apply(existing));
            return true;
        }

        public LiveContext getContext(String broadcastKey) {
            return contexts.get(broadcastKey);
        }
    }

    @Test
    void 생성자는_contextStore_매개변수에_대한_null_방어구문을_갖는다() {
        assertThrows(IllegalArgumentException.class, () -> new ContextHydrator(null));
    }

    @Test
    @AutoParams
    void handleCommand는_ApplyPinnedProducts_형식_매개변수에_대한_null_방어구문을_갖는다(
        LiveContext context
    ) {
        InMemoryContextStore contextStore = new InMemoryContextStore();
        contextStore.addContext(context);
        ContextHydrator sut = new ContextHydrator(contextStore);
        assertThrows(IllegalArgumentException.class, () -> sut.handleCommand((ApplyPinnedProducts) null));
    }

    @Test
    @AutoParams
    void handleCommand는_ApplyPinnedProducts_명령을_올바르게_처리한다(
        LiveContext context,
        List<String> productIdsToPin
    ) {
        InMemoryContextStore contextStore = new InMemoryContextStore();
        contextStore.addContext(context);
        ContextHydrator sut = new ContextHydrator(contextStore);
        sut.handleCommand(new ApplyPinnedProducts(
            context.broadcast().key(),
            productIdsToPin,
            ZonedDateTime.now()
        ));

        List<String> pinnedIds = contextStore.getContext(context.broadcast().key()).pins().stream()
            .map(LiveContext.ProductPin::productId)
            .collect(Collectors.toList());
        assertThat(pinnedIds).containsAll(productIdsToPin);
    }

    @Test
    @AutoParams
    void handleCommand는_컨텍스트가_없을_때_ApplyPinnedProducts_명령을_무시한다(
        LiveContext context,
        ApplyPinnedProducts command
    ) {
        InMemoryContextStore contextStore = new InMemoryContextStore();
        contextStore.addContext(context);
        ContextHydrator sut = new ContextHydrator(contextStore);

        sut.handleCommand(command);

        assertEquals(context, contextStore.getContext(context.broadcast().key()));
    }

    @Test
    @AutoParams
    void handleCommand는_ApplyUnpinnedProducts_형식_매개변수에_대한_null_방어구문을_갖는다(
        LiveContext context
    ) {
        InMemoryContextStore contextStore = new InMemoryContextStore();
        contextStore.addContext(context);
        ContextHydrator sut = new ContextHydrator(contextStore);
        assertThrows(IllegalArgumentException.class, () -> sut.handleCommand((ApplyUnpinnedProducts) null));
    }

    @Test
    @AutoParams
    void handleCommand는_ApplyUnpinnedProducts_명령을_올바르게_처리한다(
        LiveContext context,
        List<String> productIdsToUnpin
    ) {
        InMemoryContextStore contextStore = new InMemoryContextStore();
        contextStore.addContext(context);
        ContextHydrator sut = new ContextHydrator(contextStore);

        sut.handleCommand(new ApplyUnpinnedProducts(
            context.broadcast().key(),
            productIdsToUnpin
        ));

        List<String> pinnedIds = contextStore.getContext(context.broadcast().key()).pins().stream()
            .map(LiveContext.ProductPin::productId)
            .collect(Collectors.toList());
        assertThat(pinnedIds).doesNotContainAnyElementsOf(productIdsToUnpin);
    }

    @Test
    @AutoParams
    void handleCommand는_컨텍스트가_없을_때_ApplyUnpinnedProducts_명령을_무시한다(
        LiveContext context,
        ApplyUnpinnedProducts command
    ) {
        InMemoryContextStore contextStore = new InMemoryContextStore();
        contextStore.addContext(context);
        ContextHydrator sut = new ContextHydrator(contextStore);

        sut.handleCommand(command);

        assertEquals(context, contextStore.getContext(context.broadcast().key()));
    }

    @Test
    @AutoParams
    void handleCommand는_ApplyTranscribedUtterance_형식_매개변수에_대한_null_방어구문을_갖는다(
        LiveContext context
    ) {
        InMemoryContextStore contextStore = new InMemoryContextStore();
        contextStore.addContext(context);
        ContextHydrator sut = new ContextHydrator(contextStore);
        assertThrows(IllegalArgumentException.class, () -> sut.handleCommand((ApplyTranscribedUtterance) null));
    }

    @Test
    @AutoParams
    void handleCommand는_ApplyTranscribedUtterance_명령을_올바르게_처리한다(
        LiveContext context,
        String text,
        TimeWindow timeWindow,
        ZonedDateTime transcribedTime
    ) {
        InMemoryContextStore contextStore = new InMemoryContextStore();
        contextStore.addContext(context);
        ContextHydrator sut = new ContextHydrator(contextStore);

        sut.handleCommand(new ApplyTranscribedUtterance(
            context.broadcast().key(),
            text,
            timeWindow,
            transcribedTime
        ));

        LiveContext.TranscribedUtterance lastUtterance = contextStore.getContext(context.broadcast().key()).transcribedUtterances().getLast();
        assertEquals(text, lastUtterance.text());
        assertEquals(timeWindow.start(), lastUtterance.timeWindow().start());
        assertEquals(timeWindow.end(), lastUtterance.timeWindow().end());
        assertEquals(transcribedTime, lastUtterance.transcribedTime());
    }

    @Test
    @AutoParams
    void handleCommand는_컨텍스트가_없을_때_ApplyTranscribedUtterance_명령을_무시한다(
        LiveContext context,
        ApplyTranscribedUtterance command
    ) {
        InMemoryContextStore contextStore = new InMemoryContextStore();
        contextStore.addContext(context);
        ContextHydrator sut = new ContextHydrator(contextStore);

        sut.handleCommand(command);

        assertEquals(context, contextStore.getContext(context.broadcast().key()));
    }
}
