package com.minicompass.demo;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SwingDemo frame = new SwingDemo();
            frame.setVisible(true);
        });
    }
}