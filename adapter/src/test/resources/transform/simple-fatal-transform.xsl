<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
  xmlns:java="http://xml.apache.org/xslt/java" exclude-result-prefixes="java">
  
  <xsl:param name="world"/>
  
  <xsl:output method="text"/>
  
  <xsl:template match="/">  
    <someelement>
      <child/>
      <!-- Trying to create an attribute after children have been added -->
      <xsl:attribute name="attr">value</xsl:attribute>
    </someelement>
  </xsl:template>

   
</xsl:stylesheet>
