package io.colyseus.state_listener;

import java.util.regex.Pattern;

class PatchListener {
    PatchListenerCallback callback;
    Pattern[] rules;
    String[] rawRules;
    PatchListener(PatchListenerCallback callback, Pattern[] rules, String[] rawRules) {
        this.callback = callback;
        this.rules = rules;
        this.rawRules = rawRules;
    }
}
