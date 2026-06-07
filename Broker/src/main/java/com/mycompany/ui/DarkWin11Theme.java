package com.mycompany.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * Tema visual escuro com botoes no estilo flat/arredondado do Windows 11.
 *
 * A classe percorre os componentes existentes da tela e altera somente
 * propriedades visuais: cores, fontes, bordas, cursor e UI dos botoes.
 */
public final class DarkWin11Theme {

    /*
     * Paleta central do tema. Manter as cores em constantes evita espalhar
     * numeros RGB pelas funcoes de estilo.
     */
    private static final Color BACKGROUND = new Color(31, 31, 31);
    private static final Color SURFACE = new Color(39, 39, 42);
    private static final Color FIELD = new Color(28, 28, 30);
    private static final Color BORDER = new Color(72, 72, 78);
    private static final Color TEXT = new Color(245, 245, 245);
    private static final Color MUTED_TEXT = new Color(210, 210, 215);
    private static final Color ACCENT = new Color(0, 120, 212);
    private static final Color ACCENT_HOVER = new Color(18, 136, 235);
    private static final Color ACCENT_PRESSED = new Color(0, 95, 170);
    private static final Color DANGER = new Color(196, 43, 28);
    private static final Color DANGER_HOVER = new Color(220, 60, 45);
    private static final Color DANGER_PRESSED = new Color(155, 35, 24);

    /**
     * Impede instanciacao porque o tema e aplicado por metodos estaticos.
     */
    private DarkWin11Theme() {
    }

    /**
     * Aplica o tema em toda a hierarquia de componentes da janela informada.
     */
    public static void apply(JFrame frame) {
        installDefaults();
        frame.getContentPane().setBackground(BACKGROUND);
        // Percorre todos os filhos da janela e aplica estilo adequado por tipo.
        styleTree(frame.getContentPane());
        frame.pack();
        frame.setMinimumSize(frame.getSize());
        frame.repaint();
    }

    /**
     * Ajusta valores globais do Swing que nao pertencem a um componente
     * especifico, como ToolTip e OptionPane.
     */
    private static void installDefaults() {
        UIManager.put("ToolTip.background", SURFACE);
        UIManager.put("ToolTip.foreground", TEXT);
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("OptionPane.foreground", TEXT);
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        UIManager.put("Panel.background", BACKGROUND);
    }

