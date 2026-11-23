package com.example;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(5000);
        System.out.println("Server attivo sulla porta 5000...");

        while (true) {
            Socket client = server.accept();
            System.out.println("Nuovo client connesso!");

            Scanner in = new Scanner(client.getInputStream(), StandardCharsets.UTF_8);
            PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);

            String richiesta = "";
            if (in.hasNextLine()) {
                richiesta = in.nextLine();
            }
            System.out.println("Ricevuto: " + richiesta);

            richiesta = richiesta.trim();

            if (richiesta.isEmpty()) {
                out.println("ERR SYNTAX");
                out.println();
            } else {
                String[] parti = richiesta.split("\\s+");
                String comando = parti[0];

                if (!comando.equals("LEGGI")) {
                    out.println("ERR UNKNOWNCMD");
                    out.println();
                } else if (parti.length != 2 || !nomeValido(parti[1])) {
                    out.println("ERR SYNTAX");
                    out.println();
                } else {
                    File file = new File("documenti", parti[1]);

                    if (!file.exists() || !file.isFile()) {
                        out.println("ERR NOTFOUND");
                        out.println();
                    } else if (!file.canRead()) {
                        out.println("ERR READ");
                        out.println();
                    } else {
                        try (Scanner lettoreFile = new Scanner(file, StandardCharsets.UTF_8)) {
                            out.println("OK FILE");
                            out.println();

                            while (lettoreFile.hasNextLine()) {
                                out.println(lettoreFile.nextLine());
                            }
                        } catch (Exception e) {
                            out.println("ERR READ");
                            out.println();
                        }
                    }
                }
            }

            client.close();
        }
    }

    static boolean nomeValido(String nome) {
        if (nome.contains("/") || nome.contains("\\") || nome.contains("..")) {
            return false;
        }
        return nome.endsWith(".txt") && nome.length() > 4;
    }
}
