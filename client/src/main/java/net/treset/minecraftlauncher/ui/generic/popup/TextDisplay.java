package net.treset.minecraftlauncher.ui.generic.popup;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

public class TextDisplay extends StackPane {
    private String text;
    private boolean html;

    private Node baseNode;

    public TextDisplay(String text, boolean html) {
        this.text = text;
        this.html = html;
        update();
    }

    public TextDisplay(String text) {
        this(text, false);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        update();
    }

    public boolean isHtml() {
        return html;
    }

    public void setHtml(boolean html) {
        this.html = html;
        update();
    }

    public void update() {
        ObservableList<String> styleClasses;
        if(!this.getChildren().isEmpty()) {
            styleClasses = baseNode.getStyleClass();
        } else {
            styleClasses = this.getStyleClass();
        }
        this.getChildren().clear();
        if(html) {
            Platform.runLater(() -> {
                WebView webView = new WebView();
                webView.getStyleClass().addAll(styleClasses);
                webView.getEngine().loadContent(
                        "<body style=\"background:#202020; color:white; font-family:sans-serif\">" +
                        text +
                        "</body>"
                );
                baseNode = webView;
                this.getChildren().add(webView);
            });
        } else {
            Label label = new Label(text);
            label.getStyleClass().addAll(styleClasses);
            baseNode = label;
            this.getChildren().add(label);
        }
    }

    public ObservableList<String> getStyleClasses() {
        return baseNode.getStyleClass();
    }
}
