package io.colyseus.example.java;

import io.colyseus.Client;
import io.colyseus.example.java.classes.Cell;
import io.colyseus.example.java.classes.MyState;
import io.colyseus.example.java.classes.PrimitivesTest;

import java.util.Scanner;

public class Main {
    public static void main(String... args) {
        Client client = new Client("ws://localhost:2567");
        client.joinOrCreate(MyState.class, "game", room -> {
            System.out.println("connected to " + room.getName());

            room.getState().players.setOnAdd((player, integer) -> {
                System.out.println("added player with x = " + player.x + " to index = " + integer);

                room.send("fire", "in the hole!");
            });

            room.setOnLeave(code -> {
                System.out.println("onLeave(" + code + ")");
            });

            room.setOnError((reason, message) -> {
                System.out.println("onError(" + reason + ", " + message + ")");
            });

            room.getState().primitives.setOnChange(changes -> {
                changes.forEach(change -> {
                    System.out.println(change.getField() + ": " + change.getPreviousValue() + " -> " + change.getValue());
                });
            });

            room.setOnStateChange((state, isFirstState) -> {
                System.out.println("onStateChange()");
                System.out.println("state.primitives._boolean = " + state.primitives._boolean);
                System.out.println(isFirstState);
            });

            room.onMessage("sup", Cell.class, cell -> {
                System.out.println("cell.x = " + cell.x + ", cell.y = " + cell.y);
            });

            room.onMessage(2, Cell.class, cell -> {
                System.out.println("cell.x = " + cell.x + ", cell.y = " + cell.y);
            });

            room.onMessage(PrimitivesTest.class, primitives -> {
                System.out.println("primitives._boolean = " + primitives._boolean);
            });

            room.onMessage(Cell.class, cell -> {
                System.out.println(" ::::::: cell.x = " + cell.x);
            });

            room.onMessage("hello", Float.class, random -> {
                System.out.println("random = " + random);
            });

        }, e -> {
            e.printStackTrace();
        });

        new Scanner(System.in).nextLine();
    }
}