<?
		require "../include/page.php";
		make_header("documentation", "Manual", "../", "@import \"manual.css\";");
		?><h1 xmlns="http://www.w3.org/1999/xhtml" xmlns:doc="http://cssbox.sourceforge.net/docs">CSSBox Manual</h1><p xmlns="http://www.w3.org/1999/xhtml" xmlns:doc="http://cssbox.sourceforge.net/docs" class="author">
			[ <a href="manual.html">Downloadable version</a> ]
		</p><div xmlns="http://www.w3.org/1999/xhtml" xmlns:doc="http://cssbox.sourceforge.net/docs" class="toc"><h2>Table of Contents</h2><ul><li><a href="#intro">Introduction</a></li><li><a href="#basic">Basic usage</a><ul><li><a href="#basicLoading">Document Loading</a></li><li><a href="#media">Media Support</a></li><li><a href="#basicLayout">Obtaining the Layout</a></li><li><a href="#basicDisplay">Displaying the document</a></li><li><a href="#configOptions">Configuration Options</a></li><li><a href="#parserCustomization">Custom Document Sources and Parsers</a></li></ul></li><li><a href="#model">Rendered Document Model</a><ul><li><a href="#modelBox">Basic Box Properties</a></li><li><a href="#modelText">Text Boxes</a></li><li><a href="#modelElement">Element Boxes</a></li><li><a href="#modelInline">Inline Boxes</a></li><li><a href="#modelBlock">Block Boxes</a></li><li><a href="#modelColors">Fonts and Colors</a></li></ul></li><li><a href="#feedback">Feedback</a></li></ul></div><div xmlns="http://www.w3.org/1999/xhtml" xmlns:doc="http://cssbox.sourceforge.net/docs" class="section" id="intro"><h2>Introduction</h2><p>CSSBox is an (X)HTML/CSS rendering engine written in pure Java. Its 
primary purpose is to provide a complete and further processable information 
about the rendered page contents and layout.</p><p>This manual gives a short overview of the CSSBox usage. It shows how to
render a document and it explains how the resulting page is represented and
how the basic information about the individual parts can be obtained.</p><p>More detailed information about the individual classes can be obtained
from the <a href="../api/index.html" class="api">API documentation</a>.</p><p>Any feedback to CSSBox and/or this manual is welcome via the
<a href="http://cssbox.sourceforge.net/">CSSBox website</a>.</p></div><div xmlns="http://www.w3.org/1999/xhtml" xmlns:doc="http://cssbox.sourceforge.net/docs" class="section" id="basic"><h2>Basic usage</h2><p>The input of the rendering engine is a document DOM tree. The engine is able to
automatically load the style sheets referenced in the document and it computes the
efficient style of each element. Afterwrads, the document layout is computed.</p><div class="subsection" id="basicLoading"><h3>Document Loading</h3><p>CSSBox generally expects an implementation of the
<a href="http://www.w3.org/DOM/" title="Document Object Model">DOM</a> on its input represented
by its root <code>Document</code> node. The way how the DOM is obtained is not important for CSSBox.
However, in most situations, the DOM is obtained by parsing a HTML or XML file. Therefore, CSSBox provides
a framework for binding a parser to the layout engine. Moreover it contains a default parser implementation
that may be simply used or it can be easily replaced by a custom implementation when required. The default
implementation is based on the <a href="http://nekohtml.sourceforge.net/">NekoHTML</a> parser
and <a href="http://xerces.apache.org/xerces2-j/">Xerces 2</a>. The details about using a different
parser are described in the <a href="#parserCustomization">Custom Document Sources and Parsers</a> section.</p><p>With the default implementation,  the following code reads and parses the document based on its URL:</p><div class="code"><pre>
<em>//Open the network connection</em> 
DocumentSource docSource = new DefaultDocumentSource(urlstring);

