import java.util.*;

/**
 * CAMPO MINADO - tabuleiro 18 colunas x 14 linhas com 40 bombas (console).
 * (Arquivo 100% ASCII: sem acentos e sem simbolos especiais.)
 *
 * Comandos (colunas A-R, linhas 1-14):
 *   a C5     -> abre a casa C5   (ou apenas: C5)
 *   m C5     -> marca/desmarca bandeira na casa C5
 *   sair     -> encerra o jogo
 *
 * Simbolos: # fechada | F bandeira | . vazia | 1-8 bombas vizinhas | * bomba | X bandeira errada
 *
 * A primeira jogada nunca cai em uma bomba (as bombas sao sorteadas depois dela).
 *
 * Compilar: javac CampoMinado.java
 * Executar: java CampoMinado
 */
public class CampoMinado {

    static final int LINHAS = 14, COLUNAS = 18, MINAS = 40;

    static boolean[][] mina, aberta, bandeira;
    static int[][] vizinhas;
    static boolean minasColocadas, fim, venceu, saiu;
    static int abertas, bandeiras;
    static long inicio;

    static final Scanner sc = new Scanner(System.in);
    static final Random rnd = new Random();

    // ===================== Main =====================
    public static void main(String[] args) {
        System.out.println("=====================================");
        System.out.println("   CAMPO MINADO " + COLUNAS + " x " + LINHAS + " - " + MINAS + " bombas");
        System.out.println("=====================================");
        System.out.println("Comandos:  a C5 = abrir   m C5 = marcar/desmarcar bandeira   sair");
        System.out.println("(digitar so a coordenada, ex: C5, tambem abre a casa)");

        boolean jogarDeNovo = true;
        while (jogarDeNovo) {
            novoJogo();
            while (!fim && !saiu) {
                desenhar(false);
                System.out.print("Comando: ");
                processar(ler());
            }
            if (saiu) break;

            desenhar(true);
            if (venceu) {
                long seg = (System.currentTimeMillis() - inicio) / 1000;
                System.out.println("PARABENS! Voce limpou o campo em " + seg + " segundos!");
            } else {
                System.out.println("BOOM! Voce pisou em uma bomba. Fim de jogo.");
            }
            System.out.print("\nJogar novamente? (s/n): ");
            jogarDeNovo = ler().startsWith("s");
        }
        System.out.println("\nAte a proxima!");
    }

    // ===================== Preparacao =====================
    static void novoJogo() {
        mina = new boolean[LINHAS][COLUNAS];
        aberta = new boolean[LINHAS][COLUNAS];
        bandeira = new boolean[LINHAS][COLUNAS];
        vizinhas = new int[LINHAS][COLUNAS];
        minasColocadas = false;
        fim = false;
        venceu = false;
        abertas = 0;
        bandeiras = 0;
    }

