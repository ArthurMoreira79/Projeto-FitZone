package controle;

import entidades.Aluno;
import excecoes.FalhaPersistenciaException;

import java.io.*;
import java.util.*;

public class RepositorioAlunos implements Serializable{
    
    private static final long serialVersionUID = 1L;

    private Map<String, Aluno> alunos = new HashMap<>();
    private final String CAMINHO = "alunos.dat";

    public RepositorioAlunos() throws FalhaPersistenciaException { carregarArquivo(); }

    /**
     * Insere um aluno ao mapa usando cpf.
     */
    public void inserir(Aluno a) throws FalhaPersistenciaException{
        alunos.put(a.getCpf(), a);
        salvarArquivo();
    }

    /**
     * Busca e retorna um aluno pelo cpf.
     */
    public Aluno buscar(String cpf) { return alunos.get(cpf); }

    /**
     * Retorna uma lista com todos os alunos
     */
    public List<Aluno> listarTodos() { return new ArrayList<>(alunos.values()); }

    /**
     * Serializa o mapa de alunos para o arquivo alunos.dat.
     * Lança FalhaPersistenciaException em caso de falha de I/O.
     */
    private void salvarArquivo() throws FalhaPersistenciaException{
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CAMINHO))){
            oos.writeObject(alunos);
        } catch(IOException e){
            throw new FalhaPersistenciaException("Erro ao salvar alunos.");
        }
    }

    /**
     * Desserializa o mapa de alunos a partir do arquivo alSe o arquivo não existir, o sistema inicia com o mapa vazio.
     * Se existir mas estiver corrompido, lança FalhaPersistenciaException.
     */
    @SuppressWarnings("unchecked")
    private void carregarArquivo() throws FalhaPersistenciaException{
        File file = new File(CAMINHO);
        if(file.exists()){
            try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))){
                alunos = (Map<String, Aluno>) ois.readObject();
            } catch(IOException | ClassNotFoundException e){
                throw new FalhaPersistenciaException("Erro ao carregar o arquivo de alunos.");
            }
        } else {
            alunos = new HashMap<>();
        }
    }
}
