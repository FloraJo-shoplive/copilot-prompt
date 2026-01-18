package shoplive.liveagent;

import autoparams.AutoParams;
import autoparams.customization.Customization;
import autoparams.mockito.MockitoCustomizer;
import org.junit.jupiter.api.Test;
import shoplive.liveagent.event.ProductsPinned;
import shoplive.liveagent.event.ProductsUnpinned;
import shoplive.liveagent.event.UtteranceTranscribed;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static java.util.Collections.shuffle;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class ContextManager_specs {

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
    }

    @Test
    @AutoParams
    @Customization(MockitoCustomizer.class)
    public void 생성자는_eventAdapter_매개변수에_대한_null_방어구문을_갖는다(
        ContextHydrator hydrator
    ) {
        assertThatThrownBy(() -> new ContextManager(null, hydrator))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("The argument 'eventAdapter' must not be null");
    }

    @Test
    @AutoParams
    @Customization(MockitoCustomizer.class)
    public void 생성자는_hydrator_매개변수에_대한_null_방어구문을_갖는다(
        ContextEventAdapter eventAdapter
    ) {
        assertThatThrownBy(() -> new ContextManager(eventAdapter, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("The argument 'hydrator' must not be null");
    }

    @Test
    @AutoParams
    @Customization(MockitoCustomizer.class)
    public void getEventHandlers는_UtteranceTranscribed_형식을_처리하는_이벤트_처리기를_반환한다(
        ContextEventAdapter eventAdapter,
        ContextHydrator hydrator
    ) {
        // Arrange
        ContextManager sut = new ContextManager(eventAdapter, hydrator);

        // Act
        List<EventHandler<?>> actual = sut.getEventHandlers();

        // Assert
        assertThat(actual.stream().anyMatch(x -> x.getEventType() == UtteranceTranscribed.class))
            .isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    @AutoParams
    @Customization(MockitoCustomizer.class)
    public void UtteranceTranscribed_이벤트_처리기는_컨텍스트를_올바르게_수정한다(
        ContextEventAdapter eventAdapter,
        LiveContext liveContext,
        UtteranceTranscribed source
    ) {
        // Arrange
        InMemoryContextStore contextStore = new InMemoryContextStore();
        contextStore.addContext(liveContext);
        ContextHydrator hydrator = new ContextHydrator(contextStore);
        ContextManager sut = new ContextManager(eventAdapter, hydrator);
        List<EventHandler<?>> eventHandlers = sut.getEventHandlers();
        EventHandler<UtteranceTranscribed> eventHandler =
            (EventHandler<UtteranceTranscribed>) eventHandlers.stream()
                .filter(x -> x.getEventType() == UtteranceTranscribed.class)
                .findFirst()
                .get();
        UtteranceTranscribed utteranceTranscribed = new UtteranceTranscribed(
            liveContext.broadcast().key(),
            source.text(),
            source.timeWindow(),
            source.transcribedTime()
        );

        // Act
        eventHandler.handleEvent(utteranceTranscribed);

        // Assert
        LiveContext actual = contextStore.contexts.get(utteranceTranscribed.broadcastKey());
        assertThat(actual.transcribedUtterances()).contains(
            new LiveContext.TranscribedUtterance(
                utteranceTranscribed.text(),
                utteranceTranscribed.timeWindow(),
                utteranceTranscribed.transcribedTime()
            ));
    }

    @Test
    @AutoParams
    @Customization(MockitoCustomizer.class)
    public void getEventHandlers는_ProductsPinned_형식을_처리하는_이벤트_처리기를_반환한다(
        ContextEventAdapter eventAdapter,
        ContextHydrator hydrator
    ) {
        // Arrange
        ContextManager sut = new ContextManager(eventAdapter, hydrator);

        // Act
        List<EventHandler<?>> actual = sut.getEventHandlers();

        // Assert
        assertThat(actual.stream().anyMatch(x -> x.getEventType() == ProductsPinned.class))
            .isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    @AutoParams
    @Customization(MockitoCustomizer.class)
    public void ProductsPinned_이벤트_처리기는_컨텍스트를_올바르게_수정한다(
        ContextEventAdapter eventAdapter,
        LiveContext liveContext,
        ProductsPinned source
    ) {
        // Arrange
        InMemoryContextStore contextStore = new InMemoryContextStore();
        contextStore.addContext(liveContext);
        ContextHydrator hydrator = new ContextHydrator(contextStore);
        ContextManager sut = new ContextManager(eventAdapter, hydrator);
        List<EventHandler<?>> eventHandlers = sut.getEventHandlers();
        EventHandler<ProductsPinned> eventHandler =
            (EventHandler<ProductsPinned>) eventHandlers.stream()
                .filter(x -> x.getEventType() == ProductsPinned.class)
                .findFirst()
                .get();
        ProductsPinned productsPinned = new ProductsPinned(
            liveContext.broadcast().key(),
            source.pinnedProductIds(),
            source.pinnedTime()
        );

        // Act
        eventHandler.handleEvent(productsPinned);

        // Assert
        LiveContext actual = contextStore.contexts.get(productsPinned.broadcastKey());
        List<LiveContext.ProductPin> productPins = actual.pins();
        List<LiveContext.ProductPin> pins =
            source.pinnedProductIds().stream().map(x -> new LiveContext.ProductPin(x, source.pinnedTime())).toList();
        assertThat(productPins).containsAll(pins);
    }

    @Test
    @AutoParams
    @Customization(MockitoCustomizer.class)
    public void getEventHandlers는_ProductsUnpinned_형식을_처리하는_이벤트_처리기를_반환한다(
        ContextEventAdapter eventAdapter,
        ContextHydrator hydrator
    ) {
        // Arrange
        ContextManager sut = new ContextManager(eventAdapter, hydrator);

        // Act
        List<EventHandler<?>> actual = sut.getEventHandlers();

        // Assert
        assertThat(actual.stream().anyMatch(x -> x.getEventType() == ProductsUnpinned.class))
            .isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    @AutoParams
    @Customization(MockitoCustomizer.class)
    public void ProductsUnpinned_이벤트_처리기는_컨텍스트를_올바르게_수정한다(
        ContextEventAdapter eventAdapter,
        LiveContext liveContext
    ) {
        // Arrange
        InMemoryContextStore contextStore = new InMemoryContextStore();
        contextStore.addContext(liveContext);
        ContextHydrator hydrator = new ContextHydrator(contextStore);
        ContextManager sut = new ContextManager(eventAdapter, hydrator);
        List<EventHandler<?>> eventHandlers = sut.getEventHandlers();
        EventHandler<ProductsUnpinned> eventHandler =
            (EventHandler<ProductsUnpinned>) eventHandlers.stream()
                .filter(x -> x.getEventType() == ProductsUnpinned.class)
                .findFirst()
                .get();
        List<LiveContext.ProductPin> pins = liveContext.pins();
        shuffle(pins);
        LiveContext.ProductPin expected = pins.stream().findAny().get();

        // Act
        eventHandler.handleEvent(new ProductsUnpinned(liveContext.broadcast().key(), List.of(expected.productId())));

        // Assert
        LiveContext actual = contextStore.contexts.get(liveContext.broadcast().key());
        List<LiveContext.ProductPin> productPins = actual.pins();
        assertThat(productPins).doesNotContain(expected);
    }
}
