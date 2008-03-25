<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:doc="http://cssbox.sourceforge.net/docs"
	xmlns="http://www.w3.org/1999/xhtml">
	
    <xsl:output method="xml" 
    			indent="no"
    			encoding="utf-8"
    			doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
    			doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"/>
 
	<xsl:template match="doc:document">
		<html>
		<head>
			<title><xsl:value-of select="doc:title" /></title>
			<meta http-equiv="content-type" content="text/html; charset=utf-8" />
			<style type="text/css">
			<xsl:text>
			@import "manual.css";
			</xsl:text>
			</style>
		</head>
		<body>
		<h1><xsl:apply-templates select="doc:title" /></h1>
		<p class="author"><xsl:apply-templates select="doc:author" /></p>
		<xsl:apply-templates select="doc:section"/>
		</body>
		</html>
	</xsl:template>

	<xsl:template match="doc:title">
		<xsl:value-of select="." />
	</xsl:template>

	<xsl:template match="doc:author">
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="doc:section">
		<div class="section">
			<h2><xsl:apply-templates select="doc:title" /></h2>
			<xsl:apply-templates select="*[name()!='title']" />
		</div>
	</xsl:template>

	<xsl:template match="doc:subsection">
		<div class="subsection">
			<h3><xsl:apply-templates select="doc:title" /></h3>
			<xsl:apply-templates select="*[name()!='title']" />
		</div>
	</xsl:template>
    
    <xsl:template match="doc:codebox">
    	<div class="code">
    		<xsl:apply-templates />
    	</div>
    </xsl:template>
    
    <xsl:template match="doc:p">
    	<p>
    		<xsl:apply-templates />
    	</p>
    </xsl:template>
    
    <xsl:template match="doc:api">
    	<a>
    		<xsl:attribute name="href">
    			<xsl:text>../api/</xsl:text>
    			<xsl:value-of select="@class" />
    			<xsl:text>.html</xsl:text>
    		</xsl:attribute>
    		<xsl:attribute name="class">api</xsl:attribute>
    		<xsl:apply-templates />
    	</a>
    </xsl:template>
    
    <xsl:template match="doc:tag">
    	<code>
    		<xsl:text>&lt;</xsl:text>
    			<xsl:value-of select="." />
    		<xsl:text>&gt;</xsl:text>
    	</code>
    </xsl:template>
    
    <xsl:template match="*">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match = "@*" >
           <xsl:copy />
    </xsl:template>
    
</xsl:stylesheet>