<em>//Parse the input document</em>
DOMSource parser = new DefaultDOMSource(docSource);
Document doc = parser.parse(); <em>//doc represents the obtained DOM</em>
</pre></div><p>For the initial DOM and style sheet processing, a
<a href="../api/org/fit/cssbox/css/DOMAnalyzer.html" class="api">DOMAnalyzer</a> object is used. It is
initialized with the DOM tree and the base URL:</p><div class="code"><pre>
DOMAnalyzer da = new DOMAnalyzer(doc, docSource.getURL());
da.attributesToStyles(); <em>//convert the HTML presentation attributes to inline styles</em>
da.addStyleSheet(null, CSSNorm.stdStyleSheet(), DOMAnalyzer.Origin.AGENT); <em>//use the standard style sheet</em>
da.addStyleSheet(null, CSSNorm.userStyleSheet(), DOMAnalyzer.Origin.AGENT); <em>//use the additional style sheet</em>
da.addStyleSheet(null, CSSNorm.formsStyleSheet(), DOMAnalyzer.Origin.AGENT); <em>//(optional) use the forms style sheet</em>
da.getStyleSheets(); <em>//load the author style sheets</em>
</pre></div><p>The <a href="../api/org/fit/cssbox/css/DOMAnalyzer.html#attributesToStyles()" class="api">attributesToStyles()</a>
method converts some HTML presentation attributes to CSS
styles (e.g. the <code>&lt;font&gt;</code> tag attributes, table attributes and some more). If (X)HTML
interpretation is not required, this method need not be called. When used, this method should be called before
<code>getStyleSheets()</code> is used.</p><p>The <a href="../api/org/fit/cssbox/css/DOMAnalyzer.html#addStyleSheet(java.net.URL, java.lang.String, org.fit.cssbox.css.DOMAnalyzer.Origin)" class="api">addStyleSheet()</a> 
method is used to add a style sheet to the document. The style sheet is passed as a text
string containing the CSS code. In our case, we add two built-in style sheets that represent the standard document style. These style
sheets are imported as the user agent style sheets according to the <a href="http://www.w3.org/TR/CSS21/cascade.html#cascading-order">CSS specification</a>.
The <a href="../api/org/fit/cssbox/css/CSSNorm.html#stdStyleSheet()" class="api">CSSNorm.stdStyleSheet()</a>
method returns the default style sheet recommended by the CSS specification and the
<a href="../api/org/fit/cssbox/css/CSSNorm.html#userStyleSheet()" class="api">CSSNorm.userStyleSheet()</a> 
contains some additional CSSBox definitions not covered by the standard.</p><p>Optionally, the <a href="../api/org/fit/cssbox/css/CSSNorm.html#formsStyleSheet()" class="api">CSSNorm.formsStyleSheet()</a>
includes a basic style of form input fields. This style sheet may be used for basic rendering of the form fields when
their rendering and functionality is not implemented in any other way in the application.</p><p>Finally, the <a href="../api/org/fit/cssbox/css/DOMAnalyzer.html#getStyleSheets()" class="api">getStyleSheets()</a>
method loads and processes all the internal and external style sheets referenced from the document including the inline
style definitions. In case of external style sheets, CSSBox tries to obtain
the file from the corresponding URL, if accessible.</p><p>The resulting <a href="../api/org/fit/cssbox/css/DOMAnalyzer.html" class="api">DOMAnalyzer</a> object represents
the document code together with the associated style.</p></div><div class="subsection" id="media"><h3>Media Support</h3><p>By default, the <code>DOMAnalyzer</code> assumes that the page is being rendered on a standard desktop computer
screen. During the style sheet processing, it uses the <code>"screen"</code> media type and some
<a href="http://cssbox.sourceforge.net/jstyleparser/api/cz/vutbr/web/css/MediaSpec.html">default display feature values</a> for
evaluating the possible media queries.</p><p>A different media type or feature values may be specified by creating a new <em>media specification</em> represented
as a <a href="http://cssbox.sourceforge.net/jstyleparser/api/cz/vutbr/web/css/MediaSpec.html">MediaSpec</a> object from the
<a href="http://cssbox.sourceforge.net/jstyleparser/">jStyleParser</a> API. The typical usage would be the following:</p><div class="code"><pre>
<em>//we will use the "screen" media type for rendering</em>
MediaSpec media = new MediaSpec("screen");

<em>//specify some media feature values</em>
media.setDimensions(1000, 800); <em>//set the visible area size in pixels</em>
media.setDeviceDimensions(1600, 1200); <em>//set the display size in pixels</em>

