package net.treset.minecraftlauncher.ui.generic;

import javafx.beans.property.ObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.StyleOrigin;
import javafx.css.StyleableObjectProperty;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

public class NullCodeFontIcon extends FontIcon {
    private StyleableObjectProperty<Ikon> iconCode;

    @Override
    public ObjectProperty<Ikon> iconCodeProperty() {
        if (iconCode == null) {
            iconCode = new StyleableObjectProperty<>() {
                @Override
                public CssMetaData getCssMetaData() {
                    return getClassCssMetaData().get(2);
                }

                @Override
                public Object getBean() {
                    return this;
                }

                @Override
                public String getName() {
                    return "iconCode";
                }

                @Override
                public StyleOrigin getStyleOrigin() {
                    return StyleOrigin.USER_AGENT;
                }
            };

            iconCode.addListener((v, o, n) -> {
                if (!iconCode.isBound() && n != null) {
                    setIconCode(n);
                }
            });
        }
        return iconCode;
    }
}
