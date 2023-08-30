package Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class NetworkUtils {
    public static void enviarMensagem(String mensagem, DatagramSocket socket, InetAddress servidorInfo,
                                      int portaServidor) throws IOException {
        byte[] buffer = mensagem.getBytes();
        DatagramPacket pacote = new DatagramPacket(buffer, buffer.length, servidorInfo, portaServidor);
        socket.send(pacote);
    }

    public static void receberResposta(DatagramSocket socket) throws IOException {
        byte[] bufferResposta = new byte[1024];
        DatagramPacket pacoteResposta = new DatagramPacket(bufferResposta, bufferResposta.length);
        socket.receive(pacoteResposta);
        String resposta = new String(pacoteResposta.getData(), 0, pacoteResposta.getLength());
        System.out.println(resposta);
    }

    public static boolean verificarServidorDisponivel(DatagramSocket socket, InetAddress grupo, int portaServidor)
            throws IOException {
        String mensagemSolicitacao = "IS_SERVER_ON";
        byte[] bufferSolicitacao = mensagemSolicitacao.getBytes();
        DatagramPacket pacoteSolicitacao = new DatagramPacket(bufferSolicitacao, bufferSolicitacao.length, grupo,
                portaServidor);

        try {
            socket.send(pacoteSolicitacao);

            byte[] bufferResposta = new byte[1024];
            DatagramPacket pacoteResposta = new DatagramPacket(bufferResposta, bufferResposta.length);

            socket.receive(pacoteResposta);
            String resposta = new String(pacoteResposta.getData(), 0, pacoteResposta.getLength());

            return resposta.equals("HEARTBEAT");
        } catch (IOException e) {
            return false;
        }
    }
}
