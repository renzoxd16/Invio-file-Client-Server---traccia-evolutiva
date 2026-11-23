package com.example;


import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MioThread extends Thread {

    private Socket s;

    public MioThread(Socket s) {
        this.s = s;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(
                new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)
        ) {
            String richiesta = in.readLine();

            if (richiesta == null) {
                return;
            }

            if (richiesta.trim().isEmpty()) {
                inviaErrore(out, "ERR SYNTAX");
                return;
            }

            String[] parti = richiesta.trim().split("\\s+");

            if (!parti[0].equals("LEGGI")) {
                inviaErrore(out, "ERR UNKNOWNCMD");
                return;
            }

            if (parti.length != 2 || !nomeFileValido(parti[1])) {
                inviaErrore(out, "ERR SYNTAX");
                return;
            }

            String nomeFile = parti[1];
            File cartella = new File("documenti");
            File file = new File(cartella, nomeFile);

            System.out.println("Richiesto file: " + file.getAbsolutePath());

            if (!file.isFile()) {
                inviaErrore(out, "ERR NOTFOUND");
                return;
            }

            boolean statoInviato = false;
            try (BufferedReader lettoreFile = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

                out.println("OK FILE");
                out.println(); 
                statoInviato = true;

                String riga;
                while ((riga = lettoreFile.readLine()) != null) {
                    out.println(riga);
                }

            } catch (IOException e) {
                if (!statoInviato) {
                    inviaErrore(out, "ERR READ");
                } else {
                    System.out.println("Errore di lettura durante l'invio di " + nomeFile + ": " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.out.println("Errore di comunicazione con il client: " + e.getMessage());
        } finally {
            try {
                s.close(); 
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void inviaErrore(PrintWriter out, String codiceErrore) {
        out.println(codiceErrore);
        out.println(); 
    }

    /**
     * @param nome
     * @return
     */
    private boolean nomeFileValido(String nome) {
        if (!nome.endsWith(".txt") || nome.equals(".txt")) {
            return false;
        }
        if (nome.contains("/") || nome.contains("\\") || nome.contains("..")) {
            return false;
        }
        return true;
    }
}