/*
 * Copyright (c) 2016 Markus Uhlin <markus@dataswamp.org>
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;

public class PortScanner extends JPanel implements ItemListener,
                                                   ActionListener,
                                                   PropertyChangeListener {
    public JTextArea	pTaskOutput    = null;
    public String	pTargetMachine = "";
    public boolean	pTcpAndUdp     = false;

    private static final String progAuthor  = "Markus Uhlin";
    private static final String progVersion = "v1.0";
    private final int	portHighLimit = 65535;
    private final int	portLowLimit  = 0;

    private JButton quitButton;
    private JButton startButton;
    private JCheckBox bothProtos;
    private JLabel jlPortStart, jlPortEnd;
    private JLabel label;
    private JProgressBar progressBar;
    private JTextField jtfPortStart, jtfPortEnd;
    private JTextField targetMachine;
    private Task task;
    private boolean scanning = false;
    private int portEnd = 0;
    private int portStart = 1023;

    class Task extends SwingWorker<Void, Void> {
        /*
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() {
	    Utils u = new Utils();
	    int portsOpen = 0;

	    pTaskOutput.append(String.format("Scanning %s (%d-%d)...\n",
		pTargetMachine, portStart, portEnd));

	    if (pTcpAndUdp && (!"127.0.0.1".equals(pTargetMachine) &&
			       !"localhost".equals(pTargetMachine))) {
		pTaskOutput.append("fatal: udp isn't supported for a " +
				   "remote host\n");
		setProgress(100);
		return (null);
	    }

	    /* Initialize progress property */
	    setProgress(0);

	    for (int i = portStart; i <= portEnd; i++) {
		if (u.checkForOpenTcpPort(pTargetMachine, i)) {
		    portsOpen++;
		    pTaskOutput.append(String.format("%d/tcp open (%s)\n",
						     i, u.strPort(i)));
		}

		if (pTcpAndUdp && u.checkForOpenUdpPort(pTargetMachine, i)) {
		    portsOpen++;
		    pTaskOutput.append(String.format("%d/udp open (%s)\n",
						     i, u.strPort(i)));
		}

		setProgress((100 * i) / (portEnd - portStart));
	    }

	    setProgress(100);
	    pTaskOutput.append(String.format("%d open ports.\n", portsOpen));
	    return (null);
        }

        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            startButton.setEnabled(true);
            setCursor(null); /* Turn off the wait cursor */
	    scanning = false;
            pTaskOutput.append("Done!\n");
        }
    }

    private void createPanel1() {
	JPanel panel1 = new JPanel();

	label	    = new JLabel();
	jlPortStart = new JLabel();
	jlPortEnd   = new JLabel();

	label.setText("Target machine:");
	jlPortStart.setText("Port start:");
	jlPortEnd.setText("Port end:");

	targetMachine = new JTextField(20);
	jtfPortStart  = new JTextField(5);
	jtfPortEnd    = new JTextField(5);

	jtfPortStart.setText("0");
	jtfPortEnd.setText("1023");

	panel1.add(label);
	panel1.add(targetMachine);
	panel1.add(jlPortStart);
	panel1.add(jtfPortStart);
	panel1.add(jlPortEnd);
	panel1.add(jtfPortEnd);

	/* add panel1 */
	add(panel1, BorderLayout.PAGE_START);
    }

    private void createPanel2() {
	JPanel panel2 = new JPanel();

        bothProtos = new JCheckBox("Both TCP and UDP");
	bothProtos.addItemListener(this);
	panel2.add(bothProtos);
	add(panel2, BorderLayout.LINE_START);
    }

    private void createPanel3() {
	JPanel panel3 = new JPanel();

        startButton = new JButton("Start");
        startButton.addActionListener(this);
        startButton.setActionCommand("start");

        quitButton = new JButton("Quit");
        quitButton.addActionListener(this);
        quitButton.setActionCommand("quit");

	panel3.add(startButton);
	panel3.add(quitButton);

	add(panel3, BorderLayout.CENTER);
    }

    private void createPanel4() {
	JPanel panel4 = new JPanel();

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);

	panel4.add(progressBar);

	add(panel4, BorderLayout.LINE_END);
    }

    public PortScanner() {
	/*
	 * Create the UI...
	 */
        super(new BorderLayout());

	createPanel1();
	createPanel2();
	createPanel3();
	createPanel4();

        pTaskOutput = new JTextArea(20, 60);
        pTaskOutput.setEditable(false);
        pTaskOutput.setMargin(new Insets(5,5,5,5));
	add(new JScrollPane(pTaskOutput), BorderLayout.PAGE_END);

        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }

    public void itemStateChanged(ItemEvent evt) {
	Object source = evt.getItemSelectable();

	if (source == bothProtos) {
	    pTcpAndUdp = ((evt.getStateChange() == ItemEvent.SELECTED)
			  ? true
			  : false);
	}
    }

    /**
     * Invoked when the user presses the start and quit buttons.
     */
    public void actionPerformed(ActionEvent evt) {
	String cmd = evt.getActionCommand();

	if ("start".equals(cmd)) {
	    pTargetMachine = targetMachine.getText();
	    pTargetMachine.trim();

	    if (pTargetMachine.equals("")) {
		pTaskOutput.append("fatal: no target machine\n");
		return;
	    }

	    try {
		portStart = Integer.parseInt(jtfPortStart.getText());
		portEnd = Integer.parseInt(jtfPortEnd.getText());
	    } catch (Exception e) {
		pTaskOutput.append("fatal: error retrieving start/end port\n");
		return;
	    }

	    if (portStart < portLowLimit || portStart > portHighLimit ||
		portEnd < portLowLimit || portEnd > portHighLimit) {
		pTaskOutput.append("fatal: port range out of bounds\n");
		return;
	    }

	    startButton.setEnabled(false);
	    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	    scanning = true;
	    task = new Task();
	    task.addPropertyChangeListener(this);
	    task.execute();
	} else if ("quit".equals(cmd)) {
	    /* Exit with non-zero if scanning is in progress */
	    System.exit(scanning ? 1 : 0);
	} else {
	    /*do nothing*/;
	}
    }

    /**
     * Invoked when task's progress property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
	    if (false)
		pTaskOutput.append(String.format("Completed %d%% of task.\n",
						 task.getProgress()));
        }
    }

    private static void createAndShowGUI() {
	/* Create and set up the window */
        JFrame frame =
	    new JFrame(String.format("Simple network portscanner %s by %s",
				     progVersion, progAuthor));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	/*
	 * Create and set up the content pane
	 */
        JComponent newContentPane = new PortScanner();
	/* Content panes must be opaque */
        newContentPane.setOpaque(true);
        frame.setContentPane(newContentPane);

	/* Display the window */
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
	javax.swing.SwingUtilities.invokeLater(
	    new Runnable() {
		public void run() { createAndShowGUI(); }
	    });
    }
}
