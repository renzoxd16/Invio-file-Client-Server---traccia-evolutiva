package com.example;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class SocketHandler {
    public static void main(String[] args) {
        try (Scanner tastiera = new Scanner(System.in)) {
            while (true) {
                System.out.print("\nInserisci il comando.");
                String comando = tastiera.nextLine().trim();

                if (comando.isEmpty()) {
                    System.out.println("ERR SYNTAX");
                    return;
                }

                try (
                    Socket socket = new Socket("localhost", 3000);
                    BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
                ) {
                    out.println(comando);

                    String stato = in.readLine();
                    if (stato == null) {
                        System.out.println("Errore: connessione interrotta dal server prima della risposta.");
                        return;
                    }

                    String separatore = in.readLine();
                    if (separatore == null || !separatore.isEmpty()) {
                        System.out.println("Errore: protocollo non rispettato dal server");
                        return;
                    }

                    if ("OK FILE".equals(stato)) {
                        String[] parti = comando.split("\\s+");
                        String nomeFile = "file";

                        if (parti.length > 1) {
                            nomeFile = parti[1];
                        }

                        System.out.println("Contenuto di " + nomeFile + " ");
                        String riga;
                        while ((riga = in.readLine()) != null) {
                            System.out.println(riga);
                        }
                        System.out.println(" ");
                        
                    } else {
                        stampaMessaggioErrore(stato);
                        return;
                    }

                } catch (IOException e) {
                    System.out.println("Errore di rete con il server: " + e.getMessage());
                    return;
                }
            }
        }
    }

    private static void stampaMessaggioErrore(String codice) {
        switch (codice) {
            case "ERR SYNTAX":
                System.out.println("ERR SYNTAX");
                break;
            case "ERR UNKNOWNCMD":
                System.out.println("ERR UNKNOWNCMD");
                break;
            case "ERR NOTFOUND":
                System.out.println("ERR NOTFOUND");
                break;
            case "ERR READ":
                System.out.println("Errore dal server (ERR READ): impossibile leggere il file sul server.");
                break;
            default:
                System.out.println("Errore imprevisto dal server: " + codice);
                break;
        }
    }
}

