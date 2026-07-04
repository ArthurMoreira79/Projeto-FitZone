package controle;

import entidades.Agendamento;
import excecoes.FalhaPersistenciaException;
import java.io.*;
import java.util.*;

public class RepositorioAgendamentos implements Serializable{
    
    private static final long serialVersionUID = 1L;

    private Map<Integer, Agendamento> agendamentos = new HashMap<>();
    private final String CAMINHO = "agendamentos.dat";
    private int proximoId = 1; //Contador incremental de Ids

    public RepositorioAgendamentos() throws FalhaPersistenciaException { carregarArquivo(); }

    //Retorna o prox ID disponivel e incrementa o contador.
    public int gerarProximoId() { return proximoId++; }

    //Insere um novo agendamento no mapa usando o ID.
    public void inserir(Agendamento a) throws FalhaPersistenciaException {
        agendamentos.put(a.getId(), a);
        salvarArquivo();
    }

    //Remove um agendamento do mapa usando o ID.
    public void remover(int id) throws FalhaPersistenciaException {
        agendamentos.remove(id);
        salvarArquivo();
    }

    //Atualiza um agendamento existente no mapa (por exemplo, após adicionar serviços).
    public void atualizar(Agendamento a) throws FalhaPersistenciaException {
        agendamentos.put(a.getId(), a);
        salvarArquivo();
    }

    //Busca e retorna um agendamento pelo ID.
    public Agendamento buscar(int id) { return agendamentos.get(id); }

    //Retorna uma lista com todos os agendamentos
    public List<Agendamento> listarTodos() { return new ArrayList<>(agendamentos.values()); }

    //Serializa o mapa de agendamentos para o arquivo agendamentos.dat; Ambos são escritos em sequência para que o ID seja restaurado corretamente; Lança FalhaPersistenciaException se ocorrer erro de I/O.
    private void salvarArquivo() throws FalhaPersistenciaException{
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CAMINHO))){
            oos.writeObject(agendamentos);
            oos.writeInt(proximoId);
        } catch(IOException e){
            throw new FalhaPersistenciaException("Erro ao salvar agendamentos.");
        }
    }

    //Desserializa o mapa de agendamentos e o proximoId a partir do arquivo agendamentos.dat; Se o arquivo não existir, inicia tudo zerado. Se existir mas estiver corrompido, lança FalhaPersistenciaException.
    @SuppressWarnings("unchecked")
    private void carregarArquivo() throws FalhaPersistenciaException{
        File file = new File(CAMINHO);
        if(file.exists()){
            try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))){
                agendamentos = (Map<Integer, Agendamento>) ois.readObject();
                proximoId = ois.readInt();
            } catch(IOException | ClassNotFoundException e){
                throw new FalhaPersistenciaException("Erro ao carregar o arquivo de agendamentos.");
            }
        } else {
            agendamentos = new HashMap<>();
            proximoId = 1;
        }
    }
}
