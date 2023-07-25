import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {
        try {
            InetAddress grupo = InetAddress.getByName("239.10.10.10");
            int porta = 1111;

            while (true) {
                DatagramSocket multicastSocket = new DatagramSocket();

                Scanner scanner = new Scanner(System.in);
                System.out.println("");
                System.out.println("Bem-vindo ao Sistema de Reservas de Salas de Estudo!");
                System.out.println("Digite 1 para visualizar disponibilidade das salas");
                System.out.println("Digite 2 para fazer uma reserva");
                System.out.println("Digite 3 para cancelar uma reserva");
                System.out.println("Digite 4 para sair");
                System.out.println("Digite 0 para DESLIGAR O SERVIDOR");

                int opcao = Integer.parseInt(scanner.nextLine());

                if (opcao == 1) {
                    enviarMensagem("CONSULTAR_DISPONIBILIDADE", multicastSocket, grupo, porta);
                    receberResposta(multicastSocket);

                }

                else if (opcao == 2) {
                    System.out.print("Digite o número da sala desejada: ");
                    int numeroSala = Integer.parseInt(scanner.nextLine());
                    System.out.print("Digite o horário da reserva (hh:mm): ");
                    String horario = scanner.nextLine();
                    System.out.print("Digite seu nome: ");
                    String nome = scanner.nextLine();
                    System.out.print("Digite seu sobrenome: ");
                    String sobrenome = scanner.nextLine();
                    System.out.print("Digite seu e-mail: ");
                    String email = scanner.nextLine();
                    enviarMensagem("FAZER_RESERVA " + numeroSala + " " + horario + " " + nome + " " + sobrenome + " " + email, multicastSocket, grupo, porta);
                    receberResposta(multicastSocket);

                }

                else if (opcao == 3) {
                    System.out.print("Digite o número da sala da reserva a ser cancelada: ");
                    int numeroSala = Integer.parseInt(scanner.nextLine());
                    System.out.print("Digite o horário da reserva a ser cancelada (hh:mm): ");
                    String horario = scanner.nextLine();
                    enviarMensagem("CANCELAR_RESERVA " + numeroSala + " " + horario, multicastSocket, grupo, porta);
                    receberResposta(multicastSocket);

                }

                else if (opcao == 4) {
                    break;
                }

                else if (opcao == 0) {
                    enviarMensagem("SAIR", multicastSocket, grupo, porta);
                    break;
                }

                else {
                    System.out.println("Opção inválida.");
                }

                multicastSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void enviarMensagem(String mensagem, DatagramSocket socket, InetAddress grupo, int porta)
            throws IOException {
        byte[] buffer = mensagem.getBytes();
        DatagramPacket pacote = new DatagramPacket(buffer, buffer.length, grupo, porta);
        socket.send(pacote);
        // System.out.println("Mensagem enviada para o grupo multicast.");
    }

    private static void receberResposta(DatagramSocket socket) throws IOException {
        byte[] bufferResposta = new byte[1024];
        DatagramPacket pacoteResposta = new DatagramPacket(bufferResposta, bufferResposta.length);
        socket.receive(pacoteResposta);
        String resposta = new String(pacoteResposta.getData(), 0, pacoteResposta.getLength());
        System.out.println(resposta);
    }
}
