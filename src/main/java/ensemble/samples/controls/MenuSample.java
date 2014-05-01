/*
 * Copyright (c) 2008, 2012 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ensemble.samples.controls;

import ensemble.Sample;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;

/**
 * An example of a menu bar. The example includes use of the system bar, if the
 * current platform supports a system bar.
 *
 * @see javafx.scene.control.MenuBar
 * @see javafx.scene.control.Menu
 * @see javafx.scene.control.MenuItem
 * @resource menuInfo.png
 */
public class MenuSample extends Sample {

    private final Label sysMenuLabel = new Label("Using System Menu");

    public MenuSample() {
        final String os = System.getProperty("os.name");
        VBox vbox = new VBox(20);
        final Label outputLabel = new Label();
        final MenuBar menuBar = new MenuBar();
        
        //Sub menus for Options->Submenu 1
        MenuItem menu111 = MenuItemBuilder.create().text("blah").build();
        final MenuItem menu112 = MenuItemBuilder.create().text("foo").build();
        final CheckMenuItem menu113 = CheckMenuItemBuilder.create().text("Show \"foo\" item").selected(true).build();
        menu113.selectedProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable valueModel) {
                menu112.setVisible(menu113.isSelected());
                System.err.println("MenuItem \"foo\" is now " + (menu112.isVisible() ? "" : "not") + " visible.");
            }
        });
        // Options->Submenu 1 submenu 
        Menu menu11 = MenuBuilder.create()
                .text("Submenu 1")
                .graphic(new ImageView(new Image(MenuSample.class.getResourceAsStream("menuInfo.png"))))
                .items(menu111, menu112, menu113)
                .build();

        // Options->Submenu 2 submenu
        MenuItem menu121 = MenuItemBuilder.create().text("Item 1").build();
        MenuItem menu122 = MenuItemBuilder.create().text("Item 2").build();
        Menu menu12 = MenuBuilder.create().text("Submenu 2").items(menu121, menu122).build();

        // Options->Change Text
        final String change[] = {"Change Text", "Change Back"};
        final MenuItem menu13 = MenuItemBuilder.create().text(change[0]).accelerator(KeyCombination.keyCombination("Shortcut+C")).build();
        menu13.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                menu13.setText((menu13.getText().equals(change[0])) ? change[1] : change[0]);
                outputLabel.setText(((MenuItem) t.getTarget()).getText() + " - action called");
            }
        });        
        
        // Options menu
        Menu menu1 = MenuBuilder.create().text("Options").items(menu11, menu12, menu13).build();

        menuBar.getMenus().addAll(menu1);

        if (os != null && os.startsWith("Mac")) {
            Menu systemMenuBarMenu = new Menu("MenuBar Options");

            final CheckMenuItem useSystemMenuBarCB = new CheckMenuItem("Use System Menu Bar");
            useSystemMenuBarCB.setSelected(true);
            menuBar.useSystemMenuBarProperty().bind(useSystemMenuBarCB.selectedProperty());
            systemMenuBarMenu.getItems().add(useSystemMenuBarCB);

            menuBar.getMenus().add(systemMenuBarMenu);
        }

        vbox.getChildren().addAll(menuBar);
        if (os != null && os.startsWith("Mac")) {
            HBox hbox = HBoxBuilder.create().alignment(Pos.CENTER).build();
            sysMenuLabel.setStyle("-fx-font-size: 24");
            hbox.getChildren().add(sysMenuLabel);
            vbox.getChildren().add(hbox);
            sysMenuLabel.setVisible((menuBar.getHeight() == 0) ? true : false);
            menuBar.heightProperty().addListener(new ChangeListener<Number>() {
                public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                    sysMenuLabel.setVisible((menuBar.getHeight() == 0) ? true : false);
                }
            });
        }
        getChildren().add(vbox);
    }
}
