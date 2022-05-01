<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:exsl="http://exslt.org/common" exclude-result-prefixes="exsl">
    <xsl:import href="to_commons.xsl"/>
    <xsl:output method="xml" indent="yes"/>

    <!-- parameters -->
    <xsl:param name="extension"/>
    <xsl:param name="info-file"/>
    <xsl:param name="temp-dir"/>

    <!-- create plugin.xml snippet -->
    <xsl:template match="/">
        <lngpck-snippet>
            <xsl:call-template name="to-model">
                <xsl:with-param name="extension" select="$extension"/>
                <xsl:with-param name="info-file" select="$info-file"/>
                <xsl:with-param name="temp-dir" select="$temp-dir"/>
            </xsl:call-template>
        </lngpck-snippet>
    </xsl:template>
    <xsl:template match="extension" mode="model">
        <xsl:comment><xsl:value-of select="concat(' ', $extension, ' ')"/></xsl:comment>

        <!-- issues? -->
        <xsl:if test="*[not(@ignore)]/@issues">
            <xsl:comment>
                <xsl:text> issues: </xsl:text>
                <xsl:for-each select="*[@issues]">
                    <xsl:if test="position() > 1"><xsl:text>; </xsl:text></xsl:if>
                    <xsl:value-of select="@issues"/>
                </xsl:for-each>
                <xsl:text> >>></xsl:text>
            </xsl:comment>
        </xsl:if>

        <!-- languages -->
        <xsl:if test="language[not(@ignore)]">

            <!-- content types -->
            <extension point="org.eclipse.core.contenttype.contentTypes">
                <xsl:for-each select="language[not(@ignore)]">
                    <content-type id="lng.{@id}"
                        base-type="org.eclipse.tm4e.language_pack.basetype"
                        name="{@name} (Syntax Highlighting)"
                        priority="low">
                        <xsl:if test="@file-extensions">
                            <xsl:attribute name="file-extensions">
                                <xsl:value-of select="@file-extensions"/>
                            </xsl:attribute>
                        </xsl:if>
                        <xsl:if test="@file-names">
                            <xsl:attribute name="file-names">
                                <xsl:value-of select="@file-names"/>
                            </xsl:attribute>
                        </xsl:if>
                        <xsl:if test="@file-patterns">
                            <xsl:attribute name="file-patterns">
                                <xsl:value-of select="@file-patterns"/>
                            </xsl:attribute>
                        </xsl:if>

                    </content-type>
                </xsl:for-each>
            </extension>

            <!-- language configurations -->
            <xsl:if test="language/@configuration-file">
                <extension point="org.eclipse.tm4e.languageconfiguration.languageConfigurations">
                    <xsl:for-each select="language[not(@ignore)][@configuration-file]">
                        <languageConfiguration contentTypeId="lng.{@id}" path="{@configuration-file}"/>
                    </xsl:for-each>
                </extension>
            </xsl:if>

            <!-- editor icons -->
            <xsl:if test="language[not(@ignore)][@icon]">
                <extension point="org.eclipse.ui.genericeditor.icons">
                    <xsl:for-each select="language[not(@ignore)][@icon]">
                        <icon contentType="lng.{@id}" icon="{@icon}"/>
                    </xsl:for-each>
                </extension>
            </xsl:if>

        </xsl:if>

        <!-- grammars -->
        <xsl:if test="grammar and *[not(@ignore)]">
            <extension point="org.eclipse.tm4e.registry.grammars">
                <xsl:for-each select="grammar">
                    <grammar scopeName="lngpck.{@scope}" path="{@file}"/>
                    <xsl:if test="@language">
                        <scopeNameContentTypeBinding scopeName="lngpck.{@scope}" contentTypeId="lng.{@language}"/>
                    </xsl:if>
                </xsl:for-each>
            </extension>
        </xsl:if>

        <!-- issues end marker -->
        <xsl:if test="*[not(@ignore)]/@issues">
            <xsl:comment><xsl:text>///</xsl:text></xsl:comment>
        </xsl:if>

    </xsl:template>

</xsl:stylesheet>