<em>//use the media specification in the analyzer</em>
DOMAnalyzer da = new DOMAnalyzer(doc, docSource.getURL());
da.setMediaSpec(media);
<em>//... continue with the DOMAnalyzer initialization as above</em>
</pre></div><p>The <a href="http://github.com/radkovo/CSSBox/blob/master/src/main/java/org/fit/cssbox/demo/BoxBrowser.java">BoxBrowser</a>
demo shows a basic usage of the media specifications in a Swing application.</p></div><div class="subsection" id="basicLayout"><h3>Obtaining the Layout</h3><p>The whole layout engine is represented by a graphical
<a href="../api/org/fit/cssbox/layout/BrowserCanvas.html" class="api">BrowserCanvas</a> object.
The simplest way of creating the layout is passing the initial viewport dimensions to the BrowserCanvas
constructor. Then, the layout is computed automatically by creating an instance of this object.
The remaining constructor arguments are the root DOM element, the DOMAnalyzer used for obtaining the 
element styles and the document base URL used for loading images and other referenced content.</p><div class="code"><pre>
BrowserCanvas browser = 
        new BrowserCanvas(da.getRoot(),
                          da,
                          new java.awt.Dimension(1000, 600),
                          url);
</pre></div><p>When further browser configuration is required, the BrowserCanvas may be created without specifying
the viewport dimensions. Then, the layout is not computed automatically and it must be created by a subsequent
call of the <a href="../api/org/fit/cssbox/layout/BrowserCanvas.html#createLayout(java.awt.Dimension)" class="api">createLayout</a>
method. Before creating the layout, the browser configuration may be changed.</p><div class="code"><pre>
BrowserCanvas browser = 
        new BrowserCanvas(da.getRoot(), da, url);
<em>//... modify the browser configuration here ...</em>
browser.createLayout(new java.awt.Dimension(1000, 600));
</pre></div><p>Optionally, the <a href="../api/org/fit/cssbox/layout/BrowserCanvas.html#createLayout(java.awt.Dimension, java.awt.Rectangle)" class="api">createLayout</a>
method allows to specify different values for the preferred total canvas size and the visible area size and position] (the CSS viewport size):</p><div class="code"><pre>
BrowserCanvas browser = 
        new BrowserCanvas(da.getRoot(), da, url);
<em>//... modify the browser configuration here ...</em>
browser.createLayout(new java.awt.Dimension(1200, 600), new java.awt.Rectangle(0, 0, 1000, 600));
</pre></div><p>In this case, the preferred size of the resulting page is 1200x600 pixels (it may be adjusted during the layout
computation based on the page contents) and the size of the visible area is 1000x600 pixels; the visible area is
in the top left corner of the rendered page. The visible area size and position is used during the layout computation
and it may influence the positions of positioned elements according to the CSS specification.</p><p>Setting the visible area size automatically updates the used media specification (see the <a href="#media">previous section</a>)
so that the same size of the visible area is used for evaluating the CSS media queries. However, this behavior may be
disabled by calling <a href="../api/org/fit/cssbox/layout/BrowserCanvas.html#setAutoMediaUpdate(boolean)" class="api">setAutoMediaUpdate(false)</a>
before creating the layout. In that case, the visible area size used for the layout computation may be different from
the size used for media queries.</p><p>In all cases, the created <code>browser</code> object can be directly used for both displaying
the rendered document and for obtaining the created layout model. The details of the browser
configuration are described in the <a href="#configOptions">Configuration Options</a> section.</p></div><div class="subsection" id="basicDisplay"><h3>Displaying the document</h3><p>The <a href="../api/org/fit/cssbox/layout/BrowserCanvas.html" class="api">BrowserCanvas</a> class is
directly derived from the Swing <code>javax.swing.JPanel</code> class. Therefore, it can
be directly used as a Swing user interface component. The size of the component is
automatically adjusted according to the resulting document layout. The basic document
displaying is shown in the <a href="../api/org/fit/cssbox/demo/SimpleBrowser.html" class="api">SimpleBrowser</a>
example.</p><p>BrowserCanvas provides a simple display of the rendered page with no interactive elements.
For obtaining an interactive browser component with text selection and clickable links,
the <a href="http://cssbox.sourceforge.net/swingbox/">SwingBox</a> extension should be used.</p></div><div class="subsection" id="configOptions"><h3>Configuration Options</h3><p>Current browser configuration is represented using a
<a href="../api/org/fit/cssbox/layout/BrowserConfig.html" class="api">BrowserConfig</a>
object that may be accessed using the browser's
<a href="../api/org/fit/cssbox/layout/BrowserCanvas.html#getConfig()" class="api">getConfig()</a> method.
The following configuration options are available:</p><dl>
<dt><a href="../api/org/fit/cssbox/layout/BrowserConfig.html#setLoadImages(boolean)" class="api">browser.getConfig().setLoadImages(boolean)</a></dt>
<dd>Configures whether to load the referenced content images automatically. The default value is <code>true</code>.</dd>
<dt><a href="../api/org/fit/cssbox/layout/BrowserConfig.html#setLoadBackgroundImages(boolean)" class="api">browser.getConfig().setLoadBackgroundImages(boolean)</a></dt>
<dd>Configures whether to load the CSS background images automatically. The default value is <code>true</code>.</dd>
<dt><a href="../api/org/fit/cssbox/layout/BrowserConfig.html#setImageLoadTimeout(int)" class="api">browser.getConfig().setImageLoadTimeout(int)</a></dt>
<dd>Configures the timeout for loading images. The default value is 500ms.</dd>
<dt><a href="../api/org/fit/cssbox/layout/BrowserConfig.html#useHTML(boolean)" class="api">browser.getConfig().useHTML(boolean)</a></dt>
<dd>Configures whether the engine should use the HTML extensions or not. Currently, the HTML extensions include the following:
     <ul>
     <li>Creating replaced boxes for <code>&lt;img&gt;</code> elements</li>
     <li>Using the <code>&lt;body&gt;</code> element background for the whole canvas according to the HTML specification</li>
     <li>Support for the embedded <code>&lt;object&gt;</code> elements.</li>
     <li>Special handling of certain elements such as named anchors.</li>
     </ul>
     The default value is <code>true</code>.
     </dd>
