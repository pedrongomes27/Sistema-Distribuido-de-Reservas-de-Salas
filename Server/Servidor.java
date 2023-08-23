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

import Client.Reserva;

public class Servidor {
    private static Map<Integer, Sala> salas = new HashMap<>();
    private static Map<Integer, Reserva> reservas = new HashMap<>();
    private static int proximoIdReserva = 1;

    private InetAddress endereco;
    private int porta;

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
            int porta = 3333;

            MulticastSocket multicastSocket = new MulticastSocket(null);
            multicastSocket.bind(new InetSocketAddress(porta));
            NetworkInterface networkInterface = NetworkInterface.getByName("eth2");
            multicastSocket.joinGroup(new InetSocketAddress(grupo, porta), networkInterface);

            byte[] buffer = new byte[1024];

            System.out.println("Servidor Multicast iniciado. Aguardando mensagem...");

            while (true) {
                DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(pacote);
                String mensagemRecebida = new String(pacote.getData(), 0, pacote.getLength());
                System.out.println("Mensagem recebida: " + mensagemRecebida);

                String[] partesMensagem = mensagemRecebida.split(" ");
                String operacao = partesMensagem[0];

                if (operacao.equals("heartbeat")) {
                    try {
                        byte[] resposta = "heartbeat".getBytes();
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
                    String email = partesMensagem[5];
                    Usuario usuario = new Usuario(nome, sobrenome, email);
                    fazerReserva(numeroSala, horario, usuario,
                            new InetSocketAddress(pacote.getAddress(), pacote.getPort()));
                }

                else if (operacao.equals("CANCELAR_RESERVA")) {
                    int numeroSala = Integer.parseInt(partesMensagem[1]);
                    String horario = partesMensagem[2];
                    String email = partesMensagem[3];
                    cancelarReserva(numeroSala, horario, email,
                            new InetSocketAddress(pacote.getAddress(), pacote.getPort()));
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
                    resposta.append("Laboratório " + sala.getNumero() + ": Reservado para as ")
                            .append(reservaMap.getHorario()).append(" por ")
                            .append(reservaMap.getUsuario().getNome()).append(reservaMap.getUsuario().getSobrenome())
                            .append("\n");
                    encontrouReserva = true;
                }
            }

            if (!encontrouReserva) {
                resposta.append("Laboratório " + sala.getNumero() + ": Disponível\n");
            }
        }

        enviarMensagem(resposta.toString(), enderecoCliente);

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

        // Se não encontrou nenhuma reserva para essa sala e horário, fazer a reserva
        int idReserva = proximoIdReserva++;
        Reserva novaReserva = new Reserva(idReserva, numeroSala, horario, usuario);
        reservas.put(idReserva, novaReserva);
        enviarMensagem(
                "Reserva da Sala " + numeroSala + " feita para " + horario + " por " + usuario.getNome() + " "
                        + usuario.getSobrenome(),
                enderecoCliente);

    }

    private static void cancelarReserva(int numeroSala, String horario, String email,
            InetSocketAddress enderecoCliente) {
        boolean reservaEncontrada = false;
        int idReservaParaRemover = -1;

        // Percorra todas as reservas no mapa reservas
        for (Reserva reserva : reservas.values()) {
            if (reserva.getNumeroSala() == numeroSala && reserva.getHorario().equals(horario)
                    && reserva.getUsuario().getEmail().equals(email)) {
                reservaEncontrada = true;
                idReservaParaRemover = reserva.getId();
                break; // Se encontrou a reserva, interrompe o loop
            }
        }

        if (reservaEncontrada) {
            // Remova a reserva do mapa reservas
            reservas.remove(idReservaParaRemover);
            enviarMensagem("Reserva da Sala " + numeroSala + " para " + horario + " cancelada.",
                    enderecoCliente);
        } else {
            enviarMensagem(
                    "Reserva não encontrada para a Sala " + numeroSala + " no horário " + horario + " com e-mail "
                            + email,
                    enderecoCliente);
        }
    }

    private static void enviarMensagem(String mensagem, InetSocketAddress enderecoCliente) {
        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] buffer = mensagem.getBytes();
            DatagramPacket pacote = new DatagramPacket(buffer, buffer.length, enderecoCliente.getAddress(),
                    enderecoCliente.getPort());

            // Inicia uma nova thread para enviar a resposta ao cliente
            Thread enviarThread = new Thread(() -> {
                try {
                    socket.send(pacote);
                    socket.close();
                    System.out.println("Resposta enviada para o cliente.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            enviarThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
