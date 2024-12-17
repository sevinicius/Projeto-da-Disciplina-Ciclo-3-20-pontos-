package acesso_a_dado;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import java.util.Properties;

/**
 *
 * @author vininicius
 */
public class AcessoADado {
        
    /**
     * Connect to the PostgreSQL database
     *
     * @return a Connection object
     */
    
    public AcessoADado() {
         
    }
    
    public Connection connect() throws SQLException {
        String url = "jdbc:postgresql://localhost:5433/banco";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        props.setProperty("password", "postgres");
        props.setProperty("ssl", "false");

        return DriverManager.getConnection(url, props);
    }

    // Método para criar tabelas caso não existam
    public void verificarOuCriarTabelas() {
        String criarTabelaContas = """
            CREATE TABLE IF NOT EXISTS public.conta (
                numero VARCHAR(50) PRIMARY KEY,
                saldo DECIMAL(15, 2) NOT NULL
            );
        """;

        String criarTabelaUsuarios= """
			CREATE TABLE IF NOT EXISTS public.usuario (
			    id SERIAL PRIMARY KEY,
			    nome VARCHAR(100) NOT NULL,
			    email VARCHAR(100) UNIQUE NOT NULL,
			    numero_conta VARCHAR(50) UNIQUE NOT NULL
			);
        """;
        String criarForeingKey= """ 
        	ALTER TABLE public.usuario
			ADD CONSTRAINT fk_usuario_conta
			FOREIGN KEY (numero_conta) REFERENCES public.conta (numero);
        """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(criarTabelaContas);
            stmt.execute(criarTabelaUsuarios);
            stmt.execute(criarForeingKey);
            System.out.println("Tabelas verificadas/criadas com sucesso.");
        } catch (SQLException e) {
            System.err.println("Erro ao verificar/criar tabelas: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        AcessoADado acesso = new AcessoADado();
        acesso.verificarOuCriarTabelas();
    }
    
    
    public String cadastrar_conta(String numero, float saldo) {
        String SQL = "insert into public.conta(numero,saldo) "
                + " values (?,?)";

        String mensagem="";

       try{
            Class.forName("org.postgresql.Driver");     
       }

       catch(ClassNotFoundException e)
       {
          e.printStackTrace();
       }

        
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(SQL,
                Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, numero);
            pstmt.setFloat(2, saldo);

            int affectedRows = pstmt.executeUpdate();
            // check the affected rows 
            if (affectedRows > 0) {
                // get the ID back
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        //id = rs.getLong(1);
                        //mensagem = "Cadastro realizado com sucesso. " + id + " linhas afetadas.";
                        mensagem = " Cadastro de conta " + numero + " realizado com sucesso. ";
                    }
                } catch (SQLException ex) {
                    mensagem = ex.getMessage();
                }
            }
        } catch (SQLException ex) {
            mensagem = ex.getMessage();
        }
       
        return mensagem;
    }        
    
    public String alterar_conta(String numero, float saldo) {
        String SQL = "update public.conta set saldo = ? "
              + " where conta = ?";

        String mensagem="";

       try{
            Class.forName("org.postgresql.Driver");     
       }

       catch(ClassNotFoundException e)
       {
          e.printStackTrace();
       }

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(SQL,
                Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, saldo);
            pstmt.setString(2, numero);

            int affectedRows = pstmt.executeUpdate();
            // check the affected rows 
            if (affectedRows > 0) {
                // get the ID back
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        mensagem = "Sucesso na alteração de conta. ";
                    }
                } catch (SQLException ex) {
                    mensagem = ex.getMessage();
                }
            }
        } catch (SQLException ex) {
            mensagem = ex.getMessage();
        }
        return mensagem;
    }    
    
    // Retorna a lista de fazendas associadas ao uauário
    public ArrayList<FazendaJaxBean> validar_usuario(String email_usuario) {
        ArrayList<FazendaJaxBean> fazendas = new ArrayList<>();
        String SQL = "SELECT cod_fazenda, nome_fazenda, area_total, proprietario, email_usuario " +
                     "FROM public.fazenda WHERE email_usuario = ? ORDER BY nome_fazenda";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, email_usuario);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    FazendaJaxBean fazenda = new FazendaJaxBean(
                        rs.getString("cod_fazenda"),
                        rs.getString("nome_fazenda"),
                        rs.getDouble("area_total"),
                        rs.getString("proprietario"),
                        rs.getString("email_usuario")
                    );
                    fazendas.add(fazenda);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao validar usuário: " + e.getMessage());
        }
        return fazendas;
    }

    // Classe auxiliar para representar Fazenda
    public static class FazendaJaxBean {
        public String cod_fazenda;
        public String nome_fazenda;
        public double area_total;
        public String proprietario;
        public String email_usuario;

        public FazendaJaxBean(String cod_fazenda, String nome_fazenda, double area_total,
                              String proprietario, String email_usuario) {
            this.cod_fazenda = cod_fazenda;
            this.nome_fazenda = nome_fazenda;
            this.area_total = area_total;
            this.proprietario = proprietario;
            this.email_usuario = email_usuario;
        }
    }
