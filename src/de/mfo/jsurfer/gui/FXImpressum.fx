/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.mfo.jsurfer.gui;

/**
 * @author Panda
 */

public class FXImpressum  extends javafx.scene.CustomNode
{
    public var height:Number;
    public var width:Number;
    public var language: java.util.Locale;
    //public var pressed:function():Void;
    //var gallery:de.mfo.jsurfer.gui.Gallery=de.mfo.jsurfer.gui.Gallery{};
    //var image:javafx.scene.image.ImageView[]=[];
    public override function create(): javafx.scene.Node
    {
        //onMouseClicked=function(e: javafx.scene.input.MouseEvent): Void{}
        return javafx.scene.Group
        {
            content:
            [
                javafx.scene.image.ImageView
                {
                   image : javafx.scene.image.Image {
                    url: "{__DIR__}Impressum_de.png"}

                    fitWidth:bind width
                    fitHeight:bind height
                    preserveRatio: true
                    visible:bind (java.util.Locale.GERMAN==language)
                },
                javafx.scene.image.ImageView
                {
                   image : javafx.scene.image.Image {
                    url: "{__DIR__}Impressum_en.png"}

                    fitWidth:bind width
                    fitHeight:bind height
                    preserveRatio: true
                    visible:bind (java.util.Locale.ENGLISH==language)
                },
                javafx.scene.image.ImageView
                {
                   image : javafx.scene.image.Image {
                    url: "{__DIR__}Impressum_po.png"}

                    fitWidth:bind width
                    fitHeight:bind height
                    preserveRatio: true
                    visible:bind (new java.util.Locale("po")==language)
                },
                javafx.scene.image.ImageView
                {
                   image : javafx.scene.image.Image {
                    url: "{__DIR__}Impressum_ru.png"}

                    fitWidth:bind width
                    fitHeight:bind height
                    preserveRatio: true
                    visible:bind (new java.util.Locale("ru")==language)
                },
                javafx.scene.image.ImageView
                {
                   image : javafx.scene.image.Image {
                    url: "{__DIR__}Impressum_sr.png"}

                    fitWidth:bind width
                    fitHeight:bind height
                    preserveRatio: true
                    visible:bind (new java.util.Locale("sr")==language)
                }
            ]
        }
    }
    

}
