package build;
import java.util.Iterator;
import java.io.FileOutputStream;
import java.io.IOException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import negocio.Conta;

public class ContasPDFBuilder implements ContasBuilder {
	public String gerarCabecalho() {
		return new String("");
	}
	public String gerarListagemContas(Iterator<Conta> iterator) {
		
		return new String("");
		
	}
	public String gerarSumario() {
		
		return new String("");
	}
	
	public String listagemContas(Iterator<Conta> iterator) {
		String resultado = gerarCabecalho() + "\n" + gerarListagemContas(iterator) + "\n" +
	                       gerarSumario();
		// cria��o do documento
        Document document = new Document();
        try {

            PdfWriter.getInstance(document, new FileOutputStream("C:/listagemContas.pdf"));
            document.open();

            // adicionando um par�grafo no documento
            document.add(new Paragraph("Listagem de Contas PDF"));
            document.add(new Paragraph(resultado));

        }
        catch(DocumentException de) {
            System.err.println(de.getMessage());
        }
        catch(IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        document.close();
		return resultado;
	}

}
