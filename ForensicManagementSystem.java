import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

class Case {
    private String caseId;
    private List<String> evidence;

    public Case(String caseId) {
        this.caseId = caseId;
        this.evidence = new ArrayList<>();
    }

    public String getCaseId() {
        return caseId;
    }

    public List<String> getEvidence() {
        return evidence;
    }

    public void addEvidence(String evidenceItem) {
        evidence.add(evidenceItem);
    }

    public void removeEvidence(String evidenceItem) {
        evidence.remove(evidenceItem);
    }
}

public class ForensicManagementSystem extends JFrame {

    private List<User> users;
    private User currentUser;
    private List<Case> cases;

    private static final String CASES_FILE = "cases.txt";

    public ForensicManagementSystem() {
        users = new ArrayList<>();
        users.add(new User("admin", "password"));
        users.add(new User("user1", "123456"));
        checkCasesFile(); // Check and create cases.txt if it doesn't exist

        cases = new ArrayList<>();
        loadCasesFromFile();

        setTitle("Forensic Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        displayLogin();

        JButton analyzeButton = new JButton("Start Forensic Analysis");
        JButton viewCasesButton = new JButton("View Cases");
        JButton newCaseButton = new JButton("New Case");
        JButton deleteCaseButton = new JButton("Delete Case");
        JButton viewCaseDetailsButton = new JButton("View Case Details");
        JTextField caseField = new JTextField(20);
        JTextArea resultTextArea = new JTextArea(10, 30);
        resultTextArea.setEditable(false);

        setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(new JLabel("Case ID:"));
        panel.add(caseField);
        panel.add(analyzeButton);
        panel.add(viewCasesButton);
        panel.add(newCaseButton);
        panel.add(deleteCaseButton);
        panel.add(viewCaseDetailsButton);

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(resultTextArea), BorderLayout.CENTER);
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        fileMenu.add(exitMenuItem);
        analyzeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String caseId = caseField.getText();
                if (!caseId.isEmpty()) {
                    String analysisResult = performForensicAnalysis(caseId);
                    resultTextArea.append("Analysis Result for Case ID " + caseId + ":\n" + analysisResult + "\n\n");
                } else {
                    JOptionPane.showMessageDialog(ForensicManagementSystem.this, "Please enter a Case ID", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        viewCasesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resultTextArea.setText("List of Cases:\n");
                for (Case forensicCase : cases) {
                    resultTextArea.append("Case ID: " + forensicCase.getCaseId() + "\n");
                    resultTextArea.append("Evidence: " + String.join(", ", forensicCase.getEvidence()) + "\n\n");
                }
            }
        });

        newCaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String caseId = JOptionPane.showInputDialog(ForensicManagementSystem.this, "Enter new Case ID:");
                if (caseId != null && !caseId.isEmpty()) {
                    Case newCase = new Case(caseId);
                    addEvidenceToCase(newCase, resultTextArea);
                    cases.add(newCase);
                    resultTextArea.append("New Case added. Case ID: " + caseId + "\n\n");
                    saveCasesToFile();
                } else {
                    JOptionPane.showMessageDialog(ForensicManagementSystem.this, "Invalid Case ID", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        deleteCaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String caseId = caseField.getText();
                if (!caseId.isEmpty()) {
                    Case foundCase = findCaseById(caseId);
                    if (foundCase != null) {
                        if (currentUser.getUsername().equals("admin")) {
                            String enteredPassword = JOptionPane.showInputDialog(ForensicManagementSystem.this, "Enter deletion password:");
                            if (enteredPassword != null && enteredPassword.equals("password")) {
                                cases.remove(foundCase);
                                resultTextArea.append("Case ID " + caseId + " deleted.\n\n");
                                saveCasesToFile(); // Save cases after deletion
                            } else {
                                JOptionPane.showMessageDialog(ForensicManagementSystem.this, "Incorrect password", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(ForensicManagementSystem.this, "Only admin can delete cases", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(ForensicManagementSystem.this, "Case ID not found", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(ForensicManagementSystem.this, "Please enter a Case ID", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        viewCaseDetailsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String caseId = caseField.getText();
                if (!caseId.isEmpty()) {
                    Case foundCase = findCaseById(caseId);
                    if (foundCase != null) {
                        resultTextArea.setText("Case Details for Case ID " + caseId + ":\n");
                        resultTextArea.append("Evidence: " + String.join(", ", foundCase.getEvidence()) + "\n\n");
                    } else {
                        JOptionPane.showMessageDialog(ForensicManagementSystem.this, "Case ID not found", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(ForensicManagementSystem.this, "Please enter a Case ID", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });



        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }
    private void checkCasesFile() {
        File file = new File(CASES_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void loadCasesFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(CASES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String caseId = parts[0];
                    Case forensicCase = new Case(caseId);
                    for (int i = 1; i < parts.length; i++) {
                        forensicCase.addEvidence(parts[i]);
                    }
                    cases.add(forensicCase);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveCasesToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CASES_FILE))) {
            for (Case forensicCase : cases) {
                writer.write(forensicCase.getCaseId() + ",");
                List<String> evidenceList = forensicCase.getEvidence();
                for (String evidence : evidenceList) {
                    writer.write(evidence + ",");
                }
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Case findCaseById(String caseId) {
        for (Case forensicCase : cases) {
            if (forensicCase.getCaseId().equals(caseId)) {
                return forensicCase;
            }
        }
        return null;
    }
    private void displayLogin() {
        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JButton loginButton = new JButton("Login");

        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout(3, 2));
        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);
        loginPanel.add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                char[] password = passwordField.getPassword();
                if (validateLogin(username, password)) {
                    currentUser = getUserByUsername(username);
                    setVisible(true); // Show the main application window
                    Window loginWindow = SwingUtilities.windowForComponent((Component) e.getSource());
                    loginWindow.dispose();
                } else {
                    JOptionPane.showMessageDialog(ForensicManagementSystem.this, "Invalid login credentials", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JOptionPane.showOptionDialog(null, loginPanel, "Login", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{}, null);
    }

    private boolean validateLogin(String username, char[] password) {
        // Your validation logic here
        // This is where you would check if the username and password are correct
        // For demonstration, let's consider the first user in the list for simplicity
        if (!users.isEmpty()) {
            User user = users.get(0); // Getting the first user for demonstration
            return user.getUsername().equals(username) && user.getPassword().equals(new String(password));
        }
        return false;
    }

    private User getUserByUsername(String username) {
        // Your logic to retrieve a User object based on the username
        // This might involve searching through the users list
        // For demonstration, returning the first user in the list
        if (!users.isEmpty()) {
            return users.get(0); // Returning the first user for demonstration
        }
        return null;
    }

    private void addEvidenceToCase(Case forensicCase, JTextArea resultTextArea) {
        String evidenceItem = JOptionPane.showInputDialog(this, "Enter evidence for Case ID " + forensicCase.getCaseId() + ":");
        if (evidenceItem != null && !evidenceItem.isEmpty()) {
            forensicCase.addEvidence(evidenceItem);
            resultTextArea.append("Evidence added to Case ID " + forensicCase.getCaseId() + ": " + evidenceItem + "\n\n");
            saveCasesToFile(); // Save cases after adding evidence
        } else {
            JOptionPane.showMessageDialog(this, "Invalid evidence", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String performForensicAnalysis(String caseId) {
        Case forensicCase = new Case(caseId);
        forensicCase.addEvidence("Fingerprint");
        forensicCase.addEvidence("DNA Sample");
        cases.add(forensicCase);
        return "Analysis for Case ID " + caseId + " is complete. Evidence collected: Fingerprint, DNA Sample";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ForensicManagementSystem system = new ForensicManagementSystem();
                system.setVisible(true);
            }
        });
    }


    private static class User {
        private String username;
        private String password;

        public User(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}
