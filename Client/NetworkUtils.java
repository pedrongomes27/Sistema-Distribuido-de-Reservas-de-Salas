package Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import Server.Servidor;

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

    public static Servidor verificarServidorDisponivel(DatagramSocket socket, InetAddress enderecoServidor,
                                                       int portaServidor) throws IOException {
        String mensagemSolicitacao = "heartbeat";
        byte[] bufferSolicitacao = mensagemSolicitacao.getBytes();
        DatagramPacket pacoteSolicitacao = new DatagramPacket(bufferSolicitacao, bufferSolicitacao.length,
                enderecoServidor, portaServidor);

        try {
            socket.send(pacoteSolicitacao);

            byte[] bufferResposta = new byte[1024];
            DatagramPacket pacoteResposta = new DatagramPacket(bufferResposta, bufferResposta.length);

            socket.receive(pacoteResposta);
            String resposta = new String(pacoteResposta.getData(), 0, pacoteResposta.getLength());

            if (resposta.equals("heartbeat")) {
                return new Servidor(enderecoServidor, portaServidor);
            }

        } catch (IOException e) {
            return null;
        }

        return null;
    }
}
