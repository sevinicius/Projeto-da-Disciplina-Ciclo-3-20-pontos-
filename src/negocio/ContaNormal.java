package negocio;

public class ContaNormal extends Conta {

    @Override
    public void creditar(double valor) {
        setSaldo(getSaldo() + valor);
    }

    @Override
    public void debitar(double valor) {
        if ((getSaldo() - valor) >= 0) {
            setSaldo(getSaldo() - valor);
        }
    }
}
