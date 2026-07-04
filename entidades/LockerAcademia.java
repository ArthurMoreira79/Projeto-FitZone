package entidades;

public class LockerAcademia implements ServicoAdicional{
    
    private static final long serialVersionUID = 1L;

    private static final double VALOR_POR_UNIDADE = 5.0;

    private int quantidade;

    public LockerAcademia(int quantidade) { this.quantidade = quantidade; }

    @Override
    public String getDescricao() { return "Locker (" + quantidade + " unidades)"; }

    @Override
    public double getValorTotal() { return VALOR_POR_UNIDADE * quantidade; }
}
