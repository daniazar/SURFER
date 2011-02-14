/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.mfo.jsurfer.gui;

import javafx.scene.CustomNode;
import de.mfo.jsurfer.rendering.*;
import javax.vecmath.*;
//import java.awt.*;
import javafx.scene.Group;
import javafx.ext.swing.*;
import javafx.scene.Node;
import de.mfo.jsurfer.parser.*;
import de.mfo.jsurfer.algebra.*;
//import java.io.IOException;
import java.lang.System;
import de.mfo.jsurfer.algebra.*;
import javafx.scene.layout.LayoutInfo;
//import javax.swing.event.*;
import java.awt.event.*;

/**
 * @author Panda
 */

public class FXSurferPanel extends CustomNode {

    var renderer: JSurferRenderPanel=new JSurferRenderPanel() ;
    public var frontColor: Color3f on replace
    {
        var frontMaterial: Material=renderer.getAlgebraicSurfaceRenderer().getFrontMaterial();
        frontMaterial.setColor( new Color3f( frontColor ) );
        renderer.getAlgebraicSurfaceRenderer().setFrontMaterial(frontMaterial );
        renderer.repaintImage();
        //System.out.println("Front has changed");
    };

    public var backColor: Color3f on replace
    {
        var backMaterial: Material=renderer.getAlgebraicSurfaceRenderer().getBackMaterial();
        backMaterial.setColor( new Color3f( backColor ) );
        renderer.getAlgebraicSurfaceRenderer().setBackMaterial(backMaterial );
        renderer.repaintImage();
        //System.out.println("Back has changed");
    };

    public var x: Number;
//setRenderSize
    public var y: Number;

    public var width: Number/* on replace oldValue
    {
        var d:Dimension=new Dimension(width, height);
        renderer.setRenderSize(d);
        renderer.setSize(d);
    }/**/;
    


    public var height: Number/* on replace oldValue
    {
        var d:Dimension=new Dimension(width, height);
        renderer.setRenderSize(d);
        renderer.setSize(d);
    }/**/;
    public function setScale(n:Number)
    {
        renderer.setScale( n );
        scale=renderer.publicScaleFactor;
        //System.out.println("SetScale: {scale}, bindinghack.value{bindinghack.value}");
        //renderer.repaintImage();
    }

    public var scale:Number=renderer.publicScaleFactor on replace
    {
        renderer.setScale( scale );
        renderer.repaintImage();
        //System.out.println("Scale has changed: {scale}");
    };

    function initMaterials()
    {
        // init front material
        var frontMaterial:Material=new Material() ;
        //frontMaterial = new Material();
        frontMaterial.setColor( frontColor );
        frontMaterial.setAmbientIntensity( 0.4 );
        frontMaterial.setDiffuseIntensity( 0.8 );
        frontMaterial.setSpecularIntensity( 0.5 );
        frontMaterial.setShininess( 30 );
        renderer.getAlgebraicSurfaceRenderer().setFrontMaterial( frontMaterial );


        //frontColorPicker.setRGB( cf.getRed(), cf.getGreen(), cf.getBlue() );

        // init back material
        var backMaterial:Material=new Material();
        backMaterial.setColor( backColor );
        backMaterial.setAmbientIntensity( 0.4 );
        backMaterial.setDiffuseIntensity( 0.8 );
        backMaterial.setSpecularIntensity( 0.5 );
        backMaterial.setShininess( 30 );
        renderer.getAlgebraicSurfaceRenderer().setBackMaterial( backMaterial );

    }

    function initLights()
    {
        //var lights = [];
        //LightSource[] lights = new LightSource[ AlgebraicSurfaceRenderer.MAX_LIGHTS ];

        var light0:LightSource=new LightSource();
        light0.setPosition( new Point3f( -100, 100, 100 ) );
        light0.setIntensity( 0.5 );
        light0.setColor( new Color3f( 1, 1, 1 ) );

        var light1:LightSource=new LightSource();
        light1.setPosition( new Point3f( 100, 100, 100 ) );
        light1.setIntensity( 0.7 );
        light1.setColor( new Color3f( 1, 1, 1 ) );

        var light2:LightSource=new LightSource();
        light2.setPosition( new Point3f( 0, -100, 100 ) );
        light2.setIntensity( 0.3 );
        light2.setColor( new Color3f( 1, 1, 1 ) );

        renderer.getAlgebraicSurfaceRenderer().setLightSource(0,light0);
        renderer.getAlgebraicSurfaceRenderer().setLightSource(1,light1);
        renderer.getAlgebraicSurfaceRenderer().setLightSource(2,light2);
    }

