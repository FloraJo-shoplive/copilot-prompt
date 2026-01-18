package shoplive.liveagent;

import autoparams.AutoParams;
import org.junit.jupiter.api.Test;
import shoplive.liveagent.command.ApplyPinnedProducts;
import shoplive.liveagent.command.ApplyTranscribedUtterance;
import shoplive.liveagent.command.ApplyUnpinnedProducts;
import shoplive.liveagent.event.ProductsPinned;
import shoplive.liveagent.event.ProductsUnpinned;
import shoplive.liveagent.event.UtteranceTranscribed;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class ContextEventAdapter_specs {

    @Test
    public void buildCommand는_UtteranceTranscribed_형식_매개변수에_대한_null_방어구문을_갖는다() {
        // Arrange
        ContextEventAdapter sut = new ContextEventAdapter();

        // Act & Assert
        assertThatThrownBy(() -> sut.buildCommand((UtteranceTranscribed) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("The argument 'event' must not be null");
    }

    @Test
    @AutoParams
    public void buildCommand는_UtteranceTranscribed_이벤트를_명령으로_올바르게_변환한다(
        UtteranceTranscribed event
    ) {
        // Arrange
        ContextEventAdapter sut = new ContextEventAdapter();

        // Act
        ApplyTranscribedUtterance actual = sut.buildCommand(event);

        // Assert
        assertThat(actual).isNotNull();
        assertThat(actual.broadcastKey()).isEqualTo(event.broadcastKey());
        assertThat(actual.text()).isEqualTo(event.text());
        assertThat(actual.transcribedTime()).isEqualTo(event.transcribedTime());
        assertThat(actual.timeWindow()).isEqualTo(event.timeWindow());
    }

    @Test
    public void buildCommand는_ProductsPinned_형식_매개변수에_대한_null_방어구문을_갖는다() {
        // Arrange
        ContextEventAdapter sut = new ContextEventAdapter();

        // Act & Assert
        assertThatThrownBy(() -> sut.buildCommand((ProductsPinned) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("The argument 'event' must not be null");
    }

    @Test
    @AutoParams
    public void buildCommand는_ProductsPinned_이벤트를_명령으로_올바르게_변환한다(
        ProductsPinned event
    ) {
        // Arrange
        ContextEventAdapter sut = new ContextEventAdapter();

        // Act
        ApplyPinnedProducts actual = sut.buildCommand(event);

        // Assert
        assertThat(actual).isNotNull();
        assertThat(actual.broadcastKey()).isEqualTo(event.broadcastKey());
        assertThat(actual.pinnedProductIds()).isEqualTo(event.pinnedProductIds());
        assertThat(actual.pinnedTime()).isEqualTo(event.pinnedTime());
    }

    @Test
    public void buildCommand는_ProductsUnpinned_형식_매개변수에_대한_null_방어구문을_갖는다() {
        // Arrange
        ContextEventAdapter sut = new ContextEventAdapter();

        // Act & Assert
        assertThatThrownBy(() -> sut.buildCommand((ProductsUnpinned) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("The argument 'event' must not be null");
    }

    @Test
    @AutoParams
    public void buildCommand는_ProductsUnpinned_이벤트를_명령으로_올바르게_변환한다(
        ProductsUnpinned event
    ) {
        // Arrange
        ContextEventAdapter sut = new ContextEventAdapter();

        // Act
        ApplyUnpinnedProducts actual = sut.buildCommand(event);

        // Assert
        assertThat(actual).isNotNull();
        assertThat(actual.broadcastKey()).isEqualTo(event.broadcastKey());
        assertThat(actual.unpinnedProductIds()).isEqualTo(event.unpinnedProductIds());
    }
}
