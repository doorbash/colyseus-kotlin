package io.colyseus.state_listener;

import io.colyseus.Room;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StateContainer {

    /**
     * The current room's state. This variable is always synced with the latest state from the server-side.
     * To listen for updates on the whole state, see {@link Room.Listener#onStateChange(LinkedHashMap)} event.
     */
    public LinkedHashMap<String, Object> state;
    private List<PatchListener> _listeners;
    private FallbackPatchListener defaultListener;

    private LinkedHashMap<String, Pattern> matcherPlaceholders = new LinkedHashMap<>();

    public StateContainer(LinkedHashMap<String, Object> state) {
        this.matcherPlaceholders.put(":id", Pattern.compile("^([a-zA-Z0-9\\-_]+)$"));
        this.matcherPlaceholders.put(":number", Pattern.compile("^([0-9]+)$"));
        this.matcherPlaceholders.put(":string", Pattern.compile("^(\\w+)$"));
        this.matcherPlaceholders.put(":axis", Pattern.compile("^([xyz])$"));
        this.matcherPlaceholders.put(":*", Pattern.compile("(.*)"));

        this.state = state;
        this.reset();
    }

    protected List<PatchObject> set(LinkedHashMap<String, Object> newData) {
        List<PatchObject> patches = Compare.compare(this.state, newData);
        this.checkPatches(patches);
        this.state = newData;
        return patches;
    }

    public void registerPlaceholder(String placeholder, Pattern regex) {
        this.matcherPlaceholders.put(placeholder, regex);
    }

    public FallbackPatchListener setDefaultPatchListener(FallbackPatchListenerCallback callback) {
        FallbackPatchListener listener = new FallbackPatchListener(callback, new Pattern[]{});
        this.defaultListener = listener;
        return listener;
    }

    public PatchListener addPatchListener(String segments, PatchListenerCallback callback) {
        String[] rawRules = segments.split("/");
        Pattern[] regexpRules = this.parseRegexRules(rawRules);
        PatchListener listener = new PatchListener(callback, regexpRules, rawRules);
        this._listeners.add(listener);
        return listener;
    }

    public void removePatchListener(PatchListener listener) {
        this._listeners.remove(listener);
    }

    public void removeAllListeners() {
        this.reset();
    }

    private Pattern[] parseRegexRules(String[] rules) {
        Pattern[] regexpRules = new Pattern[rules.length];

        for (int i = 0; i < rules.length; i++) {
            String segment = rules[i];
            if (segment.indexOf(':') == 0) {
                if (this.matcherPlaceholders.containsKey(segment)) {
                    regexpRules[i] = this.matcherPlaceholders.get(segment);
                } else {
                    regexpRules[i] = this.matcherPlaceholders.get(":*");
                }
            } else {
                regexpRules[i] = Pattern.compile("^" + segment + "$");
            }
        }

        return regexpRules;
    }

    private void checkPatches(List<PatchObject> patches) {

        for (int i = patches.size() - 1; i >= 0; i--) {
            boolean matched = false;

            for (PatchListener listener : this._listeners) {
                LinkedHashMap<String, String> pathVariables = this.getPathVariables(patches.get(i), listener);
                if (pathVariables != null) {
                    DataChange change = new DataChange();
                    change.path = pathVariables;
                    change.operation = patches.get(i).operation;
                    change.value = patches.get(i).value;

                    listener.callback.callback(change);
                    matched = true;
                }
            }

            // check for fallback listener
            if (!matched && defaultListener != null) {
                this.defaultListener.callback.callback(patches.get(i));
            }

        }

    }

    private LinkedHashMap<String, String> getPathVariables(PatchObject patch, PatchListener listener) {
        LinkedHashMap result = new LinkedHashMap<String, String>();

        // skip if rules count differ from patch
        if (patch.path.size() != listener.rules.length) {
            return null;
        }

        for (int i = 0; i < listener.rules.length; i++) {
            Matcher matcher = listener.rules[i].matcher(patch.path.get(i));
            List<String> matches = new ArrayList<>();
            while (matcher.find()) {
                matches.add(matcher.group());
            }
            if (matches.size() == 0 || matches.size() > 2) {
                return null;

            } else if (listener.rawRules[i].charAt(0) == ':') {
                result.put(listener.rawRules[i].substring(1), matches.get(0));
            }
        }

        return result;
    }

    private void reset() {
        this._listeners = new ArrayList<>();
        this.defaultListener = null;
    }
}
