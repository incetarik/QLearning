import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

interface Function<T> {
    void run(T e);
}

public class MouseClickEvent implements MouseListener {
    private Function<MouseEvent> eventFunction;

    public MouseClickEvent(Function<MouseEvent> eventFunction) {
        this.eventFunction = eventFunction;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        eventFunction.run(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
