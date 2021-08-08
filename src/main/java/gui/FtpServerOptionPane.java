package gui;

import org.jetbrains.annotations.Nullable;
import util.SpringUtilities;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * Панель подключения к ftp серверу
 */
public final class FtpServerOptionPane {
    private final JTextField hostField;
    private final JTextField portField;
    private final JTextField loginField;
    private final JTextField passwordField;
    private final JPanel panel;

    public FtpServerOptionPane() {
        this.hostField = new JTextField(10);
        this.portField = new JTextField(10);
        this.loginField = new JTextField(10);
        this.passwordField = new JTextField(1);

        String[] labels = {"Host: ", "Port: ", "Login: ", "Password: "};
        JTextField[] jTextFields = {hostField, portField, loginField, passwordField};

        this.panel = new JPanel(new SpringLayout());
        for (int i = 0; i < labels.length; i++) {
            JLabel l = new JLabel(labels[i], JLabel.TRAILING);
            this.panel.add(l);
            l.setLabelFor(jTextFields[i]);
            this.panel.add(jTextFields[i]);
        }

        SpringUtilities.makeCompactGrid(this.panel,
                labels.length, 2,
                6, 6,
                6, 6);
    }

    @Nullable
    public FtpServerOption showConfirmDialog(Component parentComponent) {
        int result = JOptionPane.showConfirmDialog(parentComponent, panel,
                "Введите данные подключения к ftp серверу", JOptionPane.DEFAULT_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            return new FtpServerOption(
                    hostField.getText(), portField.getText(),
                    loginField.getText(), passwordField.getText()
            );
        }
        return null;
    }

    public static class FtpServerOption {
        private final String host;
        private final String port;
        private final String login;
        private final String password;

        public FtpServerOption(String host, String port, String login, String password) {
            this.host = host;
            this.port = port;
            this.login = login;
            this.password = password;
        }

        public String getHost() {
            return host;
        }

        public Integer getPort() {
            if (port == null || port.length() == 0) {
                return null;
            }
            return Integer.parseInt(port);
        }

        public String getLogin() {
            if (login == null || login.length() == 0) {
                return "anonymous";
            }
            return login;
        }

        public String getPassword() {
            return Objects.requireNonNullElse(password, "");
        }
    }
}
