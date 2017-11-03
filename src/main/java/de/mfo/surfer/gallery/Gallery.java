package de.mfo.surfer.gallery;

import de.mfo.surfer.util.ThumbnailGenerator;
import de.mfo.surfer.util.Utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.InvalidationListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Gallery
{
    public enum Type
    {
        TUTORIAL, FANTASY, RECORD;
    }

    private static class TitleAndPageNumber
    {
        public String title;
        public int pageNumber;

        public TitleAndPageNumber( String title, int pageNumber )
        {
            this.title = title;
            this.pageNumber = pageNumber;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger( Gallery.class );

    private static PDDocument pdfDocument;
    private static PDFRenderer pdfRenderer;

    private static Set< Locale > availableLocales;
    private static HashMap< Type, HashMap< String, HashMap< Locale, TitleAndPageNumber > > > allGalleryEntries;

    private Type type;
    private List< GalleryItem > galleryItems;
    private ReadOnlyObjectProperty< Locale > locale;

    static {
        pdfDocument = Utils.wrapInRte( () -> PDDocument.load( Gallery.class.getResourceAsStream( "Surfer-Galleries.pdf" ) ) );
        pdfRenderer = new PDFRenderer( pdfDocument );

        availableLocales = new HashSet<>();
        allGalleryEntries = new HashMap<>( Type.values().length );

        // locale loop
        for( PDOutlineItem localeItem : Gallery.pdfDocument.getDocumentCatalog().getDocumentOutline().children() )
        {
            Locale locale = Locale.forLanguageTag( localeItem.getTitle().replaceAll( "_", "-" ) );
            availableLocales.add( locale );
            for( PDOutlineItem typeItem : localeItem.children() )  // type loop
            {
                Type type = Type.valueOf( typeItem.getTitle().toUpperCase() );
                int index = 0;
                for( PDOutlineItem idParentItem : typeItem.children() ) // id loop
                {
                    String id = (index++) + " " + idParentItem.getFirstChild().getTitle();

                    HashMap< String, HashMap< Locale, TitleAndPageNumber > > typeHM = allGalleryEntries.get( type );
                    if( typeHM == null )
                    {
                        typeHM = new LinkedHashMap<>();
                        allGalleryEntries.put( type, typeHM );
                    }

                    HashMap< Locale, TitleAndPageNumber > idHM = typeHM.get( id );
                    if( idHM == null )
                    {
                        idHM = new HashMap<>();
                        typeHM.put( id, idHM );
                    }

                    String title = idParentItem.getTitle();
                    int pdfPageNumber = Utils.wrapInRte( () -> ( ( PDPageDestination ) idParentItem.getFirstChild().getDestination() ) ).retrievePageNumber();
                    idHM.put( locale, new TitleAndPageNumber( title, pdfPageNumber ) );
                }
            }
        }

        availableLocales = Collections.unmodifiableSet( availableLocales );
    }

    public static Set< Locale > getAvailableLocales()
    {
        return availableLocales;
    }

    public static Image getGalleryInfoPageRendering( int pdfPageNumber, int maxWidth, int maxHeight )
    {
        PDRectangle cropBox = pdfDocument.getPage( pdfPageNumber ).getCropBox();

        float scale_x = maxWidth / cropBox.getWidth();
        float scale_y = maxHeight / cropBox.getHeight();
        float scale = Math.min(scale_x, scale_y);

        return Utils.wrapInRte( () -> {
            try {
                BufferedImage bi = new BufferedImage( maxWidth, maxHeight, BufferedImage.TYPE_INT_RGB );
                pdfRenderer.renderPageToGraphics( pdfPageNumber, (Graphics2D) bi.getGraphics(), scale );
                return SwingFXUtils.toFXImage(pdfRenderer.renderImage(pdfPageNumber, scale), null);
            } catch (IllegalArgumentException iae) {
                return SwingFXUtils.toFXImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), null);
            }
        } );
    }

    public Gallery( ReadOnlyObjectProperty< Locale > locale, Type type )
    {
        this.type = type;
        this.locale = locale;

        LinkedList< GalleryItem > tmpGalleryItems = new LinkedList<>();
        for( HashMap.Entry< String, HashMap< Locale, TitleAndPageNumber > > thisGalleryEntries : allGalleryEntries.get( type ).entrySet() )
        {
            String[] parts = thisGalleryEntries.getKey().split( " ", 2 );
            tmpGalleryItems.add( new GalleryItemImpl( parts[ 1 ], thisGalleryEntries.getValue(), parts[ 0 ].equals( "0" ) ) );
        }
        this.galleryItems = Collections.unmodifiableList( tmpGalleryItems );
    }

    public Type getType()
    {
        return this.type;
    }

    public List< GalleryItem > getGalleryItems()
    {
        return this.galleryItems;
    }

    private class GalleryItemImpl implements GalleryItem
    {
        private String idInGallery;
        private HashMap< Locale, TitleAndPageNumber > localizationMap;
        private boolean isFirst;

        private SimpleIntegerProperty pdfPageNumber;
        private SimpleStringProperty title;
        private URL jsurfURL;
        private Image thumbnailImage;
        private LinkedList<InvalidationListener> invalidationListeners;

        public GalleryItemImpl( String idInGallery, HashMap< Locale, TitleAndPageNumber > localizationMap, boolean isFirst )
        {
            this.idInGallery = idInGallery;
            this.localizationMap = localizationMap;
            this.isFirst = isFirst;

            this.pdfPageNumber = new SimpleIntegerProperty();
            this.title = new SimpleStringProperty();
            this.jsurfURL = getClass().getResource( idInGallery + ".jsurf" );

            this.invalidationListeners = new LinkedList<>();

            Gallery.this.locale.addListener( ( o, ov, nv ) -> updateProperties( nv ) );
            updateProperties( Gallery.this.locale.get() );
        }

        private void updateProperties( Locale locale )
        {
            TitleAndPageNumber tapn = localizationMap.get( locale );
            pdfPageNumber.setValue( tapn.pageNumber );
            title.setValue( tapn.title );
            invalidationListeners.forEach( l -> l.invalidated( this ) );
        }

        public boolean isFirst() {
            return isFirst;
        }

        public ReadOnlyStringProperty titleProperty()
        {
            return title;
        }

        public String getTitle()
        {
            return title.get();
        }

        public URL getJsurfURL()
        {
            return jsurfURL;
        }

        public Image getThumbnailImage()
        {
            if( thumbnailImage == null )
                thumbnailImage = ThumbnailGenerator.getImage( getJsurfURL() );
            return thumbnailImage;
        }

        public Image getInfoPageRendering( int maxWidth, int maxHeight )
        {
            return Gallery.getGalleryInfoPageRendering( pdfPageNumber.get(), maxWidth, maxHeight );
        }

        @Override
        public void addListener(InvalidationListener listener) {
            invalidationListeners.add( listener );
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            invalidationListeners.remove( listener );
        }
    }
}