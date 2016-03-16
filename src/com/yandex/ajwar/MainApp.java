package com.yandex.ajwar;/**
 * Created by 53189 on 14.01.16.
 */

import com.yandex.ajwar.model.StringData;
import com.yandex.ajwar.view.InnerWindowController;
import com.yandex.ajwar.view.TheMainWindowController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.*;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.util.Date;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.prefs.Preferences;

public class MainApp extends Application{

    private Stage primaryStage;
    private BorderPane rootLayout;
    private ObservableList<StringData> stringNameFileData = FXCollections.observableArrayList();
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    private InnerWindowController controller;
    private Preferences progsDeletePrefs;
    private static boolean flag=false;
    private static final Logger log=Logger.getLogger(MainApp.class);
    private final JDialog dialog=new JDialog();

    @Override
    public void start(Stage primaryStage) {
        log.info("Юзер " + System.getProperty("user.name") + " запустил прогу!");
        Task<Integer> task=new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                while(true){
                    Thread.sleep(1000*60*60);//20 минут
                    break;
                }
                if (!flag) {
                    log.info("Юзер " + System.getProperty("user.name") + " завершил работу!");
                    System.exit(0);
                }
                return null;
            }
        };
        Thread thread=new Thread(task);
        thread.setDaemon(true);
        thread.start();
        comparePreferecesAndDelete();
        Platform.setImplicitExit(false);
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("ProgDelete");
        //this.primaryStage.getIcons().add(new Image("file:resources/images/android_platform.png"));
        this.primaryStage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/resources/images/android_platform.png")));
        initTheMainWindow();
        showInnerWindow();
        systemTrayInit();
        systemTrayIconifiedListener();

    }

    /**Загружает главную форму.*/
    public void initTheMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/TheMainWindow.fxml"));
            //loader.setLocation(MainApp.class.getResourceAsStream("resources/images/android_platform.png"));
            rootLayout = (BorderPane) loader.load();
            Scene scene = new Scene(rootLayout);
            primaryStage.centerOnScreen();
            primaryStage.setScene(scene);
            TheMainWindowController controller=loader.getController();
            controller.setMainApp(this);
            //primaryStage.show();
            primaryStage.setOnCloseRequest(new EventHandler<javafx.stage.WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    savePrefs();
                    log.info("Юзер " + System.getProperty("user.name") + " завершил работу!");
                    System.exit(0);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Ошибка  при чтении файла TheMainWindow.fxml в методе (MainApp.showInnerWindow).",e);
        }
    }

    /** Внедряет в главную форму вторичную*/
    public void showInnerWindow() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/InnerWindow.fxml"));
            AnchorPane personOverview = (AnchorPane) loader.load();
            rootLayout.setCenter(personOverview);
            controller = loader.getController();
            controller.setMainApp(this);
            loadPrefs();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Ошибка  при чтении файла InnerWindow.fxml в методе (MainApp.showInnerWindow).",e);
        }
    }
    /**Регистрация слушателя для минимизации в Трэй*/
    public void systemTrayIconifiedListener(){
        primaryStage.iconifiedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                if(t1.booleanValue()){
                    try {
                        systemTray.add(trayIcon);
                        primaryStage.hide();
                    } catch (AWTException e) {
                        e.printStackTrace();
                        log.error("Ошибка в методе сворачивания (MainApp.systemTrayIconifiedListener) в трей!",e);
                    }
                }
            }
        });

    }
    /**Сохранение данных формы в реестр*/
    public void savePrefs(){
        progsDeletePrefs.put("mask",getController().getMaskTextField().getText());
        progsDeletePrefs.put("directory",getController().getDirectoryNameTextField().getText());
        int period=Integer.parseInt(getController().getPeriodTextField().getText());
        if (progsDeletePrefs.getInt("period",101)!=period){
            Date date=new Date();
            progsDeletePrefs.putInt("period", period);
            progsDeletePrefs.putLong("startData", date.getTime());
            /**Прибавляем к стартовой дате период*/
            long day=period*24*1000*60*60;
            progsDeletePrefs.putLong("endData", date.getTime()+day);
        }

    }

    /**Загрузка данных формы в реестр*/
    public void loadPrefs(){
        getController().getDirectoryNameTextField().setText(progsDeletePrefs.get("directory","D:\\"));
        getController().getMaskTextField().setText(progsDeletePrefs.get("mask","newVer"));
        getController().getPeriodTextField().setText(progsDeletePrefs.getInt("period", 2)+"");
    }

    /**Проверка данных сканирования и удаления*/
    public void comparePreferecesAndDelete(){
        Date date=new Date();
        Long end=progsDeletePrefs.getLong("endData",date.getTime());
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                    if  (date.getTime()>=end){
                        getController().adapterHandleScan();
                        int period=Integer.parseInt(getController().getPeriodTextField().getText());
                        progsDeletePrefs.putLong("startData", date.getTime());
                        long day=period*24*1000*60*60;
                        progsDeletePrefs.putLong("endData", date.getTime()+day);
                    }
            }
        });
    }
    /**Регистрация слушателя для восстановления из Трэя и навешивание иконки*/
    public void systemTrayInit(){
        if (SystemTray.isSupported()){
            systemTray=SystemTray.getSystemTray();
            java.awt.Image image=new ImageIcon(MainApp.class.getResource("/resources/images/android_platform.png")).getImage();
            trayIcon=new TrayIcon(image,"ProgDelete");
            trayIcon.setImageAutoSize(true);
            try {
                systemTray.add(trayIcon);
                primaryStage.hide();
            } catch (AWTException e) {
                e.printStackTrace();
                log.error("Произошла ошибка в методе разворачивания (MainApp.SystemTrayInit) из трея!",e);
            }
            MouseListener mouseListenerSystemTray=new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount()>=1 && e.getButton()==e.BUTTON1){
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    primaryStage.setIconified(false);
                                    systemTray.remove(trayIcon);
                                    if (e.getX()>0) primaryStage.setX(e.getX());
                                    else primaryStage.setX(-e.getY());
                                    primaryStage.centerOnScreen();
                                    //primaryStage.toFront();
                                    primaryStage.show();
                                    flag=true;
                                } catch (Exception e) {
                                    log.error("Произошла ошибка в методе разворачивания (MainApp.mouseClicked) из трея!",e);
                                }
                            }
                        });
                    }}
            };
            trayIcon.addMouseListener(mouseListenerSystemTray);
        }else {
            System.err.println("Tray unavailable");
            log.error("Произошла ошибка в методе (MainApp.SystemTrayInit).Трэй не поддерживается данной ОС.");
        }
    }

    public static Logger getLog() {
        return log;
    }

    public InnerWindowController getController() {
        return controller;
    }

    public Preferences getProgsDeletePrefs() {
        return progsDeletePrefs;
    }

    public static boolean isFlag() {
        return flag;
    }

    public void setProgsDeletePrefs(Preferences progsDeletePrefs) {
        this.progsDeletePrefs = progsDeletePrefs;
    }

    public JDialog getDialog() {
        return dialog;
    }

    public MainApp() {
        progsDeletePrefs= Preferences.userRoot().node("ProgDelete");
        dialog.setAlwaysOnTop(true);
    }

    public MainApp(String tempString) {
        stringNameFileData.add(new StringData(tempString));
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public ObservableList<StringData> getStringNameFileData() {
        return stringNameFileData;
    }

    public static void main(String[] args) {
        launch(args);
    }
}