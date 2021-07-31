import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        javax.swing.SwingUtilities.invokeLater(MainFrame::new);
    }


//    public static void main(String... args) {
//        JFileChooser fc = new JFileChooser();
//        JFrame frame = new JFrame();
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//        JTextArea tarea = new JTextArea(10, 10);
//
//        JButton readButton = new JButton("OPEN FILE");
//        readButton.addActionListener(ev -> {
//            int returnVal = fc.showOpenDialog(frame);
//            if (returnVal == JFileChooser.APPROVE_OPTION) {
//                File file = fc.getSelectedFile();
//                try {
//                    BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
//                    tarea.read(input, "READING FILE :-)");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            } else {
//                System.out.println("Operation is CANCELLED :(");
//            }
//        });
//
//        frame.getContentPane().add(tarea, BorderLayout.CENTER);
//        frame.getContentPane().add(readButton, BorderLayout.PAGE_END);
//        frame.pack();
//        frame.setVisible(true);
//    }
}
