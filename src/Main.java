import ru.entel.ugp.gui.GUI;

import javax.swing.*;

/**
 * Created by Артем on 20.08.2015.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GUI();
            }
        });
    }
}
