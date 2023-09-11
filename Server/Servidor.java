package Server;

import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Map;

import Middleware.Middleware;

public class Servidor {
    private static Map<Integer, Sala> salas = new HashMap<>();
    private static Map<Integer, Reserva> reservas = new HashMap<>();
    private static int proximoIdReserva = 1;
    private static int porta = 1111;

    private static StringBuilder reservasReplicadas = new StringBuilder();

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";

    public static void main(String[] args) {
        salas.put(1, new Sala(1));
        salas.put(2, new Sala(2));
        salas.put(3, new Sala(3));

        Middleware.definirServidor(porta);

        while (true) {
            DatagramPacket pacote = Middleware.receberMensagemDoCliente();

            String mensagemRecebida = new String(pacote.getData(), 0, pacote.getLength());
            System.out.println("Mensagem recebida: " + mensagemRecebida);

            String[] partesMensagem = mensagemRecebida.split(" ");
            String operacao = partesMensagem[0];

            if (operacao.equals("SERVER_ONLINE")) {
                System.out.println("Recebida mensagem de servidor online do outro servidor.");
                replicarDados();
            }

            else if (operacao.equals("CONSULTAR_DISPONIBILIDADE")) {
                consultarDisponibilidade();
            }

            else if (operacao.equals("FAZER_RESERVA")) {
                int numeroSala = Integer.parseInt(partesMensagem[1]);
                String horario = partesMensagem[2];
                String nome = partesMensagem[3];
                String sobrenome = partesMensagem[4];
                String cpf = partesMensagem[5];
                Usuario usuario = new Usuario(nome, sobrenome, cpf);
                fazerReserva(numeroSala, horario, usuario);
            }

            else if (operacao.equals("CANCELAR_RESERVA")) {
                int numeroSala = Integer.parseInt(partesMensagem[1]);
                String horario = partesMensagem[2];
                String cpf = partesMensagem[3];
                cancelarReserva(numeroSala, horario, cpf);
            }

            else if (operacao.equals("ATUALIZAR_RESERVAS")) {
                atualizarReservas(mensagemRecebida.substring(19));
            }

        }

    }

    private static void consultarDisponibilidade() {
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

        Middleware.enviarMensagemParaCliente(resposta.toString());
        replicarDados();
    }

    private static void fazerReserva(int numeroSala, String horario, Usuario usuario) {
        // Verificar se a sala já está reservada no horário especificado
        for (Reserva reserva : reservas.values()) {
            if (reserva.getNumeroSala() == numeroSala && reserva.getHorario().equals(horario)) {
                Middleware.enviarMensagemParaCliente("Sala " + numeroSala + " já está reservada para " + horario);
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

        Middleware.enviarMensagemParaCliente(
                "Reserva da Sala " + numeroSala + " feita para " + horario + " por "
                        + usuario.getNome().concat(" ").concat(usuario.getSobrenome()));
        replicarDados();
    }

    private static void cancelarReserva(int numeroSala, String horario, String cpf) {
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
            Middleware.enviarMensagemParaCliente(
                    "Reserva da Sala " + numeroSala + " para " + horario + " foi cancelada.");

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

            replicarDados();
        } else {
            Middleware.enviarMensagemParaCliente(
                    "Não foi encontrada uma reserva da Sala " + numeroSala + " para " + horario
                            + " associada ao cpf fornecido.");
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

    public static void replicarDados() {
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
            String mensagem = ("ATUALIZAR_RESERVAS " + reservasReplicadas.toString());
            Middleware.enviarMensagemEntreServidor(mensagem, porta);
        }
    }
}
