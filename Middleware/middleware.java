package Middleware;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.Scanner;

import Client.VerificadorServidor;
import Client.Cliente;

public class Middleware {
    private static DatagramSocket multicastSocketClient = null;
    private static NetworkInterface networkInterface = null;
    private static InetAddress enderecoServidor = null;
    private static int portaServidor = -1;
    private static VerificadorServidor verificadorServidor = null;

    private static MulticastSocket multicastSocketServer = null;
    private static int portaCliente = -1;

    public static void inicializarMulticastSocket() throws IOException {
        multicastSocketClient = new DatagramSocket();
        multicastSocketClient.setSoTimeout(3000);
    }

    public static void enviarMensagemParaServidor(String mensagem) throws IOException {
        byte[] buffer = mensagem.getBytes();
        DatagramPacket pacote = new DatagramPacket(buffer, buffer.length, enderecoServidor, portaServidor);
        multicastSocketClient.send(pacote);
    }

    public static void receberRespostaDoServidor() throws IOException {
        byte[] bufferResposta = new byte[1024];
        DatagramPacket pacoteResposta = new DatagramPacket(bufferResposta, bufferResposta.length);
        multicastSocketClient.receive(pacoteResposta);
        String resposta = new String(pacoteResposta.getData(), 0, pacoteResposta.getLength());
        System.out.println(resposta);
    }

    public static boolean verificarServidorDisponivel()
            throws IOException {
        String mensagemSolicitacao = "IS_SERVER_ON";
        byte[] bufferSolicitacao = mensagemSolicitacao.getBytes();
        DatagramPacket pacoteSolicitacao = new DatagramPacket(bufferSolicitacao, bufferSolicitacao.length,
                enderecoServidor, portaServidor);

        try {
            multicastSocketClient.send(pacoteSolicitacao);

            byte[] bufferResposta = new byte[1024];
            DatagramPacket pacoteResposta = new DatagramPacket(bufferResposta, bufferResposta.length);

            multicastSocketClient.receive(pacoteResposta);
            String resposta = new String(pacoteResposta.getData(), 0, pacoteResposta.getLength());

            return resposta.equals("HEARTBEAT");
        } catch (IOException e) {
            return false;
        }

    }

    public static void localizarServidor() throws IOException, InterruptedException {
        InetAddress enderecoGrupo = InetAddress.getByName("239.10.10.11");
        // networkInterface = NetworkInterface.getByName("eth2"); // Substitua "eth2" ou
        // "wlan0"

        int[] portasServidores = { 1111, 2222, 3333 }; // Lista de portas dos servidores

        for (int multicastPort : portasServidores) {
            multicastSocketClient.joinGroup(new InetSocketAddress(enderecoGrupo, multicastPort), networkInterface);

            enderecoServidor = enderecoGrupo;
            portaServidor = multicastPort;
            boolean isOn = verificarServidorDisponivel();

            multicastSocketClient.leaveGroup(new InetSocketAddress(enderecoGrupo, multicastPort), networkInterface);

            if (isOn) {
                System.out.println("Servidor disponível no endereço: " + enderecoServidor.getHostAddress() + ", porta: "
                        + portaServidor + " (APENAS PARA FASE DEV)");
                break;

            } else {
                enderecoServidor = null;
                portaServidor = -1;
            }
        }

        if (enderecoServidor == null) {
            System.out.println("Nenhum servidor disponível. Tentando novamente...");
            localizarServidor();
        }
    }

    public static void iniciarVerificadorServidor() {
        verificadorServidor = new VerificadorServidor();
        verificadorServidor.start();
    }

    public static void encerrarVerificadorServidor() {
        if (verificadorServidor != null && verificadorServidor.isAlive()) {
            verificadorServidor.interrupt();
            try {
                verificadorServidor.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void iniciarCliente() {
        try {
            Middleware.inicializarMulticastSocket();
            Middleware.localizarServidor();
            Middleware.iniciarVerificadorServidor();

            Scanner scanner = new Scanner(System.in);
            while (true) {
                Cliente.exibirMenu();
                int opcao = Integer.parseInt(scanner.nextLine());
                Cliente.processarOpcao(opcao, scanner);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("---- | Erro inesperado! | ----");
            encerrarCliente();
        }
    }

    public static void encerrarCliente() {
        Middleware.multicastSocketClient.close();
        System.exit(0);
    }

    public static String receberMensagemDoCliente() {
        try {
            networkInterface = NetworkInterface.getByName("eth2"); // Substitua "eth2" ou "wlan0"
            enderecoServidor = InetAddress.getByName("239.10.10.11");
            ;
            int portaRunServer = 1111;

            multicastSocketServer = new MulticastSocket(null);
            multicastSocketServer.bind(new InetSocketAddress(portaRunServer));
            multicastSocketServer.joinGroup(new InetSocketAddress(enderecoServidor, portaRunServer), networkInterface);
            byte[] buffer = new byte[1024];

            System.out.println("Servidor Multicast iniciado. Aguardando mensagem...");

            int portaOutroServidor = 2222;
            InetSocketAddress enderecoDestino = new InetSocketAddress(enderecoServidor, portaOutroServidor);
            enviarMensagemServidorOnline(enderecoDestino);

            DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);

            multicastSocketServer.receive(pacote);
            String mensagemRecebida = new String(pacote.getData(), 0, pacote.getLength());
            System.out.println("Mensagem recebida: " + mensagemRecebida);

            portaCliente = pacote.getPort();

            String[] partesMensagem = mensagemRecebida.split(" ");
            String operacao = partesMensagem[0];

            if (mensagemRecebida.equals("SERVER_ONLINE")) {
                System.out.println("Recebida mensagem de servidor online do outro servidor.");
                // enviarDadosParaOutroServidor();
            } else if (operacao.equals("IS_SERVER_ON")) {
                String mensagem = "HEARTBEAT";
                enviarMensagemParaCliente(mensagem);
            } else if (operacao.equals("SAIR")) {
                multicastSocketServer.leaveGroup(new InetSocketAddress(enderecoServidor, portaRunServer),
                        networkInterface);
                multicastSocketServer.close();
                System.out.println("Servidor encerrado.");
            } else {
                return mensagemRecebida;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "null";
        }
        return "null";

    }

    public static void enviarMensagemParaCliente(String mensagem) {

        byte[] resposta = mensagem.getBytes();
        DatagramPacket pacote = new DatagramPacket(resposta, resposta.length, enderecoServidor, portaCliente);

        // Inicia uma nova thread para enviar a resposta ao cliente
        // Thread enviarThread = new Thread(() -> {
        try {
            multicastSocketServer.send(pacote);
            multicastSocketServer.close();
            System.out.println("Resposta enviada para o cliente.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        // });

        // enviarThread.start();

    }

    private static void enviarMensagemServidorOnline(InetSocketAddress enderecoDestino) {
        try {
            String mensagem = "SERVER_ONLINE";
            byte[] buffer = mensagem.getBytes();
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket pacote = new DatagramPacket(buffer, buffer.length, enderecoDestino);
            socket.send(pacote);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // private static void enviarDadosParaOutroServidor() {
    // // Implemente aqui a lógica para enviar dados para outro servidor
    // }

    // public String receberMensagemDoCliente(DatagramSocket multicastSocketClient,
    // byte[]
    // buffer) throws IOException {
    // DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
    // multicastSocket.receive(pacote);
    // String mensagemRecebida = new String(pacote.getData(), 0,
    // pacote.getLength());
    // System.out.println("Mensagem recebida: " + mensagemRecebida);
    // return mensagemRecebida;
    // }

}
