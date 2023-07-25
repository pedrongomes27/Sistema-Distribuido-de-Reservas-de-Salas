import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {
        try {
            InetAddress grupo = InetAddress.getByName("239.10.10.10");
            int[] portasServidores = { 1111, 2222, 3333 }; // Lista de portas dos servidores
            int timeout = 3000; // Tempo de espera para receber resposta em milissegundos (3 segundos)

            DatagramSocket multicastSocket = new DatagramSocket();
            multicastSocket.setSoTimeout(timeout); // Define o tempo limite para aguardar a resposta

            InetAddress enderecoServidor = null;
            int portaServidor = 0;

            while (true) {
                boolean servidorEncontrado = false;
                for (int porta : portasServidores) {
                    try {
                        if (verificarServidorDisponivel(multicastSocket, grupo, porta)) {
                            enderecoServidor = grupo;
                            portaServidor = porta;
                            System.out.println(
                                    "Servidor disponível no endereço: " + grupo.getHostAddress() + ", porta: " + porta);
                            servidorEncontrado = true;
                            break; // Encerra o loop caso encontre um servidor disponível
                        }
                    } catch (IOException e) {
                        // System.out.println("Servidor não respondeu na porta " + porta);
                    }
                }

                if (!servidorEncontrado) {
                    System.out.println("Nenhum servidor disponível. Tentando novamente em 5 segundos...");
                    Thread.sleep(5000);
                } else {
                    break; // Encerra o loop externo caso encontre um servidor disponível
                }
            }

            Thread verificadorServidor = new VerificadorServidor(multicastSocket, grupo, portaServidor);
            verificadorServidor.start();

            while (true) {
                System.out.println("");
                System.out.println("Bem-vindo ao Sistema de Reservas de Salas de Estudo!");
                System.out.println("Digite 1 para visualizar disponibilidade das salas");
                System.out.println("Digite 2 para fazer uma reserva");
                System.out.println("Digite 3 para cancelar uma reserva");
                System.out.println("Digite 4 para sair");

                Scanner scanner = new Scanner(System.in);
                int opcao = Integer.parseInt(scanner.nextLine());

                // Se chegou aqui, o servidor está disponível
                if (opcao == 1) {
                    if (verificarServidorDisponivel(multicastSocket, grupo, portaServidor)) {
                        enviarMensagem("CONSULTAR_DISPONIBILIDADE", multicastSocket, enderecoServidor, portaServidor);
                        receberResposta(multicastSocket);
                    } else {
                        System.out.println("Servidor indisponível. Aguarde, tentando conexão...");
                        enderecoServidor = null;
                        portaServidor = 0;

                        encerrarVerificadorServidor(verificadorServidor);

                        while (true) {
                            boolean servidorEncontrado = false;
                            for (int porta : portasServidores) {
                                try {
                                    if (verificarServidorDisponivel(multicastSocket, grupo, porta)) {
                                        enderecoServidor = grupo;
                                        portaServidor = porta;
                                        System.out.println(
                                                "Servidor disponível no endereço: " + grupo.getHostAddress()
                                                        + ", porta: " + porta);
                                        servidorEncontrado = true;
                                        break; // Encerra o loop caso encontre um servidor disponível
                                    }
                                } catch (IOException e) {
                                    // System.out.println("Servidor não respondeu na porta " + porta);
                                }
                            }

                            if (!servidorEncontrado) {
                                System.out.println("Nenhum servidor disponível. Tentando novamente em 5 segundos...");
                                Thread.sleep(5000);
                            } else {
                                break; // Encerra o loop externo caso encontre um servidor disponível
                            }
                        }
                        // Encerra o verificadorServidor atual, se existir
                        verificadorServidor = new VerificadorServidor(multicastSocket, grupo, portaServidor);
                        verificadorServidor.start();
                    }
                } else if (opcao == 2) {
                    if (verificarServidorDisponivel(multicastSocket, grupo, portaServidor)) {
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
                        enviarMensagem(
                                "FAZER_RESERVA " + numeroSala + " " + horario + " " + nome + " " + sobrenome + " "
                                        + email,
                                multicastSocket, enderecoServidor, portaServidor);
                        receberResposta(multicastSocket);
                    } else {
                        System.out.println("Servidor indisponível. Aguarde, tentando conexão...");
                        enderecoServidor = null;
                        portaServidor = 0;

                        encerrarVerificadorServidor(verificadorServidor);

                        while (true) {
                            boolean servidorEncontrado = false;
                            for (int porta : portasServidores) {
                                try {
                                    if (verificarServidorDisponivel(multicastSocket, grupo, porta)) {
                                        enderecoServidor = grupo;
                                        portaServidor = porta;
                                        System.out.println(
                                                "Servidor disponível no endereço: " + grupo.getHostAddress()
                                                        + ", porta: " + porta);
                                        servidorEncontrado = true;
                                        break; // Encerra o loop caso encontre um servidor disponível
                                    }
                                } catch (IOException e) {
                                    // System.out.println("Servidor não respondeu na porta " + porta);
                                }
                            }

                            if (!servidorEncontrado) {
                                System.out.println("Nenhum servidor disponível. Tentando novamente em 5 segundos...");
                                Thread.sleep(5000);
                            } else {
                                break; // Encerra o loop externo caso encontre um servidor disponível
                            }
                        }
                        // Encerra o verificadorServidor atual, se existir
                        verificadorServidor = new VerificadorServidor(multicastSocket, grupo, portaServidor);
                        verificadorServidor.start();
                    }

                } else if (opcao == 3) {
                    if (verificarServidorDisponivel(multicastSocket, grupo, portaServidor)) {
                        System.out.print("Digite o número da sala da reserva a ser cancelada: ");
                        int numeroSala = Integer.parseInt(scanner.nextLine());
                        System.out.print("Digite o horário da reserva a ser cancelada (hh:mm): ");
                        String horario = scanner.nextLine();
                        System.out.print("Digite seu e-mail: ");
                        String email = scanner.nextLine();
                        enviarMensagem("CANCELAR_RESERVA " + numeroSala + " " + horario + " " + email, multicastSocket,
                                enderecoServidor, portaServidor);
                        receberResposta(multicastSocket);
                    } else {
                        System.out.println("Servidor indisponível. Aguarde, tentando conexão...");
                        enderecoServidor = null;
                        portaServidor = 0;

                        encerrarVerificadorServidor(verificadorServidor);

                        while (true) {
                            boolean servidorEncontrado = false;
                            for (int porta : portasServidores) {
                                try {
                                    if (verificarServidorDisponivel(multicastSocket, grupo, porta)) {
                                        enderecoServidor = grupo;
                                        portaServidor = porta;
                                        System.out.println(
                                                "Servidor disponível no endereço: " + grupo.getHostAddress()
                                                        + ", porta: " + porta);
                                        servidorEncontrado = true;
                                        break; // Encerra o loop caso encontre um servidor disponível
                                    }
                                } catch (IOException e) {
                                    // System.out.println("Servidor não respondeu na porta " + porta);
                                }
                            }

                            if (!servidorEncontrado) {
                                System.out.println("Nenhum servidor disponível. Tentando novamente em 5 segundos...");
                                Thread.sleep(5000);
                            } else {
                                break; // Encerra o loop externo caso encontre um servidor disponível
                            }
                        }
                        // Encerra o verificadorServidor atual, se existir
                        verificadorServidor = new VerificadorServidor(multicastSocket, grupo, portaServidor);
                        verificadorServidor.start();
                    }
                } else if (opcao == 4) {
                    break;
                } else {
                    System.out.println("Opção inválida.");
                }
            }

            verificadorServidor.interrupt();
            verificadorServidor.join();
            multicastSocket.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    

    private static boolean verificarServidorDisponivel(DatagramSocket socket, InetAddress grupo, int portaServidor)
            throws IOException {
        String mensagemSolicitacao = "heartbeat";
        byte[] bufferSolicitacao = mensagemSolicitacao.getBytes();
        DatagramPacket pacoteSolicitacao = new DatagramPacket(bufferSolicitacao, bufferSolicitacao.length, grupo,
                portaServidor);

        try {
            socket.send(pacoteSolicitacao);

            byte[] bufferResposta = new byte[1024];
            DatagramPacket pacoteResposta = new DatagramPacket(bufferResposta, bufferResposta.length);

            socket.receive(pacoteResposta);
            String resposta = new String(pacoteResposta.getData(), 0, pacoteResposta.getLength());

            if (resposta.equals("heartbeat")) {
                return true;
            }
        } catch (IOException e) {
            // O servidor não respondeu, então consideramos como servidor indisponível
            return false;
        }

        return false;
    }

    private static void enviarMensagem(String mensagem, DatagramSocket socket, InetAddress servidorInfo,
            int portaServidor) throws IOException {
        byte[] buffer = mensagem.getBytes();
        DatagramPacket pacote = new DatagramPacket(buffer, buffer.length, servidorInfo, portaServidor);
        socket.send(pacote);
    }

    private static void receberResposta(DatagramSocket socket) throws IOException {
        byte[] bufferResposta = new byte[1024];
        DatagramPacket pacoteResposta = new DatagramPacket(bufferResposta, bufferResposta.length);
        socket.receive(pacoteResposta);
        String resposta = new String(pacoteResposta.getData(), 0, pacoteResposta.getLength());
        System.out.println(resposta);
    }

    private static class VerificadorServidor extends Thread {
        private DatagramSocket socket;
        private InetAddress grupo;
        private int portaServidor;

        public VerificadorServidor(DatagramSocket socket, InetAddress grupo, int portaServidor) {
            this.socket = socket;
            this.grupo = grupo;
            this.portaServidor = portaServidor;
        }

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(1000);
                    verificarServidorDisponivel(socket, grupo, portaServidor);
                } catch (IOException e) {
                    System.out.println("Servidor não está disponível. Tentando novamente...");
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    private static void encerrarVerificadorServidor(Thread verificadorServidor) {
        if (verificadorServidor != null && verificadorServidor.isAlive()) {
            verificadorServidor.interrupt();
            try {
                verificadorServidor.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
