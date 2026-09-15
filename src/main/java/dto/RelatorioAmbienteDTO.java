package dto;

public record RelatorioAmbienteDTO(
        String ambienteId,
        String nome,
        int quantidade,
        long horas
) {
}
