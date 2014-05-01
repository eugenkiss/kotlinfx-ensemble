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

package ensemble.samples.media;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import ensemble.Sample;
import ensemble.controls.SimplePropertySheet;
import javafx.animation.ParallelTransition;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.*;

/**
 * An alpha media player with 2 different media views and alpha channels.
 *
 * @see javafx.scene.media.MediaPlayer
 * @see javafx.scene.media.Media
 */
public class AlphaMediaPlayer extends Sample {
    private static final String ARTH_URL = "http://download.oracle.com/otndocs/products/javafx/arth_512.flv";
    private static final String FIER_URL = "http://download.oracle.com/otndocs/products/javafx/fier_512.flv";
    
    PlanetaryPlayerPane planetaryPlayerPane;

    private MediaPlayer arthPlayer;
    private MediaPlayer fierPlayer;
    
    SimpleDoubleProperty arthPos = new SimpleDoubleProperty(-90.0);
    SimpleDoubleProperty fierPos = new SimpleDoubleProperty(50.0);
    SimpleDoubleProperty arthRate = new SimpleDoubleProperty(1.0);
    SimpleDoubleProperty fierRate = new SimpleDoubleProperty(1.0);

    public AlphaMediaPlayer() {
        arthPlayer = new MediaPlayer(new Media(ARTH_URL));
        arthPlayer.setAutoPlay(true);
        fierPlayer = new MediaPlayer(new Media(FIER_URL));
        fierPlayer.setAutoPlay(true);
        
        arthPos.addListener(new InvalidationListener() {
            public void invalidated(Observable observable) {
                planetaryPlayerPane.setTranslate1(arthPos.doubleValue());
            }
        });
        
        fierPos.addListener(new InvalidationListener() {
            public void invalidated(Observable observable) {
                planetaryPlayerPane.setTranslate2(fierPos.doubleValue());

            }
        });
                
        arthRate.addListener(new InvalidationListener() {
            public void invalidated(Observable observable) {
                arthPlayer.setRate(arthRate.doubleValue());
            }
        });
        
        fierRate.addListener(new InvalidationListener() {
            public void invalidated(Observable observable) {
                fierPlayer.setRate(fierRate.doubleValue());
            }
        });
        
        planetaryPlayerPane = new PlanetaryPlayerPane(arthPlayer, fierPlayer);

        planetaryPlayerPane.setMinSize(480, 320);  
        planetaryPlayerPane.setPrefSize(480, 320);
        planetaryPlayerPane.setMaxSize(480, 320);
        getStylesheets().add("ensemble/samples/media/OverlayMediaPlayer.css");
        // REMOVE ME
        setControls(
                new SimplePropertySheet.PropDesc("Arth Position", arthPos, -100d, 100d),
                new SimplePropertySheet.PropDesc("Fier Position", fierPos, -100d, 100d),
                new SimplePropertySheet.PropDesc("Arth Rate", arthRate, 0.1d, 1d),
                new SimplePropertySheet.PropDesc("Fier Rate", fierRate, 0.1d, 1d)
        );
        // END REMOVE ME
        getChildren().add(planetaryPlayerPane);
    }

    @Override public void play() {
        Status status = fierPlayer.getStatus();
        if (status == Status.UNKNOWN || status == Status.HALTED) {
            return;
        }
        if (status == Status.PAUSED || status == Status.STOPPED || status == Status.READY) {
            fierPlayer.play();
            arthPlayer.play();
        }
    }

    @Override public void stop() {
        fierPlayer.stop();
        arthPlayer.stop();
    }

    static class PlanetaryPlayerPane extends BorderPane {
        private MediaPlayer mp;
        private Group mediaViewer1;  
        private Group mediaViewer2;  
        private Group mediaViewerGroup;
        private final boolean repeat = true;
        private boolean stopRequested = false;
        private boolean atEndOfMedia = false;
        private Duration duration;
        private HBox mediaBottomBar;
        private ParallelTransition transition = null;

        @Override protected void layoutChildren() {
            super.layoutChildren();
        }

        @Override protected double computeMinWidth(double height) {
            return mediaBottomBar.prefWidth(-1);
        }

        @Override protected double computeMinHeight(double width) {
            return 200;
        }

        @Override protected double computePrefWidth(double height) {
            return Math.max(mp.getMedia().getWidth(), mediaBottomBar.prefWidth(height));
        }

        @Override protected double computePrefHeight(double width) {
            return mp.getMedia().getHeight() + mediaBottomBar.prefHeight(width);
        }

        @Override protected double computeMaxWidth(double height) { return Double.MAX_VALUE; }

        @Override protected double computeMaxHeight(double width) { return Double.MAX_VALUE; }

