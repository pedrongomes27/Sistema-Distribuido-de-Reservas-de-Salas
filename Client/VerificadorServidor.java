package Client;

import java.io.IOException;
import Middleware.Middleware;

public class VerificadorServidor extends Thread {

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(500);
                Middleware.verificarServidorDisponivel();
            } catch (IOException e) {
                System.out.println("Servidor não está disponível. Tentando novamente...");
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
