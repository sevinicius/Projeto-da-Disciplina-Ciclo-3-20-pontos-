package negocio;

public abstract class ContaEspecial extends Conta {

    private double limite;

    public double getLimite() {
        return limite;
    }

    public void setLimite(double limite) {
        this.limite = limite;
    }

    @Override
    public void creditar(double valor) {
        setSaldo(getSaldo() + valor);
    }
}
