package Client;

import java.io.IOException;
import java.util.Scanner;
import Middleware.Middleware;

public class Cliente {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    public static void main(String[] args) {
        try {
            Middleware.inicializarMulticastSocket();
            Middleware.localizarServidor();
            Middleware.iniciarVerificadorServidor();

            Scanner scanner = new Scanner(System.in);
            while (true) {
                exibirMenu();
                int opcao = Integer.parseInt(scanner.nextLine());
                processarOpcao(opcao, scanner);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("---- | Erro inesperado! | ----");
            Middleware.encerrarCliente();
        }
    }

    public static void exibirMenu() {
        System.out.println(" ");
        System.out.println(
                ANSI_CYAN + "----- | Bem-vindo ao Sistema de Reservas de Salas de Estudo | -----" + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "      | Digite: [1] para ver a disponibilidade das salas    |");
        System.out.println("      | Digite: [2] para fazer uma reserva                  |");
        System.out.println("      | Digite: [3] para cancelar uma reserva               |");
        System.out.println("----- | Digite: [4] para sair                               | -----" + ANSI_RESET);

    }

    public static void reconectar() throws IOException, InterruptedException {
        Middleware.encerrarVerificadorServidor();
        Middleware.localizarServidor();
        Middleware.iniciarVerificadorServidor();
    }

    public static void processarOpcao(int opcao, Scanner scanner) throws IOException, InterruptedException {
        switch (opcao) {
            case 1:
                if (Middleware.verificarServidorDisponivel()) {
                    Middleware.enviarMensagemParaServidor("CONSULTAR_DISPONIBILIDADE");
                    Middleware.receberRespostaDoServidor();
                } else {
                    reconectar();
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
                if (Middleware.verificarServidorDisponivel()) {
                    Middleware.enviarMensagemParaServidor(
                            "FAZER_RESERVA " + numeroSala + " " + horario + " " + nome + " " + sobrenome + " "
                                    + cpf);
                    Middleware.receberRespostaDoServidor();
                } else {
                    reconectar();
                }
                break;
            case 3:
                System.out.print("Digite o número da sala da reserva a ser cancelada: ");
                int findSala = Integer.parseInt(scanner.nextLine());
                System.out.print("Digite o horário da reserva a ser cancelada (hh:mm): ");
                String findHorario = scanner.nextLine();
                System.out.print("Digite seu cpf: ");
                String findCpf = scanner.nextLine();
                if (Middleware.verificarServidorDisponivel()) {
                    Middleware.enviarMensagemParaServidor(
                            "CANCELAR_RESERVA " + findSala + " " + findHorario + " " + findCpf);
                    Middleware.receberRespostaDoServidor();
                } else {
                    reconectar();

                }
                break;
            case 4:
                if (Middleware.verificarServidorDisponivel()) {
                    Middleware.enviarMensagemParaServidor(
                            "SAIR_CLIENTE");
                    Middleware.encerrarCliente();
                } else {
                    reconectar();

                }
                break;
            default:
                System.out.println("Opção inválida.");
        }
    }
}
