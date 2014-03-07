/*
 * CSSNorm.java
 * Copyright (c) 2005-2007 Radek Burget
 *
 * CSSBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * CSSBox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *  
 * You should have received a copy of the GNU Lesser General Public License
 * along with CSSBox. If not, see <http://www.gnu.org/licenses/>.
 *
 * Created on 21. leden 2005, 22:28
 */

package org.fit.cssbox.css;

/**
 * This class provides standard style sheets for the browser.
 *
 * @author  radek
 */
public class CSSNorm 
{

    /**
     * Defines a standard HTML style sheet defining the basic style of the individual elements.
     * It corresponds to the CSS2.1 recommendation (Appendix D).
     * @return the style string
     */
    public static String stdStyleSheet()
    {
        return
        "html, address,"+
        "blockquote,"+
        "body, dd, div,"+
        "dl, dt, fieldset, form,"+
        "frame, frameset,"+
        "h1, h2, h3, h4,"+
        "h5, h6, noframes,"+
        "ol, p, ul, center,"+
        "dir, hr, menu, pre   { display: block }"+
        "li              { display: list-item }"+
        "head            { display: none }"+
        "table           { display: table }"+
        "tr              { display: table-row }"+
        "thead           { display: table-header-group }"+
        "tbody           { display: table-row-group }"+
        "tfoot           { display: table-footer-group }"+
        "col             { display: table-column }"+
        "colgroup        { display: table-column-group }"+
        "td, th          { display: table-cell; }"+
        "caption         { display: table-caption }"+
        "th              { font-weight: bolder; text-align: center }"+
        "caption         { text-align: center }"+
        "body            { margin: 8px; line-height: 1.12 }"+
        "h1              { font-size: 2em; margin: .67em 0 }"+
        "h2              { font-size: 1.5em; margin: .75em 0 }"+
        "h3              { font-size: 1.17em; margin: .83em 0 }"+
        "h4, p,"+
        /*"blockquote, ul,"+
        "fieldset, form,"+
        "ol, dl, dir,"+*/
        "menu            { margin: 1.12em 0 }"+
        "h5              { font-size: .83em; margin: 1.5em 0 }"+
        "h6              { font-size: .75em; margin: 1.67em 0 }"+
        "h1, h2, h3, h4,"+
        "h5, h6, b,"+
        "strong          { font-weight: bolder }"+
        "blockquote      { margin-left: 40px; margin-right: 40px }"+
        "i, cite, em,"+
        "var, address    { font-style: italic }"+
        "pre, tt, code,"+
        "kbd, samp       { font-family: monospace }"+
        "pre             { white-space: pre }"+
        "button, textarea,"+
        "input, object,"+
        "select          { display:inline-block; }"+
        "big             { font-size: 1.17em }"+
        "small, sub, sup { font-size: .83em }"+
        "sub             { vertical-align: sub }"+
        "sup             { vertical-align: super }"+
        "table           { border-spacing: 2px; }"+
        "thead, tbody,"+
        "tfoot           { vertical-align: middle }"+
        "tr, td, th      { vertical-align: inherit }"+
        "s, strike, del  { text-decoration: line-through }"+
        "hr              { border: 1px inset }"+
        "ol, ul, dir,"+
        "menu, dd        { margin-left: 40px }"+
        "ol              { list-style-type: decimal }"+
        "ol ul, ul ol,"+
        "ul ul, ol ol    { margin-top: 0; margin-bottom: 0 }"+
        "u, ins          { text-decoration: underline }"+
        //"br:before       { content: \"\\A\" }"+
        //":before, :after { white-space: pre-line }"+
        "center          { text-align: center }"+
        "abbr, acronym   { font-variant: small-caps; letter-spacing: 0.1em }"+
        ":link, :visited { text-decoration: underline }"+
        ":focus          { outline: thin dotted invert }"+

        "BDO[DIR=\"ltr\"]  { direction: ltr; unicode-bidi: bidi-override }"+
        "BDO[DIR=\"rtl\"]  { direction: rtl; unicode-bidi: bidi-override }"+

        "*[DIR=\"ltr\"]    { direction: ltr; unicode-bidi: embed }"+
        "*[DIR=\"rtl\"]    { direction: rtl; unicode-bidi: embed }";        
    }

