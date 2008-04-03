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
		<xsl:apply-templates select="doc:toc"/>
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

	<xsl:template match="doc:toc">
		<div class="toc">
			<h2>Table of Contents</h2>
			<ul>
				<xsl:for-each select="../doc:section">
					<li>
						<a>
							<xsl:attribute name="href">
								<xsl:text>#</xsl:text>
								<xsl:value-of select="@id" />
							</xsl:attribute>
							<xsl:value-of select="doc:title" />
						</a>
						<xsl:if test="count(doc:subsection)>0">
							<ul>
								<xsl:for-each select="doc:subsection">
									<li>
										<a>
											<xsl:attribute name="href">
												<xsl:text>#</xsl:text>
												<xsl:value-of select="@id" />
											</xsl:attribute>
											<xsl:value-of select="doc:title" />
										</a>
									</li>
								</xsl:for-each>
							</ul>
						</xsl:if>
					</li>
				</xsl:for-each>
			</ul>
		</div>
	</xsl:template>

	<xsl:template match="doc:section">
		<div class="section">
			<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
			<h2><xsl:apply-templates select="doc:title" /></h2>
			<xsl:apply-templates select="*[local-name()!='title']" />
		</div>
	</xsl:template>

	<xsl:template match="doc:subsection">
		<div class="subsection">
			<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
			<h3><xsl:apply-templates select="doc:title" /></h3>
			<xsl:apply-templates select="*[local-name()!='title']" />
		</div>
	</xsl:template>
    
	<xsl:template match="doc:subsubsection">
		<div class="subsubsection">
			<xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
			<h4><xsl:apply-templates select="doc:title" /></h4>
			<xsl:apply-templates select="*[local-name()!='title']" />
		</div>
	</xsl:template>
    
    <xsl:template match="doc:codebox">
    	<div class="code">
    		<pre>
    			<xsl:apply-templates />
    		</pre>
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
    			<xsl:choose>
    				<xsl:when test="string-length(@class)>0">
		    			<xsl:value-of select="@class" />
		    			<xsl:text>.html</xsl:text>
    				</xsl:when>
    				<xsl:otherwise>
    					<xsl:text>index.html</xsl:text>
    				</xsl:otherwise>
    			</xsl:choose>
    			<xsl:if test="string-length(@anchor)>0">
    				<xsl:text>#</xsl:text>
    				<xsl:value-of select="@anchor" />
    			</xsl:if>
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
    
    <xsl:template match="doc:ref">
    	<a>
    		<xsl:attribute name="href">
    			<xsl:text>#</xsl:text>
    			<xsl:value-of select="@target" />
    		</xsl:attribute>
  			<xsl:apply-templates />
    	</a>
    </xsl:template>
    
    <xsl:template match="doc:fig">
    	<div class="figure">
    		<img>
    			<xsl:attribute name="src"><xsl:value-of select="@src" /></xsl:attribute>
    			<xsl:attribute name="alt"><xsl:value-of select="@title" /></xsl:attribute>
    		</img>
    		<p>
    			<xsl:text>Figure </xsl:text>
    			<xsl:value-of select="@index" />
    			<xsl:text>: </xsl:text>
    			<xsl:value-of select="@title" />
    		</p>
    	</div>
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
