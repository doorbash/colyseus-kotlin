package io.colyseus.example.java;

import io.colyseus.Client;
import io.colyseus.Room;
import kotlin.Unit;

import java.util.Scanner;

public class Main {
    public static void main(String... args) {
        Client client = new Client("ws://localhost:2567");
        client.joinOrCreate(MyState.class, "game", room -> {
            System.out.println("connected to " + room.getName());

            room.getState().players.setOnAdd((player, integer) -> {
                System.out.println("added player with x = " + player.x + " to index = " + integer);

                room.send("fire", "in the hole!");

                return Unit.INSTANCE;
            });

            room.getOnMessageHandlers().put("ahoy", new Room.MessageHandler<>(Object.class, s -> {
                System.out.println(s);

                return Unit.INSTANCE;
            }));

            return Unit.INSTANCE;
        }, e -> {
            e.printStackTrace();
            return Unit.INSTANCE;
        });

        new Scanner(System.in).nextLine();
    }
}