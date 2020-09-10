package io.colyseus.example.java;

import io.colyseus.Client;
import io.colyseus.Room;
import io.colyseus.serializer.schema.Change;
import io.colyseus.serializer.schema.Schema;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.AbstractCoroutine;
import kotlinx.coroutines.CancellableContinuation;
import kotlinx.coroutines.CoroutineDispatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Main {
    public static void main(String... args) {
        Client client = new Client("ws://localhost:2567");
        client.joinOrCreate("game",
                MyState.class,
                null,
                null,
                null,
                new Continuation<Room<MyState>>() {
                    @NotNull
                    @Override
                    public CoroutineContext getContext() {
                        return null;
                    }

                    @Override
                    public void resumeWith(@NotNull Object o) {
                        Room<MyState> room = (Room<MyState>) o;
                        System.out.println("connected to " + room.getName());

                        room.getState().players.setOnAdd((player, integer) -> {
                            System.out.println("added player with x = " + player.x + " to index = " + integer);

                            room.send("fire", "in the hole!");
                            return Unit.INSTANCE;
                        });

                        room.getOnMessageHandlers().put("hello", new Room.MessageHandler<>(Object.class, s -> {
                            System.out.println(s);
                            return Unit.INSTANCE;
                        }));
                    }
                });
    }
}
