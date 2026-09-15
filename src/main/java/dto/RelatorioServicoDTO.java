package dto;

public record RelatorioServicoDTO(
        String tipoServico,
        int quantidade,
        double valorTotal
) {
}
