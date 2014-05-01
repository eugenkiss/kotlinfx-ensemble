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

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import ensemble.Sample;
import javafx.animation.FadeTransitionBuilder;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ParallelTransitionBuilder;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * A media player with controls for play, pause, stop, seek, and volume. This media player also demonstrates overlaying the player controls on top of the media.
 *
 * @see javafx.scene.media.MediaPlayer
 * @see javafx.scene.media.Media
 */
public class OverlayMediaPlayer extends Sample {
    private static final String MEDIA_URL = "http://download.oracle.com/otndocs/javafx/JavaRap_ProRes_H264_768kbit_Widescreen.mp4";
    private MediaPlayer mediaPlayer;
    final double mediaWidth = 480;  
    final double mediaHeight = 270;  

    public OverlayMediaPlayer() {
        mediaPlayer = new MediaPlayer(new Media(MEDIA_URL));
        mediaPlayer.setAutoPlay(true);
        PlayerPane playerPane = new PlayerPane(mediaPlayer);
        playerPane.setMinSize(mediaWidth, mediaHeight);  
        playerPane.setPrefSize(mediaWidth, mediaHeight);
        playerPane.setMaxSize(mediaWidth, mediaHeight);
        getStylesheets().add("ensemble/samples/media/OverlayMediaPlayer.css");
        getChildren().add(playerPane);
    }

    @Override public void play() {
        Status status = mediaPlayer.getStatus();
        if (status == Status.UNKNOWN || status == Status.HALTED) {
            return;
        }
        if (status == Status.PAUSED || status == Status.STOPPED || status == Status.READY) {
            mediaPlayer.play();
        }
    }

    @Override public void stop() {
        mediaPlayer.stop();
    }

    static class PlayerPane extends Region {
        private MediaPlayer mp;
        private MediaView mediaView;
        private final boolean repeat = false;
        private boolean stopRequested = false;
        private boolean atEndOfMedia = false;
        private Duration duration;
        private Slider timeSlider;
        private Label playTime;
        private Slider volumeSlider;
        private ToolBar topBar = new ToolBar();
        private ToolBar bottomBar = new ToolBar();
        private HBox mediaTopBar;
        private HBox mediaBottomBar;
        private ParallelTransition transition = null;
        final double mediaWidth = 480;  
        final double mediaHeight = 270;  
        
        private ChangeListener layoutListener = new ChangeListener() {
            @Override public void changed(ObservableValue ov, Object t, Object t1) {
                requestLayout();
            }
        };

