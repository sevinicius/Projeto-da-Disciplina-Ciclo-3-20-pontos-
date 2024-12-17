package ui;

import build.ContasPDFBuilder;
import build.ContasXMLBuilder;
import negocio.Conta;
import negocio.ContaDebEspecial;
import negocio.ContaEspecial;
import negocio.ContaNormal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ContaUI {

    private JFrame frame;
    private JTextField numeroField;
    private JTextField saldoField;
    private JTextField valorField;
    private JComboBox<String> tipoContaComboBox;
    private JButton buscarButton;
    private JButton criarContaButton;
    private JButton creditarButton;
    private JButton debitarButton;
    private JButton gerarPDFButton;
    private JButton gerarXMLButton;

    private List<Conta> contas = new ArrayList<>();

    public ContaUI() {
        frame = new JFrame("Gerenciar Conta");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);
        frame.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Painel de entrada de dados
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));

        JLabel numeroLabel = new JLabel("Número da Conta:");
        numeroLabel.setFont(new Font("Arial", Font.BOLD, 14));
        inputPanel.add(numeroLabel);
        numeroField = new JTextField();
        numeroField.addActionListener(e -> buscarConta());
        inputPanel.add(numeroField);

        JLabel saldoLabel = new JLabel("Saldo:");
        saldoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        inputPanel.add(saldoLabel);
        saldoField = new JTextField();
        saldoField.setEditable(false);
        inputPanel.add(saldoField);

        JLabel valorLabel = new JLabel("Valor (Crédito/Débito):");
        valorLabel.setFont(new Font("Arial", Font.BOLD, 14));
        inputPanel.add(valorLabel);
        valorField = new JTextField();
        inputPanel.add(valorField);

        JLabel tipoContaLabel = new JLabel("Tipo de Conta:");
        tipoContaLabel.setFont(new Font("Arial", Font.BOLD, 14));
        inputPanel.add(tipoContaLabel);
        tipoContaComboBox = new JComboBox<>(new String[]{"Conta Normal", "Conta Especial", "Conta Débito Especial"});
        inputPanel.add(tipoContaComboBox);

        mainPanel.add(inputPanel);

        // Painel de botões
        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        buscarButton = new JButton("Buscar", new ImageIcon("icons/search.png"));
        creditarButton = new JButton("Creditar", new ImageIcon("icons/credit.png"));
        debitarButton = new JButton("Debitar", new ImageIcon("icons/debit.png"));
        gerarPDFButton = new JButton("Gerar PDF", new ImageIcon("icons/pdf.png"));
        gerarXMLButton = new JButton("Gerar XML", new ImageIcon("icons/xml.png"));

        buscarButton.setBackground(Color.GRAY);
        creditarButton.setBackground(Color.GRAY);
        debitarButton.setBackground(Color.GRAY);
        gerarPDFButton.setBackground(Color.GRAY);
        gerarXMLButton.setBackground(Color.GRAY);

        buttonPanel.add(buscarButton);
        buttonPanel.add(creditarButton);
        buttonPanel.add(debitarButton);
        buttonPanel.add(gerarPDFButton);
        buttonPanel.add(gerarXMLButton);

        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(buttonPanel);

        frame.add(mainPanel, BorderLayout.CENTER);

        // Adiciona ação aos botões
        buscarButton.addActionListener(e -> buscarConta());
        creditarButton.addActionListener(e -> creditar());
        debitarButton.addActionListener(e -> debitar());
        gerarPDFButton.addActionListener(e -> gerarPDF());
        gerarXMLButton.addActionListener(e -> gerarXML());
        
        criarContaButton = new JButton("Criar Conta", new ImageIcon("icons/add.png"));
        criarContaButton.setBackground(Color.GRAY);
        criarContaButton.addActionListener(e -> criarConta());
        buttonPanel.add(criarContaButton);

        frame.setVisible(true);
    }

    private Connection connect() throws SQLException {
        String url = "jdbc:postgresql://localhost:5433/banco";
        String user = "postgres";
        String password = "postgres";
        return DriverManager.getConnection(url, user, password);
    }

    private void buscarConta() {
        String numero = numeroField.getText().trim();

        if (numero.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Por favor, insira o número da conta.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = connect()) {
            String sql = "SELECT saldo FROM public.conta WHERE numero = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, numero);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        double saldo = rs.getDouble("saldo");
                        saldoField.setText(String.valueOf(saldo));
                        JOptionPane.showMessageDialog(frame, "Conta encontrada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        saldoField.setText(""); // Limpa o campo de saldo caso a conta não seja encontrada
                        JOptionPane.showMessageDialog(frame, "Conta não encontrada.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Erro ao buscar a conta: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void creditar() {
        String numero = numeroField.getText().trim();
        String valorTexto = valorField.getText().trim();

        if (numero.isEmpty() || valorTexto.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Preencha o número da conta e o valor antes de creditar.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double valor = Double.parseDouble(valorTexto);
            try (Connection conn = connect()) {
                String sqlGetSaldo = "SELECT saldo FROM public.conta WHERE numero = ?";
                double saldoAtual;

                try (PreparedStatement stmtGet = conn.prepareStatement(sqlGetSaldo)) {
                    stmtGet.setString(1, numero);
                    try (ResultSet rs = stmtGet.executeQuery()) {
                        if (rs.next()) {
                            saldoAtual = rs.getDouble("saldo");
                        } else {
                            JOptionPane.showMessageDialog(frame, "Conta não encontrada.", "Erro", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }

                double novoSaldo = saldoAtual + valor;

                String sqlUpdateSaldo = "UPDATE public.conta SET saldo = ? WHERE numero = ?";
                try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdateSaldo)) {
                    stmtUpdate.setDouble(1, novoSaldo);
                    stmtUpdate.setString(2, numero);
                    stmtUpdate.executeUpdate();
                    saldoField.setText(String.valueOf(novoSaldo));
                    JOptionPane.showMessageDialog(frame, "Valor creditado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Por favor, insira um valor numérico válido.", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Erro ao creditar valor: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }



    private void debitar() {
        String numero = numeroField.getText().trim();
        String valorTexto = valorField.getText().trim();

        if (numero.isEmpty() || valorTexto.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Preencha o número da conta e o valor antes de debitar.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double valor = Double.parseDouble(valorTexto);
            try (Connection conn = connect()) {
                String sqlGetSaldo = "SELECT saldo FROM public.conta WHERE numero = ?";
                double saldoAtual;

                try (PreparedStatement stmtGet = conn.prepareStatement(sqlGetSaldo)) {
                    stmtGet.setString(1, numero);
                    try (ResultSet rs = stmtGet.executeQuery()) {
                        if (rs.next()) {
                            saldoAtual = rs.getDouble("saldo");
                        } else {
                            JOptionPane.showMessageDialog(frame, "Conta não encontrada.", "Erro", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }

                if (saldoAtual < valor) {
                    JOptionPane.showMessageDialog(frame, "Saldo insuficiente para débito.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double novoSaldo = saldoAtual - valor;

                String sqlUpdateSaldo = "UPDATE public.conta SET saldo = ? WHERE numero = ?";
                try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdateSaldo)) {
                    stmtUpdate.setDouble(1, novoSaldo);
                    stmtUpdate.setString(2, numero);
                    stmtUpdate.executeUpdate();
                    saldoField.setText(String.valueOf(novoSaldo));
                    JOptionPane.showMessageDialog(frame, "Valor debitado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Por favor, insira um valor numérico válido.", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Erro ao debitar valor: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarSaldoBanco(Conta conta) {
        try (Connection conn = connect()) {
            String sql = "UPDATE public.conta SET saldo = ? WHERE numero = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, conta.getSaldo());
                stmt.setString(2, conta.getNumero());
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Erro ao atualizar saldo no banco: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void gerarPDF() {
        try {
            ContasPDFBuilder pdfBuilder = new ContasPDFBuilder();
            pdfBuilder.listagemContas(contas.iterator());
            JOptionPane.showMessageDialog(frame, "PDF gerado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Erro ao gerar PDF: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void gerarXML() {
        try {
            ContasXMLBuilder xmlBuilder = new ContasXMLBuilder();
            String xml = xmlBuilder.listagemContas(contas.iterator());
            JOptionPane.showMessageDialog(frame, "XML gerado:\n" + xml, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Erro ao gerar XML: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Conta criarConta(String numero, double saldo) {
        String tipo = (String) tipoContaComboBox.getSelectedItem();
        if ("Conta Normal".equals(tipo)) {
            ContaNormal conta = new ContaNormal();
            conta.setNumero(numero);
            conta.setSaldo(saldo);
            return conta;
        } else if ("Conta Especial".equals(tipo)) {
            ContaEspecial conta = new ContaEspecial() {};
            conta.setNumero(numero);
            conta.setSaldo(saldo);
            return conta;
        } else {
            ContaDebEspecial conta = new ContaDebEspecial();
            conta.setNumero(numero);
            conta.setSaldo(saldo);
            conta.setLimite(1000); // Exemplo de limite
            return conta;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ContaUI::new);
    }
    
    private void criarConta() {
        String nome = JOptionPane.showInputDialog(frame, "Digite o nome do usuário:", "Criar Conta", JOptionPane.PLAIN_MESSAGE);
        String email = JOptionPane.showInputDialog(frame, "Digite o e-mail do usuário:", "Criar Conta", JOptionPane.PLAIN_MESSAGE);

        if (nome == null || nome.trim().isEmpty() || email == null || email.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Nome e e-mail são obrigatórios!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String numeroConta = Conta.criarNumeroConta();
        double saldoInicial = 0.0; // Saldo inicial padrão

        try (Connection conn = connect()) {
            // Inserir conta na tabela 'conta'
            String sqlConta = "INSERT INTO public.conta (numero, saldo) VALUES (?, ?)";
            try (PreparedStatement stmtConta = conn.prepareStatement(sqlConta)) {
                stmtConta.setString(1, numeroConta);
                stmtConta.setDouble(2, saldoInicial);
                stmtConta.executeUpdate();
            }

            // Inserir usuário associado à conta
            String sqlUsuario = "INSERT INTO public.usuario (nome, email, numero_conta) VALUES (?, ?, ?)";
            try (PreparedStatement stmtUsuario = conn.prepareStatement(sqlUsuario)) {
                stmtUsuario.setString(1, nome);
                stmtUsuario.setString(2, email);
                stmtUsuario.setString(3, numeroConta);
                stmtUsuario.executeUpdate();
            }

            JOptionPane.showMessageDialog(frame, "Conta criada com sucesso!\nNúmero da Conta: " + numeroConta, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Erro ao criar a conta: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
}


