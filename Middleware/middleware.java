package Middleware;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
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
    // private static Map<Integer, Integer> serverClientCounts;

    private static MulticastSocket multicastSocketServer = null;
    private static InetSocketAddress enderecoCliente = null;
    private static Map<Integer, Map<String, Integer>> serverPorts = new HashMap<>();
    private static List<Integer> clientPorts = new ArrayList<>();

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

    public static void localizarServidor() throws IOException,
            InterruptedException {
        InetAddress enderecoGrupo = InetAddress.getByName("239.10.10.11");

        for (int multicastPort : portasServidores) {
            multicastSocketClient.joinGroup(new InetSocketAddress(enderecoGrupo,
                    multicastPort), networkInterface);

            enderecoServidor = enderecoGrupo;
            portaServidor = multicastPort;
            boolean isOn = verificarServidorDisponivel();

            multicastSocketClient.leaveGroup(new InetSocketAddress(enderecoGrupo,
                    multicastPort), networkInterface);

            if (isOn) {
                // if(multicastPort < ){
                System.out.println("Servidor disponível no endereço: " +
                        enderecoServidor.getHostAddress() + ", porta: "
                        + portaServidor + " (APENAS PARA FASE DEV)");
                // connectionCount;
                break;
                // }

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

    // public static void localizarServidor() throws IOException,
    // InterruptedException {
    // InetAddress enderecoGrupo = InetAddress.getByName("239.10.10.11");
    // int selectedServerPort = -1;
    // int minClientCount = Integer.MAX_VALUE;

    // for (int multicastPort : portasServidores) {
    // multicastSocketClient.joinGroup(new InetSocketAddress(enderecoGrupo,
    // multicastPort), networkInterface);
    // enderecoServidor = enderecoGrupo;
    // portaServidor = multicastPort;
    // boolean isOn = verificarServidorDisponivel();
    // multicastSocketClient.leaveGroup(new InetSocketAddress(enderecoGrupo,
    // multicastPort), networkInterface);

    // if (isOn) {
    // // Use the serverClientCounts variable to get the client count

    // SharedData.incrementClientCount(multicastPort);
    // serverClientCounts = SharedData.getClientCount(multicastPort);

    // System.out.println("Servidor disponível no endereço: " +
    // enderecoServidor.getHostAddress() +
    // ", porta: " + portaServidor + " (APENAS PARA FASE DEV)");

    // // Connect to this server and break the loop
    // break;
    // } else {
    // enderecoServidor = null;
    // portaServidor = -1;
    // }
    // }

    // if (enderecoServidor == null) {
    // System.out.println("Nenhum servidor disponível. Tentando novamente...");
    // localizarServidor();
    // } else {
    // // Determine which server has the least clients and connect to it
    // // System.out.println(serverClientCounts);

    // for (int i = 0; i < portasServidores.length; i++) {
    // if (portasServidores[i] == portaServidor) {
    // minClientCount = connectionCount[i];
    // selectedServerPort = portaServidor;
    // continue;
    // }
    // if (connectionCount[i] < minClientCount) {
    // minClientCount = connectionCount[i];
    // selectedServerPort = portasServidores[i];
    // }
    // }

    // System.out.println("Connecting to the server with the least clients (Port: "
    // + selectedServerPort + ")");
    // portaServidor = selectedServerPort;
    // System.out.println(portaServidor);
    // // Add your code to connect to the selected server here
    // }
    // }

    // public static void verificarQntdConexao() throws IOException {
    // InetAddress enderecoGrupo = InetAddress.getByName("239.10.10.11");

    // for (int multicastPort : portasServidores) {
    // multicastSocketClient.joinGroup(new InetSocketAddress(enderecoGrupo,
    // multicastPort), networkInterface);

    // enderecoServidor = enderecoGrupo;
    // portaServidor = multicastPort;
    // boolean isOn = verificarServidorDisponivel();

    // multicastSocketClient.leaveGroup(new InetSocketAddress(enderecoGrupo,
    // multicastPort), networkInterface);

    // if (isOn) {
    // connectionCount[multicastPort];
    // } else {
    // enderecoServidor = null;
    // portaServidor = -1;
    // }
    // }
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
        multicastSocketClient.close();

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

    // public static DatagramPacket receberMensagemDoCliente() {
    // try {
    // byte[] buffer = new byte[1024];

    // DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);

    // multicastSocketServer.receive(pacote);
    // String mensagemRecebida = new String(pacote.getData(), 0,
    // pacote.getLength());

    // enderecoCliente = new InetSocketAddress(pacote.getAddress(),
    // pacote.getPort());

    // if (mensagemRecebida.equals("IS_SERVER_ON")) {
    // String mensagem = "HEARTBEAT";
    // enviarMensagemParaCliente(mensagem);
    // }
    // return pacote;

    // } catch (IOException e) {
    // e.printStackTrace();
    // return null;
    // }
    // }

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

            // } else if (mensagemRecebida.startsWith("SERVER_COUNT:") ||
            // mensagemRecebida.startsWith("CLIENT_PORT:")) {
            // // A mensagem começa com "SERVER_COUNT:" ou "CLIENT_PORT:", então contém
            // informações de contagem do servidor ou porta do cliente

            // // Divida a mensagem com base na vírgula ","
            // String[] parts = mensagemRecebida.split(",");

            // for (String part : parts) {
            // // Divida cada parte com base no sinal de igual "=" para obter a chave e o
            // valor
            // String[] keyValue = part.split("=");
            // if (keyValue.length == 2) {
            // String key = keyValue[0];
            // String value = keyValue[1];
            // System.out.println(value);

            // // Verifique se a chave é "SERVER_COUNT" para atualizar a contagem do
            // servidor
            // if (key.startsWith("SERVER_COUNT")) {
            // // O valor deve ser algo como "1111=1", onde 1111 é a porta do servidor e 1 é
            // a contagem
            // String[] serverInfo = key.split(":");
            // System.out.println(serverInfo);
            // // if (serverInfo.length == 2) {
            // int serverPort = Integer.parseInt(serverInfo[1]);
            // int serverCount = Integer.parseInt(value);
            // System.out.println(serverPort);
            // System.out.println(serverCount);
            // // Atualize a contagem do servidor no mapa serverPorts
            // serverPorts.put(serverPort, serverCount);
            // // }
            // }

            // // Verifique se a chave é "CLIENT_PORT" para adicionar a porta do cliente
            // if (key.startsWith("CLIENT_PORT")) {
            // // O valor é a porta do cliente
            // int clientPort = Integer.parseInt(value);

            // // Adicione a porta do cliente ao conjunto clientPorts
            // clientPorts.add(clientPort);
            // }
            // }
            // }
            // System.out.println("Recebido SERVER_COUNT - " + serverPorts);
            // System.out.println("Recebido CLIENT_PORT - " + clientPorts);
            // }

            else if (mensagemRecebida.startsWith("SERVER_COUNT:") || mensagemRecebida.startsWith("CLIENT_PORT:")) {
                // A mensagem começa com "SERVER_COUNT:" ou "CLIENT_PORT:", então contém
                // informações de contagem do servidor ou porta do cliente

                // Divida a mensagem com base na vírgula ","
                String[] parts = mensagemRecebida.split(",");

                for (String part : parts) {
                    // Divida cada parte com base no sinal de igual "=" para obter a chave e o valor
                    String[] keyValue = part.split("=");
                    if (keyValue.length == 2) {
                        String key = keyValue[0];
                        String value = keyValue[1];

                        // Verifique se a chave é "SERVER_COUNT" para atualizar a contagem do servidor
                        if (key.startsWith("SERVER_COUNT")) {
                            // O valor deve ser algo como "1111=1", onde 1111 é a porta do servidor e 1 é a
                            // contagem
                            String[] serverInfo = key.split(":");
                            if (serverInfo.length == 2) {
                                int serverPort = Integer.parseInt(serverInfo[1]);
                                int serverCount = Integer.parseInt(value);

                                // Verifique se já existe um mapa para esta porta do servidor
                                if (!serverPorts.containsKey(serverPort)) {
                                    serverPorts.put(serverPort, new HashMap<>());
                                }

                                // Atualize a contagem do servidor no mapa serverPorts
                                serverPorts.get(serverPort).put("COUNT", serverCount);
                            }
                        }

                    }
                    keyValue = part.split(":");
                    // Verifique se a chave é "CLIENT_PORT" para adicionar a porta do cliente
                    if (part.startsWith("CLIENT_PORT")) {
                        String value = keyValue[1];
                        int clientPort = Integer.parseInt(value);

                        clientPorts.add(clientPort);
                    }
                }
                System.out.println("SERVER_COUNT - " + serverPorts);
                System.out.println("CLIENT_PORT - " + clientPorts);
            }

            return pacote;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void incrementarContador() {
        if (!clientPorts.contains(enderecoCliente.getPort())) {
            clientPorts.add(0, enderecoCliente.getPort()); // Adiciona a porta do cliente à lista

            int connectionCount = serverPorts
                    .getOrDefault(multicastSocketServer.getLocalPort(), new HashMap<>())
                    .getOrDefault("COUNT", 0); // Obtém a
                                               // contagem
                                               // atual para
                                               // esta porta
            // do servidor
            connectionCount++;
            Map<String, Integer> serverData = serverPorts.getOrDefault(multicastSocketServer.getLocalPort(),
                    new HashMap<>());
            serverData.put("COUNT", connectionCount);
            serverPorts.put(multicastSocketServer.getLocalPort(), serverData); // Atualiza a contagem de
                                                                               // conexões para esta porta do
                                                                               // servidor

            // Antes de enviar a mensagem CONN_COUNT
            StringBuilder serverCountStr = new StringBuilder("SERVER_COUNT:");
            for (Map.Entry<Integer, Map<String, Integer>> entry : serverPorts.entrySet()) {
                int serverPort = entry.getKey();
                int serverCount = entry.getValue().getOrDefault("COUNT", 0);
                serverCountStr.append(serverPort).append("=").append(serverCount).append(",");
            }

            StringBuilder clientPortsStr = new StringBuilder("CLIENT_PORT:");
            for (Integer clientPort : clientPorts) {
                clientPortsStr.append(clientPort).append(",");
            }

            // Combine as duas partes em uma única mensagem
            String connCountMessage = serverCountStr.toString() + clientPortsStr.toString();

            // Envie a mensagem CONN_COUNT
            enviarMensagemEntreServidor(connCountMessage, multicastSocketServer.getLocalPort());

            System.out.println(
                    "Cliente na porta " + enderecoCliente.getPort() + " conectou-se. Contagem de conexões: "
                            + serverPorts);
        }
    }

    public static void decrementarContador() {
        int portaCliente = enderecoCliente.getPort();

        if (clientPorts.contains(portaCliente)) {
            // A porta do cliente está na lista, podemos decrementar o contador

            // Remove a porta do cliente da lista
            clientPorts.remove((Integer) portaCliente); // Usamos (Integer) para garantir a remoção do objeto Integer

            // Obtém a contagem atual do servidor para a porta local
            Map<String, Integer> serverData = serverPorts.getOrDefault(multicastSocketServer.getLocalPort(),
                    new HashMap<>());
            int connectionCount = serverData.getOrDefault("COUNT", 0);

            // Decrementa a contagem
            connectionCount--;

            // Atualiza a contagem do servidor
            serverData.put("COUNT", connectionCount);
            serverPorts.put(multicastSocketServer.getLocalPort(), serverData);

            // Antes de enviar a mensagem CONN_COUNT
            StringBuilder serverCountStr = new StringBuilder("SERVER_COUNT:");
            for (Map.Entry<Integer, Map<String, Integer>> entry : serverPorts.entrySet()) {
                int serverPort = entry.getKey();
                int serverCount = entry.getValue().getOrDefault("COUNT", 0);
                serverCountStr.append(serverPort).append("=").append(serverCount).append(",");
            }

            StringBuilder clientPortsStr = new StringBuilder("CLIENT_PORT:");
            for (Integer clientPort : clientPorts) {
                clientPortsStr.append(clientPort).append(",");
            }

            // Combine as duas partes em uma única mensagem
            String connCountMessage = serverCountStr.toString() + clientPortsStr.toString();

            // Envie a mensagem CONN_COUNT
            enviarMensagemEntreServidor(connCountMessage, multicastSocketServer.getLocalPort());

            System.out.println(
                    "Cliente na porta " + portaCliente + " desconectou-se. Contagem de conexões: " + serverPorts);
        } else {
            // A porta do cliente não está na lista, não há nada para decrementar
            System.out.println("Tentativa de decrementar um cliente que não está na lista: " + portaCliente);
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

    public static int getEnderecoCliente() {
        return enderecoCliente.getPort();
    }

}