<dt><a href="../api/org/fit/cssbox/layout/BrowserConfig.html#setReplaceImagesWithAlt(boolean)" class="api">browser.getConfig().setReplaceImagesWithAlt(boolean)</a></dt>
<dd>Sets whether the images should be replaced by their <code>alt</code> text.</dd>
<dt><a href="../api/org/fit/cssbox/layout/BrowserConfig.html#setDefaultFont(java.lang.String,%20java.lang.String)" class="api">browser.getConfig().setDefaultFont(String logical, String physical)</a></dt>
<dd>Configures the default physical fonts that should be used instead of the logical Java families. The typical usage is the following:
	<div class="code"><pre>
        browser.getConfig().setDefaultFont(java.awt.Font.SERIF, "Times New Roman");
        browser.getConfig().setDefaultFont(java.awt.Font.SANS_SERIF, "Arial");
        browser.getConfig().setDefaultFont(java.awt.Font.MONOSPACED, "Courier New");
	</pre></div>
</dd>
<dt><a href="../api/org/fit/cssbox/layout/BrowserConfig.html#registerDocumentSource(java.lang.Class)" class="api">browser.getConfig().registerDocumentSource(Class&lt;? extends DocumentSource&gt;)</a></dt>
<dt><a href="../api/org/fit/cssbox/layout/BrowserConfig.html#registerDOMSource(java.lang.Class)" class="api">browser.getConfig().registerDOMSource(Class&lt;? extends DOMSource&gt;)</a></dt>
<dd>Register the DocumentSource and DOMSource implementation used for automatic loading of the referenced documents.
See the <a href="#parserCustomization">Custom Document Sources and Parsers</a> section for details.</dd>
</dl></div><div class="subsection" id="parserCustomization"><h3>Custom Document Sources and Parsers</h3><p>CSSBox contains two generic abstract classes that represent the document source and the parser and provides their
default implementations:</p><ul>
<li><a href="../api/org/fit/cssbox/io/DocumentSource.html" class="api">DocumentSource</a> represents a generic source of documents
 that is able to obtain a document based on its URL. The default
 <a href="../api/org/fit/cssbox/io/DefaultDocumentSource.html" class="api">DefaultDocumentSource</a> implementation uses the standard Java
 <code>URLConnection</code> mechanism extended by the support of <code>data:</code> URL scheme.</li>
<li><a href="../api/org/fit/cssbox/io/DOMSource.html" class="api">DOMSource</a> represents a parser that is able to create a DOM
 from a document source. The default
 <a href="../api/org/fit/cssbox/io/DefaultDOMSource.html" class="api">DefaultDOMSource</a> implementation is based on the
 <a href="http://nekohtml.sourceforge.net/">NekoHTML</a> parser.</li>