/*
    public static void main(String[] args) {
        AcessoADado acesso = new AcessoADado();
        ArrayList<FazendaJaxBean> fazendas = acesso.validar_usuario("teste@email.com");

        for (FazendaJaxBean f : fazendas) {
            System.out.println("Fazenda: " + f.nome_fazenda + ", Proprietário: " + f.proprietario);
        }
    }
    */

    // Retorna a lista de fazendas associadas ao uauário
    public String autenticar_usuario(String email_usuario, String senha) {
       String SQL = "select usuario.validacao "
          //     + "from reinsertec.usuario where email_usuario =  ? and senha = ? ";
               + "from public.usuario where email_usuario =  ? ";

       String mensagem = new String();

       try{
            Class.forName("org.postgresql.Driver");     
       }

       catch(ClassNotFoundException e)
       {
          e.printStackTrace();
       }

        try (
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(SQL,Statement.RETURN_GENERATED_KEYS))
            {
            pstmt.setString(1, email_usuario);
            //pstmt.setString(2, senha);
            try (  
                ResultSet rs = pstmt.executeQuery()) {
                int quantidade = 0;
                while (rs.next()) {
                    quantidade++;    
                    mensagem = (rs.getString("validacao"));   
                }
                if (quantidade == 0) {
                    mensagem = ("404 Usuário não encontrado.");
                }
                } catch (SQLException ex) {
                    mensagem = ("500 Problemas de conexão com o SGBD");
                }
            } catch (SQLException ex) {
                mensagem = ("500 Problemas de conexão com o SGBD");
            }
        
        return mensagem;
    }  
    
    public String cadastrar_fazenda(String cod_fazenda, String cod_car, String nome_fazenda, Double area_total, String proprietario,
            String gcs_latitude_y, String gcs_longitude_x, String datum, String municipio, String uf,
            String email_usuario, String poligono_fazenda) {
        String SQL = "insert into public.fazenda(cod_fazenda, cod_car, nome_fazenda, area_total, proprietario, gcs_latitude_y, gcs_longitude_x, "
                + "datum, municipio, uf, email_usuario, poligono_fazenda) "
                + " values (?,?,?,?,?,?,?,?,?,?,?,?)";

        //long id = 0;
        String mensagem="";

       try{
            Class.forName("org.postgresql.Driver");     
       }

       catch(ClassNotFoundException e)
       {
          e.printStackTrace();
       }

        
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(SQL,
                Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, cod_fazenda);
            pstmt.setString(2, cod_car);
            pstmt.setString(3, nome_fazenda);
            pstmt.setDouble(4, area_total);
            pstmt.setString(5, proprietario);
            pstmt.setString(6, gcs_latitude_y);
            pstmt.setString(7, gcs_longitude_x);
            pstmt.setString(8, datum);     
            pstmt.setString(9, municipio);
            pstmt.setString(10, uf);
            pstmt.setString(11, email_usuario);
            pstmt.setString(12, poligono_fazenda);

            int affectedRows = pstmt.executeUpdate();
            // check the affected rows 
            if (affectedRows > 0) {
                // get the ID back
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        //id = rs.getLong(1);
                        //mensagem = "Cadastro realizado com sucesso. " + id + " linhas afetadas.";
                        mensagem = cod_fazenda + " Cadastro de fazenda " + cod_fazenda + " realizado com sucesso. ";
                    }
                } catch (SQLException ex) {
                    mensagem = ex.getMessage();
                }
            }
        } catch (SQLException ex) {
            mensagem = ex.getMessage();
        }
       
        return mensagem;
    }        

    public String alterar_fazenda(String cod_fazenda, String cod_car, String nome_fazenda, 
            Double area_total, String proprietario, String gcs_latitude_y, 
            String gcs_longitude_x, String datum, String municipio, String uf,
            String email_usuario, String poligono_fazenda) {
        String SQL = "update public.fazenda set cod_car = ?, nome_fazenda = ?, "
                + "area_total = ?, proprietario = ?, gcs_latitude_y = ?, "
                + "gcs_longitude_x = ?, datum = ?, municipio = ?, uf = ?, "
                + "email_usuario = ?, poligono_fazenda = ? where cod_fazenda = ?";

        String mensagem="";

        try{
            Class.forName("org.postgresql.Driver");     
        }

        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(SQL,
                Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, cod_car);
            pstmt.setString(2, nome_fazenda);
            pstmt.setDouble(3, area_total);
            pstmt.setString(4, proprietario);
            pstmt.setString(5, gcs_latitude_y);
            pstmt.setString(6, gcs_longitude_x);
            pstmt.setString(7, datum);     
            pstmt.setString(8, municipio);
            pstmt.setString(9, uf);
            pstmt.setString(10, email_usuario);
            pstmt.setString(11, poligono_fazenda);
            pstmt.setString(12, cod_fazenda);

            int affectedRows = pstmt.executeUpdate();
            // check the affected rows 
            if (affectedRows > 0) {
                // get the ID back
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        mensagem = "Alteração de fazenda " + cod_fazenda + " realizada com sucesso.";
                    }
                } catch (SQLException ex) {
                    mensagem = ex.getMessage();
                }
            }
        } catch (SQLException ex) {
            mensagem = ex.getMessage();
        }
       
        return mensagem;
    }      
    
    // Retorna a lista de glebas associadas a uma fazenda
    public ArrayList<GlebaJaxBean> validar_fazenda(String cod_fazenda, String validacao) {
       String SQL = "select cod_gleba, cod_fazenda, nome_gleba, area_total, ano_ult_reforma_pasto, " +
                     "ano_implantacao_pasto, tipo_solo, altitude, declividade, poligono_gleba " + 
                     "from public.gleba " +
                     "where cod_fazenda = ? " + 
                     "order by cod_gleba";

       ArrayList<GlebaJaxBean> mensagem = new ArrayList<GlebaJaxBean>();
       GlebaJaxBean g;

       try{
            Class.forName("org.postgresql.Driver");     
       }

       catch(ClassNotFoundException e)
       {
          e.printStackTrace();
       }

        try (
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(SQL,Statement.RETURN_GENERATED_KEYS))
            {
            pstmt.setString(1, cod_fazenda);
            try (  
                ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    
                    g = new GlebaJaxBean();
                    
                    g.cod_gleba = "";
                    g.cod_fazenda = "";
                    g.nome_gleba = "";
                    g.area_total = "";
                    g.ano_ult_reforma_pasto = "";
                    g.ano_implantacao_pasto = "";
                    g.tipo_solo = "";
                    g.altitude = "";
                    g.declividade = "";
                    g.poligono_gleba = "";
                    
                    g.cod_gleba = rs.getString("cod_gleba").trim();
                    g.cod_fazenda = rs.getString("cod_fazenda").trim();
                    g.nome_gleba = rs.getString("nome_gleba").trim();
                    g.area_total = rs.getString("area_total").trim();
                    g.ano_ult_reforma_pasto = rs.getString("ano_ult_reforma_pasto").trim();
                    g.ano_implantacao_pasto = rs.getString("ano_implantacao_pasto").trim();
                    g.tipo_solo = rs.getString("tipo_solo").trim();
                    g.altitude = rs.getString("altitude").trim();
                    g.declividade = rs.getString("declividade").trim();
                    g.poligono_gleba = rs.getString("poligono_gleba").trim();
                        
                    mensagem.add(g);
                }
                } catch (SQLException ex) {
                    //mensagem.add (ex.getMessage());
                }
            } catch (SQLException ex) {
                //mensagem.add(ex.getMessage());
            }
        
        return mensagem;
    }   

    // Retorna a próxima fazenda
    public String proxima_fazenda() {
       String SQL = "select public.proxima_fazenda() ";

       String mensagem = new String();

       try{
            Class.forName("org.postgresql.Driver");     
       }

       catch(ClassNotFoundException e)
       {
          e.printStackTrace();
       }

        try (
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(SQL,Statement.RETURN_GENERATED_KEYS))
            {
            //pstmt.setString(1, cod_fazenda);
            try (  
                ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                        
                    mensagem =  (rs.getString("proxima_fazenda").trim());
                        
                }
                } catch (SQLException ex) {
                    mensagem = (ex.getMessage());
                }
            } catch (SQLException ex) {
                mensagem = ex.getMessage();
            }
        
        return mensagem;
    }  
    
    public String cadastrar_gleba(String cod_gleba, String cod_fazenda, String nome_gleba, Double area_total,
            Integer ano_ult_reforma_pasto, Integer ano_implantacao_pasto, String tipo_solo, Double altitude, 
            String declividade, String poligono_gleba) {
        String SQL = "insert into public.gleba(cod_gleba, cod_fazenda, nome_gleba, area_total," +
                     "ano_ult_reforma_pasto, ano_implantacao_pasto, tipo_solo, altitude," +
                     "declividade, poligono_gleba) " +
                     "values (?,?,?,?,?,?,?,?,?,?)";

        String mensagem="";

       try{
            Class.forName("org.postgresql.Driver");     
       }

       catch(ClassNotFoundException e)
       {
          e.printStackTrace();
       }

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(SQL,
                Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, cod_gleba);
            pstmt.setString(2, cod_fazenda);
            pstmt.setString(3, nome_gleba);
            pstmt.setDouble(4, area_total);
            pstmt.setInt(5, ano_ult_reforma_pasto);
            pstmt.setInt(6, ano_implantacao_pasto);
            pstmt.setString(7, tipo_solo);     
            pstmt.setDouble(8, altitude);
            pstmt.setString(9, declividade);
            pstmt.setString(10, poligono_gleba);
            
            int affectedRows = pstmt.executeUpdate();
            // check the affected rows 
            if (affectedRows > 0) {
                // get the ID back
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        
                        mensagem = cod_gleba + " Cadastro da gleba " + cod_gleba + " para a fazenda " + cod_fazenda + " realizado com sucesso. ";
                    }
                } catch (SQLException ex) {
                    mensagem = ex.getMessage();
                }
            }
        } catch (SQLException ex) {
            mensagem = ex.getMessage();
        }
       
        return mensagem;
    }        

    public String alterar_gleba(String cod_gleba, String cod_fazenda, String 
            nome_gleba, Double area_total, Integer ano_ult_reforma_pasto, 
            Integer ano_implantacao_pasto, String tipo_solo, Double altitude, 
            String declividade, String poligono_gleba) {
        String SQL = "update public.gleba set nome_gleba = ?, area_total = ?, "
                + "ano_ult_reforma_pasto = ?, ano_implantacao_pasto = ?, "
                + "tipo_solo = ?, altitude = ?, declividade = ?, poligono_gleba = ? where "
                + "cod_gleba = ? and cod_fazenda = ?";

        String mensagem="";

        try{
            Class.forName("org.postgresql.Driver");     
        }

        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(SQL,
                Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, nome_gleba);
            pstmt.setDouble(2, area_total);
            pstmt.setInt(3, ano_ult_reforma_pasto);
            pstmt.setInt(4, ano_implantacao_pasto);
            pstmt.setString(5, tipo_solo);     
            pstmt.setDouble(6, altitude);
            pstmt.setString(7, declividade);
            pstmt.setString(8, poligono_gleba);
            pstmt.setString(9, cod_gleba);
            pstmt.setString(10, cod_fazenda);
            
                    
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
            
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        
                        mensagem = "Alteração da Gleba " + cod_gleba + ", Fazenda " + cod_fazenda + " realizado com sucesso.";
            
                    }
                } catch (SQLException ex) {
                    mensagem = ex.getMessage();
                }
            }
        } catch (SQLException ex) {
            mensagem = ex.getMessage();
        }
       
        return mensagem;
    }  
    
    // Retorna a lista de diagnósticos associadas a uma gleba
    public ArrayList<DiagnosticoJaxBean> validar_gleba(String cod_fazenda, String cod_gleba, String validacao) {
       String SQL = "select cod_diagnostico, cod_gleba, cod_fazenda, data_diagnostico, grid_pontos_amostrais, " +
                    "taxa_cobertura_calculada, situacao " + 
                    "from public.diagnostico " +
                    "where cod_fazenda = ? and cod_gleba = ? " +
                    "order by cod_diagnostico";

       ArrayList<DiagnosticoJaxBean> mensagem = new ArrayList<DiagnosticoJaxBean>();
       
       DiagnosticoJaxBean d;

       try{
            Class.forName("org.postgresql.Driver");     
       }

       catch(ClassNotFoundException e)
       {
          e.printStackTrace();
       }

        try (
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(SQL,Statement.RETURN_GENERATED_KEYS))
            {
            pstmt.setString(1, cod_fazenda);
            pstmt.setString(2, cod_gleba);
            try (  
                ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    
                    d = new DiagnosticoJaxBean();
                    
                    d.cod_diagnostico = "";
                    d.cod_gleba = "";
                    d.cod_fazenda = "";
                    d.data_diagnostico = "";
                    d.grid_pontos_amostrais = "";
                    d.situacao = "";
                    d.taxa_cobertura_calculada = "";
                        
                    d.cod_diagnostico = rs.getString("cod_diagnostico").trim();
                    d.cod_gleba = rs.getString("cod_gleba").trim();
                    d.cod_fazenda = rs.getString("cod_fazenda").trim();
                    d.data_diagnostico = rs.getString("data_diagnostico").trim();
                    d.grid_pontos_amostrais = rs.getString("grid_pontos_amostrais").trim();
                    d.situacao = rs.getString("situacao").trim();
                    d.taxa_cobertura_calculada = rs.getString("taxa_cobertura_calculada").trim();
                    
                    mensagem.add(d);
                    
                }
                } catch (SQLException ex) {
                    //mensagem.add (ex.getMessage());
                }
            } catch (SQLException ex) {
                //mensagem.add(ex.getMessage());
            }
        
        return mensagem;
    } 
    
    // Retorna a próxima gleba
    public String proxima_gleba(String cod_fazenda) {
       String SQL = "select public.proxima_gleba('" + cod_fazenda.trim() + "') ";

       String mensagem = new String();

       try{
            Class.forName("org.postgresql.Driver");     
       }

       catch(ClassNotFoundException e)
       {
          e.printStackTrace();
       }

        try (
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(SQL,Statement.RETURN_GENERATED_KEYS))
            {
            //pstmt.setString(1, cod_fazenda);
            try (  
                ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                        
                    mensagem =  (rs.getString("proxima_gleba").trim());
                        
                }
                } catch (SQLException ex) {
                    mensagem = (ex.getMessage());
                }
            } catch (SQLException ex) {
                mensagem = ex.getMessage();
            }
        
        return mensagem;
    }  
    
    public String cadastrar_diagnostico(String cod_diagnostico, String cod_gleba, String cod_fazenda, String data_diagnostico, 
                                    String grid_pontos_amostrais, Integer taxa_cobertura_calculada, String situacao) {
        String SQL = "insert into public.diagnostico(cod_diagnostico, cod_gleba, cod_fazenda, data_diagnostico, " +
                     "grid_pontos_amostrais, taxa_cobertura_calculada, situacao) " +
                     "values (?,?,?,?,?,?,?)";

        String mensagem="";

       try{
            Class.forName("org.postgresql.Driver");     
       }

       catch(ClassNotFoundException e)
       {
          e.printStackTrace();
       }

        
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(SQL,
                Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, cod_diagnostico);
            pstmt.setString(2, cod_gleba);
            pstmt.setString(3, cod_fazenda);
            pstmt.setString(4, data_diagnostico);
            pstmt.setString(5, grid_pontos_amostrais);
            pstmt.setInt(6, taxa_cobertura_calculada);
            pstmt.setString(7, situacao);

                       
            int affectedRows = pstmt.executeUpdate();
            // check the affected rows 
            if (affectedRows > 0) {
                // get the ID back
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        
                        mensagem = cod_diagnostico + " Cadastro de diagnóstico " + cod_diagnostico + 
                                " para a gleba " + cod_gleba + " , fazenda " + cod_fazenda + " realizado com sucesso. ";
                    }
                } catch (SQLException ex) {
                    mensagem = ex.getMessage();
                }
            }
        } catch (SQLException ex) {
            mensagem = ex.getMessage();
        }
       
        return mensagem;
    }  

    public String alterar_diagnostico(String cod_diagnostico, String cod_gleba, String 
            cod_fazenda, String data_diagnostico, String grid_pontos_amostrais, 
            Integer taxa_cobertura_calculada, String situacao) {
        String SQL = "update public.diagnostico set data_diagnostico = ?, " +
                    "grid_pontos_amostrais = ?, taxa_cobertura_calculada = ?, situacao = ? " +
                    "where cod_diagnostico = ? and cod_gleba = ? and cod_fazenda = ?";

        String mensagem="";

       try{
            Class.forName("org.postgresql.Driver");     
       }

       catch(ClassNotFoundException e)
       {
          e.printStackTrace();
       }
        
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(SQL,
                Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, data_diagnostico);
            pstmt.setString(2, grid_pontos_amostrais);
            pstmt.setInt(3, taxa_cobertura_calculada);
            pstmt.setString(4, situacao);
            pstmt.setString(5, cod_diagnostico);
            pstmt.setString(6, cod_gleba);
            pstmt.setString(7, cod_fazenda);
                       
            int affectedRows = pstmt.executeUpdate();
            // check the affected rows 
            if (affectedRows > 0) {
                // get the ID back
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        
                        mensagem = "Alteração de Diagnóstico " + cod_diagnostico + " ,Fazenda " + cod_fazenda +  
                                   " , Gleba " + cod_gleba + " realizado com sucesso. ";
                    }
                } catch (SQLException ex) {
                    mensagem = ex.getMessage();
                }
            }
        } catch (SQLException ex) {
            mensagem = ex.getMessage();
        }
       
        return mensagem;
    }  

    public String alterar_situacao_diagnostico(String cod_diagnostico, String cod_gleba, String 
            cod_fazenda, String situacao) {
        
        int taxa_cobertura_calculada = 0;
        
        if (situacao.trim().equals ("fechada")) {
                taxa_cobertura_calculada = 1;
            } else taxa_cobertura_calculada = 0;

        
        String SQL = "update public.diagnostico set taxa_cobertura_calculada = ?, " +
                    "situacao = ? where cod_diagnostico = ? and cod_gleba = ? and cod_fazenda = ?";

        String mensagem="";
        
        
       try{
            Class.forName("org.postgresql.Driver");     
       }

       catch(ClassNotFoundException e)
       {
          e.printStackTrace();
       }
        
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(SQL,
                Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, taxa_cobertura_calculada);
            pstmt.setString(2, situacao);
            pstmt.setString(3, cod_diagnostico);
            pstmt.setString(4, cod_gleba);
            pstmt.setString(5, cod_fazenda);
                       
            int affectedRows = pstmt.executeUpdate();
            // check the affected rows 
            if (affectedRows > 0) {
                // get the ID back
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        
                        mensagem = "Situação alterada para " + situacao + 
                                " na Fazenda " + cod_fazenda + " Gleba " + cod_gleba + 
                                " Diagnóstico " + cod_diagnostico;
                    }
                } catch (SQLException ex) {
                    mensagem = ex.getMessage();
                }
            }
        } catch (SQLException ex) {
            mensagem = ex.getMessage();
        }
       
        return mensagem;
    }  

    // Retorna a lista de pontos amostrais associados a um diagnóstico
    public ArrayList<PontoAmostralJaxBean> validar_diagnostico(String cod_fazenda, String cod_gleba, String cod_diagnostico, String validacao) {
       String SQL = "select cod_gleba, cod_ponto_amostral, cod_diagnostico, cod_fazenda, gcs_latitude_y, " +
                    "gcs_longitude_x, arquivo_foto, altitude " +
                    "from public.ponto_amostral " +
                    "where cod_fazenda = ? and cod_gleba = ? and cod_diagnostico = ? " + 
                    "order by cod_ponto_amostral";

       ArrayList<PontoAmostralJaxBean> mensagem = new ArrayList<PontoAmostralJaxBean>();
       PontoAmostralJaxBean pa;

       try{
            Class.forName("org.postgresql.Driver");     
       }

       catch(ClassNotFoundException e)
       {
          e.printStackTrace();
       }

        try (
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(SQL,Statement.RETURN_GENERATED_KEYS))
            {
            pstmt.setString(1, cod_fazenda);
            pstmt.setString(2, cod_gleba);
            pstmt.setString(3, cod_diagnostico);
            try (  
                ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    
                    pa = new PontoAmostralJaxBean();
                    
                    pa.cod_diagnostico = "";
                    pa.cod_gleba = "";
                    pa.cod_fazenda = "";
                    pa.cod_ponto_amostral = "";
                    pa.gcs_latitude_y = "";
                    pa.gcs_longitude_x = "";
                    pa.arquivo_foto = "";
                    pa.altitude = "";
                    
                    pa.cod_diagnostico = rs.getString("cod_diagnostico").trim();
                    pa.cod_gleba = rs.getString("cod_gleba").trim();
                    pa.cod_fazenda = rs.getString("cod_fazenda").trim();
                    pa.cod_ponto_amostral = rs.getString("cod_ponto_amostral").trim();
                    pa.gcs_latitude_y = rs.getString("gcs_latitude_y").trim();
                    pa.gcs_longitude_x = rs.getString("gcs_longitude_x").trim();
                    pa.arquivo_foto = rs.getString("arquivo_foto").trim(); 
                    pa.altitude = rs.getString("altitude").trim();
                    
                    mensagem.add(pa);
                }
                } catch (SQLException ex) {
                    
                }
            } catch (SQLException ex) {
               
            }
        
        return mensagem;
    } 
    
    // Retorna a próxima gleba
    public String proximo_diagnostico(String cod_fazenda, String cod_gleba) {
       String SQL = "select public.proximo_diagnostico('" + cod_fazenda.trim() + "', '" + cod_gleba.trim() + "') ";

       String mensagem = new String();

       try{
            Class.forName("org.postgresql.Driver");     
       }

       catch(ClassNotFoundException e)
       {
          e.printStackTrace();
       }

        try (
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(SQL,Statement.RETURN_GENERATED_KEYS))
            {
            //pstmt.setString(1, cod_fazenda);
            try (  
                ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                        
                    mensagem =  (rs.getString("proximo_diagnostico").trim());
                        
                }
                } catch (SQLException ex) {
                    mensagem = (ex.getMessage());
                }
            } catch (SQLException ex) {
                mensagem = ex.getMessage();
            }
        
        return mensagem;
    }  
    
    public String excluir_diagnostico(String cod_fazenda, String cod_gleba, String cod_diagnostico) {
        String SQL = "delete from public.diagnostico where cod_fazenda = ? and cod_gleba = ? and cod_diagnostico = ?";

        String mensagem = new String("");

        try{
            Class.forName("org.postgresql.Driver");     
        }

        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        try (
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(SQL,Statement.RETURN_GENERATED_KEYS))
            {
            
            pstmt.setString(1, cod_fazenda);
            pstmt.setString(2, cod_gleba);
            pstmt.setString(3, cod_diagnostico);
                
           try (  
                ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    mensagem = "Diagnóstico " + cod_diagnostico + ", Gleba " + cod_gleba +  ",Fazenda " + cod_fazenda + " excluído com sucesso.";
                }
                mensagem = "Diagnóstico " + cod_diagnostico + ", Gleba " + cod_gleba +  ",Fazenda " + cod_fazenda + " excluído com sucesso.";
                
                } catch (SQLException ex) {
                    mensagem = mensagem + ex.getMessage();
                } 
           
            } catch (SQLException ex) {
                mensagem = mensagem + ex.getMessage();
            } 
        
        return mensagem;
    } 
    
    public String cadastrar_ponto_amostral(String cod_ponto_amostral, String cod_diagnostico, String cod_gleba, 
                                           String cod_fazenda, String gcs_latitude_y, String gcs_longitude_x, 
                                           String arquivo_foto, Double altitude) {
        String SQL = "insert into public.ponto_amostral(cod_ponto_amostral, cod_diagnostico, cod_gleba, cod_fazenda, " + 
                     "gcs_latitude_y, gcs_longitude_x, arquivo_foto, altitude) " +
                     "values (?,?,?,?,?,?,?,?)";

        String mensagem="";

       try{
            Class.forName("org.postgresql.Driver");     
       }

       catch(ClassNotFoundException e)
       {
          e.printStackTrace();
       }

        
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(SQL,
                Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, cod_ponto_amostral);
            pstmt.setString(2, cod_diagnostico);
            pstmt.setString(3, cod_gleba);
            pstmt.setString(4, cod_fazenda);
            pstmt.setString(5, gcs_latitude_y);
            pstmt.setString(6, gcs_longitude_x);
            pstmt.setString(7, arquivo_foto);
            pstmt.setDouble(8, altitude);
                       
            int affectedRows = pstmt.executeUpdate();
            // check the affected rows 
            if (affectedRows > 0) {
                // get the ID back
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        
                        mensagem = cod_ponto_amostral + " Cadastro de Ponto Amostral " + cod_ponto_amostral + 
                                ", Diagnóstico " + cod_diagnostico + " , Gleba " + 
                                cod_gleba + " , Fazenda " + cod_fazenda + " realizado com sucesso. ";
                    }
                } catch (SQLException ex) {
                    mensagem = ex.getMessage();
                }
            }
        } catch (SQLException ex) {
            mensagem = ex.getMessage();
        }
        
        return mensagem;
    }  
    
    public String alterar_ponto_amostral(String cod_ponto_amostral, String cod_diagnostico, String cod_gleba, 
                                           String cod_fazenda, String gcs_latitude_y, String gcs_longitude_x, 
                                           String arquivo_foto, Double altitude) {
        String SQL = "update public.ponto_amostral set gcs_latitude_y = ? , "
                + " gcs_longitude_x = ? , arquivo_foto = ? , altitude = ? " 
                + " where cod_ponto_amostral = ? and cod_diagnostico = ? and cod_gleba = ? and cod_fazenda = ? ";

        String mensagem="";

       try{
            Class.forName("org.postgresql.Driver");     
       }

       catch(ClassNotFoundException e)
       {
          e.printStackTrace();
       }

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(SQL,
                Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, gcs_latitude_y);
            pstmt.setString(2, gcs_longitude_x);
            pstmt.setString(3, arquivo_foto);
            pstmt.setDouble(4, altitude);
            pstmt.setString(5, cod_ponto_amostral);
            pstmt.setString(6, cod_diagnostico);
            pstmt.setString(7, cod_gleba);
            pstmt.setString(8, cod_fazenda);
                       
            int affectedRows = pstmt.executeUpdate();
            
            // check the affected rows 
            if (affectedRows > 0) {
                mensagem = "Ponto Amostral " + cod_ponto_amostral + ", Diagnóstico " 
                                   + cod_diagnostico + " , Gleba " + cod_gleba + " , Fazenda " 
                                   + cod_fazenda + " alterado com sucesso. ";
                // get the ID back
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        
                        mensagem = "Ponto Amostral " + cod_ponto_amostral + ", Diagnóstico " 
                                   + cod_diagnostico + " , Gleba " + cod_gleba + " , Fazenda " 
                                   + cod_fazenda + " alterado com sucesso. ";
                    }
                } catch (SQLException ex) {
                    mensagem = ex.getMessage();
                }
            }
        } catch (SQLException ex) {
            mensagem = ex.getMessage();
        }
        
        return mensagem;
    }  
    
    public String proximo_ponto_amostral(String cod_fazenda, String cod_gleba, String cod_diagnostico) {
       String SQL = "select public.proximo_ponto_amostral('" + cod_fazenda.trim() + "', '" + cod_gleba.trim() + "','" + cod_diagnostico.trim() + "')";

       String mensagem = new String();

       try{
            Class.forName("org.postgresql.Driver");     
       }

       catch(ClassNotFoundException e)
       {
          e.printStackTrace();
       }

        try (
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(SQL,Statement.RETURN_GENERATED_KEYS))
            {
            //pstmt.setString(1, cod_fazenda);
            try (  
                ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                        
                    mensagem =  (rs.getString("proximo_ponto_amostral").trim());
                        
                }
                } catch (SQLException ex) {
                    mensagem = (ex.getMessage());
                }
            } catch (SQLException ex) {
                mensagem = ex.getMessage();
            }
        
        return mensagem;
    }  
    
    public String excluir_ponto_amostral(String cod_fazenda, String cod_gleba, String cod_diagnostico, 
           String cod_ponto_amostral) {
        
        String SQL = "delete from public.ponto_amostral where cod_fazenda = ? "
                + "and cod_gleba = ? and cod_diagnostico = ? and cod_ponto_amostral = ?";

        String mensagem = new String("");

        try{
            Class.forName("org.postgresql.Driver");     
        }

        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        try (
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(SQL,Statement.RETURN_GENERATED_KEYS))
            {
            
            pstmt.setString(1, cod_fazenda);
            pstmt.setString(2, cod_gleba);
            pstmt.setString(3, cod_diagnostico);
            pstmt.setString(4, cod_ponto_amostral);
            
           try (  
                ResultSet rs = pstmt.executeQuery()) {
                mensagem = "Ponto Amostral " + cod_ponto_amostral + " referente a Diagnóstico " + cod_diagnostico + ", Gleba " + cod_gleba +  ",Fazenda " + cod_fazenda + " excluído com sucesso.";
                
                while (rs.next()) {
                    mensagem = "Ponto Amostral " + cod_ponto_amostral + " referente a Diagnóstico " + cod_diagnostico + ", Gleba " + cod_gleba +  ",Fazenda " + cod_fazenda + " excluído com sucesso.";
                
                }
                mensagem = "Ponto Amostral " + cod_ponto_amostral + " referente a Diagnóstico " + cod_diagnostico + ", Gleba " + cod_gleba +  ",Fazenda " + cod_fazenda + " excluído com sucesso.";
                
                } catch (SQLException ex) {
                    mensagem = mensagem + ex.getMessage();
                } 
           
            } catch (SQLException ex) {
                mensagem = mensagem + ex.getMessage();
            } 
        
        return mensagem;
    } 
    
    public String cadastrar_taxa_cobertura_calculada(String cod_diagnostico, String cod_gleba, String cod_fazenda, 
                                                     Double porcentagem_plantas_daninhas, Double porcentagem_solo_exposto, 
                                                     Double porcentagem_pastagem_verde, 
                                                     Double porcentagem_palhada,
                                                     String diagnostico_informacoes_complementares) {
        String SQL = "insert into public.taxa_cobertura_calculada(cod_diagnostico, cod_gleba, cod_fazenda, " + 
                     "porcentagem_plantas_daninhas, porcentagem_solo_exposto, porcentagem_pastagem_verde, " +
                     "porcentagem_palhada, diagnostico_informacoes_complementares) " +
                     "values (?,?,?,?,?,?,?,?)";

        String mensagem="";

       try{
            Class.forName("org.postgresql.Driver");     
       }

       catch(ClassNotFoundException e)
       {
          e.printStackTrace();
       }

        
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(SQL,
                Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, cod_diagnostico);
            pstmt.setString(2, cod_gleba);
            pstmt.setString(3, cod_fazenda);
            pstmt.setDouble(4, porcentagem_plantas_daninhas);
            pstmt.setDouble(5, porcentagem_solo_exposto);
            pstmt.setDouble(6, porcentagem_pastagem_verde);
            pstmt.setDouble(7, porcentagem_palhada);
            pstmt.setString(8, diagnostico_informacoes_complementares);
                       
            int affectedRows = pstmt.executeUpdate();
            // check the affected rows 
            if (affectedRows > 0) {
                // get the ID back
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        
                        mensagem = "Cadastro de taxa de cobertura calculada para fazenda " + cod_fazenda + 
                                   " , gleba " + cod_gleba + " , diagnostico " + cod_diagnostico + 
                                   " realizado com sucesso. ";
                    }
                } catch (SQLException ex) {
                    mensagem = ex.getMessage();
                }
            }
        } catch (SQLException ex) {
            mensagem = ex.getMessage();
        }
       
        return mensagem;
    }  

    
    public String excluir_taxa_cobertura_calculada(String cod_fazenda, 
           String cod_gleba, String cod_diagnostico) {
        String SQL = "delete from public.taxa_cobertura_calculada where "
                   + "cod_fazenda = ? and cod_gleba = ? and cod_diagnostico = ?";

        String mensagem = new String("");

        try{
            Class.forName("org.postgresql.Driver");     
        }

        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        try (
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(SQL,Statement.RETURN_GENERATED_KEYS))
            {
            
            pstmt.setString(1, cod_fazenda);
            pstmt.setString(2, cod_gleba);
            pstmt.setString(3, cod_diagnostico);
                
           try (  
                ResultSet rs = pstmt.executeQuery()) {
                mensagem = "Taxa de Cobertura Calculada para Diagnóstico " + cod_diagnostico + ", Gleba " + cod_gleba +  ",Fazenda " + cod_fazenda + " excluído com sucesso.";
                
                while (rs.next()) {
                    mensagem = "Taxa de Cobertura Calculada para Diagnóstico " + cod_diagnostico + ", Gleba " + cod_gleba +  ",Fazenda " + cod_fazenda + " excluído com sucesso.";
                }
                mensagem = "Taxa de Cobertura Calculada para Diagnóstico " + cod_diagnostico + ", Gleba " + cod_gleba +  ",Fazenda " + cod_fazenda + " excluído com sucesso.";
                
                } catch (SQLException ex) {
                    mensagem = mensagem + ex.getMessage();
                } 
           
            } catch (SQLException ex) {
                mensagem = mensagem + ex.getMessage();
            } 
        
        return mensagem;
    } 
  
    public FazendaJaxBean retornar_fazenda(String cod_fazenda) {
    
        String SQL = "select cod_fazenda, cod_car, nome_fazenda, area_total, proprietario, gcs_latitude_y, " +
                     "gcs_longitude_x, datum, municipio, uf, email_usuario, poligono_fazenda " + 
                     "from public.fazenda " +
                     "where cod_fazenda = ?";

        FazendaJaxBean mensagem = new FazendaJaxBean();

        try{
            Class.forName("org.postgresql.Driver");     
        }

        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        try (
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(SQL,Statement.RETURN_GENERATED_KEYS))
            {
            
            pstmt.setString(1, cod_fazenda);
                
           try (  
                ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    mensagem.cod_fazenda = rs.getString("cod_fazenda").trim();
                    mensagem.cod_car = rs.getString("cod_car").trim();
                    mensagem.nome_fazenda = rs.getString("nome_fazenda").trim();
                    mensagem.area_total = rs.getString("area_total").trim();
                    mensagem.proprietario = rs.getString("proprietario").trim();
                    mensagem.gcs_latitude_y = rs.getString("gcs_latitude_y").trim();
                    mensagem.gcs_longitude_x = rs.getString("gcs_longitude_x").trim();
                    mensagem.datum = rs.getString("datum").trim();
                    mensagem.municipio = rs.getString("municipio").trim();
                    mensagem.uf = rs.getString("uf").trim();
                    mensagem.email_usuario = rs.getString("email_usuario").trim();
                    mensagem.poligono_fazenda = rs.getString("poligono_fazenda").trim();
                    mensagem.validacao = new String("public").trim();
                   }
                } catch (SQLException ex) {
                    //mensagem.diagnostico_informacoes_complementares = ex.getMessage();
                }
            } catch (SQLException ex) {
                //mensagem.diagnostico_informacoes_complementares = ex.getMessage();
            }
        
        return mensagem;
    } 
    
    public String excluir_fazenda(String cod_fazenda) {
    
        String SQL = "delete from public.fazenda where cod_fazenda = ?";

        String mensagem = new String("");

        try{
            Class.forName("org.postgresql.Driver");     
        }

        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        try (
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(SQL,Statement.RETURN_GENERATED_KEYS))
            {
            
            pstmt.setString(1, cod_fazenda);
                
           try (  
                ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    mensagem = "Fazenda " + cod_fazenda + " excluída com sucesso.";
                }
                mensagem = "Fazenda " + cod_fazenda + " excluída com sucesso.";
                
                } catch (SQLException ex) {
                    mensagem = mensagem + ex.getMessage();
                } 
           
            } catch (SQLException ex) {
                mensagem = mensagem + ex.getMessage();
            } 
        
        return mensagem;
    } 
    
    public String excluir_gleba(String cod_fazenda, String cod_gleba) {
        String SQL = "delete from public.gleba where cod_fazenda = ? and cod_gleba = ?";

        String mensagem = new String("");

        try{
            Class.forName("org.postgresql.Driver");     
        }

        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        try (
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(SQL,Statement.RETURN_GENERATED_KEYS))
            {
            
            pstmt.setString(1, cod_fazenda);
            pstmt.setString(2, cod_gleba);
                
           try (  
                ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    mensagem = "Gleba " + cod_gleba +  ",Fazenda " + cod_fazenda + " excluída com sucesso.";
                }
                mensagem = "Gleba " + cod_gleba +  ",Fazenda " + cod_fazenda + " excluída com sucesso.";
                
                } catch (SQLException ex) {
                    mensagem = mensagem + ex.getMessage();
                } 
           
            } catch (SQLException ex) {
                mensagem = mensagem + ex.getMessage();
            } 
        
        return mensagem;
    } 
  
    public String quantidade_pa_gleba(String cod_fazenda, String cod_gleba) {
       String SQL = "select public.quantidade_pa_gleba('"+cod_fazenda.trim()+"','"+cod_gleba.trim()+"')";

       String mensagem = new String();

       try{
            Class.forName("org.postgresql.Driver");     
       }

       catch(ClassNotFoundException e)
       {
          e.printStackTrace();
       }

        try (
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(SQL,Statement.RETURN_GENERATED_KEYS))
            {
                   
           try (  
                ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                        
                        mensagem = rs.getString("quantidade_pa_gleba");
                        
                        
                   }
                } catch (SQLException ex) {
                    //mensagem.diagnostico_informacoes_complementares = ex.getMessage();
                }
            } catch (SQLException ex) {
                //mensagem.diagnostico_informacoes_complementares = ex.getMessage();
            }
        
        return mensagem;
    }  
    
}