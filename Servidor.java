import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.HashMap;
import java.util.Map;

public class Servidor {
    private static Map<Integer, Sala> salas = new HashMap<>();

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

            while (true) {
                DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(pacote);
                String mensagemRecebida = new String(pacote.getData(), 0, pacote.getLength());
                System.out.println("Mensagem recebida: " + mensagemRecebida);

                String[] partesMensagem = mensagemRecebida.split(" ");
                String operacao = partesMensagem[0];

                if (operacao.equals("CONSULTAR_DISPONIBILIDADE")) {
                    consultarDisponibilidade(new InetSocketAddress(pacote.getAddress(), pacote.getPort()));
                } 
                
                else if (operacao.equals("FAZER_RESERVA")) {
                    int numeroSala = Integer.parseInt(partesMensagem[1]);
                    String horario = partesMensagem[2];
                    fazerReserva(numeroSala, horario, new InetSocketAddress(pacote.getAddress(), pacote.getPort()));
                } 
                
                else if (operacao.equals("CANCELAR_RESERVA")) {
                    int numeroSala = Integer.parseInt(partesMensagem[1]);
                    String horario = partesMensagem[2];
                    cancelarReserva(numeroSala, horario, new InetSocketAddress(pacote.getAddress(), pacote.getPort()));
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
        for (Sala sala : salas.values()) {
            resposta.append("Laboratório ").append(sala.getNumero()).append(": ");
            if (sala.getHorarioReservado() == null) {
                resposta.append("Disponível\n");
            } else {
                resposta.append("Reservada para ").append(sala.getHorarioReservado()).append("\n");
            }
        }

        enviarMensagem(resposta.toString(), enderecoCliente);
    }

    private static void fazerReserva(int numeroSala, String horario, InetSocketAddress enderecoCliente) {
        Sala sala = salas.get(numeroSala);

        if (sala == null) {
            enviarMensagem("Sala " + numeroSala + " não encontrada.", enderecoCliente);
        } else if (sala.getHorarioReservado() != null) {
            enviarMensagem("Sala " + numeroSala + " já está reservada para " + sala.getHorarioReservado(),
                    enderecoCliente);
        } else {
            sala.reservarSala(horario);
            enviarMensagem("Reserva da Sala " + numeroSala + " feita para " + horario, enderecoCliente);
        }
    }

    private static void cancelarReserva(int numeroSala, String horario, InetSocketAddress enderecoCliente) {
        Sala sala = salas.get(numeroSala);

        if (sala == null) {
            enviarMensagem("Sala " + numeroSala + " não encontrada.", enderecoCliente);
        } else if (sala.getHorarioReservado() == null) {
            enviarMensagem("Sala " + numeroSala + " não possui reserva.", enderecoCliente);
        } else if (!sala.getHorarioReservado().equals(horario)) {
            enviarMensagem("Sala " + numeroSala + " não está reservada para o horário especificado.", enderecoCliente);
        } else {
            sala.cancelarReserva();
            enviarMensagem("Reserva da Sala " + numeroSala + " cancelada.", enderecoCliente);
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
