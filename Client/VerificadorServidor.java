package Client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class VerificadorServidor extends Thread {
    private DatagramSocket socket;
    private InetAddress enderecoServidor;
    private int portaServidor;

    public VerificadorServidor(DatagramSocket socket, InetAddress enderecoServidor, int portaServidor) {
        this.socket = socket;
        this.enderecoServidor = enderecoServidor;
        this.portaServidor = portaServidor;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(1000);
                NetworkUtils.verificarServidorDisponivel(socket, enderecoServidor, portaServidor);
            } catch (IOException e) {
                System.out.println("Servidor não está disponível. Tentando novamente...");
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
