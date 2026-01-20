package shoplive.liveagent;

import shoplive.liveagent.command.ApplyPinnedProducts;
import shoplive.liveagent.command.ApplyTranscribedUtterance;
import shoplive.liveagent.command.ApplyUnpinnedProducts;
import shoplive.liveagent.event.ProductsPinned;
import shoplive.liveagent.event.ProductsUnpinned;
import shoplive.liveagent.event.UtteranceTranscribed;

public class ContextEventAdapter {

    public ApplyTranscribedUtterance buildCommand(UtteranceTranscribed event) {
        if (event == null) {
            throw new IllegalArgumentException("The argument 'event' must not be null");
        }

        return new ApplyTranscribedUtterance(
            event.broadcastKey(),
            event.text(),
            event.timeWindow(),
            event.transcribedTime()
        );
    }

    public ApplyPinnedProducts buildCommand(ProductsPinned event) {
        if (event == null) {
            throw new IllegalArgumentException("The argument 'event' must not be null");
        }

        return new ApplyPinnedProducts(
            event.broadcastKey(),
            event.pinnedProductIds(),
            event.pinnedTime()
        );
    }

    public ApplyUnpinnedProducts buildCommand(ProductsUnpinned event) {
        if (event == null) {
            throw new IllegalArgumentException("The argument 'event' must not be null");
        }

        return new ApplyUnpinnedProducts(event.broadcastKey(), event.unpinnedProductIds());
    }
}
