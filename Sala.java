public class Sala {
    private int numero;
    private String horarioReservado;

    public Sala(int numero) {
        this.numero = numero;
        this.horarioReservado = null;
    }

    public int getNumero() {
        return numero;
    }

    public String getHorarioReservado() {
        return horarioReservado;
    }

    public void reservarSala(String horario) {
        this.horarioReservado = horario;
    }

    public void cancelarReserva() {
        this.horarioReservado = null;
    }
}
