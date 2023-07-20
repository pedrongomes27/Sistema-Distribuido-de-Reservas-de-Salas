import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ClienteHandler extends Thread {
    private DatagramSocket socket;
    private InetAddress enderecoCliente;
    private int portaCliente;

    public ClienteHandler(DatagramSocket socket, InetAddress enderecoCliente, int portaCliente) {
        this.socket = socket;
        this.enderecoCliente = enderecoCliente;
        this.portaCliente = portaCliente;
    }

    public void run() {
        try {
            byte[] buffer = new byte[1024];
            DatagramPacket pacote = new DatagramPacket(buffer, buffer.length, enderecoCliente, portaCliente);
            socket.receive(pacote);
            String mensagemRecebida = new String(pacote.getData(), 0, pacote.getLength());
            System.out.println("Mensagem recebida do cliente: " + mensagemRecebida);

            // Processar a mensagem recebida e responder ao cliente

            // Exemplo de resposta:
            String resposta = "Olá cliente, sua mensagem foi recebida pelo servidor.";
            byte[] bufferResposta = resposta.getBytes();
            DatagramPacket pacoteResposta = new DatagramPacket(bufferResposta, bufferResposta.length, enderecoCliente,
                    portaCliente);
            socket.send(pacoteResposta);

            // Fechar o soquete
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
