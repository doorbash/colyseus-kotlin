package io.colyseus.state_listener;

import java.util.regex.Pattern;

class FallbackPatchListener {
    FallbackPatchListenerCallback callback;
    Pattern[] rules;
    String[] rawRules;

    FallbackPatchListener(FallbackPatchListenerCallback callback, Pattern[] rules) {
        this.callback = callback;
        this.rules = rules;
    }
}