        @Override protected void layoutChildren() {
            final double controlHeight = 40;
            final double controlOffset = 115;
            mediaView.relocate(0, 0);  
            mediaView.setFitWidth(mediaWidth);
            mediaView.setFitHeight(mediaHeight);
            topBar.resizeRelocate(0, 0, mediaWidth, controlHeight);  
            bottomBar.resizeRelocate(controlOffset, mediaHeight - controlHeight, mediaWidth, controlHeight);  
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

        public PlayerPane(final MediaPlayer mp) {
            this.mp = mp;
            setId("player-pane");

            mediaView = new MediaView(mp);

            mediaTopBar = HBoxBuilder.create()
                    .padding(new Insets(5, 10, 5, 10))
                    .alignment(Pos.CENTER)
                    .opacity(1.0)
                    .build();
            
            mediaBottomBar = HBoxBuilder.create()
                    .padding(new Insets(5, 10, 5, 10))
                    .alignment(Pos.CENTER)
                    .opacity(1.0)
                    .build();
                        
            topBar.setStyle("-fx-background-color: rgb(0,0,0.0,0.0);");
            bottomBar.setStyle("-fx-background-color: rgb(0,0,0.0,0.0);");
            
            setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent t) {
                    if (transition != null) transition.stop();
                    transition = ParallelTransitionBuilder.create()
                        .children(
                            FadeTransitionBuilder.create()
                                .node(topBar)
                                .toValue(1.0)
                                .duration(Duration.millis(200))
                                .interpolator(Interpolator.EASE_OUT)
                                .build(),
                            FadeTransitionBuilder.create()
                                .node(bottomBar)
                                .toValue(1.0)
                                .duration(Duration.millis(200))
                                .interpolator(Interpolator.EASE_OUT)
                                .build()
                        )
                        .build();
                    transition.play();
                }
            });
            setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent t) {
                    if (transition != null) transition.stop();
                    transition = ParallelTransitionBuilder.create()
                        .children(
                            FadeTransitionBuilder.create()
                                .node(topBar)
                                .toValue(0.0)
                                .duration(Duration.millis(800))
                                .interpolator(Interpolator.EASE_OUT)
                                .build(),
                            FadeTransitionBuilder.create()
                                .node(bottomBar)
                                .toValue(0.0)
                                .duration(Duration.millis(800))
                                .interpolator(Interpolator.EASE_OUT)
                                .build()
                        )
                        .build();
                    transition.play();
                }
            });

            mp.currentTimeProperty().addListener(new ChangeListener<Duration>() {
                @Override
                public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                    updateValues();
                }
            });
            mp.setOnPlaying(new Runnable() {
                public void run() {
                    if (stopRequested) {
                        mp.pause();
                        stopRequested = false;
                    } 
                }
            });
            mp.setOnReady(new Runnable() {
                public void run() {
                    duration = mp.getMedia().getDuration();
                    updateValues();
                }
            });
            mp.setOnEndOfMedia(new Runnable() {
                public void run() {
                    if (!repeat) {
                        stopRequested = true;
                        atEndOfMedia = true;
                    }
                }
            });
            mp.setCycleCount(repeat ? MediaPlayer.INDEFINITE : 1);

            // Time label
            Label timeLabel = LabelBuilder.create()
                    .text("Time")
                    .minWidth(Control.USE_PREF_SIZE)
                    .textFill(Color.WHITE)
                    .build();
            mediaTopBar.getChildren().add(timeLabel);
            
            // Time slider
            timeSlider = SliderBuilder.create()
                    .id("media-slider")
                    .minWidth(200)
                    .maxWidth(Double.MAX_VALUE)
                    .build();
            timeSlider.valueProperty().addListener(new InvalidationListener() {
                public void invalidated(Observable ov) {
                    if (timeSlider.isValueChanging()) {
                        // multiply duration by percentage calculated by slider position
                        if (duration != null) {
                            mp.seek(duration.multiply(timeSlider.getValue() / 100.0));
                        }
                        updateValues();

                    }
                }
            });
            mediaTopBar.getChildren().add(timeSlider);
            
            // Play label
            playTime = LabelBuilder.create()
                    .prefWidth(75)
                    .minWidth(75)
                    .textFill(Color.WHITE)
                    .build();
            mediaTopBar.getChildren().add(playTime);
            
            // Volume label
            Label volumeLabel = LabelBuilder.create()
                    .text("Vol")
                    .textFill(Color.WHITE)
                    .minWidth(Control.USE_PREF_SIZE)
                    .build();
            mediaTopBar.getChildren().add(volumeLabel);
            
            // Volume slider
            volumeSlider = SliderBuilder.create()
                    .id("media-slider")
                    .prefWidth(120)
                    .minWidth(30)
                    .maxWidth(Region.USE_PREF_SIZE)
                    .build();
            volumeSlider.valueProperty().addListener(new InvalidationListener() {
                public void invalidated(Observable ov) {
                }
            });
            volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    if (volumeSlider.isValueChanging()) {
                        mp.setVolume(volumeSlider.getValue() / 100.0);
                    }
                }
            });
            mediaTopBar.getChildren().add(volumeSlider);
                        
            final EventHandler<ActionEvent> backAction = new EventHandler<ActionEvent>() {
                public void handle(ActionEvent e) {
                    mp.seek(Duration.ZERO);
                }
            };
            final EventHandler<ActionEvent> stopAction = new EventHandler<ActionEvent>() {
                public void handle(ActionEvent e) {
                    mp.stop();
                }
            };
            final EventHandler<ActionEvent> playAction = new EventHandler<ActionEvent>() {
                public void handle(ActionEvent e) {
                    mp.play();
                }
            };
            final EventHandler<ActionEvent> pauseAction = new EventHandler<ActionEvent>() {
                public void handle(ActionEvent e) {
                    mp.pause();
                }
            };
            final EventHandler<ActionEvent> forwardAction = new EventHandler<ActionEvent>() {
                public void handle(ActionEvent e) {
                    Duration currentTime = mp.getCurrentTime();
                    mp.seek(Duration.seconds(currentTime.toSeconds() + 5.0));
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
                        
            topBar.getItems().add(mediaTopBar);
            bottomBar.getItems().add(mediaBottomBar);
            
            getChildren().addAll(mediaView, topBar, bottomBar);

            topBar.setOpacity(0.0); 
            bottomBar.setOpacity(0.0); 
        }

        protected void updateValues() {
            if (playTime != null && timeSlider != null && volumeSlider != null && duration != null) {
                Platform.runLater(new Runnable() {
                    public void run() {
                        Duration currentTime = mp.getCurrentTime();
                        playTime.setText(formatTime(currentTime, duration));
                        timeSlider.setDisable(duration.isUnknown());
                        if (!timeSlider.isDisabled() && duration.greaterThan(Duration.ZERO) && !timeSlider.isValueChanging()) {
                            timeSlider.setValue(currentTime.divide(duration).toMillis() * 100.0);
                        }
                        if (!volumeSlider.isValueChanging()) {
                            volumeSlider.setValue((int) Math.round(mp.getVolume() * 100));
                        }
                    }
                });
            }
        }

        private static String formatTime(Duration elapsed, Duration duration) {
            int intElapsed = (int)Math.floor(elapsed.toSeconds());
            int elapsedHours = intElapsed / (60 * 60);
            if (elapsedHours > 0) {
                intElapsed -= elapsedHours * 60 * 60;
            }
            int elapsedMinutes = intElapsed / 60;
            int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 - elapsedMinutes * 60;

            if (duration.greaterThan(Duration.ZERO)) {
                int intDuration = (int)Math.floor(duration.toSeconds());
                int durationHours = intDuration / (60 * 60);
                if (durationHours > 0) {
                    intDuration -= durationHours * 60 * 60;
                }
                int durationMinutes = intDuration / 60;
                int durationSeconds = intDuration - durationHours * 60 * 60 - durationMinutes * 60;

                if (durationHours > 0) {
                    return String.format("%d:%02d:%02d",
                                         elapsedHours, elapsedMinutes, elapsedSeconds);
                } else {
                    return String.format("%02d:%02d",
                                         elapsedMinutes, elapsedSeconds);
                }
            } else {
                if (elapsedHours > 0) {
                    return String.format("%d:%02d:%02d",
                                         elapsedHours, elapsedMinutes, elapsedSeconds);
                } else {
                    return String.format("%02d:%02d",
                                         elapsedMinutes, elapsedSeconds);
                }
            }
        }
    }
}
