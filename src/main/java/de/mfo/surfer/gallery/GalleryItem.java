package de.mfo.surfer.gallery;

import de.mfo.surfer.control.GalleryIcon;
import de.mfo.surfer.control.GalleryInfoPage;
import java.net.URL;

import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyStringProperty;

public interface GalleryItem extends Observable
{
    public ReadOnlyStringProperty titleProperty();
    public URL getJsurfURL();
    public Image getThumbnailImage();
    public GalleryIcon getIcon();
    public Image getInfoPageRendering( Bounds boundingBox );
}
