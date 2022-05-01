<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:import href="to_commons.xsl"/>
    <xsl:output method="xml" indent="yes"/>

    <!-- parameters -->
    <xsl:param name="extension"/>
    <xsl:param name="base-url"/>
    <xsl:param name="info-file"/>
    <xsl:param name="icons-dir"/>

    <!-- Wikidata link query -->
    <xsl:variable name="wikidata-link-query"
        select="'https://www.wikidata.org/w/api.php?action=wbgetentities&amp;format=xml&amp;props=sitelinks&amp;sitefilter=enwiki&amp;ids='"/>

    <!-- process specified extension -->
    <xsl:template match="/">
        <project default="process-extension">
            <target name="process-extension">

                <!-- language configurations -->
                <xsl:for-each select="o/o[@name='contributes']/a[@name='languages']/o">
                    <xsl:variable name="language-id" select="e[@name='id']/@string"/>
                    <xsl:variable name="language-grammars-count"
                        select="count(/o/o[@name='contributes']/a[@name='grammars']/o/e[@name='language'][@string=$language-id])"/>
                    <xsl:if test="e[@name='configuration'] and $language-grammars-count > 0">
                        <xsl:variable name="basename">
                            <xsl:call-template name="basename">
                                 <xsl:with-param name="path" select="e[@name='configuration']/@string"/>
                            </xsl:call-template>
                        </xsl:variable>
                        <mkdir dir="${{project.build.directory}}/generated/${{extension}}"/>
                        <xsl:variable name="encoded-path">
                            <xsl:call-template name="encode">
                                <xsl:with-param name="path" select="e[@name='configuration']/@string"/>
                            </xsl:call-template>
                        </xsl:variable>
                        <get src="{$base-url}/{$encoded-path}"
                            dest="${{project.build.directory}}/generated_temp/${{extension}}/{$basename}"/>
                        <copy file="${{project.build.directory}}/generated_temp/${{extension}}/{$basename}"
                            todir="${{project.build.directory}}/generated/${{extension}}" encoding="UTF-8">
                            <filterchain>
                                <replaceregex flags="g" pattern="//[^&quot;\r\n]*+(?=$$|[\r\n])" replace=""/>
                                <replaceregex byline="no" flags="g"
                                    pattern="([&quot;\}}\]])\s*+,(?=\s*+[\}}\]]([\s\{{\}}\[\]:,]|&lt;[/]?[oaev](?:\s(?:name|string|number)=&quot;(?:[^&quot;\\]|\\.)*+&quot;)*+[/]?>|(&quot;([^&quot;\\]|\\.)*+&quot;)|([+-]?\d++(?:\.\d++)?(?:[eE][+-]?\d++)?))*+$$)"
                                    replace="\1"/>

                                <!-- not yet supported: { "pattern": "...", flags: "..." } -->
                                <replaceregex byline="no" flags="g"
                                    pattern="\{{\s*+&quot;pattern&quot;\s*+:\s*+(&quot;(?:[^&quot;\\]|\\.)*+&quot;)\s*+(?:,\s*+&quot;flags&quot;\s*+:\s*+&quot;[^&quot;]*+&quot;\s*+)?\}}"
                                    replace="\1"/>

                                <!-- not yet supported: "onEnterRules": [ ... ] -->
                                <replaceregex byline="no"
                                    pattern=",\s*+&quot;onEnterRules&quot;\s*+:\s*+\[([\s\{{\}}\[:,]|(&quot;([^&quot;\\]|\\.)*+&quot;)|([+-]?\d++(?:\.\d++)?(?:[eE][+-]?\d++)?))*+\s*+\]"
                                    replace=""/>

                            </filterchain>
                        </copy>
                    </xsl:if>
                    <xsl:if test="$language-grammars-count > 0">
                        <xsl:variable name="language-name" select="a[@name='aliases']/v[1]/@string"/>
                        <xsl:variable name="language-info" select="document($info-file)/info/extension[@id = $extension]
                                                                       /language[@name = $language-name]"/>
                        <echo file="${{project.build.directory}}/generated_temp/${{extension}}/{$language-id}_href.xml">
                            <xsl:attribute name="message">
                                <xsl:text>&lt;href></xsl:text>
                                <xsl:choose>
                                    <xsl:when test="$language-info/@wikidata">
                                        <xsl:text>https://en.wikipedia.org/wiki/</xsl:text>
                                        <xsl:value-of
                                            select="document(concat($wikidata-link-query, $language-info/@wikidata))
                                                        /api/entities/entity/sitelinks/sitelink/@title"/>
                                    </xsl:when>
                                    <xsl:when test="$language-info[not(@wikidata)]/related[1][@wikidata]">
                                        <xsl:text>https://en.wikipedia.org/wiki/</xsl:text>
                                        <xsl:value-of
                                            select="document(concat($wikidata-link-query,
                                                                    $language-info/related[1]/@wikidata))
                                                        /api/entities/entity/sitelinks/sitelink/@title"/>
                                    </xsl:when>
                                    <xsl:when test="$language-info[not(@wikidata)]/related[1][@link]">
                                        <xsl:value-of select="$language-info/related[1]/@link"/>
                                    </xsl:when>
                                </xsl:choose>
                                <xsl:text>&lt;/href></xsl:text>
                            </xsl:attribute>
                        </echo>
                        <xsl:if test="$language-info/@icon">
                            <mkdir dir="${{project.build.directory}}/generated/${{extension}}"/>
                            <copy todir="${{project.build.directory}}/generated/${{extension}}">
                                <fileset dir="{$icons-dir}" includes="*.png"/>
                            </copy>
                        </xsl:if>
                    </xsl:if>
                </xsl:for-each>

                <!-- grammars -->
                <xsl:for-each select="o/o[@name='contributes']/a[@name='grammars']/o[e/@name='scopeName'][e/@name='path']">
                    <mkdir dir="${{project.build.directory}}/generated/${{extension}}"/>
                    <xsl:variable name="basename">
                        <xsl:call-template name="basename">
                             <xsl:with-param name="path" select="e[@name='path']/@string"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:variable name="encoded-path">
                        <xsl:call-template name="encode">
                            <xsl:with-param name="path" select="e[@name='path']/@string"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <get src="{$base-url}/{$encoded-path}"
                        dest="${{project.build.directory}}/generated_temp/${{extension}}/{$basename}"/>
                    <copy file="${{project.build.directory}}/generated_temp/${{extension}}/{$basename}"
                        tofile="${{project.build.directory}}/generated_temp/${{extension}}/{$basename}.xml"
                        encoding="UTF-8">
                        <filterchain refid="json-to-xml"/>
                    </copy>
                    <copy file="${{project.build.directory}}/generated_temp/${{extension}}/{$basename}"
                        tofile="${{project.build.directory}}/generated_temp/${{extension}}/{$basename}_original.xml"
                        encoding="UTF-8">
                        <filterchain>
                            <replaceregex flags="g" byline="no"
                                pattern="^\s*+\{{\s*+&quot;information_for_contributors&quot;\s*+:\s*+\[\s*+&quot;This file (?:(?:has been converted)|(?:includes some grammar rules copied)) from ([^&quot;]++)&quot;[\s\S]++$$"
                                replace="&lt;original>\1&lt;/original>"/>
                            <replaceregex flags="g" byline="no"
                                pattern="^(?!&lt;original>)[\s\S]++$$"
                                replace="&lt;original>&lt;/original>"/>
                        </filterchain>
                    </copy>
                    <copy file="${{project.build.directory}}/generated_temp/${{extension}}/{$basename}"
                        todir="${{project.build.directory}}/generated/${{extension}}" encoding="UTF-8">
                        <filterchain>
                            <replaceregex flags="g"
                                pattern="&quot;(?:scopeName|include)&quot;\s*+:\s*+&quot;(?![#\$])(?=[^&quot;]++&quot;)"
                                replace="\0lngpck."/>
                        </filterchain>
                    </copy>
                </xsl:for-each>

            </target>
        </project>
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

</xsl:stylesheet>