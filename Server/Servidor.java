package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.HashMap;
import java.util.Map;

public class Servidor {
    private static Map<Integer, Sala> salas = new HashMap<>();
    private static Map<Integer, Reserva> reservas = new HashMap<>();
    private static int proximoIdReserva = 1;
    private InetAddress endereco;
    private int porta;
    private static StringBuilder reservasReplicadas = new StringBuilder();

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";

    public Servidor(InetAddress endereco, int porta) {
        this.endereco = endereco;
        this.porta = porta;
    }

    public InetAddress getEndereco() {
        return endereco;
    }

    public int getPorta() {
        return porta;
    }

    public static void main(String[] args) {
        // Criação de algumas salas fictícias para demonstração
        salas.put(1, new Sala(1));
        salas.put(2, new Sala(2));
        salas.put(3, new Sala(3));

        try {
            String grupo = "239.10.10.10";
            int porta = 1111;

            MulticastSocket multicastSocket = new MulticastSocket(null);
            multicastSocket.bind(new InetSocketAddress(porta));
            NetworkInterface networkInterface = NetworkInterface.getByName("eth2");
            multicastSocket.joinGroup(new InetSocketAddress(grupo, porta), networkInterface);

            byte[] buffer = new byte[1024];

            System.out.println("Servidor Multicast iniciado. Aguardando mensagem...");

            int portaOutroServidor = 2222;
            InetSocketAddress enderecoDestino = new InetSocketAddress(grupo, portaOutroServidor);
            enviarMensagemServidorOnline(enderecoDestino);

            while (true) {
                DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(pacote);
                String mensagemRecebida = new String(pacote.getData(), 0, pacote.getLength());
                System.out.println("Mensagem recebida: " + mensagemRecebida);

                String[] partesMensagem = mensagemRecebida.split(" ");
                String operacao = partesMensagem[0];

                if (mensagemRecebida.equals("SERVER_ONLINE")) {
                    System.out.println("Recebida mensagem de servidor online do outro servidor.");
                    enviarDadosParaOutroServidor();
                }

                else if (operacao.equals("IS_SERVER_ON")) {
                    try {
                        byte[] resposta = "HEARTBEAT".getBytes();
                        DatagramSocket socket = new DatagramSocket();
                        DatagramPacket pacoteResposta = new DatagramPacket(resposta, resposta.length,
                                pacote.getAddress(), pacote.getPort());
                        socket.send(pacoteResposta);
                        socket.close();
                        System.out.println("Resposta enviada para o cliente.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                else if (operacao.equals("CONSULTAR_DISPONIBILIDADE")) {
                    consultarDisponibilidade(new InetSocketAddress(pacote.getAddress(), pacote.getPort()));
                }

                else if (operacao.equals("FAZER_RESERVA")) {
                    int numeroSala = Integer.parseInt(partesMensagem[1]);
                    String horario = partesMensagem[2];
                    String nome = partesMensagem[3];
                    String sobrenome = partesMensagem[4];
                    String cpf = partesMensagem[5];
                    Usuario usuario = new Usuario(nome, sobrenome, cpf);
                    fazerReserva(numeroSala, horario, usuario,
                            new InetSocketAddress(pacote.getAddress(), pacote.getPort()));
                }

                else if (operacao.equals("CANCELAR_RESERVA")) {
                    int numeroSala = Integer.parseInt(partesMensagem[1]);
                    String horario = partesMensagem[2];
                    String cpf = partesMensagem[3];
                    cancelarReserva(numeroSala, horario, cpf,
                            new InetSocketAddress(pacote.getAddress(), pacote.getPort()));
                }

                else if (operacao.equals("ATUALIZAR_RESERVAS")) {
                    atualizarReservas(mensagemRecebida.substring(19)); 
                }

                else if (operacao.equals("SAIR")) {
                    multicastSocket.leaveGroup(new InetSocketAddress(grupo, porta), networkInterface);
                    multicastSocket.close();
                    System.out.println("Servidor encerrado.");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void consultarDisponibilidade(InetSocketAddress enderecoCliente) {
        StringBuilder resposta = new StringBuilder("Salas disponíveis:\n");
        boolean encontrouReserva;

        for (Sala sala : salas.values()) {
            encontrouReserva = false;

            for (Reserva reservaMap : reservas.values()) {
                if (reservaMap.getNumeroSala() == sala.getNumero()) {
                    resposta.append("Laboratório ").append(sala.getNumero()).append(": ").append(ANSI_RED)
                            .append("Reservado para as ")
                            .append(reservaMap.getHorario()).append(" por ").append(reservaMap.getUsuario().getNome())
                            .append(" ")
                            .append(reservaMap.getUsuario().getSobrenome()).append(ANSI_RESET).append("\n");
                    encontrouReserva = true;
                }
            }

            if (!encontrouReserva) {
                resposta.append("Laboratório ").append(sala.getNumero()).append(": ").append(ANSI_GREEN)
                        .append("Disponível\n").append(ANSI_RESET);
            }
        }

        enviarMensagem(resposta.toString(), enderecoCliente);
        enviarDadosParaOutroServidor();
    }

    private static void fazerReserva(int numeroSala, String horario, Usuario usuario,
            InetSocketAddress enderecoCliente) {
        // Verificar se a sala já está reservada no horário especificado
        for (Reserva reserva : reservas.values()) {
            if (reserva.getNumeroSala() == numeroSala && reserva.getHorario().equals(horario)) {
                enviarMensagem("Sala " + numeroSala + " já está reservada para " + horario, enderecoCliente);
                return; // Encerrar a função, pois a sala já está reservada
            }
        }

        System.out.println("ID ARMAZENADO: " + proximoIdReserva);
        // Se não encontrou nenhuma reserva para essa sala e horário, fazer a reserva
        int idReserva = proximoIdReserva++;
        Reserva novaReserva = new Reserva(idReserva, numeroSala, horario, usuario);
        reservas.put(idReserva, novaReserva);

        // Adicionar a nova reserva à variável reservasReplicadas
        reservasReplicadas.append(idReserva).append(" ")
                .append(novaReserva.getNumeroSala()).append(" ")
                .append(novaReserva.getHorario()).append(" ")
                .append(novaReserva.getUsuario().getNome()).append(" ")
                .append(novaReserva.getUsuario().getSobrenome()).append(" ")
                .append(novaReserva.getUsuario().getCpf()).append(" ")
                .append("\n");

        enviarMensagem(
                "Reserva da Sala " + numeroSala + " feita para " + horario + " por "
                        + usuario.getNome().concat(" ").concat(usuario.getSobrenome()),
                enderecoCliente);
        enviarDadosParaOutroServidor();
    }

    private static void cancelarReserva(int numeroSala, String horario, String cpf,
            InetSocketAddress enderecoCliente) {
        boolean encontrouReserva = false;
        int idReserva = 0; // Inicializamos com um valor inválido

        for (Reserva reserva : reservas.values()) {
            if (reserva.getNumeroSala() == numeroSala && reserva.getHorario().equals(horario)
                    && reserva.getUsuario().getCpf().equals(cpf)) {
                reservas.remove(reserva.getId());
                encontrouReserva = true;
                idReserva = reserva.getId(); // Armazenamos o ID da reserva encontrada
                break;
            }
        }

        if (encontrouReserva) {
            enviarMensagem("Reserva da Sala " + numeroSala + " para " + horario + " foi cancelada.", enderecoCliente);

            // Remover a reserva cancelada da variável reservasReplicadas
            StringBuilder reservasAtualizadas = new StringBuilder();
            String reservaStr = idReserva + " " + numeroSala + " " + horario + " " + cpf;
            String[] reservasArray = reservasReplicadas.toString().split("\n");

            for (String reserva : reservasArray) {
                if (!reserva.contains(reservaStr)) {
                    reservasAtualizadas.append(reserva).append("\n");
                }
            }

            reservasReplicadas = reservasAtualizadas;

            enviarDadosParaOutroServidor();
        } else {
            enviarMensagem("Não foi encontrada uma reserva da Sala " + numeroSala + " para " + horario
                    + " associada ao cpf fornecido.", enderecoCliente);
        }
    }

    private static void atualizarReservas(String reservasRecebidas) {
        String[] reservasArray = reservasRecebidas.split("\n");
        reservas.clear();
        int maiorIdReserva = 0;

        for (String reservaStr : reservasArray) {
            String[] reservaDados = reservaStr.split(" ");
            int idReserva = Integer.parseInt(reservaDados[0]);
            int numeroSala = Integer.parseInt(reservaDados[1]);
            String horario = reservaDados[2];
            String nome = reservaDados[3];
            String sobrenome = reservaDados[4];
            String cpf = reservaDados[5];
            Usuario usuario = new Usuario(nome, sobrenome, cpf);
            Reserva reserva = new Reserva(idReserva, numeroSala, horario, usuario);
            reservas.put(idReserva, reserva);

            // Atualizar o próximoIdReserva se necessário
            if (idReserva > maiorIdReserva) {
                maiorIdReserva = idReserva;
            }
        }

        // Atualizar o próximoIdReserva
        proximoIdReserva = maiorIdReserva + 1;
    }

    private static void enviarMensagem(String mensagem, InetSocketAddress enderecoCliente) {
        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] buffer = mensagem.getBytes();
            DatagramPacket pacote = new DatagramPacket(buffer, buffer.length, enderecoCliente.getAddress(),
                    enderecoCliente.getPort());

            // Inicia uma nova thread para enviar a resposta ao cliente
            // Thread enviarThread = new Thread(() -> {
            try {
                socket.send(pacote);
                socket.close();
                System.out.println("Resposta enviada para o cliente.");
            } catch (IOException e) {
                e.printStackTrace();
            }
            // });

            // enviarThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private static void enviarDadosParaOutroServidor() {
        StringBuilder dados = new StringBuilder();
        StringBuilder reservasJaEnviadas = new StringBuilder();

        for (Reserva reserva : reservas.values()) {
            String idReservaStr = Integer.toString(reserva.getId());
            if (!reservasJaEnviadas.toString().contains(idReservaStr)) {
                dados.append(idReservaStr).append(" ")
                        .append(reserva.getNumeroSala()).append(" ")
                        .append(reserva.getHorario()).append(" ")
                        .append(reserva.getUsuario().getNome()).append(" ")
                        .append(reserva.getUsuario().getSobrenome()).append(" ")
                        .append(reserva.getUsuario().getCpf()).append(" ")
                        .append("\n");

                reservasJaEnviadas.append(idReservaStr).append(" ");
            }
        }

        reservasReplicadas = dados;

        if (!dados.toString().isEmpty()) {
            try {
                String grupo = "239.10.10.10";
                int porta = 2222;

                MulticastSocket multicastSocket = new MulticastSocket();
                InetAddress grupoAddr = InetAddress.getByName(grupo);

                byte[] buffer = ("ATUALIZAR_RESERVAS " + reservasReplicadas.toString()).getBytes();
                DatagramPacket pacote = new DatagramPacket(buffer, buffer.length, grupoAddr, porta);
                multicastSocket.send(pacote);

                multicastSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
