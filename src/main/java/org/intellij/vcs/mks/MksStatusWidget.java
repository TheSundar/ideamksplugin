package org.intellij.vcs.mks;

import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;

public class MksStatusWidget implements StatusBarWidget {
    private String currentStatus = "";
    private static final String MAX = "0123456789" + "0123456789" + "0123456789" + "0123456789" + "0123456789"
            + "0123456789" + "0123456789" + "0123456789" + "0123456789" + "0123456789";
    private final TextPresentation presentation = new StatusPresentation();

    public void setText(String message) {
        currentStatus = message;
    }

    class StatusPresentation implements TextPresentation {
        @Override
        public float getAlignment() {
            return -1;
        }

        @NotNull
        @Override
        public String getText() {
            return MksStatusWidget.this.currentStatus;
        }

        @NotNull
        @Override
        public String getMaxPossibleText() {
            return MAX;
        }

        @Override
        public String getTooltipText() {
            return null;
        }

        @Override
        public Consumer<MouseEvent> getClickConsumer() {
            return null;
        }
    }

    @Override
    public WidgetPresentation getPresentation(@NotNull Type type) {
        return presentation;
    }

    @NotNull
    @Override
    public String ID() {
        return "MKS_Status";
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
    }

    @Override
    public void dispose() {
    }
}