    function getRenderer(): JSurferRenderPanel{
        initLights();
        initMaterials();
        renderer.setScale( 0.5 );
        /*void mouseWheelMoved(MouseWheelEvent e)
        {
            
        }*/

        renderer.addMouseWheelListener(
        MouseWheelListener{
            override function mouseWheelMoved(e)
            {
                scale=renderer.publicScaleFactor;
            }
        }

        
        );
        /*renderer.publicScaleFactor.addChangeListener(
        
       ChangeListener {
            override function stateChanged(e)
                {
                    //color= new Color3f(colorPicker.getColor());
                   // System.out.println("color2Picker has changed");
                }
            }
        );*/
        return renderer;
    }

    public override function create(): Node {
        /*initMaterials();
        initLights();*/
        def sw:SwingComponent=SwingComponent.wrap(getRenderer());
        sw.layoutInfo=LayoutInfo{
            minWidth: bind height,
            width: bind width
            maxWidth: bind width
            minHeight: bind height
            height: bind height
            maxHeight: bind height
         };

        return Group {
                    translateX: bind x translateY: bind y;
                    content: [

                        SwingComponent.wrap(getRenderer())
                    ]
                }
    }

    public function surfaceExpressionChanged(expression:String):Boolean
    {
//        var P:AlgebraicExpressionParser= new AlgebraicExpressionParser;
        try
        {
/*
var p:PolynomialOperation;
           {
             var t1:PolynomialOperation=new PolynomialPower(new PolynomialVariable( PolynomialVariable.Var.valueOf( "x" ) ), 2 );
             var t2:PolynomialOperation=new PolynomialPower(new PolynomialVariable( PolynomialVariable.Var.valueOf( "y" ) ), 2 );
             var t3:PolynomialOperation=new PolynomialPower(new PolynomialVariable( PolynomialVariable.Var.valueOf( "z" ) ), 2 );
             var t4:PolynomialOperation=new PolynomialAddition(new PolynomialAddition(t1,t2),t3);
             var t5:PolynomialOperation=new PolynomialMultiplication(new PolynomialMultiplication(new DoubleValue( 2.0 ),new PolynomialVariable( PolynomialVariable.Var.valueOf( "x" ) )), new PolynomialMultiplication(new PolynomialVariable( PolynomialVariable.Var.valueOf( "y" ) ),new PolynomialVariable( PolynomialVariable.Var.valueOf( "z" ) )));
             var t6:PolynomialOperation=new PolynomialSubtraction(t5,new DoubleValue( 1.0 ));
             var t7:PolynomialOperation=new PolynomialAddition(t4,t6);
            //p.getAlgebraicSurfaceRenderer().setSurfaceExpression( t7 );
             p=t7;
           }
*/
           var p:PolynomialOperation = AlgebraicExpressionParser.parse( "x^2+y^2-1" );

           // current version does not support surface parameters
           /*if( p.accept( new DoubleVariableChecker(), ( Void ) null ) )
               throw new Exception();*/

               renderer.getAlgebraicSurfaceRenderer().setSurfaceExpression( p );
renderer.repaintImage();
           //surfaceExpression.setBackground( Color.WHITE );
        }
        catch( Exception )
        {
            System.out.println("falsch");
            return false;
            //surfaceExpression.setBackground( new Color( 255, 90, 90 ).brighter() );
        }
        //parserFeld.setText(P.a()+"\n 1 \n 2 \n 3");
        //errorFeld.setText("line "+ P.errorRow()+" : "+P.errorMessage());
        return true;
    }


}
