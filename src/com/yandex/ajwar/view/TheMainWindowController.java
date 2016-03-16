package com.yandex.ajwar.view;

import com.yandex.ajwar.MainApp;
import javafx.fxml.FXML;
import javax.swing.*;


public class TheMainWindowController {
    private MainApp mainApp;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;

    }

    @FXML
    private void handleExit(){
        mainApp.savePrefs();
        MainApp.getLog().info("Юзер " + System.getProperty("user.name") + " завершил работу!");
        System.exit(0);
    }

    @FXML
    private void handleAbout(){
        JOptionPane.showMessageDialog(mainApp.getDialog(), "Автор:Шагов Айвар\n" +
                "Отдел АСУ\nтелефон 22-35.", "Программа для чистки файлов.", JOptionPane.INFORMATION_MESSAGE);
    }
}