</ul><p>The default implementations may be used for obtaining the DOM from an URL easily as shown in the
<a href="#basicLoading">Document Loading</a> section. Moreover, CSSBox uses these implementations for obtaining
 the documents referenced from the HTML code such as images and embedded objects.</p><p>When a different implementation of the document source or the parser is required (e.g. for obtaining the documents
from a non-standard source, using a different parser implementation, etc.), it is possible to create a custom implementation
of the appropriate abstract class. Then, the new implementation may be registered using the browser configuration
<a href="../api/org/fit/cssbox/layout/BrowserConfig.html#registerDocumentSource(java.lang.Class)" class="api">browser.getConfig().registerDocumentSource()</a>
and
<a href="../api/org/fit/cssbox/layout/BrowserConfig.html#registerDOMSource(java.lang.Class)" class="api">browser.getConfig().registerDOMSource()</a>
methods as mentioned above in the <a href="#configOptions">Configuration Options</a> section.
</p></div></div><div xmlns="http://www.w3.org/1999/xhtml" xmlns:doc="http://cssbox.sourceforge.net/docs" class="section" id="model"><h2>Rendered Document Model</h2><p>The resulting document layout is represented as a tree of <dfn>boxes</dfn>. Each box
creates a rectangular area in the resulting page and it corresponds to a particular
rendered HTML element. There may be multiple boxes corresponding to a single element;
e.g. a multi-line paragraph <code>&lt;p&gt;</code> is split to several line boxes. A box may be
either composed from child boxes or it may correspond to a particular part of the
document content, which may be a text string or replaced content (e.g. images).</p><p>Each box is represented by an object which extends the
<a href="../api/org/fit/cssbox/layout/Box.html" class="api">Box</a> abstract class. There exist
several box types that roughly correspond to the computed value of the CSS <code>display</code>
property for the corresponding element. Figure 1 shows the type hierarchy of boxes.</p><div class="figure"><img src="box_classes.png" alt="Box type hierarchy"/><p>Figure 1: Box type hierarchy</p></div><p>The root node of the box tree is always represented by a
<a href="../api/org/fit/cssbox/layout/Viewport.html" class="api">Viewport</a> object and it represents
the browser viewport. It has always a single child which is called a <dfn>root box</dfn>.
The root box corresponds to the root element of the HTML code passed to the
<a href="../api/org/fit/cssbox/layout/BrowserCanvas.html" class="api">BrowserCanvas</a> for rendering.
Usually, it is the <code>&lt;body&gt;</code> element.</p><p>The viewport and the root box are obtained using the
<a href="../api/org/fit/cssbox/layout/BrowserCanvas.html#getViewport()" class="api">getViewport()</a> and
<a href="../api/org/fit/cssbox/layout/BrowserCanvas.html#getRootBox()" class="api">getRootBox()</a> methods
of the BrowserCanvas.</p><p>In the following chapters, we will mention the most important methods that
can be used for obtaining information about the resulting document layout. For
other methods, see the <a href="../api/index.html" class="api">CSSBox API reference</a>.</p><div class="subsection" id="modelBox"><h3>Basic Box Properties</h3><p>The basic box properties are defined in the <a href="../api/org/fit/cssbox/layout/Box.html" class="api">Box</a>
abstract class. They are mostly related to the box position and size.</p><div class="subsubsection" id="boxPositions"><h4>Box Position and Size</h4><p>During the layout, the box position is first computed relatively to the containing box and
	in the next step, the absolute position in the page is computed. The box occupies a rectangular
	area in the page which includes the box contents, borders and margins. The contents of the box
	is always inside of this area and it is again a rectangle which is equal or smaller than the whole box.
	Following methods can be used for obtaining the positions and sizes:</p><dl>
		<dt><a href="../api/org/fit/cssbox/layout/Box.html#getAbsoluteBounds()" class="api">getAbsoluteBounds()</a></dt>
		<dd>Returns the absolute box position and size on the page including all the borders
		and margins. The result is the <code>java.awt.Rectangle</code> object.</dd>
		<dt><a href="../api/org/fit/cssbox/layout/Box.html#getBounds()" class="api">getBounds()</a></dt>
		<dd>Returns the box position and size relatively to the top-left corner of the containing block.</dd>
		<dt><a href="../api/org/fit/cssbox/layout/Box.html#getAbsoluteContentX()" class="api">getAbsoluteContentX()</a></dt>
		<dd>The absolute X coordinate of the top left corner of the box contents.</dd>
		<dt><a href="../api/org/fit/cssbox/layout/Box.html#getAbsoluteContentY()" class="api">getAbsoluteContentY()</a></dt>
		<dd>The absolute Y coordinate of the top left corner of the box contents.</dd>
		<dt><a href="../api/org/fit/cssbox/layout/Box.html#getContentX()" class="api">getContentX()</a></dt>
		<dd>The absolute X coordinate of the top left corner of the box contents relatively to the containing block.</dd>
		<dt><a href="../api/org/fit/cssbox/layout/Box.html#getContentY()" class="api">getContentY()</a></dt>
		<dd>The absolute Y coordinate of the top left corner of the box contents relatively to the containing block.</dd>
		<dt><a href="../api/org/fit/cssbox/layout/Box.html#getContentWidth()" class="api">getContentWidth()</a></dt>
		<dd>The width of the box content without any margins and borders.</dd>
		<dt><a href="../api/org/fit/cssbox/layout/Box.html#getContentHeight()" class="api">getContentHeight()</a></dt>
		<dd>The height of the box content without any margins and borders.</dd>
		<dt><a href="../api/org/fit/cssbox/layout/Box.html" class="api"/></dt>
		<dd/>
	</dl></div><div class="subsubsection" id="boxStructure"><h4>Box Tree Structure</h4><dl>
		<dt><a href="../api/org/fit/cssbox/layout/Box.html#getParent()" class="api">getParent()</a></dt>
		<dd>Returns the parent box of this box or <code>null</code> if this is the root box.</dd>
		<dt><a href="../api/org/fit/cssbox/layout/Box.html#getContainingBlock()" class="api">getContainingBlock()</a></dt>
		<dd>Returns the containing block for this box.</dd>
		<dt><a href="../api/org/fit/cssbox/layout/Box.html#getViewport()" class="api">getViewport()</a></dt>
		<dd>The corresponding <a href="../api/org/fit/cssbox/layout/Viewport.html" class="api">Viewport</a>
		object (the root of the box tree).</dd>
		<dt><a href="../api/org/fit/cssbox/layout/Box.html#getNode()" class="api">getNode()</a></dt>
		<dd>The DOM node this box corresponds to. There may be multiple boxes
		corresponding to a single DOM node.</dd>
	</dl></div></div><div class="subsection" id="modelText"><h3>Text Boxes</h3><p>A text box always box corresponds to a DOM node of the type <code>Text</code>. It is represented
