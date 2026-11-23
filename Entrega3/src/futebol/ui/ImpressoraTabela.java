package futebol.ui;

import futebol.banco.ResultadoTabela;
import java.util.ArrayList;
import java.util.List;

/** Desenha um ResultadoTabela no terminal, com as colunas alinhadas. */
public final class ImpressoraTabela {

    private static final int LARGURA_MAXIMA = 40;
    private static final String SEPARADOR_COLUNAS = " | ";
    private static final String SEPARADOR_TRACO = "-+-";

    private final Terminal terminal;

    public ImpressoraTabela(Terminal terminal) {
        this.terminal = terminal;
    }

    public void imprimir(ResultadoTabela tabela) {
        if (tabela.semLinhas()) {
            terminal.mostrar("Nenhum registro encontrado.");
            return;
        }
        List<List<String>> linhas = tabela.linhas().stream().map(ImpressoraTabela::limitar).toList();
        int[] larguras = calcularLarguras(tabela.colunas(), linhas);

        terminal.mostrar(montarLinha(tabela.colunas(), larguras));
        terminal.mostrar(montarTraco(larguras));
        for (List<String> linha : linhas) {
            terminal.mostrar(montarLinha(linha, larguras));
        }
        terminal.mostrar("(" + linhas.size() + " registro(s))");
    }

    private static List<String> limitar(List<String> linha) {
        List<String> limitada = new ArrayList<>();
        for (String celula : linha) {
            boolean muitoLonga = celula.length() > LARGURA_MAXIMA;
            limitada.add(muitoLonga ? celula.substring(0, LARGURA_MAXIMA - 3) + "..." : celula);
        }
        return limitada;
    }

    private static int[] calcularLarguras(List<String> cabecalho, List<List<String>> linhas) {
        int[] larguras = new int[cabecalho.size()];
        for (int i = 0; i < larguras.length; i++) {
            larguras[i] = cabecalho.get(i).length();
        }
        for (List<String> linha : linhas) {
            for (int i = 0; i < larguras.length; i++) {
                larguras[i] = Math.max(larguras[i], linha.get(i).length());
            }
        }
        return larguras;
    }

    private static String montarLinha(List<String> celulas, int[] larguras) {
        List<String> alinhadas = new ArrayList<>();
        for (int i = 0; i < celulas.size(); i++) {
            alinhadas.add(String.format("%-" + larguras[i] + "s", celulas.get(i)));
        }
        return String.join(SEPARADOR_COLUNAS, alinhadas);
    }

    private static String montarTraco(int[] larguras) {
        List<String> tracos = new ArrayList<>();
        for (int largura : larguras) {
            tracos.add("-".repeat(largura));
        }
        return String.join(SEPARADOR_TRACO, tracos);
    }
}
