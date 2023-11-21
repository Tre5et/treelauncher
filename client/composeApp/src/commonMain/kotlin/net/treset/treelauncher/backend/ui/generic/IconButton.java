package net.treset.minecraftlauncher.ui.generic;

import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.converter.PaintConverter;
import javafx.css.converter.SizeConverter;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.javafx.FontIconConverter;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public class IconButton extends Button {
    private NullCodeFontIcon icon = new NullCodeFontIcon();

    boolean iconOnly = true;

    public IconButton() {
        this.setGraphic(icon);
        setIconOnly(true);
    }

    public boolean isIconOnly() {
        return iconOnly;
    }

    public void setIconOnly(boolean iconOnly) {
        this.iconOnly = iconOnly;
        if(iconOnly) {
            this.setText("");
            this.getStyleClass().add("invisible");
        } else {
            this.getStyleClass().remove("invisible");
        }
    }

    public String getIconLiteral() {
        return icon.getIconLiteral();
    }

    public void setIconLiteral(String iconLiteral) {
        icon.setIconLiteral(iconLiteral);
    }

    public String getTooltipText() {
        if(this.getTooltip() != null) {
            return this.getTooltip().getText();
        }
        return "";
    }

    public void setTooltipText(String text) {
        if(this.getTooltip() != null) {
            this.getTooltip().setText(text);
        } else {
            this.setTooltip(new Tooltip(text));
        }
    }

    public FontIcon getIcon() {
        return icon;
    }

    public void setIcon(NullCodeFontIcon icon) {
        this.icon = icon;
    }

    public int getIconSize() {
        return icon.getIconSize();
    }

    public void setIconSize(int size) {
        icon.setIconSize(size);
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    private static class StyleableProperties {

        private static final CssMetaData<IconButton, Number> ICON_SIZE =
                new CssMetaData<>("-fx-icon-size",
                        SizeConverter.getInstance(), 8) {

                    @Override
                    public boolean isSettable(IconButton node) {
                        return true;
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(IconButton btn) {
                        return (StyleableProperty<Number>) btn.icon.iconSizeProperty();
                    }
                };
        private static final CssMetaData<IconButton, Paint> ICON_COLOR =
                new CssMetaData<>("-fx-icon-color",
                        PaintConverter.getInstance(), Color.BLACK) {

                    @Override
                    public boolean isSettable(IconButton node) {
                        return true;
                    }

                    @Override
                    public StyleableProperty<Paint> getStyleableProperty(IconButton btn) {
                        return (StyleableProperty<Paint>) btn.icon.iconColorProperty();
                    }
                };

        private static final CssMetaData<IconButton, Ikon> ICON_CODE =
                new CssMetaData<>("-fx-icon-code",
                        FontIconConverter.getInstance(), null) {

                    @Override
                    public boolean isSettable(IconButton node) {
                        return true;
                    }

                    @Override
                    public StyleableProperty<Ikon> getStyleableProperty(IconButton btn) {
                        return (StyleableProperty<Ikon>) btn.icon.iconCodeProperty();
                    }
                };


        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(Button.getClassCssMetaData());
            styleables.add(ICON_SIZE);
            styleables.add(ICON_COLOR);
            styleables.add(ICON_CODE);
            STYLEABLES = unmodifiableList(styleables);
        }
    }
}