as <a href="../api/org/fit/cssbox/layout/TextBox.html" class="api">TextBox</a> object. When the text is split to
several lines, multiple boxes correspond to a single DOM node. In this case each of them contains
a corresponding substring of the text.</p><div class="subsubsection" id="textText"><h4>Obtaining the Text Content</h4><dl>
		<dt><a href="../api/org/fit/cssbox/layout/TextBox.html#getText()" class="api">getText()</a></dt>
		<dd>Returns the text string corresponding to this node.</dd>
	</dl></div></div><div class="subsection" id="modelElement"><h3>Element Boxes</h3><p>An element box corresponds to a DOM node of the type <code>Element</code>. It is represented
as an object of the abstract <a href="../api/org/fit/cssbox/layout/ElementBox.html" class="api">ElementBox</a>
class. It can be implemented as an inline box or a block box as described below. The common
methods of the element boxes are the following:</p><div class="subsubsection" id="elementStructure"><h4>Box Tree Structure</h4><p>Each element box may contain any number of nested child boxes that are indexed 
	from <em>0</em> to <em>n</em>. When there exist multiple element
	boxes that correspond to a single DOM node, all these boxes share all the child boxes. 
	The child boxes that belong to a particular element box can be determined using
	their index - the <a href="../api/org/fit/cssbox/layout/ElementBox.html#getStartChild()" class="api">getStartChild()</a>
	and the <a href="../api/org/fit/cssbox/layout/ElementBox.html#getEndChild()" class="api">getEndChild()</a>
	methods give the first and the last index of the child boxes that belong to this particular element box.
	Therefore, the normal way of processing all the child boxes of an element box <code>b</code> is following: </p><div class="code"><pre>
