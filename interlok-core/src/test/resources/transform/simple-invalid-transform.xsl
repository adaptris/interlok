<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
  xmlns:java="http://xml.apache.org/xslt/java" exclude-result-prefixes="java">
  
  <xsl:param name="world"/>
  
  <xsl:output method="text"/>
  
  <xsl:template match="/">  
    <xsl:apply-templates select="//payload"/>
  </xsl:template>


  <!-- //payload will match multiple templates. This will generate a recoverable error. -->  
  <xsl:template match="payload[text() = 'Hello']"/>
  <xsl:template match="payload[normalize-space(text()) = 'Hello']"/>
   
</xsl:stylesheet>