    /**
     * A style sheet defining the additional basic style not covered by the CSS recommendation.
     * @return The style string
     */
    public static String userStyleSheet()
    {
        return
        "body   { color: black; background-color: #fafafa;}"+
        "a[href]{ color: blue; text-decoration: underline; }"+
        "script { display: none; }"+
        "style  { display: none; }"+
        "option { display: none; }"+
        "br     { display: block; }"+
        "hr     { display: block; margin-top: 1px solid; }"+
        
        //standard <ul> margin according to Mozilla
        "ul     { margin-left: 0; padding-left: 40px; }";
    }
    
    /**
     * A style sheet defining a basic style of form fields. This style sheet may be used
     * for a simple rendering of form fields when their functionality is not implemented
     * in another way.
     * @return The style string
     */
    public static String formsStyleSheet()
    {
        return
            "input, textarea, select { "+
            "  font-size: 80%;"+
            "  color: black;"+
            "  white-space: pre;"+
            "}"+
            "input[type='submit'],"+
            "input[type='reset'],"+
            "input[type='button'] {"+
            "  display: inline-block;"+
            "  box-sizing: border-box;"+
            "  border-right: 1px solid black;"+
            "  border-bottom: 1px solid black;"+
            "  border-top: 1px solid white;"+
            "  border-left: 1px solid white;"+
            "  background-color: #ddd;"+
            "  padding: 0 0.5em;"+
            "}"+
            "input[type='submit']:before,"+
            "input[type='reset']:before,"+
            "input[type='button']:before {"+
            "  display: block;"+
            "  text-align: center;"+
            "  content: attr(value);"+
            "}"+
            "input[type='radio'],"+
            "input[type='checkbox'] {"+
            "  display: inline-block;"+
            "  border: 1px solid black;"+
            "  background-color: white;"+
            "  width: 6px;"+
            "  height: 10px;"+
            "  line-height: 9px;"+
            "  font-size: 10px;"+
            "  padding: 0 2px;"+
            "}"+
            "input[type='radio']:before,"+
            "input[type='checkbox']:before {"+
            "  content: ' ';"+
            "}"+
            "input[checked]:before {"+
            "  content: 'x';"+
            "}"+
            "input[type='text'],"+
            "input[type='password'],"+
            "textarea,"+
            "select {"+
            "  display: inline-block;"+
            "  border-right: 1px solid #eee;"+
            "  border-bottom: 1px solid #eee;"+
            "  border-top: 1px solid black;"+
            "  border-left: 1px solid black;"+
            "  background-color: #fff;"+
            "  width: 15em;"+
            "  overflow: hidden;"+
            "  padding: 0;"+
            "}"+
            "input[type='text']:before,"+
            "input[type='password']:before {"+
            "  content: attr(value);"+
            "}"+
            "input[type='hidden'] {"+
            "  display: none;"+
            "}"+
            "textarea {"+
            "  height: 2em;"+
            "  width: 15em;"+
            "  white-space: normal;"+
            "}"+
            "select {"+
            "  white-space: normal;"+
            "}"+
            "select option {"+
            "  display: none;"+
            "}"+
            "select option:nth-of-type(1) {"+
            "  display: block;"+
            "}"+
            "select[size] option:nth-of-type(2),"+
            "select[size] option:nth-of-type(3) {"+
            "  display: block;"+
            "}"+
            "select[size='1'] option:nth-of-type(2),"+
            "select[size='1'] option:nth-of-type(3) {"+
            "  display: none;"+
            "}"+
            "select[size='2'] option:nth-of-type(3) {"+
            "  display: none;"+
            "}";
    }
    
}
