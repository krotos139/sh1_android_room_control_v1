<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>

<xsl:template match='/'>
  <html>
  <head>
	  <title>Room sensors</title>
	  <link rel="stylesheet" type="text/css" href="/style.css"/>
  </head>
  <body>
  <h2>Sensors</h2>
    <table class='z1'>
      <tr>
        <th>Property</th>
        <th>Value</th>
      </tr>
      <tr>
        <td> Temperature </td>
        <td><xsl:value-of select='response/temperature/celsius'/> C</td>
      </tr>
      <tr>
        <td> Humidity </td>
        <td><xsl:value-of select='response/humidity/percentage'/> %</td>
      </tr>
      <tr>
        <td> Current </td>
        <td><xsl:value-of select='response/current/A'/> A</td>
      </tr>
      <tr>
        <td> Gas (MQ-2 sensor) </td>
        <td><xsl:value-of select='response/MQ2/percentage'/> %</td>
      </tr>
      <tr>
        <td> PIR sensor </td>
        <td><xsl:value-of select='response/PIR/percentage'/> %</td>
      </tr>
    </table>
	<h2>Outputs</h2>
    <table class='z1'>
      <tr>
        <th>Relay</th>
        <th>Output</th>
      </tr>
	  <xsl:for-each select='response/outputs/relays/relay'>
      <tr>
        <td> <xsl:value-of select='@name'/> </td>
        <td><xsl:value-of select='.'/> <xsl:value-of select='@unit'/></td>
      </tr>
	  </xsl:for-each>
    </table>
  </body>
  </html>
</xsl:template>
</xsl:stylesheet>

