package de.mfo.surfer;

import de.mfo.surfer.control.*;
import de.mfo.surfer.gallery.*;
import de.mfo.surfer.util.FXUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

import de.mfo.surfer.util.Utils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.When;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends Application
{
    private static final Logger logger = LoggerFactory.getLogger( Main.class );

    private static Group fxmlRoot;

    {
        try
        {
            fxmlRoot = ( Group ) new FXMLLoader().load(
                getClass().getResource( "/de/mfo/surfer/fxml/surfer_touchscreen_1920_x_1080.fxml" )
            );
        }
        catch( IOException ioe )
        {
            throw new RuntimeException( ioe );
        }
    }

    public static < N extends Node > N fxmlLookup( String id )
    {
        return ( N ) fxmlRoot.lookup( id );
    }

    public static void main( String[] args )
    {
        Application.launch( Main.class, args );
    }

    public static void handleUncaughtException( Thread thread, Throwable throwable )
    {
        if( Platform.isFxApplicationThread() )
        {
            logger.error( "Uncaught exception in JavaFX application thread", throwable );
            Platform.runLater(
                () ->
                new ExceptionDialog(
                    Alert.AlertType.ERROR,
                    "Uncaught exception in JavaFX application thread",
                    "Please report the details of the error to the developers this software.",
                    throwable
                ).showAndWait()
            );
        }
        else
        {
            throw new RuntimeException( throwable );
        }
    }

    private FormulaInputForm fif;
    private SceneNodeSliderPanel snsp;
    private RenderArea ra;
    private MiscSceneNodeButtonPanel msnbp;
    private ColorPickerPanel cpp;
    private GallerySelector gs;
    private TabPanel tp;
    private TilePane galleryIconContainer;
    private GalleryInfoPage galleryIntroPage;
    private GalleryInfoPage galleryInfoPage;

    @Override
    public void start( Stage stage ) throws Exception
    {
        Thread.currentThread().setUncaughtExceptionHandler( Main::handleUncaughtException );

        stage.setTitle( "SURFER" );

        try
        {
            Group scaledOverlay = new Group();
            scaledOverlay.setId( "scaled-overlay" );

            Group overlay = new Group();

            Scene scene = new Scene( new Group( fxmlRoot, overlay, scaledOverlay ), 192.0 * 6.0, 108.0 * 6.0 );
            scene.getStylesheets().add( Main.class.getResource( "/de/mfo/surfer/css/style.css" ).toExternalForm() );

            Scale scale = new Scale( 1.0, 1.0, 0.0, 0.0 );
            NumberBinding scaleValue = new When(
                    scene.widthProperty().multiply( 1080.0 ).greaterThan( scene.heightProperty().multiply( 1920.0 ) )
                )
                .then( scene.heightProperty().divide( 1080.0 ) )
                .otherwise( scene.widthProperty().divide( 1920.0 ) );
            scale.xProperty().bind( scaleValue );
            scale.yProperty().bind( scaleValue );

            fxmlRoot.getTransforms().add( scale );
            fxmlRoot.translateYProperty().bind( scene.heightProperty().subtract( scaleValue.multiply( 1080 ) ) );

            scaledOverlay.getTransforms().add( scale );
            scaledOverlay.translateYProperty().bind( scene.heightProperty().subtract( scaleValue.multiply( 1080 ) ) );

            fif = new FormulaInputForm();
            snsp = new SceneNodeSliderPanel();
            ra = new RenderArea();
            msnbp = new MiscSceneNodeButtonPanel(
                f ->
                {
                    logger.debug( "Open {}", f );
                    try
                    {
                        ra.load( f );
                    }
                    catch( Exception e )
                    {
                        new ExceptionDialog( Alert.AlertType.ERROR, "Error loading " + f.toString(), e ).showAndWait();
                    }
                },
                f ->
                {
                    try
                    {
                        ra.store( f );
                    }
                    catch( Exception e )
                    {
                        new ExceptionDialog( Alert.AlertType.ERROR, "Error saving to " + f.toString(), e ).showAndWait();
                    }
                },
                f ->
                {
                    try
                    {
                        ra.export( f );
                    }
                    catch( Exception e )
                    {
                        new ExceptionDialog( Alert.AlertType.ERROR, "Error exporting to " + f.toString(), e ).showAndWait();
                    }
                },
                printJob -> {
                        // TODO: render in a background thread and send the result to the print dialog when it is ready (maybe use a low resolution while rendering)
                        File tempFile = Utils.wrapInRte( () -> Files.createTempFile("SURFER-", ".png" ) ).toFile();
                        tempFile.deleteOnExit();
                        ra.export( tempFile, 512 );

                        java.util.Optional<java.util.function.Consumer<javafx.print.PrinterJob>>result = new PrintDialog( fif.getFormula(), tempFile.getAbsolutePath() ).showAndWait();
                        result.ifPresent( printJobConsumer -> printJobConsumer.accept(printJob) );
                    }
            );
            cpp = new ColorPickerPanel();

            galleryIconContainer = new javafx.scene.layout.TilePane();
            galleryIconContainer.getStyleClass().add( "galleryIconContainer" );
            FXUtils.resizeRelocateTo( galleryIconContainer, fxmlLookup( "#Gallery_Select" ) );

            ImageView galleryIntroCanvas = new ImageView();
            galleryIntroPage = new GalleryInfoPage( galleryIntroCanvas );
            FXUtils.resizeRelocateTo( galleryIntroPage, fxmlLookup( "#Gallery_Text" ) );

            ImageView galleryInfoCanvas = new ImageView();
            galleryInfoPage = new GalleryInfoPage( galleryInfoCanvas );

            gs = new GallerySelector(
                galleryIconContainer.getChildren(),
                galleryIntroPage,
                galleryInfoPage,
                ra,
                this
            );
            tp = new TabPanel(
                gs,
                galleryInfoPage,
                cpp
            );
            tp.activeTabIndexProperty().addListener( ( observable, oldValue, newValue ) -> {
                switch( newValue.intValue() ) {
                    case 0: setMode( Mode.GALLERY ); break;
                    case 1: setMode( Mode.INFO ); break;
                    case 2: setMode( Mode.COLORS ); break;
                }
            } );

            ra.load( Main.class.getResource( "/de/mfo/surfer/gallery/default.jsurf" ) );

            fif.formulaProperty().bindBidirectional( ra.formulaProperty() );
            fif.isValidProperty().bind( ra.isValidProperty() );
            fif.errorMessageProperty().bind( ra.errorMessageProperty() );
            snsp.parametersProperty().bindBidirectional( ra.parametersProperty() );
            cpp.frontColorProperty().bindBidirectional( ra.frontColorProperty() );
            cpp.backColorProperty().bindBidirectional( ra.backColorProperty() );

            scaledOverlay.getChildren().add( fif );
            scaledOverlay.getChildren().add( msnbp );
            scaledOverlay.getChildren().add( snsp );
            scaledOverlay.getChildren().add( tp );
            scaledOverlay.getChildren().add( galleryIntroPage );
            scaledOverlay.getChildren().add( galleryIconContainer );

            overlay.getChildren().add( galleryIntroCanvas );
            overlay.getChildren().add( galleryInfoCanvas );
            overlay.getChildren().add( ra );

            gs.selectGallery( Gallery.Type.values()[ 0 ] );
            setMode( Mode.COLORS );

            stage.setScene( scene );
            stage.show();
        }
        catch( Throwable t )
        {
            handleUncaughtException( Thread.currentThread(), t );
        }
    }

    public enum Mode
    {
        RENDERING, GALLERY, INFO, COLORS;
    }

    public void setMode( Mode mode )
    {
        Consumer< Boolean > disableButtons = disable -> msnbp.setDisable( disable,
            MiscSceneNodeButtonPanel.ButtonType.OPEN,
            MiscSceneNodeButtonPanel.ButtonType.SAVE,
            MiscSceneNodeButtonPanel.ButtonType.EXPORT,
            MiscSceneNodeButtonPanel.ButtonType.PRINT
        );

        switch( mode )
        {
            case RENDERING:
                ra.setVisible( true );
                snsp.setVisible( true );
                galleryIconContainer.setVisible( false );
                galleryIntroPage.setVisible( false );
                disableButtons.accept( false );
                fif.setDisable( false );
                break;
            case GALLERY:
                ra.setVisible( false );
                snsp.setVisible( false );
                galleryIconContainer.setVisible( true );
                galleryIntroPage.setVisible( true );
                disableButtons.accept( true );
                fif.setDisable( true );
                tp.setActiveTabIndex( 0 );
                break;
            case INFO:
                ra.setVisible( true );
                snsp.setVisible( true );
                galleryIconContainer.setVisible( false );
                galleryIntroPage.setVisible( false );
                disableButtons.accept( false );
                fif.setDisable( false );
                tp.setActiveTabIndex( 1 );
                break;
            case COLORS:
                ra.setVisible( true );
                snsp.setVisible( true );
                galleryIconContainer.setVisible( false );
                galleryIntroPage.setVisible( false );
                disableButtons.accept( false );
                fif.setDisable( false );
                tp.setActiveTabIndex( 2 );
                break;
        }
    }
}
