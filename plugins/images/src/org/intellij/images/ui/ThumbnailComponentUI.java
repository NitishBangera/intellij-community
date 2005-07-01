/** $Id$ */
package org.intellij.images.ui;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import org.intellij.images.editor.ImageDocument;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * UI for {@link ThumbnailComponent}.
 *
 * @author <a href="mailto:aefimov.box@gmail.com">Alexey Efimov</a>
 */
public class ThumbnailComponentUI extends ComponentUI {
    private static final Icon BLANK_ICON = IconLoader.getIcon("/org/intellij/images/icons/ThumbnailBlank.png");
    private static final Icon DIRECTORY_ICON = IconLoader.getIcon("/org/intellij/images/icons/ThumbnailDirectory.png");
    private static final Icon ERROR_ICON = Messages.getErrorIcon();
    private static final String DOTS = "...";

    private static final ThumbnailComponentUI ui = new ThumbnailComponentUI();

    static {
        UIManager.getDefaults().put("ThumbnailComponent.errorString", "Error");
    }


    public void paint(Graphics g, JComponent c) {
        ThumbnailComponent tc = (ThumbnailComponent)c;
        if (tc != null) {
            paintBackground(g, tc);

            if (tc.isDirectory()) {
                paintDirectory(g, tc);
            } else {
                paintImageThumbnail(g, tc);
            }

            // File name
            paintFileName(g, tc);
        }
    }

    private void paintDirectory(Graphics g, ThumbnailComponent tc) {
        // Paint directory icon
        DIRECTORY_ICON.paintIcon(tc, g, 5, 5);

        int imagesCount = tc.getImagesCount();
        if (imagesCount > 0) {
            String title = (imagesCount > 100 ? ">100" : "" + imagesCount) + " icons";

            Font font = getSmallFont();
            FontMetrics fontMetrics = g.getFontMetrics(font);
            g.setColor(Color.BLACK);
            g.setFont(font);
            g.drawString(title, 5 + (DIRECTORY_ICON.getIconWidth() - fontMetrics.stringWidth(title)) / 2, DIRECTORY_ICON.getIconHeight() / 2 + fontMetrics.getAscent());
        }
    }

    private void paintImageThumbnail(Graphics g, ThumbnailComponent tc) {
        // Paint blank
        BLANK_ICON.paintIcon(tc, g, 5, 5);

        ImageComponent imageComponent = tc.getImageComponent();
        ImageDocument document = imageComponent.getDocument();
        BufferedImage image = document.getValue();
        if (image != null) {
            paintImage(g, tc);
        } else {
            paintError(g, tc);
        }

        paintFileSize(g, tc);
    }

    private void paintBackground(Graphics g, ThumbnailComponent tc) {
        Dimension size = tc.getSize();
        g.setColor(tc.getBackground());
        g.fillRect(0, 0, size.width, size.height);
    }

    private void paintImage(Graphics g, ThumbnailComponent tc) {
        ImageComponent imageComponent = tc.getImageComponent();
        BufferedImage image = imageComponent.getDocument().getValue();

        int blankHeight = BLANK_ICON.getIconHeight();

        // Paint image info (and reduce height of text from available height)
        blankHeight -= paintImageCaps(g, image);
        // Paint image format (and reduce height of text from available height)
        blankHeight -= paintFormatText(tc, g);

        // Paint image
        paintThumbnail(g, imageComponent, blankHeight);
    }

    private int paintImageCaps(Graphics g, BufferedImage image) {
        String description = image.getWidth() + "x" + image.getHeight() + "x" + image.getColorModel().getPixelSize();

        Font font = getSmallFont();
        FontMetrics fontMetrics = g.getFontMetrics(font);
        g.setColor(Color.BLACK);
        g.setFont(font);
        g.drawString(description, 8, 8 + fontMetrics.getAscent());

        return fontMetrics.getHeight();
    }

    private int paintFormatText(ThumbnailComponent tc, Graphics g) {
        Font font = getSmallFont();
        FontMetrics fontMetrics = g.getFontMetrics(font);

        String format = tc.getFormat();
        int stringWidth = fontMetrics.stringWidth(format);
        g.drawString(
            format,
            BLANK_ICON.getIconWidth() - stringWidth - 3,
            BLANK_ICON.getIconHeight() + 2 - fontMetrics.getHeight() + fontMetrics.getAscent()
        );

        return fontMetrics.getHeight();
    }