        public PlanetaryPlayerPane(final MediaPlayer mp1, final MediaPlayer mp2) {
            this.mp = mp1;
            setId("player-pane");

            mediaViewer1 = createViewer(mp1, 0.4, false);
            mediaViewer2 = createViewer(mp2, 0.55, false);

            mediaViewerGroup = new Group();
            mediaViewerGroup.getChildren().add(mediaViewer2);
            mediaViewerGroup.getChildren().add(mediaViewer1);
            mediaViewerGroup.setTranslateX(-17.0);
            mediaViewerGroup.setTranslateY(-115.0);
            setTranslate1(-90.0);
            setTranslate2(50.0);

            Pane mvPane = new Pane() { };
            mvPane.setId("media-pane");
            mvPane.getChildren().add(mediaViewerGroup);
            setCenter(mvPane);

            mediaBottomBar = HBoxBuilder.create()
                    .padding(new Insets(5, 10, 5, 10))
                    .alignment(Pos.CENTER)
                    .opacity(1.0)
                    .build();
            BorderPane.setAlignment(mediaBottomBar, Pos.CENTER);

            mp1.setOnPlaying(new Runnable() {
                public void run() {
                    if (stopRequested) {
                        mp1.pause();
                        stopRequested = false;
                    } 
                }
            });
            mp1.setOnEndOfMedia(new Runnable() {
                public void run() {
                    if (!repeat) {
                        stopRequested = true;
                        atEndOfMedia = true;
                    }
                }
            });
            mp1.setCycleCount(repeat ? MediaPlayer.INDEFINITE : 1);

            mp2.setOnPlaying(new Runnable() {
                public void run() {
                    if (stopRequested) {
                        mp2.pause();
                        stopRequested = false;
                    } 
                }
            });
            mp2.setOnEndOfMedia(new Runnable() {
                public void run() {
                    if (!repeat) {
                        stopRequested = true;
                        atEndOfMedia = true;
                    }
                }
            });
            mp2.setCycleCount(repeat ? MediaPlayer.INDEFINITE : 1);

            final EventHandler<ActionEvent> backAction = new EventHandler<ActionEvent>() {
                public void handle(ActionEvent e) {
                    mp1.seek(Duration.ZERO);
                    mp2.seek(Duration.ZERO);
                }
            };
            final EventHandler<ActionEvent> stopAction = new EventHandler<ActionEvent>() {
                public void handle(ActionEvent e) {
                    mp1.stop();
                    mp2.stop();
                }
            };
            final EventHandler<ActionEvent> playAction = new EventHandler<ActionEvent>() {
                public void handle(ActionEvent e) {
                    mp1.play();
                    mp2.play();
                }
            };
            final EventHandler<ActionEvent> pauseAction = new EventHandler<ActionEvent>() {
                public void handle(ActionEvent e) {
                    mp1.pause();
                    mp2.pause();
                }
            };
            final EventHandler<ActionEvent> forwardAction = new EventHandler<ActionEvent>() {
                public void handle(ActionEvent e) {
                    Duration currentTime = mp1.getCurrentTime();
                    mp1.seek(Duration.seconds(currentTime.toSeconds() + 0.1));
                    mp2.seek(Duration.seconds(currentTime.toSeconds() + 0.1));
                }
            };

            mediaBottomBar = HBoxBuilder.create()
                    .id("bottom")
                    .spacing(0)
                    .alignment(Pos.CENTER)
                    .children(
                        ButtonBuilder.create()
                            .id("back-button")
                            .text("Back")
                            .onAction(backAction)
                            .build(),
                        ButtonBuilder.create()
                            .id("stop-button")
                            .text("Stop")
                            .onAction(stopAction)
                            .build(),
                        ButtonBuilder.create()
                            .id("play-button")
                            .text("Play")
                            .onAction(playAction)
                            .build(),
                        ButtonBuilder.create()
                            .id("pause-button")
                            .text("Pause")
                            .onAction(pauseAction)
                            .build(),
                        ButtonBuilder.create()
                            .id("forward-button")
                            .text("Forward")
                            .onAction(forwardAction)
                            .build()
                     )
                    .build();
            
            setBottom(mediaBottomBar);
        }

        public void setTranslate1(double tx) {
            mediaViewer1.setTranslateX(tx);
        }
        
        public void setTranslate2(double tx) {
            mediaViewer2.setTranslateX(tx);
        }

        private static Group createViewer(final MediaPlayer player, final double scale, boolean blur) {
            Group mediaGroup = new Group();

            final MediaView mediaView = new MediaView(player);

            if (blur) {
                BoxBlur bb = new BoxBlur();
                bb.setWidth(4);
                bb.setHeight(4);
                bb.setIterations(1);
                mediaView.setEffect(bb);
            }

            double width = player.getMedia().getWidth();
            double height = player.getMedia().getHeight();

            mediaView.setFitWidth(width);
            mediaView.setTranslateX(-width/2.0); 
            mediaView.setScaleX(-scale);

            mediaView.setFitHeight(height);
            mediaView.setTranslateY(-height/2.0);
            mediaView.setScaleY(scale);

            mediaView.setDepthTest(DepthTest.ENABLE);
            mediaGroup.getChildren().add(mediaView);
            return mediaGroup;
        }
    }
}
