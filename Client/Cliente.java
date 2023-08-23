package Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Scanner;

import Server.Servidor;

public class Cliente {
    private DatagramSocket multicastSocket;
    private InetAddress enderecoServidor;
    private int portaServidor;

    public void iniciar() {
        System.out.println("entrou");
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
        InetAddress enderecoGrupo = InetAddress.getByName("239.10.10.10");
        NetworkInterface networkInterface = NetworkInterface.getByName("eth2"); // Substitua "eth2" pela interface de
                                                                                // rede correta

        int[] portasServidores = { 1111, 2222, 3333 }; // Lista de portas dos servidores

        for (int multicastPort : portasServidores) {
            multicastSocket.joinGroup(new InetSocketAddress(enderecoGrupo, multicastPort), networkInterface);

            Servidor servidor = NetworkUtils.verificarServidorDisponivel(multicastSocket, enderecoGrupo, multicastPort);

            multicastSocket.leaveGroup(new InetSocketAddress(enderecoGrupo, multicastPort), networkInterface);

            if (servidor != null) {
                enderecoServidor = servidor.getEndereco();
                portaServidor = servidor.getPorta();
                System.out.println("Servidor disponível no endereço: " + enderecoServidor.getHostAddress() + ", porta: "
                        + portaServidor);
                break; // Encerra o loop caso encontre um servidor disponível
            }
        }

        // Se não encontrou servidor, esperar e tentar novamente
        if (enderecoServidor == null) {
            System.out.println("Nenhum servidor disponível. Tentando novamente em 5 segundos...");
            Thread.sleep(5000);
            localizarServidor();
        }
    }

    private void iniciarVerificadorServidor() {
        VerificadorServidor verificadorServidor = new VerificadorServidor(multicastSocket, enderecoServidor,
                portaServidor);
        verificadorServidor.start();
    }

    private void exibirMenu() {
        System.out.println("");
        System.out.println("Bem-vindo ao Sistema de Reservas de Salas de Estudo!");
        System.out.println("Digite 1 para visualizar disponibilidade das salas");
        System.out.println("Digite 2 para fazer uma reserva");
        System.out.println("Digite 3 para cancelar uma reserva");
        System.out.println("Digite 4 para sair");
    }

    private void processarOpcao(int opcao, Scanner scanner) throws IOException {
        switch (opcao) {
            case 1:
                if (NetworkUtils.verificarServidorDisponivel(multicastSocket, enderecoServidor,
                        portaServidor) != null) {
                    NetworkUtils.enviarMensagem("CONSULTAR_DISPONIBILIDADE", multicastSocket, enderecoServidor,
                            portaServidor);
                    NetworkUtils.receberResposta(multicastSocket);
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
                        portaServidor) != null) {
                    NetworkUtils.enviarMensagem(
                            "FAZER_RESERVA " + numeroSala + " " + horario + " " + nome + " " + sobrenome + " "
                                    + cpf,
                            multicastSocket, enderecoServidor, portaServidor);
                    NetworkUtils.receberResposta(multicastSocket);
                }
                break;
            case 3:
                System.out.print("Digite o número da sala da reserva a ser cancelada: ");
                int findSala = Integer.parseInt(scanner.nextLine());
                System.out.print("Digite o horário da reserva a ser cancelada (hh:mm): ");
                String findHorario = scanner.nextLine();
                System.out.print("Digite seu cpf: ");
                String findCpf = scanner.nextLine();
                NetworkUtils.enviarMensagem("CANCELAR_RESERVA " + findSala + " " + findHorario + " " + findCpf,
                        multicastSocket,
                        enderecoServidor, portaServidor);
                NetworkUtils.receberResposta(multicastSocket);
                break;
            case 4:
                encerrar();
                break;
            default:
                System.out.println("Opção inválida.");
        }
    }
}