    private void paintThumbnail(Graphics g, ImageComponent imageComponent, int blankHeight) {

        // Zoom image by available size
        int maxWidth = BLANK_ICON.getIconWidth() - 10;
        int maxHeight = blankHeight - 10;

        BufferedImage image = imageComponent.getDocument().getValue();
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        double proportion = (double)imageWidth / (double)imageHeight;

        if (imageWidth > maxWidth || imageHeight > maxHeight) {
            if (imageWidth > imageHeight) {
                if (imageWidth > maxWidth) {
                    imageWidth = maxWidth;
                    imageHeight = (int)(maxWidth / proportion);
                }
            } else {
                if (imageHeight > maxHeight) {
                    imageHeight = maxHeight;
                    imageWidth = (int)(maxHeight * proportion);
                }
            }
        }

        imageComponent.setCanvasSize(imageWidth, imageHeight);
        Dimension size = imageComponent.getSize();

        int x = 5 + (BLANK_ICON.getIconWidth() - size.width) / 2;
        int y = 5 + (BLANK_ICON.getIconHeight() - size.height) / 2;


        imageComponent.paint(g.create(x, y, size.width, size.height));
    }

    private void paintFileName(Graphics g, ThumbnailComponent tc) {
        Font font = getLabelFont();
        FontMetrics fontMetrics = g.getFontMetrics(font);

        g.setFont(font);
        g.setColor(tc.getForeground());

        String fileName = tc.getFileName();
        String title = fileName;
        while (fontMetrics.stringWidth(title) > BLANK_ICON.getIconWidth() - 8) {
            title = title.substring(0, title.length() - 1);
        }

        if (fileName.equals(title)) {
            // Center
            g.drawString(fileName, 6 + (BLANK_ICON.getIconWidth() - 2 - fontMetrics.stringWidth(title)) / 2, BLANK_ICON.getIconHeight() + 8 + fontMetrics.getAscent());
        } else {
            int dotsWidth = fontMetrics.stringWidth(DOTS);
            while (fontMetrics.stringWidth(title) > BLANK_ICON.getIconWidth() - 8 - dotsWidth) {
                title = title.substring(0, title.length() - 1);
            }
            g.drawString(title + DOTS, 6, BLANK_ICON.getIconHeight() + 8 + fontMetrics.getAscent());
        }
    }

    private void paintFileSize(Graphics g, ThumbnailComponent tc) {
        Font font = getSmallFont();
        FontMetrics fontMetrics = g.getFontMetrics(font);
        g.setColor(Color.BLACK);
        g.setFont(font);
        g.drawString(
            tc.getFileSizeText(),
            8,
            BLANK_ICON.getIconHeight() + 2 - fontMetrics.getHeight() + fontMetrics.getAscent()
        );
    }

    private void paintError(Graphics g, ThumbnailComponent tc) {
        Font font = getSmallFont();
        FontMetrics fontMetrics = g.getFontMetrics(font);

        ERROR_ICON.paintIcon(
            tc,
            g,
            5 + (BLANK_ICON.getIconWidth() - ERROR_ICON.getIconWidth()) / 2,
            5 + (BLANK_ICON.getIconHeight() - ERROR_ICON.getIconHeight()) / 2
        );

        // Error
        String error = UIManager.getString("ThumbnailComponent.errorString");
        g.setColor(Color.RED);
        g.setFont(font);
        g.drawString(error, 8, 8 + fontMetrics.getAscent());
    }

    private static Font getLabelFont() {
        return UIManager.getFont("Label.font");
    }

    private static Font getSmallFont() {
        Font labelFont = getLabelFont();
        return labelFont.deriveFont(labelFont.getSize2D() - 2.0f);
    }

    public Dimension getPreferredSize(JComponent c) {
        Font labelFont = getLabelFont();
        FontMetrics fontMetrics = c.getFontMetrics(labelFont);
        return new Dimension(
            BLANK_ICON.getIconWidth() + 10,
            BLANK_ICON.getIconHeight() + fontMetrics.getHeight() + 15
        );
    }

    public static ComponentUI createUI(JComponent c) {
        return ui;
    }
}

