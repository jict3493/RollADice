package com.simu.rolladice;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.AttributedString;
import java.util.List;
import java.util.Random;

public class RollADice extends JFrame {
    private DefaultPieDataset dataset;
    private int[] resultados = new int[6];
    private int lanzamientos = 0;
    private final JLabel[] resultadoLabels = new JLabel[6];
    private JTextField vecesALanzar;
    private TextTitle contadorTitle;
    private JButton[] botones;

    public RollADice() {
        setTitle("Roll-A-Dice");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        dataset = new DefaultPieDataset();
        for (int i = 1; i <= 6; i++) {
            dataset.setValue(String.valueOf(i), 1);
        }

        JFreeChart chart = ChartFactory.createPieChart("Resultados", dataset, true, false, false);
        PiePlot plot = (PiePlot) chart.getPlot();

        plot.setLabelGenerator(new PieSectionLabelGenerator() {
            @Override
            public String generateSectionLabel(PieDataset dataset, Comparable key) {
                int indice = Integer.parseInt(key.toString()) - 1;
                int count = resultados[indice];
                double porcentaje = ((double) count / lanzamientos) * 100.0;

                if (Double.isNaN(porcentaje) || lanzamientos == 0) {
                    porcentaje = 16.67;
                }

                return String.format("%s: %d veces (%.2f%%)", key, count, porcentaje);
            }

            @Override
            public AttributedString generateAttributedSectionLabel(PieDataset pieDataset, Comparable comparable) {
                return null;
            }
        });

        contadorTitle = new TextTitle("Lanzamientos: 0");
        contadorTitle.setPosition(RectangleEdge.TOP);
        contadorTitle.setHorizontalAlignment(HorizontalAlignment.RIGHT);
        chart.addSubtitle(contadorTitle);

        ChartPanel chartPanel = new ChartPanel(chart);
        add(chartPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        JButton lanzar10Button = new JButton("10");
        JButton lanzar100Button = new JButton("100");
        JButton lanzar1000Button = new JButton("1000");
        vecesALanzar = new JTextField("1", 5);
        JButton lanzarButton = new JButton("Lanzar");

        controlPanel.add(lanzar10Button);
        controlPanel.add(lanzar100Button);
        controlPanel.add(lanzar1000Button);
        controlPanel.add(vecesALanzar);
        controlPanel.add(lanzarButton);

        JPanel resultadosPanel = new JPanel(new GridLayout(1, 6));
        for (int i = 0; i < 6; i++) {
            resultadoLabels[i] = new JLabel(String.valueOf(i + 1));
            resultadoLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            resultadoLabels[i].setOpaque(true);
            resultadoLabels[i].setBackground(Color.WHITE);
            resultadosPanel.add(resultadoLabels[i]);
        }

        add(resultadosPanel, BorderLayout.NORTH);
        add(controlPanel, BorderLayout.SOUTH);

        botones = new JButton[]{lanzarButton, lanzar10Button, lanzar100Button, lanzar1000Button};

        ActionListener vecesButtonListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton clickedButton = (JButton) e.getSource();
                vecesALanzar.setText(clickedButton.getText());
            }
        };
        lanzar10Button.addActionListener(vecesButtonListener);
        lanzar100Button.addActionListener(vecesButtonListener);
        lanzar1000Button.addActionListener(vecesButtonListener);

        lanzarButton.addActionListener(e -> lanzarDado(Integer.parseInt(vecesALanzar.getText())));
        setVisible(true);
    }

    private void lanzarDado(int veces) {
        setBotonesHabilitados(false);

        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            Random random = new Random();

            @Override
            protected Void doInBackground() throws Exception {
                long sleepTime = (long) (1000 * (Math.sqrt(Math.sin(Math.PI * (Math.log10(veces) / 4)))) / (5 * Math.log10(veces)));
                for (int i = 0; i < veces; i++) {
                    int resultado = random.nextInt(6) + 1;

                    lanzamientos++;
                    resultados[resultado - 1]++;

                    for (int j = 0; j < 6; j++) {
                        resultadoLabels[j].setBackground(j == resultado - 1 ? Color.YELLOW : Color.WHITE);
                    }

                    publish(lanzamientos);

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException ignored) {}
                }

                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                actualizarGrafico();
            }

            @Override
            protected void done() {
                setBotonesHabilitados(true);
                for (JLabel label : resultadoLabels) {
                    label.setBackground(Color.WHITE);
                }
            }
        };

        worker.execute();
    }

    private void actualizarGrafico() {
        if (lanzamientos == 0) return;

        for (int i = 1; i <= 6; i++) {
            double porcentaje = (double) resultados[i - 1] * 100 / lanzamientos;
            dataset.setValue(String.valueOf(i), porcentaje);
        }

        contadorTitle.setText("Lanzamientos: " + lanzamientos);
    }

    public void setBotonesHabilitados(boolean habilitados) {
        for (JButton boton : botones) {
            boton.setEnabled(habilitados);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RollADice::new);
    }
}