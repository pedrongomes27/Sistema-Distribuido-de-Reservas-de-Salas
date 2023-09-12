package Middleware;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

import Client.VerificadorServidor;
import Client.Cliente;

public class Middleware {
    private static DatagramSocket multicastSocketClient = null;
    private static NetworkInterface networkInterface = null;
    private static InetAddress enderecoServidor = null;
    private static int portaServidor = -1;
    private static VerificadorServidor verificadorServidor = null;
    private static int[] portasServidores = { 1111, 2222 };
    private static int[] connectionCount = { 0, 0 };
    private static Map<Integer, Integer> serverClientCounts = new HashMap<>();

    private static MulticastSocket multicastSocketServer = null;
    private static InetSocketAddress enderecoCliente = null;

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
        if (!resposta.equals("HEARTBEAT")) {
            System.out.println(resposta);
        }
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

    // public static void localizarServidor() throws IOException, InterruptedException {
    //     InetAddress enderecoGrupo = InetAddress.getByName("239.10.10.11");

    //     // int[] portasServidores = { 1111, 2222 };

    //     // int[] connectionCount = { 0, 0 }; // {(COUNT SERVER1), (COUNT SERVER2)}

    //     for (int multicastPort : portasServidores) {
    //         multicastSocketClient.joinGroup(new InetSocketAddress(enderecoGrupo, multicastPort), networkInterface);

    //         enderecoServidor = enderecoGrupo;
    //         portaServidor = multicastPort;
    //         boolean isOn = verificarServidorDisponivel();

    //         multicastSocketClient.leaveGroup(new InetSocketAddress(enderecoGrupo, multicastPort), networkInterface);

    //         if (isOn) {
    //             // if(multicastPort < ){
    //             System.out.println("Servidor disponível no endereço: " + enderecoServidor.getHostAddress() + ", porta: "
    //                     + portaServidor + " (APENAS PARA FASE DEV)");
    //             // connectionCount;
    //             break;
    //             // }

    //         } else {
    //             enderecoServidor = null;
    //             portaServidor = -1;
    //         }
    //     }

    //     if (enderecoServidor == null) {
    //         System.out.println("Nenhum servidor disponível. Tentando novamente...");
    //         localizarServidor();
    //     }
    // }

    public static void localizarServidor() throws IOException, InterruptedException{
     InetAddress enderecoGrupo = InetAddress.getByName("239.10.10.11");
     int selectedServerPort = -1;
     int minClientCount = Integer.MAX_VALUE;

     
    
        // Loop through the available servers
        for (int multicastPort : portasServidores) {
            // Join the multicast group and check server availability
            multicastSocketClient.joinGroup(new InetSocketAddress(enderecoGrupo, multicastPort), networkInterface);
            enderecoServidor = enderecoGrupo;
            portaServidor = multicastPort;
            boolean isOn = verificarServidorDisponivel();
            multicastSocketClient.leaveGroup(new InetSocketAddress(enderecoGrupo, multicastPort), networkInterface);
            
            if (isOn) {
                // Update the client count for this server
                int clientCount = serverClientCounts.getOrDefault(multicastPort, 0);
                serverClientCounts.put(multicastPort, clientCount + 1);
                
                System.out.println("Servidor disponível no endereço: " + enderecoServidor.getHostAddress() +
                                   ", porta: " + portaServidor + " (APENAS PARA FASE DEV)");
                
                // Connect to this server and break the loop
                break;
            } else {
                enderecoServidor = null;
                portaServidor = -1;
            }
        }

        if (enderecoServidor == null) {
            System.out.println("Nenhum servidor disponível. Tentando novamente...");
            localizarServidor();
        } else {
            // Determine which server has the least clients and connect to it
            System.out.println(serverClientCounts);
            
            for (Map.Entry<Integer, Integer> entry : serverClientCounts.entrySet()) {
                if (entry.getValue() < minClientCount) {
                    minClientCount = entry.getValue();
                    System.out.println(minClientCount);
                    selectedServerPort = entry.getKey();
                    System.out.println(selectedServerPort);
                }
            }
            
            System.out.println("Connecting to the server with the least clients (Port: " + selectedServerPort + ")");
            portaServidor = selectedServerPort;
            System.out.println(portaServidor);
            // Add your code to connect to the selected server here
        }
    }
    

    // public static void verificarQntdConexao() throws IOException {
    //     InetAddress enderecoGrupo = InetAddress.getByName("239.10.10.11");

    //     for (int multicastPort : portasServidores) {
    //         multicastSocketClient.joinGroup(new InetSocketAddress(enderecoGrupo, multicastPort), networkInterface);

    //         enderecoServidor = enderecoGrupo;
    //         portaServidor = multicastPort;
    //         boolean isOn = verificarServidorDisponivel();

    //         multicastSocketClient.leaveGroup(new InetSocketAddress(enderecoGrupo, multicastPort), networkInterface);

    //         if (isOn) {
    //             connectionCount[multicastPort];
    //         } else {
    //             enderecoServidor = null;
    //             portaServidor = -1;
    //         }
    //     }
    // }

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

    public static String getConnectionType() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.isUp()) {
                    Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress address = addresses.nextElement();
                        if (!address.isLoopbackAddress()) {
                            if (networkInterface.getName().startsWith("eth")
                                    || networkInterface.getName().startsWith("en")) {
                                return "eth2";
                            } else if (networkInterface.getName().startsWith("wlan")) {
                                return "wlan0";
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "Desconhecido";
    }

    public static void definirServidor(int porta) {
        try {
            String connectionType = getConnectionType();
            networkInterface = NetworkInterface.getByName(connectionType); // Substitua "eth2" ou "wlan0"
            enderecoServidor = InetAddress.getByName("239.10.10.11");

            multicastSocketServer = new MulticastSocket(null);
            multicastSocketServer.bind(new InetSocketAddress(porta));
            multicastSocketServer.joinGroup(new InetSocketAddress(enderecoServidor, porta), networkInterface);

            enviarMensagemEntreServidor("SERVER_ONLINE", porta);

            System.out.println("Servidor Multicast iniciado na porta " + porta + ". Aguardando mensagem...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DatagramPacket receberMensagemDoCliente() {
        try {
            byte[] buffer = new byte[1024];

            DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);

            multicastSocketServer.receive(pacote);
            String mensagemRecebida = new String(pacote.getData(), 0, pacote.getLength());

            enderecoCliente = new InetSocketAddress(pacote.getAddress(), pacote.getPort());

            if (mensagemRecebida.equals("IS_SERVER_ON")) {
                String mensagem = "HEARTBEAT";
                enviarMensagemParaCliente(mensagem);
            }
            return pacote;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void enviarMensagemParaCliente(String mensagem) {
        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] buffer = mensagem.getBytes();
            DatagramPacket pacote = new DatagramPacket(buffer, buffer.length, enderecoCliente.getAddress(),
                    enderecoCliente.getPort());

            try {
                socket.send(pacote);
                socket.close();
                System.out.println("Resposta enviada para o cliente.");
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void enviarMensagemEntreServidor(String mensagem, int porta) {
        try {
            byte[] buffer = mensagem.getBytes();
            DatagramSocket socket = new DatagramSocket();

            if (porta == 1111) {
                DatagramPacket pacote = new DatagramPacket(buffer, buffer.length,
                        new InetSocketAddress(enderecoServidor, 2222));
                socket.send(pacote);

            } else if (porta == 2222) {
                DatagramPacket pacote = new DatagramPacket(buffer, buffer.length,
                        new InetSocketAddress(enderecoServidor, 1111));
                socket.send(pacote);
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