    /**
     * Percorre recursivamente a arvore Swing, aplicando estilo no componente
     * atual e depois em todos os filhos.
     */
    private static void styleTree(Component component) {
        styleComponent(component);
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                styleTree(child);
            }
        }
    }

    /**
     * Seleciona a funcao de estilo correta conforme o tipo real do componente.
     */
    private static void styleComponent(Component component) {
        component.setFont(segoe(component.getFont(), Font.PLAIN));

        if (component instanceof JPanel panel) {
            stylePanel(panel);
        } else if (component instanceof JLabel label) {
            styleLabel(label);
        } else if (component instanceof JButton button) {
            styleButton(button);
        } else if (component instanceof JTextArea textArea) {
            styleTextArea(textArea);
        } else if (component instanceof JTextField textField) {
            styleTextField(textField);
        } else if (component instanceof JScrollPane scrollPane) {
            styleScrollPane(scrollPane);
        } else if (component instanceof JViewport viewport) {
            viewport.setBackground(FIELD);
        } else if (component instanceof JScrollBar scrollBar) {
            scrollBar.setBackground(SURFACE);
            scrollBar.setForeground(BORDER);
        }
    }

    /**
     * Estiliza paineis. Paineis com borda original viram superficies destacadas.
     */
    private static void stylePanel(JPanel panel) {
        boolean hasCustomBorder = panel.getBorder() != null;
        panel.setOpaque(true);
        panel.setBackground(hasCustomBorder ? SURFACE : BACKGROUND);
        if (hasCustomBorder) {
            panel.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedLineBorder(BORDER, 10),
                    new EmptyBorder(10, 10, 10, 10)));
        }
    }

    /**
     * Estiliza labels, usando maior contraste para titulos.
     */
    private static void styleLabel(JLabel label) {
        Font current = label.getFont();
        int style = current != null && current.getSize() >= 20 ? Font.BOLD : Font.PLAIN;
        label.setFont(segoe(current, style));
        label.setForeground(current != null && current.getSize() >= 20 ? TEXT : MUTED_TEXT);
        if (current != null && current.getSize() >= 20) {
            label.setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

    /**
     * Estiliza botoes e escolhe paleta de perigo para acoes destrutivas.
     */
    private static void styleButton(JButton button) {
        boolean danger = isDangerAction(button.getText());
        Color base = danger ? DANGER : ACCENT;
        Color hover = danger ? DANGER_HOVER : ACCENT_HOVER;
        Color pressed = danger ? DANGER_PRESSED : ACCENT_PRESSED;

        button.setUI(new RoundedButtonUI(base, hover, pressed));
        button.setForeground(Color.WHITE);
        button.setFont(segoe(button.getFont(), Font.BOLD));
        button.setBorder(new EmptyBorder(8, 14, 8, 14));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setRolloverEnabled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     * Estiliza areas de texto usadas para logs, status e chat.
     */
    private static void styleTextArea(JTextArea textArea) {
        textArea.setBackground(FIELD);
        textArea.setForeground(TEXT);
        textArea.setCaretColor(TEXT);
        textArea.setSelectionColor(ACCENT);
        textArea.setSelectedTextColor(Color.WHITE);
        textArea.setBorder(new EmptyBorder(8, 8, 8, 8));
    }

    /**
     * Estiliza campos de entrada simples.
     */
    private static void styleTextField(JTextField textField) {
        textField.setBackground(FIELD);
        textField.setForeground(TEXT);
        textField.setCaretColor(TEXT);
        textField.setSelectionColor(ACCENT);
        textField.setSelectedTextColor(Color.WHITE);
        textField.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER, 8),
                new EmptyBorder(6, 10, 6, 10)));
    }

    /**
     * Estiliza a borda e o fundo dos scroll panes.
     */
    private static void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBackground(FIELD);
        scrollPane.getViewport().setBackground(FIELD);
        scrollPane.setBorder(new RoundedLineBorder(BORDER, 8));
    }

    /**
     * Identifica botoes que representam exclusao, cancelamento ou encerramento.
     */
    private static boolean isDangerAction(String text) {
        String value = text == null ? "" : text.toLowerCase();
        return value.contains("encerrar") || value.contains("excluir") || value.contains("cancelar");
    }

    /**
     * Mantem o tamanho original do componente e troca apenas familia/estilo.
     */
    private static Font segoe(Font current, int style) {
        int size = current == null ? 12 : current.getSize();
        return new Font("Segoe UI", style, size);
    }

    /**
     * UI customizada para pintar fundo arredondado dos botoes.
     */
    private static final class RoundedButtonUI extends BasicButtonUI {

        private final Color base;
        private final Color hover;
        private final Color pressed;

        /**
         * Recebe as cores dos estados normal, hover e pressionado.
         */
        private RoundedButtonUI(Color base, Color hover, Color pressed) {
            this.base = base;
            this.hover = hover;
            this.pressed = pressed;
        }

        /**
         * Pinta o fundo arredondado antes do texto original do JButton.
         */
        @Override
        public void paint(Graphics graphics, JComponent component) {
            AbstractButton button = (AbstractButton) component;
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(resolveColor(button));
            g2.fillRoundRect(0, 0, component.getWidth(), component.getHeight(), 10, 10);
            g2.dispose();
            super.paint(graphics, component);
        }

        /**
         * Escolhe a cor visual conforme estado do botao.
         */
        private Color resolveColor(AbstractButton button) {
            if (!button.isEnabled()) {
                return new Color(80, 80, 84);
            }
            if (button.getModel().isPressed()) {
                return pressed;
            }
            if (button.getModel().isRollover()) {
                return hover;
            }
            return base;
        }
    }

    /**
     * Borda arredondada simples para paineis, campos e areas com scroll.
     */
    private static final class RoundedLineBorder extends AbstractBorder {

        private final Color color;
        private final int radius;

        /**
         * Guarda cor e raio usados no desenho da borda.
         */
        private RoundedLineBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        /**
         * Desenha a borda com antialiasing para ficar suave.
         */
        @Override
        public void paintBorder(Component component, Graphics graphics, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        /**
         * Define espessura visual padrao da borda.
         */
        @Override
        public Insets getBorderInsets(Component component) {
            return new Insets(1, 1, 1, 1);
        }

        /**
         * Reaproveita o objeto Insets recebido pelo Swing.
         */
        @Override
        public Insets getBorderInsets(Component component, Insets insets) {
            insets.set(1, 1, 1, 1);
            return insets;
        }

        /**
         * Indica que a borda nao pinta uma area opaca propria.
         */
        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}
