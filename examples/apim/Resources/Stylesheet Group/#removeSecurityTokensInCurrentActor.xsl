<?xml version="1.0" ?>
<xsl:stylesheet exclude-result-prefixes="wssedec2002 wssejuly2002 wsu2002 wsse2003 wsu2003 wsse10 wsse11 wsu10 dsig"
                xmlns:wssedec2002="http://schemas.xmlsoap.org/ws/2002/12/secext"
                xmlns:wssejuly2002="http://schemas.xmlsoap.org/ws/2002/07/secext"
                xmlns:wsu2002="http://schemas.xmlsoap.org/ws/2002/07/utility"
                xmlns:wsse2003="http://schemas.xmlsoap.org/ws/2003/06/secext"
                xmlns:wsu2003="http://schemas.xmlsoap.org/ws/2003/06/utility"
                xmlns:wsse10="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"
                xmlns:wsse11="http://docs.oasis-open.org/wss/oasis-wss-wssecurity-secext-1.1.xsd"
                xmlns:wsu10="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"
                xmlns:dsig="http://www.w3.org/2000/09/xmldsig#"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                xmlns:soap12="http://www.w3.org/2003/05/soap-envelope" >

  <xsl:output method="xml" />

  <!-- Omit all ws-sec Security blocks from output for SOAP 1.1 -->
  <xsl:template match="soap:Header/wssedec2002:Security[@soap:actor=false()]" />
  <xsl:template match="soap:Header/wssejuly2002:Security[@soap:actor=false()]" />
  <xsl:template match="soap:Header/wsse2003:Security[@soap:actor=false()]" />
  <xsl:template match="soap:Header/wsse10:Security[@soap:actor=false()]" />
  <xsl:template match="soap:Header/wsse11:Security[@soap:actor=false()]"/>

  <!-- Omit all ws-sec Security blocks from output for SOAP 1.2 -->
  <xsl:template match="soap12:Header/wssedec2002:Security[@soap12:role=false()]" />
  <xsl:template match="soap12:Header/wssejuly2002:Security[@soap12:role=false()]" />
  <xsl:template match="soap12:Header/wsse2003:Security[@soap12:role=false()]" />
  <xsl:template match="soap12:Header/wsse10:Security[@soap12:role=false()]" />
  <xsl:template match="soap12:Header/wsse11:Security[@soap12:role=false()]"/>

  <!-- identity transformation, copy everything -->
  <xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
