package com.xatoxa.screenshot;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;

public class CopyImgToClipboard implements ClipboardOwner {
    private BufferedImage image;
    public CopyImgToClipboard(BufferedImage image) {
        this.image = image;
    }

    public void copy(){
        TransferableImage trans = new TransferableImage(this.image);
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        c.setContents( trans, this );
    }

    public void lostOwnership( Clipboard clip, Transferable trans ) {
        System.out.println("Lost Clipboard Ownership");
    }

    private static class TransferableImage implements Transferable {
        Image i;

        public TransferableImage( Image i ) {
            this.i = i;
        }

        public Object getTransferData( DataFlavor flavor ) throws UnsupportedFlavorException{
            if ( flavor.equals( DataFlavor.imageFlavor ) && i != null ) {
                return i;
            }
            else {
                throw new UnsupportedFlavorException( flavor );
            }
        }

        public DataFlavor[] getTransferDataFlavors() {
            DataFlavor[] flavors = new DataFlavor[ 1 ];
            flavors[ 0 ] = DataFlavor.imageFlavor;
            return flavors;
        }

        public boolean isDataFlavorSupported( DataFlavor flavor ) {
            DataFlavor[] flavors = getTransferDataFlavors();
            for (DataFlavor dataFlavor : flavors) {
                if (flavor.equals(dataFlavor)) {
                    return true;
                }
            }
            return false;
        }
    }
}