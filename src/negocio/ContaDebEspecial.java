package negocio;

public class ContaDebEspecial extends ContaEspecial {

    public ContaDebEspecial() {
        super();
    }

    public ContaDebEspecial(String numero, double saldo, double limite) {
        setNumero(numero);
        setSaldo(saldo);
        setLimite(limite);
    }

    @Override
    public void debitar(double valor) {
        if ((getLimite() + getSaldo() - valor) >= 0) {
            setSaldo(getSaldo() - valor);
        }
    }
}
