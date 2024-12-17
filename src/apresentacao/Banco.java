package apresentacao;

import java.util.ArrayList;
import java.util.Iterator;

import acesso_a_dado.AcessoADado;
import negocio.Conta;
import negocio.ContaDebEspecial;
import negocio.ContaNormal;
import build.ContasXMLBuilder;

public class Banco {

    private ArrayList<Conta> contas;

    public Banco() {
        contas = new ArrayList<>();
    }

    private void criaConta(Conta c) {
        contas.add(c);
    }

    private void removeConta(String numero) {
        Iterator<Conta> iterator = contas.iterator();
        while (iterator.hasNext()) {
            Conta c = iterator.next();
            if (c.getNumero().equals(numero)) {
                iterator.remove();
            }
        }
    }

    private void creditaConta(String numero, double valor) {
        for (Conta c : contas) {
            if (c.getNumero().equals(numero)) {
                c.creditar(valor);
                break;
            }
        }
    }

    private void debitaConta(String numero, double valor) {
        for (Conta c : contas) {
            if (c.getNumero().equals(numero)) {
                c.debitar(valor);
                break;
            }
        }
    }

    private void transfereConta(String origem, String destino, double valor) {
        debitaConta(origem, valor);
        creditaConta(destino, valor);
    }

    private void listaContas() {
        for (Conta c : contas) {
            System.out.printf("Conta %s, Saldo: %.2f\n", c.getNumero(), c.getSaldo());
        }
    }

    private void listaContasXML() {
        ContasXMLBuilder builder = new ContasXMLBuilder();
        String resultado = builder.listagemContas(contas.iterator());
        System.out.println(resultado);
    }

    public static void main(String[] args) {
        Banco banco = new Banco();
        AcessoADado acesso = new AcessoADado();

        ContaNormal c1 = new ContaNormal();
        c1.setNumero("1234-5");
        c1.setSaldo(1000.0);
        banco.criaConta(c1);

        ContaDebEspecial c2 = new ContaDebEspecial("8503-6", 2000.0, 500.0);
        banco.criaConta(c2);

        System.out.println("--- Listando Contas ---");
        banco.listaContas();

        System.out.println("--- Transferindo R$ 500 de c1 para c2 ---");
        banco.transfereConta("1234-5", "5678-9", 500.0);

        banco.listaContas();

        System.out.println("--- Exportando contas para XML ---");
        banco.listaContasXML();
    }
}