package com.yandex.ajwar.view;

import com.yandex.ajwar.MainApp;
import com.yandex.ajwar.model.StringData;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;

import javax.swing.*;
import java.io.File;


/**
 * Created by 53189 on 14.01.16.
 */
public class InnerWindowController {
    @FXML
    private TableView<StringData> stringTableView;
    @FXML
    private TableColumn<StringData, String> stringNameColumn;
    @FXML
    private TextField directoryNameTextField;
    @FXML
    private TextField periodTextField;
    @FXML
    private TextField maskTextField;

    private MainApp mainApp;

    public InnerWindowController() {
    }

    @FXML
    private void initialize() {
        stringNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameFileProperty());
    }

    /**
     * Функция открытия директории
     */
    @FXML
    private void handleOpen() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Выберите папку для поиска и удаления файлов.");
        directoryChooser.setInitialDirectory(new File("D:\\Test\\"));
        File file = directoryChooser.showDialog(mainApp.getPrimaryStage());
        if (file == null) directoryNameTextField.setText("");
        else directoryNameTextField.setText(file.getAbsolutePath());
    }

    @FXML
    private void handleCancel() {
        MainApp.getLog().info("Юзер " + System.getProperty("user.name") + " завершил работу!");
        System.exit(0);
    }

    /**
     * Привязка метода к кнопке
     */
    @FXML
    private void handleScan() {
        try {
            if (verificationTextFieldScan()) {
                findAndDeleteFile(directoryNameTextField.getText());
            }
        } catch (Exception e) {
            if (MainApp.isFlag())
                JOptionPane.showMessageDialog(mainApp.getDialog(), "Произошла непредвиденная ошибка при удалении файл" +
                        "ов!", "Ошибка удаления!", JOptionPane.ERROR_MESSAGE);
            else MainApp.getLog().error("Ошибка во время удаления файлов(InnerWindowController).", e);
        }
    }

    /**
     * Проверка заполненных всех textfield в форме
     */
    private boolean verificationTextFieldScan() {
        if (directoryNameTextField.getText() == null || "".equals(directoryNameTextField.getText())
                || maskTextField.getText() == null || "".equals(maskTextField.getText())) {
            JOptionPane.showMessageDialog(mainApp.getDialog(), "Заполните все необходимые поля!!!", "Внимание", JOptionPane.WARNING_MESSAGE);
            return false;
        } else return true;
    }

    public void adapterHandleScan() {
        handleScan();
    }

    private void showStringData(String directoryString) {
        StringData tempStringData = new StringData(directoryString);
        mainApp.getStringNameFileData().add(tempStringData);
    }

    /**
     * Поиск и удаления файлов в выбранной директории.
     */
    private void findAndDeleteFile(String str) {
        File[] list = null;
        File file = new File(str);
        if (file.isDirectory()) {
            list = file.listFiles();
            for (int i = 0; i < list.length; i++) {
                if (list[i].isDirectory()) {
                    findAndDeleteFile(list[i].getAbsolutePath());
                } else if (list[i].getName().indexOf(maskTextField.getText()) > -1) {
                    showStringData(list[i].getAbsolutePath());
                    list[i].delete();
                    MainApp.getLog().warn("Произошло удаление файла:" + list[i].getAbsolutePath());
                }
            }
        } else {
            JOptionPane.showMessageDialog(mainApp.getDialog(), "Некорректное название каталога!", "Ввод данных.", JOptionPane.WARNING_MESSAGE);
        }
    }

    public TextField getMaskTextField() {
        return maskTextField;
    }

    public TextField getDirectoryNameTextField() {
        return directoryNameTextField;
    }

    public TextField getPeriodTextField() {
        return periodTextField;
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        stringTableView.setItems(mainApp.getStringNameFileData());
    }

}
