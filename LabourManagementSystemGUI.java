import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class Employee {
    String name;
    String contactInfo;

    public Employee(String name, String contactInfo) {
        this.name = name;
        this.contactInfo = contactInfo;
    }

    @Override
    public String toString() {
        return name;
    }
}

class Contract {
    String jobDescription;
    int durationMonths;
    double hourlyRate;

    public Contract(String jobDescription, int durationMonths, double hourlyRate) {
        this.jobDescription = jobDescription;
        this.durationMonths = durationMonths;
        this.hourlyRate = hourlyRate;
    }

    @Override
    public String toString() {
        return jobDescription;
    }
}

class Attendance {
    String date;
    int hoursWorked;

    public Attendance(String date, int hoursWorked) {
        this.date = date;
        this.hoursWorked = hoursWorked;
    }
}

class Payment {
    double totalPayment;

    public void calculatePayment(Contract contract, Attendance attendance) {
        totalPayment = contract.hourlyRate * attendance.hoursWorked;
    }

    public double getTotalPayment() {
        return totalPayment;
    }
}

class ReportGenerator {
    public String generateReport(List<Employee> employees, List<Contract> contracts,
                                 List<Attendance> attendanceRecords, List<Payment> payments) {
        StringBuilder reportText = new StringBuilder();

        reportText.append(String.format("| %-25s | %-40s | %-15s | %-15s |\n", "Employee", "Job Description", "Hours Worked", "Total Payment"));
        reportText.append("|--------------------------|----------------------------------------|-----------------|-----------------|\n");

        int size = Math.max(Math.max(Math.max(employees.size(), contracts.size()), attendanceRecords.size()), payments.size());

        for (int i = 0; i < size; i++) {
            String employeeName = (i < employees.size()) ? employees.get(i).name : "";
            String jobDescription = (i < contracts.size()) ? contracts.get(i).jobDescription : "";
            int hoursWorked = (i < attendanceRecords.size()) ? attendanceRecords.get(i).hoursWorked : 0;
            double totalPayment = (i < payments.size()) ? payments.get(i).getTotalPayment() : 0.0;

            reportText.append(String.format("| %-25s | %-40s | %-15d | %-15.2f |\n", employeeName, jobDescription, hoursWorked, totalPayment));
        }

        return reportText.toString();
    }
}

class LabourManagementSystemGUI extends JFrame {
    private List<Employee> employees;
    private List<Contract> contracts;
    private List<Attendance> attendanceRecords;
    private List<Payment> payments;
    private ReportGenerator reportGenerator;
    private Connection connection;

    public LabourManagementSystemGUI() {
        this.employees = new ArrayList<>();
        this.contracts = new ArrayList<>();
        this.attendanceRecords = new ArrayList<>();
        this.payments = new ArrayList<>();
        this.reportGenerator = new ReportGenerator();

        // Connect to the database
        connectToDatabase();

        // Load data from the database
        loadDataFromDatabase();

        initComponents();
    }

