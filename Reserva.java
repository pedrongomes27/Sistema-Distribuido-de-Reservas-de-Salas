public class Reserva {
    private int id;
    private int numeroSala;
    private String horario;
    private Usuario usuario;

    public Reserva(int id, int numeroSala, String horario, Usuario usuario) {
        this.id = id;
        this.numeroSala = numeroSala;
        this.horario = horario;
        this.usuario = usuario;
    }

    public int getId(){
        return id;
    }

    public int getNumeroSala() {
        return numeroSala;
    }

    public String getHorario() {
        return horario;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}