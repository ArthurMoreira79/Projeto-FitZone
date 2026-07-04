package controle;

import entidades.Ambiente;
import excecoes.FalhaPersistenciaException;
import java.io.*;
import java.util.*;

public class RepositorioAmbientes implements Serializable{

    private static final long serialVersionUID = 1L;
    private Map<String, Ambiente> ambientes = new HashMap<>();
    private final String CAMINHO = "ambientes.dat";

    public RepositorioAmbientes() throws FalhaPersistenciaException { carregarArquivo(); }

    //Insere um ambiente no mapa usando o id.
    public void inserir(Ambiente a) throws FalhaPersistenciaException{
        ambientes.put(a.getId(), a);
        salvarArquivo();
    }

    //Busca e retorna um ambiente pelo id.
    public Ambiente buscar(String id) { return ambientes.get(id); }

    //Retorna uma lista com todos os ambientes.
    public List<Ambiente> listarTodos() { return new ArrayList<>(ambientes.values()); }

    //Serializa o mapa de ambientes para o arquivo ambientes.dat; Lança FalhaPersistenciaException se ocorrer erro de I/O.
    private void salvarArquivo() throws FalhaPersistenciaException{
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CAMINHO))){
            oos.writeObject(ambientes);
        } catch(IOException e){
            throw new FalhaPersistenciaException("Erro ao salvar ambientes.");
        }
    }

    //Desserializa o mapa de ambiente a partir do arquivo ambientes.dat; Se o arquivo não existir, inicia com o mapa vazio. Se existir mas estiver corrompido, lança FalhaPersistenciaException.
    @SuppressWarnings("unchecked")
    private void carregarArquivo() throws FalhaPersistenciaException{
        File file = new File(CAMINHO);
        if(file.exists()){
            try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))){
                ambientes = (Map<String, Ambiente>) ois.readObject();
            } catch(IOException | ClassNotFoundException e){
                throw new FalhaPersistenciaException("Erro ao carregar o arquivo de ambientes.");
            }
        } else {
            ambientes = new HashMap<>();
        }
    }    
}
