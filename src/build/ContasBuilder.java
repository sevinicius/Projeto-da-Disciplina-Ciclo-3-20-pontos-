package build;

import java.util.Iterator;

import negocio.Conta;

public interface ContasBuilder {
	abstract String gerarCabecalho();
	abstract String gerarListagemContas(Iterator<Conta> iterator);
	abstract String gerarSumario();

}
