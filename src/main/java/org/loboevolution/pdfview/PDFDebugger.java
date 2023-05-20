package org.loboevolution.pdfview;

import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

/**
 * <p>PDFDebugger class.</p>
 *
  *
  *
 */
public class PDFDebugger {
	
	private static final Logger logger = Logger.getLogger(PDFDebugger.class.getName());
    /** Constant <code>DEBUG_DCTDECODE_DATA="debugdctdecode"</code> */
    public static final  String DEBUG_DCTDECODE_DATA = "debugdctdecode";
    /** Constant <code>DEBUG_TEXT=false</code> */
    public static final boolean DEBUG_TEXT = false;
    /** Constant <code>DEBUG_IMAGES=false</code> */
    public static final boolean DEBUG_IMAGES = false;
    /** Constant <code>DEBUG_OPERATORS=false</code> */
    public static final boolean DEBUG_OPERATORS = false;
    /** Constant <code>DEBUG_PATH=false</code> */
    public static final boolean DEBUG_PATH = false;
    /** Constant <code>DEBUG_STOP_AT_INDEX=0</code> */
    public static final int DEBUG_STOP_AT_INDEX = 0;
    /** Constant <code>DISABLE_TEXT=false</code> */
    public static final boolean DISABLE_TEXT = false;
    /** Constant <code>DISABLE_IMAGES=false</code> */
    public static final boolean DISABLE_IMAGES = false;
    /** Constant <code>DISABLE_PATH_STROKE=false</code> */
    public static final boolean DISABLE_PATH_STROKE = false;
    /** Constant <code>DISABLE_PATH_FILL=false</code> */
    public static final boolean DISABLE_PATH_FILL = false;
    /** Constant <code>DISABLE_PATH_STROKE_FILL=false</code> */
    public static final boolean DISABLE_PATH_STROKE_FILL = false;
    /** Constant <code>DISABLE_CLIP=false</code> */
    public static final boolean DISABLE_CLIP = false;
    /** Constant <code>DISABLE_FORMS=false</code> */
    public static final boolean DISABLE_FORMS = false;
    /** Constant <code>DISABLE_SHADER=false</code> */
    public static final boolean DISABLE_SHADER = false;
    /** Constant <code>SHOW_TEXT_REGIONS=false</code> */
    public static final boolean SHOW_TEXT_REGIONS = false;
    /** Constant <code>SHOW_TEXT_ANCHOR=false</code> */
    public static final boolean SHOW_TEXT_ANCHOR = false;
    /** Constant <code>DISABLE_THUMBNAILS=false</code> */
    public static final boolean DISABLE_THUMBNAILS = false;
    /** Constant <code>DRAW_DELAY=0</code> */
    public static final long DRAW_DELAY = 0;

    /** Constant <code>debuglevel=4000</code> */
    public static int debuglevel = 4000;

    @SuppressWarnings("serial")
    public static class DebugStopException extends Exception {
        // nothing to do
    }

    /**
     * <p>debugImage.</p>
     *
     * @param image a {@link java.awt.image.BufferedImage} object.
     * @param name a {@link java.lang.String} object.
     */
    public static void debugImage(BufferedImage image, String name) {
        if (PDFDebugger.DEBUG_IMAGES) {
            if (image == null) {
                return;
            }
            try {
                // retrieve image
                File outputfile = new File("D:/tmp/PDFimages/" + name + ".png");
                ImageIO.write(image, "png", outputfile);
            } catch (IOException e) {
                BaseWatchable.getErrorHandler().publishException(e);
            }
        }
    }

    // TODO: add debug level and print it? 
    /**
     * <p>debug.</p>
     *
     * @param msg a {@link java.lang.String} object.
     * @param level a int.
     */
    public static void debug(String msg, int level) {
        if (level > debuglevel) {
            logger.info(escape(msg));
        }
    }

    // TODO: add debug level and print it? 
    /**
     * <p>debug.</p>
     *
     * @param msg a {@link java.lang.String} object.
     */
    public static void debug(String msg) {
        debug(msg, debuglevel);
    }

    /**
     * <p>escape.</p>
     *
     * @param msg a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String escape(String msg) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < msg.length(); i++) {
            char c = msg.charAt(i);
            if (c != '\n' && (c < 32 || c >= 127)) {
                c = '?';
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * <p>setDebugLevel.</p>
     *
     * @param level a int.
     */
    public static void setDebugLevel(int level) {
        debuglevel = level;
    }


    /**
     * <p>dumpStream.</p>
     *
     * @param stream an array of {@link byte} objects.
     * @return a {@link java.lang.String} object.
     */
    public static String dumpStream(byte[] stream) {
        return PDFDebugger.escape(new String(stream).replace('\r', '\n'));
    }

    /**
     * <p>logPath.</p>
     *
     * @param path a {@link java.awt.geom.GeneralPath} object.
     * @param operation a {@link java.lang.String} object.
     */
    public static void logPath(GeneralPath path, String operation) {
        if (PDFDebugger.DEBUG_PATH) {
            if (operation != null) {
                logger.info("Operation: " + operation + "; ");
            }
            logger.info("Current path: ");
            Rectangle b = path.getBounds();
            if (b != null)
                logger.info("        Bounds [x=" + b.x + ",y=" + b.y + ",width=" + b.width + ",height=" + b.height + "]");
            Point2D p = path.getCurrentPoint();
            if (p != null)
                logger.info("        Point  [x=" + p.getX() + ",y=" + p.getY() + "]");
        }
    }

    /**
     * take a byte array and write a temporary file with it's data.
     * This is intended to capture data for analysis, like after decoders.
     *
     * @param ary an array of {@link byte} objects.
     * @param name a {@link java.lang.String} object.
     */
    public static void emitDataFile(byte[] ary, String name) {
        FileOutputStream ostr;
        try {
            File file = File.createTempFile("DateFile", name);
            ostr = new FileOutputStream(file);
            PDFDebugger.debug("Write: " + file.getPath());
            ostr.write(ary);
            ostr.close();
        } catch (IOException ex) {
            // ignore
        }
    }

    /**
     * <p>dump.</p>
     *
     * @param obj a {@link org.loboevolution.pdfview.PDFObject} object.
     * @throws java.io.IOException if any.
     */
    public static void dump(PDFObject obj) throws IOException {
        PDFDebugger.debug("dumping PDF object: " + obj);
        if (obj == null) {
            return;
        }
        Map<String, PDFObject> dict = obj.getDictionary();
        PDFDebugger.debug("   dict = " + dict);
        for (Object key : dict.keySet()) {
            PDFDebugger.debug("key = " + key + " value = " + dict.get(key));
        }
    }

}
