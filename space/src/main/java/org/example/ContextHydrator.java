package shoplive.liveagent;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shoplive.liveagent.command.ApplyPinnedProducts;
import shoplive.liveagent.command.ApplyTranscribedUtterance;
import shoplive.liveagent.command.ApplyUnpinnedProducts;

public class ContextHydrator {

    private final ContextStore contextStore;

    public ContextHydrator(ContextStore contextStore) {
        if (contextStore == null) {
            throw new IllegalArgumentException("The argument 'contextStore' must not be null");
        }

        this.contextStore = contextStore;
    }

    public void handleCommand(ApplyPinnedProducts command) {
        if (command == null) {
            throw new IllegalArgumentException("The argument 'command' must not be null");
        }
        contextStore.reviseContextIfExists(
            command.broadcastKey(), context -> reviseContextByUpdatingPins(context, command)
        );
    }

    private LiveContext reviseContextByUpdatingPins(LiveContext original, ApplyPinnedProducts command) {
        Map<String, LiveContext.ProductPin> contextPins = HashMap.newHashMap(original.pins().size() + command.pinnedProductIds().size());
        original.pins().forEach(pin -> contextPins.put(pin.productId(), pin));

        command.pinnedProductIds().stream()
            .map(id -> createProductPin(id, command.pinnedTime()))
            .forEach(pin -> contextPins.put(pin.productId(), pin));
        List<LiveContext.ProductPin> newPins = List.copyOf(contextPins.values());

        return new LiveContext(
            original.productDetailsScraped(),
            original.broadcast(),
            original.products(),
            newPins,
            original.transcribedUtterances()
        );
    }

    public void handleCommand(ApplyUnpinnedProducts command) {
        if (command == null) {
            throw new IllegalArgumentException("The argument 'command' must not be null");
        }
        contextStore.reviseContextIfExists(
            command.broadcastKey(), context -> reviseContextByRemovingPins(context, command)
        );
    }

    private LiveContext reviseContextByRemovingPins(LiveContext original, ApplyUnpinnedProducts command) {
        Map<String, LiveContext.ProductPin> contextPins = HashMap.newHashMap(original.pins().size());
        original.pins().forEach(pin -> contextPins.put(pin.productId(), pin));
        command.unpinnedProductIds().forEach(contextPins::remove);
        List<LiveContext.ProductPin> newPins = List.copyOf(contextPins.values());
        return new LiveContext(
            original.productDetailsScraped(),
            original.broadcast(),
            original.products(),
            newPins,
            original.transcribedUtterances()
        );
    }

    public void handleCommand(ApplyTranscribedUtterance command) {
        if (command == null) {
            throw new IllegalArgumentException("The argument 'command' must not be null");
        }
        contextStore.reviseContextIfExists(
            command.broadcastKey(), context -> reviseContextByAddingTranscription(context, command)
        );
    }

    private LiveContext reviseContextByAddingTranscription(LiveContext original, ApplyTranscribedUtterance command) {
        List<LiveContext.TranscribedUtterance> newUtterances = new ArrayList<>(original.transcribedUtterances());
        newUtterances.add(new LiveContext.TranscribedUtterance(command.text(), command.timeWindow(), command.transcribedTime()));
        return new LiveContext(
            original.productDetailsScraped(),
            original.broadcast(),
            original.products(),
            original.pins(),
            newUtterances
        );
    }

    private static LiveContext.ProductPin createProductPin(String productId, ZonedDateTime pinnedTime) {
        return new LiveContext.ProductPin(productId, pinnedTime);
    }
}