for (int i = b.getStartChild(); i &lt; b.getEndChild(); i++)
{
    Box sub = b.getSubBox(i);
    <em>//process the child box here...</em>
}
</pre></div><p>The overview of the related ElementBox methods follows:</p><dl>
		<dt><a href="../api/org/fit/cssbox/layout/ElementBox.html#getStartChild()" class="api">getStartChild()</a></dt>
		<dd>Returns the index of the first child box contained in this element box.</dd>
		<dt><a href="../api/org/fit/cssbox/layout/ElementBox.html#getEndChild()" class="api">getEndChild()</a></dt>
		<dd>Returns the index of the first child box <strong>not</strong> contained in this element box.</dd>
		<dt><a href="../api/org/fit/cssbox/layout/ElementBox.html#getSubBox(int)" class="api">getSubBox(int)</a></dt>
		<dd>Returns the child box with the given index.</dd>
		<dt><a href="../api/org/fit/cssbox/layout/ElementBox.html#getSubBoxNumber()" class="api">getSubBoxNumber()</a></dt>
		<dd>The number of child boxes in this box.</dd>
		<dt><a href="../api/org/fit/cssbox/layout/ElementBox.html#getElement()" class="api">getElement()</a></dt>
		<dd>The corresponding DOM Element.</dd>
	</dl></div><div class="subsubsection" id="elementModel"><h4>Box Sizes</h4><p>The dimensions of an element box are modelled according to the
	<a href="http://www.w3.org/TR/CSS21/box.html">CSS Box Model</a>. In addition
	to the <em>content size</em> discussed in the <a href="#boxPositions"><code>Box
	interface description</code></a>, it consists of <em>padding</em>, 
	<em>border width</em> and <em>margin</em>. All these dimensions are represented
	by objects of a special <a href="../api/org/fit/cssbox/layout/LengthSet.html" class="api">LengthSet</a>
	class that contain the <code>top</code>, <code>left</code>, <code>bottom</code> and
	<code>right</code> values of the dimension.</p><p>There are two values of <em>margin</em> available: A computed value of the margin
	that corresponds to the specified style and an efficient margin value (<em>emargin</em>)
	that considers the margin collapsing and it is used during the layout.</p><p>Following methods can be used for obtaining the individual values:</p><dl>
		<dt><a href="../api/org/fit/cssbox/layout/ElementBox.html#getPadding()" class="api">getPadding()</a></dt>
		<dd>Returns the padding sizes.</dd>
		<dt><a href="../api/org/fit/cssbox/layout/ElementBox.html#getBorder()" class="api">getBorder()</a></dt>
		<dd>The border sizes.</dd>
		<dt><a href="../api/org/fit/cssbox/layout/ElementBox.html#getMargin()" class="api">getMargin()</a></dt>
		<dd>The computed margin sizes.</dd>
		<dt><a href="../api/org/fit/cssbox/layout/ElementBox.html#getEMargin()" class="api">getEMargin()</a></dt>
		<dd>The efficient margin sizes used during the layout with margin collapsing.</dd>
	</dl></div><div class="subsubsection" id="elementVisual"><h4>Visual Properties of the Element Box</h4><dl>
		<dt><a href="../api/org/fit/cssbox/layout/ElementBox.html#getDisplay()" class="api">getDisplay()</a></dt>
		<dd>Represents the value of the <code>display:</code> CSS property. The returned
		value is one of the following constants defined in the
		<a href="../api/org/fit/cssbox/layout/ElementBox.html" class="api">ElementBox</a> class:
			<code>DISPLAY_ANY</code>,
			<code>DISPLAY_BLOCK</code>,
			<code>DISPLAY_INLINE</code>,
			<code>DISPLAY_INLINE_BLOCK</code>,
			<code>DISPLAY_INLINE_TABLE</code>,
			<code>DISPLAY_LIST_ITEM</code>,
			<code>DISPLAY_NONE</code>,
			<code>DISPLAY_RUN_IN</code>,
			<code>DISPLAY_TABLE</code>,
			<code>DISPLAY_TABLE_CAPTION</code>,
			<code>DISPLAY_TABLE_CELL</code>,
			<code>DISPLAY_TABLE_COLUMN</code>,
			<code>DISPLAY_TABLE_COLUMN_GROUP</code>,
			<code>DISPLAY_TABLE_FOOTER_GROUP</code>,
			<code>DISPLAY_TABLE_HEADER_GROUP</code>,
			<code>DISPLAY_TABLE_ROW</code>,
			<code>DISPLAY_TABLE_ROW_GROUP</code>.
		</dd>
		<dt><a href="../api/org/fit/cssbox/layout/ElementBox.html#getBgcolor()" class="api">getBgcolor()</a></dt>
		<dd>The background color of the element or null when transparent.</dd>
		<dt><a href="../api/org/fit/cssbox/layout/ElementBox.html" class="api"/></dt>
		<dd/>
		<dt><a href="../api/org/fit/cssbox/layout/ElementBox.html" class="api"/></dt>
		<dd/>
	</dl></div></div><div class="subsection" id="modelInline"><h3>Inline Boxes</h3><p>Inline boxes are the <a href="#modelElement">element boxes</a> with value
