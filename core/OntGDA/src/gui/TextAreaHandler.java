package gui;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.LogRecord;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 *
 * @author Dustin Dannenhauer
 * @email  dtd212@lehigh.edu
 * @date   Aug 5, 2013
 */
public class TextAreaHandler extends java.util.logging.Handler {

    private JTextArea textArea;// = new JTextArea(100, 100);

    public TextAreaHandler(JTextArea textArea) {
        super();
//        textArea.setColumns(20);
//        textArea.setRows(5);
//        jScrollPane.setViewportView(textArea);
        this.textArea = textArea;
    }
    
    @Override
    public void publish(final LogRecord record) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                StringWriter text = new StringWriter();
                PrintWriter out = new PrintWriter(text);
                out.println(textArea.getText());
                out.printf("[%s] [Thread-%d]: %s.%s -> %s", record.getLevel(),
                        record.getThreadID(), record.getSourceClassName(),
                        record.getSourceMethodName(), record.getMessage());
                textArea.setText(text.toString());
            }

        });
    }

    public JTextArea getTextArea() {
        return this.textArea;
    }

    @Override
    public void flush() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() throws SecurityException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}