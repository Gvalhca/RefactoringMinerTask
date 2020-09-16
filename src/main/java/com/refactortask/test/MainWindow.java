package com.refactortask.test;

import org.apache.commons.validator.routines.UrlValidator;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.style.PieStyler;
import org.knowm.xchart.style.Styler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MainWindow {

    private JFrame frame;
    private JPanel panel;
    private String pathToRepo;
    private GridBagConstraints gridBagConstraints;
    private JButton openLocalRepoButton;
    private JButton runButton;
    private JTable namesCountTable;
    private JLabel selectedRepoLabel;
    private JTextField pathTextField;

    public void createWindow() {
        frame = new JFrame("Refactoring names task");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        createUI(frame);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void createPieChartRefactoringNames(ConcurrentMap<String, AtomicInteger> refactoringSeries) {
        // Create Chart
        PieChart chart = new PieChartBuilder().width(1600).height(800).title("Refactoring Names Pie Chart").theme(Styler.ChartTheme.GGPlot2).build();

        // Customize Chart
        chart.getStyler().setLegendVisible(true);
        chart.getStyler().setAnnotationType(PieStyler.AnnotationType.LabelAndPercentage);
        chart.getStyler().setAnnotationDistance(1.15);
        chart.getStyler().setDrawAllAnnotations(true);
        chart.getStyler().setPlotContentSize(.7);
        chart.getStyler().setStartAngleInDegrees(90);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);

        // Series
        refactoringSeries.forEach(chart::addSeries);

        // Show it
        Thread t = new Thread(() -> new SwingWrapper(chart).displayChart());
        t.start();
    }

    private void createUI(final JFrame frame) {
        panel = new JPanel();
        openLocalRepoButton = new JButton("Open local repository");
        runButton = new JButton("Run Refactoring Miner");
        selectedRepoLabel = new JLabel();

        pathTextField = createPathTextField();

        openLocalRepoButton.addActionListener(e -> openLocalRepoButtonActionListener(frame));

        runButton.addActionListener(e -> runButtonActionListener());

        drawFrameContent();
    }

    private void openLocalRepoButtonActionListener(JFrame frame) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int option = fileChooser.showOpenDialog(frame);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            selectedRepoLabel.setText("Repository: " + file.getName());
            pathToRepo = file.getAbsolutePath();
            pathTextField.setText(pathToRepo);
        }
    }

    private void runButtonActionListener() {
        if (pathTextField.getText().equals("")) {
            return;
        }
        try {
            String temp = pathTextField.getText();
            RefactoringMinerWorker refactoringMinerWorker;
            if (checkStringIsUrl(temp)) {
                pathToRepo = temp;
                String repoName = temp.substring(temp.lastIndexOf('/')).trim();
                refactoringMinerWorker = new RefactoringMinerWorker("tmp" + repoName, pathToRepo);
            } else {
                refactoringMinerWorker = new RefactoringMinerWorker(pathToRepo);
            }

            ConcurrentMap<String, AtomicInteger> refactoringNamesCountMap = refactoringMinerWorker.getAllRefactoringNames();
            createPieChartRefactoringNames(refactoringNamesCountMap);
            namesCountTable = createRefactoringNamesTable(convertMapToArray(refactoringNamesCountMap));
            showRefactoringNamesTable();
            frame.setSize(1000, 600);
            frame.setLocation(0, 0);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void drawFrameContent() {
        LayoutManager layout = new GridBagLayout();
        panel.setLayout(layout);
        gridBagConstraints = new GridBagConstraints();

        // Put constraints on label, textbox and buttons
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        panel.add(selectedRepoLabel, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        panel.add(pathTextField, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridwidth = 2;
        panel.add(openLocalRepoButton, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridwidth = 2;
        panel.add(runButton, gridBagConstraints);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.setFocusable(true);
    }

    private Boolean checkStringIsUrl(String strUrl) {
        String[] schemes = {"http", "https", "github.com/", "git@github.com"};
        UrlValidator urlValidator = new UrlValidator(schemes);
        return urlValidator.isValid(strUrl);
    }

    private JTextField createPathTextField() {
        JTextField pathTextField = new JTextField("Enter Git URL or open local repository");
        pathTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                pathTextField.setText("");
            }

            @Override
            public void focusLost(FocusEvent focusEvent) {

            }
        });
        return pathTextField;
    }

    private String[][] convertMapToArray(ConcurrentMap<String, AtomicInteger> map) {
        String[][] result = new String[map.size()][2];
        int count = 0;
        for (ConcurrentMap.Entry<String, AtomicInteger> entry : map.entrySet()) {
            result[count][0] = entry.getKey();
            result[count][1] = entry.getValue().toString();
            count++;
        }
        return result;
    }

    private JTable createRefactoringNamesTable(String[][] data) {
        String[] column = {"Refactoring name", "Count"};
        return new JTable(data, column);
    }

    private void showRefactoringNamesTable() {
        JScrollPane sp = new JScrollPane(namesCountTable);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(sp, gridBagConstraints);
    }
}
