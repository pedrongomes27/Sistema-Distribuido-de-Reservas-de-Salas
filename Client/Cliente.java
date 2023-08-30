package Client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Scanner;

public class Cliente {
    private DatagramSocket multicastSocket = null;
    private InetAddress enderecoServidor = null;
    private int portaServidor = -1;
    VerificadorServidor verificadorServidor = null;

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    public void iniciar() {
        try {
            inicializarMulticastSocket();
            localizarServidor();
            iniciarVerificadorServidor();

            Scanner scanner = new Scanner(System.in);
            while (true) {
                exibirMenu();
                int opcao = Integer.parseInt(scanner.nextLine());
                processarOpcao(opcao, scanner);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        // finally {
        // System.out.println("ERRO2");
        // encerrar();
        // }
    }

    private void encerrar() {
        multicastSocket.close();
        System.exit(0);
    }

    private void inicializarMulticastSocket() throws IOException {
        multicastSocket = new DatagramSocket();
        multicastSocket.setSoTimeout(3000);
    }

    private void localizarServidor() throws IOException, InterruptedException {
        InetAddress enderecoGrupo = InetAddress.getByName("239.10.10.11");
        NetworkInterface networkInterface = NetworkInterface.getByName("wlan0"); // Substitua "eth2" ou "wlan0"

        int[] portasServidores = { 1111, 2222, 3333 }; // Lista de portas dos servidores

        for (int multicastPort : portasServidores) {
            multicastSocket.joinGroup(new InetSocketAddress(enderecoGrupo, multicastPort), networkInterface);

            boolean isOn = NetworkUtils.verificarServidorDisponivel(multicastSocket, enderecoGrupo, multicastPort);

            multicastSocket.leaveGroup(new InetSocketAddress(enderecoGrupo, multicastPort), networkInterface);

            if (isOn == true) {
                enderecoServidor = enderecoGrupo;
                portaServidor = multicastPort;
                System.out.println("Servidor disponível no endereço: " + enderecoServidor.getHostAddress() + ", porta: "
                        + portaServidor + " (APENAS PARA FASE DEV)");
                break;
            }
        }

        if (enderecoServidor == null) {
            System.out.println("Nenhum servidor disponível. Tentando novamente...");
            // Thread.sleep(5000);
            localizarServidor();
        }
    }

    private void iniciarVerificadorServidor() {
        verificadorServidor = new VerificadorServidor(multicastSocket, enderecoServidor,
                portaServidor);
        verificadorServidor.start();
    }

    private void encerrarVerificadorServidor(Thread verificadorServidor) {
        if (verificadorServidor != null && verificadorServidor.isAlive()) {
            verificadorServidor.interrupt();
            try {
                verificadorServidor.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void exibirMenu() {
        System.out.println(" ");
        System.out.println(ANSI_CYAN + "Bem-vindo ao Sistema de Reservas de Salas de Estudo!" + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "Digite 1 para visualizar disponibilidade das salas");
        System.out.println("Digite 2 para fazer uma reserva");
        System.out.println("Digite 3 para cancelar uma reserva");
        System.out.println("Digite 4 para sair" + ANSI_RESET);

    }

    private void processarOpcao(int opcao, Scanner scanner) throws IOException, InterruptedException {
        switch (opcao) {
            case 1:
                if (NetworkUtils.verificarServidorDisponivel(multicastSocket, enderecoServidor,
                        portaServidor)) {
                    NetworkUtils.enviarMensagem("CONSULTAR_DISPONIBILIDADE", multicastSocket, enderecoServidor,
                            portaServidor);
                    NetworkUtils.receberResposta(multicastSocket);
                } else {
                    encerrarVerificadorServidor(verificadorServidor);
                    localizarServidor();
                    iniciarVerificadorServidor();
                }

                break;
            case 2:
                System.out.print("Digite o número da sala desejada: ");
                int numeroSala = Integer.parseInt(scanner.nextLine());
                System.out.print("Digite o horário da reserva (hh:mm): ");
                String horario = scanner.nextLine();
                System.out.print("Digite seu nome: ");
                String nome = scanner.nextLine();
                System.out.print("Digite seu sobrenome: ");
                String sobrenome = scanner.nextLine();
                System.out.print("Digite seu cpf: ");
                String cpf = scanner.nextLine();
                if (NetworkUtils.verificarServidorDisponivel(multicastSocket, enderecoServidor,
                        portaServidor)) {
                    NetworkUtils.enviarMensagem(
                            "FAZER_RESERVA " + numeroSala + " " + horario + " " + nome + " " + sobrenome + " "
                                    + cpf,
                            multicastSocket, enderecoServidor, portaServidor);
                    NetworkUtils.receberResposta(multicastSocket);
                } else {
                    encerrarVerificadorServidor(verificadorServidor);
                    localizarServidor();
                    iniciarVerificadorServidor();
                }
                break;
            case 3:
                System.out.print("Digite o número da sala da reserva a ser cancelada: ");
                int findSala = Integer.parseInt(scanner.nextLine());
                System.out.print("Digite o horário da reserva a ser cancelada (hh:mm): ");
                String findHorario = scanner.nextLine();
                System.out.print("Digite seu cpf: ");
                String findCpf = scanner.nextLine();
                if (NetworkUtils.verificarServidorDisponivel(multicastSocket, enderecoServidor,
                        portaServidor)) {
                    NetworkUtils.enviarMensagem("CANCELAR_RESERVA " + findSala + " " + findHorario + " " + findCpf,
                            multicastSocket,
                            enderecoServidor, portaServidor);
                    NetworkUtils.receberResposta(multicastSocket);
                } else {
                    encerrarVerificadorServidor(verificadorServidor);
                    localizarServidor();
                    iniciarVerificadorServidor();
                }
                break;
            case 4:
                encerrar();
                break;
            default:
                System.out.println("Opção inválida.");
        }
    }
}
