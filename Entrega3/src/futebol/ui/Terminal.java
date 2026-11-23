package futebol.ui;

import futebol.modelo.Coluna;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Entrada e saída do terminal. Toda leitura do que o usuário digita (com
 * validação) e toda escrita na tela passam por aqui.
 */
public final class Terminal {

    /** Lançada quando a entrada acaba (Ctrl+Z, Ctrl+D ou arquivo redirecionado). */
    public static class FimDeEntrada extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private final Scanner entrada;
    private final PrintStream saida;

    private Terminal(Scanner entrada, PrintStream saida) {
        this.entrada = entrada;
        this.saida = saida;
    }

    /** Terminal padrão, com UTF-8 para os acentos aparecerem certos. */
    public static Terminal padrao() {
        return new Terminal(
                new Scanner(System.in, StandardCharsets.UTF_8),
                new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
    }

    // ---------------------------------------------------------------
    // Saída
    // ---------------------------------------------------------------
    public void mostrar(String texto) {
        saida.println(texto);
    }

    public void linhaEmBranco() {
        saida.println();
    }

    // ---------------------------------------------------------------
    // Entrada
    // ---------------------------------------------------------------
    public String texto(String pergunta) {
        saida.print(pergunta);
        saida.flush();
        if (!entrada.hasNextLine()) {
            throw new FimDeEntrada();
        }
        return entrada.nextLine().trim();
    }

    /** Lê um inteiro entre min e max, repetindo até vir um valor válido. */
    public int opcao(String pergunta, int min, int max) {
        while (true) {
            String digitado = texto(pergunta);
            try {
                int numero = Integer.parseInt(digitado);
                if (numero >= min && numero <= max) {
                    return numero;
                }
            } catch (NumberFormatException e) {
                // cai na mensagem abaixo
            }
            mostrar("Opção inválida. Digite um número de " + min + " a " + max + ".");
        }
    }

    public int inteiro(String pergunta) {
        while (true) {
            try {
                return Integer.parseInt(texto(pergunta));
            } catch (NumberFormatException e) {
                mostrar("Digite um número inteiro.");
            }
        }
    }

    public LocalDate data(String pergunta) {
        while (true) {
            try {
                return LocalDate.parse(texto(pergunta + " (AAAA-MM-DD): "));
            } catch (DateTimeParseException e) {
                mostrar("Data inválida. Use o formato AAAA-MM-DD.");
            }
        }
    }

    public boolean confirmar(String pergunta) {
        while (true) {
            String resposta = texto(pergunta + " (S/N): ").toUpperCase();
            if (resposta.equals("S")) {
                return true;
            }
            if (resposta.equals("N")) {
                return false;
            }
        }
    }

    /**
     * Lê o valor de uma coluna conforme o tipo dela, repetindo até ser válido.
     * Se permitirNulo for true e a coluna aceitar NULL, deixar vazio devolve null.
     */
    public Object valor(Coluna coluna, boolean permitirNulo) {
        boolean aceitaNulo = permitirNulo && coluna.anulavel();
        while (true) {
            String digitado = texto(perguntaDe(coluna, aceitaNulo));
            if (digitado.isEmpty()) {
                if (aceitaNulo) {
                    return null;
                }
                mostrar("Esse campo é obrigatório.");
                continue;
            }
            try {
                return converter(coluna.tipo(), digitado);
            } catch (NumberFormatException | DateTimeParseException e) {
                mostrar("Valor inválido para esse campo.");
            }
        }
    }

    private static String perguntaDe(Coluna coluna, boolean aceitaNulo) {
        String dica = switch (coluna.tipo()) {
            case TEXTO -> "";
            case NUMERO -> " (número)";
            case DATA -> " (AAAA-MM-DD)";
        };
        String aviso = aceitaNulo ? " [vazio = nenhum]" : "";
        return coluna.rotulo() + dica + aviso + ": ";
    }

    private static Object converter(Coluna.Tipo tipo, String digitado) {
        return switch (tipo) {
            case TEXTO -> digitado;
            case NUMERO -> Integer.valueOf(digitado);
            case DATA -> LocalDate.parse(digitado);
        };
    }
}
