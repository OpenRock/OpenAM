<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:web="http://java.sun.com/xml/ns/j2ee">

    <xsl:template match="@* | node()">
        <xsl:choose>
            <xsl:when test="local-name(.)='servlet'">
                <xsl:call-template name="servlet"/>
            </xsl:when>
            <xsl:when test="local-name(.)='servlet-mapping'">
                <xsl:call-template name="servlet-mapping"/>
            </xsl:when>
            <xsl:when test="local-name(.)='context-param'">
                <xsl:call-template name="context-param"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy>
                    <xsl:apply-templates select="@*|node()"/>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="non-console">
        <xsl:param name="checkText"/>
        <xsl:choose>
            <xsl:when test="not(contains($checkText, 'com.sun.identity.console'))">
                <xsl:copy-of select="."/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message>
                    Excluding <xsl:value-of select="name(.)"/> item: <xsl:value-of select="$checkText"/>
                </xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="context-param">
        <xsl:call-template name="non-console">
            <xsl:with-param name="checkText" select="./web:param-name/text()"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="servlet">
        <xsl:call-template name="non-console">
            <xsl:with-param name="checkText" select="./web:servlet-class/text()"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="servlet-mapping">
        <xsl:variable name="sName" select="./web:servlet-name/text()"/>
        <xsl:call-template name="non-console">
            <xsl:with-param name="checkText" select="//web:servlet[web:servlet-name/text() = $sName]/web:servlet-class/text()"/>
        </xsl:call-template>
    </xsl:template>

</xsl:stylesheet>