<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:exsl="http://exslt.org/common" exclude-result-prefixes="exsl">
    <xsl:import href="to_commons.xsl"/>
    <xsl:output method="html" omit-xml-declaration="yes" indent="yes"/>

    <!-- parameters -->
    <xsl:param name="extension"/>
    <xsl:param name="extension-href"/>
    <xsl:param name="info-file"/>
    <xsl:param name="temp-dir"/>

    <!-- create about.html snippet -->
    <xsl:template match="/">
        <table>
            <xsl:call-template name="to-model">
                <xsl:with-param name="extension" select="$extension"/>
                <xsl:with-param name="info-file" select="$info-file"/>
                <xsl:with-param name="temp-dir" select="$temp-dir"/>
            </xsl:call-template>
        </table>
    </xsl:template>
    <xsl:template match="extension" mode="model">
        <xsl:key name="k" match="language" use="concat(@configuration-file, @file)"/>
        <xsl:for-each select="*[@duplicate = 0]">
            <tr>
                <td><xsl:value-of select="concat(@configuration-file, @file)"/></td>
                <xsl:if test="position() = 1">
                    <td>
                        <xsl:if test="count(../*[@duplicate = 0]) > 1">
                            <xsl:attribute name="rowspan">
                                <xsl:value-of select="count(../*[@duplicate = 0])"/>
                            </xsl:attribute>
                        </xsl:if>
                        <a href="{$extension-href}" target="_blank"><xsl:value-of select="$extension"/></a>
                    </td>
                </xsl:if>
                <td>
                    <a href="{$extension-href}/{@configuration-path}{@path}" target="_blank">
                        <xsl:call-template name="basename">
                            <xsl:with-param name="path" select="concat(@configuration-path, @path)"/>
                        </xsl:call-template>
                    </a>
                </td>
                <td>
                    <xsl:choose>
                        <xsl:when test="@original-href">
                            <a href="{@original-href}">
                                <xsl:call-template name="decode">
                                    <xsl:with-param name="path">
                                        <xsl:call-template name="basename">
                                            <xsl:with-param name="path" select="@original-href"/>
                                        </xsl:call-template>
                                    </xsl:with-param>
                                </xsl:call-template>
                            </a>
                        </xsl:when>
                        <xsl:when test="name() = 'language'"><xsl:text>-</xsl:text></xsl:when>
                    </xsl:choose>
                </td>
            </tr>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>