of the CSS <code>display:</code> property equal to <code>inline</code>. These boxes are
represented as the <a href="../api/org/fit/cssbox/layout/InlineBox.html" class="api">InlineBox</a> objects.
They have no special properties in addition to the properties defined in the
<a href="../api/org/fit/cssbox/layout/ElementBox.html" class="api">ElementBox</a> class.</p></div><div class="subsection" id="modelBlock"><h3>Block Boxes</h3><p>Block boxes, with the <code>display:</code> value set to <code>block</code>
are represented by the 
<a href="../api/org/fit/cssbox/layout/BlockBox.html" class="api">BlockBox</a> objects. 
In addition, the boxes with other values of the <code>display:</code> property
are represented by several special classes derived from this class:</p><ul>
	<li><a href="../api/org/fit/cssbox/layout/ListItemBox.html" class="api">ListItemBox</a></li>
	<li><a href="../api/org/fit/cssbox/layout/TableBox.html" class="api">TableBox</a></li>
	<li><a href="../api/org/fit/cssbox/layout/TableCaptionBox.html" class="api">TableCaptionBox</a></li>
	<li><a href="../api/org/fit/cssbox/layout/TableBodyBox.html" class="api">TableBodyBox</a></li>
	<li><a href="../api/org/fit/cssbox/layout/TableRowBox.html" class="api">TableRowBox</a></li>
	<li><a href="../api/org/fit/cssbox/layout/Viewport.html" class="api">Viewport</a></li>
</ul><p>These objects differ mainly in the way how the boxes and their contents are laid out
on the page. From the resulting layout model point of view, they have no special
properties in addition to the properties defined in the
<a href="../api/org/fit/cssbox/layout/ElementBox.html" class="api">ElementBox</a> class.</p></div><div class="subsection" id="modelColors"><h3>Fonts and Colors</h3><p>For each box, a <a href="../api/org/fit/cssbox/layout/VisualContext.html" class="api">VisualContext</a>
object is defined that gathers the information about the current text font and color. This 
object is obtained using the
<a href="../api/org/fit/cssbox/layout/Box.html#getVisualContext()" class="api">getVisualContext()</a>
method of the box. Following methods can be used for obtaining the appropriate information:</p><dl>
	<dt><a href="../api/org/fit/cssbox/layout/VisualContext.html#getColor()" class="api">getColor()</a></dt>
	<dd>Current text color.</dd>
	<dt><a href="../api/org/fit/cssbox/layout/VisualContext.html#getFont()" class="api">getFont()</a></dt>
	<dd>Returns the current font represented as the <code>java.awt.Font</code> object.
	    From this object most of the font properties can be obtained.</dd>
	<dt><a href="../api/org/fit/cssbox/layout/VisualContext.html#getFontVariant()" class="api">getFontVariant()</a></dt>
	<dd>Current font variant in the CSS syntax - i.e. <code>normal</code> or <code>small-caps</code>.</dd>
	<dt><a href="../api/org/fit/cssbox/layout/VisualContext.html#getTextDecoration()" class="api">getTextDecoration()</a></dt>
	<dd>Current text decoration in the CSS syntax - i.e. <code>none</code>, <code>underline</code>, 
	    <code>overline</code>, <code>line-through</code> or <code>blink</code>.</dd>
</dl><p>The background color is only applicable to <a href="#modelElement">element boxes</a>
as mentioned in <a href="#elementVisual">Visual Properties of the Element Box</a>.</p></div></div><div xmlns="http://www.w3.org/1999/xhtml" xmlns:doc="http://cssbox.sourceforge.net/docs" class="section" id="feedback"><h2>Feedback</h2><p>The CSSBox library and this manual are under development. Any feedback is welcome
mainly via the forums and the bug tracker available via the
<a href="http://www.sourceforge.net/projects/cssbox">project page</a>. We will be very
happy if you want to contribute to the code. In this case, please contact the author. 
</p></div><?
		make_footer();
		?>