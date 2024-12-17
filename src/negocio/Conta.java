package negocio;

import java.util.UUID;

public abstract class Conta implements Comparable<Conta> {

    private String numero;
    private double saldo;

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public abstract void creditar(double valor);

    public abstract void debitar(double valor);

    @Override
    public int compareTo(Conta o) {
        return this.numero.compareTo(o.getNumero());
    }

    // Método para gerar número de conta
    public static String criarNumeroConta() {
        return UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
