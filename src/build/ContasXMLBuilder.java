package build;

import java.math.BigDecimal;
import java.util.Iterator;

import negocio.Conta;

public class ContasXMLBuilder implements ContasBuilder {

    private BigDecimal saldoTotal = BigDecimal.ZERO;

    @Override
    public String gerarCabecalho() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    }

    @Override
    public String gerarListagemContas(Iterator<Conta> iterator) {
        StringBuilder corpo = new StringBuilder("<contas>\n");

        while (iterator.hasNext()) {
            Conta c = iterator.next();
            if (c != null) {
                corpo.append("\t<conta>\n")
                     .append("\t\t<numero>").append(c.getNumero()).append("</numero>\n")
                     .append("\t\t<saldo>").append(c.getSaldo()).append("</saldo>\n")
                     .append("\t</conta>\n");
                saldoTotal = saldoTotal.add(BigDecimal.valueOf(c.getSaldo()));
            }
        }

        corpo.append("</contas>\n");
        return corpo.toString();
    }

    @Override
    public String gerarSumario() {
        return "<saldo_total>" + saldoTotal + "</saldo_total>";
    }

    public String listagemContas(Iterator<Conta> iterator) {
        String resultado = gerarCabecalho() + "\n" + gerarListagemContas(iterator) + "\n" +
                           gerarSumario();

        // Criação do documento
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream("listagemContas.pdf"));
            document.open();

            // Adicionando um parágrafo ao documento
            document.add(new Paragraph("Listagem de Contas PDF"));
            document.add(new Paragraph(resultado));

        } catch (DocumentException | IOException e) {
            System.err.println("Erro ao gerar PDF: " + e.getMessage());
        } finally {
            document.close();
        }
        return resultado;
    }

}