    private void connectToDatabase() {
        String url = "jdbc:mysql://localhost:3306/labour_management";
        String user = "root";
        String password = "root123";

        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    private void handleDatabaseError(SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void loadDataFromDatabase() {
        employees.clear();
        contracts.clear();
        attendanceRecords.clear();

        // Load employees from the database
        String selectEmployeesQuery = "SELECT * FROM employees";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectEmployeesQuery)) {
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String contactInfo = resultSet.getString("contact_info");
                employees.add(new Employee(name, contactInfo));
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }

        // Load contracts from the database
        String selectContractQuery = "SELECT * FROM contracts";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectContractQuery)) {
            while (resultSet.next()) {
                String jobDescription = resultSet.getString("job_description");
                int durationMonths = resultSet.getInt("duration_months");
                double hourlyRate = resultSet.getDouble("hourly_rate");
                contracts.add(new Contract(jobDescription, durationMonths, hourlyRate));
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }

        // Load attendance records from the database
        String selectAttendanceQuery = "SELECT id, date, hours_worked FROM attendance";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectAttendanceQuery)) {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");  // assuming id is an integer
                String date = resultSet.getString("date");
                int hoursWorked = resultSet.getInt("hours_worked");
                attendanceRecords.add(new Attendance(date, hoursWorked));
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    private void initComponents() {
        JButton registerEmployeeButton = new JButton("Register Employee");
        JButton assignContractButton = new JButton("Assign Contract");
        JButton trackAttendanceButton = new JButton("Track Attendance");
        JButton generateReportButton = new JButton("Generate Report");

        JPanel panel = new JPanel();
        panel.add(registerEmployeeButton);
        panel.add(assignContractButton);
        panel.add(trackAttendanceButton);
        panel.add(generateReportButton);

        registerEmployeeButton.addActionListener(e -> registerEmployee());
        assignContractButton.addActionListener(e -> assignContract());
        trackAttendanceButton.addActionListener(e -> trackAttendance());
        generateReportButton.addActionListener(e -> {
            calculatePayments();
            String report = reportGenerator.generateReport(employees, contracts, attendanceRecords, payments);
            showReportDialog(report);
        });

        this.getContentPane().add(panel);
        this.setSize(600, 250); // Adjusted the UI size
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    private void showReportDialog(String report) {
        JFrame frame = new JFrame("Report");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 400);

        JTextArea reportArea = new JTextArea(report);
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN 12));

        JScrollPane scrollPane = new JScrollPane(reportArea);

        frame.getContentPane().add(scrollPane);
        frame.setVisible(true);
    }

    private void registerEmployee() {
        String name = JOptionPane.showInputDialog("Enter employee name:");
        String contactInfo = JOptionPane.showInputDialog("Enter contact information:");

        if (isValidInput(name, contactInfo)) {
            employees.add(new Employee(name, contactInfo));

            // Insert into the database
            String insertEmployeeQuery = "INSERT INTO employees (name, contact_info) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertEmployeeQuery)) {
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, contactInfo);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                handleDatabaseError(e);
            }

            JOptionPane.showMessageDialog(this, "Employee registered successfully.");
        } else {
            JOptionPane.showMessageDialog(this, "Invalid input. Please provide valid information.");
        }
    }

    private void assignContract() {
        if (employees.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No employees registered. Please register an employee first.");
            return;
        }

        Employee selectedEmployee = (Employee) JOptionPane.showInputDialog(this, "Select employee:",
                "Assign Contract", JOptionPane.QUESTION_MESSAGE, null, employees.toArray(), null);

        if (selectedEmployee != null) {
            String jobDescription = JOptionPane.showInputDialog("Enter job description:");
            int durationMonths = getIntegerInput("Enter contract duration (months):");
            double hourlyRate = getDoubleInput("Enter hourly rate:");

            if (isValidInput(jobDescription) && durationMonths > 0 && hourlyRate > 0) {
                contracts.add(new Contract(jobDescription, durationMonths, hourlyRate));

                // Insert into the database
                String insertContractQuery = "INSERT INTO contracts (job_description, duration_months, hourly_rate) " +
                        "VALUES (?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertContractQuery)) {
                    preparedStatement.setString(1, jobDescription);
                    preparedStatement.setInt(2, durationMonths);
                    preparedStatement.setDouble(3, hourlyRate);
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    handleDatabaseError(e);
                }

                JOptionPane.showMessageDialog(this, "Contract assigned successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid input. Please provide valid information.");
            }
        }
    }

    private void trackAttendance() {
        if (employees.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No employees registered. Please register an employee first.");
            return;
        }

        Employee selectedEmployee = (Employee) JOptionPane.showInputDialog(this, "Select employee:",
                "Track Attendance", JOptionPane.QUESTION_MESSAGE, null, employees.toArray(), null);

        if (selectedEmployee != null) {
            String date = JOptionPane.showInputDialog("Enter attendance date:");
            int hoursWorked = getIntegerInput("Enter hours worked:");

            if (isValidInput(date) && hoursWorked >= 0) {
                attendanceRecords.add(new Attendance(date, hoursWorked));

                // Insert into the database
                String insertAttendanceQuery = "INSERT INTO attendance (employee_id, date, hours_worked) " +
                        "VALUES (?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertAttendanceQuery)) {
                    preparedStatement.setInt(1, employees.indexOf(selectedEmployee) + 1);
                    preparedStatement.setString(2, date);
                    preparedStatement.setInt(3, hoursWorked);
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    handleDatabaseError(e);
                }

                JOptionPane.showMessageDialog(this, "Attendance tracked successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid input. Please provide valid information.");
            }
        }
    }

    private void calculatePayments() {
        payments.clear();

        // Ensure both contracts and attendanceRecords have the same size
        int size = Math.min(contracts.size(), attendanceRecords.size());

        for (int i = 0; i < size; i++) {
            Contract contract = contracts.get(i);
            Attendance attendance = attendanceRecords.get(i);

            Payment payment = new Payment();
            payment.calculatePayment(contract, attendance);

            payments.add(payment);
        }
    }

    private boolean isValidInput(String... inputs) {
        for (String input : inputs) {
            if (input == null || input.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private int getIntegerInput(String prompt) {
        int result = 0;
        boolean validInput = false;
        while (!validInput) {
            String input = JOptionPane.showInputDialog(prompt);
            try {
                result = Integer.parseInt(input);
                validInput = true;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid integer.");
            }
        }
        return result;
    }

    private double getDoubleInput(String prompt) {
        double result = 0;
        boolean validInput = false;
        while (!validInput) {
            String input = JOptionPane.showInputDialog(prompt);
            try {
                result = Double.parseDouble(input);
                validInput = true;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number.");
            }
        }
        return result;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LabourManagementSystemGUI());
    }
}
