<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- Template to match the root element -->
  <xsl:template match="/">

    <!-- Output a processing instruction -->
    <?xml-stylesheet type="text/css" href="style.css"?>

    <!-- Apply templates to child elements -->
    <xsl:apply-templates/>

    <!-- Call a named template -->
    <xsl:call-template name="customTemplate"/>

  </xsl:template>

  <!-- Template to match specific elements -->
  <xsl:template match="greeting">
    <h1>
      <xsl:value-of select="."/>
    </h1>
  </xsl:template>

  <!-- Template with parameters -->
  <xsl:template name="customTemplate">
    <xsl:param name="param1" select="'Default'"/>
    <p>
      Parameter: <xsl:value-of select="$param1"/>
    </p>
  </xsl:template>

  <!-- Conditional template -->
  <xsl:template match="person[@gender='female']">
    <p>
      Female person: <xsl:value-of select="@name"/>
    </p>
  </xsl:template>

  <!-- Iterative template -->
  <xsl:template match="fruits">
    <ul>
      <xsl:for-each select="fruit">
        <li>
          <xsl:value-of select="."/>
        </li>
      </xsl:for-each>
    </ul>
  </xsl:template>

</xsl:stylesheet>
