<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:exsl="http://exslt.org/common" exclude-result-prefixes="exsl">
    <xsl:import href="to_commons.xsl"/>
    <xsl:output method="html" omit-xml-declaration="yes" indent="yes"/>

    <!-- parameters -->
    <xsl:param name="extension"/>
    <xsl:param name="info-file"/>
    <xsl:param name="temp-dir"/>

    <!-- table of languages with their file associations -->
    <xsl:template match="/">
        <table>
            <xsl:call-template name="to-model">
                <xsl:with-param name="extension" select="$extension"/>
                <xsl:with-param name="info-file" select="$info-file"/>
                <xsl:with-param name="temp-dir" select="$temp-dir"/>
            </xsl:call-template>
        </table>
    </xsl:template>
    <xsl:template match="language[not(@ignore) and not(../*[@issues and not(@ignore)])]" mode="model">
        <tr>
            <td>
                <xsl:choose>
                    <xsl:when test="@href">
                        <a href="{@href}">
                            <xsl:value-of select="@name"/>
                        </a>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="@name"/>
                    </xsl:otherwise>
                </xsl:choose>
            </td>
            <td><xsl:value-of select="@file-associations"/></td>
        </tr>
    </xsl:template>

</xsl:stylesheet>