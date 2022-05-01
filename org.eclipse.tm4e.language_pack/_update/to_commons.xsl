<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:exsl="http://exslt.org/common" exclude-result-prefixes="exsl">

    <!-- transform to a model of an <extension> element containing <language> and <grammar> elements
         and applying this model with mode="model":
         <extension>
             <language id="..." name="..." ...(more attributes) />
             ...(more languages)
             <grammar scope="..." file="..." ...(more attributes) />
             ...(more grammars)
         </extension> -->
    <xsl:template name="to-model">
        <xsl:param name="extension"/>
        <xsl:param name="info-file"/>
        <xsl:param name="temp-dir"/>
        <xsl:variable name="languages">
            <extension>

                <!-- languages -->
                <xsl:for-each select="o/o[@name='contributes']/a[@name='languages']/o">
                    <xsl:variable name="language-id" select="e[@name='id']/@string"/>
                    <xsl:variable name="grammars-count"
                        select="count(/o/o[@name='contributes']/a[@name='grammars']
                                          /o/e[@name='language'][@string=$language-id])"/>
                    <xsl:if test="    $grammars-count > 0
                                  and a[@name='extensions' or @name='filenames' or @name='filenamePatterns']">
                        <xsl:variable name="original-name" select="a[@name='aliases']/v[1]/@string"/>
                        <xsl:variable name="language-info" select="document($info-file)/info/extension[@id = $extension]
                                                                       /language[@name = $original-name]"/>

                        <!-- <language name="..." file-associations="..." -->
                        <xsl:variable name="name">
                            <xsl:choose>
                                <xsl:when test="$language-info[@better-name]">
                                    <xsl:value-of select="$language-info/@better-name"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="$original-name"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <xsl:variable name="file-associations">
                            <xsl:for-each select="a[@name='extensions']/v">
                                <xsl:value-of select="concat(', *', @string)"/>
                            </xsl:for-each>
                            <xsl:for-each select="a[@name='filenames' or @name='filenamePatterns']/v">
                                <xsl:value-of select="concat(', ', @string)"/>
                            </xsl:for-each>
                        </xsl:variable>
                        <language id="{$language-id}" name="{$name}"
                            file-associations="{substring($file-associations, 3)}">

                            <!-- icon="..." -->
                            <xsl:if test="$language-info/@icon">
                                <xsl:attribute name="icon">
                                    <xsl:value-of select="concat($extension, '/', $language-info/@icon)"/>
                                </xsl:attribute>
                            </xsl:if>

                            <!-- href="..." -->
                            <xsl:if test="not(document(concat($temp-dir, e[@name='id']/@string, '_href.xml'))/href/@text = '')">
                                <xsl:attribute name="href">
                                    <xsl:call-template name="encode">
                                        <xsl:with-param name="path"
                                            select="document(concat($temp-dir, e[@name='id']/@string, '_href.xml'))/href/text()"/>
                                    </xsl:call-template>
                                </xsl:attribute>
                            </xsl:if>

                            <!-- file-extensions="..." -->
                            <xsl:variable name="file-extensions">
                                <xsl:for-each select="a[@name='extensions']/v">
                                    <xsl:if test="    substring(@string, 1, 1) = '.'
                                                  and translate(@string, '.', '') = substring(@string, 2)">
                                        <xsl:value-of select="concat(',', substring(@string, 2))"/>
                                    </xsl:if>
                                </xsl:for-each>
                            </xsl:variable>
                            <xsl:if test="string-length($file-extensions) > 0">
                                <xsl:attribute name="file-extensions">
                                    <xsl:value-of select="substring($file-extensions, 2)"/>
                                </xsl:attribute>
                            </xsl:if>

                            <!-- file-names="..." -->
                            <xsl:variable name="file-names">
                                <xsl:for-each select="a[@name='filenames']/v">
                                    <xsl:value-of select="concat(',', @string)"/>
                                </xsl:for-each>
                            </xsl:variable>
                            <xsl:if test="string-length($file-names) > 0">
                                <xsl:attribute name="file-names">
                                    <xsl:value-of select="substring($file-names, 2)"/>
                                </xsl:attribute>
                            </xsl:if>

                            <!-- file-patterns="..." -->
                            <xsl:variable name="file-patterns">
                                <xsl:for-each select="a[@name='extensions']/v">
                                    <xsl:if test="   not(substring(@string, 1, 1) = '.')
                                                  or not(translate(@string, '.', '') = substring(@string, 2))">
                                        <xsl:value-of select="concat(',*', @string, 2)"/>
                                    </xsl:if>
                                </xsl:for-each>
                                <xsl:for-each select="a[@name='filenamePatterns']/v">
                                    <xsl:value-of select="concat(',', @string)"/>
                                </xsl:for-each>
                            </xsl:variable>
                            <xsl:if test="string-length($file-patterns) > 0">
                                <xsl:attribute name="file-patterns">
                                    <xsl:value-of select="substring($file-patterns, 2)"/>
                                </xsl:attribute>
                            </xsl:if>

                            <!-- configuration-file="..." configuration-path="..." -->
                            <xsl:if test="e[@name='configuration']/@string">
                                <xsl:variable name="path" select="e[@name='configuration']/@string"/>
                                <xsl:attribute name="configuration-file">
                                    <xsl:value-of select="concat($extension, '/')"/>
                                    <xsl:call-template name="basename">
                                        <xsl:with-param name="path" select="$path"/>
                                    </xsl:call-template>
                                </xsl:attribute>
                                <xsl:attribute name="configuration-path">
                                    <xsl:value-of select="e[@name='configuration']/@string"/>
                                </xsl:attribute>
                                <xsl:attribute name="duplicate">
                                    <xsl:value-of
                                        select="count(preceding-sibling::o/e[@name='configuration'][@string = $path])"/>
                                </xsl:attribute>
                            </xsl:if>

                            <!-- issues="..." -->
                            <xsl:if test="$language-info/@issues">
                                <xsl:attribute name="issues">
                                    <xsl:value-of select="$language-info/@issues"/>
                                </xsl:attribute>
                            </xsl:if>

                            <!-- ignore="..." -->
                            <xsl:if test="$language-info/@ignore">
                                <xsl:attribute name="ignore">
                                    <xsl:value-of select="$language-info/@ignore"/>
                                </xsl:attribute>
                            </xsl:if>

                        </language>
                    </xsl:if>
                </xsl:for-each>

                <!-- grammars -->
                <xsl:for-each select="o/o[@name='contributes']/a[@name='grammars']
                                          /o[e/@name='scopeName'][e/@name='path']">

                    <!-- <grammar scope="..." file="..." path="..." -->
                    <xsl:variable name="path" select="e[@name='path']/@string"/>
                    <xsl:variable name="basename">
                        <xsl:call-template name="basename">
                             <xsl:with-param name="path" select="$path"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <grammar scope="{e[@name='scopeName']/@string}" file="{$extension}/{$basename}"
                        duplicate="{count(preceding-sibling::o/e[@name='path'][@string = $path])}"
                        path="{e[@name='path']/@string}">

                        <!-- original-href="..." -->
                        <xsl:if test="not(document(concat($temp-dir, $basename, '_original.xml'))/original/text()='')">
                            <xsl:attribute name="original-href">
                                <xsl:value-of select="document(concat($temp-dir, $basename, '_original.xml'))
                                                          /original/text()"/>
                            </xsl:attribute>
                        </xsl:if>

                        <!-- language="..." -->
                        <xsl:if test="e[@name='language']">
                            <xsl:attribute name="language">
                                <xsl:value-of select="e[@name='language']/@string"/>
                            </xsl:attribute>
                        </xsl:if>

                    </grammar>
                </xsl:for-each>

            </extension>
        </xsl:variable>
        <xsl:apply-templates select="exsl:node-set($languages)" mode="model"/>
    </xsl:template>

    <!-- basename -->
    <xsl:template name="basename">
        <xsl:param name="path"/>
        <xsl:choose>
            <xsl:when test="translate($path, '/', '') = $path">
                <xsl:value-of select="$path"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="basename">
                     <xsl:with-param name="path" select="substring-after($path, '/')"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- encode -->
    <xsl:template name="encode">
        <xsl:param name="path"/>
        <xsl:choose>
            <xsl:when test="string-length($path) = 0">
                <xsl:value-of select="$path"/>
            </xsl:when>
            <xsl:when test="substring($path, 1, 1) = ' '">
                <xsl:text>%20</xsl:text>
                <xsl:call-template name="encode">
                     <xsl:with-param name="path" select="substring($path, 2)"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="substring($path, 1, 1) = '('">
                <xsl:text>%28</xsl:text>
                <xsl:call-template name="encode">
                     <xsl:with-param name="path" select="substring($path, 2)"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="substring($path, 1, 1) = ')'">
                <xsl:text>%29</xsl:text>
                <xsl:call-template name="encode">
                     <xsl:with-param name="path" select="substring($path, 2)"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="substring($path, 1, 1)"/>
                <xsl:call-template name="encode">
                     <xsl:with-param name="path" select="substring($path, 2)"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- decode -->
    <xsl:template name="decode">
        <xsl:param name="path"/>
        <xsl:choose>
            <xsl:when test="string-length($path) = 0">
                <xsl:value-of select="$path"/>
            </xsl:when>
            <xsl:when test="substring($path, 1, 3) = '%20'">
                <xsl:text> </xsl:text>
                <xsl:call-template name="decode">
                     <xsl:with-param name="path" select="substring($path, 4)"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="substring($path, 1, 1)"/>
                <xsl:call-template name="decode">
                     <xsl:with-param name="path" select="substring($path, 2)"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>