    /** Sorteia as bombas evitando a casa clicada e suas vizinhas. */
    static void colocarMinas(int r0, int c0) {
        int colocadas = 0;
        while (colocadas < MINAS) {
            int r = rnd.nextInt(LINHAS);
            int c = rnd.nextInt(COLUNAS);
            if (mina[r][c]) continue;
            if (Math.abs(r - r0) <= 1 && Math.abs(c - c0) <= 1) continue;
            mina[r][c] = true;
            colocadas++;
        }
        for (int r = 0; r < LINHAS; r++) {
            for (int c = 0; c < COLUNAS; c++) {
                int n = 0;
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        int nr = r + dr, nc = c + dc;
                        if (nr >= 0 && nr < LINHAS && nc >= 0 && nc < COLUNAS && mina[nr][nc]) n++;
                    }
                }
                vizinhas[r][c] = n;
            }
        }
    }

    // ===================== Comandos =====================
    static void processar(String entrada) {
        if (entrada.isEmpty()) return;
        if (entrada.equals("sair")) {
            saiu = true;
            return;
        }

        String[] partes = entrada.split("\\s+");
        String cmd = "a";
        String coord;
        if (partes.length == 1) {
            coord = partes[0];
        } else if (partes.length == 2) {
            cmd = partes[0];
            coord = partes[1];
        } else {
            System.out.println("Comando invalido.");
            return;
        }

        int[] pos = lerCoordenada(coord);
        if (pos == null) {
            System.out.println("Coordenada invalida. Use letra A-" + (char) ('A' + COLUNAS - 1)
                    + " e numero 1-" + LINHAS + " (ex: C5).");
            return;
        }
        int r = pos[0], c = pos[1];

        if (cmd.equals("a") || cmd.equals("abrir")) {
            if (bandeira[r][c]) {
                System.out.println("Essa casa tem bandeira. Desmarque antes de abrir (m " + coord + ").");
            } else if (aberta[r][c]) {
                System.out.println("Essa casa ja esta aberta.");
            } else {
                abrir(r, c);
            }
        } else if (cmd.equals("m") || cmd.equals("marcar") || cmd.equals("f")) {
            if (aberta[r][c]) {
                System.out.println("Nao da para marcar uma casa aberta.");
            } else if (bandeira[r][c]) {
                bandeira[r][c] = false;
                bandeiras--;
            } else {
                bandeira[r][c] = true;
                bandeiras++;
            }
        } else {
            System.out.println("Comando invalido. Use 'a' (abrir), 'm' (marcar) ou 'sair'.");
        }
    }

    /** Converte "c5" em {linha, coluna}; retorna null se invalido. */
    static int[] lerCoordenada(String s) {
        if (s.length() < 2) return null;
        int c = s.charAt(0) - 'a';
        int r;
        try {
            r = Integer.parseInt(s.substring(1)) - 1;
        } catch (NumberFormatException e) {
            return null;
        }
        if (r < 0 || r >= LINHAS || c < 0 || c >= COLUNAS) return null;
        return new int[]{r, c};
    }

    static void abrir(int r, int c) {
        if (!minasColocadas) {
            colocarMinas(r, c);
            minasColocadas = true;
            inicio = System.currentTimeMillis();
        }
        if (mina[r][c]) {
            aberta[r][c] = true;
            fim = true;
            venceu = false;
            return;
        }

        // Abre a casa; se for 0, abre os vizinhos em cascata
        Deque<int[]> pilha = new ArrayDeque<>();
        pilha.push(new int[]{r, c});
        while (!pilha.isEmpty()) {
            int[] p = pilha.pop();
            int y = p[0], x = p[1];
            if (aberta[y][x] || bandeira[y][x]) continue;
            aberta[y][x] = true;
            abertas++;
            if (vizinhas[y][x] == 0) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int ny = y + dy, nx = x + dx;
                        if (ny >= 0 && ny < LINHAS && nx >= 0 && nx < COLUNAS && !aberta[ny][nx]) {
                            pilha.push(new int[]{ny, nx});
                        }
                    }
                }
            }
        }

        if (abertas == LINHAS * COLUNAS - MINAS) {
            fim = true;
            venceu = true;
        }
    }

    // ===================== Tela =====================
    static void desenhar(boolean revelar) {
        System.out.println();
        System.out.println("Bombas restantes: " + (MINAS - bandeiras));
        StringBuilder cab = new StringBuilder("     ");
        for (int c = 0; c < COLUNAS; c++) cab.append((char) ('A' + c)).append(' ');
        System.out.println(cab);
        System.out.println("   +" + repetir("-", COLUNAS * 2 + 1) + "+");

        for (int r = 0; r < LINHAS; r++) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%2d", r + 1)).append(" | ");
            for (int c = 0; c < COLUNAS; c++) {
                sb.append(simbolo(r, c, revelar)).append(' ');
            }
            sb.append("| ").append(r + 1);
            System.out.println(sb);
        }

        System.out.println("   +" + repetir("-", COLUNAS * 2 + 1) + "+");
        System.out.println(cab);
        System.out.println();
    }

    static char simbolo(int r, int c, boolean revelar) {
        if (revelar) {
            if (mina[r][c]) return bandeira[r][c] ? 'F' : '*';
            if (bandeira[r][c]) return 'X';
        }
        if (bandeira[r][c]) return 'F';
        if (!aberta[r][c]) return '#';
        if (mina[r][c]) return '*';
        return vizinhas[r][c] == 0 ? '.' : (char) ('0' + vizinhas[r][c]);
    }

    static String repetir(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(s);
        return sb.toString();
    }

    static String ler() {
        if (!sc.hasNextLine()) {
            System.out.println("\nEntrada encerrada.");
            System.exit(0);
        }
        return sc.nextLine().trim().toLowerCase();
    }
}