package apps.content;

import java.io.Serial;
import java.io.Serializable;
import java.util.logging.Logger;

public class ContentData implements Serializable {
    @Serial
    private static final long serialVersionUID = 800853;
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public ContentData() {

    }
}
