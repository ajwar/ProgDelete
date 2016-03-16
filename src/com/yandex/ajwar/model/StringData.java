package com.yandex.ajwar.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by 53189 on 14.01.16.
 */
public class StringData {
    private final StringProperty nameFile;
    public StringData(){
        this.nameFile=null;
    }

    public StringData(String nameFile){
        this.nameFile=new SimpleStringProperty(nameFile);
    }

    public void setNameFile(String nameFile) {
        this.nameFile.set(nameFile);
    }

    public String getNameFile() {
        return nameFile.get();
    }

    public StringProperty nameFileProperty() {
        return nameFile;
    }
}
